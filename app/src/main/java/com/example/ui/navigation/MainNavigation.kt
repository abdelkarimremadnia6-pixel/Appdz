package com.example.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.R
import com.example.data.repository.InventoryRepository
import com.example.ui.AppViewModelFactory
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.dashboard.DashboardViewModel
import com.example.ui.debts.DebtsScreen
import com.example.ui.debts.DebtsViewModel
import com.example.ui.history.StockHistoryScreen
import com.example.ui.license.LicenseScreen
import com.example.ui.license.LicenseViewModel
import com.example.ui.products.AddEditProductScreen
import com.example.ui.products.ProductDetailScreen
import com.example.ui.products.ProductListScreen
import com.example.ui.products.ProductsViewModel
import com.example.ui.purchases.NewPurchaseScreen
import com.example.ui.purchases.PurchasesScreen
import com.example.ui.purchases.PurchasesViewModel
import com.example.ui.reminders.RemindersScreen
import com.example.ui.reminders.RemindersViewModel
import com.example.ui.sales.NewSaleScreen
import com.example.ui.sales.SalesScreen
import com.example.ui.sales.SalesViewModel
import com.example.ui.scanner.BarcodeScannerScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.settings.SettingsViewModel
import com.example.ui.suppliers.SuppliersScreen
import com.example.ui.suppliers.SuppliersViewModel

data class BottomNavItem(
    val route: String,
    val titleRes: Int,
    val icon: ImageVector,
    val testTag: String
)

@Composable
fun MainNavigation(
    viewModelFactory: AppViewModelFactory,
    repository: InventoryRepository
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        BottomNavItem(Screen.Dashboard.route, R.string.dashboard, Icons.Default.Dashboard, "nav_dashboard"),
        BottomNavItem(Screen.Products.route, R.string.products, Icons.Default.Inventory2, "nav_products"),
        BottomNavItem(Screen.Sales.route, R.string.sales, Icons.Default.ShoppingCart, "nav_sales"),
        BottomNavItem(Screen.Purchases.route, R.string.purchases, Icons.Default.LocalShipping, "nav_purchases"),
        BottomNavItem(Screen.Settings.route, R.string.settings, Icons.Default.Settings, "nav_settings")
    )

    // Show bottom bar only on top-level destinations
    val showBottomBar = bottomNavItems.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = stringResource(item.titleRes)) },
                            label = { Text(stringResource(item.titleRes)) },
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Dashboard
            composable(Screen.Dashboard.route) {
                val dashboardViewModel: DashboardViewModel = viewModel(factory = viewModelFactory)
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onNavigateToAddProduct = { navController.navigate(Screen.AddProduct.createRoute()) },
                    onNavigateToScan = { navController.navigate(Screen.Scanner.route) },
                    onNavigateToNewSale = { navController.navigate(Screen.NewSale.route) },
                    onNavigateToNewPurchase = { navController.navigate(Screen.NewPurchase.route) },
                    onNavigateToProducts = { filter ->
                        navController.navigate("${Screen.Products.route}?filter=$filter")
                    },
                    onNavigateToDebts = { navController.navigate(Screen.Debts.route) },
                    onNavigateToSuppliers = { navController.navigate(Screen.Suppliers.route) }
                )
            }

            // Products
            composable(
                route = "${Screen.Products.route}?filter={filter}",
                arguments = listOf(
                    navArgument("filter") {
                        type = NavType.StringType
                        defaultValue = "ALL"
                    }
                )
            ) { backStackEntry ->
                val filter = backStackEntry.arguments?.getString("filter") ?: "ALL"
                val productsViewModel: ProductsViewModel = viewModel(factory = viewModelFactory)
                ProductListScreen(
                    viewModel = productsViewModel,
                    initialFilter = filter,
                    onNavigateToAddProduct = { navController.navigate(Screen.AddProduct.createRoute()) },
                    onNavigateToProductDetail = { id -> navController.navigate(Screen.ProductDetail.createRoute(id)) },
                    onNavigateToEditProduct = { id -> navController.navigate(Screen.EditProduct.createRoute(id)) }
                )
            }

            // Add Product
            composable(
                route = Screen.AddProduct.route,
                arguments = listOf(
                    navArgument("barcode") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { backStackEntry ->
                val barcode = backStackEntry.arguments?.getString("barcode") ?: ""
                val productsViewModel: ProductsViewModel = viewModel(factory = viewModelFactory)
                AddEditProductScreen(
                    viewModel = productsViewModel,
                    productId = 0L,
                    initialBarcode = barcode,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToScan = { navController.navigate(Screen.Scanner.route) }
                )
            }

            // Edit Product
            composable(
                route = Screen.EditProduct.route,
                arguments = listOf(
                    navArgument("productId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getLong("productId") ?: 0L
                val productsViewModel: ProductsViewModel = viewModel(factory = viewModelFactory)
                AddEditProductScreen(
                    viewModel = productsViewModel,
                    productId = productId,
                    initialBarcode = "",
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToScan = { navController.navigate(Screen.Scanner.route) }
                )
            }

            // Product Detail
            composable(
                route = Screen.ProductDetail.route,
                arguments = listOf(
                    navArgument("productId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getLong("productId") ?: 0L
                val productsViewModel: ProductsViewModel = viewModel(factory = viewModelFactory)
                ProductDetailScreen(
                    viewModel = productsViewModel,
                    productId = productId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { id -> navController.navigate(Screen.EditProduct.createRoute(id)) }
                )
            }

            // Barcode Scanner
            composable(Screen.Scanner.route) {
                BarcodeScannerScreen(
                    repository = repository,
                    onNavigateBack = { navController.popBackStack() },
                    onProductFound = { productId ->
                        navController.navigate(Screen.ProductDetail.createRoute(productId)) {
                            popUpTo(Screen.Scanner.route) { inclusive = true }
                        }
                    },
                    onProductNotFound = { scannedBarcode ->
                        navController.navigate(Screen.AddProduct.createRoute(scannedBarcode)) {
                            popUpTo(Screen.Scanner.route) { inclusive = true }
                        }
                    }
                )
            }

            // Sales
            composable(Screen.Sales.route) {
                val salesViewModel: SalesViewModel = viewModel(factory = viewModelFactory)
                SalesScreen(
                    viewModel = salesViewModel,
                    onNavigateToNewSale = { navController.navigate(Screen.NewSale.route) }
                )
            }

            // New Sale
            composable(Screen.NewSale.route) {
                val salesViewModel: SalesViewModel = viewModel(factory = viewModelFactory)
                NewSaleScreen(
                    viewModel = salesViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Purchases
            composable(Screen.Purchases.route) {
                val purchasesViewModel: PurchasesViewModel = viewModel(factory = viewModelFactory)
                PurchasesScreen(
                    viewModel = purchasesViewModel,
                    onNavigateToNewPurchase = { navController.navigate(Screen.NewPurchase.route) }
                )
            }

            // New Purchase
            composable(Screen.NewPurchase.route) {
                val purchasesViewModel: PurchasesViewModel = viewModel(factory = viewModelFactory)
                NewPurchaseScreen(
                    viewModel = purchasesViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Suppliers
            composable(Screen.Suppliers.route) {
                val suppliersViewModel: SuppliersViewModel = viewModel(factory = viewModelFactory)
                SuppliersScreen(viewModel = suppliersViewModel)
            }

            // Debts
            composable(Screen.Debts.route) {
                val debtsViewModel: DebtsViewModel = viewModel(factory = viewModelFactory)
                DebtsScreen(viewModel = debtsViewModel)
            }

            // Reminders
            composable(Screen.Reminders.route) {
                val remindersViewModel: RemindersViewModel = viewModel(factory = viewModelFactory)
                RemindersScreen(viewModel = remindersViewModel)
            }

            // Stock History
            composable(Screen.History.route) {
                StockHistoryScreen(
                    repository = repository,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Settings
            composable(Screen.Settings.route) {
                val settingsViewModel: SettingsViewModel = viewModel(factory = viewModelFactory)
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onNavigateToLicense = { navController.navigate(Screen.License.route) },
                    onNavigateToHistory = { navController.navigate(Screen.History.route) }
                )
            }

            // License
            composable(Screen.License.route) {
                val licenseViewModel: LicenseViewModel = viewModel(factory = viewModelFactory)
                LicenseScreen(
                    viewModel = licenseViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
