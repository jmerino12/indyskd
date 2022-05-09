package com.jmb.indyskd.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
fun decodeBase64(encodedString: String): String {
    val decodedBytes =  Base64.getDecoder().decode(encodedString)
    return String(decodedBytes)
}

fun splitCI(data: String): String {
    return  data.split("c_i ")[0]
}