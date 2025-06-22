package com.example.facedetectorapp.ui.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.facedetectorapp.data.api.RetrofitClient
import com.example.facedetectorapp.data.models.User
import com.example.facedetectorapp.data.models.UserPhoto
import com.example.facedetectorapp.ui.theme.*
import com.example.facedetectorapp.utils.createMultipartFromUri
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun UserDetailScreen(navController: NavController, userId: Int) {
    var user by remember { mutableStateOf<User?>(null) }
    var userPhotos by remember { mutableStateOf<List<UserPhoto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showPhotoOptions by remember { mutableStateOf(false) }
    var showDeleteUserDialog by remember { mutableStateOf(false) }
    var showDeletePhotoDialog by remember { mutableStateOf<Int?>(null) }
    var showChangeProfileDialog by remember { mutableStateOf<Int?>(null) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Camera permission
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    // Create photo file
    fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = File(context.filesDir, "images")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        return File(storageDir, "IMG_${timeStamp}.jpg")
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoUri != null) {
            // Upload the photo
            uploadPhoto(photoUri!!, context, scope, userId,
                onSuccess = { loadUserPhotos(scope, userId) { photos -> userPhotos = photos } },
                onError = { error -> errorMessage = error }
            )
        }
        showPhotoOptions = false
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            uploadPhoto(it, context, scope, userId,
                onSuccess = { loadUserPhotos(scope, userId) { photos -> userPhotos = photos } },
                onError = { error -> errorMessage = error }
            )
        }
        showPhotoOptions = false
    }

    // Load user data on start
    LaunchedEffect(userId) {
        loadUserData(scope, userId,
            onUserLoaded = { loadedUser -> user = loadedUser },
            onPhotosLoaded = { photos -> userPhotos = photos },
            onError = { error -> errorMessage = error }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Detalles del Usuario",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showPhotoOptions = true },
                containerColor = PrimaryBlue
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Agregar foto",
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            user?.let { userInfo ->
                // User profile section
                UserProfileSection(userInfo)

                // Photos section
                PhotosSection(
                    userPhotos = userPhotos,
                    onChangeProfilePhoto = { photoId ->
                        showChangeProfileDialog = photoId
                    },
                    onDeletePhoto = { photoId ->
                        showDeletePhotoDialog = photoId
                    }
                )

                // Action buttons
                ActionButtonsSection(
                    onEdit = { navController.navigate("edit_user/${userInfo.id}") },
                    onDelete = { showDeleteUserDialog = true }
                )
            } ?: run {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                }
            }
        }
    }

    // Photo options dialog
    if (showPhotoOptions) {
        AlertDialog(
            onDismissRequest = { showPhotoOptions = false },
            title = { Text("Agregar Foto") },
            text = { Text("¿Cómo deseas agregar la foto?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (cameraPermissionState.status.isGranted) {
                            val imageFile = createImageFile()
                            photoUri = FileProvider.getUriForFile(
                                context,
                                "com.example.facedetectorapp.provider",
                                imageFile
                            )
                            cameraLauncher.launch(photoUri!!)
                        } else {
                            cameraPermissionState.launchPermissionRequest()
                        }
                    }
                ) {
                    Text("Cámara")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                ) {
                    Text("Galería")
                }
            }
        )
    }

    // Delete user confirmation dialog
    if (showDeleteUserDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteUserDialog = false },
            title = { Text("Confirmar Eliminación") },
            text = { Text("¿Estás seguro de que deseas eliminar este usuario? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteUser(scope, userId,
                            onSuccess = {
                                showDeleteUserDialog = false
                                navController.popBackStack()
                            },
                            onError = { error ->
                                errorMessage = error
                                showDeleteUserDialog = false
                            }
                        )
                    }
                ) {
                    Text("Eliminar", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteUserDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Delete photo confirmation dialog
    showDeletePhotoDialog?.let { photoId ->
        AlertDialog(
            onDismissRequest = { showDeletePhotoDialog = null },
            title = { Text("Confirmar Eliminación") },
            text = { Text("¿Estás seguro de que deseas eliminar esta foto?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        deletePhoto(scope, userId, photoId,
                            onSuccess = {
                                loadUserPhotos(scope, userId) { photos -> userPhotos = photos }
                                showDeletePhotoDialog = null
                            },
                            onError = { error ->
                                errorMessage = error
                                showDeletePhotoDialog = null
                            }
                        )
                    }
                ) {
                    Text("Eliminar", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeletePhotoDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Change profile photo confirmation dialog
    showChangeProfileDialog?.let { photoId ->
        AlertDialog(
            onDismissRequest = { showChangeProfileDialog = null },
            title = { Text("Cambiar Foto de Perfil") },
            text = { Text("¿Deseas establecer esta foto como foto de perfil?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        changeProfilePhoto(scope, userId, photoId,
                            onSuccess = {
                                loadUserData(scope, userId,
                                    onUserLoaded = { loadedUser -> user = loadedUser },
                                    onPhotosLoaded = { photos -> userPhotos = photos },
                                    onError = { error -> errorMessage = error }
                                )
                                showChangeProfileDialog = null
                            },
                            onError = { error ->
                                errorMessage = error
                                showChangeProfileDialog = null
                            }
                        )
                    }
                ) {
                    Text("Cambiar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangeProfileDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Error message
    errorMessage?.let { message ->
        LaunchedEffect(message) {
            // TODO: Show snackbar
            errorMessage = null
        }
    }
}

@Composable
fun UserProfileSection(user: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile photo
            if (user.foto != null) {
                AsyncImage(
                    model = user.foto,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(PrimaryBlue.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Sin foto",
                        modifier = Modifier.size(60.dp),
                        tint = PrimaryBlue
                    )
                }
            }

            // User info
            Text(
                text = "${user.nombres} ${user.apellidos}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "ID UPAO: ${user.upaoID}",
                style = MaterialTheme.typography.bodyLarge,
                color = OnSurfaceLight.copy(alpha = 0.7f)
            )

            Text(
                text = user.correo,
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceLight.copy(alpha = 0.7f)
            )

            if (user.requisitoriado) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.1f))
                ) {
                    Text(
                        text = "REQUISITORIADO",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = ErrorRed,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@Composable
fun PhotosSection(
    userPhotos: List<UserPhoto>,
    onChangeProfilePhoto: (Int) -> Unit,
    onDeletePhoto: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Fotos del Usuario (${userPhotos.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (userPhotos.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(userPhotos) { photo ->
                        PhotoItem(
                            photo = photo,
                            onChangeProfilePhoto = { onChangeProfilePhoto(photo.id) },
                            onDeletePhoto = { onDeletePhoto(photo.id) }
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay fotos adicionales",
                        color = OnSurfaceLight.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun PhotoItem(
    photo: UserPhoto,
    onChangeProfilePhoto: () -> Unit,
    onDeletePhoto: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Box {
        AsyncImage(
            model = photo.foto_url,
            contentDescription = "Foto del usuario",
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable { showMenu = true },
            contentScale = ContentScale.Crop
        )

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Usar como perfil") },
                onClick = {
                    onChangeProfilePhoto()
                    showMenu = false
                },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null)
                }
            )
            DropdownMenuItem(
                text = { Text("Eliminar") },
                onClick = {
                    onDeletePhoto()
                    showMenu = false
                },
                leadingIcon = {
                    Icon(Icons.Default.Delete, contentDescription = null)
                }
            )
        }
    }
}

@Composable
fun ActionButtonsSection(
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = onEdit,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.Edit, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Editar")
        }

        Button(
            onClick = onDelete,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
        ) {
            Icon(Icons.Default.Delete, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Eliminar")
        }
    }
}

// Helper functions
private fun loadUserData(
    scope: kotlinx.coroutines.CoroutineScope,
    userId: Int,
    onUserLoaded: (User) -> Unit,
    onPhotosLoaded: (List<UserPhoto>) -> Unit,
    onError: (String) -> Unit
) {
    scope.launch {
        try {
            val userResponse = RetrofitClient.apiService.getUser(userId)
            if (userResponse.isSuccessful) {
                userResponse.body()?.let { onUserLoaded(it) }
            }

            loadUserPhotos(scope, userId, onPhotosLoaded)
        } catch (e: Exception) {
            onError("Error al cargar datos: ${e.message}")
        }
    }
}

private fun loadUserPhotos(
    scope: kotlinx.coroutines.CoroutineScope,
    userId: Int,
    onPhotosLoaded: (List<UserPhoto>) -> Unit
) {
    scope.launch {
        try {
            val photosResponse = RetrofitClient.apiService.getUserPhotos(userId)
            if (photosResponse.isSuccessful) {
                photosResponse.body()?.let { onPhotosLoaded(it) }
            }
        } catch (_: Exception) {
            // Handle error silently or log
        }
    }
}

private fun uploadPhoto(
    uri: Uri,
    context: Context,
    scope: kotlinx.coroutines.CoroutineScope,
    userId: Int,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    scope.launch {
        try {
            val imagePart = createMultipartFromUri(context, uri, "file")
            val response = RetrofitClient.apiService.addPhotoToUser(
                usuarioId = userId.toString().toRequestBody(),
                file = imagePart
            )

            if (response.isSuccessful) {
                onSuccess()
            } else {
                onError("Error al subir foto: ${response.message()}")
            }
        } catch (e: Exception) {
            onError("Error de conexión: ${e.message}")
        }
    }
}

private fun changeProfilePhoto(
    scope: kotlinx.coroutines.CoroutineScope,
    userId: Int,
    photoId: Int,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    scope.launch {
        try {
            val response = RetrofitClient.apiService.changeProfilePhoto(userId, photoId)
            if (response.isSuccessful) {
                onSuccess()
            } else {
                onError("Error al cambiar foto de perfil: ${response.message()}")
            }
        } catch (e: Exception) {
            onError("Error de conexión: ${e.message}")
        }
    }
}

private fun deleteUser(
    scope: CoroutineScope,
    userId: Int,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    scope.launch {
        try {
            val response = RetrofitClient.apiService.deleteUser(userId)
            if (response.isSuccessful) {
                onSuccess()
            } else {
                onError("Error al eliminar usuario: ${response.message()}")
            }
        } catch (e: Exception) {
            onError("Error de conexión: ${e.message}")
        }
    }
}

private fun deletePhoto(
    scope: kotlinx.coroutines.CoroutineScope,
    userId: Int,
    photoId: Int,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    scope.launch {
        try {
            val response = RetrofitClient.apiService.deletePhoto(userId, photoId)
            if (response.isSuccessful) {
                onSuccess()
            } else {
                onError("Error al eliminar foto: ${response.message()}")
            }
        } catch (e: Exception) {
            onError("Error de conexión: ${e.message}")
        }
    }
}
