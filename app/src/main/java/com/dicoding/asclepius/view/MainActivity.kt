package com.dicoding.asclepius.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper
import org.tensorflow.lite.task.vision.classifier.Classifications

class MainActivity : AppCompatActivity(), ImageClassifierHelper.ClassifierListener {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: ViewModel by viewModels()
    private lateinit var imageClassifierHelper: ImageClassifierHelper

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            viewModel.setImage(uri)
        } else {
            showToast("Tidak ada gambar yang dipilih.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imageClassifierHelper = ImageClassifierHelper(
            context = this,
            classifierListener = this
        )

        viewModel.currentimage.observe(this) { uri ->
            if (uri != null) {
                showImage(uri)
            }
        }

        binding.galleryButton.setOnClickListener {
            startGallery()
        }

        binding.analyzeButton.setOnClickListener {
            val uri = viewModel.currentimage.value
            if (uri != null) {
                analyzeImage(uri)
            } else {
                showToast("Pilih gambar terlebih dahulu.")
            }
        }
    }

    private fun startGallery() {
        galleryLauncher.launch("image/*")
    }

    private fun showImage(uri: Uri) {
        binding.previewImageView.setImageURI(uri)
    }

    private fun analyzeImage(uri: Uri) {
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            imageClassifierHelper.classifyImage(bitmap)
        } catch (e: Exception) {
            showToast("Gagal memproses gambar.")
        }
    }

    private fun moveToResult(bestCategory: String, confidence: Float) {
        val confidenceScore = String.format("%.2f", confidence * 100)

        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra("RESULT_TEXT", "Hasil Analisis: $bestCategory")
            putExtra("CONFIDENCE_SCORE", confidenceScore)
            viewModel.currentimage.value?.let {
                putExtra("IMAGE_URI", it)
            }
        }
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onError(error: String) {
        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra("RESULT_TEXT", "Terjadi kesalahan selama analisis.")
            putExtra("CONFIDENCE_SCORE", "N/A")
            putExtra("IMAGE_URI", viewModel.currentimage.value)
        }
        startActivity(intent)
    }

    override fun onResults(results: List<Classifications>?, inferenceTime: Long) {
        if (results.isNullOrEmpty()) {
            showToast("Tidak ada hasil klasifikasi.")
            return
        }

        val firstResult = results[0]
        val categories = firstResult.categories

        val debugResult = StringBuilder("Hasil Klasifikasi:\n")
        categories.forEach { category ->
            debugResult.append("${category.label}: ${String.format("%.2f", category.score * 100)}%\n")
        }
        Log.d("ClassificationResult", debugResult.toString())

        val bestCategory = categories.maxByOrNull { it.score }
        if (bestCategory != null) {
            moveToResult(bestCategory.label, bestCategory.score)
        } else {
            showToast("Gagal mendapatkan hasil prediksi.")
        }
    }
}