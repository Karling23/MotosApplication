// presentation/navigation/NavGraph.kt
package com.motosapp.presentation.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.*
import androidx.navigation.compose.*
import com.motosapp.presentation.components.LoadingScreen
import com.motosapp.presentation.ui.admin.AdminScaffold
import com.motosapp.presentation.ui.admin.marcas.MarcasAdminScreen
import com.motosapp.presentation.ui.admin.dashboard.DashboardScreen
import com.motosapp.presentation.ui.admin.orders.OrderAdminDetailScreen
import com.motosapp.presentation.ui.admin.orders.OrdersAdminScreen
import com.motosapp.presentation.ui.admin.accesorios.AccesoriosAdminScreen
import com.motosapp.presentation.ui.admin.cascos.CascosAdminScreen
import com.motosapp.presentation.ui.admin.motocicletas.MotocicletasAdminScreen
import com.motosapp.presentation.ui.admin.users.UsersAdminScreen
import com.motosapp.presentation.ui.auth.LoginScreen
import com.motosapp.presentation.ui.auth.RegisterScreen
import com.motosapp.presentation.ui.client.orders.OrderDetailScreen
import com.motosapp.presentation.ui.client.orders.OrdersScreen
import com.motosapp.presentation.ui.client.profile.ProfileScreen
import com.motosapp.presentation.ui.uipublic.cart.CartBottomSheet
import com.motosapp.presentation.ui.uipublic.catalog.CatalogScreen
import com.motosapp.presentation.ui.uipublic.home.HomeScreen
import com.motosapp.presentation.ui.uipublic.motocicleta.MotocicletaDetailScreen
import com.motosapp.presentation.viewmodel.AuthViewModel
import com.motosapp.presentation.viewmodel.CartViewModel
import com.motosapp.presentation.viewmodel.OrdersAdminViewModel
import com.motosapp.theme.Surface
import com.motosapp.theme.TextSecondary

@Composable
fun NavGraph(
    authViewModel: AuthViewModel,
    cartViewModel: CartViewModel = hiltViewModel(),
) {
    val navController     = rememberNavController()
    val isCheckingSession by authViewModel.isCheckingSession.collectAsState()
    val isAuthenticated   by authViewModel.isAuthenticated.collectAsState()
    val isStaff           by authViewModel.isStaff.collectAsState()
    val cartCount         by cartViewModel.totalItems.collectAsState()
    val currentUser       by authViewModel.currentUser.collectAsState()

    var showCart         by remember { mutableStateOf(false) }
    var confirmedOrderId by remember { mutableStateOf<Int?>(null) }

    if (isCheckingSession) {
        LoadingScreen("Iniciando MotosApp...")
        return
    }

    // Reacción reactiva al logout: si pierde autenticación redirige al Login
    // limpiando el backstack completo. Evita condición de carrera en ProfileScreen.
    val protectedRoutes = remember {
        listOf(
            Screen.Orders.route,
            Screen.Profile.route,
            Screen.AdminDashboard.route,
            "admin/marcas", "admin/motocicletas", "admin/cascos", "admin/accesorios",
            "admin/orders", "admin/users",
        )
    }
    LaunchedEffect(isAuthenticated) {
        if (!isAuthenticated) {
            val currentRouteSafe = navController.currentBackStackEntry?.destination?.route
            if (currentRouteSafe != null && currentRouteSafe !in listOf(Screen.Login.route, Screen.Register.route)) {
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    val startDestination = when {
        !isAuthenticated -> Screen.Login.route
        isStaff          -> Screen.AdminDashboard.route
        else             -> Screen.Home.route
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute      = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Catalog.route,
        Screen.Orders.route,
        Screen.Profile.route,
    )

    Scaffold(
        containerColor = Surface,
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    navController = navController,
                    cartCount     = cartCount,
                    onCartClick   = { showCart = true },
                )
            }
        },
    ) { innerPadding ->

        // ── BottomSheet carrito
        if (showCart) {
            CartBottomSheet(
                cartViewModel   = cartViewModel,
                isAuthenticated = isAuthenticated,
                onDismiss       = { showCart = false },
                onLoginRequired = {
                    showCart = false
                    navController.navigate(Screen.Login.route)
                },
                onOrderSuccess = { orderId ->
                    confirmedOrderId = orderId
                    showCart = false
                },
            )
        }

        NavHost(
            navController    = navController,
            startDestination = startDestination,
            modifier         = Modifier.padding(innerPadding),
        ) {

            // ── LOGIN ───────────────────────────────
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = { staff ->
                        val dest = if (staff) Screen.AdminDashboard.route else Screen.Home.route
                        navController.navigate(dest) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                    viewModel            = authViewModel,
                )
            }

            // ── REGISTER ────────────────────────────
            composable(Screen.Register.route) {
                RegisterScreen(
                    onRegisterSuccess = { staff ->
                        val dest = if (staff) Screen.AdminDashboard.route else Screen.Home.route
                        navController.navigate(dest) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.popBackStack() },
                    viewModel         = authViewModel,
                )
            }

            // ── HOME ───────────────────────────────
            composable(Screen.Home.route) {
                HomeScreen(
                    onItemClick = { id -> navController.navigate("motocicleta/$id") },
                    onCatalogClick = { navController.navigate(Screen.Catalog.route) },
                )
            }

            // ── CATALOGO ───────────────────────────
            composable(Screen.Catalog.route) {
                CatalogScreen(
                    onItemClick = { type, id -> navController.navigate("motocicleta/$id") },
                )
            }

            // ── DETALLE PRODUCTO ───────────────────
            composable(
                route     = "motocicleta/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType }),
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id") ?: return@composable
                MotocicletaDetailScreen(
                    motocicletaId     = id,
                    onBack        = { navController.popBackStack() },
                    cartViewModel = cartViewModel,
                )
            }

            // ── ORDERS CLIENT ──────────────────────
            composable(Screen.Orders.route) {
                if (!isAuthenticated) {
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Home.route)
                        }
                    }
                } else {
                    OrdersScreen(
                        onOrderClick = { id -> navController.navigate("orders/$id") },
                    )
                }
            }

            // ── ORDER DETAIL CLIENT ────────────────
            composable(
                route     = "orders/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType }),
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id") ?: return@composable
                OrderDetailScreen(
                    orderId = id,
                    onBack  = { navController.popBackStack() },
                )
            }

            // ── PROFILE ────────────────────────────
            composable(Screen.Profile.route) {
                if (!isAuthenticated) {
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Home.route)
                        }
                    }
                } else {
                    ProfileScreen(
                        authViewModel     = authViewModel,
                        onLogout          = {},
                        onNavigateToAdmin = {
                            navController.navigate(Screen.AdminDashboard.route) {
                                launchSingleTop = true
                            }
                        },
                    )
                }
            }

            // ── ADMIN DASHBOARD ────────────────────
            composable(Screen.AdminDashboard.route) {
                if (!isStaff) {
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Home.route) { popUpTo(0) }
                    }
                    return@composable
                }

                AdminScaffold(
                    currentRoute = Screen.AdminDashboard.route,
                    user         = currentUser,
                    title        = "Dashboard",
                    onNavClick   = { route ->
                        navController.navigate(route) {
                            launchSingleTop = true
                            restoreState    = true
                        }
                    },
                    onStoreClick = { navController.navigate(Screen.Home.route) },
                    onLogout     = {
                        authViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                ) { padding ->
                    Box(modifier = Modifier.padding(padding)) {
                        DashboardScreen(
                            onNavigate = { route -> navController.navigate(route) }
                        )
                    }
                }
            }

            // ── ADMIN CATEGORIES ───────────────────
            composable("admin/marcas") {
                if (!isStaff) {
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Home.route) { popUpTo(0) }
                    }
                    return@composable
                }

                AdminScaffold(
                    currentRoute = "admin/marcas",
                    user         = currentUser,
                    title        = "Marcas",
                    onNavClick   = { route ->
                        navController.navigate(route) { launchSingleTop = true }
                    },
                    onStoreClick = { navController.navigate(Screen.Home.route) },
                    onLogout     = {
                        authViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                ) { padding ->
                    Box(modifier = Modifier.padding(padding)) {
                        MarcasAdminScreen()
                    }
                }
            }

            // ── ADMIN CATEGORÍAS ───────────────────
            composable("admin/categorias") {
                if (!isStaff) {
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Home.route) { popUpTo(0) }
                    }
                    return@composable
                }

                AdminScaffold(
                    currentRoute = "admin/categorias",
                    user         = currentUser,
                    title        = "Categorías",
                    onNavClick   = { route ->
                        navController.navigate(route) { launchSingleTop = true }
                    },
                    onStoreClick = { navController.navigate(Screen.Home.route) },
                    onLogout     = {
                        authViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                ) { padding ->
                    Box(modifier = Modifier.padding(padding)) {
                        com.motosapp.presentation.ui.admin.categorias.CategoriasAdminScreen()
                    }
                }
            }

            // ── ADMIN PRODUCTS ─────────────────────
            composable("admin/motocicletas") {
                if (!isStaff) {
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Home.route) { popUpTo(0) }
                    }
                    return@composable
                }

                AdminScaffold(
                    currentRoute = "admin/motocicletas",
                    user         = currentUser,
                    title        = "Motocicletas",
                    onNavClick   = { route ->
                        navController.navigate(route) { launchSingleTop = true }
                    },
                    onStoreClick = { navController.navigate(Screen.Home.route) },
                    onLogout     = {
                        authViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                ) { padding ->
                    Box(modifier = Modifier.padding(padding)) {
                        MotocicletasAdminScreen()
                    }
                }
            }

            // ── ADMIN CASCOS ─────────────────────
            composable("admin/cascos") {
                if (!isStaff) {
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Home.route) { popUpTo(0) }
                    }
                    return@composable
                }

                AdminScaffold(
                    currentRoute = "admin/cascos",
                    user         = currentUser,
                    title        = "Cascos",
                    onNavClick   = { route ->
                        navController.navigate(route) { launchSingleTop = true }
                    },
                    onStoreClick = { navController.navigate(Screen.Home.route) },
                    onLogout     = {
                        authViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                ) { padding ->
                    Box(modifier = Modifier.padding(padding)) {
                        CascosAdminScreen()
                    }
                }
            }

            // ── ADMIN ACCESORIOS ─────────────────────
            composable("admin/accesorios") {
                if (!isStaff) {
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Home.route) { popUpTo(0) }
                    }
                    return@composable
                }

                AdminScaffold(
                    currentRoute = "admin/accesorios",
                    user         = currentUser,
                    title        = "Accesorios",
                    onNavClick   = { route ->
                        navController.navigate(route) { launchSingleTop = true }
                    },
                    onStoreClick = { navController.navigate(Screen.Home.route) },
                    onLogout     = {
                        authViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                ) { padding ->
                    Box(modifier = Modifier.padding(padding)) {
                        AccesoriosAdminScreen()
                    }
                }
            }

            // ── ADMIN ORDERS ───────────────────────
            composable("admin/orders") {
                if (!isStaff) {
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Home.route) { popUpTo(0) }
                    }
                    return@composable
                }

                val ordersAdminVm: OrdersAdminViewModel = hiltViewModel()

                AdminScaffold(
                    currentRoute = "admin/orders",
                    user         = currentUser,
                    title        = "Pedidos",
                    onNavClick   = { route ->
                        navController.navigate(route) { launchSingleTop = true }
                    },
                    onStoreClick = { navController.navigate(Screen.Home.route) },
                    onLogout     = {
                        authViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                ) { padding ->
                    Box(modifier = Modifier.padding(padding)) {
                        OrdersAdminScreen(
                            onOrderDetail = { id ->
                                navController.navigate("admin/orders/$id")
                            },
                            viewModel = ordersAdminVm,
                        )
                    }
                }
            }

            // ── ADMIN ORDER DETAIL ─────────────────
            composable(
                route     = "admin/orders/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType }),
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id") ?: return@composable

                if (!isStaff) {
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Home.route) { popUpTo(0) }
                    }
                    return@composable
                }

                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("admin/orders")
                }

                val ordersAdminVm: OrdersAdminViewModel = hiltViewModel(parentEntry)

                AdminScaffold(
                    currentRoute = "admin/orders",
                    user         = currentUser,
                    title        = "Detalle pedido #$id",
                    onNavClick   = { route ->
                        navController.navigate(route) { launchSingleTop = true }
                    },
                    onStoreClick = { navController.navigate(Screen.Home.route) },
                    onLogout     = {
                        authViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                ) { padding ->
                    Box(modifier = Modifier.padding(padding)) {
                        OrderAdminDetailScreen(
                            orderId = id,
                            onBack  = { navController.popBackStack() },
                            onStatusChange = { ordId, newStatus ->
                                ordersAdminVm.changeStatus(ordId, newStatus)
                            },
                        )
                    }
                }
            }

            // ── ADMIN USERS (CORREGIDO) ────────────
            composable("admin/users") {
                if (!isStaff) {
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Home.route) { popUpTo(0) }
                    }
                    return@composable
                }

                AdminScaffold(
                    currentRoute = "admin/users",
                    user         = currentUser,
                    title        = "Usuarios",
                    onNavClick   = { route ->
                        navController.navigate(route) { launchSingleTop = true }
                    },
                    onStoreClick = { navController.navigate(Screen.Home.route) },
                    onLogout     = {
                        authViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                ) { padding ->
                    Box(modifier = Modifier.padding(padding)) {
                        UsersAdminScreen()
                    }
                }
            }
        }
    }
}

