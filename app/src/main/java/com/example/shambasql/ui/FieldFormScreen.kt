package com.example.shambasql.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shambasql.model.FarmField

@Composable
fun FieldFormScreen(
    onSave: (FarmField) -> Unit,
    onCancel: () -> Unit,
    initialField: FarmField? = null  // ✅ Supports editing
) {
    var name by remember { mutableStateOf(initialField?.name ?: "") }
    var areaText by remember { mutableStateOf(initialField?.area?.toString() ?: "") }
    var comments by remember { mutableStateOf(initialField?.comments ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (initialField == null) "Add New Field" else "Edit Field",
            style = MaterialTheme.typography.headlineSmall
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Field Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = areaText,
            onValueChange = { areaText = it },
            label = { Text("Area (hectares)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = comments,
            onValueChange = { comments = it },
            label = { Text("Comments") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = {
                    val area = areaText.toDoubleOrNull() ?: 0.0
                    val field = FarmField(
                        id = initialField?.id ?: 0,
                        name = name,
                        area = area,
                        comments = comments
                    )
                    onSave(field)
                }
            ) {
                Text("Save")
            }

            OutlinedButton(onClick = onCancel) {
                Text("Cancel")
            }
        }
    }
}
