package com.motosapp.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.motosapp.domain.model.ShopItem

@Composable
fun ShopItemCard(
    item: ShopItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(item.displayName, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("$${item.displayPrice}", color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))
            Text(item.itemType, style = MaterialTheme.typography.bodySmall)
        }
    }
}
