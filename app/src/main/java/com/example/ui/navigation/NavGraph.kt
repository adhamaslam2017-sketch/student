package com.example.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.data.repository.SchoolRepository
import com.example.ui.screens.*
import com.example.ui.viewmodel.*

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "الرئيسية", Icons.Default.Dashboard)
    object Students : Screen("students", "الطلاب", Icons.Default.People)
    object Attendance : Screen("attendance", "الحضور", Icons.Default.EventAvailable)
    object Subjects : Screen("subjects", "المواد", Icons.Default.Book)
    object Grades : Screen("grades", "الدرجات", Icons.Default.AssignmentTurnedIn)
}

@Composable
fun MainSchoolApp(repository: SchoolRepository) {
    val factory = SchoolViewModelFactory(repository)
    val navController = rememberNavController()

    val dashboardViewModel: DashboardViewModel = viewModel(factory = factory)
    val studentViewModel: StudentViewModel = viewModel(factory = factory)
    val attendanceViewModel: AttendanceViewModel = viewModel(factory = factory)
    val subjectViewModel: SubjectViewModel = viewModel(factory = factory)
    val gradeViewModel: GradeViewModel = viewModel(factory = factory)

    val items = listOf(
        Screen.Dashboard,
        Screen.Students,
        Screen.Attendance,
        Screen.Subjects,
        Screen.Grades
    )

    // Force Right-To-Left (RTL) Layout Direction for Arabic
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Dashboard.route) {
                    DashboardScreen(
                        viewModel = dashboardViewModel,
                        onNavigateToStudents = { navController.navigate(Screen.Students.route) },
                        onNavigateToAttendance = { navController.navigate(Screen.Attendance.route) },
                        onNavigateToGrades = { navController.navigate(Screen.Grades.route) },
                        onNavigateToSubjects = { navController.navigate(Screen.Subjects.route) }
                    )
                }
                composable(Screen.Students.route) {
                    StudentListScreen(viewModel = studentViewModel)
                }
                composable(Screen.Attendance.route) {
                    AttendanceScreen(viewModel = attendanceViewModel)
                }
                composable(Screen.Subjects.route) {
                    SubjectListScreen(viewModel = subjectViewModel)
                }
                composable(Screen.Grades.route) {
                    GradesScreen(
                        gradeViewModel = gradeViewModel,
                        subjectViewModel = subjectViewModel
                    )
                }
            }
        }
    }
}
