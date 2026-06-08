package com.motosapp.presentation.ui.admin.marcas

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
import com.motosapp.domain.model.Marca
import com.motosapp.presentation.viewmodel.MarcasAdminViewModel

@Composable
fun MarcasAdminScreen(
    viewModel: MarcasAdminViewModel = hiltViewModel()
) {
    val marcas by viewModel.marcas.collectAsState()
    var showSheet by remember { mutableStateOf(false) }
    var selectedMarca by remember { mutableStateOf<Marca?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { selectedMarca = null; showSheet = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        LazyColumn(contentPadding = padding, modifier = Modifier.fillMaxSize()) {
            items(marcas) { marca ->
                ListItem(
                    headlineContent = { Text(marca.nombre) },
                    supportingContent = { Text(marca.descripcion ?: "") },
                    trailingContent = {
                        Row {
                            TextButton(onClick = { selectedMarca = marca; showSheet = true }) { Text("Editar") }
                            TextButton(onClick = { viewModel.delete(marca.id) }) { Text("Borrar") }
                        }
                    }
                )
                HorizontalDivider()
            }
        }
    }

    if (showSheet) {
        MarcaFormSheet(
            marca = selectedMarca,
            onDismiss = { showSheet = false },
            onSave = { payload ->
                viewModel.save(selectedMarca?.id, payload)
                showSheet = false
            }
        )
    }
}
