package com.example.employeemanagementsystemlab

import androidx.room.*
import androidx.room.Dao
import androidx.room.Query
import com.example.employeemanagementsystemlab.Department
import com.example.employeemanagementsystemlab.Employee

@Dao
interface DepartmentDao {
    @Query("SELECT * FROM departments")
    fun getAllDepartments(): List<Department>
    @Insert
    fun insertDepartment(department: Department)
    @Update
    fun updateDepartment(department: Department)
    @Delete
    fun deleteDepartment(department: Department)
    @Query("DELETE FROM sqlite_sequence")
    fun clearPrimaryKeyIndex()
}
