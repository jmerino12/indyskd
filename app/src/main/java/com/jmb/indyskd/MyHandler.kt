package com.jmb.indyskd

import android.util.Log
import org.hyperledger.aries.api.Handler
import java.nio.charset.StandardCharsets

class MyHandler : Handler {
        var lastTopic: String? = null
        var lastMessage: String? = null
        val lastNotification: String
            get() = """
            $lastTopic
            $lastMessage
            """.trimIndent()

        override fun handle(topic: String, message: ByteArray) {
            lastTopic = topic
            lastMessage = String(message, StandardCharsets.UTF_8)
            Log.d("HANDLERSTATE", lastTopic!!)
            Log.d("HANDLERSTATE", lastMessage!!)
        }
    }