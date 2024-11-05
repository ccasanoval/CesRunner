package com.cesoft.cesrunner.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.cesoft.cesrunner.domain.entity.TrackPointDto
import org.osmdroid.util.GeoPoint

@Composable
fun OpenStreetMapView(
    points: List<TrackPointDto>,
    modifier: Modifier = Modifier,
    //onStoreClick: (GeoPoint) -> Unit,
    //location: Location,
    //centerStores: Boolean = false,
) {
    val context = LocalContext.current
    val mapView = rememberMapView(context)
    LaunchedEffect(points) {
        mapView.refreshDrawableState()
    }
    AndroidView(
        factory = { mapView },
        modifier = modifier
    ) { view ->
        view.apply {
            overlays.removeAll { true }
            addMyLocation(context, view)
            if(points.isNotEmpty()) {
                createPolyline(mapView, points.map { GeoPoint(it.latitude, it.longitude) })
            }
            /*
            stores.forEach { store ->
                val geoPoint = GeoPoint(store.location.latitude, store.location.longitude)
                val icon = context.getDrawable(store.getIcon(hasFocus = storeSelected?.id == store.id))
                addMarker(
                    mapView = view,
                    geoPoint = geoPoint,
                    icon = icon,
                    onClick = { onStoreClick(store) }
                )
            }
            if(centerStores) {
                val res = Location.centerOf(stores.map { it.location })
                controller.setCenter(GeoPoint(res.latitude, res.longitude))
            }
            else {
                controller.setCenter(GeoPoint(location.latitude, location.longitude))
            }*/
        }
    }
}