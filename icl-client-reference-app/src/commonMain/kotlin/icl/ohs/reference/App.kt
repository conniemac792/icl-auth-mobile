/*
 * Copyright 2026 Open Health Stack Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package icl.ohs.reference

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.savedstate.read
import icl.ohs.library.registry.LocalViewRegistry
import icl.ohs.reference.feature.group.list.GroupListScreen
import icl.ohs.reference.feature.group.profile.GroupProfileScreen
import icl.ohs.reference.feature.patient.profile.PatientProfileScreen
import icl.ohs.libs.auth.*
import icl.ohs.libs.auth.profile.*

private const val PROFILE_1_ROUTE = "profile1"
private const val PROFILE_2_ROUTE = "profile2"
private const val CHANGE_PASSWORD_ROUTE = "changePassword"
private const val GROUP_LIST_ROUTE = "groupList"
private const val GROUP_PROFILE_ROUTE = "groupProfile"
private const val PATIENT_PROFILE_ROUTE = "patientProfile"
private const val GROUP_ID_ARG = "groupId"
private const val PATIENT_ID_ARG = "patientId"

private val AUTH_CONFIG =
  IclAuthConfig(baseAuthUrl = "https://dsrkeycloak.intellisoftkenya.com/auth")

@Composable
fun App() {
  remember(AUTH_CONFIG) { IclAuth.initialize(AUTH_CONFIG) }
  val registry = remember { buildAppViewRegistry() }

  CompositionLocalProvider(LocalViewRegistry provides registry) {
    OhsPlayerTheme {
      var isLoggedIn by rememberSaveable { mutableStateOf(IclAuth.hasValidAccessToken()) }

      if (isLoggedIn) {
        ReferenceAppNavigation(onLogout = { 
            IclAuth.clear()
            ProfileRepository.clearProfile()
            isLoggedIn = false 
        })
      } else {
        AuthNavigation(onAuthenticated = { isLoggedIn = true })
      }
    }
  }
}

@Composable
private fun ReferenceAppNavigation(onLogout: () -> Unit) {
  val navController = rememberNavController()

  NavHost(navController = navController, startDestination = GROUP_LIST_ROUTE) {

    // Profile 1 (Original Detailed View)
    composable(PROFILE_1_ROUTE) {
      val viewModel = remember { ProfileViewModel() }
      ProfileScreen(
        viewModel = viewModel,
        onBack = { navController.popBackStack() }
      )
    }

    // Profile 2 (Modern Action View)
    composable(PROFILE_2_ROUTE) {
      val viewModel = remember { ProfileViewModel() }
      AlternativeProfileScreen(
        viewModel = viewModel,
        onLogout = onLogout,
        onBack = { navController.popBackStack() },
        onChangePasswordClick = { navController.navigate(CHANGE_PASSWORD_ROUTE) }
      )
    }

    // Change Password Screen
    composable(CHANGE_PASSWORD_ROUTE) {
      ResetPasswordScreen(
        config = ResetPasswordScreenConfig(showFooter = true),
        identifier = IclAuth.currentProviderUser()?.idNumber.orEmpty(),
        onPasswordResetSuccess = { navController.popBackStack() },
        onBackToLoginClick = { navController.popBackStack() }
      )
    }

    // Household list
    composable(GROUP_LIST_ROUTE) {
      GroupListScreen(
        onGroupClick = { id -> navController.navigate("$GROUP_PROFILE_ROUTE/$id") },
        onProfile1Click = { navController.navigate(PROFILE_1_ROUTE) },
        onProfile2Click = { navController.navigate(PROFILE_2_ROUTE) },
        onSettingsClick = { /* Optional: already direct access to P1/P2 */ },
        onLogoutClick = { onLogout() }
      )
    }

    // Household profile (head + members)
    composable(
      route = "$GROUP_PROFILE_ROUTE/{$GROUP_ID_ARG}",
      arguments = listOf(navArgument(GROUP_ID_ARG) { type = NavType.StringType }),
    ) { back ->
      val groupId = back.arguments?.read { getString(GROUP_ID_ARG) }.orEmpty()
      GroupProfileScreen(
        groupId = groupId,
        onBack = { navController.popBackStack() },
        onMemberClick = { id -> navController.navigate("$PATIENT_PROFILE_ROUTE/$id") },
      )
    }

    // Patient IPS summary
    composable(
      route = "$PATIENT_PROFILE_ROUTE/{$PATIENT_ID_ARG}",
      arguments = listOf(navArgument(PATIENT_ID_ARG) { type = NavType.StringType }),
    ) { back ->
      val patientId = back.arguments?.read { getString(PATIENT_ID_ARG) }.orEmpty()
      PatientProfileScreen(patientId = patientId, onBack = { navController.popBackStack() })
    }
  }
}
