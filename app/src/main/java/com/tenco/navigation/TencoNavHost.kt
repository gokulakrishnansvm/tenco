package com.tenco.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tenco.feature.onboarding.LoginScreen
import com.tenco.feature.onboarding.RoleSelectScreen
import com.tenco.feature.supplier.ComplaintsScreen
import com.tenco.feature.supplier.DealersScreen
import com.tenco.feature.supplier.PricingScreen
import com.tenco.feature.supplier.ReportsScreen
import com.tenco.feature.supplier.SupplierDashboardScreen
import com.tenco.feature.supplier.TransactionsScreen
import com.tenco.feature.supplier.VendorsScreen
import com.tenco.feature.vendor.VendorComplaintScreen
import com.tenco.feature.vendor.VendorDashboardScreen
import com.tenco.feature.vendor.VendorHistoryScreen
import com.tenco.feature.vendor.VendorPayScreen

@Composable
fun TencoNavHost(
    appViewModel: AppViewModel,
    onChangeLanguage: () -> Unit,
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = appViewModel.startRoute,
        enterTransition = {
            slideIntoContainer(androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Start, androidx.compose.animation.core.tween(320)) +
                androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(320))
        },
        exitTransition = { androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(200)) },
        popEnterTransition = { androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(300)) },
        popExitTransition = {
            slideOutOfContainer(androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.End, androidx.compose.animation.core.tween(320)) +
                androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(320))
        },
    ) {

        val logout: () -> Unit = {
            appViewModel.logout()
            navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoggedIn = {
                    navController.navigate(Routes.ROLE) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.ROLE) {
            RoleSelectScreen(
                onSupplier = {
                    appViewModel.chooseSupplier()
                    navController.navigate(Routes.SUPPLIER_HOME) {
                        popUpTo(Routes.ROLE) { inclusive = true }
                    }
                },
                onVendor = {
                    appViewModel.chooseVendor {
                        navController.navigate(Routes.VENDOR_HOME) {
                            popUpTo(Routes.ROLE) { inclusive = true }
                        }
                    }
                },
                onChangeLanguage = onChangeLanguage,
            )
        }

        // ---- Supplier ----
        composable(Routes.SUPPLIER_HOME) {
            SupplierDashboardScreen(
                onNavigate = { route -> navController.navigate(route) },
                onChangeLanguage = onChangeLanguage,
                onLogout = logout,
            )
        }
        composable(Routes.SUPPLIER_DEALERS) { DealersScreen(onBack = navController::popBackStack, onOpenDealer = { navController.navigate(Routes.dealerDetail(it)) }) }
        composable(Routes.SUPPLIER_VENDORS) { VendorsScreen(onBack = navController::popBackStack, onOpenVendor = { navController.navigate(Routes.vendorDetail(it)) }) }
        composable(
            Routes.SUPPLIER_VENDOR_DETAIL,
            arguments = listOf(androidx.navigation.navArgument("vendorId") { type = androidx.navigation.NavType.StringType }),
        ) { entry ->
            com.tenco.feature.supplier.VendorDetailScreen(entry.arguments?.getString("vendorId").orEmpty(), onBack = navController::popBackStack)
        }
        composable(
            Routes.SUPPLIER_DEALER_DETAIL,
            arguments = listOf(androidx.navigation.navArgument("dealerId") { type = androidx.navigation.NavType.StringType }),
        ) { entry ->
            com.tenco.feature.supplier.DealerDetailScreen(entry.arguments?.getString("dealerId").orEmpty(), onBack = navController::popBackStack)
        }
        composable(Routes.SUPPLIER_PRICING) { PricingScreen(onBack = navController::popBackStack) }
        composable(Routes.SUPPLIER_TRANSACTIONS) { TransactionsScreen(onBack = navController::popBackStack) }
        composable(Routes.SUPPLIER_REPORTS) { ReportsScreen(onBack = navController::popBackStack) }
        composable(Routes.SUPPLIER_COMPLAINTS) { ComplaintsScreen(onBack = navController::popBackStack) }
        composable(Routes.SUPPLIER_INSIGHTS) { com.tenco.feature.supplier.InsightsScreen(onBack = navController::popBackStack) }
        composable(Routes.SUPPLIER_INVENTORY) { com.tenco.feature.supplier.InventoryScreen(onBack = navController::popBackStack) }
        composable(Routes.SUPPLIER_SELL) { com.tenco.feature.supplier.SellScreen(onBack = navController::popBackStack) }
        composable(Routes.SUPPLIER_ORDERS) {
            com.tenco.feature.supplier.SupplierOrdersScreen(
                onBack = navController::popBackStack,
                onOpenOrder = { navController.navigate(Routes.orderDetail(it)) },
            )
        }
        composable(Routes.SUPPLIER_CASH_APPROVALS) {
            com.tenco.feature.supplier.SupplierCashApprovalsScreen(onBack = navController::popBackStack)
        }
        composable(Routes.SUPPLIER_ADJUSTMENTS) {
            com.tenco.feature.supplier.SupplierAdjustmentsScreen(onBack = navController::popBackStack)
        }
        composable(
            Routes.SUPPLIER_ORDER_DETAIL,
            arguments = listOf(androidx.navigation.navArgument("orderId") { type = androidx.navigation.NavType.StringType }),
        ) { entry ->
            com.tenco.feature.supplier.SupplierOrderDetailScreen(entry.arguments?.getString("orderId").orEmpty(), onBack = navController::popBackStack)
        }

        composable(Routes.PROFILE) {
            com.tenco.feature.profile.ProfileScreen(
                onBack = navController::popBackStack,
                onChangeLanguage = onChangeLanguage,
                onNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                onLogout = logout,
            )
        }
        composable(Routes.NOTIFICATIONS) {
            com.tenco.feature.profile.NotificationsScreen(onBack = navController::popBackStack)
        }

        // ---- Vendor ----
        composable(Routes.VENDOR_HOME) {
            VendorDashboardScreen(
                vendorId = appViewModel.currentVendorId.orEmpty(),
                onNavigate = { route -> navController.navigate(route) },
                onChangeLanguage = onChangeLanguage,
                onLogout = logout,
            )
        }
        composable(Routes.VENDOR_PAY) {
            VendorPayScreen(
                vendorId = appViewModel.currentVendorId.orEmpty(),
                onBack = navController::popBackStack,
            )
        }
        composable(Routes.VENDOR_ORDERS) {
            com.tenco.feature.vendor.VendorOrdersScreen(
                vendorId = appViewModel.currentVendorId.orEmpty(),
                onBack = navController::popBackStack,
            )
        }
        composable(Routes.VENDOR_COMPLAINT) {
            VendorComplaintScreen(
                vendorId = appViewModel.currentVendorId.orEmpty(),
                onBack = navController::popBackStack,
            )
        }
        composable(Routes.VENDOR_HISTORY) {
            VendorHistoryScreen(
                vendorId = appViewModel.currentVendorId.orEmpty(),
                onBack = navController::popBackStack,
            )
        }
    }
}
