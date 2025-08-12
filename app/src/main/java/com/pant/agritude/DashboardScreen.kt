package com.pant.agritude

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pant.agritude.data.MarketPrice
import com.pant.agritude.data.SampleChartData
import com.patrykandpatryk.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatryk.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatryk.vico.compose.chart.Chart
import com.patrykandpatryk.vico.compose.chart.column.columnChart
import com.patrykandpatryk.vico.core.axis.AxisPosition
import com.patrykandpatryk.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatryk.vico.core.entry.entryModelOf

@Composable
fun DashboardScreen(strings: AppStrings, viewModel: DashboardViewModel = viewModel()) {
    val marketDataState by viewModel.marketData.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = strings.dashboardTitle,
                style = MaterialTheme.typography.headlineMedium,
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        when (marketDataState) {
            is MarketDataState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.padding(top = 32.dp))
                Text(text = strings.loadingMessage, modifier = Modifier.padding(top = 16.dp))
            }
            is MarketDataState.Error -> {
                Text(text = strings.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 32.dp)
                )
            }
            is MarketDataState.Success -> {
                val data = (marketDataState as MarketDataState.Success).data.records
                if (data.isNotEmpty()) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(data) { marketPrice ->
                            MarketPriceCard(strings, marketPrice)
                        }
                    }
                } else {
                    Text(text = strings.noDataMessage,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 32.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MarketPriceCard(strings: AppStrings, marketPrice: MarketPrice) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Crop Icon",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = marketPrice.commodity,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            PriceTrendChart(strings = strings, marketPrice = marketPrice)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${strings.averagePrice}: ${marketPrice.price} ${strings.currency}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${strings.market}: ${marketPrice.market}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

// vico Bar Chart-க்கான Composable Function
@Composable
fun PriceTrendChart(strings: AppStrings, marketPrice: MarketPrice) {
    val sampleData = remember(marketPrice.price) {
        SampleChartData.getSampleData(marketPrice.price.toFloatOrNull() ?: 0f)
    }

    // ChartEntryModel-ஐ List<FloatEntry>-இல் இருந்து உருவாக்குகிறோம்
    val model = entryModelOf(sampleData)

    val axisValueFormatter = remember {
        AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
            SampleChartData.labels.getOrNull(value.toInt()) ?: ""
        }
    }

    Chart(
        chart = columnChart(),
        model = model,
        startAxis = rememberStartAxis(),
        bottomAxis = rememberBottomAxis(valueFormatter = axisValueFormatter),
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(horizontal = 8.dp)
    )
}
