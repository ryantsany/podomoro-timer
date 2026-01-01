package com.agiztya.podomoro.ui.timer

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.agiztya.podomoro.R
import com.agiztya.podomoro.StatsActivity
import com.agiztya.podomoro.ui.complete.BreakCompleteScreen
import com.agiztya.podomoro.ui.complete.TimerCompleteScreen
import com.agiztya.podomoro.ui.settings.SettingScreen
import com.agiztya.podomoro.ui.settings.SettingsViewModel
import com.agiztya.podomoro.ui.theme.BackgroundColor
import com.agiztya.podomoro.ui.theme.GreenPrimary
import com.agiztya.podomoro.ui.theme.NavigationBackground
import com.agiztya.podomoro.ui.theme.OrangePrimary

@Composable
fun Timer(
    totalTime: Long,
    currentTime: Long,
    isTimeRunning: Boolean,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 8.dp,
    inactiveBarColor: Color = Color.LightGray,
    activeBarColor: Color,
) {
    val progress = if (totalTime > 0) currentTime / totalTime.toFloat() else 0f

    val totalSeconds = currentTime / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val arcSize = size.minDimension

            drawArc(
                color = inactiveBarColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                size = Size(arcSize, arcSize),
                style = Stroke(strokeWidth.toPx(), cap = StrokeCap.Round)
            )

            drawArc(
                color = activeBarColor,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                size = Size(arcSize, arcSize),
                style = Stroke(strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }

        Text(
            text = String.format("%02d:%02d", minutes, seconds),
            fontSize = 56.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun BottomBar(selectedIndex: Int, onIndexChange: (Int) -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val items = listOf(
        "Komodoro",
        "Short Break",
        "Long Break"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 24.dp, start = 16.dp, end = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(30.dp),
            modifier = Modifier
                .widthIn(max = 400.dp)
                .fillMaxWidth()
                .height(60.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, title ->
                    val isSelected = selectedIndex == index
                    val activeColor = if (index == 0) OrangePrimary else GreenPrimary

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(26.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                            .clickable(enabled = enabled) { onIndexChange(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(viewModel: TimerViewModel, settingsViewModel: SettingsViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()
    val currentTime by viewModel.currentTime.collectAsState()
    val showCompleteScreen by viewModel.showCompleteScreen.collectAsState()
    val taskName by viewModel.taskName.collectAsState()
    
    val podomoroLength by settingsViewModel.podomoroLength.collectAsState()
    val shortBreakLength by settingsViewModel.shortBreakLength.collectAsState()
    val longBreakLength by settingsViewModel.longBreakLength.collectAsState()

    var showSettings by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val totalTime = when (selectedTab) {
        0 -> podomoroLength.toLong() * 60 * 1000L
        1 -> shortBreakLength.toLong() * 60 * 1000L
        else -> longBreakLength.toLong() * 60 * 1000L
    }

    if (showSettings) {
        SettingScreen(onBack = { showSettings = false }, viewModel = settingsViewModel)
    } else if (showCompleteScreen) {
        if (selectedTab == 0) {
            TimerCompleteScreen(
                taskName = taskName,
                durationMinutes = podomoroLength,
                onTakeABreak = { viewModel.takeABreak() },
                onSkip = { viewModel.skipBreak() }
            )
        } else {
            BreakCompleteScreen(
                onStartNextSession = { viewModel.skipBreak() },
                onExtend = { viewModel.extendBreak() }
            )
        }
    } else {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(end = 15.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    context.startActivity(
                                        Intent(context, StatsActivity::class.java)
                                    )
                                },
                                enabled = !isTimerRunning
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.history),
                                    contentDescription = "History",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                            Text("Focus Timer", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                            IconButton(
                                onClick = { showSettings = true },
                                enabled = !isTimerRunning
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.settings),
                                    contentDescription = "Settings",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            bottomBar = {
                BottomBar(
                    selectedIndex = selectedTab,
                    onIndexChange = { viewModel.onTabSelected(it) },
                    enabled = !isTimerRunning
                )
            }
        ) { innerPadding ->
            Surface(
                modifier = Modifier.padding(innerPadding),
                color = MaterialTheme.colorScheme.background
            ) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val maxHeight = maxHeight
                    val maxWidth = maxWidth

                    Column(
                        Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            val timerSize = if (maxHeight < 600.dp) 200.dp else 250.dp
                            Timer(
                                totalTime = totalTime,
                                currentTime = currentTime,
                                isTimeRunning = isTimerRunning,
                                activeBarColor = if (selectedTab == 0) OrangePrimary else GreenPrimary,
                                modifier = Modifier.size(timerSize)
                            )
                        }

                        Spacer(modifier = Modifier.height(if (maxHeight < 600.dp) 16.dp else 32.dp))

                        if (isTimerRunning) {
                            if (selectedTab == 0) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.height(50.dp).padding(horizontal = 16.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Focusing on: ",
                                            color = Color.Gray,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = taskName.ifEmpty { "Task" },
                                            color = Color.Gray,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            } else {
                                Spacer(Modifier.height(50.dp))
                            }
                        } else {
                            if (selectedTab == 0) {
                                val fieldWidth = if (maxWidth < 400.dp) maxWidth * 0.85f else 320.dp
                                BasicTextField(
                                    value = taskName,
                                    onValueChange = { viewModel.onTaskNameChanged(it) },
                                    modifier = Modifier.width(fieldWidth).height(56.dp).shadow(
                                        elevation = 2.dp,
                                        shape = RoundedCornerShape(24.dp),
                                        clip = true
                                    ).background(Color.White),
                                    decorationBox = { innerTextField ->
                                        Box(
                                            modifier = Modifier.padding(horizontal = 20.dp),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            if (taskName.isEmpty())
                                                Text(
                                                    "What are you working on?",
                                                    color = Color.LightGray,
                                                    fontSize = 14.sp
                                                )
                                            innerTextField()
                                        }
                                    }
                                )
                            } else {
                                Spacer(modifier = Modifier.height(56.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(if (maxHeight < 600.dp) 16.dp else 32.dp))

                        val buttonColor = if (!isTimerRunning) {
                            if (selectedTab == 0) OrangePrimary else GreenPrimary
                        } else {
                            Color.White
                        }

                        val textColor = if (!isTimerRunning) {
                            Color.White
                        } else if (selectedTab == 0) {
                            OrangePrimary
                        } else {
                            GreenPrimary
                        }

                        val borderColor = if (!isTimerRunning) {
                            null
                        } else if (selectedTab == 0) {
                            BorderStroke(2.dp, OrangePrimary)
                        } else {
                            BorderStroke(2.dp, GreenPrimary)
                        }

                        val buttonTextString = if (isTimerRunning) {
                            "Pause"
                        } else if (selectedTab == 0) {
                            "Start Focus"
                        } else {
                            "Start Break"
                        }

                        val buttonWidth = if (maxWidth < 400.dp) maxWidth * 0.85f else 320.dp
                        Button(
                            colors = ButtonDefaults.buttonColors(
                                containerColor = buttonColor,
                                contentColor = textColor
                            ),
                            border = borderColor,
                            onClick = { viewModel.toggleTimer() },
                            modifier = Modifier
                                .width(buttonWidth)
                                .height(56.dp)
                                .shadow(
                                    elevation = 0.dp,
                                    shape = RoundedCornerShape(28.dp),
                                    clip = true
                                )
                        ) {
                            Text(text = buttonTextString, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }
            }
        }
    }
}
