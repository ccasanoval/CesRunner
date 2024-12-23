package com.cesoft.cesrunner.data.gpx

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.cesoft.cesrunner.domain.AppError
import com.cesoft.cesrunner.domain.entity.TrackDto
import com.cesoft.cesrunner.domain.entity.TrackPointDto
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

//https://github.com/bvn13/GpxAndroidSdk/tree/master
//https://mapcreator.io/es/cartography/the-3-primary-types-of-geographic-data-formats/
class GpxUtil {

    fun import(uri: Uri, context: Context): TrackDto? {
        var input: InputStream? = null
        try {
            input = context.contentResolver.openInputStream(uri)
            if(input == null) return null
            val gpx = GpxType.read(input)
            if(gpx.wpt != null && gpx.wpt.size > 1) {
                val points = gpx.wpt.map {
                    TrackPointDto.Empty.copy(
                        latitude = it.lat,
                        longitude = it.lon,
                        altitude = it.ele ?: 0.0,
                        time = it.time?.toEpochSecond() ?: 0,
                    )
                }
                val track = TrackDto(
                    name = "?",
                    points = points,
                    timeIni = points.first().time,
                    timeEnd = points.last().time,
                    distance = TrackDto.calcDistance(points).toInt()
                )
                return track
            }
            Log.e(TAG, "import: Doesn't exist:")
            return null
        }
        catch(e: IOException) {
            Log.e(TAG, "import:e: $e")
            return null
        }
        finally {
            input?.close()
        }
    }

    fun import(filename: String): TrackDto? {
        var input: FileInputStream? = null
        try {
            val file = File(filename)
            if(file.exists()) {
                input = FileInputStream(file)
                //val data = convertStreamToString(input)
                //val gpx = GpxType.read(data.byteInputStream())
                val gpx = GpxType.read(input)
                if(gpx.wpt != null && gpx.wpt.size > 1) {
                    val points = gpx.wpt.map {
                        TrackPointDto.Empty.copy(
                            latitude = it.lat,
                            longitude = it.lon,
                            altitude = it.ele ?: 0.0,
                            time = it.time?.toEpochSecond() ?: 0,
                        )
                    }
                    val track = TrackDto(
                        name = filename,
                        points = points,
                        timeIni = points.first().time,
                        timeEnd = points.last().time,
                        distance = TrackDto.calcDistance(points).toInt()
                    )
                    return track
                }
            }
            return null
        }
        catch(e: IOException) {
            Log.e(TAG, "import:e: $filename $e")
            return null
        }
        finally {
            input?.close()
        }
    }

    @Throws(Exception::class)
    fun convertStreamToString(input: InputStream?): String {
        val reader = BufferedReader(InputStreamReader(input))
        val sb = StringBuilder()
        var line: String?
        while ((reader.readLine().also { line = it }) != null) {
            sb.append(line).append("\n")
        }
        reader.close()
        return sb.toString()
    }

    fun export(track: TrackDto): Result<String> {
        val gpxType = GpxType(
            MetadataType(track.name, description = "", authorName = "CesRunner"),
            wpt = track.points.map {
                    WptType(
                        lat = it.latitude,
                        lon = it.longitude,
                        ele = it.altitude,
                        time = OffsetDateTime.of(
                            LocalDateTime.ofEpochSecond(
                                it.time, 0, ZoneOffset.UTC), ZoneOffset.UTC)
                    )
                }.toList()
            )
        val data = gpxType.toXmlString()
        val filename = track.name.removeFileChars() + ".gpx"
        return saveFilePublic(data, filename)
    }

    private fun String.removeFileChars() = this
        .replace("|","").replace("*","")
        .replace("<","").replace(">","")
        .replace("[","").replace("]","")
        .replace("\"","").replace("'","")
        .replace("\\","").replace("/","-")
        .replace(":",".").replace(" ","_")

    private fun saveFilePublic(
        data: String,
        name: String
    ): Result<String> {
        var input: InputStream? = null
        var output: FileOutputStream? = null
        try {
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(dir, name)
            if(!file.exists()) file.createNewFile()
            if(file.exists()) {
                input = data.byteInputStream()
                output = FileOutputStream(file)
                output.write(input.readBytes())
                output.flush()
                return Result.success(file.path)
            }
            else {
                Log.e(TAG, "saveFilePublic:e: Couldn't create file: ${file.path}")
                return Result.failure(AppError.FileError(file.absolutePath))
            }
        }
        catch(e: IOException) {
            Log.e(TAG, "saveFilePublic:e: $e")
            return Result.failure(AppError.FileError(e.localizedMessage ?: e.message ?: ""))
        }
        finally {
            input?.close()
            output?.close()
        }
    }

    companion object {
        private const val TAG = "GpxUtil"
    }
}