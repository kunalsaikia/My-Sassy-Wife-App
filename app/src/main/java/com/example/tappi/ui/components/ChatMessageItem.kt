package com.example.tappi.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.tappi.R
import com.example.tappi.data.model.Message
import com.example.tappi.data.model.MessageRole
import com.example.tappi.ui.theme.DeepGreen
import com.example.tappi.ui.theme.SassyOrange
import com.example.tappi.ui.theme.SassyTeal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatMessageItem(
    message: Message,
    isSaved: Boolean,
    isPlaying: Boolean,
    userAvatarUri: String?,
    onSaveToggle: () -> Unit,
    onSpeakToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isAssistant = message.role == MessageRole.ASSISTANT
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    val timeFormatted = remember(message.timestamp) {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        sdf.format(Date(message.timestamp))
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = if (isAssistant) Arrangement.Start else Arrangement.End
    ) {
        Column(
            modifier = Modifier.widthIn(max = 340.dp),
            horizontalAlignment = if (isAssistant) Alignment.Start else Alignment.End
        ) {
            // Header: Avatar, Name, Action icons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                if (isAssistant) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, SassyOrange, CircleShape)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.tappi_avatar),
                            contentDescription = "Tappi avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TAPPI",
                        style = MaterialTheme.typography.labelSmall,
                        color = SassyOrange,
                        fontWeight = FontWeight.ExtraBold
                    )
                } else {
                    Text(
                        text = "KUNAL",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(SassyTeal),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!userAvatarUri.isNullOrBlank()) {
                            AsyncImage(
                                model = userAvatarUri,
                                contentDescription = "User avatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = "K",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Message Bubble
            val bubbleShape = if (isAssistant) {
                RoundedCornerShape(topStart = 4.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 20.dp)
            } else {
                RoundedCornerShape(topStart = 20.dp, topEnd = 4.dp, bottomEnd = 20.dp, bottomStart = 20.dp)
            }

            val bubbleModifier = if (isAssistant) {
                Modifier
                    .clip(bubbleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), bubbleShape)
            } else {
                Modifier
                    .clip(bubbleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(SassyTeal, DeepGreen)
                        )
                    )
            }

            Surface(
                modifier = bubbleModifier,
                color = Color.Transparent,
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isAssistant) MaterialTheme.colorScheme.onSurface else Color.White,
                        lineHeight = 22.sp
                    )

                    // Grounding Sources
                    if (isAssistant && message.sources.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "SOURCES & LINKS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SassyOrange,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        message.sources.forEach { source ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                                    .clickable {
                                        try {
                                            uriHandler.openUri(source.uri)
                                        } catch (_: Exception) {}
                                    }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Link,
                                    contentDescription = "Link",
                                    tint = SassyOrange,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = source.title,
                                    fontSize = 11.sp,
                                    color = SassyOrange,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // Bottom Row: Timestamp and Action Buttons
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = timeFormatted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isAssistant) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.7f)
                        )

                        Row(
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isAssistant) {
                                // Speak TTS Button
                                IconButton(
                                    onClick = onSpeakToggle,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .testTag("speak_button_${message.id}")
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Stop else Icons.Outlined.VolumeUp,
                                        contentDescription = if (isPlaying) "Stop speaking" else "Speak message",
                                        tint = if (isPlaying) SassyOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                // Bookmark / Save Wisdom Button
                                IconButton(
                                    onClick = onSaveToggle,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .testTag("bookmark_button_${message.id}")
                                ) {
                                    Icon(
                                        imageVector = if (isSaved) Icons.Filled.Star else Icons.Outlined.BookmarkBorder,
                                        contentDescription = "Save wisdom",
                                        tint = if (isSaved) SassyOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                // Share Button
                                IconButton(
                                    onClick = {
                                        val shareIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, "Tappi's Wisdom: ${message.content}")
                                            type = "text/plain"
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share Tappi's Wisdom"))
                                    },
                                    modifier = Modifier
                                        .size(28.dp)
                                        .testTag("share_button_${message.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Share,
                                        contentDescription = "Share",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // Copy Button
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Chat message", message.content)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Copied to clipboard, honey!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .size(28.dp)
                                    .testTag("copy_button_${message.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = if (isAssistant) MaterialTheme.colorScheme.onSurfaceVariant else Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
