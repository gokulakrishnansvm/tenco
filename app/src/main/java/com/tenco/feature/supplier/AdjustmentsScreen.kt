package com.tenco.feature.supplier

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import com.tenco.domain.ComplaintStatus
import com.tenco.ui.components.EmptyState
import com.tenco.ui.components.HeroEarningsCard
import com.tenco.ui.components.SectionHeader
import com.tenco.ui.components.TencoCard
import com.tenco.ui.components.TencoScaffold
import com.tenco.ui.components.complaintReasonLabel

@Composable
fun SupplierAdjustmentsScreen(onBack: () -> Unit, viewModel: SupplierViewModel = hiltViewModel()) {
    val complaints by viewModel.complaints.collectAsStateWithLifecycle()
    val coconuts = stringResource(R.string.coconuts)

    // Total money adjusted (resolved complaints) — this is what reduces vendor dues / shows as loss.
    val totalAdjusted = complaints.filter { it.status == ComplaintStatus.RESOLVED }.sumOf { it.adjustmentPaise }
    val byReason = complaints.groupBy { it.reason }

    TencoScaffold(title = stringResource(R.string.adjustments), onBack = onBack) { padding ->
        if (complaints.isEmpty()) {
            EmptyState(R.drawable.ic_palm_leaf, stringResource(R.string.no_data))
        } else {
            LazyColumn(
                Modifier.padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    HeroEarningsCard(
                        label = stringResource(R.string.total_adjusted),
                        paise = totalAdjusted,
                        caption = stringResource(R.string.reduces_dues),
                    )
                }
                item { SectionHeader(stringResource(R.string.menu_complaints)) }
                byReason.forEach { (reason, list) ->
                    item(key = "reason-$reason") {
                        val qty = list.sumOf { it.shortQuantity }
                        val adjusted = list.filter { it.status == ComplaintStatus.RESOLVED }.sumOf { it.adjustmentPaise }
                        TencoCard(Modifier.fillMaxWidth()) {
                            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(complaintReasonLabel(reason), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        "${list.size} ${stringResource(R.string.menu_complaints)}" +
                                            if (qty > 0) " · ${stringResource(R.string.affected_qty)}: $qty $coconuts" else "",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Text(Money.format(adjusted), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
