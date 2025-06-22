package com.example.facedetectorapp.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.facedetectorapp.ui.theme.FaceDetectorAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FaceDetectorAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        onCreateUserClick = {
                            startActivity(Intent(this, CreateUserActivity::class.java))
                        },
                        onUserListClick = {
                            startActivity(Intent(this, UserListActivity::class.java))
                        },
                        onCompareImageClick = {
                            startActivity(Intent(this, CompareImageActivity::class.java))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    onCreateUserClick: () -> Unit,
    onUserListClick: () -> Unit,
    onCompareImageClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = onCreateUserClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Crear Usuario")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onUserListClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Lista de Usuarios")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onCompareImageClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Comparar Imagen")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    FaceDetectorAppTheme {
        MainScreen(onCreateUserClick = {}, onUserListClick = {}, onCompareImageClick = {})
    }
}
