package com.agiztya.podomoro.ui.complete

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agiztya.podomoro.ui.theme.FocusRed

@Composable
fun TimerCompleteScreen(
    taskName: String,
    durationMinutes: Int,
    onTakeABreak: () -> Unit,
    onSkip: () -> Unit
) {
    // Container Utama (Background Full Screen)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FocusRed),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .fillMaxWidth()
        ) {

            // --- Bagian Ikon Centang ---
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .border(width = 4.dp, color = Color.White, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Completed",
                    tint = Color.White,
                    modifier = Modifier.size(60.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // --- Judul Besar ---
            Text(
                text = "Focus Completed!",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Subjudul ---
            val sessionText = if (taskName.isNotBlank()) "on \"$taskName\"" else "on your task"
            Text(
                text = "You just focused for $durationMinutes minutes $sessionText.",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(60.dp))

            // --- Tombol "Take a Break" ---
            Button(
                onClick = onTakeABreak,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = FocusRed
                ),
                shape = RoundedCornerShape(50), // Membuat tombol bulat (pill shape)
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "Take a Break",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Teks Footer (Skip Break) ---
            Text(
                text = "Skip Break & Start New Session",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clickable { onSkip() }
                    .padding(8.dp) // Padding agar area klik lebih luas
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TimerCompleteScreenPreview() {
    MaterialTheme {
        TimerCompleteScreen(
            taskName = "Coding",
            durationMinutes = 25,
            onTakeABreak = {},
            onSkip = {}
        )
    }
}
