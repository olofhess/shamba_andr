package com.example.shambasql.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import com.example.shambasql.model.*
import com.example.shambasql.sql.ShambaDatabaseHelper
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CropDetailScreen(
    dbHelper: ShambaDatabaseHelper,
    crop: Crop,
    field: FarmField,
    onBack: () -> Unit
) {
    val tasks = remember { mutableStateListOf<CropTask>() }
    val inputs = remember { mutableStateListOf<CropInput>() }
    val harvests = remember { mutableStateListOf<CropHarvest>() }

    var showTaskForm by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<CropTask?>(null) }

    var showInputForm by remember { mutableStateOf(false) }
    var inputToEdit by remember { mutableStateOf<CropInput?>(null) }

    var showHarvestForm by remember { mutableStateOf(false) }
    var harvestToEdit by remember { mutableStateOf<CropHarvest?>(null) }

    LaunchedEffect(crop.id) {
        tasks.clear()
        tasks.addAll(dbHelper.getTasksForCrop(crop.id))
        inputs.clear()
        inputs.addAll(dbHelper.getInputsForCrop(crop.id))
        harvests.clear()
        harvests.addAll(dbHelper.getHarvestsForCrop(crop.id))
    }

    val shortDateFormatter = remember {
        SimpleDateFormat("yy-MM-dd", Locale.getDefault())
    }

    Column(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .fillMaxSize()
            .border(2.dp, Color.Black)
            .padding(12.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "${crop.name} in ${field.name} (${field.area} ha)",
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = onBack) {
            Text("Back", fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Section("Tasks", tasks.map {
            "${shortDateFormatter.format(it.date)} - ${it.name}: ${it.quantity} ${it.unitType} "
        },
            onAddClick = {
                taskToEdit = null
                showTaskForm = true
            },
            onEditClick = {
                taskToEdit = tasks[it]
                showTaskForm = true
            },
            onDeleteClick = {
                dbHelper.deleteTaskById(tasks[it].id)
                tasks.removeAt(it)
            },
            backgroundColor = Color(0xFFFFFF99)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Section("Inputs", inputs.map {
            "${shortDateFormatter.format(it.date)} - ${it.name}: ${it.quantity} ${it.unit}"
        },
            onAddClick = {
                inputToEdit = null
                showInputForm = true
            },
            onEditClick = {
                inputToEdit = inputs[it]
                showInputForm = true
            },
            onDeleteClick = {
                dbHelper.deleteInputById(inputs[it].id)
                inputs.removeAt(it)
            },
            backgroundColor = Color(0xFF99FF99)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Section("Harvests", harvests.map {
            "${shortDateFormatter.format(it.harvestDate)} - ${it.unitType}: ${it.quantity}"
        },
            onAddClick = {
                harvestToEdit = null
                showHarvestForm = true
            },
            onEditClick = {
                harvestToEdit = harvests[it]
                showHarvestForm = true
            },
            onDeleteClick = {
                dbHelper.deleteHarvestById(harvests[it].id)
                harvests.removeAt(it)
            },
            backgroundColor = Color(0xFFFF99FF)
        )

        Spacer(modifier = Modifier.height(80.dp))
    }

    if (showTaskForm) {
        Dialog(
            onDismissRequest = { showTaskForm = false },
            properties = DialogProperties(usePlatformDefaultWidth = true)
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            ) {
                CropTaskFormScreen(
                    initialTask = taskToEdit,
                    fieldId = field.id,
                    cropId = crop.id,
                    onSave = {
                        if (taskToEdit == null) tasks.add(it) else {
                            tasks.remove(taskToEdit)
                            tasks.add(it)
                        }
                        dbHelper.insertTask(it)
                        showTaskForm = false
                    },
                    onCancel = { showTaskForm = false }
                )
            }
        }
    }

    if (showInputForm) {
        Dialog(
            onDismissRequest = { showInputForm = false },
            properties = DialogProperties(usePlatformDefaultWidth = true)
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            ) {
                CropInputFormScreen(
                    initialInput = inputToEdit,
                    fieldId = field.id,
                    cropId = crop.id,
                    fieldArea = field.area,
                    onSave = {
                        if (inputToEdit == null) inputs.add(it) else {
                            inputs.remove(inputToEdit)
                            inputs.add(it)
                        }
                        dbHelper.insertInput(it)
                        showInputForm = false
                    },
                    onCancel = { showInputForm = false }
                )
            }
        }
    }

    if (showHarvestForm) {
        Dialog(
            onDismissRequest = { showHarvestForm = false },
            properties = DialogProperties(usePlatformDefaultWidth = true)
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            ) {
                CropHarvestFormScreen(
                    initialHarvest = harvestToEdit,
                    fieldId = field.id,
                    cropId = crop.id,
                    fieldArea = field.area,
                    onSave = {
                        if (harvestToEdit == null) harvests.add(it) else {
                            harvests.remove(harvestToEdit)
                            harvests.add(it)
                        }
                        dbHelper.insertHarvest(it)
                        showHarvestForm = false
                    },
                    onCancel = { showHarvestForm = false }
                )
            }
        }
    }
}

@Composable
fun Section(
    title: String,
    items: List<String>,
    onAddClick: () -> Unit,
    onEditClick: (Int) -> Unit,
    onDeleteClick: (Int) -> Unit,
    backgroundColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, fontSize = 24.sp)
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = onAddClick) {
                Text("+", fontSize = 32.sp)
            }
        }
        items.forEachIndexed { index, label ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = label,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    softWrap = true,
                    maxLines = 3
                )
                IconButton(onClick = { onEditClick(index) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = { onDeleteClick(index) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}
