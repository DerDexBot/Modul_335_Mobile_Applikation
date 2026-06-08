package com.example.workforce_mobile

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log
import java.nio.charset.StandardCharsets

class HceAuthService : HostApduService() {
    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        Log.d(TAG, "processCommandApdu called")
        Log.d(TAG, "APDU received: ${commandApdu?.toHexString() ?: "null"}")

        val selectAid = commandApdu?.isSelectAid() == true
        Log.d(TAG, "SELECT AID recognized: $selectAid")

        if (!selectAid) {
            return STATUS_FILE_NOT_FOUND
        }

        val payload = HceSessionStore.payload(applicationContext)
        Log.d(TAG, "Payload sent: $payload")
        return payload.toByteArray(StandardCharsets.UTF_8) + STATUS_OK
    }

    override fun onDeactivated(reason: Int) {
        Log.d(TAG, "HCE deactivated, reason=$reason")
    }

    private fun ByteArray.isSelectAid(): Boolean {
        if (size != SELECT_APDU_WITH_LE.size && size != SELECT_APDU_WITHOUT_LE.size) {
            return false
        }

        return contentEquals(SELECT_APDU_WITH_LE) || contentEquals(SELECT_APDU_WITHOUT_LE)
    }

    private fun ByteArray.toHexString(): String = joinToString(separator = "") { byte ->
        "%02X".format(byte)
    }

    companion object {
        private const val TAG = "FlipperAuthHce"
        private const val FLIPPER_AUTH_AID_HEX = "F0464C495050455241555448"

        private val SELECT_APDU_WITH_LE = hexToBytes("00A404000C${FLIPPER_AUTH_AID_HEX}00")
        private val SELECT_APDU_WITHOUT_LE = hexToBytes("00A404000C$FLIPPER_AUTH_AID_HEX")
        private val STATUS_OK = byteArrayOf(0x90.toByte(), 0x00)
        private val STATUS_FILE_NOT_FOUND = byteArrayOf(0x6A.toByte(), 0x82.toByte())

        private fun hexToBytes(value: String): ByteArray {
            val result = ByteArray(value.length / 2)
            for (index in result.indices) {
                val offset = index * 2
                result[index] = value.substring(offset, offset + 2).toInt(16).toByte()
            }
            return result
        }
    }
}
