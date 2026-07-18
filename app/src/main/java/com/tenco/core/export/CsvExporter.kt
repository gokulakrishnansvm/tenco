package com.tenco.core.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.tenco.data.local.DeliveryEntity
import com.tenco.data.local.PaymentEntity
import com.tenco.domain.PnlReport
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Builds a CSV report and shares it via a content Uri (FileProvider + ACTION_SEND). */
object CsvExporter {

    private val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
    private fun rupees(paise: Long) = String.format(Locale.US, "%.2f", paise / 100.0)
    private fun csvEscape(s: String) = "\"" + s.replace("\"", "\"\"") + "\""

    fun buildReport(
        pnl: PnlReport,
        deliveries: List<DeliveryEntity>,
        payments: List<PaymentEntity>,
        vendorNames: Map<String, String>,
    ): String = buildString {
        appendLine("TENCO Report")
        appendLine()
        appendLine("Profit & Loss (INR)")
        appendLine("Revenue,${rupees(pnl.revenuePaise)}")
        appendLine("Purchase cost,${rupees(pnl.purchaseCostPaise)}")
        appendLine("Complaint losses,${rupees(pnl.complaintLossesPaise)}")
        appendLine("Net profit,${rupees(pnl.netProfitPaise)}")
        appendLine()
        appendLine("Deliveries")
        appendLine("Date,Vendor,Quantity,UnitPrice(INR),Value(INR),Status")
        deliveries.forEach {
            val name = vendorNames[it.vendorId] ?: it.vendorId
            appendLine("${fmt.format(Date(it.createdAt))},${csvEscape(name)},${it.quantity},${rupees(it.unitPricePaise)},${rupees(it.quantity * it.unitPricePaise)},${it.status}")
        }
        appendLine()
        appendLine("Payments")
        appendLine("Date,Vendor,Amount(INR),Method,Status")
        payments.forEach {
            val name = vendorNames[it.vendorId] ?: it.vendorId
            appendLine("${fmt.format(Date(it.createdAt))},${csvEscape(name)},${rupees(it.amountPaise)},${it.method},${it.status}")
        }
    }

    fun share(context: Context, csv: String) {
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, "tenco-report-${System.currentTimeMillis()}.csv")
        file.writeText(csv)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(send, "Export CSV").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
