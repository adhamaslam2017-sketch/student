package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val academicNumber: String,
    val name: String,
    val gradeLevel: String,
    val birthDate: String,
    val parentName: String,
    val phone: String,
    val address: String,
    val gender: String = "ذكر"
)

@Entity(
    tableName = "attendance",
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("studentId"), Index("date")]
)
data class Attendance(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val date: String,
    val status: String, // "حاضر", "غائب", "متأخر", "بعذر"
    val notes: String = ""
)

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val name: String,
    val teacherName: String,
    val maxScore: Int = 100
)

@Entity(
    tableName = "grades",
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("studentId"), Index("subjectId")]
)
data class Grade(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val subjectId: Long,
    val courseworkScore: Double = 0.0, // أعمال السنة
    val midtermScore: Double = 0.0,    // اختبار منتصف الفصل
    val finalScore: Double = 0.0,      // الاختبار النهائي
    val semester: String = "الفصل الأول"
) {
    val totalScore: Double
        get() = courseworkScore + midtermScore + finalScore

    val gradeLetter: String
        get() = when {
            totalScore >= 90 -> "ممتاز (A)"
            totalScore >= 80 -> "جيد جداً (B)"
            totalScore >= 70 -> "جيد (C)"
            totalScore >= 60 -> "مقبول (D)"
            else -> "راسب (F)"
        }
}
