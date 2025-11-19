package com.cesoft.cesrunner.data.groq

import android.location.Location
import android.util.Log
import com.cesoft.cesrunner.data.BuildConfig
import com.cesoft.cesrunner.data.groq.GroqRunDto.Companion.toRun
import com.cesoft.cesrunner.data.local.AppDatabase
import com.cesoft.cesrunner.data.local.entity.LocalTrackDto
import com.cesoft.cesrunner.data.local.entity.LocalTrackPointDto
import com.cesoft.cesrunner.data.location.LocationDataSource
import com.cesoft.cesrunner.domain.AppError
import com.cesoft.cesrunner.domain.entity.TrackDto
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.github.vyfor.groqkt.GroqClient
import io.github.vyfor.groqkt.GroqModel
import io.github.vyfor.groqkt.api.GroqResponse
import io.github.vyfor.groqkt.api.chat.ChatCompletion
import io.github.vyfor.groqkt.api.chat.CompletionToolCall

//NOTE: https://github.com/vyfor/groq-kt
class Groq(
    private val db: AppDatabase,
    private val location: LocationDataSource
) {
    //TODO: Thread safety
    private var toolCall: List<CompletionToolCall> = listOf()
    private var result: Result<String> = Result.failure(Exception())
    private val client = GroqClient(BuildConfig.GROQ_KEY)

    suspend fun chat(prompt: String): Result<String> {
        toolCall = listOf()
        result = Result.failure(Exception())

        Log.e(TAG, "chat-----------------------00000 prompt = $prompt")
        Log.wtf(TAG, "chat-----000")
        var response: Result<GroqResponse<ChatCompletion>>
        try {
            response = client.chat {
                model = MODEL
                seed = SEED
                temperature = TEMPERATURE
                tools = GroqTools.tools

                // json mode cannot be combined with tool/function calling
                //responseFormat = CompletionResponseFormat(CompletionResponseFormatType.JSON_OBJECT)

                /*var frequencyPenalty: Double? = null
            var functionCall: CompletionFunctionCallType? = null
            var functions: MutableList<CompletionFunction>? = null
            var maxTokens: Int? = null
            var n: Int? = null
            var parallelToolCalls: Boolean? = null
            var presencePenalty: Double? = null
            var responseFormat: CompletionResponseFormat? = null
            var seed: Int? = null
            var stop: MutableList<String>? = null
            var stream: Boolean? = null
            var streamOptions: CompletionStreamOptions? = null
            var temperature: Double? = null
            var toolChoice: CompletionToolChoice? = null
            var topP: Double? = null
            var user: String? = null*/
                messages {
                    system(SYSTEM)
                    text(prompt)
                }
            }
        }
        catch (e: Exception) {
            response = Result.failure(e)
            Log.wtf(TAG, "chat-----e:$e")
            /*chat-----e:io.ktor.client.call.NoTransformationFoundException: Expected response body of the type 'class io.github.vyfor.groqkt.api.GroqResponseType' but was 'class io.ktor.utils.io.SourceByteReadChannel'
            In response from `https://api.groq.com/openai/v1/chat/completions`
                    Response status `500 Internal Server Error`
            Response header `ContentType: text/html; charset=UTF-8`
            Request header `Accept: application/json`

            You can read how to resolve NoTransformationFoundException at FAQ:
            https://ktor.io/docs/faq.html#no-transformation-found-exception*/
        }
        Log.wtf(TAG, "chat-----001")
        Log.e(TAG, "chat-----------------------data = ${response.getOrNull()} / e= ${response.exceptionOrNull()}")
        Log.e(TAG, "chat-----------------------msg  = ${response.getOrNull()?.data?.choices?.firstOrNull()?.message?.content}")
        Log.e(TAG, "chat-----------------------tool  = ${response.getOrNull()?.data?.choices?.firstOrNull()?.message?.toolCalls?.firstOrNull()?.function}")

        process(response)
        var avoidInfiniteLoop = 0
        while(toolCall.isNotEmpty() && avoidInfiniteLoop < 3) {
            avoidInfiniteLoop++

            Log.e(TAG, "while(toolCall.isNotEmpty())-----------------------#tool ${toolCall.size}")
            val toolObj: CompletionToolCall = toolCall.first()
            val toolName = toolObj.function.name
            val tool = enumValueOf<GroqToolType>(toolName)
            val params = toolObj.function.arguments
            Log.e(TAG, "while(toolCall.isNotEmpty())-----------------------tool $tool($params)")
            val runs = processTool(tool, params)
            Log.e(TAG, "while(toolCall.isNotEmpty())-----------------------result: #${runs.getOrNull()?.size} / first.name= ${runs.getOrNull()?.first()?.name}")
            for(i in runs.getOrNull() ?: listOf())Log.e(TAG, "while(toolCall.isNotEmpty())----------------------- i = ${i.id}")
            Log.e(TAG, "while(toolCall.isNotEmpty())-----------------------result: json=" + Gson().toJson(runs))
            toolCall = listOf()

            // Call the LLM again with the tool response
            if(runs.isFailure) {
                result = Result.failure(Exception("I couldn't find the requested data"))
                return result
            }
            val runsJson = Gson().toJson(runs)
            Log.e(TAG, "while(toolCall.isNotEmpty())-----------------------runs = $runsJson / $runs")
            //val system1 = ". Choose the run o runs from the following list $runsJson"
            val systemData = " Choose the answer from the following runs: $runsJson"
            Log.e(TAG, "while(toolCall.isNotEmpty())-----------------------prompt= $systemData")
            response = client.chat {
                model = MODEL
                seed = SEED
                temperature = TEMPERATURE
                //tools = GroqTools.tools
                messages {
                    system(SYSTEM + systemData)
                    text(prompt)
                }
            }
            Log.e(TAG, "while(toolCall.isNotEmpty())--------999--------------- response = $response")
            process(response)
            Log.e(TAG, "while(toolCall.isNotEmpty())--------999--------------- ${toolCall.firstOrNull()?.function?.name} == $toolName")
            if(toolCall.firstOrNull()?.function?.name == toolName) {
                Log.e(TAG, "while(toolCall.isNotEmpty())--------REPEATED TOOL-------- $runsJson ")
                result = Result.success(runsJson)
                toolCall = listOf()
            }
            Log.e(TAG, "while(toolCall.isNotEmpty())--------END--------------- ${toolCall.size} / ${result.getOrNull()}")
        }
        return result
    }

    private suspend fun getAllRuns_(): List<GroqRunDto> {
        val tracks: List<LocalTrackDto> = db.trackDao().getAll()
        return tracks.map { it.toRun() }
    }
    private suspend fun getAllRuns(): List<GroqRunDto> {
        val locUsr = location.getLastKnownLocation()
        val tracks: List<LocalTrackDto> = db.trackDao().getAll()
        val runs = mutableListOf<GroqRunDto>()
        for(track in tracks) {
            val trackPoints: List<LocalTrackPointDto> = db.trackPointDao().getByTrackId(track.id)
            val trackPoint = trackPoints.firstOrNull()
            val points = if(trackPoint!=null) listOf(trackPoint) else listOf()

            var distanceToLocation = 0
            if(locUsr != null && trackPoint != null) {
                val locRun = Location("")
                locRun.latitude = trackPoint.latitude
                locRun.longitude = trackPoint.longitude
                distanceToLocation = locUsr.distanceTo(locRun).toInt()
            }

            runs.add(track.toRun(points, distanceToLocation))
        }
        return runs
    }
    private suspend fun getAllRunsComplete(): List<GroqRunDto> {
        val tracks: List<LocalTrackDto> = db.trackDao().getAll()
        val runs = mutableListOf<GroqRunDto>()
        for(track in tracks) {
            val trackPoints: List<LocalTrackPointDto> = db.trackPointDao().getByTrackId(track.id)
            runs.add(track.toRun(trackPoints))
        }
        return runs
    }

    private suspend fun processTool(
        tool: GroqToolType,
        params: String
    ): Result<List<GroqRunDto>> {
        try {
            val obj: GroqRunDto =
                try {
                    Gson().fromJson(params, GroqRunDto::class.java)
                }
                catch (e: Exception) {
                    Log.e("AA", "-------- param:e = $e / $params")
                    GroqRunDto.EMPTY
                }
            Log.e("AA", "-------- param = $obj")
/*
            if(tracks.isEmpty()) return Result.failure(AppError.NotFound)
            val all = tracks.map {
                //val points = db.trackPointDao().getByTrackId(it.id)
                val points = listOf<LocalTrackPointDto>()
                it.toModel(points)
            }*/
            Log.e(TAG, "processTool----------000------- tool = $tool")
            return when(tool) {
                GroqToolType.GetLongest -> {
                    val run = getAllRuns().maxByOrNull { it.distance }
                    Log.e(TAG, "processTool-$tool----------------------run= ${run?.name}")
                    if(run != null) Result.success(listOf(run))
                    else Result.failure(AppError.NotFound)
                }
                GroqToolType.GetShortest -> {
                    val run = getAllRuns().minByOrNull { it.distance }
                    Log.e(TAG, "processTool-$tool---------------------- ${run?.name}")
                    if(run != null) Result.success(listOf(run))
                    else Result.failure(AppError.NotFound)
                }
                GroqToolType.GetNear -> {
                    /*val i = params.indexOf("distanceToLocation\":")
                    val j = params.lastIndexOf("\"}")
                    Log.e(TAG, "processTool-$tool---------------------- $params $i $j")
                    val distance = if(i in 1..< j-22) {
                        try { params.substring(i + 21, j).toInt() }
                        catch (e: Exception) {
                            Log.e(TAG, "processTool-$tool---------------------- $e")
                            100
                        }
                    } else null*/
                    val distance = if(obj.distanceToLocation > 0) obj.distanceToLocation else 100
                    Log.e(TAG, "processTool-$tool---------------------- d = $distance")
                    val runs = getAllRuns().filter { it.distanceToLocation < distance }
                    Log.e(TAG, "processTool-$tool---------------------- # = ${runs.size}")
                    if(runs.isNotEmpty()) Result.success(runs)
                    else Result.failure(AppError.NotFound)
                }
                GroqToolType.GetAll -> {
                    Log.e(TAG, "processTool-$tool---------------------- ")
                    //val runs = getAllRunsComplete()
                    val runs = getAllRuns()
                    Result.success(runs)
                }
            }
        }
        catch(e: Throwable) {
            Log.e(TAG, "processTool-$tool----------------------e: $e")
            return Result.failure(e)
        }
    }

    private fun process(response: Result<GroqResponse<ChatCompletion>>) {
        if(response.isSuccess) {
            val res = response.getOrNull()
            val data = res?.data
            val obj = data?.obj
            val id = data?.id
            val totalTime = data?.usage?.totalTime
            Log.e(TAG, "process------------------- SUCCESS: id=$id / obj=$obj / time=$totalTime ")
            for(r in data?.choices ?: listOf()) {
                Log.e(TAG, "process------------------- SUCCESS: finish=${r.finishReason} / msg=${r.message.content} / tool=${r.message.toolCalls} /")
                val content = r.message.content ?: ""
                toolCall = r.message.toolCalls ?: listOf()

                //finish=tool_calls / content=La carrera más corta es la correspondient / tool=GetShortest ... -> It shoul be finishReason=stop, but LLM wanna call tool again ??!!
                //if(r.finishReason == "stop" && content.isNotEmpty()) {
                if(content.isNotEmpty()) {
                    Log.e(TAG, "process------------------- SUCCESS: STOP : $content")
                    if(content == "[][]")
                        result = Result.success("No se encontró una carrera con tales características")
                    else
                        result = Result.success(content)
                }
                else if(r.finishReason == "tool_calls" && toolCall.isNotEmpty()) {
                    Log.e(TAG, "process------------------- SUCCESS: TOOL CALL : ${toolCall.size}")
                    val gson = GsonBuilder().create()
                    val answer = gson.toJson(toolCall)
                    result = Result.success(answer)
                }
                else {
                    Log.e(TAG, "process------------------- SUCCESS: ??? ")
                    result = Result.failure(AppError.NotFound)
                }
            }
        }
        else {
            val error = response.exceptionOrNull() ?: UnknownError()
            Log.e(TAG, "process------------------- FAILURE: $error")
            result = Result.failure(error)
        }
    }

    companion object {
        private const val TAG = "Groq"
        const val SEED = 10
        const val TEMPERATURE = 0.2
        private val MODEL = GroqModel.LLAMA_4_SCOUT_17B_16E_INSTRUCT
        private const val SYSTEM =
            "You are a helpful assistant that answers questions about the runs." +
            //
            //" Do not extend much in your response." +
            //" After your textual response, return also a json of the list of runs you have selected," +
            //" compose a json list even if the run selected is just one, and the root of the list must be and object named 'cesjson'." +
            " Return only a json of the list of runs you have selected as response, do not add any other text;" +
            //" Return a json of the list of runs you have selected as response;" +
            " compose a json list even if the run selected is just one;" +
            " the root of the list must be and object named 'cesjson'."+
            //
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