package com.xperiencelabs.arapp

import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.ar.core.Config
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.math.Position
import io.github.sceneview.node.VideoNode

class MainActivity : AppCompatActivity() {

    private lateinit var sceneView: ArSceneView
    private lateinit var placeButton: ExtendedFloatingActionButton
    private lateinit var modelNode: ArModelNode
    private lateinit var videoNode: VideoNode
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var numberInput: EditText
    private lateinit var convertButton: Button
    private lateinit var romanTextView: TextView
    private val currentRomanNodes = mutableListOf<ArModelNode>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // AR görünümü ve bileşenleri başlatma
        sceneView = findViewById<ArSceneView>(R.id.sceneView).apply {
            this.lightEstimationMode = Config.LightEstimationMode.DISABLED
        }

        mediaPlayer = MediaPlayer.create(this, R.raw.ad)
        placeButton = findViewById(R.id.place)
        numberInput = findViewById(R.id.numberInput)
        convertButton = findViewById(R.id.convertButton)
        romanTextView = findViewById(R.id.romanTextView)

        // Sezar modeli başlangıçta sahneye eklenir
        modelNode = ArModelNode(sceneView.engine).apply {
            loadModelGlbAsync(
                glbFileLocation = "models/sezar.glb",
                scaleToUnits = 1f,
                centerOrigin = Position(0f, 0f, -1f)
            ) {
                sceneView.planeRenderer.isVisible = true
            }
        }

        // Televizyon düğümü
        videoNode = VideoNode(sceneView.engine, scaleToUnits = 0.7f, centerOrigin = Position(y = -4f), glbFileLocation = "models/.glb", player = mediaPlayer)

        sceneView.addChild(modelNode)
        modelNode.addChild(videoNode)

        // Convert buton tıklaması
        convertButton.setOnClickListener {
            val number = numberInput.text.toString().toIntOrNull()
            if (number != null) {
                val romanNumeral = toRomanNumeral(number)
                romanTextView.text = romanNumeral
                showNumberInAR(romanNumeral)
            }
        }
    }

    private fun toRomanNumeral(number: Int): String {
        if (number <= 0) throw IllegalArgumentException("Number must be greater than 0")

        val romanNumerals = arrayOf(
            1000 to "M", 900 to "CM", 500 to "D", 400 to "CD",
            100 to "C", 90 to "XC", 50 to "L", 40 to "XL",
            10 to "X", 9 to "IX", 5 to "V", 4 to "IV", 1 to "I"
        )

        var num = number
        val result = StringBuilder()

        for ((value, numeral) in romanNumerals) {
            while (num >= value) {
                result.append(numeral)
                num -= value
            }
        }
        return result.toString()
    }

    private fun showNumberInAR(romanNumeral: String) {
        // Mevcut modelleri kaldır
        sceneView.removeChild(modelNode)
        sceneView.removeChild(videoNode)

        // Eski Roma düğümlerini kaldır
        currentRomanNodes.forEach { sceneView.removeChild(it) }
        currentRomanNodes.clear()

        // Roma rakamları dizisi için başlangıç X pozisyonu
        var currentXPosition = 0f

        romanNumeral.forEach { char ->
            val glbFileLocation = "models/$char.glb" // Her harfe karşılık gelen glb dosyası
            val romanNode = ArModelNode(sceneView.engine).apply {
                loadModelGlbAsync(
                    glbFileLocation = glbFileLocation,
                    scaleToUnits = 0.3f, // Her modelin boyutu
                    centerOrigin = Position(currentXPosition, 0.5f, -1f)
                )
            }

            // Yeni düğümü sahneye ekle ve mevcut listeye kaydet
            sceneView.addChild(romanNode)
            currentRomanNodes.add(romanNode)

            // Harfler arasındaki boşluğu ayarla
            currentXPosition += -2.3f
        }
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }
}
