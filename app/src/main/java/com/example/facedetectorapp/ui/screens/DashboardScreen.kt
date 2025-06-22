package com.example.facedetectorapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.facedetectorapp.data.api.RetrofitClient
import com.example.facedetectorapp.data.models.User
import com.example.facedetectorapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController) {
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.apiService.getUsers()
            if (response.isSuccessful) {
                users = response.body() ?: emptyList()
            }
        } catch (e: Exception) {
            // Handle error
        } finally {
            isLoading = false
        }
    }

    val totalUsers = users.size
    val requisitoriados = users.count { it.requisitoriado }
    val regulares = totalUsers - requisitoriados

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "FaceDetectorAPP",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Dashboard",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnBackgroundLight
                )
            }

            item {
                StatsSection(
                    totalUsers = totalUsers,
                    regulares = regulares,
                    requisitoriados = requisitoriados,
                    isLoading = isLoading
                )
            }

            item {
                Text(
                    text = "Acciones Rápidas",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = OnBackgroundLight,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                QuickActionsGrid(navController)
            }
        }
    }
}

@Composable
fun StatsSection(
    totalUsers: Int,
    regulares: Int,
    requisitoriados: Int,
    isLoading: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Estadísticas",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = OnBackgroundLight
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Total Usuarios",
                value = if (isLoading) "..." else totalUsers.toString(),
                icon = Icons.Default.People,
                color = PrimaryBlue,
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "Regulares",
                value = if (isLoading) "..." else regulares.toString(),
                icon = Icons.Default.Check,
                color = SuccessGreen,
                modifier = Modifier.weight(1f)
            )
        }

        StatCard(
            title = "Requisitoriados",
            value = if (isLoading) "..." else requisitoriados.toString(),
            icon = Icons.Default.Warning,
            color = ErrorRed,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(color.copy(alpha = 0.1f), color.copy(alpha = 0.05f))
                    )
                )
                .padding(16.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceLight.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }
}

@Composable
fun QuickActionsGrid(navController: NavController) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                title = "Gestionar Usuarios",
                subtitle = "Ver y administrar usuarios",
                icon = Icons.Default.People,
                color = PrimaryBlue,
                modifier = Modifier.weight(1f)
            ) {
                navController.navigate("users")
            }

            QuickActionCard(
                title = "Nuevo Usuario",
                subtitle = "Registrar usuario",
                icon = Icons.Default.PersonAdd,
                color = SuccessGreen,
                modifier = Modifier.weight(1f)
            ) {
                navController.navigate("create_user")
            }
        }

        QuickActionCard(
            title = "Comparar Imagen",
            subtitle = "Identificar persona por foto",
            icon = Icons.Default.CameraAlt,
            color = AccentOrange,
            modifier = Modifier.fillMaxWidth()
        ) {
            navController.navigate("compare_image")
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(color.copy(alpha = 0.1f), color.copy(alpha = 0.05f))
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(36.dp)
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceLight
                    )

                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceLight.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}