package com.example.employeemanagementsystemlab

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
class EmployeeRepository(context: Context) {
    val database = AppDatabase.getInstance(context)
    private val employeeDao = database.employeeDao()
    private val departmentDao = database.departmentDao()
    suspend fun getAllEmployees(): List<Employee> =
        withContext(Dispatchers.IO) {
            employeeDao.getAllEmployees()
        }
    suspend fun insertEmployee(employee: Employee) =
        withContext(Dispatchers.IO) {
            employeeDao.insertEmployee(employee)
        }
    suspend fun getAllDepartments(): List<Department> =
        withContext(Dispatchers.IO) {
            departmentDao.getAllDepartments()
        }
    suspend fun insertDepartment(department: Department) =
        withContext(Dispatchers.IO) {
            departmentDao.insertDepartment(department)
        }
    suspend fun updateDepartment(department: Department) =
        withContext(Dispatchers.IO) {
            departmentDao.updateDepartment(department)
        }
    suspend fun updateEmployee(employee: Employee) =
        withContext(Dispatchers.IO) {
            employeeDao.updateEmployee(employee)
        }
    suspend fun deleteEmployee(employee: Employee) =
        withContext(Dispatchers.IO) {
            employeeDao.deleteEmployee(employee)
        }
    suspend fun deleteDepartment (department: Department) =
        withContext(Dispatchers.IO) {
            departmentDao.deleteDepartment(department)
        }
    suspend fun resetDb() = withContext(Dispatchers.IO){
        database.runInTransaction{
            runBlocking {
                database.clearAllTables()
                database.employeeDao().clearPrimaryKeyIndex()
            }
        }
    }
    suspend fun getEmployeesByDepartment(departmentId: Int): List<Employee> {
        return employeeDao.getEmployeesByDepartment(departmentId)
    }
}