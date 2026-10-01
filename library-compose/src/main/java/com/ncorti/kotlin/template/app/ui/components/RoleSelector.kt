package com.ncorti.kotlin.template.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.ncorti.kotlin.template.app.ui.theme.AppBackground
import com.ncorti.kotlin.template.app.ui.theme.AppTextStyles
import com.ncorti.kotlin.template.app.ui.theme.AppWhite
import com.ncorti.kotlin.template.app.ui.theme.MarkerYellow
import com.ncorti.kotlin.template.library.domain.enums.RoleType

@Composable
fun RoleSelector(
    options: List<RoleOption>,
    selectedRole: RoleType,
    onRoleSelected: (RoleType) -> Unit,
    modifier: Modifier = Modifier
) {
    require(options.isNotEmpty()) { "RoleSelector requires at least one role." }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(MarkerYellow),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEachIndexed { index, option ->
            val selected = option.type == selectedRole
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .background(if (selected) MarkerYellow else MarkerYellow.copy(alpha = 0.7f))
                    .semantics { this.selected = selected }
                    .clickable(role = Role.RadioButton) { onRoleSelected(option.type) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option.label,
                    style = AppTextStyles.labelsScroll,
                    color = if (selected && options.size > 1) AppWhite else AppBackground
                )
            }
            if (index < options.lastIndex) {
                Box(Modifier.width(3.dp).height(48.dp).background(AppBackground))
            }
        }
    }
}
