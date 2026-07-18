package com.tenco.feature.vendor

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tenco.R
import com.tenco.core.Demo
import com.tenco.core.Money
import com.tenco.core.UpiPayment
import com.tenco.domain.PaymentStatus
import com.tenco.ui.components.EmptyState
import com.tenco.ui.components.StatCard
import com.tenco.ui.components.StatusChip
import com.tenco.ui.components.TencoScaffold
import com.tenco.ui.theme.StatusPending
import com.tenco.navigation.Routes
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateFmt = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())

// ---------------- Dashboard ----------------
@Composable
fun VendorDashboardScreen(
    vendorId: String,
    onNavigate: (String) -> Unit,
    onChangeLanguage: () -> Unit,
    onLogout: () -> Unit,
    viewModel: VendorViewModel = hiltViewModel(),
) {
    LaunchedEffect(vendorId) { viewModel.setVendor(vendorId) }
    val dashboard by viewModel.dashboard.collectAsStateWithLifecycle()
    val context = LocalContext.current

    TencoScaffold(
        title = stringResource(R.string.vendor_dashboard),
        actions = {
            IconButton(onClick = onChangeLanguage) {
                Icon(Icons.Filled.Translate, contentDescription = stringResource(R.string.change_language))
            }
            IconButton(onClick = onLogout) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = stringResource(R.string.logout))
            }
        },
    ) { padding ->
        Column(
            Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val d = dashboard
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    label = stringResource(R.string.received),
                    value = "${d?.receivedQty ?: 0} @ ${Money.formatShort(d?.lastUnitPricePaise ?: 0)}",
                    icon = Icons.Filled.CheckCircle,
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    label = stringResource(R.string.pending_dues),
                    value = Money.formatShort(d?.pendingDuesPaise ?: 0),
                    icon = Icons.Filled.CurrencyRupee,
                    accent = StatusPending,
                    modifier = Modifier.weight(1f),
                )
            }

            Button(
                onClick = { viewModel.confirmLatestDelivery() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null)
                Text("  " + stringResource(R.string.confirm_delivery))
            }
            Button(
                onClick = { onNavigate(Routes.VENDOR_PAY) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) {
                Icon(Icons.Filled.CurrencyRupee, contentDescription = null)
                Text("  " + stringResource(R.string.pay_now))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { onNavigate(Routes.VENDOR_COMPLAINT) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.ReportProblem, contentDescription = null)
                    Text("  " + stringResource(R.string.raise_complaint))
                }
                OutlinedButton(onClick = { onNavigate(Routes.VENDOR_HISTORY) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.History, contentDescription = null)
                    Text("  " + stringResource(R.string.history))
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Demo.SUPPLIER_PHONE}")))
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Filled.Call, contentDescription = null)
                    Text("  " + stringResource(R.string.call))
                }
                OutlinedButton(
                    onClick = {
                        val phone = Demo.SUPPLIER_PHONE.removePrefix("+")
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$phone"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Filled.Chat, contentDescription = null)
                    Text("  " + stringResource(R.string.whatsapp))
                }
            }
        }
    }
}

// ---------------- Pay (UPI) ----------------
@Composable
fun VendorPayScreen(
    vendorId: String,
    onBack: () -> Unit,
    viewModel: VendorViewModel = hiltViewModel(),
) {
    LaunchedEffect(vendorId) { viewModel.setVendor(vendorId) }
    val dashboard by viewModel.dashboard.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var amount by remember { mutableStateOf("") }
    var showConfirm by remember { mutableStateOf(false) }
    val dues = dashboard?.pendingDuesPaise ?: 0L

    TencoScaffold(title = stringResource(R.string.pay_now), onBack = onBack) { padding ->
        // Shared payment launcher used by both "Pay full" and "Pay".
        val startPay: (Double) -> Unit = { amt ->
            if (amt > 0) {
                amount = String.format("%.2f", amt)
                // Ask the backend for a payment intent (Razorpay order + UPI link); if the
                // backend is unreachable, fall back to a locally-built UPI deep link.
                viewModel.createBackendIntent(Money.rupeesToPaise(amt)) { backendLink ->
                    val launched = if (backendLink != null) {
                        UpiPayment.launchLink(context, backendLink)
                    } else {
                        UpiPayment.launch(
                            context = context,
                            payeeVpa = dashboard?.supplierVpa ?: Demo.SUPPLIER_VPA,
                            payeeName = Demo.SUPPLIER_NAME,
                            amountRupees = amt,
                            note = "TENCO dues",
                        )
                    }
                    if (launched) showConfirm = true
                    else Toast.makeText(context, R.string.no_upi_app, Toast.LENGTH_LONG).show()
                }
            }
        }
        Column(
            Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("${stringResource(R.string.remaining_dues)}: ${Money.format(dues)}", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text(stringResource(R.string.amount_hint)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
            // One tap: fills the full amount AND launches payment.
            OutlinedButton(onClick = { startPay(dues / 100.0) }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.pay_full))
            }
            Button(
                onClick = { amount.toDoubleOrNull()?.let { startPay(it) } },
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) {
                Text(stringResource(R.string.pay))
            }
        }
    }

    if (showConfirm) {
        val amt = amount.toDoubleOrNull() ?: 0.0
        val paise = Money.rupeesToPaise(amt)
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text(stringResource(R.string.did_payment_succeed)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.recordPayment(paise, success = true)
                    showConfirm = false
                    Toast.makeText(context, R.string.payment_recorded, Toast.LENGTH_SHORT).show()
                    onBack()
                }) { Text(stringResource(R.string.mark_paid)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.recordPayment(paise, success = false)
                    showConfirm = false
                    onBack()
                }) { Text(stringResource(R.string.mark_failed)) }
            },
        )
    }
}

// ---------------- Complaint ----------------
@Composable
fun VendorComplaintScreen(
    vendorId: String,
    onBack: () -> Unit,
    viewModel: VendorViewModel = hiltViewModel(),
) {
    LaunchedEffect(vendorId) { viewModel.setVendor(vendorId) }
    val context = LocalContext.current
    val reasons = listOf(
        R.string.reason_spoiled,
        R.string.reason_damaged,
        R.string.reason_short,
        R.string.reason_other,
    )
    var selected by remember { mutableStateOf(reasons.first()) }
    var photoUri by remember { mutableStateOf<String?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        photoUri = uri?.toString()
    }

    TencoScaffold(title = stringResource(R.string.raise_complaint), onBack = onBack) { padding ->
        Column(
            Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(stringResource(R.string.complaint_reason), style = MaterialTheme.typography.titleMedium)
            reasons.forEach { res ->
                Row(
                    Modifier.fillMaxWidth().selectable(selected = selected == res, onClick = { selected = res }).padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = selected == res, onClick = { selected = res })
                    Text(stringResource(res), style = MaterialTheme.typography.bodyLarge)
                }
            }
            OutlinedButton(onClick = { picker.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                Text(if (photoUri == null) stringResource(R.string.attach_photo) else "✓ " + stringResource(R.string.attach_photo))
            }
            Button(
                onClick = {
                    viewModel.raiseComplaint(context.getString(selected), photoUri)
                    Toast.makeText(context, R.string.complaint_submitted, Toast.LENGTH_SHORT).show()
                    onBack()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) {
                Text(stringResource(R.string.submit))
            }
        }
    }
}

// ---------------- History ----------------
@Composable
fun VendorHistoryScreen(
    vendorId: String,
    onBack: () -> Unit,
    viewModel: VendorViewModel = hiltViewModel(),
) {
    LaunchedEffect(vendorId) { viewModel.setVendor(vendorId) }
    val payments by viewModel.payments.collectAsStateWithLifecycle()

    TencoScaffold(title = stringResource(R.string.transaction_history), onBack = onBack) { padding ->
        if (payments.isEmpty()) {
            EmptyState(stringResource(R.string.no_data))
        } else {
            LazyColumn(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(payments) { p ->
                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(Money.format(p.amountPaise), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text(dateFmt.format(Date(p.createdAt)), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                            StatusChip(p.status, paymentStatusLabel(p.status))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun paymentStatusLabel(status: String): String = when (status) {
    PaymentStatus.COMPLETED -> stringResource(R.string.status_completed)
    PaymentStatus.FAILED -> stringResource(R.string.status_failed)
    PaymentStatus.PENDING_VERIFICATION -> stringResource(R.string.status_pending_verification)
    else -> stringResource(R.string.status_pending)
}
