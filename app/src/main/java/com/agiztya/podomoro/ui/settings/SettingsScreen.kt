package com.agiztya.podomoro.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agiztya.podomoro.ui.theme.BackgroundColor
import com.agiztya.podomoro.ui.theme.OrangePrimary
import com.agiztya.podomoro.ui.theme.SettingsLabelGrey
import com.agiztya.podomoro.ui.theme.SettingsTextBlue

// --- 1. STATEFUL (Screen Utama yang dipakai di App) ---
@Composable
fun SettingScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    // Intercept back gesture
    BackHandler {
        onBack()
    }

    // Ambil data asli dari ViewModel
    val podomoroLength by viewModel.podomoroLength.collectAsState()
    val shortBreakLength by viewModel.shortBreakLength.collectAsState()
    val longBreakLength by viewModel.longBreakLength.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    // Panggil Content UI dan kirim datanya
    SettingScreenContent(
        onBackClicked = onBack,
        podomoroLength = podomoroLength,
        shortBreakLength = shortBreakLength,
        longBreakLength = longBreakLength,
        isDarkMode = isDarkMode,
        onToggleDarkMode = { viewModel.toggleDarkMode(it) }
    )
}

// --- 2. STATELESS (UI Murni - Ini yang dipanggil Preview) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreenContent(
    onBackClicked: () -> Unit,
    podomoroLength: Int,
    shortBreakLength: Int,
    longBreakLength: Int,
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            SectionHeader(text = "TIMER DURATION")

            SettingsGroupCard {
                SettingsItem(
                    title = "Podomoro Length",
                    value = "$podomoroLength min",
                    onClick = { }
                )
                HorizontalDivider(color = BackgroundColor, thickness = 1.dp)
                SettingsItem(
                    title = "Short Break Length",
                    value = "$shortBreakLength min",
                    onClick = { }
                )
                HorizontalDivider(color = BackgroundColor, thickness = 1.dp)
                SettingsItem(
                    title = "Long Break Length",
                    value = "$longBreakLength min",
                    showDivider = false,
                    onClick = { }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader(text = "PREFERENCES")

            SettingsGroupCard {
                SettingsSwitchItem(
                    title = "Dark Mode",
                    checked = isDarkMode,
                    onCheckedChange = onToggleDarkMode
                )
            }
        }
    }
}

// --- Helper Components ---
@Composable
fun SectionHeader(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
    )
}

@Composable
fun SettingsGroupCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(content = content)
    }
}

@Composable
fun SettingsItem(title: String, value: String, showDivider: Boolean = true, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(value, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp, modifier = Modifier.padding(end = 8.dp))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun SettingsSwitchItem(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical =8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                uncheckedThumbColor = Color.White,
                checkedTrackColor = OrangePrimary,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                uncheckedBorderColor = Color.Transparent,
                checkedBorderColor = Color.Transparent    
            )
        )
    }
}


@Preview(showBackground = true, showSystemUi = true, device = "id:pixel_5")
@Composable
fun PreviewSettings() {
    SettingScreenContent(
        onBackClicked = {},
        podomoroLength = 25,
        shortBreakLength = 5,
        longBreakLength = 15,
        isDarkMode = false,
        onToggleDarkMode = {}
    )
}
