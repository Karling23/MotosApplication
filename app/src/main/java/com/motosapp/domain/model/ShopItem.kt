package com.motosapp.domain.model

interface ShopItem {
    val id: Int
    val displayName: String
    val displayPrice: Double
    val displayImageUrl: String?
    val itemType: String
}
