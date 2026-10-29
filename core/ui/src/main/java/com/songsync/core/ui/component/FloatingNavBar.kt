package com.songsync.core.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.songsync.core.ui.theme.PillShape
import com.songsync.core.ui.theme.SongSyncMotion
import com.songsync.core.ui.theme.bouncyClickable
import com.songsync.core.ui.theme.isAnimationDisabled

data class NavItem(
    val title: String,
    val iconOutlined: ImageVector,
    val iconFilled: ImageVector
)

@Composable
fun FloatingNavBar(
    items: List<NavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val noAnim = isAnimationDisabled()
    val shadowColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .height(72.dp)
            .shadow(
                elevation = 8.dp,
                shape = PillShape,
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .clip(PillShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = index == selectedIndex
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.05f else 1.0f,
                    animationSpec = if (noAnim) SongSyncMotion.EffectsFast else SongSyncMotion.SpatialDefault,
                    label = "navScale"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(vertical = 10.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent
                        )
                        .bouncyClickable { onItemSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSelected) item.iconFilled else item.iconOutlined,
                        contentDescription = item.title,
                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}
