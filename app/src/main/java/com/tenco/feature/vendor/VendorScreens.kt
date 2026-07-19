package com.tenco.feature.vendor

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun VendorDashboardScreen(
    vendorId: String,
    onNavigate: (String) -> Unit,
    onChangeLanguage: () -> Unit,
    onLogout: () -> Unit,
    viewModel: VendorViewModel = hiltViewModel(),
) {
    LaunchedEffect(vendorId) { viewModel.setVendor(vendorId) }
    var tab by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(0) }
    androidx.compose.material3.Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { VendorBottomBar(tab) { tab = it } },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding())) {
            when (tab) {
                0 -> VendorHomeTab(onChangeLanguage, onLogout, { tab = it }, viewModel)
                1 -> VendorPayScreen(vendorId, onBack = { tab = 0 })
                2 -> VendorHistoryScreen(vendorId, onBack = { tab = 0 })
                3 -> VendorComplaintScreen(vendorId, onBack = { tab = 0 })
                else -> com.tenco.feature.profile.ProfileScreen(
                    onChangeLanguage = onChangeLanguage,
                    onNotifications = { onNavigate(Routes.NOTIFICATIONS) },
                    onLogout = onLogout,
                )
            }
        }
    }
}

@Composable
private fun VendorHomeTab(
    onChangeLanguage: () -> Unit,
    onLogout: () -> Unit,
    onTab: (Int) -> Unit,
    viewModel: VendorViewModel,
) {
    val dashboard by viewModel.dashboard.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val d = dashboard
    val langTag = androidx.compose.ui.platform.LocalConfiguration.current.locales[0].language
    val receivedLabel = stringResource(R.string.received)
    val coconutsLabel = stringResource(R.string.coconuts)
    val pendingLabel = stringResource(R.string.pending_dues)

    Column(
        Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Namaste 👋", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(d?.vendorName?.ifBlank { stringResource(R.string.role_vendor) } ?: stringResource(R.string.role_vendor),
                    style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            }
            Row {
                IconButton(onClick = {
                    val rupees = (d?.pendingDuesPaise ?: 0L) / 100
                    viewModel.speak(langTag, "$receivedLabel ${d?.receivedQty ?: 0} $coconutsLabel. $pendingLabel ₹$rupees")
                }) { Icon(Icons.Filled.VolumeUp, contentDescription = stringResource(R.string.speak)) }
                IconButton(onClick = onChangeLanguage) { Icon(Icons.Filled.Translate, contentDescription = stringResource(R.string.change_language)) }
                IconButton(onClick = onLogout) { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = stringResource(R.string.logout)) }
            }
        }

        com.tenco.ui.components.HeroEarningsCard(
            label = pendingLabel,
            paise = d?.pendingDuesPaise ?: 0L,
            caption = "$receivedLabel ${d?.receivedQty ?: 0} @ ${Money.formatShort(d?.lastUnitPricePaise ?: 0L)}",
        )

        Box(
            Modifier.fillMaxWidth().height(66.dp).clip(MaterialTheme.shapes.extraLarge)
                .background(com.tenco.ui.theme.Gradients.lime)
                .clickable { onTab(1) },
            contentAlignment = Alignment.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.CurrencyRupee, contentDescription = null, tint = Color.White)
                Text("  " + stringResource(R.string.pay_now), style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            com.tenco.ui.components.QuickActionTile(Icons.Filled.CheckCircle, stringResource(R.string.confirm_delivery), com.tenco.ui.theme.TileGreen, { viewModel.confirmLatestDelivery() }, Modifier.weight(1f))
            com.tenco.ui.components.QuickActionTile(Icons.Filled.ReportProblem, stringResource(R.string.raise_complaint), com.tenco.ui.theme.TileRed, { onTab(3) }, Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            com.tenco.ui.components.QuickActionTile(Icons.Filled.History, stringResource(R.string.history), com.tenco.ui.theme.TileBlue, { onTab(2) }, Modifier.weight(1f))
            com.tenco.ui.components.QuickActionTile(Icons.Filled.Chat, stringResource(R.string.contact_supplier), com.tenco.ui.theme.TileOrange, {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/${Demo.SUPPLIER_PHONE.removePrefix("+")}")))
            }, Modifier.weight(1f))
        }
    }
}

// ---------------- Pay (UPI) ----------------
@Composable
private fun VendorBottomBar(selected: Int, onSelect: (Int) -> Unit) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp) {
        val c = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
        )
        NavigationBarItem(selected = selected == 0, onClick = { onSelect(0) }, icon = { Icon(Icons.Rounded.Home, null) }, label = { Text("Home") }, colors = c)
        NavigationBarItem(selected = selected == 1, onClick = { onSelect(1) }, icon = { Icon(Icons.Filled.CurrencyRupee, null) }, label = { Text(stringResource(R.string.pay)) }, colors = c)
        NavigationBarItem(selected = selected == 2, onClick = { onSelect(2) }, icon = { Icon(Icons.Filled.History, null) }, label = { Text(stringResource(R.string.history)) }, colors = c)
        NavigationBarItem(selected = selected == 3, onClick = { onSelect(3) }, icon = { Icon(Icons.Filled.ReportProblem, null) }, label = { Text(stringResource(R.string.raise_complaint)) }, colors = c)
        NavigationBarItem(selected = selected == 4, onClick = { onSelect(4) }, icon = { Icon(Icons.Rounded.Person, null) }, label = { Text(stringResource(R.string.menu_profile)) }, colors = c)
    }
}

@Composable
fun VendorPayScreen(
    vendorId: String,
    onBack: (() -> Unit)? = null,
    viewModel: VendorViewModel = hiltViewModel(),
) {
    LaunchedEffect(vendorId) { viewModel.setVendor(vendorId) }
    val dashboard by viewModel.dashboard.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var amount by remember { mutableStateOf("") }
    var showConfirm by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<Boolean?>(null) }
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
            run {
                val qrAmt = amount.toDoubleOrNull() ?: (dues / 100.0)
                val upiStr = com.tenco.core.UpiPayment.buildUri(
                    dashboard?.supplierVpa ?: Demo.SUPPLIER_VPA, Demo.SUPPLIER_NAME, qrAmt, "TENCO dues",
                ).toString()
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    com.tenco.ui.components.QrCode(upiStr)
                    Text(stringResource(R.string.scan_to_pay), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
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
                    result = true
                }) { Text(stringResource(R.string.mark_paid)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.recordPayment(paise, success = false)
                    showConfirm = false
                    result = false
                }) { Text(stringResource(R.string.mark_failed)) }
            },
        )
    }

    result?.let { ok ->
        val paise = Money.rupeesToPaise(amount.toDoubleOrNull() ?: 0.0)
        com.tenco.ui.components.PaymentResultOverlay(
            success = ok,
            amountText = Money.format(paise),
            title = if (ok) stringResource(R.string.payment_recorded) else stringResource(R.string.status_failed),
            onDone = { onBack?.invoke() },
        )
    }
}

// ---------------- Complaint ----------------
@Composable
fun VendorComplaintScreen(
    vendorId: String,
    onBack: (() -> Unit)? = null,
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
                    onBack?.invoke()
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
    onBack: (() -> Unit)? = null,
    viewModel: VendorViewModel = hiltViewModel(),
) {
    LaunchedEffect(vendorId) { viewModel.setVendor(vendorId) }
    val payments by viewModel.payments.collectAsStateWithLifecycle()
    val complaints by viewModel.complaints.collectAsStateWithLifecycle()

    TencoScaffold(title = stringResource(R.string.transaction_history), onBack = onBack) { padding ->
        if (payments.isEmpty() && complaints.isEmpty()) {
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
                if (complaints.isNotEmpty()) {
                    item {
                        Text(
                            stringResource(R.string.my_complaints),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                    items(complaints) { c ->
                        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text(c.reason, fontWeight = FontWeight.SemiBold)
                                    Text(dateFmt.format(Date(c.createdAt)), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                                StatusChip(c.status, complaintStatusLabel(c.status))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun complaintStatusLabel(status: String): String = when (status) {
    com.tenco.domain.ComplaintStatus.OPEN -> stringResource(R.string.status_open)
    com.tenco.domain.ComplaintStatus.UNDER_REVIEW -> stringResource(R.string.status_under_review)
    com.tenco.domain.ComplaintStatus.RESOLVED -> stringResource(R.string.status_resolved)
    com.tenco.domain.ComplaintStatus.REJECTED -> stringResource(R.string.status_rejected)
    else -> stringResource(R.string.status_open)
}

@Composable
private fun paymentStatusLabel(status: String): String = when (status) {
    PaymentStatus.COMPLETED -> stringResource(R.string.status_completed)
    PaymentStatus.FAILED -> stringResource(R.string.status_failed)
    PaymentStatus.PENDING_VERIFICATION -> stringResource(R.string.status_pending_verification)
    else -> stringResource(R.string.status_pending)
}
