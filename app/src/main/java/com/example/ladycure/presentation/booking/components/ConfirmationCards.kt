package com.example.ladycure.presentation.booking

import android.content.Intent
import android.location.Geocoder
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.SubcomposeAsyncImage
import com.example.ladycure.domain.model.AppointmentType
import com.example.ladycure.ui.theme.DefaultOnPrimary
import com.example.ladycure.ui.theme.DefaultPrimary
import com.example.ladycure.ui.theme.YellowOrange
import com.example.ladycure.ui.theme.rememberResponsiveDimens
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import java.util.Locale

@Composable
fun ConfirmationLoadingView() {
    val dimens = rememberResponsiveDimens()
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = DefaultPrimary)
            Spacer(modifier = Modifier.height(dimens.h(16 / 914f)))
            Text("Loading appointment details...", color = DefaultOnPrimary)
        }
    }
}

@Composable
fun AppointmentTypeCard(
    appointmentType: AppointmentType,
    referralId: String?,
    modifier: Modifier = Modifier
) {
    val dimens = rememberResponsiveDimens()
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = appointmentType.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "${appointmentType.durationInMinutes} min",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DefaultPrimary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(DefaultPrimary.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(dimens.h(12 / 914f)))

            Text(
                text = "Service Description",
                style = MaterialTheme.typography.labelLarge,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = appointmentType.additionalInfo,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = dimens.h(12 / 914f))
            )

            Text(
                text = "Preparation Instructions",
                style = MaterialTheme.typography.labelLarge,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = appointmentType.preparationInstructions,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = dimens.h(12 / 914f))
            )

            if (appointmentType.needsReferral && referralId == null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Referral required",
                        tint = YellowOrange,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Medical referral required",
                        style = MaterialTheme.typography.labelMedium,
                        color = YellowOrange
                    )
                }
            }
        }
    }
}

@Composable
fun PaymentCard(
    appointmentType: AppointmentType,
    modifier: Modifier = Modifier
) {
    val dimens = rememberResponsiveDimens()
    val taxRate = 0.09 // 9% tax
    val taxAmount = appointmentType.price * taxRate
    val totalAmount = appointmentType.price + taxAmount

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
            Text(
                text = "Payment Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Service Fee",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "$${"%.2f".format(appointmentType.price)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tax (9%)",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "$${"%.2f".format(taxAmount)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Divider(
                modifier = Modifier.padding(vertical = dimens.h(12 / 914f)),
                color = Color.LightGray,
                thickness = 1.dp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Amount",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$${"%.2f".format(totalAmount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DefaultPrimary
                )
            }
        }
    }
}

@Composable
fun LocationCard(
    doctor: Map<String, Any>,
    modifier: Modifier = Modifier
) {
    val dimens = rememberResponsiveDimens()
    val address = doctor["address"] as? String ?: "Address unavailable"
    val city = doctor["city"] as? String ?: ""
    val fullAddress = "$address, $city"

    val context = LocalContext.current
    val geocoder = Geocoder(context, Locale.getDefault())
    val location = remember(fullAddress) {
        geocoder.getFromLocationName(fullAddress, 1)?.firstOrNull()
    }
    val latLng = location?.let { LatLng(it.latitude, it.longitude) }

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
            Text(
                text = "Location",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = dimens.h(12 / 914f))
            )

            if (latLng != null) {
                GoogleMap(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimens.h(200 / 914f))
                        .clip(RoundedCornerShape(8.dp)),
                    cameraPositionState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(latLng, 15f)
                    },
                    properties = MapProperties(isBuildingEnabled = true),
                    uiSettings = MapUiSettings(zoomControlsEnabled = false)
                ) {
                    Marker(
                        state = MarkerState(position = latLng),
                        title = "Clinic Location",
                        snippet = fullAddress
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimens.h(200 / 914f))
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Loading map...")
                }
            }

            Spacer(modifier = Modifier.height(dimens.h(24 / 914f)))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Address",
                    tint = DefaultPrimary,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Spacer(modifier = Modifier.width(dimens.w(12 / 411f)))
                Column {
                    Text(
                        text = "Clinic Address",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = address,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimens.h(12 / 914f)))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = "Phone",
                    tint = DefaultPrimary,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Spacer(modifier = Modifier.width(dimens.w(12 / 411f)))
                Column {
                    Text(
                        text = "Contact Number",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = doctor["phone"] as? String ?: "Phone unavailable",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val gmmIntentUri = if (latLng != null) {
                        "geo:${latLng.latitude},${latLng.longitude}?q=${fullAddress}".toUri()
                    } else {
                        "geo:0,0?q=${fullAddress}".toUri()
                    }
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    context.startActivity(mapIntent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DefaultPrimary.copy(alpha = 0.1f),
                    contentColor = DefaultPrimary
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Directions,
                    contentDescription = "Directions",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Get Directions")
            }
        }
    }
}

@Composable
fun DoctorConfirmationCard(
    doctor: Map<String, Any>,
    modifier: Modifier = Modifier
) {
    val dimens = rememberResponsiveDimens()
    val name = doctor["name"] as? String ?: "Dr. Unknown"
    val surname = doctor["surname"] as? String ?: "Unknown"
    val specialization = doctor["speciality"] as? String ?: "Specialist"
    val imageUrl = doctor["profilePictureUrl"] as? String ?: ""
    val bio = doctor["bio"] as? String ?: "Experienced medical professional"

    val experience = when (val exp = doctor["experience"]) {
        is Int -> exp
        is Long -> exp.toInt()
        is Double -> exp.toInt()
        is String -> exp.toIntOrNull() ?: 5
        else -> 5
    }

    val rating = when (val rat = doctor["rating"]) {
        is Int -> rat.toDouble()
        is Long -> rat.toDouble()
        is Double -> rat
        is String -> rat.toDouble()
        else -> 4.5
    }


    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = Color.Black
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = dimens.w(16 / 411f), vertical = dimens.h(16 / 914f))
                .fillMaxWidth()
        ) {
            Text(
                text = "Your Doctor",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = dimens.h(16 / 914f))
            ) {
                SubcomposeAsyncImage(
                    model = imageUrl,
                    contentDescription = "Doctor $name",
                    modifier = Modifier
                        .size(dimens.w(80 / 411f))
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = DefaultPrimary
                            )
                        }
                    },
                    error = {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Doctor $name",
                            modifier = Modifier
                                .size(dimens.w(80 / 411f))
                                .clip(CircleShape),
                            tint = Color.Gray
                        )
                    }
                )

                Spacer(modifier = Modifier.width(dimens.w(16 / 411f)))

                Column {
                    Text(
                        text = "Dr. $name $surname",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = specialization,
                        style = MaterialTheme.typography.bodyMedium,
                        color = DefaultPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⭐ $rating",
                            style = MaterialTheme.typography.labelMedium,
                            color = YellowOrange
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "•",
                            color = Color.LightGray
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$experience yrs exp",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Gray
                        )
                    }
                }
            }

            Text(
                text = "About Dr. ${name.split(" ").last()}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = bio,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}
