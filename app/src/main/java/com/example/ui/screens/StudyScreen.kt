package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.OrbState
import com.example.ui.components.VyomOrb
import com.example.ui.theme.*

data class StudyTopic(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color,
    val samplePrompts: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(
    onLaunchStudyPrompt: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val topics = remember {
        listOf(
            StudyTopic(
                "Mathematics",
                Icons.Rounded.Calculate,
                Color(0xFF2563EB),
                listOf(
                    "Solve step-by-step: 2x² + 5x - 12 = 0",
                    "Explain the fundamental theorem of calculus simply",
                    "Derive Euler's formula and explain its geometric meaning"
                )
            ),
            StudyTopic(
                "Physics",
                Icons.Rounded.Science,
                Color(0xFF7C3AED),
                listOf(
                    "Explain Einstein's General Relativity vs Newton's Gravity",
                    "How does quantum tunneling work in modern microchips?",
                    "Derive Bernoulli's equation with fluid dynamics"
                )
            ),
            StudyTopic(
                "Chemistry",
                Icons.Rounded.Biotech,
                Color(0xFF0D9488),
                listOf(
                    "Balance reaction and explain mechanism: Fe + O2 -> Fe2O3",
                    "Explain orbital hybridization in sp, sp2, and sp3 carbons",
                    "Why does water have anomalous expansion below 4°C?"
                )
            ),
            StudyTopic(
                "Biology",
                Icons.Rounded.Eco,
                Color(0xFF059669),
                listOf(
                    "Explain the light and dark reactions of photosynthesis",
                    "How does CRISPR-Cas9 perform gene editing?",
                    "Detail the stages of cellular mitosis and meiosis"
                )
            ),
            StudyTopic(
                "Languages & Literature",
                Icons.Rounded.Translate,
                Color(0xFFD97706),
                listOf(
                    "Explain Hindi Alankar (उपमा, रूपक, अनुप्रास) with examples",
                    "Active vs Passive voice rules and transformations in English",
                    "Analyze Shakespeare's Sonnet 18 poetic themes"
                )
            ),
            StudyTopic(
                "History & Geography",
                Icons.Rounded.Public,
                Color(0xFFB45309),
                listOf(
                    "Summary of the Indian National Movement from 1857 to 1947",
                    "Explain Plate Tectonics and how the Himalayas formed",
                    "Causes and outcomes of the Industrial Revolution"
                )
            )
        )
    }

    var selectedTopic by remember { mutableStateOf(topics[0]) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = VyomWhite,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        VyomOrb(size = 24.dp, state = OrbState.THINKING)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "VYOM Study",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                ),
                                color = VyomTextPrimary
                            )
                            Text(
                                "Rigorous step-by-step learning & reasoning",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = VyomTextTertiary
                            )
                        }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Horizontal Topics Ribbon
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(topics) { topic ->
                    val isSelected = topic.title == selectedTopic.title
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) topic.color else VyomSurfaceContainer,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) topic.color else VyomBorder),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { selectedTopic = topic }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = topic.icon,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else topic.color,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = topic.title,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else VyomTextPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Topic Details & Prompts
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    // Flashcard / Quiz Generator Callout
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = selectedTopic.color.copy(alpha = 0.08f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, selectedTopic.color.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Quiz & Step-by-Step Problem Solving",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = selectedTopic.color
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Generate interactive practice quizzes or request full derivations for ${selectedTopic.title}.",
                                    fontSize = 12.sp,
                                    color = VyomTextSecondary
                                )
                            }
                            Button(
                                onClick = {
                                    onLaunchStudyPrompt("Generate an interactive 5-question multiple choice quiz on ${selectedTopic.title} with detailed explanations.")
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = selectedTopic.color)
                            ) {
                                Text("Quiz Me", fontSize = 12.sp)
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "EXPLORE FOUNDATIONAL PROBLEMS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = VyomTextTertiary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(selectedTopic.samplePrompts) { prompt ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onLaunchStudyPrompt(prompt) },
                        shape = RoundedCornerShape(16.dp),
                        color = VyomWhite,
                        border = androidx.compose.foundation.BorderStroke(1.dp, VyomBorder),
                        tonalElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Lightbulb,
                                contentDescription = null,
                                tint = selectedTopic.color,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = prompt,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                color = VyomTextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = "Solve",
                                tint = VyomTextTertiary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
