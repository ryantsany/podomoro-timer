package com.agiztya.podomoro.ui.timer

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
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
    activeBarColor: Color = Color(0xFFFF5722),
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
fun BottomBar(modifier: Modifier = Modifier){
    var selectedIndex by remember { mutableStateOf(0) }

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

            NavigationBarItem(
                selected = isSelected,
                modifier = Modifier,
                onClick = {
                    selectedIndex = index
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFFFF5722),
                    unselectedTextColor = Color.Gray,
                    indicatorColor = Color.White
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
    val totalTime = 25 * 60 * 1000L

    var isTimerRunning by remember { mutableStateOf(false) }
    var currentTime by remember { mutableStateOf(totalTime) }
    var textField by remember { mutableStateOf("") }

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
                            onClick = {}
                        ) {
                            Icon(painter = painterResource(R.drawable.history),
                                contentDescription = "History")
                        }
                        Text("Focus Timer", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        IconButton(
                            onClick = {}
                        ) {
                            Icon(painter = painterResource(R.drawable.settings),
                                contentDescription = "Settings")
                        }
                    }
                }
            )
        },
        bottomBar = { BottomBar(modifier) }
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
                        onTimeChange = { newTime ->
                            currentTime = newTime
                            if (newTime <= 0L) {
                                isTimerRunning = false
                            }
                        },
                        modifier = Modifier.size(250.dp)
                    )


                }
                

                Spacer(modifier.padding(top = 20.dp))

                // Text field
                BasicTextField(
                    value = textField,
                    onValueChange = {newText ->
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
                            if (textField.isEmpty()) {
                                Text(
                                    "What are you working on?",
                                    color = Color.LightGray
                                )
                            }
                            innerTextField()
                        }
                    }
                )
                Spacer(modifier.padding(top = 20.dp))

                // if condition for button colors
                val buttonColor =
                    if (!isTimerRunning) Color(0xFFFF5722)
                    else Color.White
                val textColor =
                    if(!isTimerRunning) Color.White
                    else Color(0xFFFF5722)
                val borderColor =
                    if(!isTimerRunning) null
                    else BorderStroke(2.dp, Color(0xFFFF5722))

                // This is start button
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
                    if(isTimerRunning){
                        Text(text= "Pause", fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
                    }else{
                        Text(text= "Start Focus", fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
                    }
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