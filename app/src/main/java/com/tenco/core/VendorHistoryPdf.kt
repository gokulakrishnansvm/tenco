package com.tenco.core

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Generates a simple, printable vendor-history PDF and shares it (e.g. to WhatsApp) so the
 * supplier can send a statement to the vendor's phone. Files are written to cacheDir/exports
 * which is exposed via the app FileProvider (see @xml/file_paths).
 */
object VendorHistoryPdf {

    data class Row(val time: Long, val title: String, val amount: String)

    private const val PAGE_W = 595 // A4 @ 72dpi
    private const val PAGE_H = 842
    private const val MARGIN = 40f

    fun generateAndShare(
        context: Context,
        vendorName: String,
        vendorPhone: String,
        summary: List<Pair<String, String>>, // label -> value (dues, billed, paid…)
        rows: List<Row>,
    ) {
        val file = generate(context, vendorName, vendorPhone, summary, rows)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val share = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "TENCO – $vendorName statement")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(share, "Share statement").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    private fun generate(
        context: Context,
        vendorName: String,
        vendorPhone: String,
        summary: List<Pair<String, String>>,
        rows: List<Row>,
    ): File {
        val df = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        val doc = PdfDocument()

        val title = Paint().apply { textSize = 22f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); color = android.graphics.Color.rgb(27, 94, 32) }
        val h = Paint().apply { textSize = 13f; typeface = Typeface.DEFAULT_BOLD }
        val body = Paint().apply { textSize = 12f }
        val muted = Paint().apply { textSize = 11f; color = android.graphics.Color.GRAY }
        val line = Paint().apply { color = android.graphics.Color.LTGRAY; strokeWidth = 0.7f }

        var pageNo = 1
        var page = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, pageNo).create())
        var canvas: Canvas = page.canvas
        var y = MARGIN

        fun newPage() {
            doc.finishPage(page)
            pageNo += 1
            page = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, pageNo).create())
            canvas = page.canvas
            y = MARGIN
        }
        fun ensure(space: Float) { if (y + space > PAGE_H - MARGIN) newPage() }

        // Header
        canvas.drawText("TENCO", MARGIN, y + 16, title); y += 30
        canvas.drawText("Vendor statement", MARGIN, y, h); y += 18
        canvas.drawText(vendorName, MARGIN, y, body); y += 15
        if (vendorPhone.isNotBlank()) { canvas.drawText(vendorPhone, MARGIN, y, muted); y += 14 }
        canvas.drawText("Generated ${df.format(Date())}", MARGIN, y, muted); y += 18
        canvas.drawLine(MARGIN, y, PAGE_W - MARGIN, y, line); y += 18

        // Summary
        summary.forEach { (label, value) ->
            ensure(16f)
            canvas.drawText(label, MARGIN, y, body)
            canvas.drawText(value, PAGE_W - MARGIN - body.measureText(value), y, h)
            y += 16
        }
        y += 8
        canvas.drawLine(MARGIN, y, PAGE_W - MARGIN, y, line); y += 18

        // Transactions
        canvas.drawText("Transactions", MARGIN, y, h); y += 18
        if (rows.isEmpty()) {
            canvas.drawText("No transactions.", MARGIN, y, muted); y += 16
        }
        rows.forEach { r ->
            ensure(30f)
            canvas.drawText(r.title, MARGIN, y, body)
            canvas.drawText(r.amount, PAGE_W - MARGIN - h.measureText(r.amount), y, h)
            y += 14
            canvas.drawText(df.format(Date(r.time)), MARGIN, y, muted)
            y += 16
        }

        doc.finishPage(page)

        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val safeName = vendorName.replace(Regex("[^A-Za-z0-9]"), "_").ifBlank { "vendor" }
        val file = File(dir, "TENCO_${safeName}_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
        return file
    }
}
