package com.motosapp.presentation.ui.admin.accesorios

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.motosapp.domain.model.Accesorio
import com.motosapp.presentation.viewmodel.AccesoriosAdminViewModel

@Composable
fun AccesoriosAdminScreen(
    viewModel: AccesoriosAdminViewModel = hiltViewModel()
) {
    val accesorios by viewModel.filtered.collectAsState()
    val marcas by viewModel.marcas.collectAsState()
    val formState by viewModel.formState.collectAsState()
    
    var showSheet by remember { mutableStateOf(false) }
    var selectedAccesorio by remember { mutableStateOf<Accesorio?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { selectedAccesorio = null; showSheet = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Accesorio")
            }
        }
    ) { padding ->
        LazyColumn(contentPadding = padding, modifier = Modifier.fillMaxSize()) {
            items(accesorios) { accesorio ->
                ListItem(
                    headlineContent = { Text(accesorio.nombre) },
                    supportingContent = { Text("Stock: ${accesorio.stock} - Precio: $${accesorio.precio}") },
                    trailingContent = {
                        Row {
                            TextButton(onClick = { selectedAccesorio = accesorio; showSheet = true }) { Text("Editar") }
                            TextButton(onClick = { viewModel.deleteAccesorio(accesorio.id) }) { Text("Borrar") }
                        }
                    }
                )
                HorizontalDivider()
            }
        }
    }

    if (showSheet) {
        AccesorioFormSheet(
            accesorio = selectedAccesorio,
            marcas = marcas,
            formState = formState,
            onDismiss = { 
                showSheet = false 
                viewModel.resetFormState()
            },
            onSave = { payload ->
                if (selectedAccesorio == null) {
                    viewModel.createAccesorio(payload)
                } else {
                    viewModel.updateAccesorio(selectedAccesorio!!.id, payload)
                }
            }
        )
    }
}
