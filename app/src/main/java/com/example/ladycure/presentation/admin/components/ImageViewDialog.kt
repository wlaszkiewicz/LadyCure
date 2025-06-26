package com.example.ladycure.presentation.admin.components

import DefaultPrimary
import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import coil.compose.AsyncImage

/**
 * A dialog that displays an image loaded from a URL with a title,
 * and provides options to download the image or close the dialog.
 *
 * @param imageUrl The URL of the image to display.
 * @param title The title to display at the top of the dialog and used as the download filename.
 * @param onDismiss Callback invoked when the dialog should be dismissed.
 */
@Composable
fun ImageViewDialog(
    imageUrl: String,
    title: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = DefaultPrimary
                        )
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        IconButton(onClick = {
                            downloadFile(
                                context = context,
                                url = imageUrl,
                                fileName = title + ".jpg",
                                title = title,
                                description = "Downloading $title"
                            )
                        }) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download",
                                tint = DefaultPrimary
                            )
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = DefaultPrimary
                            )
                        }
                    }
                }

                AsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    modifier = Modifier
                        .fillMaxHeight(0.8f)
                        .clip(RoundedCornerShape(12.dp)),
                )


            }
        }
    }
}

/**
 * Initiates a download of a file from the provided URL using Android's DownloadManager.
 *
 * The downloaded file will be saved to the device's public Downloads directory.
 * A notification will be shown when the download completes.
 *
 * @param context The Android context required to access system services.
 * @param url The URL of the file to download.
 * @param fileName The name to use for the downloaded file.
 * @param title The title to display in the download notification.
 * @param description The description to display in the download notification.
 */
fun downloadFile(
    context: Context,
    url: String,
    fileName: String,
    title: String,
    description: String
) {
    val request = DownloadManager.Request(url.toUri())
        .apply {
            setTitle(title)
            setDescription(description)
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                fileName
            )
            setAllowedOverMetered(true)
            setAllowedOverRoaming(true)
        }
    val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    dm.enqueue(request)
}
