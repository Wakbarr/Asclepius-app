package com.dicoding.asclepius.view

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val resultText = intent.getStringExtra("RESULT_TEXT") ?: "Hasil tidak tersedia."
        val confidenceScore = intent.getStringExtra("CONFIDENCE_SCORE") ?: "Skor kepercayaan tidak tersedia."
        val imageUri = intent.getParcelableExtra<Uri>("IMAGE_URI")

        binding.resultText.text = resultText
        binding.confidenceScore.text = "Confidence Score: $confidenceScore%"

        imageUri?.let {
            binding.resultImage.setImageURI(it)
        } ?: run {
            binding.resultImage.setImageResource(R.drawable.ic_place_holder)
        }
    }
}