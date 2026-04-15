package com.towhid.tapin.presentation.attendance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.towhid.tapin.domain.model.AttendanceRecord
import com.towhid.tapin.domain.util.Formatter
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(HomeEvent.ClearError)
        }
    }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(HomeEvent.ClearSuccess)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "TapIn",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold 
                    ) 
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Section: Today's Status
                TodayStatusCard(state)

                Spacer(modifier = Modifier.height(24.dp))

                // Middle Section: Actions
                ActionButtons(
                    state = state,
                    onCheckIn = { viewModel.onEvent(HomeEvent.OnCheckInClicked) },
                    onCheckOut = { viewModel.onEvent(HomeEvent.OnCheckOutClicked) }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Bottom Section: Attendance List Header
                Text(
                    text = "History",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start).padding(bottom = 16.dp)
                )
                
                AttendanceList(state.attendanceList)
            }

            if (state.isLoading) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
fun TodayStatusCard(state: HomeState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatusRow("Check In", if (state.todayCheckInTime != null) 
                Formatter.formatTime(state.todayCheckInTime) 
                else "Not Checked In")

            StatusRow("Check Out", if (state.todayCheckOutTime != null) 
                Formatter.formatTime(state.todayCheckOutTime) 
                else "Not Checked Out")
            
            if (state.todayCheckInTime != null) {
                val statusColor = if (state.isLateToday) Color.Red else Color(0xFF2E7D32)
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Status: ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (state.isLateToday) "Late" else "On Time",
                        color = statusColor,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
fun StatusRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ActionButtons(
    state: HomeState,
    onCheckIn: () -> Unit,
    onCheckOut: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = onCheckIn,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            enabled = state.todayCheckInTime == null && !state.isLoading,
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonColors().let { ButtonDefaults.elevatedButtonElevation() }
        ) {
            Text("Check In", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Button(
            onClick = onCheckOut,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            enabled = state.todayCheckInTime != null && state.todayCheckOutTime == null && !state.isLoading,
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonColors().let { ButtonDefaults.elevatedButtonElevation() }
        ) {
            Text("Check Out", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AttendanceList(
    list: List<AttendanceRecord>
) {
    if (list.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 64.dp), 
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No records yet", 
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "Your check-ins will appear here", 
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                )
            }
        }
    } else {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(list) { index, record ->
                    AttendanceItem(record)
                    if (index < list.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AttendanceItem(
    record: AttendanceRecord
) {
    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = Formatter.formatDate(record.date),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row {
                Text(
                    text = "In: ${Formatter.formatTime(record.checkInTime)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                record.checkOutTime?.let {
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Out: ${Formatter.formatTime(it)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (record.isLate) Color.Red.copy(alpha = 0.1f) else Color(0xFF2E7D32).copy(alpha = 0.1f)
        ) {
            Text(
                text = if (record.isLate) "LATE" else "ON TIME",
                color = if (record.isLate) Color.Red else Color(0xFF2E7D32),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}
