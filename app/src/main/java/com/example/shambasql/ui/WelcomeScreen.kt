package com.example.shambasql.ui

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.shambasql.R

@Composable
fun WelcomeScreen(onContinue: () -> Unit) {
    val context = LocalContext.current

    // Load version safely
    val versionName = remember {
        try {
            val pkgInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pkgInfo.versionName ?: ""
        } catch (_: Exception) {
            ""
        }
    }

    // Persistent farm info state
    var farmName by remember { mutableStateOf("") }
    var farmerName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val (f, r, a) = loadFarmInfo(context)
        farmName = f
        farmerName = r
        address = a
    }

    var showEdit by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .systemBarsPadding()
            .fillMaxSize()
    ) {
        // Main content
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.maize),
                contentDescription = "Maize growth comparison",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Farm details card
            FarmInfoCard(
                farmName = farmName,
                farmerName = farmerName,
                address = address,
                onEdit = { showEdit = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Farmers' Logbook!", fontSize = 28.sp)

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Get Started", fontSize = 20.sp)
            }
        }

        // App version at the bottom
        if (versionName.isNotBlank()) {
            Text(
                text = "v$versionName",
                fontSize = 16.sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(12.dp)
            )
        }
    }

    // Edit dialog
    if (showEdit) {
        EditFarmInfoDialog(
            initialFarmName = farmName,
            initialFarmerName = farmerName,
            initialAddress = address,
            onSave = { newFarm, newFarmer, newAddr ->
                saveFarmInfo(context, newFarm, newFarmer, newAddr)
                farmName = newFarm.trim()
                farmerName = newFarmer.trim()
                address = newAddr.trim()
                showEdit = false
            },
            onCancel = { showEdit = false }
        )
    }
}

@Composable
private fun FarmInfoCard(
    farmName: String,
    farmerName: String,
    address: String,
    onEdit: () -> Unit
) {
    val hasAny = farmName.isNotBlank() || farmerName.isNotBlank() || address.isNotBlank()

    Surface(
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 4.dp,
        shadowElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, Color.Black, MaterialTheme.shapes.medium)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (hasAny) {
                if (farmName.isNotBlank()) Text(
                    " $farmName",
                    fontSize = 20.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                if (farmerName.isNotBlank()) Text(
                    " $farmerName",
                    fontSize = 20.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                if (address.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        address,
                        fontSize = 20.sp,
                        lineHeight = 18.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onEdit) { Text("Edit") }
            } else {
                Text(
                    "No farm details yet.",
                    fontSize = 20.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onEdit) { Text("Add farm details") }
            }
        }
    }
}

@Composable
private fun EditFarmInfoDialog(
    initialFarmName: String,
    initialFarmerName: String,
    initialAddress: String,
    onSave: (farm: String, farmer: String, address: String) -> Unit,
    onCancel: () -> Unit
) {
    var farm by remember { mutableStateOf(initialFarmName) }
    var farmer by remember { mutableStateOf(initialFarmerName) }
    var addr by remember { mutableStateOf(initialAddress) }

    val canSave = (farm.isNotBlank() || farmer.isNotBlank())

    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = true)
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
            shadowElevation = 3.dp,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Farm details", fontSize = 20.sp)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = farm,
                    onValueChange = { farm = it },
                    label = { Text("Farm name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = farmer,
                    onValueChange = { farmer = it },
                    label = { Text("Farmer name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = addr,
                    onValueChange = { addr = it },
                    label = { Text("Address") },
                    singleLine = false,
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onCancel) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onSave(farm, farmer, addr) }, enabled = canSave) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

/* ----------------- SharedPreferences helpers ----------------- */

private const val PREFS_NAME = "farm_prefs"
private const val KEY_FARM = "farm_name"
private const val KEY_FARMER = "farmer_name"
private const val KEY_ADDRESS = "address"

private fun loadFarmInfo(context: Context): Triple<String, String, String> {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val farm = prefs.getString(KEY_FARM, "") ?: ""
    val farmer = prefs.getString(KEY_FARMER, "") ?: ""
    val addr = prefs.getString(KEY_ADDRESS, "") ?: ""
    return Triple(farm, farmer, addr)
}

private fun saveFarmInfo(context: Context, farm: String, farmer: String, address: String) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(KEY_FARM, farm.trim())
        .putString(KEY_FARMER, farmer.trim())
        .putString(KEY_ADDRESS, address.trim())
        .apply()
}
