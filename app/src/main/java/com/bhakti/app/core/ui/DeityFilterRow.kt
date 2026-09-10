package com.bhakti.app.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bhakti.app.data.model.Deity

@Composable
fun DeityFilterRow(
    selected: Deity?,
    onSelect: (Deity?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { FilterChip(selected = selected == null, onClick = { onSelect(null) }, label = { Text("All") }) }
        items(Deity.entries) { deity ->
            FilterChip(
                selected = selected == deity,
                onClick = { onSelect(if (selected == deity) null else deity) },
                label = { Text(deity.displayName) }
            )
        }
    }
}
