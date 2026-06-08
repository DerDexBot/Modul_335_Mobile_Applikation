package com.example.workforce_mobile

import android.content.ComponentName
import android.nfc.NfcAdapter
import android.nfc.cardemulation.CardEmulation
import android.util.Log
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {
    private var cardEmulation: CardEmulation? = null
    private var hceComponent: ComponentName? = null

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        configurePreferredHceService()

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, HceSessionStore.CHANNEL)
            .setMethodCallHandler { call, result ->
                when (call.method) {
                    "updateSession" -> {
                        val args = call.arguments as? Map<*, *>
                        if (args == null) {
                            result.error("invalid_args", "Session arguments missing", null)
                            return@setMethodCallHandler
                        }

                        val sessionId = (args["sessionId"] as? Number)?.toLong()
                        val username = args["username"] as? String
                        val action = args["action"] as? String
                        val challenge = args["challenge"] as? String
                        val expiresAt = args["expiresAt"] as? String

                        if (sessionId == null || username.isNullOrBlank() ||
                            action.isNullOrBlank() || challenge.isNullOrBlank()
                        ) {
                            result.error("invalid_session", "Session data incomplete", null)
                            return@setMethodCallHandler
                        }

                        val saved = HceSessionStore.save(
                            context = applicationContext,
                            sessionId = sessionId,
                            username = username.trim(),
                            action = action.trim(),
                            challenge = challenge.trim(),
                            expiresAt = expiresAt.orEmpty()
                        )
                        if (!saved) {
                            result.error("save_failed", "Session could not be saved", null)
                            return@setMethodCallHandler
                        }

                        setPreferredHceService()
                        result.success(null)
                    }

                    "clearSession" -> {
                        HceSessionStore.clear(applicationContext)
                        result.success(null)
                    }

                    else -> result.notImplemented()
                }
            }
    }

    override fun onResume() {
        super.onResume()
        setPreferredHceService()
    }

    override fun onPause() {
        unsetPreferredHceService()
        super.onPause()
    }

    private fun configurePreferredHceService() {
        val adapter = NfcAdapter.getDefaultAdapter(this)
        if (adapter == null) {
            Log.d(TAG, "NFC adapter not available")
            return
        }

        cardEmulation = CardEmulation.getInstance(adapter)
        hceComponent = ComponentName(this, HceAuthService::class.java)
    }

    private fun setPreferredHceService() {
        val emulation = cardEmulation ?: return
        val component = hceComponent ?: return
        val preferred = emulation.setPreferredService(this, component)
        Log.d(TAG, "Preferred HCE service set=$preferred")
    }

    private fun unsetPreferredHceService() {
        val emulation = cardEmulation ?: return
        val unset = emulation.unsetPreferredService(this)
        Log.d(TAG, "Preferred HCE service unset=$unset")
    }

    companion object {
        private const val TAG = "FlipperAuthMain"
    }
}
