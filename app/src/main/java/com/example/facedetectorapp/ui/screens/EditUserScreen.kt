package com.example.facedetectorapp.ui.screens

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.example.facedetectorapp.data.api.RetrofitClient
import com.example.facedetectorapp.data.models.User
import com.example.facedetectorapp.ui.theme.*
import com.example.facedetectorapp.utils.createMultipartFromUri
import kotlinx.coroutines.launch
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun EditUserScreen(navController: NavController, userId: Int) {
    var user by remember { mutableStateOf<User?>(null) }
    var upaoID by remember { mutableStateOf("") }
    var nombres by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var requisitoriado by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showPhotoOptions by remember { mutableStateOf(false) }
    var showUpdateConfirmation by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var conservarFotoAnterior by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Camera permission
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    // Load user data
    LaunchedEffect(userId) {
        try {
            val response = RetrofitClient.apiService.getUser(userId)
            if (response.isSuccessful) {
                response.body()?.let { userData ->
                    user = userData
                    upaoID = userData.upaoID.toString()
                    nombres = userData.nombres
                    apellidos = userData.apellidos
                    correo = userData.correo
                    requisitoriado = userData.requisitoriado
                }
            }
        } catch (e: Exception) {
            errorMessage = "Error al cargar datos del usuario: ${e.message}"
        }
    }

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
            selectedImageUri = photoUri
        }
        showPhotoOptions = false
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        selectedImageUri = uri
        showPhotoOptions = false
    }

    fun updateUser() {
        scope.launch {
            isLoading = true
            try {
                val response = if (selectedImageUri != null) {
                    val imagePart = createMultipartFromUri(context, selectedImageUri!!, "foto")
                    RetrofitClient.apiService.updateUserWithPhoto(
                        userId = userId,
                        nombres = nombres.toRequestBody(),
                        apellidos = apellidos.toRequestBody(),
                        correo = correo.toRequestBody(),
                        requisitoriado = requisitoriado.toString().toRequestBody(),
                        conservar = conservarFotoAnterior.toString().toRequestBody(),
                        foto = imagePart
                    )
                } else {
                    RetrofitClient.apiService.updateUser(
                        userId = userId,
                        nombres = nombres,
                        apellidos = apellidos,
                        correo = correo,
                        requisitoriado = requisitoriado
                    )
                }

                if (response.isSuccessful) {
                    showSuccessDialog = true
                } else {
                    errorMessage = "Error al actualizar el usuario: ${response.message()}"
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Editar Usuario",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue
                )
            )
        }
    ) { paddingValues ->
        user?.let {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Photo selection
                EditPhotoSelectionCard(
                    currentPhotoUrl = user?.foto,
                    selectedImageUri = selectedImageUri,
                    onPhotoClick = { showPhotoOptions = true }
                )

                // Form fields
                OutlinedTextField(
                    value = upaoID,
                    onValueChange = { upaoID = it },
                    label = { Text("ID UPAO") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.Badge, contentDescription = null)
                    }
                )

                OutlinedTextField(
                    value = nombres,
                    onValueChange = { nombres = it },
                    label = { Text("Nombres") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null)
                    }
                )

                OutlinedTextField(
                    value = apellidos,
                    onValueChange = { apellidos = it },
                    label = { Text("Apellidos") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null)
                    }
                )

                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it },
                    label = { Text("Correo electrónico") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null)
                    }
                )

                // Requisitoriado switch
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "¿Requisitoriado?",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Marcar si la persona está requisitoriada",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceLight.copy(alpha = 0.6f)
                            )
                        }

                        Switch(
                            checked = requisitoriado,
                            onCheckedChange = { requisitoriado = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ErrorRed,
                                checkedTrackColor = ErrorRed.copy(alpha = 0.3f)
                            )
                        )
                    }
                }

                // Conservar foto anterior (solo si se selecciona nueva foto)
                if (selectedImageUri != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Conservar foto anterior",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Mantener la foto anterior como foto adicional",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSurfaceLight.copy(alpha = 0.6f)
                                )
                            }

                            Switch(
                                checked = conservarFotoAnterior,
                                onCheckedChange = { conservarFotoAnterior = it }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Update button
                Button(
                    onClick = { showUpdateConfirmation = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White
                        )
                    } else {
                        Icon(
                            Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Actualizar Usuario")
                    }
                }
            }
        }
    }

    // Photo options dialog
    if (showPhotoOptions) {
        AlertDialog(
            onDismissRequest = { showPhotoOptions = false },
            title = { Text("Cambiar Foto") },
            text = { Text("¿Cómo deseas cambiar la foto?") },
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

    // Update confirmation dialog
    if (showUpdateConfirmation) {
        AlertDialog(
            onDismissRequest = { showUpdateConfirmation = false },
            title = { Text("Confirmar Actualización") },
            text = { Text("¿Estás seguro de que deseas actualizar este usuario?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showUpdateConfirmation = false
                        updateUser()
                    }
                ) {
                    Text("Actualizar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpdateConfirmation = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Success dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                navController.navigateUp()
            },
            title = { Text("¡Éxito!") },
            text = { Text("Usuario actualizado correctamente") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        navController.navigateUp()
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }

    // Error snackbar
    errorMessage?.let { message ->
        LaunchedEffect(message) {
            // Show snackbar or handle error
            errorMessage = null
        }
    }
}

@Composable
fun EditPhotoSelectionCard(
    currentPhotoUrl: String?,
    selectedImageUri: Uri?,
    onPhotoClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPhotoClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val imageToShow = selectedImageUri ?: currentPhotoUrl

            if (imageToShow != null) {
                AsyncImage(
                    model = imageToShow,
                    contentDescription = "Imagen seleccionada",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Text(
                    text = if (selectedImageUri != null) "Nueva foto seleccionada" else "Toca para cambiar la foto",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PrimaryBlue
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(PrimaryBlue.copy(alpha = 0.1f))
                        .border(2.dp, PrimaryBlue.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Text(
                    text = "Toca para seleccionar una foto",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PrimaryBlue
                )
            }
        }
    }
}
