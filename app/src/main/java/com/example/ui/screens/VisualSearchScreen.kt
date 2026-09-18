package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.OrbState
import com.example.ui.components.VyomOrb
import com.example.ui.theme.*

data class VisualActionChip(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val promptPrefix: String
)

@Composable
fun VisualSearchScreen(
    onAnalyzeSelection: (String, Rect?) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val drawnPath = remember { mutableStateListOf<Offset>() }
    var selectedRect by remember { mutableStateOf<Rect?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var analysisResult by remember { mutableStateOf<String?>(null) }

    val actionChips = remember {
        listOf(
            VisualActionChip("Explain This", Icons.Rounded.Lightbulb, "Explain what is shown in this selected screen area: "),
            VisualActionChip("Translate", Icons.Rounded.Translate, "Translate all text detected in this selected screen area: "),
            VisualActionChip("Extract Text", Icons.Rounded.DocumentScanner, "Extract and digitize all visible text in this selection: "),
            VisualActionChip("Solve Math", Icons.Rounded.Calculate, "Solve the equation or scientific problem shown in this selection step-by-step: "),
            VisualActionChip("Search with VYOM", Icons.Rounded.Search, "Search information and verified sources about this subject: "),
            VisualActionChip("Find Similar", Icons.Rounded.ImageSearch, "Find visually similar items, articles, and references for: ")
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0x99111827)) // Translucent screen overlay
    ) {
        // Canvas for drawing gesture to circle/select area
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            drawnPath.clear()
                            drawnPath.add(offset)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            drawnPath.add(change.position)
                        },
                        onDragEnd = {
                            if (drawnPath.size > 5) {
                                val minX = drawnPath.minOf { it.x }
                                val maxX = drawnPath.maxOf { it.x }
                                val minY = drawnPath.minOf { it.y }
                                val maxY = drawnPath.maxOf { it.y }
                                selectedRect = Rect(minX, minY, maxX, maxY)
                            }
                        }
                    )
                }
        ) {
            // Draw path user traced with finger
            if (drawnPath.isNotEmpty()) {
                val path = Path().apply {
                    moveTo(drawnPath.first().x, drawnPath.first().y)
                    for (point in drawnPath) {
                        lineTo(point.x, point.y)
                    }
                }
                drawPath(
                    path = path,
                    color = Color(0xFF60A5FA),
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Draw highlighted boundary if selected
            selectedRect?.let { rect ->
                drawRect(
                    color = Color(0x333B82F6),
                    topLeft = rect.topLeft,
                    size = rect.size
                )
                drawRect(
                    color = Color(0xFF60A5FA),
                    topLeft = rect.topLeft,
                    size = rect.size,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        // Top Controls: Exit & Instruction
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = VyomWhite,
                border = androidx.compose.foundation.BorderStroke(1.dp, VyomBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    VyomOrb(size = 20.dp, state = OrbState.SEARCHING)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "VYOM Screen Intelligence",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        color = VyomTextPrimary
                    )
                }
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(VyomWhite)
                    .testTag("visual_search_close")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Close",
                    tint = VyomTextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Center Hint (if user hasn't circled yet)
        if (drawnPath.isEmpty() && selectedRect == null && analysisResult == null) {
            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp),
                shape = RoundedCornerShape(20.dp),
                color = VyomWhite.copy(alpha = 0.95f),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Gesture,
                        contentDescription = null,
                        tint = VyomCelestialBlue,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Circle or tap anything on screen",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = VyomTextPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Draw around any text, image, product, or math formula to search or translate instantly.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                        color = VyomTextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Result Card if analyzed
        if (analysisResult != null) {
            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.9f)
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                color = VyomWhite,
                shadowElevation = 10.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        VyomOrb(size = 28.dp, state = OrbState.IDLE)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Visual Intelligence Analysis",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = VyomTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = analysisResult ?: "",
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = VyomTextPrimary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            drawnPath.clear()
                            selectedRect = null
                            analysisResult = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VyomCosmicIndigo)
                    ) {
                        Text("Analyze Another Area", color = VyomWhite)
                    }
                }
            }
        }

        // Bottom Action Bar: Action Chips
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding(),
            color = VyomWhite,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            shadowElevation = 12.dp
        ) {
            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                Text(
                    text = if (selectedRect != null) "Area selected • Choose action:" else "Tap an action or circle screen:",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = VyomTextSecondary
                    ),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(actionChips) { chip ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = VyomSurfaceContainer,
                            border = androidx.compose.foundation.BorderStroke(1.dp, VyomBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    isAnalyzing = true
                                    analysisResult = "Analysis for [${chip.title}]: Selected visual subjects processed. Identified key data, structural composition, and actionable insights."
                                    onAnalyzeSelection(chip.promptPrefix, selectedRect)
                                }
                                .testTag("visual_chip_${chip.title.lowercase().replace(" ", "_")}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = chip.icon,
                                    contentDescription = chip.title,
                                    tint = VyomCosmicIndigo,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = chip.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = VyomTextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
