package com.watch.cypher

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.CaptureActivity
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory

class PortraitCaptureActivity : CaptureActivity() {
    private lateinit var barcodeView: DecoratedBarcodeView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_scanner)

        barcodeView = findViewById(R.id.barcode_scanner)

        if (!::barcodeView.isInitialized) {
            Log.e("PortraitCaptureActivity", "barcodeView not initialized!")
            return
        }

        val formats = listOf(BarcodeFormat.QR_CODE)
        barcodeView.decoderFactory = DefaultDecoderFactory(formats)
        barcodeView.setStatusText("") // Change this to your desired prompt

        // Initiate scanning when the activity is created
        barcodeView.decodeSingle { result ->
            if (result != null) {
                // If QR code is detected, return result to fragment
                Log.d("PortraitCaptureActivity", "QR Code Scanned: ${result.text}")

                // Return scan result using ScanContract's default handling
                // Inside PortraitCaptureActivity after a successful scan
                val intent = Intent()
                intent.putExtra("SCAN_RESULT", result.text) // Replace with actual scanned data
                setResult(Activity.RESULT_OK, intent)
                finish() // This returns to the fragment
            } else {
                Log.e("PortraitCaptureActivity", "QR Code not detected")
                setResult(Activity.RESULT_CANCELED) // Return canceled result if scan fails
                finish()  // Close activity
            }
        }

        // Add a small delay to start scanning
        Handler(Looper.getMainLooper()).postDelayed({
            barcodeView.resume()  // Resume scanning after the delay
        }, 500)
    }

    override fun onResume() {
        super.onResume()
        if (::barcodeView.isInitialized) {
            barcodeView.resume()  // Ensure scanning continues
        }
    }

    override fun onPause() {
        super.onPause()
        barcodeView.pause()  // Pause scanning when the activity is paused
    }
}

