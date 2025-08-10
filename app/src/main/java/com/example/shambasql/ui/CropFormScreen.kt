package com.example.shambasql.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import com.example.shambasql.model.Crop
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropFormScreen(
    initialCrop: Crop?,
    fieldId: Long,
    onSave: (Crop) -> Unit,
    onCancel: () -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val context = LocalContext.current

    var name by remember { mutableStateOf(initialCrop?.name ?: "") }
    var type by remember { mutableStateOf(initialCrop?.type ?: "") }
    var season by remember { mutableStateOf(initialCrop?.season ?: "") }

    var startDateString by remember { mutableStateOf(dateFormat.format(initialCrop?.startDate ?: Date())) }
    var endDateString by remember { mutableStateOf(dateFormat.format(initialCrop?.endDate ?: Date())) }

    val startPickerState = rememberDatePickerState()
    var showStartPicker by remember { mutableStateOf(false) }

    val endPickerState = rememberDatePickerState()
    var showEndPicker by remember { mutableStateOf(false) }

    var isActive by remember { mutableStateOf(initialCrop?.isActive ?: false) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Crop Details", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("Type") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = season, onValueChange = { season = it }, label = { Text("Season") }, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(8.dp))

        // Start date picker field
        OutlinedTextField(
            value = startDateString,
            onValueChange = {},
            label = { Text("Start Date") },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { showStartPicker = true }) {
                    Icon(imageVector = Icons.Default.DateRange, contentDescription = "Select start date")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        if (showStartPicker) {
            DatePickerDialog(
                onDismissRequest = { showStartPicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        startPickerState.selectedDateMillis?.let {
                            startDateString = dateFormat.format(Date(it))
                        }
                        showStartPicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showStartPicker = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = startPickerState)
            }
        }

        // End date picker field
        OutlinedTextField(
            value = endDateString,
            onValueChange = {},
            label = { Text("End Date") },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { showEndPicker = true }) {
                    Icon(imageVector = Icons.Default.DateRange, contentDescription = "Select end date")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        if (showEndPicker) {
            DatePickerDialog(
                onDismissRequest = { showEndPicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        endPickerState.selectedDateMillis?.let {
                            endDateString = dateFormat.format(Date(it))
                        }
                        showEndPicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showEndPicker = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = endPickerState)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = isActive, onCheckedChange = { isActive = it })
            Text("Active crop")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = {
                val cropToSave = Crop(
                    id = initialCrop?.id ?: 0L,
                    fieldId = fieldId,
                    name = name,
                    type = type,
                    season = season,
                    startDate = dateFormat.parse(startDateString) ?: Date(),
                    endDate = dateFormat.parse(endDateString) ?: Date(),
                    isActive = isActive
                )
                onSave(cropToSave)
            }) {
                Text("Save")
            }

            OutlinedButton(onClick = onCancel) {
                Text("Cancel")
            }
        }
    }
}
