package com.tenco.feature.supplier

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
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
import com.tenco.domain.CoconutColor
import com.tenco.domain.CoconutGrade
import com.tenco.ui.components.EmptyState
import com.tenco.ui.components.SectionHeader
import com.tenco.ui.components.TencoCard
import com.tenco.ui.components.TencoScaffold
import com.tenco.ui.components.coconutColorLabel
import com.tenco.ui.components.coconutColorSwatch
import com.tenco.ui.components.coconutGradeLabel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val invDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

@Composable
fun InventoryScreen(onBack: (() -> Unit)? = null, viewModel: SupplierViewModel = hiltViewModel()) {
    val purchases by viewModel.purchases.collectAsStateWithLifecycle()
    val dealers by viewModel.allDealers.collectAsStateWithLifecycle()
    val dealerById = dealers.associateBy { it.id }
    val valid = purchases.filter { dealerById.containsKey(it.dealerId) }

    TencoScaffold(title = stringResource(R.string.menu_inventory), onBack = onBack) { padding ->
        if (valid.isEmpty()) {
            EmptyState(R.drawable.ic_coconut, stringResource(R.string.no_data))
        } else {
            // Batches grouped (batchId, or the row id for legacy single purchases), oldest first for numbering.
            val batches = valid.groupBy { it.batchId.ifBlank { it.id } }
                .toList().sortedBy { it.second.minOf { p -> p.createdAt } }
            LazyColumn(
                Modifier.padding(padding).padding(horizontal = 16.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Stock summary by colour + grade
                item { SectionHeader(stringResource(R.string.stock_summary)) }
                item {
                    TencoCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            CoconutColor.ALL.forEach { color ->
                                val forColor = valid.filter { it.color == color }
                                if (forColor.isNotEmpty()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(Modifier.size(12.dp).background(coconutColorSwatch(color), CircleShape))
                                        Text("  ${coconutColorLabel(color)}", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                        Text("${forColor.sumOf { it.quantity }}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                    CoconutGrade.ALL.forEach { grade ->
                                        val q = forColor.filter { it.grade == grade }.sumOf { it.quantity }
                                        if (q > 0) {
                                            Row(Modifier.padding(start = 20.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text(coconutGradeLabel(grade), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                                                Text("$q ${stringResource(R.string.coconuts)}", style = MaterialTheme.typography.bodyMedium)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item { SectionHeader(stringResource(R.string.batch)) }
                itemsIndexed(batches) { index, entry ->
                    val lines = entry.second
                    val dealer = dealerById[lines.first().dealerId]
                    TencoCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("${stringResource(R.string.batch)} #${index + 1}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text("${dealer?.name ?: ""} · ${dealer?.location ?: ""}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(invDate.format(Date(lines.first().createdAt)), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            lines.forEach { l ->
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(Modifier.size(10.dp).background(coconutColorSwatch(l.color), CircleShape))
                                        Text("  ${coconutColorLabel(l.color)} ${coconutGradeLabel(l.grade)}", style = MaterialTheme.typography.bodyMedium)
                                    }
                                    Text("${l.quantity} @ ${Money.formatShort(l.unitCostPaise)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
