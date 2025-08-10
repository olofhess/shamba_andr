package com.example.shambasql.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.systemBarsPadding
import com.example.shambasql.model.FarmField
import com.example.shambasql.sql.ShambaDatabaseHelper

@Composable
fun FieldListScreen(
    dbHelper: ShambaDatabaseHelper,
    onAddField: () -> Unit,
    onEditField: (FarmField) -> Unit,
    onDeleteField: (FarmField) -> Unit,
    onViewCrops: (FarmField) -> Unit,
    onMeasureField: (FarmField) -> Unit,
) {
    val fields = remember { mutableStateListOf<FarmField>() }

    LaunchedEffect(Unit) {
        fields.clear()
        fields.addAll(dbHelper.getAllFields())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "My Fields",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            Button(onClick = onAddField) { Text("Add Field") }
        }

        Spacer(Modifier.height(12.dp))

        if (fields.isEmpty()) {
            Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                Text(
                    "No fields yet. Tap 'Add Field' to create one.",
                    modifier = Modifier.padding(12.dp)
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(fields, key = { it.id }) { field ->
                    FieldRow(
                        field = field,
                        onEdit = { onEditField(field) },
                        onDelete = {
                            onDeleteField(field)
                            fields.remove(field)
                        },
                        onViewCrops = { onViewCrops(field) },
                        onMeasure = { onMeasureField(field) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FieldRow(
    field: FarmField,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onViewCrops: () -> Unit,
    onMeasure: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFCCCCCC)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("${field.name}  —  ${field.area} ha", style = MaterialTheme.typography.titleMedium)
            if (field.comments.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(field.comments, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = onViewCrops, modifier = Modifier.weight(1f)) {
                    Text("View Crops")
                }
                OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f)) {
                    Text("Edit")
                }
                OutlinedButton(onClick = onDelete, modifier = Modifier.weight(1f)) {
                    Text("Delete")
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Button(onClick = onMeasure, modifier = Modifier.fillMaxWidth()) {
                    Text("Measure this field")
                }
            }
        }
    }
}
