package co.huggingface.chat_ui_android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.enableEdgeToEdge
import android.view.KeyEvent
import android.webkit.ValueCallback
import android.webkit.WebChromeClient

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var fileUploadCallback: ValueCallback<Array<Uri>>? = null
    private lateinit var currentPhotoUri: Uri

    companion object {
        private const val FILE_CHOOSER_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        webView = findViewById(R.id.webview)
        webView.settings.setJavaScriptEnabled(true)

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                url?.let {
                    if (it.contains("huggingface.co/chat")
                        || it.contains("huggingface.co/oauth")
                        || it.contains("huggingface.co/login")
                        || it.contains("huggingface.co/authorize")
                    ) {
                        view?.loadUrl(it)
                        return false
                    } else {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                        startActivity(browserIntent)
                    }
                }
                return true
            }
        }

        webView.getSettings()
            .setUserAgentString(BuildConfig.APPLICATION_ID + "/" + BuildConfig.VERSION_NAME + " " + webView.settings.userAgentString);

        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                fileUploadCallback?.onReceiveValue(null)
                fileUploadCallback = filePathCallback

                // Use file picker
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "*/*"
                val chooserIntent = Intent.createChooser(intent, "Choose File")
                startActivityForResult(chooserIntent, FILE_CHOOSER_REQUEST_CODE)

                return true
            }
        }

        webView.loadUrl("https://huggingface.co/chat")
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == FILE_CHOOSER_REQUEST_CODE) {
            if (fileUploadCallback == null) {
                super.onActivityResult(requestCode, resultCode, data)
                return
            }

            val results: Array<Uri>? = when {
                resultCode == RESULT_OK && data?.data != null -> arrayOf(data.data!!)
                resultCode == RESULT_OK -> arrayOf(currentPhotoUri)
                else -> null
            }

            fileUploadCallback?.onReceiveValue(results)
            fileUploadCallback = null
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
