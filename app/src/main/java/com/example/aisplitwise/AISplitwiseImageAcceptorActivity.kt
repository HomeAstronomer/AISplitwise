package com.example.aisplitwise

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.aisplitwise.ui.theme.AISplitwiseTheme
import com.example.aisplitwise.utils.ifNullOrEmpty
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class AISplitwiseImageAcceptorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val imageUri =  intent.extras?.get(Intent.EXTRA_STREAM).toString()

        setContent {
            AISplitwiseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black.copy(alpha = 0.8f)
                ) {
                     ImageAcceptorScreen(imageUri)
                    LoadImageAndRecognizeText(imageUri){}
                }
            }
        }
    }
}

@Composable
fun ImageAcceptorScreen(imageUri:String) {
    val context = LocalContext.current
    val loader = ImageLoader(context)
    val request = ImageRequest.Builder(context)
        .data(imageUri)
        .build()

    val painter = rememberAsyncImagePainter(
        model =request
    )
    var recognizedText by remember { mutableStateOf("") }

    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(request) {
        val result = (loader.execute(request) as? SuccessResult)?.drawable
        imageBitmap = drawableToBitmap(result)
        imageBitmap?.let { bitmap ->
            processImage(bitmap,onTextRecognized= { text ->
                recognizedText = text
            },
                error = {})
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(16.dp)
        ) {
            if (imageUri != null) {
                Image(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier.size(250.dp)
                )
            } else {
                Text(text = "No Image Received")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = { /* Handle image accept action */ }) {
                Text(text = "Accept Image")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(text =recognizedText.ifNullOrEmpty { "No text recognised" },




                color = Color.White)
        }
    }
}

private fun processImage(bitmap: Bitmap, onTextRecognized: (String) -> Unit,error:(Exception)->Unit) {
    val image = InputImage.fromBitmap(bitmap, 0)

    // Initialize Text Recognizer
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    // Process the image
    recognizer.process(image)
        .addOnSuccessListener { visionText ->
            val recognizedText = visionText.text
            Log.d("OCR", "Text Recognized: $recognizedText")
            onTextRecognized(recognizedText)
        }
        .addOnFailureListener { e ->
            Log.e("OCR", "Text recognition failed", e)
            error.invoke(e)
        }
}

@Composable
fun LoadImageAndRecognizeText(imageUri: String, onTextRecognized: (String) -> Unit) {
    val context = LocalContext.current
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var recognizedText by remember { mutableStateOf("") }

    // Load the image using Coil's ImageLoader
    LaunchedEffect(imageUri) {
        val loader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(imageUri)
            .build()

        val result = (loader.execute(request) as? SuccessResult)?.drawable
        imageBitmap = drawableToBitmap(result)
    }

    // Process the image once the bitmap is available
    imageBitmap?.let { bitmap ->
        processImage(bitmap,onTextRecognized= { text ->
            recognizedText = text
            onTextRecognized(text)
        },
            error = {})
    }
}

// Function to convert Drawable to Bitmap
private fun drawableToBitmap(drawable: Drawable?): Bitmap? {
    return if (drawable is BitmapDrawable) {
        drawable.bitmap
    } else {
        null
    }
}