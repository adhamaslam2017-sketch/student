package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.data.AppDatabase
import com.example.data.repository.SchoolRepository
import com.example.ui.navigation.MainSchoolApp
import com.example.ui.theme.StudentManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = SchoolRepository(
            studentDao = database.studentDao(),
            attendanceDao = database.attendanceDao(),
            subjectDao = database.subjectDao(),
            gradeDao = database.gradeDao()
        )

        setContent {
            StudentManagerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainSchoolApp(repository = repository)
                }
            }
        }
    }
}
