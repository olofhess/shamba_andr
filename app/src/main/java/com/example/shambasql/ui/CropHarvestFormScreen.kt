package com.example.shambasql.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.shambasql.model.CropHarvest
import com.example.shambasql.sql.ShambaDatabaseHelper
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CropHarvestFormScreen(
    initialHarvest: CropHarvest?,
    fieldId: Long,
    cropId: Long,
    fieldArea: Double,
    onSave: (CropHarvest) -> Unit,
    onCancel: () -> Unit
) {
    var unitType by remember { mutableStateOf(initialHarvest?.unitType ?: "") }
    var quantity by remember { mutableStateOf(initialHarvest?.quantity?.toString() ?: "") }
    var valuePerUnit by remember { mutableStateOf(initialHarvest?.valuePerUnit?.toString() ?: "") }
    var harvestDate by remember { mutableStateOf(initialHarvest?.harvestDate ?: Date()) }

    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val dateString = remember(harvestDate) { dateFormatter.format(harvestDate) }

    var showAlert by remember { mutableStateOf(false) }
    val totalSoFar = remember { mutableStateOf(0.0) }

    val dbHelper = remember { ShambaDatabaseHelper(context) }
    val previousHarvests = remember { dbHelper.getHarvestsForCrop(cropId) }

    Dialog(onDismissRequest = onCancel) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (initialHarvest == null) "Add Harvest" else "Edit Harvest",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = unitType,
                    onValueChange = { unitType = it },
                    label = { Text("Unit Type (e.g. kg, ton, bags, cobs)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = valuePerUnit,
                    onValueChange = { valuePerUnit = it },
                    label = { Text("Value Per Unit") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    "Harvest Date: $dateString",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val calendar = Calendar.getInstance().apply { time = harvestDate }
                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    calendar.set(year, month, day)
                                    harvestDate = calendar.time
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                        .padding(8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = onCancel) {
                        Text("Cancel")
                    }
                    Button(onClick = {
                        val qty = quantity.toDoubleOrNull() ?: 0.0
                        // Convert quantity to kg including cobs case
                        val currentKg = when (unitType.lowercase()) {
                            "bag", "bags" -> qty * 90.0
                            "ton", "tons" -> qty * 1000.0
                            "cob", "cobs" -> qty * 0.27
                            else -> qty
                        }
                        // Sum previous harvests
                        val previousKg = previousHarvests.sumOf { h ->
                            when (h.unitType.lowercase()) {
                                "bag", "bags" -> h.quantity * 90.0
                                "ton", "tons" -> h.quantity * 1000.0
                                "cob", "cobs" -> h.quantity * 0.27
                                else -> h.quantity
                            }
                        }
                        val totalKg = previousKg + currentKg
                        totalSoFar.value = if (fieldArea > 0) totalKg / fieldArea else 0.0
                        showAlert = true
                    }) {
                        Text("Save")
                    }
                }
            }
        }
    }

    if (showAlert) {
        AlertDialog(
            onDismissRequest = { showAlert = false },
            confirmButton = {
                TextButton(onClick = {
                    val harvest = CropHarvest(
                        id = initialHarvest?.id ?: 0,
                        fieldId = fieldId,
                        cropId = cropId,
                        unitType = unitType,
                        quantity = quantity.toDoubleOrNull() ?: 0.0,
                        valuePerUnit = valuePerUnit.toDoubleOrNull() ?: 0.0,
                        totalValue = (quantity.toDoubleOrNull() ?: 0.0) * (valuePerUnit.toDoubleOrNull() ?: 0.0),
                        harvestDate = harvestDate
                    )
                    onSave(harvest)
                    showAlert = false
                }) {
                    Text("OK")
                }
            },
            title = { Text("Harvest Summary") },
            text = {
                Text("You have recorded ${"%.1f".format(totalSoFar.value)} kg/ha so far for this crop.")
            }
        )
    }
}
