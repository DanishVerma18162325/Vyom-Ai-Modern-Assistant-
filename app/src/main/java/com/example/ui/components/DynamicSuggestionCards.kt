package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ShortText
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import java.util.Calendar

data class SuggestionItem(
    val title: String,
    val subtitle: String,
    val prompt: String,
    val icon: ImageVector,
    val iconTint: Color
)

@Composable
fun DynamicSuggestionCards(
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }

    val suggestions = remember(currentHour) {
        val baseList = mutableListOf<SuggestionItem>()

        // Time-contextual hero suggestions
        when (currentHour) {
            in 5..11 -> {
                baseList.add(SuggestionItem("Today's Briefing", "Weather, calendar & focus", "Give me my morning briefing with weather and agenda", Icons.Rounded.WbSunny, Color(0xFFD97706)))
                baseList.add(SuggestionItem("Plan My Day", "Schedule priority tasks", "Help me plan my day with high priority focus tasks", Icons.Rounded.EventNote, Color(0xFF2563EB)))
            }
            in 12..17 -> {
                baseList.add(SuggestionItem("Summarize Something", "Documents, articles, notes", "Summarize the key points of: ", Icons.AutoMirrored.Rounded.ShortText, Color(0xFF0D9488)))
                baseList.add(SuggestionItem("Start Study Mode", "Quizzes, math, science", "Open study mode for practice and concept explanation", Icons.Rounded.School, Color(0xFF7C3AED)))
            }
            else -> {
                baseList.add(SuggestionItem("Summarize My Day", "Wrap up insights", "Summarize what we worked on today and prepare for tomorrow", Icons.Rounded.NightsStay, Color(0xFF4338CA)))
                baseList.add(SuggestionItem("Set Tomorrow's Reminder", "Alarm & schedule", "Remind me tomorrow at 8 AM to review my goals", Icons.Rounded.Alarm, Color(0xFFEA580C)))
            }
        }

        // Standard core capabilities
        baseList.add(SuggestionItem("Ask Anything", "Infinite reasoning", "Explain how quantum entanglement works in simple terms", Icons.Rounded.AutoAwesome, Color(0xFF4F46E5)))
        baseList.add(SuggestionItem("Analyze an Image", "Point camera or upload", "Can you explain what's in this picture?", Icons.Rounded.CenterFocusWeak, Color(0xFF0284C7)))
        baseList.add(SuggestionItem("Talk to Vyom", "Real-time live voice", "Hey Vyom, let's have a voice conversation", Icons.Rounded.Mic, Color(0xFF059669)))
        baseList.add(SuggestionItem("Search the Web", "Fresh verified facts", "Search the web for the latest space exploration discoveries", Icons.Rounded.Search, Color(0xFF2563EB)))
        baseList.add(SuggestionItem("Create an Image", "AI poster & wallpaper", "Generate image of a futuristic floating observatory in deep cosmic space", Icons.Rounded.Palette, Color(0xFF9333EA)))

        baseList
    }

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(suggestions) { item ->
            Surface(
                modifier = Modifier
                    .width(180.dp)
                    .height(112.dp)
                    .testTag("suggestion_${item.title.lowercase().replace(" ", "_")}")
                    .clickable { onSuggestionClick(item.prompt) },
                shape = RoundedCornerShape(20.dp),
                color = VyomWhite,
                tonalElevation = 1.dp,
                shadowElevation = 0.5.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, VyomBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(10.dp),
                            color = item.iconTint.copy(alpha = 0.12f)
                        ) {}
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = item.iconTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.5.sp,
                                letterSpacing = (-0.1).sp
                            ),
                            color = VyomTextPrimary,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp
                            ),
                            color = VyomTextTertiary,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
