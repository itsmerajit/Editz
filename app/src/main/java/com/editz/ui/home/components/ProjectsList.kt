package com.editz.ui.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.editz.theme.EditzColors

@Composable
fun ProjectsList(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Project",
            style = MaterialTheme.typography.titleMedium,
            color = EditzColors.TextPrimary
        )
        Text(
            text = "Past 30 days",
            style = MaterialTheme.typography.bodyMedium,
            color = EditzColors.TextSecondary,
            modifier = Modifier.padding(top = 4.dp)
        )
        LazyColumn(
            modifier = Modifier.padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Project items will be added here
        }
    }
} 