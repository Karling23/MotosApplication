package com.motosapp.presentation.ui.uipublic.catalog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.motosapp.presentation.viewmodel.CatalogViewModel
import com.motosapp.theme.*
import com.motosapp.presentation.components.ShopItemCard

@Composable
fun CatalogScreen(
    viewModel: CatalogViewModel = hiltViewModel(),
    onItemClick: (String, Int) -> Unit, // pass type and ID
) {
    val state by viewModel.state.collectAsState()
    val tabs = listOf("Motocicletas", "Cascos", "Accesorios")

    Column(modifier = Modifier.fillMaxSize().padding(top = 16.dp)) {
        Text(
            text = "Catálogo",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(Modifier.height(12.dp))
        
        OutlinedTextField(
            value = state.search,
            onValueChange = viewModel::setSearch,
            placeholder = { Text("Buscar...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(12.dp))

        TabRow(
            selectedTabIndex = state.selectedTab,
            containerColor = Surface,
            contentColor = Accent
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = state.selectedTab == index,
                    onClick = { viewModel.setTab(index) },
                    text = { Text(title) }
                )
            }
        }

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = Accent)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.currentItems, key = { "${it.itemType}_${it.id}" }) { item ->
                    ShopItemCard(
                        item = item,
                        onClick = { onItemClick(item.itemType, item.id) }
                    )
                }
            }
        }
    }
}
