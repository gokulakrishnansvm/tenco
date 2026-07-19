package com.tenco.feature.supplier

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tenco.R
import com.tenco.core.Money
import com.tenco.ui.components.SectionTitle
import com.tenco.ui.components.StatCard
import com.tenco.ui.components.TencoScaffold

@Composable
fun InsightsScreen(onBack: (() -> Unit)? = null, viewModel: SupplierViewModel = hiltViewModel()) {
    val insights by viewModel.insights.collectAsStateWithLifecycle()

    TencoScaffold(title = stringResource(R.string.insights), onBack = onBack) { padding ->
        Column(
            Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    label = stringResource(R.string.total_billed),
                    value = Money.formatShort(insights.totalBilledPaise),
                    icon = Icons.Filled.TrendingUp,
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    label = stringResource(R.string.total_collected),
                    value = Money.formatShort(insights.totalCollectedPaise),
                    icon = Icons.Filled.CurrencyRupee,
                    modifier = Modifier.weight(1f),
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    label = stringResource(R.string.outstanding),
                    value = Money.formatShort(insights.outstandingPaise),
                    icon = Icons.Filled.CurrencyRupee,
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    label = stringResource(R.string.deliveries_count),
                    value = "${insights.deliveriesCount}",
                    icon = Icons.Filled.LocalShipping,
                    modifier = Modifier.weight(1f),
                )
            }

            SectionTitle("${stringResource(R.string.collection_rate)}: ${insights.collectionRatePercent}%")
            LinearProgressIndicator(
                progress = { insights.collectionRatePercent / 100f },
                modifier = Modifier.fillMaxWidth().height(10.dp),
            )

            SectionTitle(stringResource(R.string.top_vendors))
            val maxDues = (insights.topVendors.maxOfOrNull { it.duesPaise } ?: 1L).coerceAtLeast(1L)
            insights.topVendors.forEach { v ->
                Column(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(v.vendorName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        Text(Money.formatShort(v.duesPaise), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    // Simple horizontal bar proportional to dues.
                    Box(
                        Modifier.fillMaxWidth().height(10.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50)),
                    ) {
                        Box(
                            Modifier.fillMaxWidth(v.duesPaise.toFloat() / maxDues).fillMaxHeight()
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50)),
                        )
                    }
                }
            }
        }
    }
}
