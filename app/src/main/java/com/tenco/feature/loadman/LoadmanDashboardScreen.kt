package com.tenco.feature.loadman

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.tenco.data.local.OrderEntity
import com.tenco.domain.OrderStatus
import com.tenco.ui.components.EmptyState
import com.tenco.ui.components.OrderStatusChip
import com.tenco.ui.components.TencoCard
import com.tenco.ui.components.coconutColorLabel
import com.tenco.ui.components.coconutGradeLabel

private val DELIVERABLE = setOf(OrderStatus.CONFIRMED, OrderStatus.IN_PROGRESS, OrderStatus.IN_TRANSIT)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadmanDashboardScreen(
    onChangeLanguage: () -> Unit,
    onLogout: () -> Unit,
    viewModel: LoadmanViewModel = hiltViewModel(),
) {
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val vendors by viewModel.vendors.collectAsStateWithLifecycle()
    val names = remember(vendors) { vendors.associate { it.id to it.name } }
    val coconuts = stringResource(R.string.coconuts)

    fun groupsFor(filter: (OrderEntity) -> Boolean) =
        orders.filter(filter)
            .groupBy { it.groupId.ifBlank { it.id } }
            .toList()
            .sortedByDescending { (_, ls) -> ls.maxOf { it.updatedAt } }

    val toDeliver = groupsFor { it.status in DELIVERABLE && it.unitPricePaise != null }
    val delivered = groupsFor { it.status == OrderStatus.DELIVERED }.take(20)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.deliveries)) },
                navigationIcon = {
                    IconButton(onClick = onChangeLanguage) { Icon(Icons.Rounded.Translate, contentDescription = stringResource(R.string.change_language)) }
                },
                actions = {
                    IconButton(onClick = onLogout) { Icon(Icons.Rounded.Logout, contentDescription = null) }
                },
            )
        },
    ) { padding ->
        if (toDeliver.isEmpty() && delivered.isEmpty()) {
            Column(Modifier.fillMaxSize().padding(padding), verticalArrangement = Arrangement.Center) {
                EmptyState(R.drawable.ic_coconut, stringResource(R.string.nothing_to_deliver))
            }
            return@Scaffold
        }
        LazyColumn(
            Modifier.padding(padding).fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Text("${stringResource(R.string.to_deliver)} (${toDeliver.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            if (toDeliver.isEmpty()) {
                item { Text(stringResource(R.string.nothing_to_deliver), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            items(toDeliver, key = { it.first }) { (_, lines) ->
                DeliveryCard(lines, names, coconuts, actionable = true) {
                    viewModel.markGroupDelivered(lines.map { it.id })
                }
            }
            if (delivered.isNotEmpty()) {
                item {
                    Text(stringResource(R.string.delivered), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                }
                items(delivered, key = { it.first }) { (_, lines) ->
                    DeliveryCard(lines, names, coconuts, actionable = false, onDelivered = {})
                }
            }
        }
    }
}

@Composable
private fun DeliveryCard(
    lines: List<OrderEntity>,
    names: Map<String, String>,
    coconuts: String,
    actionable: Boolean,
    onDelivered: () -> Unit,
) {
    val head = lines.first()
    val total = lines.sumOf { (it.unitPricePaise ?: 0L) * it.quantity }
    TencoCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(names[head.vendorId] ?: "—", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OrderStatusChip(head.status)
            }
            if (head.sourceLocation.isNotBlank()) {
                Text("${stringResource(R.string.source_location)}: ${head.sourceLocation}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            lines.forEach { l ->
                Text("${coconutColorLabel(l.color)} ${coconutGradeLabel(l.grade)} · ${l.quantity} $coconuts", style = MaterialTheme.typography.bodyMedium)
            }
            if (total > 0) {
                Text(Money.format(total), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            if (actionable) {
                Button(onClick = onDelivered, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.mark_delivered))
                }
            }
        }
    }
}
