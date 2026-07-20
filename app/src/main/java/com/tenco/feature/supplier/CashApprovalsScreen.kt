package com.tenco.feature.supplier

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tenco.R
import com.tenco.core.Money
import com.tenco.ui.components.CashApprovalCard
import com.tenco.ui.components.EmptyState
import com.tenco.ui.components.TencoScaffold

@Composable
fun SupplierCashApprovalsScreen(onBack: () -> Unit, viewModel: SupplierViewModel = hiltViewModel()) {
    val pending by viewModel.pendingCashPayments.collectAsStateWithLifecycle()
    val vendors by viewModel.allVendors.collectAsStateWithLifecycle()
    val names = vendors.associate { it.id to it.name }

    TencoScaffold(title = stringResource(R.string.pending_cash), onBack = onBack) { padding ->
        if (pending.isEmpty()) {
            EmptyState(R.drawable.ic_coconut, stringResource(R.string.no_data))
        } else {
            LazyColumn(
                Modifier.padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(pending, key = { it.id }) { p ->
                    CashApprovalCard(
                        name = names[p.vendorId] ?: "",
                        amount = Money.format(p.amountPaise),
                        onApprove = { viewModel.approvePayment(p.id) },
                        onReject = { viewModel.rejectPayment(p.id) },
                    )
                }
            }
        }
    }
}
