package com.cesoft.cesrunner.ui.common

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import com.cesoft.cesrunner.ui.theme.Green
import org.osmdroid.config.Configuration
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@Composable
fun rememberMapView(context : Context): MapView {
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
        minZoomLevel = 4.0
        controller.setZoom(14.0)
    }
}

fun addMyLocation(context: Context, mapView: MapView) {
    val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), mapView)
    locationOverlay.enableMyLocation()
    //locationOverlay.enableFollowLocation()--> Doesn't let you move manually
    //locationOverlay.setPersonAnchor(100f, 100f)

    //val bm = BitmapFactory.decodeResource(context.resources, android.R.drawable.btn_radio)
    //locationOverlay.setDirectionIcon(bm)
    //locationOverlay.setPersonIcon(bm)
    locationOverlay.isOptionsMenuEnabled = true
    locationOverlay.isDrawAccuracyEnabled = true

//    locationOverlay.runOnFirstFix {
//        controller.setCenter(locationOverlay.myLocation)
//        //controller.animateTo(locationOverlay.myLocation)//Only out of main thread..
//    }
    mapView.overlays.add(locationOverlay)
    //mapView.overlayManager.overlays().add(locationOverlay)
    //overlays.add(CompassOverlay(context, mapView))
    //
    val compassOverlay = CompassOverlay(context, InternalCompassOrientationProvider(context), mapView)
    compassOverlay.enableCompass()
    mapView.overlays.add(compassOverlay)
}

fun addMarker(
    mapView : MapView,
    geoPoint: GeoPoint,
    title: String?=null,
    snippet : String?=null,
    icon: Drawable?=null,
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

fun createPolyline(mapView: MapView, points: List<GeoPoint>): Polyline {
    val polyline = Polyline(mapView)
    polyline.color = Green.toArgb()
    //for(p in points) polyline.addPoint(p)
    polyline.setPoints(points)
    polyline.infoWindow = null
    mapView.overlayManager.add(polyline)
    return polyline
}
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
