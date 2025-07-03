package com.example.pocstriperm2

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pocstriperm2.ui.theme.viewmodel.BluetoothViewModel

class MainActivity : ComponentActivity() {
    private lateinit var bluetoothViewModel: BluetoothViewModel

    private val requestBluetoothScanPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            bluetoothViewModel.scanDevices(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            bluetoothViewModel = viewModel()
            val devices by bluetoothViewModel.devices.collectAsState()

            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = { checkAndRequestBluetoothScanPermission() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Scan Devices")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Dispositivos encontrados:")
                    LazyColumn {
                        items(devices) { device ->
                            Text("${device.name ?: "Sem nome"} - ${device.address}")
                        }
                    }
                }
            }
        }
    }

    private fun checkAndRequestBluetoothScanPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestBluetoothScanPermission.launch(Manifest.permission.BLUETOOTH_SCAN)
        } else {
            bluetoothViewModel.scanDevices(this)
        }
    }
}