package com.example.yesiwant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MainMenuPanel(
    modifier: Modifier = Modifier,
    menuItems: List<String>,
    selectedIndex: Int
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF161616), Color(0xFF0D0D10))
                )
            )
            .padding(start = 32.dp, top = 24.dp, end = 32.dp, bottom = 24.dp)
    ) {
        // The Top Left Icon (Using a simple text placeholder for now)
        Text(
            text = "e9", // The logo in your image
            color = Color.White,
            fontSize = 18.sp,
            modifier = Modifier
                .background(Color.White.copy(alpha = 0.2f), shape = CircleShape)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // The Menu List
        menuItems.forEachIndexed { index, title ->
            val isSelected = index == selectedIndex

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text(
                    text = title,
                    color = if (isSelected) Color.White else Color(0xFF888888),
                    fontSize = 20.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )

                // The Blue Selection Dot
                if (isSelected) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF244BCB))
                    )
                }
            }
        }
    }
}