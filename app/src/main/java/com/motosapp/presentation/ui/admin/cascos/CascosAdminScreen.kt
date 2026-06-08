package com.motosapp.presentation.ui.admin.cascos

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
import com.motosapp.domain.model.Casco
import com.motosapp.presentation.viewmodel.CascosAdminViewModel

@Composable
fun CascosAdminScreen(
    viewModel: CascosAdminViewModel = hiltViewModel()
) {
    val cascos by viewModel.filtered.collectAsState()
    val marcas by viewModel.marcas.collectAsState()
    val formState by viewModel.formState.collectAsState()
    
    var showSheet by remember { mutableStateOf(false) }
    var selectedCasco by remember { mutableStateOf<Casco?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { selectedCasco = null; showSheet = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Casco")
            }
        }
    ) { padding ->
        LazyColumn(contentPadding = padding, modifier = Modifier.fillMaxSize()) {
            items(cascos) { casco ->
                ListItem(
                    headlineContent = { Text(casco.modelo) },
                    supportingContent = { Text("Stock: ${casco.stock} - Precio: $${casco.precio}") },
                    trailingContent = {
                        Row {
                            TextButton(onClick = { selectedCasco = casco; showSheet = true }) { Text("Editar") }
                            TextButton(onClick = { viewModel.deleteCasco(casco.id) }) { Text("Borrar") }
                        }
                    }
                )
                HorizontalDivider()
            }
        }
    }

    if (showSheet) {
        CascoFormSheet(
            casco = selectedCasco,
            marcas = marcas,
            formState = formState,
            onDismiss = { 
                showSheet = false 
                viewModel.resetFormState()
            },
            onSave = { payload ->
                if (selectedCasco == null) {
                    viewModel.createCasco(payload)
                } else {
                    viewModel.updateCasco(selectedCasco!!.id, payload)
                }
            }
        )
    }
}
