package com.example.pocstriperm2.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "devices")
data class BluetoothDeviceEntity(
    @PrimaryKey val address: String,
    val name: String?
)