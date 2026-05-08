package com.example.ladycure.presentation.login.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.ladycure.R
import com.example.ladycure.ui.theme.DefaultPrimary
import com.example.ladycure.ui.theme.rememberResponsiveDimens

@Composable
fun NonWomanWelcomeDialog(
    onContinue: () -> Unit,
    onUninstall: () -> Unit,
    onDismiss: () -> Unit
) {
    val dimens = rememberResponsiveDimens()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Important Information",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = dimens.h(0.018f)),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.akczuali_kapi),
                        contentDescription = "Welcome illustration",
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .zIndex(1f)
                            .height(dimens.h(0.186f))
                            .offset(y = (0).dp),
                        contentScale = ContentScale.Fit
                    )

                    Card(
                        modifier = Modifier
                            .width(dimens.w(0.681f))
                            .padding(top = dimens.h(0.186f)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Text(
                            text = "Our app is designed primarily for women's health needs. " +
                                    "However, if you still find our services helpful, you're welcome to continue.",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(
                                vertical = dimens.h(0.022f),
                                horizontal = dimens.w(0.039f)
                            ),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Button(
                    onClick = onContinue,
                    modifier = Modifier.width(dimens.w(0.487f)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DefaultPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text("Continue to App")
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Continue",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onUninstall,
                    modifier = Modifier.width(dimens.w(0.487f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = DefaultPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text("Not for me")
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Not for me",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {},
        modifier = Modifier.wrapContentSize()
    )
}