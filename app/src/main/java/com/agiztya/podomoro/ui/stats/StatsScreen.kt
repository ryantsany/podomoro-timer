package com.agiztya.podomoro.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agiztya.podomoro.data.local.entity.PomodoroSession
import com.agiztya.podomoro.ui.theme.*
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.core.cartesian.AutoScrollCondition
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.shape.Shape
import androidx.compose.material3.MaterialTheme
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatScreen(viewModel: StatsViewModel, onBack: () -> Unit) {
    val allSessions by viewModel.allSessions.collectAsState()
    val todayFocusMinutes by viewModel.todayFocusMinutes.collectAsState()
    val todaySessionsCount by viewModel.todaySessionsCount.collectAsState()
    val weeklyActivityData by viewModel.weeklyActivityData.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopBar(onBack = onBack)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Summary(todayFocusMinutes, todaySessionsCount)
            WeeklyActivity(weeklyActivityData)
            RecentHistory(allSessions)
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun Summary(focusMinutes: Int, sessionsCount: Int) {
    val hours = focusMinutes / 60
    val minutes = focusMinutes % 60
    val focusText = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"

    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SummaryCard(
            title = "Today's Focus",
            value = focusText,
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            title = "Sessions",
            value = sessionsCount.toString(),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun SummaryCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun WeeklyActivity(weeklyData: List<Int>, modifier: Modifier = Modifier) {
    Card(
        modifier = Modifier.fillMaxWidth(0.9f),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.padding(start = 16.dp, top = 20.dp, end = 16.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(0.65f),
                    text = "Weekly Activity",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
                        text = "Last 7 Days",
                        fontWeight = FontWeight.SemiBold,
                        color = LightGreyText
                    )
                }
            }
            WeeklyActivityChart(weeklyData)
        }
    }
}

@Composable
fun WeeklyActivityChart(weeklyData: List<Int>) {
    var highestActivityIndex = 0
    for (i in weeklyData.indices) {
        if (weeklyData[i] > weeklyData[highestActivityIndex]) {
            highestActivityIndex = i
        }
    }

    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(weeklyData) {
        modelProducer.runTransaction {
            columnSeries { series(weeklyData) }
        }
    }

    val lightPinkColumn = rememberLineComponent(
        color = LightChartBar,
        thickness = 24.dp,
        shape = Shape.rounded(allPercent = 50)
    )
    val brightRedColumn = rememberLineComponent(
        color = BrightChartBar,
        thickness = 24.dp,
        shape = Shape.rounded(allPercent = 50)
    )

    val columnProvider = object : ColumnCartesianLayer.ColumnProvider {
        override fun getColumn(entry: ColumnCartesianLayerModel.Entry, seriesIndex: Int, extraStore: ExtraStore): LineComponent {
            return if (entry.x.toInt() == highestActivityIndex && weeklyData[highestActivityIndex] > 0) brightRedColumn else lightPinkColumn
        }
        override fun getWidestSeriesColumn(seriesIndex: Int, extraStore: ExtraStore): LineComponent = brightRedColumn
    }

    val chart = rememberCartesianChart(
        rememberColumnCartesianLayer(columnProvider = columnProvider, columnCollectionSpacing = 6.dp),
        bottomAxis = rememberBottomAxis(
            line = rememberLineComponent(thickness = 0.dp),
            label = rememberTextComponent(color = LightGreyText, textSize = 14.sp),
            valueFormatter = { value, _, _ ->
                val calendar = Calendar.getInstance()
                // index 6 is today, 5 is yesterday, etc.
                calendar.add(Calendar.DAY_OF_YEAR, value.toInt() - 6)
                val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                when(dayOfWeek) {
                    Calendar.SUNDAY -> "S"
                    Calendar.MONDAY -> "M"
                    Calendar.TUESDAY -> "T"
                    Calendar.WEDNESDAY -> "W"
                    Calendar.THURSDAY -> "T"
                    Calendar.FRIDAY -> "F"
                    Calendar.SATURDAY -> "S"
                    else -> ""
                }
            },
            tick = null,
            guideline = null
        ),
    )

    CartesianChartHost(
        chart = chart,
        modelProducer = modelProducer,
        modifier = Modifier.fillMaxWidth().height(280.dp).padding(16.dp),
        scrollState = rememberVicoScrollState(scrollEnabled = false, autoScrollCondition = AutoScrollCondition.Never)
    )
}

@Composable
fun RecentHistory(sessions: List<PomodoroSession>) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp).fillMaxWidth()
    ) {
        Text(
            text = "Recent History",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            sessions.forEach { session ->
                HistoryCard(session)
            }
        }
    }
}

@Composable
fun HistoryCard(session: PomodoroSession) {
    val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())
    val dateFormatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = session.timestamp
    val sessionTime = timeFormatter.format(calendar.time)
    
    val today = Calendar.getInstance()
    val isToday = today.get(Calendar.DATE) == calendar.get(Calendar.DATE) &&
            today.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
            today.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
    
    val displayDate = if (isToday) sessionTime else dateFormatter.format(calendar.time)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).background(HistoryGreenLight, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = HistoryGreen,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = session.taskName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(text = displayDate, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            }
            Box(
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(text = "${session.durationMinutes} min", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = LightGreyText)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(onBack: () -> Unit) {
    CenterAlignedTopAppBar(
        title = { Text(text = "Activity", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onBackground) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Back", modifier = Modifier.size(28.dp), tint = MaterialTheme.colorScheme.onBackground)
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
    )
}
