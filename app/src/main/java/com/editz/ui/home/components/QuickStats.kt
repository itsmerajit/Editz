package com.editz.ui.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.editz.theme.EditzColors

@Composable
fun QuickStats(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = "Quick Stats",
            style = MaterialTheme.typography.titleMedium,
            color = EditzColors.TextPrimary
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatItem(title = "Projects", value = "12")
            StatItem(title = "Storage", value = "2.3 GB")
            StatItem(title = "Exports", value = "45")
        }
    }
}

@Composable
private fun StatItem(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = EditzColors.TextPrimary
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = EditzColors.TextSecondary
        )
    }
} 