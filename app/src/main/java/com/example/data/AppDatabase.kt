package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AttendanceDao
import com.example.data.dao.GradeDao
import com.example.data.dao.StudentDao
import com.example.data.dao.SubjectDao
import com.example.data.entity.Attendance
import com.example.data.entity.Grade
import com.example.data.entity.Student
import com.example.data.entity.Subject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Database(
    entities = [Student::class, Attendance::class, Subject::class, Grade::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun studentDao(): StudentDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun subjectDao(): SubjectDao
    abstract fun gradeDao(): GradeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "school_management_db"
                )
                    .addCallback(DatabaseCallback(context.applicationContext))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }
        }

        private suspend fun populateInitialData(db: AppDatabase) {
            val studentDao = db.studentDao()
            val subjectDao = db.subjectDao()
            val attendanceDao = db.attendanceDao()
            val gradeDao = db.gradeDao()

            // 1. Insert Initial Students
            val s1 = studentDao.insertStudent(
                Student(
                    academicNumber = "2026001",
                    name = "أحمد محمد علي",
                    gradeLevel = "الصف العاشر - أ",
                    birthDate = "2009-04-15",
                    parentName = "محمد علي",
                    phone = "0501234567",
                    address = "الرياض - حي النخيل",
                    gender = "ذكر"
                )
            )
            val s2 = studentDao.insertStudent(
                Student(
                    academicNumber = "2026002",
                    name = "سارة يوسف الحمد",
                    gradeLevel = "الصف العاشر - أ",
                    birthDate = "2009-08-22",
                    parentName = "يوسف الحمد",
                    phone = "0509876543",
                    address = "الرياض - حي الياسمين",
                    gender = "أنثى"
                )
            )
            val s3 = studentDao.insertStudent(
                Student(
                    academicNumber = "2026003",
                    name = "عمر خالد الدوسري",
                    gradeLevel = "الصف العاشر - ب",
                    birthDate = "2009-01-10",
                    parentName = "خالد الدوسري",
                    phone = "0554433221",
                    address = "الرياض - حي الملقا",
                    gender = "ذكر"
                )
            )
            val s4 = studentDao.insertStudent(
                Student(
                    academicNumber = "2026004",
                    name = "فاطمة إبراهيم الشمري",
                    gradeLevel = "الصف الحادي عشر - أ",
                    birthDate = "2008-11-05",
                    parentName = "إبراهيم الشمري",
                    phone = "0566778899",
                    address = "الرياض - حي الصحافة",
                    gender = "أنثى"
                )
            )
            val s5 = studentDao.insertStudent(
                Student(
                    academicNumber = "2026005",
                    name = "عبدالله القحطاني",
                    gradeLevel = "الصف الحادي عشر - ب",
                    birthDate = "2008-03-30",
                    parentName = "سعيد القحطاني",
                    phone = "0511223344",
                    address = "الرياض - حي العليا",
                    gender = "ذكر"
                )
            )

            // 2. Insert Initial Subjects
            val sub1 = subjectDao.insertSubject(
                Subject(code = "MATH101", name = "الرياضيات", teacherName = "أ. عبدالسلام العتيبي", maxScore = 100)
            )
            val sub2 = subjectDao.insertSubject(
                Subject(code = "PHYS101", name = "الفيزياء", teacherName = "د. محمود سليمان", maxScore = 100)
            )
            val sub3 = subjectDao.insertSubject(
                Subject(code = "ARAB101", name = "اللغة العربية", teacherName = "أ. طارق عبدالفتاح", maxScore = 100)
            )
            val sub4 = subjectDao.insertSubject(
                Subject(code = "ENGL101", name = "اللغة الإنجليزية", teacherName = "أ. جون سميث", maxScore = 100)
            )
            val sub5 = subjectDao.insertSubject(
                Subject(code = "CHEM101", name = "الكيمياء", teacherName = "د. نورة التميمي", maxScore = 100)
            )

            // 3. Insert Today's Attendance
            val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            attendanceDao.insertAllAttendance(
                listOf(
                    Attendance(studentId = s1, date = todayDate, status = "حاضر", notes = "ملتزم"),
                    Attendance(studentId = s2, date = todayDate, status = "حاضر", notes = ""),
                    Attendance(studentId = s3, date = todayDate, status = "متأخر", notes = "تأخر 15 دقيقة"),
                    Attendance(studentId = s4, date = todayDate, status = "بعذر", notes = "تقرير طبي"),
                    Attendance(studentId = s5, date = todayDate, status = "غائب", notes = "لم يتم التواصل")
                )
            )

            // 4. Insert Initial Grades
            gradeDao.insertGrade(Grade(studentId = s1, subjectId = sub1, courseworkScore = 28.0, midtermScore = 27.0, finalScore = 38.0)) // 93
            gradeDao.insertGrade(Grade(studentId = s1, subjectId = sub2, courseworkScore = 25.0, midtermScore = 24.0, finalScore = 34.0)) // 83
            gradeDao.insertGrade(Grade(studentId = s2, subjectId = sub1, courseworkScore = 30.0, midtermScore = 29.0, finalScore = 39.0)) // 98
            gradeDao.insertGrade(Grade(studentId = s2, subjectId = sub3, courseworkScore = 27.0, midtermScore = 28.0, finalScore = 37.0)) // 92
            gradeDao.insertGrade(Grade(studentId = s3, subjectId = sub1, courseworkScore = 20.0, midtermScore = 18.0, finalScore = 28.0)) // 66
            gradeDao.insertGrade(Grade(studentId = s4, subjectId = sub4, courseworkScore = 29.0, midtermScore = 28.0, finalScore = 39.0)) // 96
            gradeDao.insertGrade(Grade(studentId = s5, subjectId = sub5, courseworkScore = 15.0, midtermScore = 14.0, finalScore = 25.0)) // 54
        }
    }
}
