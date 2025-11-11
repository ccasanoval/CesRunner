package com.cesoft.cesrunner.data.groq

import android.util.Log
import com.cesoft.cesrunner.data.BuildConfig
import com.cesoft.cesrunner.data.local.AppDatabase
import com.cesoft.cesrunner.data.local.entity.LocalTrackDto
import com.cesoft.cesrunner.data.local.entity.LocalTrackPointDto
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
class Groq(private val db: AppDatabase) {
    //TODO: Thread safety
    private var isFailure = false
    private var isLlmResponse = false
    private var toolCall: List<CompletionToolCall> = listOf()
    private var result: Result<String> = Result.failure(Exception())
    private val client = GroqClient(BuildConfig.GROQ_KEY)
    private val system0 = " You are a helpful assistant that answers questions about the user runs." +
            " The runs in the database have the following fields:" +
            " id, as the identification number of the run;" +
            " name, as the user call the run;" +
            " distance, as the distance in meters ran by the user;" +
            " timeIni, as the date and time when the user start running;" +
            " timeEnd, as the date and time when the user finish running."
    private val system1 = " You can call some useful tools that filter and return runs."

    suspend fun chat(prompt: String): Result<String> {

        val response: Result<GroqResponse<ChatCompletion>> = client.chat {
            model = GroqModel.LLAMA_4_SCOUT_17B_16E_INSTRUCT
            seed = 1
            temperature = 0.2
            tools = GroqTools.tools
            messages {
                system(system0 + system1)
                text(prompt)
            }
        }

        ///TODO: Loop of tools calls and LLM inferences
        isFailure = false
        isLlmResponse = false
        process(response)
        //while (!isFailure && !isLlmResponse) {
        var avoidInfiniteLoop = 0
        while(toolCall.isNotEmpty() && avoidInfiniteLoop < 3) {
            avoidInfiniteLoop++

            Log.e(TAG, "while(toolCall.isNotEmpty())----------------------- ${toolCall.size}")
            val toolName: CompletionToolCall = toolCall.first()
            val tool = enumValueOf<GroqToolType>(toolName.function.name)
            val params = toolName.function.arguments
            Log.e(TAG, "while(toolCall.isNotEmpty())----------------------- $tool / $params")
            val runs = processTool(tool, params)
            Log.e(TAG, "while(toolCall.isNotEmpty())----------------------- ${runs.getOrNull()?.first()?.name} / " + Gson().toJson(runs))
            toolCall = listOf()


            // Call the LLM again with the tool response
            val prompt2 = " Have in mind that the tool named $tool has returned the value "+Gson().toJson(runs)
            Log.e(TAG, "while(toolCall.isNotEmpty())----------------------- ${system0 + prompt2} ")
            val response: Result<GroqResponse<ChatCompletion>> = client.chat {
                model = GroqModel.LLAMA_4_SCOUT_17B_16E_INSTRUCT
                seed = 1
                temperature = 0.2
                tools = GroqTools.tools
                messages {
                    system(system0 )
                    text(prompt + prompt2)
                }
            }
            process(response)
            Log.e(TAG, "while(toolCall.isNotEmpty())----------------------- ${toolCall.size} / ${result.getOrNull()?.first()}")
        }
        return result
    }

    private suspend fun processTool(
        tool: GroqToolType,
        params: String
    ): Result<List<TrackDto>> {
        try {
            val tracks: List<LocalTrackDto> = db.trackDao().getAll()
            if(tracks.isEmpty()) return Result.failure(AppError.NotFound)
            val all = tracks.map {
                //val points = db.trackPointDao().getByTrackId(it.id)
                val points = listOf<LocalTrackPointDto>()
                it.toModel(points)
            }
            Log.e(TAG, "processTool----------------------- $tool ${all.size}")
            return when(tool) {
                GroqToolType.GetLongest -> {
                    var l = 0
                    var run: TrackDto? = null
                    for(r in all) {
                        if(r.distance > l) {
                            l = r.distance
                            run = r
                        }
                    }
                    Log.e(TAG, "processTool-a---------------------- ${run?.name}")
                    if(run != null) Result.success(listOf(run))
                    else Result.failure(AppError.NotFound)
                }
                GroqToolType.GetShortest -> {
                    var l = Int.MAX_VALUE
                    var run: TrackDto? = null
                    for(r in all) {
                        if(r.distance < l) {
                            l = r.distance
                            run = r
                        }
                    }
                    Log.e(TAG, "processTool-b---------------------- ${run?.name}")
                    if(run != null) Result.success(listOf(run))
                    else Result.failure(AppError.NotFound)
                }
                GroqToolType.GetNear -> {
                    Log.e(TAG, "processTool-c---------------------- ")
                    Result.failure(Exception())//TODO:
                }
                //GroqToolType.GetAll -> {
                else -> {
                    Log.e(TAG, "processTool-d---------------------- ")
                    Result.success(all)
                }
            }
        }
        catch(e: Throwable) {
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
            Log.e(TAG, "chat------------------- SUCCESS: id=$id / obj=$obj / time=$totalTime ")
            for(r in data?.choices ?: listOf()) {
                Log.e(TAG, "chat------------------- SUCCESS: finish=${r.finishReason} / msg=${r.message.content} / tool=${r.message.toolCalls} /")
                val content = r.message.content ?: ""
                toolCall = r.message.toolCalls ?: listOf()
                if(r.finishReason == "stop" && content.isNotEmpty()) {
                    Log.e(TAG, "chat------------------- SUCCESS: STOP : $content")
                    result = Result.success(content)
                    isLlmResponse = true
                }
                else if(r.finishReason == "tool_calls" && toolCall.isNotEmpty()) {
                    Log.e(TAG, "chat------------------- SUCCESS: TOOL CALL : ${toolCall.size}")
                    val gson = GsonBuilder().create()
                    val answer = gson.toJson(toolCall)
                    result = Result.success(answer)
                }
                else {
                    Log.e(TAG, "chat------------------- SUCCESS: ??? ")
                    result = Result.failure(AppError.NotFound)
                }
            }
        }
        else {
            val error = response.exceptionOrNull() ?: UnknownError()
            Log.e(TAG, "chat------------------- FAILURE: $error")
            result = Result.failure(error)
            isFailure = true
        }
    }

    companion object {
        private const val TAG = "Groq"
    }
}