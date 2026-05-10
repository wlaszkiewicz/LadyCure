package com.example.ladycure.presentation.booking

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.example.ladycure.domain.model.Referral
import com.example.ladycure.ui.theme.BabyBlue
import com.example.ladycure.ui.theme.DefaultPrimary
import com.example.ladycure.ui.theme.Red
import com.example.ladycure.ui.theme.rememberResponsiveDimens
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReferralInfoCard(
    referral: Referral?,
    onUploadNew: () -> Unit,
    modifier: Modifier = Modifier,
    isUploading: Boolean = false,
    uploadProgress: Float = 0f,
    showUploadSuccess: Boolean = false
) {
    val dimens = rememberResponsiveDimens()
    val referralUrl = referral?.url
    val serviceName = referral?.service
    val uploadDate = referral?.uploadedAt
    val context = LocalContext.current
    val pdfIconPainter = rememberVectorPainter(Icons.Default.PictureAsPdf)
    val fileSize = remember(referralUrl) { calculateFileSize(context, referralUrl) }

    var showSuccessMessage by remember { mutableStateOf(false) }
    LaunchedEffect(showUploadSuccess) {
        if (showUploadSuccess) {
            showSuccessMessage = true
            delay(2000) // Show for 2 seconds
            showSuccessMessage = false
        }
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = Color.Black
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = dimens.w(16 / 411f),
                vertical = dimens.h(16 / 914f)
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Referral Document",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(dimens.h(12 / 914f)))

            if (isUploading) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LinearProgressIndicator(
                        progress = { uploadProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = BabyBlue,
                        trackColor = BabyBlue.copy(alpha = 0.2f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Uploading... ${(uploadProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = BabyBlue,
                    )
                }
            } else if (showSuccessMessage) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(BabyBlue.copy(alpha = 0.1f))
                        .padding(dimens.w(12 / 411f)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = BabyBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Upload successful!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = BabyBlue,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(dimens.h(12 / 914f)))
            }

            if (!isUploading && referralUrl != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(dimens.w(12 / 411f)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = pdfIconPainter,
                        contentDescription = "PDF",
                        tint = Red.copy(alpha = 0.8f),
                        modifier = Modifier.size(dimens.w(40 / 411f))
                    )

                    Spacer(modifier = Modifier.width(dimens.w(12 / 411f)))

                    Column {
                        Text(
                            text = "Referral.pdf",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = fileSize ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(
                        onClick = { openPdf(context, referralUrl) },
                        modifier = Modifier.size(dimens.w(36 / 411f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "View",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "View",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(dimens.h(12 / 914f)))

                Column(
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    InfoRow(
                        icon = Icons.Default.Badge,
                        label = "For service:",
                        value = serviceName ?: "Not specified"
                    )

                    InfoRow(
                        icon = Icons.Default.DateRange,
                        label = "Uploaded:",
                        value = uploadDate?.let {
                            SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
                                .format(Date(it))
                        } ?: "Unknown date"
                    )
                }
            } else if (!isUploading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimens.h(100 / 914f))
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No referral uploaded",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimens.h(16 / 914f)))

            Button(
                onClick = onUploadNew,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isUploading) Color.LightGray else DefaultPrimary.copy(alpha = 0.1f),
                    contentColor = if (isUploading) Color.Gray else DefaultPrimary
                ),
                enabled = !isUploading,
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp
                )
            ) {
                Icon(
                    imageVector = if (referralUrl == null) Icons.Default.Upload else Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (referralUrl == null) "Upload Referral" else "Change Document",
                    color = if (isUploading) Color.Gray else DefaultPrimary
                )
            }
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun calculateFileSize(context: Context, uriString: String?): String? {
    if (uriString == null) return null

    return try {
        val uri = uriString.toUri()
        val file = when (uri.scheme) {
            "content" -> {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                        it.getString(sizeIndex)
                    } else null
                }
            }

            "file" -> File(uri.path!!).length().toString()
            else -> null
        }

        file?.let {
            val sizeBytes = it.toLong()
            when {
                sizeBytes >= 1_000_000 -> "${sizeBytes / 1_000_000} MB"
                sizeBytes >= 1_000 -> "${sizeBytes / 1_000} KB"
                else -> "$sizeBytes bytes"
            }
        }
    } catch (e: Exception) {
        null
    }
}

private fun openPdf(context: Context, pdfUrl: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(pdfUrl.toUri(), "application/pdf")
            setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NO_HISTORY)
        }
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No PDF viewer installed", Toast.LENGTH_SHORT).show()
    }
}
