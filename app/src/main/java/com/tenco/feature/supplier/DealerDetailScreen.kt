package com.tenco.feature.supplier

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tenco.R
import com.tenco.core.Money
import com.tenco.ui.components.HeroEarningsCard
import com.tenco.ui.components.SectionHeader
import com.tenco.ui.components.TencoCard
import com.tenco.ui.components.TencoScaffold
import com.tenco.ui.theme.Gradients
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dealerFmt = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())

@Composable
fun DealerDetailScreen(dealerId: String, onBack: () -> Unit, viewModel: SupplierViewModel = hiltViewModel()) {
    val dealers by viewModel.dealers.collectAsStateWithLifecycle()
    val purchases by viewModel.purchases.collectAsStateWithLifecycle()

    val dealer = dealers.firstOrNull { it.id == dealerId }
    val dPurchases = purchases.filter { it.dealerId == dealerId }.sortedByDescending { it.createdAt }
    val totalSpent = dPurchases.sumOf { it.quantity * it.unitCostPaise }
    val totalQty = dPurchases.sumOf { it.quantity }
    var showDelete by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    TencoScaffold(
        title = dealer?.name ?: stringResource(R.string.dealers),
        onBack = onBack,
        actions = {
            androidx.compose.material3.IconButton(onClick = { showDelete = true }) {
                androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Rounded.DeleteOutline, contentDescription = stringResource(R.string.delete), tint = MaterialTheme.colorScheme.onPrimary)
            }
        },
    ) { padding ->
        LazyColumn(
            Modifier.padding(padding).padding(horizontal = 16.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                HeroEarningsCard(
                    label = stringResource(R.string.purchase_cost),
                    paise = totalSpent,
                    caption = "${dealer?.location ?: ""}  ·  $totalQty ${stringResource(R.string.coconuts)}",
                    gradient = Gradients.lime,
                )
            }
            item { SectionHeader(stringResource(R.string.transaction_history)) }
            items(dPurchases) { p ->
                TencoCard(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("${p.quantity} ${stringResource(R.string.coconuts)} @ ${Money.formatShort(p.unitCostPaise)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(dealerFmt.format(Date(p.createdAt)), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(Money.format(p.quantity * p.unitCostPaise), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }

    if (showDelete) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("${stringResource(R.string.delete)} ${dealer?.name ?: ""}") },
            text = { Text(stringResource(R.string.delete_confirm)) },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    viewModel.deleteDealer(dealerId); showDelete = false; onBack()
                }) { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showDelete = false }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
}
