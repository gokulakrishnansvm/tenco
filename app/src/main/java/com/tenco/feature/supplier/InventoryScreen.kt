package com.tenco.feature.supplier

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.tenco.ui.theme.StatusCompleted
import com.tenco.ui.theme.StatusFailed
import com.tenco.ui.theme.StatusPending
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val invDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

@Composable
fun InventoryScreen(onBack: (() -> Unit)? = null, viewModel: SupplierViewModel = hiltViewModel()) {
    val purchases by viewModel.purchases.collectAsStateWithLifecycle()
    val dealers by viewModel.dealers.collectAsStateWithLifecycle()
    val deliveries by viewModel.deliveries.collectAsStateWithLifecycle()
    val dealerById = dealers.associateBy { it.id }

    val totalIn = purchases.sumOf { it.quantity }.coerceAtLeast(1)
    val totalOut = deliveries.sumOf { it.quantity }
    val soldRatio = (totalOut.toFloat() / totalIn).coerceIn(0f, 1f)

    TencoScaffold(title = stringResource(R.string.menu_inventory), onBack = onBack) { padding ->
        if (purchases.isEmpty()) {
            EmptyState(stringResource(R.string.no_data))
        } else {
            LazyColumn(
                Modifier.padding(padding).padding(horizontal = 16.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(purchases) { p ->
                    val dealer = dealerById[p.dealerId]
                    BatchCard(
                        batchNo = "Batch #${p.id.take(6).uppercase()}",
                        market = dealer?.location ?: "-",
                        dealer = dealer?.name ?: "-",
                        quantity = p.quantity,
                        remaining = (p.quantity * (1f - soldRatio)).toInt(),
                        costText = Money.format(p.quantity * p.unitCostPaise),
                        dateText = invDate.format(Date(p.createdAt)),
                        soldRatio = soldRatio,
                    )
                }
            }
        }
    }
}

@Composable
private fun BatchCard(
    batchNo: String,
    market: String,
    dealer: String,
    quantity: Int,
    remaining: Int,
    costText: String,
    dateText: String,
    soldRatio: Float,
) {
    var expanded by remember { mutableStateOf(false) }
    val pct = remaining.toFloat() / quantity.coerceAtLeast(1)
    val (badge, badgeColor) = when {
        pct <= 0.15f -> "Low stock" to StatusFailed
        pct <= 0.5f -> "Selling" to StatusPending
        else -> "In stock" to StatusCompleted
    }
    TencoCard(Modifier.fillMaxWidth(), onClick = { expanded = !expanded }) {
        Column(Modifier.fillMaxWidth().padding(16.dp).animateContentSize()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(batchNo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("$market · $dealer", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(shape = RoundedCornerShape(50), color = badgeColor.copy(alpha = 0.15f)) {
                    Text(badge, style = MaterialTheme.typography.labelMedium, color = badgeColor, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("$remaining / $quantity ${stringResource(R.string.coconuts)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text("${(pct * 100).toInt()}% left", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            LinearProgressIndicator(
                progress = { pct },
                modifier = Modifier.fillMaxWidth().height(8.dp).padding(top = 6.dp),
                color = badgeColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            if (expanded) {
                Column(Modifier.padding(top = 14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    DetailRow(stringResource(R.string.purchase_cost), costText)
                    DetailRow(stringResource(R.string.dealers), dealer)
                    DetailRow("Purchased", dateText)
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}
