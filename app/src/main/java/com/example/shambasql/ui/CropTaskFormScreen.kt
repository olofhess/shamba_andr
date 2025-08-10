package com.example.shambasql.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shambasql.model.CropTask
import java.text.SimpleDateFormat
import java.util.*

// Ensure you're using Material 3 with ExperimentalMaterial3Api
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropTaskFormScreen(
    initialTask: CropTask?,
    fieldId: Long,
    cropId: Long,
    onSave: (CropTask) -> Unit,
    onCancel: () -> Unit
) {
    var taskName by remember { mutableStateOf(initialTask?.name ?: "") }
    var quantity by remember { mutableStateOf(initialTask?.quantity?.toString() ?: "") }
    var unitType by remember { mutableStateOf(initialTask?.unitType ?: "") }
    var costPerUnit by remember { mutableStateOf(initialTask?.costPerUnit?.toString() ?: "") }

    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val initialDateMillis = initialTask?.date?.time
    var selectedDateMillis by remember { mutableStateOf(initialDateMillis) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = taskName,
            onValueChange = { taskName = it },
            label = { Text("Task Name") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = quantity,
            onValueChange = { quantity = it },
            label = { Text("Quantity") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = unitType,
            onValueChange = { unitType = it },
            label = { Text("Unit Type") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = costPerUnit,
            onValueChange = { costPerUnit = it },
            label = { Text("Cost Per Unit") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Date field with modal date picker
        OutlinedTextField(
            value = selectedDateMillis?.let { dateFormatter.format(Date(it)) } ?: "",
            onValueChange = {},
            label = { Text("Date") },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(imageVector = Icons.Default.DateRange, contentDescription = "Select date")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        selectedDateMillis = datePickerState.selectedDateMillis
                        showDatePicker = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val task = CropTask(
                id = initialTask?.id ?: 0L,
                fieldId = fieldId,
                cropId = cropId,
                name = taskName,
                date = Date(selectedDateMillis ?: Date().time),
                unitType = unitType,
                costPerUnit = costPerUnit.toDoubleOrNull() ?: 0.0,
                quantity = quantity.toDoubleOrNull() ?: 0.0,
                cost = (costPerUnit.toDoubleOrNull() ?: 0.0) * (quantity.toDoubleOrNull() ?: 0.0),
                notes = initialTask?.notes ?: ""
            )
            onSave(task)
        }) {
            Text("Save")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = onCancel) {
            Text("Cancel")
        }
    }
}
