package com.jmb.indyskd.models


import com.google.gson.annotations.SerializedName

data class CreateInvitationResponse(
    @SerializedName("connection_id")
    val connectionId: String,
    @SerializedName("invitation")
    val invitation: Invitation,
    @SerializedName("invitation_url")
    val invitationUrl: String
)