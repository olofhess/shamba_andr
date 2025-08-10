package com.example.shambasql.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shambasql.model.Crop
import com.example.shambasql.model.FarmField
import com.example.shambasql.sql.ShambaDatabaseHelper
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CropListScreen(
    dbHelper: ShambaDatabaseHelper,
    field: FarmField,
    onAddCrop: () -> Unit,
    onEditCrop: (Crop) -> Unit,
    onDeleteCrop: (Crop) -> Unit,
    onShowCropDetails: (Crop) -> Unit,
    onBack: () -> Unit
) {
    val crops = dbHelper.getCropsForField(field.id)
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Crops for ${field.name}", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onAddCrop) {
            Text("Add Crop")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(crops) { crop ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text("${crop.name} (${crop.type})", style = MaterialTheme.typography.titleMedium)
                    Text("Season: ${crop.season}")
                    Text("Start: ${dateFormat.format(crop.startDate)}, End: ${dateFormat.format(crop.endDate)}")
                    Text("Active: ${if (crop.isActive) "Yes" else "No"}")

                    Row(modifier = Modifier.padding(top = 8.dp)) {
                        Button(onClick = { onEditCrop(crop) }, modifier = Modifier.padding(end = 8.dp)) {
                            Text("Edit")
                        }
                        Button(onClick = { onDeleteCrop(crop) }, modifier = Modifier.padding(end = 8.dp)) {
                            Text("Delete")
                        }
                        Button(onClick = { onShowCropDetails(crop) }) {
                            Text("Show Details")
                        }
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onBack) {
            Text("Back")
        }
    }
}
