package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.db.UserMealReviewEntity
import com.example.util.DateUtils

@Composable
fun ReviewDialog(
    dateYmd: String,
    existingReview: UserMealReviewEntity?,
    onSave: (rating: Int, memo: String) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    var rating by remember { mutableIntStateOf(existingReview?.rating ?: 5) }
    var memo by remember { mutableStateOf(existingReview?.memo ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "급식 평가 남기기",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = DateUtils.formatToDisplay(dateYmd),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "오늘의 급식은 어떠셨나요?",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Star selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..5) {
                        IconButtonStar(
                            isSelected = i <= rating,
                            onClick = { rating = i }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = memo,
                    onValueChange = { memo = it },
                    label = { Text("한줄평 / 메모") },
                    placeholder = { Text("맛있었던 반찬이나 개선점을 적어보세요.") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("review_memo_input"),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(rating, memo)
                    onDismiss()
                },
                modifier = Modifier.testTag("save_review_button")
            ) {
                Text("저장")
            }
        },
        dismissButton = {
            Row {
                if (existingReview != null) {
                    TextButton(
                        onClick = {
                            onDelete()
                            onDismiss()
                        }
                    ) {
                        Text("삭제", color = MaterialTheme.colorScheme.error)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }
                TextButton(onClick = onDismiss) {
                    Text("취소")
                }
            }
        }
    )
}

@Composable
private fun IconButtonStar(
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Icon(
        imageVector = if (isSelected) Icons.Default.Star else Icons.Default.StarBorder,
        contentDescription = null,
        modifier = Modifier
            .size(36.dp)
            .clickable(onClick = onClick)
            .padding(4.dp),
        tint = Color(0xFFFFB300)
    )
}
