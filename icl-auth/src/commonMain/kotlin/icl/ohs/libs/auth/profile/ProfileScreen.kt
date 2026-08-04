package icl.ohs.libs.auth.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import icl.ohs.libs.auth.AuthMessageBanner
import icl.ohs.libs.auth.AuthMessageBannerType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onBack: () -> Unit
) {
    val uiState = viewModel.uiState
    val scrollState = rememberScrollState()

    // The ViewModel's refresh() runs on its own CoroutineScope; make sure it's
    // cancelled once this screen leaves composition instead of leaking.
    DisposableEffect(viewModel) {
        onDispose { viewModel.clear() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Profile 1", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.refresh() }, enabled = !viewModel.isRefreshing) {
                            if (viewModel.isRefreshing) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF1B1B1F),
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            },
            containerColor = Color(0xFF1B1B1F) // Dark background
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Header Section (Visual Summary)
                ProfileHeaderCard(uiState)

                // Personal Details Section (Read-only)
                ProfileSection(title = "Personal Details", initiallyExpanded = true) {
                    PersonalDetailsForm(uiState)
                }

                // Contact Information Section (Read-only)
                ProfileSection(title = "Contact") {
                    ContactInfoForm(uiState)
                }

                // Location / Supervisor Area Section (Read-only)
                val locationTitle = if (uiState.isSupervisor) "Supervisor Area" else "Location Information"
                ProfileSection(title = locationTitle) {
                    LocationInfoForm(uiState)
                }

                // Community Health Units Section (Read-only)
                ProfileSection(title = "Community Health Unit(s) (CHUs)") {
                    CommunityHealthUnitsForm(uiState)
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        if (viewModel.errorMessage != null) {
            AuthMessageBanner(
                message = viewModel.errorMessage.orEmpty(),
                type = AuthMessageBannerType.Error,
                onDismiss = { viewModel.dismissMessages() },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(start = 20.dp, top = 12.dp, end = 20.dp),
            )
        }
    }
}

@Composable
fun ProfileHeaderCard(uiState: ProfileUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF252529))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(60.dp).clip(CircleShape).background(Color(0xFF424242)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.initials.ifBlank { "?" }, 
                    color = Color.White, 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 20.sp
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = uiState.fullName.ifBlank { "Full Name" }, 
                    style = MaterialTheme.typography.titleLarge, 
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "ID Number: ${uiState.idNumber.ifBlank { "Not provided" }}", 
                    color = Color.Gray
                )
            }
            
            // Status Indicator
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (uiState.status == "Active") Color(0xFF1B5E20).copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.1f)
            ) {
                Text(
                    text = uiState.status,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = if (uiState.status == "Active") Color(0xFF4CAF50) else Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun PersonalDetailsForm(uiState: ProfileUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ProfileDetailItem(label = "First Name", value = uiState.firstName)
        ProfileDetailItem(label = "Last Name", value = uiState.lastName)
        ProfileDetailItem(label = "ID Number", value = uiState.idNumber)
        ProfileDetailItem(label = "Role", value = uiState.role)
    }
}

@Composable
fun ContactInfoForm(uiState: ProfileUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ContactDetailItem(
            icon = Icons.Default.Phone,
            label = "Phone",
            value = uiState.phone.ifBlank { "Not provided" }
        )
        
        ContactDetailItem(
            icon = Icons.Default.Email,
            label = "Email Address",
            value = uiState.email.ifBlank { "Not provided" }
        )
    }
}

@Composable
fun ContactDetailItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                color = Color.Gray,
                fontSize = 12.sp
            )
            Text(
                text = value,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun LocationInfoForm(uiState: ProfileUiState) {
    val loc = uiState.locationInfo
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ProfileDetailItem(label = "Is Supervisor Area?", value = if (uiState.isSupervisor) "Yes" else "No")
        ProfileDetailItem(label = "Country", value = loc.countryName)
        ProfileDetailItem(label = "County", value = loc.countyName)
        ProfileDetailItem(label = "Sub-county", value = loc.subCountyName)
        ProfileDetailItem(label = "Ward", value = loc.wardName)
        ProfileDetailItem(label = "Facility", value = loc.facilityName)
    }
}

@Composable
fun ProfileDetailItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 12.sp
        )
        Text(
            text = value.ifBlank { "Not provided" },
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun CommunityHealthUnitsForm(uiState: ProfileUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (uiState.communityHealthUnits.isEmpty()) {
            Text(
                text = "No Community Health Units assigned",
                color = Color.Gray,
                fontSize = 14.sp
            )
        } else {
            uiState.communityHealthUnits.forEachIndexed { index, unit ->
                Text(
                    text = "${index + 1}. $unit",
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun ProfileSection(
    title: String,
    initiallyExpanded: Boolean = false,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title, 
                color = Color.White,
                style = MaterialTheme.typography.titleMedium, 
                fontWeight = FontWeight.Bold
            )
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, 
                contentDescription = null,
                tint = Color.White
            )
        }
        AnimatedVisibility(visible = expanded) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF252529)
                ),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.DarkGray)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    content()
                }
            }
        }
    }
}
