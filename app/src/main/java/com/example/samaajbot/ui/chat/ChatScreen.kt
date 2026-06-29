package com.example.samaajbot.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.samaajbot.data.models.ChatMessageEntity
import com.example.samaajbot.ui.theme.BotBubble
import com.example.samaajbot.ui.theme.BotBubbleDark
import com.example.samaajbot.ui.theme.BotBubbleText
import com.example.samaajbot.ui.theme.BotBubbleTextDark
import com.example.samaajbot.ui.theme.UserBubble
import com.example.samaajbot.ui.theme.UserBubbleText
import com.example.samaajbot.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    communityId: Int,
    communityName: String,
    isAdmin: Boolean,
    onBack: () -> Unit,
    onDocuments: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messages       by viewModel.messages.collectAsState()
    val askState       by viewModel.askState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState      = rememberLazyListState()
    val isLoading      = askState is Resource.Loading

    var inputText       by remember { mutableStateOf("") }
    var pendingQuestion by remember { mutableStateOf("") }
    var showClearDialog by remember { mutableStateOf(false) }

    // Initialise viewmodel with community id
    LaunchedEffect(communityId) {
        viewModel.init(communityId)
    }

    // Scroll to bottom whenever messages change or loading state changes
    LaunchedEffect(messages.size, isLoading) {
        val itemCount = messages.size + if (isLoading && pendingQuestion.isNotBlank()) 2 else 0
        if (itemCount > 0) {
            listState.animateScrollToItem(itemCount - 1)
        }
    }

    // Handle ask state changes
    LaunchedEffect(askState) {
        when (val state = askState) {
            is Resource.Success -> {
                // Clear pending question — real messages now loaded from server
                pendingQuestion = ""
                viewModel.resetAskState()
            }
            is Resource.Error -> {
                pendingQuestion = ""
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetAskState()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = communityName,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Ask anything about community docs",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onDocuments) {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = "Documents",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(onClick = { showClearDialog = true }) {
                        Icon(
                            Icons.Default.DeleteSweep,
                            contentDescription = "Clear History",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .navigationBarsPadding()
                        .imePadding(),
                    verticalAlignment = Alignment.Bottom
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Ask a question...") },
                        modifier = Modifier.weight(1f),
                        maxLines = 4,
                        shape = RoundedCornerShape(24.dp),
                        enabled = !isLoading,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    FilledIconButton(
                        onClick = {
                            val question = inputText.trim()
                            if (question.isNotBlank() && !isLoading) {
                                pendingQuestion = question  // show immediately on right
                                viewModel.askQuestion(question)
                                inputText = ""
                            }
                        },
                        enabled = inputText.isNotBlank() && !isLoading,
                        modifier = Modifier.size(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color    = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send"
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->

        // Empty state
        if (messages.isEmpty() && !isLoading && pendingQuestion.isBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Chat,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Ask anything!",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Questions are answered from\ncommunity documents.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Real messages from server (already in correct order)
                items(messages, key = { it.id }) { message ->
                    MessageBubble(message = message)
                }

                // Show pending question on RIGHT while waiting for answer
                // This prevents the flash — no temp DB entry, just UI state
                if (isLoading && pendingQuestion.isNotBlank()) {
                    item(key = "pending_question") {
                        MessageBubble(
                            message = ChatMessageEntity(
                                id           = -1,
                                communityId  = communityId,
                                userId       = 0,
                                role         = "user",
                                content      = pendingQuestion,
                                sourceDoc    = null,
                                createdAt    = ""
                            )
                        )
                    }
                    item(key = "typing_indicator") {
                        TypingIndicator()
                    }
                }

                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }

    // Clear history dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title   = { Text("Clear History") },
            text    = { Text("Delete all chat messages for this community?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearHistory()
                    showClearDialog = false
                }) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun MessageBubble(message: ChatMessageEntity) {
    val isUser = message.role == "user"
    val isDark = isSystemInDarkTheme()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        // Bot avatar on the left
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "S",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 300.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            // Message bubble
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart    = 18.dp,
                            topEnd      = 18.dp,
                            bottomStart = if (isUser) 18.dp else 4.dp,
                            bottomEnd   = if (isUser) 4.dp else 18.dp
                        )
                    )
                    .background(
                        if (isUser) UserBubble
                        else if (isDark) BotBubbleDark else BotBubble
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text  = message.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isUser) UserBubbleText
                    else if (isDark) BotBubbleTextDark else BotBubbleText
                )
            }

            // Source citation below bot message
            if (!message.sourceDoc.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text     = "Source: ${message.sourceDoc}",
                    style    = MaterialTheme.typography.labelSmall.copy(fontStyle = FontStyle.Italic),
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "S",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                "Thinking...",
                style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}