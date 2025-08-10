package com.example.shambasql.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shambasql.model.CropInput
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropInputFormScreen(
    initialInput: CropInput?,
    fieldId: Long,
    cropId: Long,
    fieldArea: Double,
    onSave: (CropInput) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(initialInput?.name ?: "") }
    var inputType by remember { mutableStateOf(initialInput?.inputType ?: "") }
    var unit by remember { mutableStateOf(initialInput?.unit ?: "") }
    var quantity by remember { mutableStateOf(initialInput?.quantity?.toString() ?: "") }
    var costPerUnit by remember { mutableStateOf(initialInput?.costPerUnit?.toString() ?: "") }
    var notes by remember { mutableStateOf(initialInput?.notes ?: "") }

    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val initialDateMillis = initialInput?.date?.time
    var selectedDateMillis by remember { mutableStateOf(initialDateMillis) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = inputType,
            onValueChange = { inputType = it },
            label = { Text("Input Type") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = unit,
            onValueChange = { unit = it },
            label = { Text("Unit") },
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
            value = costPerUnit,
            onValueChange = { costPerUnit = it },
            label = { Text("Cost Per Unit") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Date picker integrated here
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
            val input = CropInput(
                id = initialInput?.id ?: 0L,
                fieldId = fieldId,
                cropId = cropId,
                name = name,
                inputType = inputType,
                unit = unit,
                quantity = quantity.toDoubleOrNull() ?: 0.0,
                costPerUnit = costPerUnit.toDoubleOrNull() ?: 0.0,
                totalCost = (costPerUnit.toDoubleOrNull() ?: 0.0) * (quantity.toDoubleOrNull() ?: 0.0),
                date = Date(selectedDateMillis ?: Date().time),
                notes = notes
            )
            onSave(input)
        }) {
            Text("Save")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = onCancel) {
            Text("Cancel")
        }
    }
}
