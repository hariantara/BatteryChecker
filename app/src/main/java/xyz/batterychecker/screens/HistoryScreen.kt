package xyz.batterychecker.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import xyz.batterychecker.viewmodel.HistoryViewModel
import java.time.format.DateTimeFormatter
import xyz.batterychecker.viewmodel.BatteryHistoryItem
import androidx.compose.foundation.shape.RoundedCornerShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = viewModel()
) {
    val historyState by viewModel.historyState.collectAsState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        TabRow(
            selectedTabIndex = historyState.selectedTab,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = historyState.selectedTab == 0,
                onClick = { viewModel.updateSelectedTab(0) },
                text = { Text("Daily") }
            )
            Tab(
                selected = historyState.selectedTab == 1,
                onClick = { viewModel.updateSelectedTab(1) },
                text = { Text("Weekly") }
            )
            Tab(
                selected = historyState.selectedTab == 2,
                onClick = { viewModel.updateSelectedTab(2) },
                text = { Text("Monthly") }
            )
        }

        // Chart
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(top = 16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            shadowElevation = 4.dp
        ) {
            val entries = historyState.chartEntries.mapIndexed { index, entry ->
                FloatEntry(
                    x = index.toFloat(),
                    y = entry.avgLevel
                )
            }

            Chart(
                chart = lineChart(),
                model = entryModelOf(entries),
                startAxis = startAxis(
                    valueFormatter = { value, _ -> "${value.toInt()}%" }
                ),
                bottomAxis = bottomAxis(
                    valueFormatter = { index, _ ->
                        historyState.chartEntries.getOrNull(index.toInt())?.timePoint ?: ""
                    }
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )
        }

        // History List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val history = when (historyState.selectedTab) {
                0 -> historyState.dailyHistory
                1 -> historyState.weeklyHistory
                else -> historyState.monthlyHistory
            }
            items(history) { item ->
                HistoryItem(item)
            }
        }
    }
}

@Composable
private fun HistoryItem(item: BatteryHistoryItem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = item.timestamp.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm")),
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${item.startPercentage}% → ${item.endPercentage}%",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${item.duration}min",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "%.1f°C".format(item.temperature),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "%.1fV".format(item.voltage),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

private fun <A> List<Triple<A, A, A>>.unzip(): Triple<List<A>, List<A>, List<A>> {
    val first = ArrayList<A>(size)
    val second = ArrayList<A>(size)
    val third = ArrayList<A>(size)
    for (triple in this) {
        first.add(triple.first)
        second.add(triple.second)
        third.add(triple.third)
    }
    return Triple(first, second, third)
}