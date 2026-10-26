package com.example.expiryalert

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Calendar

@Composable
fun rememberEscanearProducto(
    alLeerCodigo: (String, String?, String?) -> Unit,
    alLeerFecha: (String) -> Unit
): Pair<() -> Unit, () -> Unit> {
    val context = LocalContext.current
    val barcodeScanner = remember { BarcodeScanning.getClient() }
    val textRecognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }

    val codigoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        if (bitmap == null) return@rememberLauncherForActivityResult
        barcodeScanner.process(InputImage.fromBitmap(bitmap, 0))
            .addOnSuccessListener { barcodes ->
                val codigo = barcodes.firstOrNull()?.rawValue
                if (codigo.isNullOrBlank()) {
                    Toast.makeText(context, "No se detectó un código. Intenta acercar la cámara.", Toast.LENGTH_LONG).show()
                } else {
                    alLeerCodigo(codigo, null, null)
                    Thread {
                        try {
                            val conexion = URL("https://world.openfoodfacts.org/api/v2/product/$codigo.json?fields=product_name,product_name_es,categories").openConnection() as HttpURLConnection
                            conexion.connectTimeout = 8000
                            conexion.readTimeout = 8000
                            conexion.setRequestProperty("User-Agent", "ExpiryAlert/1.0 (Android)")
                            val respuesta = conexion.inputStream.bufferedReader().use { it.readText() }
                            conexion.disconnect()
                            val json = JSONObject(respuesta)
                            if (json.optInt("status") == 1) {
                                val producto = json.optJSONObject("product")
                                val nombre = producto?.optString("product_name_es")?.takeIf { it.isNotBlank() }
                                    ?: producto?.optString("product_name")?.takeIf { it.isNotBlank() }
                                val categoria = producto?.optString("categories")?.substringBefore(",")?.takeIf { it.isNotBlank() }
                                (context as? android.app.Activity)?.runOnUiThread { alLeerCodigo(codigo, nombre, categoria) }
                            }
                        } catch (_: Exception) {
                            // El código sigue disponible aunque no haya conexión o ficha del producto.
                        }
                    }.start()
                }
            }.addOnFailureListener {
                Toast.makeText(context, "No se pudo leer el código de barras", Toast.LENGTH_SHORT).show()
            }
    }

    val fechaLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        if (bitmap == null) return@rememberLauncherForActivityResult
        textRecognizer.process(InputImage.fromBitmap(bitmap, 0))
            .addOnSuccessListener { resultado ->
                val fecha = detectarFechaCaducidad(resultado.text)
                if (fecha != null) alLeerFecha(fecha)
                else Toast.makeText(context, "No se reconoció una fecha. Puedes seleccionarla manualmente.", Toast.LENGTH_LONG).show()
            }.addOnFailureListener {
                Toast.makeText(context, "No se pudo leer la etiqueta", Toast.LENGTH_SHORT).show()
            }
    }
    return Pair({ codigoLauncher.launch(null) }, { fechaLauncher.launch(null) })
}

private fun detectarFechaCaducidad(texto: String): String? {
    // Formatos numéricos habituales: 24/10/2026, 24-10-26 y 2026-10-24.
    val patrones = listOf(
        Regex("\\b(\\d{1,2})[./-](\\d{1,2})[./-](\\d{2,4})\\b"),
        Regex("\\b(20\\d{2})[./-](\\d{1,2})[./-](\\d{1,2})\\b")
    )
    val fechas = mutableListOf<Calendar>()
    for ((indice, patron) in patrones.withIndex()) {
        for (m in patron.findAll(texto)) {
            val (dia, mes, anio) = if (indice == 0) {
                Triple(m.groupValues[1].toInt(), m.groupValues[2].toInt(), m.groupValues[3].toInt())
            } else Triple(m.groupValues[3].toInt(), m.groupValues[2].toInt(), m.groupValues[1].toInt())
            val anioCompleto = if (anio < 100) anio + 2000 else anio
            val cal = Calendar.getInstance().apply { isLenient = false; set(anioCompleto, mes - 1, dia) }
            try {
                cal.timeInMillis
                if (anioCompleto in 2020..2100) fechas.add(cal)
            } catch (_: Exception) { }
        }
    }
    // Si hay varias fechas, no elegimos arbitrariamente una fecha de fabricación.
    if (fechas.size != 1) return null
    return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(fechas.first().time)
}
