package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.EngineType
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommandCenterScreen(
    currentEngineOverride: EngineType?,
    onSelectEngineOverride: (EngineType?) -> Unit,
    onOpenMemory: () -> Unit,
    onOpenStudy: () -> Unit,
    onClearAllData: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPrivateModeEnabled by remember { mutableStateOf(false) }
    var isOnDevicePreferred by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = VyomWhite,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Command Center",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            color = VyomTextPrimary
                        )
                        Text(
                            "VYOM ENGINE & Intelligence Configuration",
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VyomWhite)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: VYOM ENGINE Overview
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = VyomSurfaceContainer,
                    border = androidx.compose.foundation.BorderStroke(1.dp, VyomBorder)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(VyomCosmicIndigo),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = VyomWhite, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("VYOM ENGINE ARCHITECTURE", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = VyomCosmicIndigo)
                                Text("Independent Model Routing & Intelligence Layer", fontSize = 12.sp, color = VyomTextSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        EngineStatusRow("Cloud Intelligence", "High-capacity reasoning & broad domain synthesis", Icons.Rounded.CloudQueue, Color(0xFF2563EB))
                        EngineStatusRow("On-Device Intelligence", "Ultra-fast, private, on-device operations", Icons.Rounded.PhoneAndroid, Color(0xFF059669))
                        EngineStatusRow("Vision Intelligence", "Multimodal perception & screen understanding", Icons.Rounded.Visibility, Color(0xFF0284C7))
                        EngineStatusRow("Voice Intelligence", "Natural conversational speech & real-time live mode", Icons.Rounded.Mic, Color(0xFFD97706))
                        EngineStatusRow("Reasoning Intelligence", "Deep multi-step logic, math & code proofs", Icons.Rounded.Psychology, Color(0xFF7C3AED))
                        EngineStatusRow("Creative Intelligence", "Generative visual art & design synthesis", Icons.Rounded.Palette, Color(0xFF9333EA))
                    }
                }
            }

            // Section 2: Quick Feature Shortcuts
            item {
                Text(
                    text = "INTELLIGENCE HUBS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = VyomTextTertiary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HubShortcutCard(
                        title = "Personal Memory",
                        subtitle = "Preferences & facts",
                        icon = Icons.Rounded.Psychology,
                        tint = Color(0xFF0D9488),
                        modifier = Modifier.weight(1f),
                        onClick = onOpenMemory
                    )
                    HubShortcutCard(
                        title = "VYOM Study",
                        subtitle = "Math, science, quizzes",
                        icon = Icons.Rounded.School,
                        tint = Color(0xFF7C3AED),
                        modifier = Modifier.weight(1f),
                        onClick = onOpenStudy
                    )
                }
            }

            // Section 3: Privacy & Security
            item {
                Text(
                    text = "PRIVACY & SECURITY CONTROLS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = VyomTextTertiary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = VyomWhite,
                    border = androidx.compose.foundation.BorderStroke(1.dp, VyomBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Prefer On-Device Processing", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("Route tasks to local device models whenever available", fontSize = 12.sp, color = VyomTextSecondary)
                            }
                            Switch(
                                checked = isOnDevicePreferred,
                                onCheckedChange = { isOnDevicePreferred = it }
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Private Conversation Mode", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("Do not store recent messages in database history", fontSize = 12.sp, color = VyomTextSecondary)
                            }
                            Switch(
                                checked = isPrivateModeEnabled,
                                onCheckedChange = { isPrivateModeEnabled = it }
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        // Clear All Data button
                        OutlinedButton(
                            onClick = { showClearConfirmDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Clear All Conversations & Memories")
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        if (showClearConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showClearConfirmDialog = false },
                title = { Text("Clear All Data?") },
                text = { Text("This will permanently delete all conversation threads and saved personal memories from this device.") },
                confirmButton = {
                    Button(
                        onClick = {
                            onClearAllData()
                            showClearConfirmDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                    ) {
                        Text("Delete All", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearConfirmDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun EngineStatusRow(title: String, description: String, icon: ImageVector, tint: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = VyomTextPrimary)
            Text(description, fontSize = 11.5.sp, color = VyomTextSecondary)
        }
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(tint)
        )
    }
}

@Composable
fun HubShortcutCard(title: String, subtitle: String, icon: ImageVector, tint: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        color = VyomWhite,
        border = androidx.compose.foundation.BorderStroke(1.dp, VyomBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(tint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = VyomTextPrimary)
            Text(subtitle, fontSize = 11.5.sp, color = VyomTextTertiary)
        }
    }
}
