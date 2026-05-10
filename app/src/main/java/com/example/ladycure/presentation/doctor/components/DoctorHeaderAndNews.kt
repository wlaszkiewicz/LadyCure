package com.example.ladycure.presentation.doctor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.SubcomposeAsyncImage
import com.example.ladycure.ui.theme.DefaultOnPrimary
import com.example.ladycure.ui.theme.DefaultPrimary
import com.example.ladycure.ui.theme.rememberResponsiveDimens
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DoctorHeader(
    doctorData: Map<String, Any>?,
    unreadNotificationsCount: Int,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
) {
    val dimens = rememberResponsiveDimens()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = dimens.w(0.039f),
                end = dimens.w(0.039f),
                top = dimens.h(0.017f),
                bottom = dimens.h(0.015f)
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Welcome Dr. ${doctorData?.get("name") ?: ""}",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = DefaultPrimary
            )
            Text(
                text = LocalDate.now()
                    .format(DateTimeFormatter.ofPattern("EEEE, MMMM d")),
                style = MaterialTheme.typography.bodyMedium,
                color = DefaultOnPrimary.copy(alpha = 0.6f)
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = {
                    onNotificationClick()
                },
                modifier = Modifier.height(dimens.h(0.044f))
            ) {
                if (unreadNotificationsCount > 0) {
                    BadgedBox(
                        badge = {
                            Badge(containerColor = DefaultPrimary) {
                                Text(
                                    text = unreadNotificationsCount.toString(),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = "Notifications",
                            tint = DefaultPrimary,
                            modifier = Modifier.size(dimens.w(0.068f))
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = DefaultPrimary,
                        modifier = Modifier.size(dimens.w(0.068f))
                    )
                }

            }


            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(dimens.w(0.136f))
                    .clip(CircleShape)
                    .background(DefaultPrimary.copy(alpha = 0.2f))
                    .clickable { onProfileClick() },
                contentAlignment = Alignment.Center
            ) {
                val profileUrl = doctorData?.get("profilePictureUrl") as? String
                if (profileUrl != null) {
                    SubcomposeAsyncImage(
                        model = profileUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        loading = {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = DefaultPrimary
                            )
                        },
                        error = {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Profile",
                                tint = DefaultPrimary
                            )
                        }
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Profile",
                        tint = DefaultPrimary,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

data class NewsItemData(
    val title: String,
    val summary: String,
    val category: String,
    val imageUrl: String,
    val time: String
)

@OptIn(ExperimentalPagerApi::class)
@Composable
fun NewsCarousel(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val dimens = rememberResponsiveDimens()
    val pagerState = rememberPagerState()
    val newsItems = listOf(
        NewsItemData(
            title = "New Guidelines for Diabetes Management",
            summary = "The ADA has released updated guidelines emphasizing personalized treatment plans...",
            category = "Endocrinology",
            imageUrl = "https://sa1s3optim.patientpop.com/assets/images/provider/photos/2638535.jpg",
            time = "2 hours ago"
        ),
        NewsItemData(
            title = "New Advances in Cancer Treatment",
            summary = "Recent studies show promising results in immunotherapy for breast cancer...",
            category = "Oncology",
            imageUrl = "https://www.oregoncancer.com/hubfs/Pros%20and%20Cons%20of%20Treating%20Cancer%20with%20Radiation%20Therapy%20%281%29.jpg",
            time = "1 hour ago"
        ),
        NewsItemData(
            title = "Cardiology Breakthroughs in 2026",
            summary = "New techniques in heart surgery are improving patient outcomes significantly...",
            category = "Cardiology",
            imageUrl = "https://reverehealth.com/_next/image/?url=https%3A%2F%2Fcms.reverehealth.com%2Fwp-content%2Fuploads%2F2022%2F02%2FCardiological_Technology_Advancements-scaled.jpeg&w=2048&q=75",
            time = "3 hours ago"
        )
    )

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Medical News & Updates",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = DefaultPrimary
                )
            )
            Text(
                text = "Today, ${LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d"))}",
                style = MaterialTheme.typography.bodySmall,
                color = DefaultOnPrimary.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(dimens.h(0.017f)))

        HorizontalPager(
            count = newsItems.size,
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(dimens.h(0.197f))
        ) { page ->
            val item = newsItems[page]
            NewsCard(
                title = item.title,
                summary = item.summary,
                category = item.category,
                imageUrl = item.imageUrl,
                time = item.time,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                onClick = { /* Handle click */ }
            )
        }

        HorizontalPagerIndicator(
            pagerState = pagerState,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(
                    start = dimens.w(0.039f),
                    end = dimens.w(0.039f),
                    top = dimens.h(0.017f),
                    bottom = dimens.h(0f)
                ),
            activeColor = DefaultPrimary,
            inactiveColor = DefaultPrimary.copy(alpha = 0.2f)
        )
    }
}

@Composable
fun NewsCard(
    title: String,
    summary: String,
    category: String,
    imageUrl: String,
    time: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val dimens = rememberResponsiveDimens()
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = dimens.w(0.039f), vertical = dimens.h(0.017f)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(dimens.w(0.243f))
                    .clip(RoundedCornerShape(12.dp))
            ) {
                SubcomposeAsyncImage(
                    model = imageUrl,
                    contentDescription = "News image",
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(DefaultPrimary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = DefaultPrimary)
                        }
                    },
                    error = {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Error loading image",
                            tint = DefaultPrimary,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(DefaultPrimary.copy(alpha = 0.1f))
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.width(dimens.w(0.039f)))

            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(DefaultPrimary.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.labelSmall,
                        color = DefaultPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = time,
                        style = MaterialTheme.typography.labelSmall,
                        color = DefaultOnPrimary.copy(alpha = 0.5f)
                    )

                    TextButton(
                        onClick = onClick,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            "Read more",
                            style = MaterialTheme.typography.labelSmall,
                            color = DefaultPrimary
                        )
                    }
                }
            }
        }
    }
}
