package com.example.shambasql

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.shambasql.model.Crop
import com.example.shambasql.sql.ShambaDatabaseHelper
import com.example.shambasql.ui.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dbHelper = ShambaDatabaseHelper(this)

        setContent {
            var currentScreen by remember { mutableStateOf("welcome") }
            var selectedFieldId by remember { mutableStateOf(0L) }
            var selectedCrop by remember { mutableStateOf<Crop?>(null) }

            when (currentScreen) {
                "welcome" -> WelcomeScreen(
                    onContinue = { currentScreen = "fields" }
                )

                "fields" -> FieldListScreen(
                    dbHelper = dbHelper,
                    onAddField = { currentScreen = "addfield" },
                    onEditField = { field ->
                        selectedFieldId = field.id
                        currentScreen = "editfield"
                    },
                    onDeleteField = { field ->
                        dbHelper.deleteFieldById(field.id)
                    },
                    onViewCrops = { field ->
                        selectedFieldId = field.id
                        currentScreen = "crops"
                    },
                    onMeasureField = { field ->
                        selectedFieldId = field.id
                        currentScreen = "measure"
                    }
                )

                "addfield" -> FieldFormScreen(
                    onSave = {
                        dbHelper.insertField(it)
                        currentScreen = "fields"
                    },
                    onCancel = { currentScreen = "fields" }
                )

                "editfield" -> {
                    val field = dbHelper.getFieldById(selectedFieldId)
                    if (field != null) {
                        FieldFormScreen(
                            initialField = field,
                            onSave = {
                                dbHelper.insertField(it)  // upsert as before
                                currentScreen = "fields"
                            },
                            onCancel = { currentScreen = "fields" }
                        )
                    }
                }

                "crops" -> {
                    val field = dbHelper.getFieldById(selectedFieldId)
                    if (field != null) {
                        CropListScreen(
                            dbHelper = dbHelper,
                            field = field,
                            onAddCrop = { currentScreen = "addcrop" },
                            onEditCrop = {
                                selectedCrop = it
                                currentScreen = "editcrop"
                            },
                            onDeleteCrop = {
                                dbHelper.deleteCropById(it.id)
                                currentScreen = "crops" // refresh
                            },
                            onShowCropDetails = {
                                selectedCrop = it
                                currentScreen = "cropdetails"
                            },
                            onBack = { currentScreen = "fields" }
                        )
                    }
                }

                "addcrop" -> {
                    val field = dbHelper.getFieldById(selectedFieldId)
                    if (field != null) {
                        CropFormScreen(
                            fieldId = field.id,
                            initialCrop = null,
                            onSave = {
                                dbHelper.insertCrop(it)
                                currentScreen = "crops"
                            },
                            onCancel = { currentScreen = "crops" }
                        )
                    }
                }

                "editcrop" -> {
                    val crop = selectedCrop
                    if (crop != null) {
                        CropFormScreen(
                            fieldId = crop.fieldId,
                            initialCrop = crop,
                            onSave = {
                                dbHelper.updateCrop(it)
                                currentScreen = "crops"
                            },
                            onCancel = { currentScreen = "crops" }
                        )
                    }
                }

                "cropdetails" -> {
                    val crop = selectedCrop
                    if (crop != null && selectedFieldId != 0L) {
                        val selectedField = dbHelper.getFieldById(selectedFieldId)
                        if (selectedField != null) {
                            CropDetailScreen(
                                dbHelper = dbHelper,
                                crop = crop,
                                field = selectedField,
                                onBack = { currentScreen = "crops" }
                            )
                        } else {
                            currentScreen = "crops"
                        }
                    } else {
                        currentScreen = "crops"
                    }
                }

                // NEW: boundary measurement (standalone tool)
                "measure" -> {
                    BoundaryMappingScreen(
                        fieldId = selectedFieldId,
                        onFinish = { currentScreen = "fields" }
                    )
                }
            }
        }
    }
}
