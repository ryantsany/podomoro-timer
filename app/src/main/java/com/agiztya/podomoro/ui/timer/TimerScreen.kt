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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.agiztya.podomoro.R
import com.agiztya.podomoro.StatsActivity
import com.agiztya.podomoro.ui.complete.BreakCompleteScreen
import com.agiztya.podomoro.ui.complete.TimerCompleteScreen
import com.agiztya.podomoro.ui.settings.SettingScreen
import com.agiztya.podomoro.ui.theme.BackgroundColor
import com.agiztya.podomoro.ui.theme.GreenPrimary
import com.agiztya.podomoro.ui.theme.NavigationBackground
import com.agiztya.podomoro.ui.theme.OrangePrimary
import kotlinx.coroutines.delay

@Composable
fun Timer(
    totalTime: Long,
    currentTime: Long,
    isTimeRunning: Boolean,
    onTimeChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 8.dp,
    inactiveBarColor: Color = Color.LightGray,
    activeBarColor: Color,
) {

    val progress = currentTime / totalTime.toFloat()

    val totalSeconds = currentTime / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60

    LaunchedEffect(isTimeRunning, currentTime) {
        if (isTimeRunning && currentTime > 0L) {
            delay(1000L)
            onTimeChange(currentTime - 1000L)
        }
    }

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
        "Pomodoro",
        "Short Break",
        "Long Break"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding() // Pushes the bar above system navigation buttons
            .padding(bottom = 24.dp, start = 16.dp, end = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = NavigationBackground,
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
                            .background(if (isSelected) Color.White else Color.Transparent)
                            .clickable(enabled = enabled) { onIndexChange(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (isSelected) activeColor else Color.Gray,
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
fun TimerScreen(modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val totalTime = if (selectedTab == 0) 25 * 60 * 1000L else if (selectedTab == 1) 5 * 60 * 1000L else 15 * 60 * 1000L

    var isTimerRunning by remember { mutableStateOf(false) }
    var currentTime by remember { mutableStateOf(totalTime) }
    var textField by remember { mutableStateOf("") }
    var showSettings by remember { mutableStateOf(false) }
    var showCompleteScreen by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(selectedTab) {
        currentTime = totalTime
        isTimerRunning = false
    }

    if (showSettings) {
        SettingScreen(onBack = { showSettings = false })
    } else if (showCompleteScreen) {
        if (selectedTab == 0) {
            TimerCompleteScreen(
                onTakeABreak = {
                    selectedTab = 1
                    showCompleteScreen = false
                },
                onSkip = {
                    currentTime = totalTime
                    showCompleteScreen = false
                }
            )
        } else {
            BreakCompleteScreen(
                onStartNextSession = {
                    selectedTab = 0
                    showCompleteScreen = false
                },
                onExtend = {
                    currentTime = 5 * 60 * 1000L
                    isTimerRunning = true
                    showCompleteScreen = false
                }
            )
        }
    } else {
        Scaffold(
            containerColor = BackgroundColor,
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
                                    contentDescription = "History"
                                )
                            }
                            Text("Focus Timer", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            IconButton(
                                onClick = { showSettings = true },
                                enabled = !isTimerRunning
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.settings),
                                    contentDescription = "Settings"
                                )
                            }
                        }
                    }
                )
            },
            bottomBar = {
                BottomBar(
                    selectedIndex = selectedTab,
                    onIndexChange = { selectedTab = it },
                    enabled = !isTimerRunning,
                    modifier = modifier
                )
            }
        ) { innerPadding ->
            Surface(
                modifier = Modifier.padding(innerPadding),
                color = BackgroundColor
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
                                onTimeChange = { newTime ->
                                    currentTime = newTime
                                    if (newTime <= 0L) {
                                        isTimerRunning = false
                                        showCompleteScreen = true
                                    }
                                },
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
                                            text = textField.ifEmpty { "Task" },
                                            color = Color.Gray,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            } else {
                                Spacer(modifier.height(50.dp))
                            }
                        } else {
                            if (selectedTab == 0) {
                                val fieldWidth = if (maxWidth < 400.dp) maxWidth * 0.85f else 320.dp
                                BasicTextField(
                                    value = textField,
                                    onValueChange = { textField = it },
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
                                            if (textField.isEmpty())
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
                                Spacer(modifier.height(56.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(if (maxHeight < 600.dp) 16.dp else 32.dp))

                        val buttonColor = if (!isTimerRunning) {
                            if (selectedTab == 0) OrangePrimary else GreenPrimary
                        } else {
                            Color.White
                        }
                        val textColor = if (!isTimerRunning) Color.White
                        else if (selectedTab == 0) OrangePrimary
                        else GreenPrimary
                        val borderColor = if (!isTimerRunning) null
                        else if (selectedTab == 0) BorderStroke(2.dp, OrangePrimary)
                        else BorderStroke(2.dp, GreenPrimary)
                        val buttonTextString = if (isTimerRunning) "Pause"
                        else if (selectedTab == 0) "Start Focus"
                        else "Start Break"

                        val buttonWidth = if (maxWidth < 400.dp) maxWidth * 0.85f else 320.dp
                        Button(
                            colors = ButtonDefaults.buttonColors(
                                containerColor = buttonColor,
                                contentColor = textColor
                            ),
                            border = borderColor,
                            onClick = {
                                if (currentTime <= 0L) {
                                    currentTime = totalTime
                                    isTimerRunning = true
                                } else {
                                    isTimerRunning = !isTimerRunning
                                }
                            },
                            modifier = Modifier
                                .width(buttonWidth)
                                .height(56.dp)
                                .shadow(
                                    elevation = 8.dp,
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

@Preview
@Composable
fun TimerScreenPreview() {
    TimerScreen()
}
