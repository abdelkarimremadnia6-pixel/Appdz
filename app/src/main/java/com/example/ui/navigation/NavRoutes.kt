package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Products : Screen("products")
    object AddProduct : Screen("product_add?barcode={barcode}") {
        fun createRoute(barcode: String = "") = "product_add?barcode=$barcode"
    }
    object EditProduct : Screen("product_edit/{productId}") {
        fun createRoute(productId: Long) = "product_edit/$productId"
    }
    object ProductDetail : Screen("product_detail/{productId}") {
        fun createRoute(productId: Long) = "product_detail/$productId"
    }
    object Scanner : Screen("scanner")
    object Sales : Screen("sales")
    object NewSale : Screen("new_sale")
    object Purchases : Screen("purchases")
    object NewPurchase : Screen("new_purchase")
    object Suppliers : Screen("suppliers")
    object Debts : Screen("debts")
    object Reminders : Screen("reminders")
    object History : Screen("history")
    object Settings : Screen("settings")
    object License : Screen("license")
}
