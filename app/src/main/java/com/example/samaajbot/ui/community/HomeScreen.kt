package com.example.samaajbot.ui.community

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.samaajbot.data.models.CommunityResponse
import com.example.samaajbot.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onCommunityClick: (Int, String, Boolean) -> Unit,
    onLogout: () -> Unit,
    // Notification tap params — -1 means not opened from notification
    notificationCommunityId: Int = -1,
    onNotificationNavigate: (Int, String, Boolean) -> Unit = { _, _, _ -> },
    viewModel: CommunityViewModel = hiltViewModel()
) {
    val communities by viewModel.communities.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showCreateDialog by remember { mutableStateOf(false) }
    var showJoinDialog   by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Handle notification tap — auto navigate to the community
    // Runs once when communities load and notificationCommunityId is set
    LaunchedEffect(communities, notificationCommunityId) {
        if (notificationCommunityId != -1 && communities is Resource.Success) {
            val communityList = (communities as Resource.Success).data
            val target = communityList.find { it.id == notificationCommunityId }
            if (target != null) {
                val isAdmin = target.adminId == viewModel.currentUserId
                onNotificationNavigate(target.id, target.name, isAdmin)
            }
        }
    }

    LaunchedEffect(actionState) {
        when (val state = actionState) {
            is Resource.Success -> {
                snackbarHostState.showSnackbar(state.data)
                viewModel.resetActionState()
            }
            is Resource.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetActionState()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("My Communities") },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor         = MaterialTheme.colorScheme.primary,
                    titleContentColor      = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment  = Alignment.End,
                verticalArrangement  = Arrangement.spacedBy(12.dp)
            ) {
                SmallFloatingActionButton(
                    onClick         = { showJoinDialog = true },
                    containerColor  = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(Icons.Default.Link, contentDescription = "Join Community")
                }
                FloatingActionButton(
                    onClick        = { showCreateDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Create Community",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    ) { padding ->
        when (val state = communities) {
            is Resource.Loading -> {
                Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }

            is Resource.Error -> {
                Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadCommunities() }) { Text("Retry") }
                    }
                }
            }

            is Resource.Success -> {
                if (state.data.isEmpty()) {
                    Box(
                        Modifier.fillMaxSize().padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Group,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint     = MaterialTheme.colorScheme.onSurfaceVariant
                                    .copy(alpha = 0.4f)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "No communities yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Tap + to create or join one",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                    .copy(alpha = 0.6f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier        = Modifier.fillMaxSize().padding(padding),
                        contentPadding  = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.data) { community ->
                            CommunityCard(
                                community = community,
                                isAdmin   = community.adminId == viewModel.currentUserId,
                                onClick   = {
                                    onCommunityClick(
                                        community.id,
                                        community.name,
                                        community.adminId == viewModel.currentUserId
                                    )
                                }
                            )
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateCommunityDialog(
            onDismiss = { showCreateDialog = false },
            onCreate  = { name, desc ->
                viewModel.createCommunity(name, desc)
                showCreateDialog = false
            }
        )
    }

    if (showJoinDialog) {
        JoinCommunityDialog(
            onDismiss = { showJoinDialog = false },
            onJoin    = { code ->
                viewModel.joinCommunity(code)
                showJoinDialog = false
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title   = { Text("Logout") },
            text    = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(onClick = { viewModel.logout(); onLogout() }) {
                    Text("Logout", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun CommunityCard(
    community: CommunityResponse,
    isAdmin: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick    = onClick,
        modifier   = Modifier.fillMaxWidth(),
        shape      = RoundedCornerShape(20.dp),
        colors     = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation  = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text     = community.name,
                    style    = MaterialTheme.typography.titleMedium,
                    color    = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isAdmin)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text     = if (isAdmin) "Admin" else "Member",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style    = MaterialTheme.typography.labelSmall,
                        color    = if (isAdmin)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            if (!community.description.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text     = community.description,
                    style    = MaterialTheme.typography.bodyMedium,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Tag,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint     = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text  = community.joinCode,
                    style = MaterialTheme.typography.bodyMedium
                        .copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.weight(1f))
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CreateCommunityDialog(onDismiss: () -> Unit, onCreate: (String, String?) -> Unit) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Community") },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value         = name,
                    onValueChange = { name = it },
                    label         = { Text("Community Name *") },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    shape         = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value         = desc,
                    onValueChange = { desc = it },
                    label         = { Text("Description (optional)") },
                    modifier      = Modifier.fillMaxWidth(),
                    maxLines      = 3,
                    shape         = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick  = { onCreate(name.trim(), desc.trim().ifBlank { null }) },
                enabled  = name.isNotBlank()
            ) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun JoinCommunityDialog(onDismiss: () -> Unit, onJoin: (String) -> Unit) {
    var code by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Join Community") },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Enter the 7-character join code shared by your admin.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value         = code,
                    onValueChange = { if (it.length <= 7) code = it.uppercase() },
                    label         = { Text("Join Code") },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    shape         = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick  = { onJoin(code) },
                enabled  = code.length == 7
            ) { Text("Join") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}