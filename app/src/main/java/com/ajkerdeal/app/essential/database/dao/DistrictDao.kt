package com.ajkerdeal.app.essential.database.dao

import androidx.room.*
import com.ajkerdeal.app.essential.api.models.district.DistrictData
import com.ajkerdeal.app.essential.fcm.FCMData

@Dao
interface DistrictDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<DistrictData>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(model: DistrictData): Long

    @Query("SELECT * FROM district_table")
    suspend fun getAllDistrict(): List<DistrictData>

    @Query("SELECT * FROM district_table WHERE districtId = :districtId")
    suspend fun getDistrictById(districtId: Int): DistrictData

    @Query("SELECT * FROM district_table WHERE parentId = :parentId")
    suspend fun getDistrictByParentId(parentId: Int): List<DistrictData>

    @Update
    suspend fun updateDistrict(model: DistrictData): Int

    @Query("DELETE FROM district_table WHERE uid = :id")
    suspend fun deleteDistrictById(id: Int): Int

    @Query("DELETE FROM district_table")
    suspend fun deleteAllDistrict(): Int
}