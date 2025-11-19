package com.cesoft.cesrunner.data.groq

import io.github.vyfor.groqkt.api.chat.CompletionFunction
import io.github.vyfor.groqkt.api.chat.CompletionTool
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

//{
// "value":[
//      {"distance":1921,"id":37,"name":"06/11/2025 17:36:11","points":[],"time":630000,"timeEnd":1762447601000,"timeIni":1762446971000},
//      {"distance":1969,"id":36,"name":"06/11/2025 17:23:47","points":[],"time":616336,"timeEnd":1762446842000,"timeIni":1762446225664}
//      ,{"distance":1199,"id":35,"name":"02/11/2025 12:36:47","points":[],"time":392000,"timeEnd":1762083797000,"timeIni":1762083405000},
//      {"distance":1645,"id":32,"name":"23/10/2025 17:30:34 canarias","points":[],"time":551156,"timeEnd":1761233968000,"timeIni":1761233416844},
//      {"distance":5019,"id":31,"name":"19/10/2025 16:33:51","points":[],"time":1610423,"timeEnd":1760886040000,"timeIni":1760884429577},
//      {"distance":3605,"id":30,"name":"15/10/2025 18:19:08","points":[],"time":1151000,"timeEnd":1760546283000,"timeIni":1760545132000},
//      {"distance":4624,"id":29,"name":"04/10/2025 14:10:53","points":[],"time":1657947,"timeEnd":1759581493000,"timeIni":1759579835053},
//      {"distance":4669,"id":28,"name":"04/10/2025 12:14:07","points":[],"time":1550952,"timeEnd":1759574389000,"timeIni":1759572838048},
//      {"distance":3007,"id":27,"name":"02/10/2025 17:32:00","points":[],"time":952013,"timeEnd":1759420069000,"timeIni":1759419116987},
//      {"distance":2021,"id":26,"name":"Canarias 30/09/2025 18:27:20","points":[],"time":752000,"timeEnd":1759250299000,"timeIni":1759249547000},
//      {"distance":1641,"id":25,"name":"30/09/2025 17:50:42","points":[],"time":518000,"timeEnd":1759247951000,"timeIni":1759247433000},
//      {"distance":1307,"id":22,"name":"25/09/2025 17:51:17","points":[],"time":406913,"timeEnd":1758815858000,"timeIni":1758815451087},
//      {"distance":1326,"id":20,"name":"25/09/2025 17:43:58","points":[],"time":378000,"timeEnd":1758815406000,"timeIni":1758815028000},
//      {"distance":3702,"id":17,"name":"24/09/2025 18:09:21","points":[],"time":1248000,"timeEnd":1758731394000,"timeIni":1758730146000},
//      {"distance":3920,"id":15,"name":"24/09/2025 17:41:45","points":[],"time":1371742,"timeEnd":1758729875000,"timeIni":1758728503258},
//      {"distance":1564,"id":14,"name":"21/09/2025 17:40:53","points":[],"time":517585,"timeEnd":1758469767000,"timeIni":1758469249415},
//      {"distance":3187,"id":13,"name":"13/08/2025 18:00:20","points":[],"time":1050368,"timeEnd":1755101866000,"timeIni":1755100815632},
//      {"distance":2405,"id":12,"name":"02/08/2025 17:01:30","points":[],"time":811868,"timeEnd":1754147702000,"timeIni":1754146890132},
//      {"distance":2774,"id":11,"name":"10/07/2025 21:15:24","points":[],"time":854249,"timeEnd":1752175777000,"timeIni":1752174922751},
//      {"distance":1363,"id":10,"name":"09/07/2025 19:59:01","points":[],"time":518632,"timeEnd":1752084458000,"timeIni":1752083939368},
//      {"distance":2231,"id":9,"name":"09/07/2025 19:27:48","points":[],"time":713426,"timeEnd":1752082784000,"timeIni":1752082070574},
//      {"distance":5627,"id":8,"name":"02/07/2025 17:14:33","points":[],"time":3024393,"timeEnd":1751472292000,"timeIni":1751469267607},
//      {"distance":1656,"id":7,"name":"20/05/2025 17:35:25","points":[],"time":573800,"timeEnd":1747755889000,"timeIni":1747755315200},
//      {"distance":4409,"id":6,"name":"21/04/2025 18:41:57","points":[],"time":4032446,"timeEnd":1745257750000,"timeIni":1745253717554},
//      {"distance":4039,"id":5,"name":"21/04/2025 17:26:50","points":[],"time":1330448,"timeEnd":1745250534000,"timeIni":1745249203552},
//      {"distance":2905,"id":4,"name":"20/03/2025 17:09:55","points":[],"time":1123945,"timeEnd":1742488117000,"timeIni":1742486993055},
//      {"distance":336050,"id":2,"name":"Avión Canarias","points":[],"time":1260157,"timeEnd":1737565385000,"timeIni":1737564124843},
//      {"distance":3699,"id":1,"name":"Playa las Canteras","points":[],"time":3045110,"timeEnd":1737315635000,"timeIni":1737312589890}
// ]
// }

//{"value":[{"distance":1921,"id":37,"name":"06/11/2025 17:36:11","points":[],"time":630000,"timeEnd":1762447601000,"timeIni":1762446971000},{"distance":1969,"id":36,"name":"06/11/2025 17:23:47","points":[],"time":616336,"timeEnd":1762446842000,"timeIni":1762446225664},{"distance":1199,"id":35,"name":"02/11/2025 12:36:47","points":[],"time":392000,"timeEnd":1762083797000,"timeIni":1762083405000},{"distance":1645,"id":32,"name":"23/10/2025 17:30:34 canarias","points":[],"time":551156,"timeEnd":1761233968000,"timeIni":1761233416844},{"distance":5019,"id":31,"name":"19/10/2025 16:33:51","points":[],"time":1610423,"timeEnd":1760886040000,"timeIni":1760884429577},{"distance":3605,"id":30,"name":"15/10/2025 18:19:08","points":[],"time":1151000,"timeEnd":1760546283000,"timeIni":1760545132000},{"distance":4624,"id":29,"name":"04/10/2025 14:10:53","points":[],"time":1657947,"timeEnd":1759581493000,"timeIni":1759579835053},{"distance":4669,"id":28,"name":"04/10/2025 12:14:07","points":[],"time":1550952,"timeEnd":1759574389000,"timeIni":1759572838048},{"distance":3007,"id":27,"name":"02/10/2025 17:32:00","points":[],"time":952013,"timeEnd":1759420069000,"timeIni":1759419116987},{"distance":2021,"id":26,"name":"Canarias 30/09/2025 18:27:20","points":[],"time":752000,"timeEnd":1759250299000,"timeIni":1759249547000},{"distance":1641,"id":25,"name":"30/09/2025 17:50:42","points":[],"time":518000,"timeEnd":1759247951000,"timeIni":1759247433000},{"distance":1307,"id":22,"name":"25/09/2025 17:51:17","points":[],"time":406913,"timeEnd":1758815858000,"timeIni":1758815451087},{"distance":1326,"id":20,"name":"25/09/2025 17:43:58","points":[],"time":378000,"timeEnd":1758815406000,"timeIni":1758815028000},{"distance":3702,"id":17,"name":"24/09/2025 18:09:21","points":[],"time":1248000,"timeEnd":1758731394000,"timeIni":1758730146000},{"distance":3920,"id":15,"name":"24/09/2025 17:41:45","points":[],"time":1371742,"timeEnd":1758729875000,"timeIni":1758728503258},{"distance":1564,"id":14,"name":"21/09/2025 17:40:53","points":[],"time":517585,"timeEnd":1758469767000,"timeIni":1758469249415},{"distance":3187,"id":13,"name":"13/08/2025 18:00:20","points":[],"time":1050368,"timeEnd":1755101866000,"timeIni":1755100815632},{"distance":2405,"id":12,"name":"02/08/2025 17:01:30","points":[],"time":811868,"timeEnd":1754147702000,"timeIni":1754146890132},{"distance":2774,"id":11,"name":"10/07/2025 21:15:24","points":[],"time":854249,"timeEnd":1752175777000,"timeIni":1752174922751},{"distance":1363,"id":10,"name":"09/07/2025 19:59:01","points":[],"time":518632,"timeEnd":1752084458000,"timeIni":1752083939368},{"distance":2231,"id":9,"name":"09/07/2025 19:27:48","points":[],"time":713426,"timeEnd":1752082784000,"timeIni":1752082070574},{"distance":5627,"id":8,"name":"02/07/2025 17:14:33","points":[],"time":3024393,"timeEnd":1751472292000,"timeIni":1751469267607},{"distance":1656,"id":7,"name":"20/05/2025 17:35:25","points":[],"time":573800,"timeEnd":1747755889000,"timeIni":1747755315200},{"distance":4409,"id":6,"name":"21/04/2025 18:41:57","points":[],"time":4032446,"timeEnd":1745257750000,"timeIni":1745253717554},{"distance":4039,"id":5,"name":"21/04/2025 17:26:50","points":[],"time":1330448,"timeEnd":1745250534000,"timeIni":1745249203552},{"distance":2905,"id":4,"name":"20/03/2025 17:09:55","points":[],"time":1123945,"timeEnd":1742488117000,"timeIni":1742486993055},{"distance":336050,"id":2,"name":"Avión Canarias","points":[],"time":1260157,"timeEnd":1737565385000,"timeIni":1737564124843},{"distance":3699,"id":1,"name":"Playa las Canteras","points":[],"time":3045110,"timeEnd":1737315635000,"timeIni":1737312589890}]}

enum class GroqToolType { GetAll, GetNear, GetLongest, GetShortest }
object GroqTools {

    private val noParams = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {}
    }

    private val toolAll = CompletionTool(CompletionFunction(
        name = GroqToolType.GetAll.name,
        description = "Returns all the runs in the database, so you can later filter by fields like id, name, etc.",// +", filtering by the optional fields",
        parameters = buildJsonObject {
            put("type", "object")
            putJsonObject("properties") {
                /*putJsonObject("id") {
                    put("type", "string")//integer
                    put("description", "The id or identifier of the run record.")
                }
                putJsonObject("name") {
                    put("type", "string")
                    put("description", "The name of the run record.")
                }
                putJsonObject("distance") {
                    put("type", "string")//integer
                    put("description", "Approximate distance in meters ran by the user in the run record.")
                }
                putJsonObject("dateIni") {
                    put("type", "string")
                    put("description", "Initial range of date. The run must be after this date.")
                }
                putJsonObject("dateEnd") {
                    put("type", "string")
                    put("description", "Final range of date. The run must be before this date.")
                }*/
            }
        }
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
                    putJsonObject("distanceToLocation") {
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