package com.example.pocstriperm2.ui.theme.viewmodel

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import com.example.pocstriperm2.data.local.model.BluetoothDeviceEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BluetoothViewModel : ViewModel() {
    private val _devices = MutableStateFlow<List<BluetoothDeviceEntity>>(emptyList())
    val devices: StateFlow<List<BluetoothDeviceEntity>> = _devices

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun scanDevices(context: Context) {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            Log.e("BluetoothViewModel", "Bluetooth não disponível ou desativado")
            return
        }

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                val action = intent.action
                if (BluetoothDevice.ACTION_FOUND == action) {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.let {
                        Log.d("BluetoothViewModel", "Encontrado: ${it.name} - ${it.address}")
                        val entity = BluetoothDeviceEntity(it.address, it.name)
                        _devices.value = _devices.value + entity
                    }
                }
            }
        }
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        context.registerReceiver(receiver, filter)
        bluetoothAdapter.startDiscovery()
    }
}