package com.cesoft.cesrunner.data.groq

import android.util.Log
import com.cesoft.cesrunner.data.local.AppDatabase
import com.cesoft.cesrunner.data.location.LocationDataSource
import com.cesoft.cesrunner.data.prefs.PrefDataSource
import com.cesoft.cesrunner.domain.AppError
import com.cesoft.cesrunner.domain.entity.AIAgentRes
import com.cesoft.cesrunner.domain.entity.LocationDto
import com.cesoft.cesrunner.domain.entity.RunDto
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import com.google.gson.reflect.TypeToken
import io.github.vyfor.groqkt.GroqClient
import io.github.vyfor.groqkt.GroqModel
import io.github.vyfor.groqkt.api.GroqResponse
import io.github.vyfor.groqkt.api.chat.ChatCompletion
import io.github.vyfor.groqkt.api.chat.CompletionToolCall

//NOTE: https://github.com/vyfor/groq-kt
class Groq(
    private val db: AppDatabase,
    private val location: LocationDataSource,
    private val prefs: PrefDataSource,
) {
    private var client: Pair<String, GroqClient>? = null

    /// The key is entered by the user in settings, so the client is created on demand
    private suspend fun getClient(): GroqClient? {
        val key = prefs.readApiKeys().getOrNull()?.groq.orEmpty()
        if(key.isBlank()) return null
        client?.let { (cachedKey, cachedClient) -> if(cachedKey == key) return cachedClient }
        return GroqClient(key).also { client = key to it }
    }

    /// Result of one LLM call: the answer so far and the tools the LLM wants to call
    private data class Answer(
        val result: Result<AIAgentRes>,
        val toolCalls: List<CompletionToolCall> = listOf(),
    )

    suspend fun chat(prompt: String): Result<AIAgentRes> {
        val client = getClient() ?: return Result.failure(AppError.MissingApiKey)

        var answer = process(
            request(client, prompt, system = SYSTEM, withTools = true)
        )
        var avoidInfiniteLoop = 0
        while(answer.toolCalls.isNotEmpty() && avoidInfiniteLoop < 3) {
            avoidInfiniteLoop++

            val toolCall = answer.toolCalls.first()
            val toolName = toolCall.function.name
            // The LLM may hallucinate a tool name that doesn't exist
            val tool = GroqToolType.entries.firstOrNull { it.name == toolName }
                ?: return Result.failure(AppError.NotFound)
            val runs = processTool(tool, toolCall.function.arguments).getOrElse {
                return Result.failure(AppError.NotFound)
            }

            // Call the LLM again with the tool response
            val runsJson = Gson().toJson(runs)
            Log.d(TAG, "chat: $tool -> ${runs.size} runs")
            answer = process(
                request(client, prompt, system = "$SYSTEM Choose the answer from the following runs: $runsJson")
            )
            if(answer.toolCalls.firstOrNull()?.function?.name == toolName) {
                // The LLM asks for the same tool again: answer with the data we already have
                answer = Answer(Result.success(AIAgentRes(runsJson, runs)))
            }
        }
        return answer.result
    }

    private suspend fun request(
        client: GroqClient,
        prompt: String,
        system: String,
        withTools: Boolean = false,
    ): Result<GroqResponse<ChatCompletion>> =
        try {
            client.chat {
                model = MODEL
                seed = SEED
                temperature = TEMPERATURE
                // json mode cannot be combined with tool/function calling
                if(withTools) tools = GroqTools.tools
                messages {
                    system(system)
                    text(prompt)
                }
            }
        }
        catch(e: Exception) {
            Log.e(TAG, "request:e: $e")
            Result.failure(e)
        }

    private suspend fun getAllRuns(): List<RunDto> {
        val userLocation = location.getLastKnownLocation()?.let { LocationDto(it.latitude, it.longitude) }
        return db.trackDao().getAll().map { track ->
            val firstPoint = db.trackPointDao().getFirstByTrackId(track.id)
            val runLocation = firstPoint?.let { LocationDto(it.latitude, it.longitude) }
            val distanceToLocation =
                if(userLocation != null && runLocation != null) userLocation.distanceTo(runLocation).toInt()
                else 0
            track.toModelDto(
                distanceToLocation = distanceToLocation,
                latitude = runLocation?.latitude ?: 0.0,
                longitude = runLocation?.longitude ?: 0.0,
            )
        }
    }

    private suspend fun processTool(
        tool: GroqToolType,
        params: String
    ): Result<List<RunDto>> {
        try {
            val obj: RunDto =
                try { Gson().fromJson(params, RunDto::class.java) ?: RunDto() }
                catch(e: Exception) { RunDto() }
            val runs = when(tool) {
                GroqToolType.GetLongest -> listOfNotNull(getAllRuns().maxByOrNull { it.distance })
                GroqToolType.GetShortest -> listOfNotNull(getAllRuns().minByOrNull { it.distance })
                GroqToolType.GetNear -> {
                    val distance = if(obj.distanceToLocation > 0) obj.distanceToLocation else 100
                    getAllRuns().filter { it.distanceToLocation < distance }
                }
                GroqToolType.GetAll -> return Result.success(getAllRuns())
            }
            return if(runs.isNotEmpty()) Result.success(runs) else Result.failure(AppError.NotFound)
        }
        catch(e: Throwable) {
            Log.e(TAG, "processTool:$tool:e: $e")
            return Result.failure(e)
        }
    }

    private fun process(response: Result<GroqResponse<ChatCompletion>>): Answer {
        val data = response.getOrElse { return Answer(Result.failure(it)) }.data
        var answer = Answer(Result.failure(AppError.NotFound))
        for(choice in data.choices) {
            val msg = choice.message.content ?: ""
            val toolCalls = choice.message.toolCalls ?: listOf()
            //finish=tool_calls / content=La carrera más corta es... / tool=GetShortest
            // -> It should be finishReason=stop, but LLM wanna call tool again ??!!
            answer = when {
                msg.isNotEmpty() -> Answer(Result.success(AIAgentRes(msg, parseRuns(msg))), toolCalls)
                choice.finishReason == "tool_calls" && toolCalls.isNotEmpty() ->
                    Answer(Result.success(AIAgentRes(Gson().toJson(toolCalls))), toolCalls)
                else -> Answer(Result.failure(AppError.NotFound), toolCalls)
            }
        }
        return answer
    }

    companion object {
        private const val TAG = "Groq"
        private const val CES_JSON_KEY = "\"cesjson\":"

        /// Extracts the list of runs the LLM returns inside the "cesjson" object of its answer
        internal fun parseRuns(msg: String): List<RunDto> {
            val i = msg.indexOf(CES_JSON_KEY)
            if(i < 0) return listOf()
            val j = msg.indexOf("]", i)
            if(j < 0) return listOf()
            val json = msg.substring(i + CES_JSON_KEY.length, j + 1)
            return try {
                val typeOfT = object: TypeToken<List<RunDto>>(){}.type
                GsonBuilder()
                    .setStrictness(Strictness.LENIENT)
                    .create()
                    .fromJson<List<RunDto>>(json, typeOfT) ?: listOf()
            }
            catch(e: Exception) {
                listOf()
            }
        }
        const val SEED = 10
        const val TEMPERATURE = 0.2
        private val MODEL = GroqModel.LLAMA_4_SCOUT_17B_16E_INSTRUCT
        private const val SYSTEM =
            "You are a helpful assistant that answers questions about the runs." +
            " Return a json of the list of runs you have selected as response;" +
            " compose a json list even if the run selected is just one;" +
            " the root of the list must be and object named 'cesjson'."+
            " The runs have the following fields:" +
            " id (long, the identification number of the run);" +
            " name (string, how the user calls the run);" +
            " distance (integer, the number of meters between the starting point and the final point of the run);" +
            " distanceToLocation (integer, the number of meters between the run and the user current location);" +
            " timeIni (the date and time when the user start running);" +
            " timeEnd (the date and time when the user finish running);" +
            " time (long, milliseconds spent in the run);" +
            " vo2Max (double, the VO2Max, precalculated measure of the user fitness in this run);" +
            " latitude (double, the approximate latitude of the run location);"+
            " longitude (double, the approximate longitude of the run location)."
    }
}
