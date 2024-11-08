package com.example.employeemanagementsystemlab

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "departments")
data class Department(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)