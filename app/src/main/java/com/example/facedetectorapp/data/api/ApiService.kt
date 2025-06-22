package com.example.facedetectorapp.data.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @Multipart
    @POST("users/")
    suspend fun createUser(
        @Part("upaoID") upaoID: Int,
        @Part("nombres") nombres: String,
        @Part("apellidos") apellidos: String,
        @Part("correo") correo: String,
        @Part("requisitoriado") requisitoriado: Boolean,
        @Part foto: MultipartBody.Part
    ): Response<Map<String, Any>>

    @GET("users/{user_id}")
    suspend fun getUser(@Path("user_id") userId: Int): Response<User>

    @GET("users/")
    suspend fun listUsers(): Response<List<User>>

    @PUT("users/{user_id}")
    suspend fun updateUser(
        @Path("user_id") userId: Int,
        @Query("nombres") nombres: String?,
        @Query("apellidos") apellidos: String?,
        @Query("correo") correo: String?,
        @Query("requisitoriado") requisitoriado: Boolean?,
        @Query("conservar") conservar: Boolean?,
        @Part foto: MultipartBody.Part?
    ): Response<Map<String, Any>>

    @DELETE("users/{user_id}")
    suspend fun deleteUser(@Path("user_id") userId: Int): Response<Map<String, Any>>

    @Multipart
    @POST("users/photo/")
    suspend fun addPhotoToUser(
        @Part("usuario_id") usuarioId: Int,
        @Part file: MultipartBody.Part
    ): Response<Map<String, Any>>

    @DELETE("users/photo/{user_id}/{photo_id}")
    suspend fun deletePhoto(
        @Path("user_id") userId: Int,
        @Path("photo_id") photoId: Int
    ): Response<Map<String, Any>>

    @Multipart
    @POST("compare/")
    suspend fun compareImage(
        @Part file: MultipartBody.Part
    ): Response<FaceComparisonResponse>
}
