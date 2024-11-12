package com.cesoft.cesrunner.ui.common

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.cesoft.cesrunner.domain.entity.TrackPointDto
import com.cesoft.cesrunner.ui.theme.Green
import org.osmdroid.config.Configuration
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay


@SuppressLint("UseCompatLoadingForDrawables")
@Composable
fun MapCompo(
    context : Context,
    mapView: MapView,
    trackPoints: List<TrackPointDto>,
    modifier: Modifier = Modifier
) {
    //val mapView = rememberMapCompo(context)
    AndroidView(
        factory = { mapView },
        modifier = modifier
    ) { view ->
        view.overlays.removeAll { true }

        val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), mapView)
        locationOverlay.enableMyLocation()
        view.overlays.add(locationOverlay)
        //view.controller.setCenter(locationOverlay.myLocation)

        val points = trackPoints.map { p -> GeoPoint(p.latitude, p.longitude) }

        val iIni = context.getDrawable(android.R.drawable.ic_menu_compass)
        iIni?.setTint(Green.toArgb())
        val iEnd = context.getDrawable(android.R.drawable.ic_menu_mylocation)
        iEnd?.setTint(Color.Red.toArgb())
        points.firstOrNull()?.let { addMarker(view, it, iIni) }
        points.lastOrNull()?.let { addMarker(view, it, iEnd) }

        //val polyline = createPolyline(mapView, points, Green)
        val polyline = Polyline(mapView)
        polyline.isGeodesic = true
        polyline.setPoints(points)
        view.overlays.add(polyline)
        points.lastOrNull()?.let { view.controller.setCenter(it) }

        android.util.Log.e("MapCompo", "0000000------------- ${locationOverlay.myLocation}")
//        val polygon = Polygon()
//        polygon.setPoints(points)
//        polygon.fillColor = Color.Red.toArgb()
//        polygon.title = "test"
//        polygon.getOutlinePaint().color = polygon.fillPaint.color
//        view.overlays.add(polygon)
    }
}

@Composable
fun rememberMapCompo(context : Context): MapView {
    android.util.Log.e("rememberMapCompo", "CREATE---------------------")
    val pack = context.packageName
    val prefs = context.getSharedPreferences(pack+"OSM", Context.MODE_PRIVATE)
    Configuration.getInstance().load(context, prefs)
    val mapView = remember { MapView(context) }
    DisposableEffect(Unit) {
        onDispose {
            mapView.onDetach()
        }
    }
    return mapView.apply { initMap(this) }
}

private fun initMap(mapView: MapView) {
    android.util.Log.e("rememberMapCompo", "initMap---------------------")
    mapView.apply {
        isHorizontalMapRepetitionEnabled = false
        isVerticalMapRepetitionEnabled = false
        setMultiTouchControls(true)
        val tileSystem = MapView.getTileSystem()
        setScrollableAreaLimitDouble(
            BoundingBox(
                tileSystem.maxLatitude, tileSystem.maxLongitude, // top-left
                tileSystem.minLatitude, tileSystem.minLongitude  // bottom-right
            )
        )
        minZoomLevel = 5.0
        controller.setZoom(20.0)
    }
}

fun addMyLocation(context: Context, mapView: MapView) {
    val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), mapView)
    locationOverlay.enableMyLocation()
    //locationOverlay.enableFollowLocation()--> Doesn't let you move manually
    //locationOverlay.runOnFirstFix { mapView.setExpectedCenter(locationOverlay.myLocation) }
    mapView.overlays.add(locationOverlay)
    //
    val compassOverlay = CompassOverlay(context, InternalCompassOrientationProvider(context), mapView)
    compassOverlay.enableCompass()
    mapView.overlays.add(compassOverlay)
}

fun addMarker(
    mapView : MapView,
    geoPoint: GeoPoint,
    icon: Drawable?=null,
    title: String?=null,
    snippet : String?=null,
    //onClick: (Marker, MapView) -> Boolean
    onClick: (() -> Unit)? = null
): Marker {
    val marker = Marker(mapView)
    marker.position = geoPoint
    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
    marker.infoWindow = null
    marker.setOnMarkerClickListener { _, _ -> //onClick(m, v)
        onClick?.let {
            it()
            true
        } ?: run { false }
    }

    title?.let { marker.title = it }
    snippet?.let { marker.snippet = it }
    icon?.let { marker.icon = it }

    mapView.overlays.add(marker)
    return marker
}

fun createPolyline(
    mapView: MapView,
    points: List<GeoPoint>,
    color: Color?
): Polyline {
    val polyline = Polyline(mapView)
    color?.let { polyline.color = color.toArgb() }
    polyline.setPoints(points)
    polyline.infoWindow = null
    return polyline
}

/*
fun createPolyline(mapView: MapView, points: List<GeoPoint>): Polyline {
    val polyline = Polyline(mapView)
    //polyline.color = Green.toArgb()
    //for(p in points) polyline.addPoint(p)
    polyline.setPoints(points)
    polyline.infoWindow = null
    mapView.overlayManager.add(polyline)
    return polyline
}*/
//
//fun drawPath(mapView: MapView, points: List<GeoPoint>): Polyline {
//    val paint: Paint = Paint()
//    paint.color = Green.toArgb()
//    paint.alpha = 90
//    paint.style = Paint.Style.STROKE
//    paint.strokeWidth = 10f
//    val myPath: PathOverlay = PathOverlay(Color.RED, this)
//    myPath.setPaint(paint)
//    for(p in points) {
//        myPath.addPoint(p)
//    }
//    mapView.overlays.add(myPath)
//}

//fun createPolyline(mapView: MapView, startPoint: GeoPoint, endPoint: GeoPoint): Polyline {
//    val polyline = Polyline(mapView)
//    polyline.color = Green.toArgb()
//    polyline.addPoint(startPoint)
//    polyline.addPoint(endPoint)
//    polyline.infoWindow = null
//    return polyline
//}

//
//fun mapEventsOverlay(
//    view : MapView,
//    onTap : (GeoPoint)->Unit
//): MapEventsOverlay {
//    return MapEventsOverlay(object : MapEventsReceiver {
//        override fun singleTapConfirmedHelper(geoPoint: GeoPoint?): Boolean {
//            // Handle the map click event
//            if (geoPoint != null) {
//                onTap(geoPoint)
//                view.invalidate() // Refresh the map view
//            }
//            return true
//        }
//
//        override fun longPressHelper(p: GeoPoint?): Boolean {
//            // Handle long press event if needed
//            return false
//        }
//    })
//}
