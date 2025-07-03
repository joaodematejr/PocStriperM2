package com.example.pocstriperm2.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pocstriperm2.data.local.model.BluetoothDeviceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BluetoothDeviceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(device: BluetoothDeviceEntity)

    @Query("SELECT * FROM devices")
    fun getAll(): Flow<List<BluetoothDeviceEntity>>
}