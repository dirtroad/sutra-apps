package com.sutra.app

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var webView: WebView
    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var currentSpeed = 1.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 初始化TTS
        tts = TextToSpeech(this, this)

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

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.let {
                val result = it.setLanguage(Locale.CHINESE)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    it.setLanguage(Locale("zh", "CN"))
                }
                ttsReady = true
            }
        }
    }

    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            mediaPlaybackRequiresUserGesture = false
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            cacheMode = WebSettings.LOAD_DEFAULT
            setSupportZoom(false)
            builtInZoomControls = false
            displayZoomControls = false
            useWideViewPort = true
            loadWithOverviewMode = true
            blockNetworkImage = false
            blockNetworkLoads = false
        }
        
        // 添加JS接口
        webView.addJavascriptInterface(WebViewInterface(), "AndroidBridge")
        
        webView.setWebViewClient(object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                url?.let { view?.loadUrl(it) }
                return true
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                // 注入原生TTS支持
                view?.evaluateJavascript("""
                    (function() {
                        if (window.speechSynthesis) {
                            window.speechSynthesis.speak = function(utterance) {
                                if (utterance && utterance.text) {
                                    AndroidBridge.speak(utterance.text, utterance.rate || 1.0);
                                    if (utterance.onend) {
                                        setTimeout(utterance.onend, utterance.text.length * 200);
                                    }
                                }
                            };
                            window.speechSynthesis.cancel = function() {
                                AndroidBridge.stop();
                            };
                            window.speechSynthesis.pause = function() {
                                AndroidBridge.stop();
                            };
                        }
                    })();
                """, null)
            }
        })
        
        webView.setWebChromeClient(object : android.webkit.WebChromeClient() {
            override fun onPermissionRequest(request: android.webkit.PermissionRequest?) {
                request?.grant(request.resources)
            }
        })
    }

    inner class WebViewInterface {
        @JavascriptInterface
        fun speak(text: String, rate: Double) {
            currentSpeed = rate.toFloat()
            tts?.let {
                if (ttsReady) {
                    it.stop()
                    it.setSpeechRate(currentSpeed)
                    it.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts1")
                }
            }
        }

        @JavascriptInterface
        fun stop() {
            tts?.stop()
        }

        @JavascriptInterface
        fun pause() {
            tts?.stop()
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}