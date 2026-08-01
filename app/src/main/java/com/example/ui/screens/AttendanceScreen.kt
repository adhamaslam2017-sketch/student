package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.*
import com.example.ui.viewmodel.AttendanceViewModel
import com.example.ui.viewmodel.StudentAttendanceItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    viewModel: AttendanceViewModel
) {
    val date by viewModel.selectedDate.collectAsStateWithLifecycle()
    val attendanceItems by viewModel.attendanceItems.collectAsStateWithLifecycle()

    val presentCount = attendanceItems.count { it.attendance?.status == "حاضر" }
    val absentCount = attendanceItems.count { it.attendance?.status == "غائب" }
    val lateCount = attendanceItems.count { it.attendance?.status == "متأخر" }
    val excusedCount = attendanceItems.count { it.attendance?.status == "بعذر" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تسجيل الحضور والغياب", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                actions = {
                    TextButton(
                        onClick = { viewModel.markAllPresent() },
                        modifier = Modifier.testTag("mark_all_present_btn")
                    ) {
                        Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("الجميع حاضر")
                    }
                }
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
            // Date Picker Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "التاريخ: $date",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Button(
                        onClick = {
                            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                            viewModel.onDateSelected(today)
                        },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("اليوم", fontSize = 12.sp)
                    }
                }
            }

            // Summary Stats Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AttendanceChip(label = "حاضر: $presentCount", color = StatusPresent, modifier = Modifier.weight(1f))
                AttendanceChip(label = "غائب: $absentCount", color = StatusAbsent, modifier = Modifier.weight(1f))
                AttendanceChip(label = "متأخر: $lateCount", color = StatusLate, modifier = Modifier.weight(1f))
                AttendanceChip(label = "بعذر: $excusedCount", color = StatusExcused, modifier = Modifier.weight(1f))
            }

            // Students Attendance List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(attendanceItems, key = { it.student.id }) { item ->
                    AttendanceRowItem(
                        item = item,
                        onStatusSelected = { status ->
                            viewModel.setStudentStatus(item.student.id, status)
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
fun AttendanceChip(
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = color.copy(alpha = 0.15f),
        contentColor = color,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun AttendanceRowItem(
    item: StudentAttendanceItem,
    onStatusSelected: (String) -> Unit
) {
    val currentStatus = item.attendance?.status ?: "لم يسجل"

    ElevatedCard(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
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
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.student.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${item.student.academicNumber} • ${item.student.gradeLevel}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Status Selector Segmented Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatusOptionButton(
                    label = "حاضر",
                    isSelected = currentStatus == "حاضر",
                    activeColor = StatusPresent,
                    onClick = { onStatusSelected("حاضر") },
                    modifier = Modifier.weight(1f)
                )
                StatusOptionButton(
                    label = "غائب",
                    isSelected = currentStatus == "غائب",
                    activeColor = StatusAbsent,
                    onClick = { onStatusSelected("غائب") },
                    modifier = Modifier.weight(1f)
                )
                StatusOptionButton(
                    label = "متأخر",
                    isSelected = currentStatus == "متأخر",
                    activeColor = StatusLate,
                    onClick = { onStatusSelected("متأخر") },
                    modifier = Modifier.weight(1f)
                )
                StatusOptionButton(
                    label = "بعذر",
                    isSelected = currentStatus == "بعذر",
                    activeColor = StatusExcused,
                    onClick = { onStatusSelected("بعذر") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun StatusOptionButton(
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val background = if (isSelected) activeColor else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = contentColor
        )
    }
}
