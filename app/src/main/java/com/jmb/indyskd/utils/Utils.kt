package com.jmb.indyskd.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.util.*


fun decodeBase64(encodedString: String): String {
    val decodedBytes =  Base64.getDecoder().decode(encodedString)
    return String(decodedBytes)
}

fun splitCI(data: String): String {
    val splitList = data.split("c_i=")
    return splitList[1]
}