package com.motosapp.presentation.ui.admin.categorias

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.motosapp.presentation.viewmodel.CategoriasAdminViewModel

@Composable
fun CategoriasAdminScreen(
    viewModel: CategoriasAdminViewModel = hiltViewModel()
) {
    val categorias by viewModel.categorias.collectAsState()

    var showSheet by remember { mutableStateOf(false) }
    var selectedCategoria by remember { mutableStateOf<com.motosapp.domain.model.Categoria?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { selectedCategoria = null; showSheet = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        LazyColumn(contentPadding = padding, modifier = Modifier.fillMaxSize()) {
            items(categorias) { categoria ->
                ListItem(
                    headlineContent = { Text(categoria.nombre) },
                    supportingContent = { Text(categoria.descripcion ?: "") },
                    trailingContent = {
                        Row {
                            TextButton(onClick = { selectedCategoria = categoria; showSheet = true }) { Text("Editar") }
                            TextButton(onClick = { viewModel.delete(categoria.id) }) { Text("Borrar") }
                        }
                    }
                )
                HorizontalDivider()
            }
        }
    }

    if (showSheet) {
        CategoriaFormSheet(
            categoria = selectedCategoria,
            onDismiss = { showSheet = false },
            onSave = { payload ->
                viewModel.save(selectedCategoria?.id, payload)
                showSheet = false
            }
        )
    }
}
