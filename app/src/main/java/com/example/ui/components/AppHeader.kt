package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CountryOption
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppHeader(
    selectedCountry: CountryOption,
    availableCountries: List<CountryOption>,
    onCountrySelected: (CountryOption) -> Unit,
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var countryMenuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Search bar pill
        Row(
            modifier = Modifier
                .weight(1f, fill = false)
                .widthIn(max = 380.dp)
                .background(Color(0xFFF8FAFC), RoundedCornerShape(24.dp))
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
                .clickable { onSearchClick() }
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Search analytics, keywords...",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextMuted,
                    fontSize = 13.sp
                ),
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Right side controls: Country Selector, Notifications, User profile
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Country Selector Dropdown
            Box {
                Row(
                    modifier = Modifier
                        .background(Color(0xFFF1F5F9), RoundedCornerShape(20.dp))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                        .clickable { countryMenuExpanded = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = selectedCountry.flagEmoji, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = selectedCountry.code,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Select country",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                DropdownMenu(
                    expanded = countryMenuExpanded,
                    onDismissRequest = { countryMenuExpanded = false }
                ) {
                    availableCountries.forEach { country ->
                        DropdownMenuItem(
                            text = {
                                Text("${country.flagEmoji} ${country.name} (${country.code})")
                            },
                            onClick = {
                                onCountrySelected(country)
                                countryMenuExpanded = false
                            }
                        )
                    }
                }
            }

            // Notification Bell
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                // Unread dot
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(PrimaryBlue, CircleShape)
                        .align(Alignment.TopEnd)
                )
            }

            // Profile Avatar (JD John Doe)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onProfileClick() }
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(PrimaryBlue, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "JD",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "John Doe",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
