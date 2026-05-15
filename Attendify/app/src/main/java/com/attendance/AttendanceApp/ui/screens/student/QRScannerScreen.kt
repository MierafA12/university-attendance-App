package com.attendance.attendanceapp.ui.screens.student

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

@Composable
fun QRScannerScreen(
    viewModel: StudentViewModel,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val schoolColor = Color(0xFF006064)
    val uiState by viewModel.uiState
    var showManualEntry by remember { mutableStateOf(false) }
    var manualCode by remember { mutableStateOf("") }
    var isTorchOn by remember { mutableStateOf(false) }
    
    // Permission Handling
    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasCameraPermission = it }
    
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) launcher.launch(Manifest.permission.CAMERA)
    }

    // Single-trigger scan logic
    var processingScan by remember { mutableStateOf(false) }
    val onCodeScanned: (String) -> Unit = { code ->
        if (!processingScan && uiState !is StudentUiState.Loading) {
            processingScan = true
            viewModel.markAttendance(code)
        }
    }

    // Feedback & Auto-close
    val feedbackColor = when (uiState) {
        is StudentUiState.Success -> Color.Green
        is StudentUiState.Error -> Color.Red
        is StudentUiState.Loading -> schoolColor
        else -> Color.White.copy(alpha = 0.5f)
    }

    LaunchedEffect(uiState) {
        if (uiState is StudentUiState.Success) {
            vibrate(context, 200)
            onClose()
        } else if (uiState is StudentUiState.Error) {
            vibrate(context, 100)
            processingScan = false // Allow retry on error
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (hasCameraPermission) {
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                uiState = uiState,
                isTorchOn = isTorchOn,
                onBarcodeScanned = onCodeScanned
            )
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Camera permission required", color = Color.White)
            }
        }

        // Overlay Frame
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.size(280.dp).border(3.dp, feedbackColor, RoundedCornerShape(24.dp)))
            if (uiState is StudentUiState.Loading) {
                Surface(color = Color.Black.copy(alpha = 0.7f), shape = RoundedCornerShape(16.dp), modifier = Modifier.padding(top = 320.dp)) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Verifying...", color = Color.White)
                    }
                }
            }
        }

        // Controls
        Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            IconButton(onClick = onClose, modifier = Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape)) {
                Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
            }
            IconButton(onClick = { isTorchOn = !isTorchOn }, modifier = Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape)) {
                Icon(if (isTorchOn) Icons.Default.FlashlightOff else Icons.Default.FlashlightOn, contentDescription = null, tint = Color.White)
            }
        }

        // Bottom Info
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)).background(Color.White).padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = schoolColor, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Point camera at the QR code", fontWeight = FontWeight.Bold)
            if (uiState is StudentUiState.Error) {
                Text((uiState as StudentUiState.Error).message, color = Color.Red, fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp))
            }
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedButton(onClick = { showManualEntry = true }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) {
                Text("Enter Code Manually", color = Color.Black)
            }
        }

        if (showManualEntry) {
            AlertDialog(
                onDismissRequest = { showManualEntry = false },
                title = { Text("Manual Entry") },
                text = { OutlinedTextField(value = manualCode, onValueChange = { manualCode = it }, label = { Text("Session Code") }, singleLine = true) },
                confirmButton = { Button(onClick = { onCodeScanned(manualCode); showManualEntry = false }) { Text("Submit") } },
                dismissButton = { TextButton(onClick = { showManualEntry = false }) { Text("Cancel") } }
            )
        }
    }
}

@Suppress("DEPRECATION")
private fun vibrate(context: android.content.Context, duration: Long) {
    try {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? Vibrator
        }

        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                vibrator.vibrate(duration)
            }
        }
    } catch (e: Exception) {
        android.util.Log.e("QRScanner", "Vibration failed", e)
    }
}

@Composable
fun CameraPreview(
    modifier: Modifier,
    uiState: StudentUiState,
    isTorchOn: Boolean,
    onBarcodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var camera by remember { mutableStateOf<androidx.camera.core.Camera?>(null) }
    
    LaunchedEffect(isTorchOn) {
        camera?.cameraControl?.enableTorch(isTorchOn)
    }
    
    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val executor = ContextCompat.getMainExecutor(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().apply { setSurfaceProvider(previewView.surfaceProvider) }
                val scanner = BarcodeScanning.getClient(BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build())
                val analysis = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
                
                analysis.setAnalyzer(executor) { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage != null && uiState !is StudentUiState.Loading) {
                        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                        scanner.process(image)
                            .addOnSuccessListener { barcodes ->
                                barcodes.firstOrNull()?.rawValue?.let { onBarcodeScanned(it) }
                            }
                            .addOnCompleteListener { imageProxy.close() }
                    } else {
                        imageProxy.close()
                    }
                }
                
                try {
                    cameraProvider.unbindAll()
                    camera = cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
                } catch (e: Exception) {}
            }, executor)
            previewView
        },
        modifier = modifier
    )
}
