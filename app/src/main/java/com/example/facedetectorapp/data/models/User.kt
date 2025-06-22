package com.example.facedetectorapp.data.models

data class User(
    val id: Int,
    val upaoID: Int,
    val nombres: String,
    val apellidos: String,
    val correo: String,
    val requisitoriado: Boolean,
    val foto: String,
    val KP: String
)
