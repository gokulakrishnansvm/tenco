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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tenco.R
import com.tenco.core.Money
import com.tenco.domain.ComplaintStatus
import com.tenco.domain.PaymentStatus
import com.tenco.ui.components.HeroEarningsCard
import com.tenco.ui.components.SectionHeader
import com.tenco.ui.components.StatusChip
import com.tenco.ui.components.TencoCard
import com.tenco.ui.components.TencoScaffold
import com.tenco.ui.theme.StatusCompleted
import com.tenco.ui.theme.StatusFailed
import com.tenco.ui.theme.TileBlue
import com.tenco.ui.theme.TileGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dFmt = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())

private data class LedgerEntry(val time: Long, val title: String, val amount: String, val amountColor: androidx.compose.ui.graphics.Color, val status: String, val statusLabel: String)

@Composable
fun VendorDetailScreen(vendorId: String, onBack: () -> Unit, viewModel: SupplierViewModel = hiltViewModel()) {
    val vendors by viewModel.vendors.collectAsStateWithLifecycle()
    val deliveries by viewModel.deliveries.collectAsStateWithLifecycle()
    val payments by viewModel.payments.collectAsStateWithLifecycle()
    val complaints by viewModel.complaints.collectAsStateWithLifecycle()

    val vendor = vendors.firstOrNull { it.id == vendorId }
    val vDeliveries = deliveries.filter { it.vendorId == vendorId }
    val vPayments = payments.filter { it.vendorId == vendorId }
    val vComplaints = complaints.filter { it.vendorId == vendorId }
    val billed = vDeliveries.sumOf { it.quantity * it.unitPricePaise }
    val paid = vPayments.filter { it.status == PaymentStatus.COMPLETED }.sumOf { it.amountPaise }
    val adjust = vComplaints.filter { it.status == ComplaintStatus.RESOLVED }.sumOf { it.adjustmentPaise }
    val dues = (billed - adjust - paid).coerceAtLeast(0)

    val soldLabel = stringResource(R.string.sell_to_vendor)
    val paymentLabel = stringResource(R.string.received)
    val entries = buildList {
        vDeliveries.forEach { add(LedgerEntry(it.createdAt, "$soldLabel · ${it.quantity} @ ${Money.formatShort(it.unitPricePaise)}", "+" + Money.formatShort(it.quantity * it.unitPricePaise), TileBlue, it.status, statusLabelOf(it.status))) }
        vPayments.forEach { add(LedgerEntry(it.createdAt, paymentLabel, "-" + Money.formatShort(it.amountPaise), StatusCompleted, it.status, statusLabelOf(it.status))) }
    }.sortedByDescending { it.time }

    TencoScaffold(title = vendor?.name ?: stringResource(R.string.vendors), onBack = onBack) { padding ->
        LazyColumn(
            Modifier.padding(padding).padding(horizontal = 16.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                HeroEarningsCard(
                    label = stringResource(R.string.pending_dues),
                    paise = dues,
                    caption = "${vendor?.phone ?: ""}  ·  ${vendor?.upiVpa ?: "-"}",
                )
            }
            item { SectionHeader(stringResource(R.string.transaction_history)) }
            items(entries) { e ->
                TencoCard(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(e.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(dFmt.format(Date(e.time)), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(e.amount, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = e.amountColor)
                    }
                }
            }
        }
    }
}

@Composable
private fun statusLabelOf(status: String): String = status.lowercase().replaceFirstChar { it.uppercase() }
