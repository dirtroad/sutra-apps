package com.sutra.app

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private val TAG = "SutraApp"
    private var currentText = ""
    private var currentIndex = 0
    private var textChunks = mutableListOf<String>()
    private var isPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initTTS()
        
        webView = findViewById(R.id.webview)
        setupWebView()
        
        val assetPath = when (packageName) {
            "com.sutra.xinjing" -> "xinjing"
            "com.sutra.jingangjing" -> "jingangjing"
            "com.sutra.dizangjing" -> "dizangjing"
            else -> "xinjing"
        }
        
        webView.loadUrl("file:///android_asset/$assetPath/index.html")
    }

    private fun initTTS() {
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.CHINESE)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.setLanguage(Locale("zh", "CN"))
                }
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        Log.d(TAG, "TTS started: $utteranceId")
                    }
                    override fun onDone(utteranceId: String?) {
                        Log.d(TAG, "TTS done: $utteranceId")
                        playNext()
                    }
                    override fun onError(utteranceId: String?) {
                        Log.e(TAG, "TTS error: $utteranceId")
                        playNext()
                    }
                })
                ttsReady = true
                runOnUiThread {
                    webView.evaluateJavascript("window.ttsReady = true;", null)
                }
            }
        }
    }

    private fun playNext() {
        if (!isPlaying || currentIndex >= textChunks.size) {
            isPlaying = false
            runOnUiThread {
                webView.evaluateJavascript("if(window.onPlaybackEnd) window.onPlaybackEnd();", null)
            }
            return
        }
        
        val text = textChunks[currentIndex]
        currentIndex++
        
        runOnUiThread {
            webView.evaluateJavascript("if(window.onProgress) window.onProgress($currentIndex, ${textChunks.size});", null)
        }
        
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "chunk_$currentIndex")
    }

    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            mediaPlaybackRequiresUserGesture = false
            cacheMode = WebSettings.LOAD_DEFAULT
            setSupportZoom(false)
            builtInZoomControls = false
        }
        
        webView.addJavascriptInterface(WebViewInterface(), "AndroidTTS")
        
        webView.setWebViewClient(object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return false
            }
        })
    }

    inner class WebViewInterface {
        @JavascriptInterface
        fun isReady(): Boolean = ttsReady

        @JavascriptInterface
        fun play(jsonTexts: String, speed: Float) {
            if (!ttsReady) {
                Log.e(TAG, "TTS not ready")
                return
            }
            
            try {
                textChunks.clear()
                val arr = org.json.JSONArray(jsonTexts)
                for (i in 0 until arr.length()) {
                    textChunks.add(arr.getString(i))
                }
                currentIndex = 0
                isPlaying = true
                tts?.setSpeechRate(speed)
                playNext()
            } catch (e: Exception) {
                Log.e(TAG, "Parse error: ${e.message}")
            }
        }

        @JavascriptInterface
        fun stop() {
            isPlaying = false
            tts?.stop()
        }

        @JavascriptInterface
        fun pause() {
            isPlaying = false
            tts?.stop()
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}