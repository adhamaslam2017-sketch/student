package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.entity.Attendance
import com.example.data.entity.Grade
import com.example.data.entity.Student
import com.example.data.entity.Subject
import com.example.data.repository.SchoolRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// --- Dashboard ViewModel ---
data class DashboardUiState(
    val studentCount: Int = 0,
    val subjectCount: Int = 0,
    val presentTodayCount: Int = 0,
    val absentTodayCount: Int = 0,
    val averageScore: Double = 0.0,
    val recentStudents: List<Student> = emptyList()
)

class DashboardViewModel(private val repository: SchoolRepository) : ViewModel() {

    private val todayDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.allStudents,
        repository.allSubjects,
        repository.getAttendanceForDate(todayDate),
        repository.allGrades
    ) { students, subjects, attendanceList, grades ->
        val presentCount = attendanceList.count { it.status == "حاضر" }
        val absentCount = attendanceList.count { it.status == "غائب" }
        val avgGrade = if (grades.isNotEmpty()) grades.map { it.totalScore }.average() else 0.0

        DashboardUiState(
            studentCount = students.size,
            subjectCount = subjects.size,
            presentTodayCount = presentCount,
            absentTodayCount = absentCount,
            averageScore = avgGrade,
            recentStudents = students.take(5)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )
}

// --- Student ViewModel ---
class StudentViewModel(private val repository: SchoolRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedGradeFilter = MutableStateFlow("الكل")
    val selectedGradeFilter: StateFlow<String> = _selectedGradeFilter.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val students: StateFlow<List<Student>> = combine(_searchQuery, _selectedGradeFilter) { query, filter ->
        Pair(query, filter)
    }.flatMapLatest { (query, filter) ->
        if (query.isBlank()) {
            repository.allStudents
        } else {
            repository.searchStudents(query)
        }
    }.map { list ->
        if (_selectedGradeFilter.value != "الكل") {
            list.filter { it.gradeLevel.contains(_selectedGradeFilter.value) }
        } else list
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onGradeFilterChange(newFilter: String) {
        _selectedGradeFilter.value = newFilter
    }

    fun addStudent(student: Student) {
        viewModelScope.launch {
            repository.insertStudent(student)
        }
    }

    fun updateStudent(student: Student) {
        viewModelScope.launch {
            repository.updateStudent(student)
        }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            repository.deleteStudent(student)
        }
    }
}

// --- Attendance ViewModel ---
data class StudentAttendanceItem(
    val student: Student,
    val attendance: Attendance?
)

class AttendanceViewModel(private val repository: SchoolRepository) : ViewModel() {

    private val _selectedDate = MutableStateFlow(
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    )
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    val attendanceItems: StateFlow<List<StudentAttendanceItem>> = combine(
        repository.allStudents,
        _selectedDate.flatMapLatest { date -> repository.getAttendanceForDate(date) }
    ) { students, attendanceList ->
        val attendanceMap = attendanceList.associateBy { it.studentId }
        students.map { student ->
            StudentAttendanceItem(
                student = student,
                attendance = attendanceMap[student.id]
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onDateSelected(date: String) {
        _selectedDate.value = date
    }

    fun setStudentStatus(studentId: Long, status: String, notes: String = "") {
        viewModelScope.launch {
            val date = _selectedDate.value
            val existing = attendanceItems.value.find { it.student.id == studentId }?.attendance
            val updated = existing?.copy(status = status, notes = notes)
                ?: Attendance(studentId = studentId, date = date, status = status, notes = notes)
            repository.insertAttendance(updated)
        }
    }

    fun markAllPresent() {
        viewModelScope.launch {
            val date = _selectedDate.value
            val items = attendanceItems.value.map { item ->
                item.attendance?.copy(status = "حاضر")
                    ?: Attendance(studentId = item.student.id, date = date, status = "حاضر")
            }
            repository.saveAttendanceList(items)
        }
    }
}

// --- Subject ViewModel ---
class SubjectViewModel(private val repository: SchoolRepository) : ViewModel() {

    val subjects: StateFlow<List<Subject>> = repository.allSubjects.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addSubject(subject: Subject) {
        viewModelScope.launch {
            repository.insertSubject(subject)
        }
    }

    fun updateSubject(subject: Subject) {
        viewModelScope.launch {
            repository.updateSubject(subject)
        }
    }

    fun deleteSubject(subject: Subject) {
        viewModelScope.launch {
            repository.deleteSubject(subject)
        }
    }
}

// --- Grade ViewModel ---
data class GradeDetailItem(
    val student: Student,
    val subject: Subject,
    val grade: Grade?
)

class GradeViewModel(private val repository: SchoolRepository) : ViewModel() {

    private val _selectedSubjectId = MutableStateFlow<Long?>(null)
    val selectedSubjectId: StateFlow<Long?> = _selectedSubjectId.asStateFlow()

    val gradeDetails: StateFlow<List<GradeDetailItem>> = combine(
        repository.allStudents,
        repository.allSubjects,
        repository.allGrades,
        _selectedSubjectId
    ) { students, subjects, grades, selectedSubId ->
        if (selectedSubId == null && subjects.isNotEmpty()) {
            _selectedSubjectId.value = subjects.first().id
        }

        val activeSubjectId = selectedSubId ?: subjects.firstOrNull()?.id ?: return@combine emptyList()
        val currentSubject = subjects.find { it.id == activeSubjectId } ?: return@combine emptyList()
        val gradeMap = grades.filter { it.subjectId == activeSubjectId }.associateBy { it.studentId }

        students.map { student ->
            GradeDetailItem(
                student = student,
                subject = currentSubject,
                grade = gradeMap[student.id]
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun selectSubject(subjectId: Long) {
        _selectedSubjectId.value = subjectId
    }

    fun saveGrade(studentId: Long, subjectId: Long, coursework: Double, midterm: Double, finalScore: Double) {
        viewModelScope.launch {
            val existing = repository.getGradeForStudentAndSubject(studentId, subjectId)
            val gradeToSave = existing?.copy(
                courseworkScore = coursework,
                midtermScore = midterm,
                finalScore = finalScore
            ) ?: Grade(
                studentId = studentId,
                subjectId = subjectId,
                courseworkScore = coursework,
                midtermScore = midterm,
                finalScore = finalScore
            )
            repository.insertGrade(gradeToSave)
        }
    }
}
