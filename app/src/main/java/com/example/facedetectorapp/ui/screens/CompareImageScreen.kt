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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.example.facedetectorapp.data.api.RetrofitClient
import com.example.facedetectorapp.data.models.ComparisonResult
import com.example.facedetectorapp.ui.theme.*
import com.example.facedetectorapp.utils.createMultipartFromUri
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun CompareImageScreen(navController: NavController) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var comparisonResult by remember { mutableStateOf<ComparisonResult?>(null) }
    var showPhotoOptions by remember { mutableStateOf(false) }
    var showCompareConfirmation by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
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

    fun compareImage() {
        selectedImageUri?.let { uri ->
            scope.launch {
                isLoading = true
                try {
                    val imagePart = createMultipartFromUri(context, uri, "file")
                    val response = RetrofitClient.apiService.compareImage(imagePart)

                    if (response.isSuccessful) {
                        comparisonResult = response.body()
                    } else {
                        errorMessage = "Error en la comparación: ${response.message()}"
                    }
                } catch (e: Exception) {
                    errorMessage = "Error de conexión: ${e.message}"
                } finally {
                    isLoading = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Comparar Rostros",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Reconocimiento Facial",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Selecciona una imagen para comparar con la base de datos",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = OnSurfaceLight.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            )

            // Image selection card
            CompareImageSelectionCard(
                selectedImageUri = selectedImageUri,
                onPhotoClick = { showPhotoOptions = true }
            )

            // Compare button
            Button(
                onClick = { showCompareConfirmation = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = selectedImageUri != null && !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
                } else {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Comparar Rostro")
                }
            }

            // Results section
            comparisonResult?.let { result ->
                ComparisonResultCard(result = result)
            }
        }
    }

    // Photo options dialog
    if (showPhotoOptions) {
        AlertDialog(
            onDismissRequest = { showPhotoOptions = false },
            title = { Text("Seleccionar Imagen") },
            text = { Text("¿Cómo deseas seleccionar la imagen?") },
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

    // Compare confirmation dialog
    if (showCompareConfirmation) {
        AlertDialog(
            onDismissRequest = { showCompareConfirmation = false },
            title = { Text("Confirmar Comparación") },
            text = { Text("¿Deseas comparar esta imagen con la base de datos?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCompareConfirmation = false
                        compareImage()
                    }
                ) {
                    Text("Comparar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompareConfirmation = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Error handling
    errorMessage?.let { message ->
        LaunchedEffect(message) {
            errorMessage = null
        }
    }
}

@Composable
fun CompareImageSelectionCard(
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
            if (selectedImageUri != null) {
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = "Imagen seleccionada",
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )

                Text(
                    text = "Toca para cambiar la imagen",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PrimaryBlue
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PrimaryBlue.copy(alpha = 0.1f))
                        .border(2.dp, PrimaryBlue.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Seleccionar imagen",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PrimaryBlue
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ComparisonResultCard(result: ComparisonResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (result.securityAlert == true) ErrorRed.copy(alpha = 0.1f) else Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Resultado de Comparación",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                if (result.securityAlert == true) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Alerta de seguridad",
                        tint = ErrorRed,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Security alert (if present)
            if (result.securityAlert == true) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ErrorRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "⚠️ ALERTA DE SEGURIDAD",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "USUARIO REQUISITORIADO DETECTADO",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                        Text(
                            text = "Se ha notificado automáticamente a las autoridades",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            // User information
            result.user?.let { user ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Profile photo
                        if (user.foto != null) {
                            AsyncImage(
                                model = user.foto,
                                contentDescription = "Foto de perfil",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryBlue.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }

                        // User details
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "${user.nombres} ${user.apellidos}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "ID UPAO: ${user.upaoID}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurfaceLight.copy(alpha = 0.7f)
                            )
                            Text(
                                text = user.correo,
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceLight.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            // Similarity information
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Información de Similitud",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Similitud:")
                        Text(
                            text = "${(result.similarity * 100).toInt()}%",
                            fontWeight = FontWeight.Bold,
                            color = when {
                                result.similarity > 0.8 -> SuccessGreen
                                result.similarity > 0.6 -> Color(0xFFFF9800)
                                else -> ErrorRed
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Confianza:")
                        Text(
                            text = result.confidence.uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = when (result.confidence) {
                                "high" -> SuccessGreen
                                "medium" -> Color(0xFFFF9800)
                                else -> ErrorRed
                            }
                        )
                    }
                }
            }
        }
    }
}