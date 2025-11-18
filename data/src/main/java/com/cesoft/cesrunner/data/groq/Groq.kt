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
    private val system0 = "You are a helpful assistant that answers questions about the runs." +
            //
            " Do not extend much in your response." +
            " After your textual response, return also a json of the list of runs you have selected," +
            " compose a json list even if the run selected is just one, and the root of the list must be and object named 'cesjson'." +
            //
            " The runs have the following fields:" +
            " id (the identification number of the run);" +
            " name (how the user calls the run);" +
            " distance (the number of meters between the starting point and the final point of the run);" +
            " timeIni (the date and time when the user start running);" +
            " timeEnd (the date and time when the user finish running)."
//            " The runs you have to have in mind are the following ones:"+
//            " [{\"distance\":1921,\"id\":37,\"name\":\"06/11/2025 17:36:11\",\"points\":[],\"time\":630000,\"timeEnd\":1762447601000,\"timeIni\":1762446971000},{\"distance\":1969,\"id\":36,\"name\":\"06/11/2025 17:23:47\",\"points\":[],\"time\":616336,\"timeEnd\":1762446842000,\"timeIni\":1762446225664},{\"distance\":1199,\"id\":35,\"name\":\"02/11/2025 12:36:47\",\"points\":[],\"time\":392000,\"timeEnd\":1762083797000,\"timeIni\":1762083405000},{\"distance\":1645,\"id\":32,\"name\":\"23/10/2025 17:30:34 canarias\",\"points\":[],\"time\":551156,\"timeEnd\":1761233968000,\"timeIni\":1761233416844},{\"distance\":5019,\"id\":31,\"name\":\"19/10/2025 16:33:51\",\"points\":[],\"time\":1610423,\"timeEnd\":1760886040000,\"timeIni\":1760884429577},{\"distance\":3605,\"id\":30,\"name\":\"15/10/2025 18:19:08\",\"points\":[],\"time\":1151000,\"timeEnd\":1760546283000,\"timeIni\":1760545132000},{\"distance\":4624,\"id\":29,\"name\":\"04/10/2025 14:10:53\",\"points\":[],\"time\":1657947,\"timeEnd\":1759581493000,\"timeIni\":1759579835053},{\"distance\":4669,\"id\":28,\"name\":\"04/10/2025 12:14:07\",\"points\":[],\"time\":1550952,\"timeEnd\":1759574389000,\"timeIni\":1759572838048},{\"distance\":3007,\"id\":27,\"name\":\"02/10/2025 17:32:00\",\"points\":[],\"time\":952013,\"timeEnd\":1759420069000,\"timeIni\":1759419116987},{\"distance\":2021,\"id\":26,\"name\":\"Canarias 30/09/2025 18:27:20\",\"points\":[],\"time\":752000,\"timeEnd\":1759250299000,\"timeIni\":1759249547000},{\"distance\":1641,\"id\":25,\"name\":\"30/09/2025 17:50:42\",\"points\":[],\"time\":518000,\"timeEnd\":1759247951000,\"timeIni\":1759247433000},{\"distance\":1307,\"id\":22,\"name\":\"25/09/2025 17:51:17\",\"points\":[],\"time\":406913,\"timeEnd\":1758815858000,\"timeIni\":1758815451087},{\"distance\":1326,\"id\":20,\"name\":\"25/09/2025 17:43:58\",\"points\":[],\"time\":378000,\"timeEnd\":1758815406000,\"timeIni\":1758815028000},{\"distance\":3702,\"id\":17,\"name\":\"24/09/2025 18:09:21\",\"points\":[],\"time\":1248000,\"timeEnd\":1758731394000,\"timeIni\":1758730146000},{\"distance\":3920,\"id\":15,\"name\":\"24/09/2025 17:41:45\",\"points\":[],\"time\":1371742,\"timeEnd\":1758729875000,\"timeIni\":1758728503258},{\"distance\":1564,\"id\":14,\"name\":\"21/09/2025 17:40:53\",\"points\":[],\"time\":517585,\"timeEnd\":1758469767000,\"timeIni\":1758469249415},{\"distance\":3187,\"id\":13,\"name\":\"13/08/2025 18:00:20\",\"points\":[],\"time\":1050368,\"timeEnd\":1755101866000,\"timeIni\":1755100815632},{\"distance\":2405,\"id\":12,\"name\":\"02/08/2025 17:01:30\",\"points\":[],\"time\":811868,\"timeEnd\":1754147702000,\"timeIni\":1754146890132},{\"distance\":2774,\"id\":11,\"name\":\"10/07/2025 21:15:24\",\"points\":[],\"time\":854249,\"timeEnd\":1752175777000,\"timeIni\":1752174922751},{\"distance\":1363,\"id\":10,\"name\":\"09/07/2025 19:59:01\",\"points\":[],\"time\":518632,\"timeEnd\":1752084458000,\"timeIni\":1752083939368},{\"distance\":2231,\"id\":9,\"name\":\"09/07/2025 19:27:48\",\"points\":[],\"time\":713426,\"timeEnd\":1752082784000,\"timeIni\":1752082070574},{\"distance\":5627,\"id\":8,\"name\":\"02/07/2025 17:14:33\",\"points\":[],\"time\":3024393,\"timeEnd\":1751472292000,\"timeIni\":1751469267607},{\"distance\":1656,\"id\":7,\"name\":\"20/05/2025 17:35:25\",\"points\":[],\"time\":573800,\"timeEnd\":1747755889000,\"timeIni\":1747755315200},{\"distance\":4409,\"id\":6,\"name\":\"21/04/2025 18:41:57\",\"points\":[],\"time\":4032446,\"timeEnd\":1745257750000,\"timeIni\":1745253717554},{\"distance\":4039,\"id\":5,\"name\":\"21/04/2025 17:26:50\",\"points\":[],\"time\":1330448,\"timeEnd\":1745250534000,\"timeIni\":1745249203552},{\"distance\":2905,\"id\":4,\"name\":\"20/03/2025 17:09:55\",\"points\":[],\"time\":1123945,\"timeEnd\":1742488117000,\"timeIni\":1742486993055},{\"distance\":336050,\"id\":2,\"name\":\"Avión Canarias\",\"points\":[],\"time\":1260157,\"timeEnd\":1737565385000,\"timeIni\":1737564124843},{\"distance\":3699,\"id\":1,\"name\":\"Playa las Canteras\",\"points\":[],\"time\":3045110,\"timeEnd\":1737315635000,\"timeIni\":1737312589890}]}"
    //private val system1 = " You can call some useful tools that filter and return runs."

    suspend fun chat(prompt: String): Result<String> {

        var response: Result<GroqResponse<ChatCompletion>> = client.chat {
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
                system(system0 )
                text(prompt)
            }
        }

        isFailure = false
        isLlmResponse = false
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
            Log.e(TAG, "while(toolCall.isNotEmpty())-----------------------result ${runs.getOrNull()?.first()?.name} / " + Gson().toJson(runs))
            toolCall = listOf()

            // Call the LLM again with the tool response
            if(runs.isFailure) {
                isFailure = true
                isLlmResponse = true
                result = Result.failure(Exception("I couldn't find the requested data"))
                return result
            }
            val runsJson = Gson().toJson(runs)
            Log.e(TAG, "while(toolCall.isNotEmpty())-----------------------runs = $runsJson / $runs")
            //val system1 = ". Choose the run o runs from the following list $runsJson"
            val system1 = " The runs you have to have in mind are the following ones: $runsJson"
            Log.e(TAG, "while(toolCall.isNotEmpty())-----------------------prompt= $system1")
            response = client.chat {
                model = MODEL
                seed = SEED
                temperature = TEMPERATURE
                //tools = GroqTools.tools
                messages {
                    system(system0 + system1)
                    text(prompt)
                }
            }
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

    private suspend fun processTool(
        tool: GroqToolType,
        params: String
    ): Result<List<TrackDto>> {
        try {
            var obj: TrackDto?
            if(params.isNotEmpty()) {
                try {
                    obj = Gson().fromJson(params, TrackDto::class.java)
                    Log.e("AA", "-------- param = $obj")
                }
                catch (e: Exception) {
                    Log.e("AA", "-------- param:e = $e / $params")
                }
            }

            val tracks: List<LocalTrackDto> = db.trackDao().getAll()
                /*
                if(obj == null) db.trackDao().getAll()
                else db.trackDao().filter(
                    id = null,//if(obj.id > 0) obj.id else null,
                    name = "avion", //obj.name.ifEmpty { null },
                    distance = null//if(obj.distance > 0) obj.distance else null,
                    //dateIni = obj.timeIni.toT//TODO
                )*/
            if(tracks.isEmpty()) return Result.failure(AppError.NotFound)
            val all = tracks.map {
                //val points = db.trackPointDao().getByTrackId(it.id)
                val points = listOf<LocalTrackPointDto>()
                it.toModel(points)
            }
            Log.e(TAG, "processTool----------000------- tool = $tool / # regs = ${all.size}")
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
                    Log.e(TAG, "processTool-$tool---------------------- ${run?.name}")
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
                    Log.e(TAG, "processTool-$tool---------------------- ${run?.name}")
                    if(run != null) Result.success(listOf(run))
                    else Result.failure(AppError.NotFound)
                }
                GroqToolType.GetNear -> {
                    Log.e(TAG, "processTool-$tool---------------------- ")
                    Result.failure(Exception())//TODO:
                }
                GroqToolType.GetAll -> {
                    Log.e(TAG, "processTool-$tool---------------------- ")
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
                    isLlmResponse = true
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
            isFailure = true
        }
    }

    companion object {
        private const val TAG = "Groq"
        const val SEED = 10
        const val TEMPERATURE = 0.2
        val MODEL = GroqModel.LLAMA_4_SCOUT_17B_16E_INSTRUCT
    }
}