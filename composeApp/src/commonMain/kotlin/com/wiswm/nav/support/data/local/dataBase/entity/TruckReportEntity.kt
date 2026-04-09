package com.wiswm.nav.support.data.local.dataBase.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Entity(tableName = "TruckReportEntity")
data class TruckReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val key: String,
    val name: String,
    val value: String?,
    val photoPath: String?,
    val checkedStatus: Boolean
){
    @Serializable
    data class TruckReportParam(
        val key: String,
        val name: String,
        val value: String,
    )

    companion object{
        fun convertTruckReportEntityListToString(truckReportEntityList: List<TruckReportEntity>): String{
            val truckReportParamList = mutableListOf<TruckReportParam>()
            truckReportEntityList.forEach { truckReportEntity ->
                truckReportParamList.add(TruckReportParam(
                    key = truckReportEntity.key,
                    name = truckReportEntity.name,
                    value = truckReportEntity.value ?: "OK")
                )
            }
            val json = Json { ignoreUnknownKeys = true }
            return json.encodeToString(truckReportParamList)
        }

        fun resetTruckReportEntityList(truckReportEntityList: List<TruckReportEntity>): List<TruckReportEntity>{
            return truckReportEntityList.map { truckReportEntity ->
                truckReportEntity.copy(value = null, photoPath = null, checkedStatus = false)
            }
        }
    }
}