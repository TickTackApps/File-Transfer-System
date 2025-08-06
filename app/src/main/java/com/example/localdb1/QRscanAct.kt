package com.example.localdb1

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class QRscanAct : AppCompatActivity() {

    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_qrscan)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 101)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun startCamera() {
        val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> =
            ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(findViewById<androidx.camera.view.PreviewView>(R.id.previewView).surfaceProvider)
            }

            val analyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            analyzer.setAnalyzer(cameraExecutor) { imageProxy ->
                processImageProxy(imageProxy)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, analyzer)
            } catch (e: Exception) {
                Log.e("QRScanner", "Use case binding failed", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        val scanner = BarcodeScanning.getClient()
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    val rawValue = barcode.rawValue
                    if (!rawValue.isNullOrEmpty()) {

                        var ip = check(rawValue, "ip")
                        var pass = check(rawValue, "pass")

                        if (pass != "false"){

                            var intent = Intent(this,SettingsActivity::class.java)
                            finish()
                            intent.putExtra("ip",ip)
                            intent.putExtra("pass",pass)
                            startActivity(intent)
                            cameraExecutor.shutdown()


                        }else{

                            Toast.makeText(this, "Invalid QR code", Toast.LENGTH_SHORT).show()

                        }

                        Log.d("QRScanner", "QR Code: $rawValue")
                        imageProxy.close()
                        return@addOnSuccessListener
                    }
                }
                imageProxy.close()
            }
            .addOnFailureListener {
                Log.e("QRScanner", "QR Code scanning failed", it)
                imageProxy.close()
            }
    }


    fun check(raw:String,cred:String):String{
        var count = 0
        val d = arrayOf("0","1","2","3","4","5","6","7","8","9")

        for (i in raw){
            if(count<=11){
                for (j in d){
                    if (i.toString() == j){
                        count++
                        break
                    }
                }
            }else{
                break
            }
        }

        for (j in raw.take(12)){

            if (!j.isDigit()){
                return "false"
            }

        }
        for (i in 1..4) {

            var part = raw.take(i*3).drop((i-1)*3).toString().toInt()

            if(part > 255){
                return "false"
            }

        }

        if(count == 12){
            if(raw.drop(count).length in 4..8){
                if(cred == "ip"){
                    return raw.take(count)
                }else if(cred == "pass") {
                    return raw.drop(count)

                }
            }
        }
        return "false"
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

}