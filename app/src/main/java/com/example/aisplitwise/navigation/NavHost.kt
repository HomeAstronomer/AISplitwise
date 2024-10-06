package com.example.aisplitwise.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.example.aisplitwise.data.local.Member
import com.example.aisplitwise.feature.dashboard.DashBoard
import com.example.aisplitwise.feature.feature_add_member.AddMemberDialog
import com.example.aisplitwise.feature.feature_create_group.CreateGroupScreen
import com.example.aisplitwise.feature.feature_expense_dialog.ExpenseDialog
import com.example.aisplitwise.feature.feature_expense_dialog.ExpenseDialogViewModel
import com.example.aisplitwise.feature.feature_joinGroup.JoinGroupDialog
import com.example.aisplitwise.feature.feature_ledger.LedgerScreen
import com.example.aisplitwise.feature.feature_login.LoginScreen
import com.example.aisplitwise.feature.feature_signup.SignUpScreen
import com.example.aisplitwise.feature.feature_splash.SplashScreen

@Composable
fun NavHostInitializer(navController: NavHostController) {
    NavHost(navController = navController, startDestination = SplashRoute) {
        composable<SplashRoute>(
            enterTransition = { slideInOutTransition(slideDirection = SlideDirection.RightToLeft).first() },
            exitTransition = { fadeOut(tween(300,100)) },
            popExitTransition = { slideInOutTransition(slideDirection = SlideDirection.LeftToRight).second() },
            popEnterTransition =  { slideInOutTransition(slideDirection = SlideDirection.LeftToRight).first() },
        ) {
            SplashScreen(hiltViewModel(),navController)
        }

        composable<DashBoardRoute> (
            enterTransition = { slideInOutTransition(slideDirection = SlideDirection.RightToLeft).first() },
            exitTransition = { fadeOut(tween(300,100)) },
            popExitTransition = { slideInOutTransition(slideDirection = SlideDirection.LeftToRight).second() },
            popEnterTransition =  { fadeIn() },
        ) {
            DashBoard(dashBoardViewModel = hiltViewModel(),navController)
        }


        composable<LoginScreenRoute> (
            enterTransition = { slideInOutTransition(slideDirection = SlideDirection.RightToLeft).first() },
            exitTransition = { fadeOut(tween(300,100)) },
            popExitTransition = { slideInOutTransition(slideDirection = SlideDirection.LeftToRight).second() },
            popEnterTransition =  { fadeIn() },
        ) {
            LoginScreen( loginViewModel = hiltViewModel(),
                navController)
        }

        composable<SignUpScreenRoute>(
            enterTransition = { slideInOutTransition(slideDirection = SlideDirection.RightToLeft).first() },
            exitTransition = { fadeOut(tween(300,100)) },
            popExitTransition = { slideInOutTransition(slideDirection = SlideDirection.LeftToRight).second() },
            popEnterTransition =  { fadeIn() },
            ) {
            SignUpScreen( signUpViewModel = hiltViewModel(),
                navController = navController)
        }

        composable<CreateGroupRoute>(
            enterTransition = { slideInOutTransition(slideDirection = SlideDirection.RightToLeft).first() },
            exitTransition = { fadeOut(tween(300,100)) },
            popExitTransition = { slideInOutTransition(slideDirection = SlideDirection.LeftToRight).second() },
            popEnterTransition =  { fadeIn() },
            ) {
            CreateGroupScreen(
                navController = navController)
        }

        composable<LedgerRoute>(
            enterTransition = { slideInOutTransition(slideDirection = SlideDirection.RightToLeft).first() },
            exitTransition = { fadeOut(tween(300,100)) },
            popExitTransition = { slideInOutTransition(slideDirection = SlideDirection.LeftToRight).second() },
            popEnterTransition =  { fadeIn() },
        ) {
            LedgerScreen(
                navHostController = navController,
                ledgerViewModel = hiltViewModel(),
                navigateToExpenseDialog={
                    navController.currentBackStackEntry?.savedStateHandle?.set("Members_Key",it)
                    navController.navigate(ExpenseDialogRoute)
                })
        }

        composable<ExpenseDialogRoute>(
            enterTransition = { slideInOutTransition(slideDirection = SlideDirection.RightToLeft).first() },
            exitTransition = { fadeOut(tween(300,100)) },
            popExitTransition = { slideInOutTransition(slideDirection = SlideDirection.LeftToRight).second() },
            popEnterTransition =  { fadeIn() },
        ) {

            ExpenseDialog(
                navHostController = navController,
                expenseViewModel = hiltViewModel<ExpenseDialogViewModel>(),
                members= navController.previousBackStackEntry?.savedStateHandle?.get<List<Member>>("Members_Key")?: emptyList()
            )
        }
        dialog<AddMemberDialogRoute>(dialogProperties = DialogProperties(usePlatformDefaultWidth = false)) {

            AddMemberDialog(
                navHostController = navController,
                groupId= it.arguments?.getString("groupId")?:""
            )
        }
        val uri="https://com.example.aisplitwise"
        dialog<JoinGroupDialogRoute>(dialogProperties = DialogProperties(usePlatformDefaultWidth = false),
            deepLinks = listOf(navDeepLink { uriPattern= "$uri/joinGroup/{groupId}"})
        ) {
            JoinGroupDialog(
                groupId= it.toRoute<JoinGroupDialogRoute>().groupId,
                navhostController=navController
            )
        }

    }
}



