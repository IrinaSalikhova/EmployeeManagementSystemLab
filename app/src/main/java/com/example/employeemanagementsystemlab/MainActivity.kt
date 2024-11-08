package com.example.employeemanagementsystemlab

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MainActivity : AppCompatActivity() {
    private lateinit var repository: EmployeeRepository
    private lateinit var employeeNameEditText: EditText
    private lateinit var employeeEmailEditText: EditText
    private lateinit var departmentNameEditText: EditText
    private lateinit var addEmployeeButton: Button
    private lateinit var addDepartmentButton: Button
    private lateinit var displayDataTextView: TextView
    private lateinit var searchEmployeeEditText: EditText
    private lateinit var searchEmployeeButton: Button
    private lateinit var showEmployeesByDepartmentButton: Button



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        repository = EmployeeRepository(this)
        //lifecycleScope.launch {repository.resetDb()}

        employeeNameEditText = findViewById(R.id.employeeNameEditText)
        employeeEmailEditText =
            findViewById(R.id.employeeEmailEditText)
        departmentNameEditText =
            findViewById(R.id.departmentNameEditText)
        addEmployeeButton = findViewById(R.id.addEmployeeButton)
        addDepartmentButton = findViewById(R.id.addDepartmentButton)
        displayDataTextView = findViewById(R.id.displayDataTextView)
        addEmployeeButton.setOnClickListener { addEmployee() }
        addDepartmentButton.setOnClickListener { addDepartment() }
        searchEmployeeEditText = findViewById(R.id.searchEmployeeEditText)
        searchEmployeeButton = findViewById(R.id.searchEmployeeButton)
        searchEmployeeButton.setOnClickListener { searchEmployee() }
        showEmployeesByDepartmentButton = findViewById(R.id.showEmployeesByDepartmentButton)
        showEmployeesByDepartmentButton.setOnClickListener { showEmployeesByDepartment() }
        displayData()
    }

    private fun showEmployeesByDepartment() {
        lifecycleScope.launch(Dispatchers.IO) {  // Use Dispatchers.IO for database calls
            val departments = repository.getAllDepartments()  // This runs on background thread
            withContext(Dispatchers.Main) {  // Switch back to the main thread to update UI
                val departmentNames = departments.map { it.name }.toTypedArray()

                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Choose a Department")
                    .setItems(departmentNames) { _, which ->
                        lifecycleScope.launch(Dispatchers.IO) {  // Use Dispatchers.IO for database calls
                            val selectedDepartment = departments[which]
                            val employees = repository.getEmployeesByDepartment(selectedDepartment.id)
                            withContext(Dispatchers.Main) {  // Switch back to the main thread to update UI
                                showEmployeesInDepartmentDialog(employees, selectedDepartment.name)
                            }
                        }
                    }
                    .show()
            }
        }
    }

    private fun showEmployeesInDepartmentDialog(employees: List<Employee>, departmentName: String) {
        val employeeNames = employees.map { "${it.name} - ${it.email}" }.toTypedArray()

        AlertDialog.Builder(this@MainActivity)
            .setTitle("Employees in $departmentName")
            .setItems(employeeNames) { _, _ -> }
            .show()
    }

    private fun searchEmployee() {
        val query = searchEmployeeEditText.text.toString()
        if (query.isNotEmpty()) {
            lifecycleScope.launch {
                val employees = repository.getAllEmployees()
                val filteredEmployees = employees.filter {
                    it.name.contains(query, ignoreCase = true) || it.email.contains(
                        query,
                        ignoreCase = true
                    )
                }
                displayEmployeeSearchResults(filteredEmployees)
            }
        } else {
            displayData()
            Toast.makeText(this, "Please enter a name or email to search", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayEmployeeSearchResults(filteredEmployees: List<Employee>) {
        val employeeText = filteredEmployees.map {
            "\n${it.name}, ${it.email}, (${it.departmentId})"
        }
        val displayText = buildString {
            append("Search Results:\n")
            append(employeeText)
        }
        displayDataTextView.text = displayText
    }

    private fun addEmployee() {
        val name = employeeNameEditText.text.toString()
        val email = employeeEmailEditText.text.toString()
        if (name.isNotEmpty() && email.isNotEmpty()) {
            lifecycleScope.launch {
                if (repository.getAllDepartments().isEmpty()) {
                    Toast.makeText(
                        this@MainActivity,
                        "Please add a department first",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (repository.getAllEmployees().size == 1) {
                    repository.insertEmployee(
                        Employee(
                            name = name, email =
                            email, departmentId = 1
                        )
                    )
                    displayData()
                } else {
                    repository.insertEmployee(
                        Employee(
                            name = name, email =
                            email, departmentId = askDepartment()
                        )
                    )
                    displayData()
                }
            }
        }
    }

    private suspend fun askDepartment(): Int = suspendCoroutine { continuation ->
        lifecycleScope.launch {
            val departments = repository.getAllDepartments()
            val departmentNames = departments.map { it.name }.toTypedArray()
            AlertDialog.Builder(this@MainActivity)
                .setTitle("Choose a Department")
                .setItems(departmentNames) { dialog, which ->
                    continuation.resume(departments[which].id)
                }
                .show()
        }
    }

    private fun addDepartment() {
        val name = departmentNameEditText.text.toString()
        if (name.isNotEmpty()) {
            lifecycleScope.launch {
                repository.insertDepartment(Department(name = name))
                displayData()
            }
        }
    }

    private fun displayData() {
        lifecycleScope.launch {
            val employees = repository.getAllEmployees()
            val departments = repository.getAllDepartments()
            val employeeText = employees.map {
                "\n${it.name}, ${it.email}, (${it.departmentId})"
            }
            val departmentText = departments.map { "\n${it.name}" }
            val displayText = buildString {
                append("Employees:\n")
                append(employeeText)
                append("\n\nDepartments:\n")
                append(departmentText)
            }
            displayDataTextView.text = displayText
            displayDataTextView.setOnClickListener {
                val allItems = employeeText + departmentText
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Employees and Departments")
                    .setItems(allItems.toTypedArray()) { _, index ->
                        if (index < employees.size) {
                            showEditDialog(employees[index])
                        } else {
                            showEditDialog(departments[index - employees.size])
                        }
                    }
                    .show()
            }
        }
    }

    private fun showEditDialog(department: Department) {
        val departmentNameEditText = EditText(this).apply {
            setText(department.name)
            hint = "Department Name"
        }
        AlertDialog.Builder(this)
            .setTitle("Edit Department")
            .setView(departmentNameEditText)
            .setPositiveButton("Save") { _, _ ->
                val updatedName = departmentNameEditText.text.toString()
                if (updatedName.isNotEmpty()) {
                    lifecycleScope.launch {
                        repository.updateDepartment(department.copy(name = updatedName))
                        displayData()
                    }
                }
            }
            .setNeutralButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    repository.deleteDepartment(department)
                    displayData()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditDialog(employee: Employee) {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val employeeNameEditText = EditText(this).apply {
            setText(employee.name)
            hint = "Employee Name"
        }
        val employeeEmailEditText = EditText(this).apply {
            setText(employee.email)
            hint = "Employee Email"
        }
        val departmentIdEditText = EditText(this).apply {
            setText(employee.departmentId.toString())  // Corrected with .toString()
            hint = "Department ID"
        }

        container.addView(employeeNameEditText)
        container.addView(employeeEmailEditText)
        container.addView(departmentIdEditText)

        AlertDialog.Builder(this)
            .setTitle("Edit Employee")  // Corrected title
            .setView(container)
            .setPositiveButton("Save") { _, _ ->
                val updatedName = employeeNameEditText.text.toString()
                val updatedEmail = employeeEmailEditText.text.toString()
                val updatedDepartmentId = departmentIdEditText.text.toString()
                if (updatedName.isNotEmpty() && updatedEmail.isNotEmpty()
                    && updatedDepartmentId.isNotEmpty()) {
                    lifecycleScope.launch {
                        repository.updateEmployee(employee.copy(
                            name = updatedName,
                            email = updatedEmail,
                            departmentId = updatedDepartmentId.toInt()
                        ))
                        displayData()
                    }
                }
            }
            .setNeutralButton("Delete") { _, _ ->  // Changed to setNeutralButton
                lifecycleScope.launch {
                    repository.deleteEmployee(employee)
                    displayData()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
