package com.jmb.indyskd.model


import com.google.gson.annotations.SerializedName

data class InvitationOutBand(
    @SerializedName("connection_id")
    val connectionId: String
)