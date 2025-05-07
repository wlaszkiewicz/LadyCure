package com.example.ladycure.presentation.admin

import DefaultPrimary
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
 fun AdminTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    onLogout: () -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp, vertical = 16.dp)
        ) {
            Text(
                "Admin Dashboard",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            IconButton(
                onClick = onLogout,
                modifier = Modifier
                    .size(30.dp)
                    .background(
                        DefaultPrimary.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(10.dp)
                    )
            ) {
                Icon(
                    Icons.AutoMirrored.Default.Logout,
                    contentDescription = "Logout",
                    tint = Color.White
                )
            }

        }

        AdminSearchBar(
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchQueryChange
        )

        // Tab selection
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FilterChip(
                selected = selectedTab == "Users",
                onClick = { onTabSelected("Users") },
                label = { Text("Users") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = DefaultPrimary,
                    selectedLabelColor = Color.White
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            FilterChip(
                selected = selectedTab == "Doctors",
                onClick = { onTabSelected("Doctors") },
                label = { Text("Doctors") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = DefaultPrimary,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}