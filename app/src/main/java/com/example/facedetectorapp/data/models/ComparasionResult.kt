package com.example.facedetectorapp.data.models

data class ComparisonResult(
    val message: String,
    val user: User?,
    val similarity: Double,
    val confidence: String,
    val securityAlert: Boolean? = null,
    val alertLevel: String? = null,
    val alertMessage: String? = null,
    val securityNotification: SecurityNotification? = null,
    val immediateActionsRequired: Boolean? = null
)

data class SecurityNotification(
    val alertType: String,
    val timestamp: String,
    val detectedUser: DetectedUser,
    val authorityNotification: AuthorityNotification,
    val recommendedActions: List<String>
)

data class DetectedUser(
    val id: Int,
    val upaoID: Int,
    val nombres: String,
    val apellidos: String,
    val correo: String
)

data class AuthorityNotification(
    val status: String,
    val message: String,
    val referenceCode: String,
    val priority: String
)