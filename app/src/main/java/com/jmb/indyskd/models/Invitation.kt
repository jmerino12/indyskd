package com.jmb.indyskd.models


import com.google.gson.annotations.SerializedName

data class Invitation(
    @SerializedName("@id")
    val id: String,
    @SerializedName("label")
    val label: String,
    @SerializedName("recipientKeys")
    val recipientKeys: List<String>,
    @SerializedName("serviceEndpoint")
    val serviceEndpoint: String,
    @SerializedName("@type")
    val type: String
)