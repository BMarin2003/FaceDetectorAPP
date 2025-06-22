package com.example.facedetectorapp.data.repositories

import okhttp3.MultipartBody

class UserRepository(private val apiService: ApiService) {
    suspend fun createUser(
        upaoID: Int,
        nombres: String,
        apellidos: String,
        correo: String,
        requisitoriado: Boolean,
        foto: MultipartBody.Part
    ) = apiService.createUser(upaoID, nombres, apellidos, correo, requisitoriado, foto)

    suspend fun getUser(userId: Int) = apiService.getUser(userId)

    suspend fun listUsers() = apiService.listUsers()

    suspend fun updateUser(
        userId: Int,
        nombres: String?,
        apellidos: String?,
        correo: String?,
        requisitoriado: Boolean?,
        conservar: Boolean?,
        foto: MultipartBody.Part?
    ) = apiService.updateUser(userId, nombres, apellidos, correo, requisitoriado, conservar, foto)

    suspend fun deleteUser(userId: Int) = apiService.deleteUser(userId)

    suspend fun addPhotoToUser(usuarioId: Int, file: MultipartBody.Part) =
        apiService.addPhotoToUser(usuarioId, file)

    suspend fun deletePhoto(userId: Int, photoId: Int) = apiService.deletePhoto(userId, photoId)

    suspend fun compareImage(file: MultipartBody.Part) = apiService.compareImage(file)
}
