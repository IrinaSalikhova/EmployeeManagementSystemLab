package com.example.employeemanagementsystemlab

import androidx.room.*
import androidx.room.Dao
import androidx.room.Query
import com.example.employeemanagementsystemlab.Employee

@Dao
interface EmployeeDao {
    @Query("SELECT * FROM employees")
    fun getAllEmployees(): List<Employee>
    @Insert
    fun insertEmployee(employee: Employee)
    @Update
    fun updateEmployee(employee: Employee)
    @Delete
    fun deleteEmployee(employee: Employee)
    @Query("DELETE FROM sqlite_sequence")
    fun clearPrimaryKeyIndex()
    @Query("SELECT * FROM employees WHERE departmentId = :departmentId")
    fun getEmployeesByDepartment(departmentId: Int): List<Employee>
}