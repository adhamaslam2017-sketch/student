package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.*
import com.example.ui.viewmodel.GradeDetailItem
import com.example.ui.viewmodel.GradeViewModel
import com.example.ui.viewmodel.SubjectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradesScreen(
    gradeViewModel: GradeViewModel,
    subjectViewModel: SubjectViewModel
) {
    val subjects by subjectViewModel.subjects.collectAsStateWithLifecycle()
    val selectedSubjectId by gradeViewModel.selectedSubjectId.collectAsStateWithLifecycle()
    val gradeDetails by gradeViewModel.gradeDetails.collectAsStateWithLifecycle()

    var gradeItemToEdit by remember { mutableStateOf<GradeDetailItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("إدارة الدرجات والاختبارات", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Subject Tab Filter Row
            if (subjects.isNotEmpty()) {
                Text(
                    text = "اختر المادة الدراسية:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(subjects, key = { it.id }) { subject ->
                        val isSelected = selectedSubjectId == subject.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { gradeViewModel.selectSubject(subject.id) },
                            label = { Text(subject.name) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Grade,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            }

            if (gradeDetails.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("يرجى إضافة طلاب ومواد لعرض رصد الدرجات")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(gradeDetails, key = { it.student.id }) { item ->
                        GradeRowCard(
                            item = item,
                            onEditGrade = { gradeItemToEdit = item }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }

    // Edit Grade Dialog
    gradeItemToEdit?.let { item ->
        GradeEditDialog(
            item = item,
            onDismiss = { gradeItemToEdit = null },
            onSave = { coursework, midterm, finalScore ->
                gradeViewModel.saveGrade(
                    studentId = item.student.id,
                    subjectId = item.subject.id,
                    coursework = coursework,
                    midterm = midterm,
                    finalScore = finalScore
                )
                gradeItemToEdit = null
            }
        )
    }
}

@Composable
fun GradeRowCard(
    item: GradeDetailItem,
    onEditGrade: () -> Unit
) {
    val totalScore = item.grade?.totalScore ?: 0.0
    val gradeLetter = item.grade?.gradeLetter ?: "غير مرصود"

    val badgeColor = when {
        totalScore >= 90 -> StatusPresent
        totalScore >= 70 -> MaterialTheme.colorScheme.primary
        totalScore >= 60 -> StatusLate
        totalScore > 0 -> StatusAbsent
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("grade_item_${item.student.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.student.name.take(1),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.student.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "${item.student.academicNumber} • ${item.student.gradeLevel}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    color = badgeColor.copy(alpha = 0.15f),
                    contentColor = badgeColor,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = gradeLetter,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onEditGrade) {
                    Icon(Icons.Default.Edit, contentDescription = "تعديل الدرجة", tint = MaterialTheme.colorScheme.primary)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            // Scores Breakdown Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ScoreColumn(label = "أعمال السنة (30)", score = item.grade?.courseworkScore ?: 0.0)
                ScoreColumn(label = "منتصف الفصل (30)", score = item.grade?.midtermScore ?: 0.0)
                ScoreColumn(label = "النهائي (40)", score = item.grade?.finalScore ?: 0.0)
                ScoreColumn(label = "المجموع (100)", score = totalScore, isTotal = true)
            }
        }
    }
}

@Composable
fun ScoreColumn(
    label: String,
    score: Double,
    isTotal: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = String.format(java.util.Locale.US, "%.1f", score),
            fontSize = if (isTotal) 15.sp else 13.sp,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.SemiBold,
            color = if (isTotal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun GradeEditDialog(
    item: GradeDetailItem,
    onDismiss: () -> Unit,
    onSave: (coursework: Double, midterm: Double, finalScore: Double) -> Unit
) {
    var courseworkStr by remember { mutableStateOf((item.grade?.courseworkScore ?: 0.0).toString()) }
    var midtermStr by remember { mutableStateOf((item.grade?.midtermScore ?: 0.0).toString()) }
    var finalStr by remember { mutableStateOf((item.grade?.finalScore ?: 0.0).toString()) }

    val cw = courseworkStr.toDoubleOrNull() ?: 0.0
    val mt = midtermStr.toDoubleOrNull() ?: 0.0
    val fn = finalStr.toDoubleOrNull() ?: 0.0
    val total = cw + mt + fn

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("رصد درجات: ${item.student.name}", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "المادة: ${item.subject.name}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = courseworkStr,
                    onValueChange = { courseworkStr = it },
                    label = { Text("أعمال السنة (الحد الأقصى 30)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = midtermStr,
                    onValueChange = { midtermStr = it },
                    label = { Text("اختبار منتصف الفصل (الحد الأقصى 30)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = finalStr,
                    onValueChange = { finalStr = it },
                    label = { Text("الاختبار النهائي (الحد الأقصى 40)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("المجموع الكلي:", fontWeight = FontWeight.Bold)
                        Text(
                            text = String.format(java.util.Locale.US, "%.1f / 100", total),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(cw, mt, fn)
                }
            ) {
                Text("حفظ الدرجات")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
