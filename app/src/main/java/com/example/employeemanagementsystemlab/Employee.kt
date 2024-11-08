package com.example.employeemanagementsystemlab

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "employees",
    foreignKeys = [ForeignKey(
        entity = Department::class,
        parentColumns = ["id"],
        childColumns = ["departmentId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Employee(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val departmentId: Int )
