package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.entity.MemoryEntity
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryScreen(
    memories: List<MemoryEntity>,
    onAddMemory: (String, String, String) -> Unit,
    onDeleteMemory: (Long) -> Unit,
    onClearAll: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var newKey by remember { mutableStateOf("") }
    var newValue by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("Preference") }

    val filtered = remember(memories, searchQuery) {
        if (searchQuery.isBlank()) memories
        else memories.filter {
            it.key.contains(searchQuery, ignoreCase = true) ||
            it.value.contains(searchQuery, ignoreCase = true) ||
            it.category.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = VyomWhite,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "VYOM Memory",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            color = VyomTextPrimary
                        )
                        Text(
                            "Persistent preferences & instructions",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = VyomTextTertiary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = VyomTextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Rounded.Add, contentDescription = "Add Memory", tint = VyomCosmicIndigo)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VyomWhite)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search remembered facts...") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = VyomTextTertiary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.Psychology, contentDescription = null, tint = VyomBorder, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isBlank()) "No memories saved yet" else "No matching memories",
                            fontWeight = FontWeight.SemiBold,
                            color = VyomTextSecondary
                        )
                        Text(
                            text = "Tell Vyom: \"Remember that I prefer short answers\" or tap '+'",
                            fontSize = 12.sp,
                            color = VyomTextTertiary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(filtered, key = { it.id }) { memory ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = VyomSurfaceContainer,
                            border = androidx.compose.foundation.BorderStroke(1.dp, VyomBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = VyomCosmicIndigo.copy(alpha = 0.1f)
                                        ) {
                                            Text(
                                                text = memory.category,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = VyomCosmicIndigo,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = memory.key,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = VyomTextPrimary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = memory.value,
                                        fontSize = 13.5.sp,
                                        color = VyomTextPrimary,
                                        lineHeight = 19.sp
                                    )
                                }
                                IconButton(
                                    onClick = { onDeleteMemory(memory.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete", tint = VyomTextTertiary, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Remember Fact / Preference") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = newKey,
                            onValueChange = { newKey = it },
                            label = { Text("Topic / Title (e.g. Work, Style)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newValue,
                            onValueChange = { newValue = it },
                            label = { Text("Fact to remember") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newKey.isNotBlank() && newValue.isNotBlank()) {
                                onAddMemory(newKey, newValue, newCategory)
                                newKey = ""
                                newValue = ""
                                showAddDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VyomCosmicIndigo)
                    ) {
                        Text("Save to VYOM", color = VyomWhite)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
