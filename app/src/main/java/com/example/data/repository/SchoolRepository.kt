package com.example.data.repository

import com.example.data.dao.AttendanceDao
import com.example.data.dao.GradeDao
import com.example.data.dao.StudentDao
import com.example.data.dao.SubjectDao
import com.example.data.entity.Attendance
import com.example.data.entity.Grade
import com.example.data.entity.Student
import com.example.data.entity.Subject
import kotlinx.coroutines.flow.Flow

class SchoolRepository(
    private val studentDao: StudentDao,
    private val attendanceDao: AttendanceDao,
    private val subjectDao: SubjectDao,
    private val gradeDao: GradeDao
) {
    // Students
    val allStudents: Flow<List<Student>> = studentDao.getAllStudents()
    val studentCount: Flow<Int> = studentDao.getStudentCount()

    fun getStudentById(id: Long): Flow<Student?> = studentDao.getStudentById(id)
    fun searchStudents(query: String): Flow<List<Student>> = studentDao.searchStudents(query)

    suspend fun insertStudent(student: Student): Long = studentDao.insertStudent(student)
    suspend fun updateStudent(student: Student) = studentDao.updateStudent(student)
    suspend fun deleteStudent(student: Student) = studentDao.deleteStudent(student)

    // Subjects
    val allSubjects: Flow<List<Subject>> = subjectDao.getAllSubjects()
    val subjectCount: Flow<Int> = subjectDao.getSubjectCount()

    fun getSubjectById(id: Long): Flow<Subject?> = subjectDao.getSubjectById(id)

    suspend fun insertSubject(subject: Subject): Long = subjectDao.insertSubject(subject)
    suspend fun updateSubject(subject: Subject) = subjectDao.updateSubject(subject)
    suspend fun deleteSubject(subject: Subject) = subjectDao.deleteSubject(subject)

    // Attendance
    val allAttendance: Flow<List<Attendance>> = attendanceDao.getAllAttendance()

    fun getAttendanceForDate(date: String): Flow<List<Attendance>> = attendanceDao.getAttendanceForDate(date)
    fun getAttendanceForStudent(studentId: Long): Flow<List<Attendance>> = attendanceDao.getAttendanceForStudent(studentId)

    suspend fun insertAttendance(attendance: Attendance): Long = attendanceDao.insertAttendance(attendance)
    suspend fun saveAttendanceList(list: List<Attendance>) = attendanceDao.insertAllAttendance(list)
    suspend fun updateAttendance(attendance: Attendance) = attendanceDao.updateAttendance(attendance)
    suspend fun deleteAttendance(attendance: Attendance) = attendanceDao.deleteAttendance(attendance)

    // Grades
    val allGrades: Flow<List<Grade>> = gradeDao.getAllGrades()

    fun getGradesForStudent(studentId: Long): Flow<List<Grade>> = gradeDao.getGradesForStudent(studentId)
    fun getGradesForSubject(subjectId: Long): Flow<List<Grade>> = gradeDao.getGradesForSubject(subjectId)
    suspend fun getGradeForStudentAndSubject(studentId: Long, subjectId: Long): Grade? =
        gradeDao.getGradeForStudentAndSubject(studentId, subjectId)

    suspend fun insertGrade(grade: Grade): Long = gradeDao.insertGrade(grade)
    suspend fun updateGrade(grade: Grade) = gradeDao.updateGrade(grade)
    suspend fun deleteGrade(grade: Grade) = gradeDao.deleteGrade(grade)
}
