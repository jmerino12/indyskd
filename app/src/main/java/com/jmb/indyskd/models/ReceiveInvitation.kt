package com.jmb.indyskd.models


import com.google.gson.annotations.SerializedName

data class ReceiveInvitation(
    @SerializedName("accept")
    val accept: String,
    @SerializedName("connection_id")
    val connectionId: String,
    @SerializedName("connection_protocol")
    val connectionProtocol: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("invitation_key")
    val invitationKey: String,
    @SerializedName("invitation_mode")
    val invitationMode: String,
    @SerializedName("invitation_msg_id")
    val invitationMsgId: String,
    @SerializedName("my_did")
    val myDid: String,
    @SerializedName("request_id")
    val requestId: String,
    @SerializedName("rfc23_state")
    val rfc23State: String,
    @SerializedName("routing_state")
    val routingState: String,
    @SerializedName("state")
    val state: String,
    @SerializedName("their_label")
    val theirLabel: String,
    @SerializedName("their_role")
    val theirRole: String,
    @SerializedName("updated_at")
    val updatedAt: String
)