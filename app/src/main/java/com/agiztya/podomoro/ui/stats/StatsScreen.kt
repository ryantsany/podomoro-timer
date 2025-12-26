package com.agiztya.podomoro.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agiztya.podomoro.ui.theme.BackgroundColor
import com.agiztya.podomoro.ui.theme.BrightChartBar
import com.agiztya.podomoro.ui.theme.LightChartBar
import com.agiztya.podomoro.ui.theme.LightGreyBackground
import com.agiztya.podomoro.ui.theme.LightGreyText
import com.agiztya.podomoro.ui.theme.TextGrey
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

@Composable
fun Stat(modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier
            .fillMaxSize(),
        containerColor = BackgroundColor,
        topBar = {
            TopBar()
        }
    ) { 
        StatContent(
            modifier = Modifier.padding(it)
        )
    }
}

@Composable
fun StatContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Summary()
        WeeklyActivity()
        RecentHistory()
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun Summary() {
    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(15.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ){
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Today's Focus",
                    fontWeight = FontWeight.SemiBold,
                    color = TextGrey
                )
                Text(
                    text = "4h 15m",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp
                )
            }
        }
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(15.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ){
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Sessions",
                    fontWeight = FontWeight.SemiBold,
                    color = TextGrey
                )
                Text(
                    text = "5",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp
                )
            }

        }
    }
}

@Composable
fun WeeklyActivity(modifier: Modifier = Modifier) {
    Card(
        modifier = Modifier.fillMaxWidth(0.9f),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ){
        Column(
            modifier = Modifier
        ) {
            Row(
                modifier = Modifier
                    .padding(start = 16.dp, top = 20.dp, end = 16.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = modifier
                        .fillMaxWidth(0.65f),
                    text = "Weekly Activity",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LightGreyBackground, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        modifier = modifier
                            .padding(vertical = 4.dp, horizontal = 8.dp),
                        text = "Last 7 Days",
                        fontWeight = FontWeight.SemiBold,
                        color = LightGreyText
                    )
                }
            }

            WeeklyActivityChart()
        }

    }
}

@Composable
fun WeeklyActivityChart() {
    val activitiesCount = listOf(
        2, 1, 4, 5, 3, 4, 2
    )

    var highestActivityIndex = 0

    for (i in 0 until activitiesCount.size - 1) {
        if (activitiesCount[i] > activitiesCount[highestActivityIndex]) {
            highestActivityIndex = i
        }
    }

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(Unit) {
        modelProducer.runTransaction {
            columnSeries {
                series(activitiesCount)
            }
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
        override fun getColumn(
            entry: ColumnCartesianLayerModel.Entry,
            seriesIndex: Int,
            extraStore: ExtraStore
        ): LineComponent {
            return if (entry.x.toInt() == highestActivityIndex) brightRedColumn else lightPinkColumn
        }

        override fun getWidestSeriesColumn(
            seriesIndex: Int,
            extraStore: ExtraStore
        ): LineComponent {
            return brightRedColumn
        }
    }

    val chart = rememberCartesianChart(
        rememberColumnCartesianLayer(
            columnProvider = columnProvider,
            columnCollectionSpacing = 6.dp
        ),
        bottomAxis = rememberBottomAxis(
            line = rememberLineComponent(
                thickness = 0.dp
            ),
            label = rememberTextComponent(
                color = LightGreyText,
                textSize = 14.sp
            ),
            valueFormatter = { value, _, _ ->
                listOf("S", "M", "T", "W", "T", "F", "S").getOrNull(value.toInt()) ?: ""
            },
            tick = null,
            guideline = null
        ),
    )

    CartesianChartHost(
        chart = chart,
        modelProducer = modelProducer,
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(16.dp),
        scrollState = rememberVicoScrollState(
            scrollEnabled = false,
            autoScrollCondition = AutoScrollCondition.Never,
        )
    )
}

@Composable
fun RecentHistory(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = "Recent History",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        val historyItems = listOf(
            HistoryItemData("Study Math", "10:00 AM", "25 min", Icons.Default.Edit, Color(0xFFEF4444), Color(0xFFFEE2E2)),
            HistoryItemData("Reading", "9:15 AM", "25 min", Icons.Default.Add, Color(0xFF3B82F6), Color(0xFFDBEAFE)),
            HistoryItemData("Coding", "8:00 AM", "50 min", Icons.Default.Call, Color(0xFF10B981), Color(0xFFD1FAE5)),
            HistoryItemData("Writing", "Yesterday", "25 min", Icons.Default.Edit, Color(0xFF8B5CF6), Color(0xFFEDE9FE))
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            historyItems.forEach { item ->
                HistoryCard(item)
            }
        }
    }
}

data class HistoryItemData(
    val title: String,
    val time: String,
    val duration: String,
    val icon: ImageVector,
    val iconColor: Color,
    val iconBgColor: Color
)

@Composable
fun HistoryCard(item: HistoryItemData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(item.iconBgColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = item.iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = item.time,
                    color = LightGreyText,
                    fontSize = 14.sp
                )
            }
            Box(
                modifier = Modifier
                    .background(LightGreyBackground, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = item.duration,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextGrey
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(modifier: Modifier = Modifier) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = "Activity",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        navigationIcon = {
            IconButton(onClick = {

            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Back",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = BackgroundColor
        )
    )
}

@Preview
@Composable
private fun StatPreview() {
    Stat()
}
