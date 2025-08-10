package com.example.shambasql.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.shambasql.model.FieldBoundaryPoint
import kotlin.math.*

@Composable
fun BoundaryMappingScreen(
    fieldId: Long,
    onFinish: () -> Unit
) {
    val context = LocalContext.current

    // Permissions
    var hasFine by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasCoarse by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
        )
    }
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        hasFine = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
        hasCoarse = result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    // Live location while measuring
    var currentLoc by remember { mutableStateOf<Location?>(null) }
    var currentAcc by remember { mutableStateOf<Float?>(null) }
    var measuring by remember { mutableStateOf(false) }

    // Collected points
    var points by remember { mutableStateOf(listOf<FieldBoundaryPoint>()) }

    // Derived metrics (recompute when points change)
    val perimM by remember(points) { mutableStateOf(perimeterMeters(points)) }
    val areaHa by remember(points) { mutableStateOf(areaHectares(points)) }

    // Result dialog flag
    var showResult by remember { mutableStateOf(false) }

    // Ask permission on first show if needed
    LaunchedEffect(Unit) {
        if (!hasFine && !hasCoarse) {
            permLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Start/stop lightweight location updates tied to `measuring`
    DisposableEffect(measuring, hasFine, hasCoarse) {
        if (measuring && (hasFine || hasCoarse)) {
            val lm = context.getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager
            val listener = object : LocationListener {
                override fun onLocationChanged(loc: Location) {
                    currentLoc = loc
                    currentAcc = loc.accuracy
                }
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }
            try {
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 1f, listener)
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1500L, 5f, listener)
            } catch (_: SecurityException) { /* ignore */ }

            onDispose { lm.removeUpdates(listener) }
        } else {
            onDispose { }
        }
    }

    fun addPointFromCurrent() {
        val loc = currentLoc ?: return
        if (loc.accuracy > 50f) return // too noisy
        if (points.isNotEmpty()) {
            val last = points.last()
            val d = distanceMeters(last.latitude, last.longitude, loc.latitude, loc.longitude)
            if (d < 3.0) return // ignore tiny movement
        }
        val p = FieldBoundaryPoint(
            id = 0,
            fieldId = fieldId,
            latitude = loc.latitude,
            longitude = loc.longitude,
            timestamp = System.currentTimeMillis()
        )
        points = points + p
    }

    val clipboard = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .systemBarsPadding()
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Controls (buttons unchanged)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    points = emptyList()
                    measuring = true
                },
                enabled = hasFine || hasCoarse
            ) { Text("Start") }

            Button(
                onClick = { addPointFromCurrent() },
                enabled = measuring && (hasFine || hasCoarse)
            ) { Text("Add point") }

            Button(
                onClick = {
                    measuring = false
                    showResult = true // show dialog with results
                },
                enabled = measuring && points.size >= 3
            ) { Text("Finish") }
        }

        Spacer(Modifier.height(8.dp))

        // Live status with COLOR-CODED accuracy
        val (accText, accColor) = currentAcc?.let {
            val txt = "±${it.roundToInt()} m"
            val color = when {
                it <= 10f -> Color(0xFF2E7D32)      // green
                it <= 25f -> Color(0xFFF9A825)      // yellow
                else -> Color(0xFFC62828)           // red
            }
            txt to color
        } ?: ("—" to Color.Gray)

        Text("GPS accuracy: $accText", color = accColor, fontSize = 18.sp)

        Spacer(Modifier.height(8.dp))
        Divider()
        Spacer(Modifier.height(8.dp))

        // Metrics row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Points: ${points.size}")
            Text("Perimeter: ${formatMeters(perimM)}")
            Text("Area: ${formatHectares(areaHa)}")
        }

        Spacer(Modifier.height(10.dp))

        // Highlighted Area card + Copy
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                tonalElevation = 2.dp,
                modifier = Modifier
                    .weight(1f)
                    .border(2.dp, Color.Black)
                    .padding(12.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("Estimated area", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = String.format("%.4f ha", areaHa),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }
            Button(
                onClick = { clipboard.setText(AnnotatedString(String.format("%.4f", areaHa))) },
                enabled = areaHa > 0.0
            ) { Text("Copy") }
        }

        Spacer(Modifier.height(8.dp))
        Divider()
        Spacer(Modifier.height(8.dp))

        // Scrollable coordinates + preview
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            if (points.isEmpty()) {
                Surface(tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Tap Start, then Add point at each corner/turn as you walk.",
                        modifier = Modifier.padding(12.dp),
                        fontSize = 18.sp
                    )
                }
            } else {
                // Bigger coordinate list
                points.forEachIndexed { i, p ->
                    Text(
                        "${i + 1}. ${"%.6f".format(p.latitude)}, ${"%.6f".format(p.longitude)}",
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            if (points.size >= 2) {
                Text("Field outline", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                PolygonPreviewOutline(points)
            }
        }

        Spacer(Modifier.height(12.dp))

        // Quit button at bottom
        Button(
            onClick = {
                measuring = false
                onFinish()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text("Quit")
        }
    }

    // Results Dialog after Finish
    if (showResult) {
        AlertDialog(
            onDismissRequest = { /* keep open until user chooses */ },
            title = { Text("Measurement complete") },
            text = {
                Column {
                    Text("Estimated area for this field:")
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        tonalElevation = 2.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, Color.Black)
                            .padding(12.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = String.format("%.4f ha", areaHa),
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Spacer(Modifier.height(4.dp))
                            Text("Perimeter: ${formatMeters(perimM)}")
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Reminder: take this area value to the Field page, use the Edit button and enter it as your actual value. " +
                                "It will then be stored for this field and used for various calculations."
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { clipboard.setText(AnnotatedString(String.format("%.4f", areaHa))) },
                        enabled = areaHa > 0.0,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Copy area (ha)")
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    showResult = false
                    onFinish()
                }) { Text("Done") }
            }
        )
    }
}

/* ---------------- Geometry preview (outline only, static) ---------------- */

@Composable
private fun PolygonPreviewOutline(points: List<FieldBoundaryPoint>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(Color(0xFFEFEFEF)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val minLat = points.minOf { it.latitude }
            val maxLat = points.maxOf { it.latitude }
            val minLon = points.minOf { it.longitude }
            val maxLon = points.maxOf { it.longitude }

            val latRange = max(1e-9, maxLat - minLat)
            val lonRange = max(1e-9, maxLon - minLon)

            val scaleX = size.width / lonRange.toFloat()
            val scaleY = size.height / latRange.toFloat()
            val scale = min(scaleX, scaleY) * 0.9f

            val drawWidth = lonRange.toFloat() * scale
            val drawHeight = latRange.toFloat() * scale
            val offsetX = (size.width - drawWidth) / 2f
            val offsetY = (size.height - drawHeight) / 2f

            val path = Path()
            points.forEachIndexed { idx, p ->
                val x = ((p.longitude - minLon).toFloat() * scale) + offsetX
                val y = size.height - (((p.latitude - minLat).toFloat() * scale) + offsetY)
                if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
                drawCircle(Color.Red, radius = 5f, center = Offset(x, y))
            }
            if (points.size >= 3) path.close()

            drawPath(path, color = Color.Black, style = Stroke(width = 2f))
        }
    }
}

/* ---------------------- Math helpers (meters/ha) ---------------------- */

private fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371000.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2.0) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2).pow(2.0)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return r * c
}

private fun perimeterMeters(points: List<FieldBoundaryPoint>): Double {
    if (points.size < 2) return 0.0
    var sum = 0.0
    for (i in 0 until points.size - 1) {
        sum += distanceMeters(
            points[i].latitude, points[i].longitude,
            points[i + 1].latitude, points[i + 1].longitude
        )
    }
    if (points.size >= 3) {
        sum += distanceMeters(
            points.last().latitude, points.last().longitude,
            points.first().latitude, points.first().longitude
        )
    }
    return sum
}

private fun areaHectares(points: List<FieldBoundaryPoint>): Double {
    if (points.size < 3) return 0.0
    val lat0 = points.map { it.latitude }.average() * Math.PI / 180.0
    val lon0 = points.map { it.longitude }.average() * Math.PI / 180.0
    val r = 6371000.0

    val xy = points.map { p ->
        val lat = p.latitude * Math.PI / 180.0
        val lon = p.longitude * Math.PI / 180.0
        val x = r * (lon - lon0) * cos(lat0)
        val y = r * (lat - lat0)
        x to y
    }

    var s = 0.0
    for (i in xy.indices) {
        val (x1, y1) = xy[i]
        val (x2, y2) = xy[(i + 1) % xy.size]
        s += x1 * y2 - x2 * y1
    }
    val areaM2 = abs(s) * 0.5
    return areaM2 / 10_000.0
}

/* -------------------------- Formatting helpers -------------------------- */

private fun formatMeters(m: Double): String =
    if (m >= 1000) String.format("%.2f km", m / 1000.0) else String.format("%.0f m", m)

private fun formatHectares(h: Double): String = String.format("%.4f ha", h)
