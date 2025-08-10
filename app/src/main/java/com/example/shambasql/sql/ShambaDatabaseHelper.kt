package com.example.shambasql.sql

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.shambasql.model.*
import java.text.SimpleDateFormat
import java.util.*

class ShambaDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "shamba_db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE farm_fields (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                area REAL,
                comments TEXT
            )
            """
        )
        db.execSQL(
            """
            CREATE TABLE crops (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                fieldId INTEGER,
                name TEXT,
                type TEXT,
                season TEXT,
                startDate TEXT,
                endDate TEXT,
                isActive INTEGER
            )
            """
        )
        db.execSQL(
            """
            CREATE TABLE crop_tasks (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                fieldId INTEGER,
                cropId INTEGER,
                name TEXT,
                date TEXT,
                unit TEXT,
                costPerUnit REAL,
                quantity REAL,
                cost REAL,
                notes TEXT
            )
            """
        )
        db.execSQL(
            """
            CREATE TABLE crop_inputs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                fieldId INTEGER,
                cropId INTEGER,
                name TEXT,
                inputType TEXT,
                unit TEXT,
                quantity REAL,
                costPerUnit REAL,
                totalCost REAL,
                date TEXT,
                notes TEXT
            )
            """
        )
        db.execSQL(
            """
            CREATE TABLE crop_harvests (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                fieldId INTEGER,
                cropId INTEGER,
                unitType TEXT,
                quantity REAL,
                valuePerUnit REAL,
                totalValue REAL,
                harvestDate TEXT
            )
            """
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // For future schema upgrades
    }

    // --- FarmField CRUD ---
    fun insertField(field: FarmField) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", field.name)
            put("area", field.area)
            put("comments", field.comments)
        }
        if (field.id == 0L) {
            db.insert("farm_fields", null, values)
        } else {
            db.update("farm_fields", values, "id = ?", arrayOf(field.id.toString()))
        }
    }

    fun getAllFields(): List<FarmField> {
        val fields = mutableListOf<FarmField>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM farm_fields", null)
        with(cursor) {
            while (moveToNext()) {
                fields.add(
                    FarmField(
                        id = getLong(getColumnIndexOrThrow("id")),
                        name = getString(getColumnIndexOrThrow("name")),
                        area = getDouble(getColumnIndexOrThrow("area")),
                        comments = getString(getColumnIndexOrThrow("comments"))
                    )
                )
            }
            close()
        }
        return fields
    }

    fun getFieldById(id: Long): FarmField? {
        return getAllFields().find { it.id == id }
    }

    fun deleteField(id: Long) {
        writableDatabase.delete("farm_fields", "id = ?", arrayOf(id.toString()))
    }

    fun deleteFieldById(id: Long) {
        deleteField(id)
    }
    fun deleteTaskById(id: Long) {
        writableDatabase.delete("crop_tasks", "id = ?", arrayOf(id.toString()))
    }

    fun deleteInputById(id: Long) {
        writableDatabase.delete("crop_inputs", "id = ?", arrayOf(id.toString()))
    }

    fun deleteHarvestById(id: Long) {
        writableDatabase.delete("crop_harvests", "id = ?", arrayOf(id.toString()))
    }


    // --- Crop CRUD ---
    fun insertCrop(crop: Crop) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("fieldId", crop.fieldId)
            put("name", crop.name)
            put("type", crop.type)
            put("season", crop.season)
            put("startDate", formatDate(crop.startDate))
            put("endDate", formatDate(crop.endDate))
            put("isActive", if (crop.isActive) 1 else 0)
        }
        if (crop.id == 0L) {
            db.insert("crops", null, values)
        } else {
            db.update("crops", values, "id = ?", arrayOf(crop.id.toString()))
        }
    }

    fun updateCrop(crop: Crop) {
        insertCrop(crop)
    }

    fun getCropsForField(fieldId: Long): List<Crop> {
        val crops = mutableListOf<Crop>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM crops WHERE fieldId = ?", arrayOf(fieldId.toString()))
        with(cursor) {
            while (moveToNext()) {
                crops.add(
                    Crop(
                        id = getLong(getColumnIndexOrThrow("id")),
                        fieldId = getLong(getColumnIndexOrThrow("fieldId")),
                        name = getString(getColumnIndexOrThrow("name")),
                        type = getString(getColumnIndexOrThrow("type")),
                        season = getString(getColumnIndexOrThrow("season")),
                        startDate = parseDate(getString(getColumnIndexOrThrow("startDate"))),
                        endDate = parseDate(getString(getColumnIndexOrThrow("endDate"))),
                        isActive = getInt(getColumnIndexOrThrow("isActive")) == 1
                    )
                )
            }
            close()
        }
        return crops
    }

    fun deleteCrop(id: Long) {
        writableDatabase.delete("crops", "id = ?", arrayOf(id.toString()))
    }

    fun deleteCropById(id: Long) {
        deleteCrop(id)
    }

    // --- CropTask CRUD ---
    fun insertTask(task: CropTask) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("fieldId", task.fieldId)
            put("cropId", task.cropId)
            put("name", task.name)
            put("date", formatDate(task.date))
            put("unit", task.unitType)
            put("costPerUnit", task.costPerUnit)
            put("quantity", task.quantity)
            put("cost", task.cost)
            put("notes", task.notes)
        }
        if (task.id == 0L) {
            db.insert("crop_tasks", null, values)
        } else {
            db.update("crop_tasks", values, "id = ?", arrayOf(task.id.toString()))
        }
    }

    fun getTasksForCrop(cropId: Long): List<CropTask> {
        val tasks = mutableListOf<CropTask>()
        val cursor = readableDatabase.rawQuery("SELECT * FROM crop_tasks WHERE cropId = ?", arrayOf(cropId.toString()))
        with(cursor) {
            while (moveToNext()) {
                tasks.add(
                    CropTask(
                        id = getLong(getColumnIndexOrThrow("id")),
                        fieldId = getLong(getColumnIndexOrThrow("fieldId")),
                        cropId = getLong(getColumnIndexOrThrow("cropId")),
                        name = getString(getColumnIndexOrThrow("name")),
                        date = parseDate(getString(getColumnIndexOrThrow("date"))),
                        unitType = getString(getColumnIndexOrThrow("unit")),
                        costPerUnit = getDouble(getColumnIndexOrThrow("costPerUnit")),
                        quantity = getDouble(getColumnIndexOrThrow("quantity")),
                        cost = getDouble(getColumnIndexOrThrow("cost")),
                        notes = getString(getColumnIndexOrThrow("notes"))
                    )
                )
            }
            close()
        }
        return tasks
    }

    fun deleteTask(id: Long) {
        writableDatabase.delete("crop_tasks", "id = ?", arrayOf(id.toString()))
    }

    // --- CropInput CRUD ---
    fun insertInput(input: CropInput) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("fieldId", input.fieldId)
            put("cropId", input.cropId)
            put("name", input.name)
            put("inputType", input.inputType)
            put("unit", input.unit)
            put("quantity", input.quantity)
            put("costPerUnit", input.costPerUnit)
            put("totalCost", input.totalCost)
            put("date", formatDate(input.date))
            put("notes", input.notes)
        }
        if (input.id == 0L) {
            db.insert("crop_inputs", null, values)
        } else {
            db.update("crop_inputs", values, "id = ?", arrayOf(input.id.toString()))
        }
    }

    fun getInputsForCrop(cropId: Long): List<CropInput> {
        val inputs = mutableListOf<CropInput>()
        val cursor = readableDatabase.rawQuery("SELECT * FROM crop_inputs WHERE cropId = ?", arrayOf(cropId.toString()))
        with(cursor) {
            while (moveToNext()) {
                inputs.add(
                    CropInput(
                        id = getLong(getColumnIndexOrThrow("id")),
                        fieldId = getLong(getColumnIndexOrThrow("fieldId")),
                        cropId = getLong(getColumnIndexOrThrow("cropId")),
                        name = getString(getColumnIndexOrThrow("name")),
                        inputType = getString(getColumnIndexOrThrow("inputType")),
                        unit = getString(getColumnIndexOrThrow("unit")),
                        quantity = getDouble(getColumnIndexOrThrow("quantity")),
                        costPerUnit = getDouble(getColumnIndexOrThrow("costPerUnit")),
                        totalCost = getDouble(getColumnIndexOrThrow("totalCost")),
                        date = parseDate(getString(getColumnIndexOrThrow("date"))),
                        notes = getString(getColumnIndexOrThrow("notes"))
                    )
                )
            }
            close()
        }
        return inputs
    }

    fun deleteInput(id: Long) {
        writableDatabase.delete("crop_inputs", "id = ?", arrayOf(id.toString()))
    }

    // --- CropHarvest CRUD ---
    fun insertHarvest(harvest: CropHarvest) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("fieldId", harvest.fieldId)
            put("cropId", harvest.cropId)
            put("unitType", harvest.unitType)
            put("quantity", harvest.quantity)
            put("valuePerUnit", harvest.valuePerUnit)
            put("totalValue", harvest.totalValue)
            put("harvestDate", formatDate(harvest.harvestDate))
        }
        if (harvest.id == 0L) {
            db.insert("crop_harvests", null, values)
        } else {
            db.update("crop_harvests", values, "id = ?", arrayOf(harvest.id.toString()))
        }
    }

    fun getHarvestsForCrop(cropId: Long): List<CropHarvest> {
        val harvests = mutableListOf<CropHarvest>()
        val cursor = readableDatabase.rawQuery("SELECT * FROM crop_harvests WHERE cropId = ?", arrayOf(cropId.toString()))
        with(cursor) {
            while (moveToNext()) {
                harvests.add(
                    CropHarvest(
                        id = getLong(getColumnIndexOrThrow("id")),
                        fieldId = getLong(getColumnIndexOrThrow("fieldId")),
                        cropId = getLong(getColumnIndexOrThrow("cropId")),
                        unitType = getString(getColumnIndexOrThrow("unitType")),
                        quantity = getDouble(getColumnIndexOrThrow("quantity")),
                        valuePerUnit = getDouble(getColumnIndexOrThrow("valuePerUnit")),
                        totalValue = getDouble(getColumnIndexOrThrow("totalValue")),
                        harvestDate = parseDate(getString(getColumnIndexOrThrow("harvestDate")))
                    )
                )
            }
            close()
        }
        return harvests
    }

    fun deleteHarvest(id: Long) {
        writableDatabase.delete("crop_harvests", "id = ?", arrayOf(id.toString()))
    }

    // --- Utility ---
    private fun parseDate(dateStr: String): Date {
        return try {
            SimpleDateFormat("yyyy-MM-dd").parse(dateStr) ?: Date()
        } catch (e: Exception) {
            Date()
        }
    }

    private fun formatDate(date: Date): String {
        return SimpleDateFormat("yyyy-MM-dd").format(date)
    }
}
