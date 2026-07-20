package com.tenco.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.ReportProblem
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tenco.R
import com.tenco.domain.OrderStatus
import com.tenco.feature.supplier.SupplierViewModel
import com.tenco.ui.components.EmptyState
import com.tenco.ui.components.TencoCard
import com.tenco.ui.components.TencoScaffold
import com.tenco.ui.theme.StatusPending
import com.tenco.ui.theme.TileBlue

private data class Notif(val icon: ImageVector, val title: String, val body: String, val time: String, val color: Color)

@Composable
fun NotificationsScreen(onBack: () -> Unit, viewModel: SupplierViewModel = hiltViewModel()) {
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val vendors by viewModel.allVendors.collectAsStateWithLifecycle()
    val cashPayments by viewModel.pendingCashPayments.collectAsStateWithLifecycle()
    val names = vendors.associate { it.id to it.name }
    val coconuts = stringResource(R.string.coconuts)
    val newOrderTitle = stringResource(R.string.new_orders)
    val cancelTitle = stringResource(R.string.ord_cancel_requested)

    val items = orders
        .filter { it.status == OrderStatus.PLACED || it.status == OrderStatus.CANCEL_REQUESTED }
        .map { o ->
            val name = names[o.vendorId] ?: ""
            val rel = android.text.format.DateUtils.getRelativeTimeSpanString(o.updatedAt).toString()
            if (o.status == OrderStatus.PLACED) {
                Notif(Icons.Rounded.ShoppingCart, newOrderTitle, "$name · ${o.quantity} $coconuts", rel, TileBlue)
            } else {
                Notif(Icons.Rounded.ReportProblem, cancelTitle, name, rel, StatusPending)
            }
        }

    TencoScaffold(title = stringResource(R.string.menu_notifications), onBack = onBack) { padding ->
        if (items.isEmpty() && cashPayments.isEmpty()) {
            EmptyState(R.drawable.ic_palm_leaf, stringResource(R.string.no_data))
        } else {
            LazyColumn(
                Modifier.padding(padding).padding(horizontal = 16.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(cashPayments) { p ->
                    CashApprovalCard(
                        name = names[p.vendorId] ?: "",
                        amount = com.tenco.core.Money.format(p.amountPaise),
                        onApprove = { viewModel.approvePayment(p.id) },
                        onReject = { viewModel.rejectPayment(p.id) },
                    )
                }
                items(items) { n ->
                    TencoCard(Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = CircleShape, color = n.color.copy(alpha = 0.15f), modifier = Modifier.size(44.dp)) {
                                Box(contentAlignment = Alignment.Center) { Icon(n.icon, null, tint = n.color) }
                            }
                            Column(Modifier.weight(1f).padding(start = 14.dp)) {
                                Text(n.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text(n.body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(n.time, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CashApprovalCard(name: String, amount: String, onApprove: () -> Unit, onReject: () -> Unit) {
    TencoCard(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = StatusPending.copy(alpha = 0.15f), modifier = Modifier.size(44.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Payments, null, tint = StatusPending) }
                }
                Column(Modifier.weight(1f).padding(start = 14.dp)) {
                    Text(stringResource(R.string.cash_payment_request), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("$name · $amount", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                androidx.compose.material3.OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text(stringResource(R.string.reject)) }
                androidx.compose.material3.Button(onClick = onApprove, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.approve))
                }
            }
        }
    }
}
