package com.agiztya.podomoro.ui.timer

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import com.agiztya.podomoro.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import com.agiztya.podomoro.SettingsActivity
import com.agiztya.podomoro.StatsActivity
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
    activeBarColor: Color ,
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
fun BottomBar(selectedIndex: Int,onIndexChange: (Int) -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true){


    val items = listOf(
        "Pomodoro",
        "Short Break",
        "Long Break"
    )

    NavigationBar(
        containerColor = Color(0xFFEDF2F7),
        modifier = Modifier.padding(10.dp).width(500.dp).height(50.dp).clip(RoundedCornerShape(25.dp)).shadow(
            elevation = 8.dp,
            shape = RoundedCornerShape(20.dp),
            clip = true
        )
    ) {
        items.forEachIndexed { index, title ->
            val isSelected = selectedIndex == index
            val itemColor = if (selectedIndex == 0) Color(0xFFFF5722) else Color(0xFF66BB6A)

            NavigationBarItem(
                selected = isSelected,
                enabled = enabled,
                modifier = Modifier,
                onClick = {
                    onIndexChange(index)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = itemColor,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = Color.White,
                    disabledIconColor = Color.LightGray,
                    disabledTextColor = Color.LightGray
                ),
                icon = {
                    Text(
                        title,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                    )
                }
            )

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(modifier: Modifier = Modifier){
    var selectedTab by remember { mutableIntStateOf(0) }
    val totalTime = if (selectedTab == 0) 1 * 60 * 1000L else 5 * 60 * 1000L

    var isTimerRunning by remember { mutableStateOf(false) }
    var currentTime by remember { mutableStateOf(totalTime) }
    var textField by remember { mutableStateOf("") }

    val context = LocalContext.current

    //reset timer saat pindah tab
    LaunchedEffect(selectedTab) {
        currentTime = totalTime
        isTimerRunning = false
    }

    Scaffold(
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
                            } ,
                            enabled = !isTimerRunning
                        ) {
                            Icon(painter = painterResource(R.drawable.history),
                                contentDescription = "History")
                        }
                        Text("Focus Timer", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        IconButton(
                            onClick = {
                                context.startActivity(
                                    Intent(context, SettingsActivity::class.java)
                                )
                            },
                            enabled = !isTimerRunning

                        ) {
                            Icon(painter = painterResource(R.drawable.settings),
                                contentDescription = "Settings")
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
    ) {innerPadding ->
        Surface(modifier = Modifier.padding(innerPadding)) {

            Column(
                Modifier
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // The timer in here
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Timer(
                        totalTime = totalTime,
                        currentTime = currentTime,
                        isTimeRunning = isTimerRunning,
                        activeBarColor = if (selectedTab == 0) Color(0xFFFF5722) else Color(0xFF66BB6A),
                        onTimeChange = { newTime ->
                            currentTime = newTime
                            if (newTime <= 0L) {
                                isTimerRunning = false
                                if (selectedTab == 0)
                                    selectedTab = 1
                            }
                        },
                        modifier = Modifier.size(250.dp)
                    )


                }

                //if condition for checking if timer is running and make every button disable for pressing (beside pause)
                Spacer(modifier.padding(top = 20.dp))
                if (isTimerRunning) {
                    if (selectedTab == 0) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.height(50.dp).padding(16.dp)

                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                //making inputted text appear on screen
                                Text(
                                    text="Focusing on:",
                                    color = Color.Gray,
                                    fontSize = 16.sp,
                                    fontWeight= FontWeight.Medium
                                )
                                Text(
                                    text=textField.ifEmpty { "Task"},
                                    color = Color.Gray,
                                    fontSize = 16.sp,
                                    fontWeight= FontWeight.Bold
                                )
                            }
                        }

                    }else{
                        Spacer(modifier.height(50.dp))
                    }
                }else {
                    if (selectedTab == 0) {
                        // Text field
                        BasicTextField(
                            value = textField,
                            onValueChange = { newText ->
                                textField = newText
                            },
                            modifier = modifier.width(250.dp).height(50.dp).shadow(
                                elevation = 2.dp,
                                shape = RoundedCornerShape(20.dp),
                                clip = true
                            ).background(Color.White),
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    if (textField.isEmpty())
                                        Text(
                                            "What are you working on?",
                                            color = Color.LightGray
                                        )
                                    innerTextField()
                                }
                            }
                        )
                    }else{
                        Spacer(modifier.height(50.dp))
                    }
                }
                Spacer(modifier.padding(top = 20.dp))

                // if condition for button colors and pause logic
                val buttonColor =
                    if (!isTimerRunning) {
                        if (selectedTab == 0) Color(0xFFFF5722) else Color(0xFF66BB6A)
                    } else {
                        Color.White
                    }
                val textColor =
                    if(!isTimerRunning) Color.White
                    else if (selectedTab == 0) Color(0xFFFF5722)
                    else Color(0xFF66BB6A)
                val borderColor =
                    if(!isTimerRunning) null
                    else if (selectedTab == 0) BorderStroke(2.dp, Color(0xFFFF5722))
                    else BorderStroke(2.dp, Color(0xFF66BB6A))
                val buttonTextString =
                    if(isTimerRunning) "Pause"
                    else if(selectedTab == 0) "Start Focus"
                    else "Start Break"


                // This is start button/Pause button
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonColor,
                        contentColor = textColor
                    ),
                    border = borderColor,
                    onClick = {
                        if(currentTime <= 0L){
                            currentTime = totalTime
                            isTimerRunning = true
                        }else{
                            isTimerRunning = !isTimerRunning
                        }
                    },
                    modifier = Modifier
                        .width(250.dp)
                        .height(50.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(20.dp),
                            clip = true
                        )
                ) {
                    Text(text= buttonTextString, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
                }
            }
        }

    }
}

@Preview
@Composable
fun TimerScreenPreview(){
    TimerScreen()
}