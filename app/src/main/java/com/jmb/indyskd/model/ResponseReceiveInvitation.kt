package com.jmb.indyskd.model


import com.google.gson.annotations.SerializedName

data class ResponseReceiveInvitation(
    @SerializedName("connection_id")
    val connectionId: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("my_did")
    val myDid: String,
    @SerializedName("request_id")
    val requestId: String,
    @SerializedName("state")
    val state: String,
    @SerializedName("updated_at")
    val updatedAt: String
)