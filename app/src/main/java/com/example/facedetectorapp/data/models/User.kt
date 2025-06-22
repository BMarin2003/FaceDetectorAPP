package com.example.facedetectorapp.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val id: Int,
    val upaoID: Int,
    val nombres: String,
    val apellidos: String,
    val correo: String,
    val requisitoriado: Boolean,
    val foto: String? = null,
    val KP: String? = null
) : Parcelable

@Parcelize
data class UserPhoto(
    val id: Int,
    val foto_url: String,
    val kp_url: String
) : Parcelable

data class CreateUserRequest(
    val upaoID: Int,
    val nombres: String,
    val apellidos: String,
    val correo: String,
    val requisitoriado: Boolean = false
)

data class CreateUserResponse(
    val message: String,
    val user_id: Int
)

data class ComparisonResponse(
    val message: String,
    val user: User,
    val similarity: Double,
    val confidence: String,
    val SECURITY_ALERT: Boolean? = null,
    val alert_level: String? = null,
    val alert_message: String? = null,
    val security_notification: SecurityNotification? = null,
    val immediate_actions_required: Boolean? = null
)