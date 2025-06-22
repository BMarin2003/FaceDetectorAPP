package com.example.facedetectorapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

class UserViewModel(private val userRepository: UserRepository) : ViewModel() {
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun createUser(
        upaoID: Int,
        nombres: String,
        apellidos: String,
        correo: String,
        requisitoriado: Boolean,
        foto: MultipartBody.Part
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = userRepository.createUser(upaoID, nombres, apellidos, correo, requisitoriado, foto)
                if (response.isSuccessful) {
                    _errorMessage.value = null
                    fetchUsers()
                } else {
                    _errorMessage.value = response.errorBody()?.string() ?: "Unknown error"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = userRepository.listUsers()
                if (response.isSuccessful) {
                    _users.value = response.body() ?: emptyList()
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = response.errorBody()?.string() ?: "Unknown error"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateUser(
        userId: Int,
        nombres: String?,
        apellidos: String?,
        correo: String?,
        requisitoriado: Boolean?,
        conservar: Boolean?,
        foto: MultipartBody.Part?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = userRepository.updateUser(userId, nombres, apellidos, correo, requisitoriado, conservar, foto)
                if (!response.isSuccessful) {
                    _errorMessage.value = response.errorBody()?.string() ?: "Unknown error"
                } else {
                    fetchUsers()
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteUser(userId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = userRepository.deleteUser(userId)
                if (!response.isSuccessful) {
                    _errorMessage.value = response.errorBody()?.string() ?: "Unknown error"
                } else {
                    fetchUsers()
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addPhotoToUser(usuarioId: Int, file: MultipartBody.Part) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = userRepository.addPhotoToUser(usuarioId, file)
                if (!response.isSuccessful) {
                    _errorMessage.value = response.errorBody()?.string() ?: "Unknown error"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deletePhoto(userId: Int, photoId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = userRepository.deletePhoto(userId, photoId)
                if (!response.isSuccessful) {
                    _errorMessage.value = response.errorBody()?.string() ?: "Unknown error"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun compareImage(file: MultipartBody.Part) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = userRepository.compareImage(file)
                if (!response.isSuccessful) {
                    _errorMessage.value = response.errorBody()?.string() ?: "Unknown error"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
