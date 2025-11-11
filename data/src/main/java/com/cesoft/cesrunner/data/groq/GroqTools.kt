package com.cesoft.cesrunner.data.groq

import io.github.vyfor.groqkt.api.chat.CompletionFunction
import io.github.vyfor.groqkt.api.chat.CompletionTool
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

enum class GroqToolType { GetAll, GetNear, GetLongest, GetShortest }
object GroqTools {

    private val noParams = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {}
    }

    private val toolAll = CompletionTool(CompletionFunction(
        name = GroqToolType.GetAll.name,
        description = "Returns all the runs in the database",
        parameters = noParams,
    ))

    private val toolLongest = CompletionTool(CompletionFunction(
        name = GroqToolType.GetLongest.name,
        description = "Returns the run with the greater distance ran in the database, the longest run",
        parameters = noParams,
    ))

    private val toolShortest = CompletionTool(CompletionFunction(
        name = GroqToolType.GetShortest.name,
        description = "Returns the run with the lower distance ran in the database, the shortest run",
        parameters = noParams,
    ))

    private val toolNear = CompletionTool(CompletionFunction(
        name = GroqToolType.GetNear.name,
        description = "Finds the runs in the database that are near to the current user location." +
                " Optional parameter distance can be used to calculate maximum distance" +
                " from current location, do not misunderstand with the distance field of the run",
        parameters =
            buildJsonObject {
                put("type", "object")
                putJsonObject("properties") {
                    putJsonObject("distance") {
                        put("type", "string")
                        put("description", "Maximum distance of the run from the current location in meters, default value is 100 meters")
                    }
//                    putJsonObject("unit") {
//                        put("type", "string")
//                        putJsonArray("enum") {
//                            add("celsius")
//                            add("fahrenheit")
//                        }
//                    }
                }
                //putJsonArray("required") { add("distance") }
            }
    ))


    ///---------------------------------------------------------------------------------------------
    val tools = mutableListOf(toolAll, toolNear, toolLongest, toolShortest)
}