package com.example.facedetectorapp.data.api

import com.example.facedetectorapp.data.models.ComparisonResult
import com.example.facedetectorapp.data.models.User
import com.example.facedetectorapp.data.models.UserPhoto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @Multipart
    @POST("users/")
    suspend fun createUser(
        @Part("upaoID") upaoID: RequestBody,
        @Part("nombres") nombres: RequestBody,
        @Part("apellidos") apellidos: RequestBody,
        @Part("correo") correo: RequestBody,
        @Part("requisitoriado") requisitoriado: RequestBody,
        @Part foto: MultipartBody.Part
    ): Response<Map<String, Any>>

    @GET("users/{userId}")
    suspend fun getUser(@Path("userId") userId: Int): Response<User>

    @GET("users/")
    suspend fun getUsers(): Response<List<User>>

    @GET("users/{userId}/photos")
    suspend fun getUserPhotos(@Path("userId") userId: Int): Response<List<UserPhoto>>

    @FormUrlEncoded
    @PUT("users/{userId}")
    suspend fun updateUser(
        @Path("userId") userId: Int,
        @Field("nombres") nombres: String? = null,
        @Field("apellidos") apellidos: String? = null,
        @Field("correo") correo: String? = null,
        @Field("requisitoriado") requisitoriado: Boolean? = null
    ): Response<Map<String, String>>

    @Multipart
    @PUT("users/{userId}")
    suspend fun updateUserWithPhoto(
        @Path("userId") userId: Int,
        @Part("nombres") nombres: RequestBody? = null,
        @Part("apellidos") apellidos: RequestBody? = null,
        @Part("correo") correo: RequestBody? = null,
        @Part("requisitoriado") requisitoriado: RequestBody? = null,
        @Part("conservar") conservar: RequestBody? = null,
        @Part foto: MultipartBody.Part? = null
    ): Response<Map<String, String>>

    @PUT("users/{userId}/change_profile_photo/{photoId}")
    suspend fun changeProfilePhoto(
        @Path("userId") userId: Int,
        @Path("photoId") photoId: Int
    ): Response<Map<String, String>>

    @DELETE("users/{userId}")
    suspend fun deleteUser(@Path("userId") userId: Int): Response<Map<String, String>>

    @DELETE("users/photo/{userId}/{photoId}")
    suspend fun deletePhoto(
        @Path("userId") userId: Int,
        @Path("photoId") photoId: Int
    ): Response<Map<String, String>>

    @Multipart
    @POST("users/photo/")
    suspend fun addPhotoToUser(
        @Part("usuario_id") usuarioId: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<Map<String, Any>>

    @Multipart
    @POST("compare/")
    suspend fun compareImage(@Part file: MultipartBody.Part): Response<ComparisonResult>
}
