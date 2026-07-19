package com.tenco.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

/** Renders [content] as a QR code with a subtle scale/fade-in animation. */
@Composable
fun QrCode(content: String, modifier: Modifier = Modifier, size: Dp = 200.dp) {
    val painter = remember(content) {
        runCatching {
            val px = 512
            val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, px, px)
            val bmp = createBitmap(px, px)
            for (x in 0 until px) for (y in 0 until px) {
                bmp.setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
            BitmapPainter(bmp.asImageBitmap())
        }.getOrNull()
    }
    val anim by animateFloatAsState(targetValue = 1f, animationSpec = tween(500), label = "qr")
    if (painter != null) {
        Image(
            painter = painter,
            contentDescription = "QR code",
            modifier = modifier
                .size(size)
                .scale(0.85f + 0.15f * anim)
                .alpha(anim)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(10.dp),
        )
    }
}
