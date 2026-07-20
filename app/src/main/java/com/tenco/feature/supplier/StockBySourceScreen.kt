package com.tenco.feature.supplier

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tenco.R
import com.tenco.core.Money
import com.tenco.ui.components.EmptyState
import com.tenco.ui.components.TencoCard
import com.tenco.ui.components.TencoScaffold
import com.tenco.ui.components.coconutColorLabel
import com.tenco.ui.components.coconutGradeLabel

/**
 * Supplier view of purchased stock grouped by source (dealer). Shows total quantity + cost per
 * source and a colour/grade breakdown, so the supplier can see where their stock came from.
 */
@Composable
fun StockBySourceScreen(onBack: () -> Unit, viewModel: SupplierViewModel = hiltViewModel()) {
    val purchases by viewModel.purchases.collectAsStateWithLifecycle()
    val dealers by viewModel.allDealers.collectAsStateWithLifecycle()
    val dealerNames = remember(dealers) { dealers.associate { it.id to it.name } }

    val bySource = purchases.groupBy { it.dealerId }
        .toList()
        .sortedByDescending { (_, ps) -> ps.sumOf { it.quantity } }

    TencoScaffold(title = stringResource(R.string.stock_by_source), onBack = onBack) { padding ->
        if (bySource.isEmpty()) {
            EmptyState(R.drawable.ic_coconut, stringResource(R.string.no_stock))
        } else {
            LazyColumn(
                Modifier.padding(padding).fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(bySource, key = { it.first }) { (dealerId, ps) ->
                    val totalQty = ps.sumOf { it.quantity }
                    val totalCost = ps.sumOf { it.quantity * it.unitCostPaise }
                    // Aggregate by colour+grade.
                    val breakdown = ps.groupBy { it.color to it.grade }
                        .mapValues { e -> e.value.sumOf { it.quantity } }
                        .toList()
                        .sortedByDescending { it.second }
                    TencoCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(dealerNames[dealerId] ?: "—", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("$totalQty ${stringResource(R.string.coconuts)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            breakdown.forEach { (combo, qty) ->
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("${coconutColorLabel(combo.first)} ${coconutGradeLabel(combo.second)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("$qty", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            Text("${stringResource(R.string.purchase_cost)}: ${Money.format(totalCost)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
