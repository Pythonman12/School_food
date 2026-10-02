package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NoMeals
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.AllergySettingsDialog
import com.example.ui.components.DateBar
import com.example.ui.components.DatePickerHelper
import com.example.ui.components.FavoriteFoodDialog
import com.example.ui.components.MealCard
import com.example.ui.components.MealHeader
import com.example.ui.components.ReviewDialog
import com.example.ui.components.ReviewHistoryView
import com.example.ui.components.SchoolInfoDialog
import com.example.ui.components.WeeklyMealView
import com.example.util.DateUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealScreen(
    viewModel: MealViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val viewMode by viewModel.viewMode.collectAsStateWithLifecycle()
    val dailyState by viewModel.dailyState.collectAsStateWithLifecycle()
    val weeklyState by viewModel.weeklyState.collectAsStateWithLifecycle()
    val showAllergyTags by viewModel.showAllergyTags.collectAsStateWithLifecycle()
    val currentReview by viewModel.currentReview.collectAsStateWithLifecycle()
    val allReviews by viewModel.allReviews.collectAsStateWithLifecycle()
    val favoriteKeywords by viewModel.favoriteKeywords.collectAsStateWithLifecycle()
    val userAllergies by viewModel.userAllergies.collectAsStateWithLifecycle()

    var showSchoolInfoDialog by remember { mutableStateOf(false) }
    var showAllergyDialog by remember { mutableStateOf(false) }
    var showFavoriteDialog by remember { mutableStateOf(false) }
    var showReviewDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "대진전자통신고 급식",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        )
                    }
                },
                actions = {
                    // Allergy settings icon
                    IconButton(
                        onClick = { showAllergyDialog = true },
                        modifier = Modifier.testTag("allergy_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "알레르기 설정",
                            tint = if (userAllergies.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Favorite foods icon
                    IconButton(
                        onClick = { showFavoriteDialog = true },
                        modifier = Modifier.testTag("favorite_foods_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "최애 메뉴 설정",
                            tint = if (favoriteKeywords.isNotEmpty()) Color(0xFFFF8F00) else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Refresh icon
                    IconButton(
                        onClick = {
                            viewModel.refresh()
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("급식 정보를 새로고침했습니다.")
                            }
                        },
                        modifier = Modifier.testTag("refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "새로고침"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = viewMode == ViewMode.DAILY,
                    onClick = { viewModel.setViewMode(ViewMode.DAILY) },
                    icon = { Icon(Icons.Default.Restaurant, contentDescription = null) },
                    label = { Text("일간 식단") },
                    modifier = Modifier.testTag("tab_daily")
                )
                NavigationBarItem(
                    selected = viewMode == ViewMode.WEEKLY,
                    onClick = { viewModel.setViewMode(ViewMode.WEEKLY) },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    label = { Text("주간 식단표") },
                    modifier = Modifier.testTag("tab_weekly")
                )
                NavigationBarItem(
                    selected = viewMode == ViewMode.REVIEWS,
                    onClick = { viewModel.setViewMode(ViewMode.REVIEWS) },
                    icon = { Icon(Icons.Default.RateReview, contentDescription = null) },
                    label = { Text("식단 다이어리") },
                    modifier = Modifier.testTag("tab_reviews")
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (viewMode) {
                ViewMode.DAILY -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("daily_meal_scroll_list")
                    ) {
                        item {
                            MealHeader(
                                onSchoolInfoClick = { showSchoolInfoDialog = true }
                            )
                        }

                        item {
                            DateBar(
                                currentDateYmd = selectedDate,
                                onPrevClick = { viewModel.goToPrevDay() },
                                onNextClick = { viewModel.goToNextDay() },
                                onTodayClick = { viewModel.goToToday() },
                                onTomorrowClick = { viewModel.goToTomorrow() },
                                onCalendarClick = {
                                    DatePickerHelper.showDatePicker(context, selectedDate) { ymd ->
                                        viewModel.selectDate(ymd)
                                    }
                                }
                            )
                        }

                        // Allergy toggle chip & allergy count indicator
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FilterChip(
                                    selected = showAllergyTags,
                                    onClick = { viewModel.toggleShowAllergyTags() },
                                    label = {
                                        Text(
                                            text = if (showAllergyTags) "알레르기 태그 표시중" else "알레르기 숨김",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    leadingIcon = if (showAllergyTags) {
                                        {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("toggle_allergy_chip")
                                )

                                if (userAllergies.isNotEmpty()) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Warning,
                                                contentDescription = null,
                                                modifier = Modifier.size(12.dp),
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "내 알레르기 ${userAllergies.size}종 감시중",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onErrorContainer,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // State content
                        when (val state = dailyState) {
                            is MealUiState.Loading -> {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(260.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            CircularProgressIndicator()
                                            Spacer(modifier = Modifier.height(14.dp))
                                            Text(
                                                text = "대진전자통신고 급식을 나이스(NEIS)에서 불러오는 중...",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            is MealUiState.Error -> {
                                item {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(20.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "급식 정보를 불러올 수 없습니다",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = state.message,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onErrorContainer,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(14.dp))
                                            Button(onClick = { viewModel.refresh() }) {
                                                Text("다시 시도")
                                            }
                                        }
                                    }
                                }
                            }

                            is MealUiState.Empty -> {
                                item {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        shape = RoundedCornerShape(20.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(28.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.NoMeals,
                                                contentDescription = null,
                                                modifier = Modifier.size(56.dp),
                                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(
                                                text = "급식이 없는 날입니다",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = state.message,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }

                            is MealUiState.Success -> {
                                items(state.meals) { meal ->
                                    MealCard(
                                        meal = meal,
                                        favoriteKeywords = favoriteKeywords,
                                        userAllergies = userAllergies,
                                        showAllergyTags = showAllergyTags,
                                        currentReview = currentReview,
                                        onOpenReviewDialog = { showReviewDialog = true }
                                    )
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }

                ViewMode.WEEKLY -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        MealHeader(
                            onSchoolInfoClick = { showSchoolInfoDialog = true }
                        )

                        WeeklyMealView(
                            state = weeklyState,
                            currentDateYmd = selectedDate,
                            favoriteKeywords = favoriteKeywords,
                            userAllergies = userAllergies,
                            onDaySelected = { ymd ->
                                viewModel.selectDate(ymd)
                                viewModel.setViewMode(ViewMode.DAILY)
                            },
                            onPrevWeekClick = {
                                val prevWeekYmd = DateUtils.offsetDate(selectedDate, -7)
                                viewModel.selectDate(prevWeekYmd)
                                viewModel.loadWeeklyMeals(prevWeekYmd)
                            },
                            onNextWeekClick = {
                                val nextWeekYmd = DateUtils.offsetDate(selectedDate, 7)
                                viewModel.selectDate(nextWeekYmd)
                                viewModel.loadWeeklyMeals(nextWeekYmd)
                            }
                        )
                    }
                }

                ViewMode.REVIEWS -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 12.dp)
                    ) {
                        ReviewHistoryView(
                            reviews = allReviews,
                            onSelectDate = { ymd ->
                                viewModel.selectDate(ymd)
                                viewModel.setViewMode(ViewMode.DAILY)
                            },
                            onDeleteReview = { date ->
                                viewModel.selectDate(date)
                                viewModel.deleteReview()
                            }
                        )
                    }
                }
            }
        }
    }

    // Dialogs
    if (showSchoolInfoDialog) {
        SchoolInfoDialog(onDismiss = { showSchoolInfoDialog = false })
    }

    if (showAllergyDialog) {
        AllergySettingsDialog(
            userAllergies = userAllergies,
            onToggleAllergy = { code, enabled -> viewModel.toggleAllergy(code, enabled) },
            onDismiss = { showAllergyDialog = false }
        )
    }

    if (showFavoriteDialog) {
        FavoriteFoodDialog(
            favorites = favoriteKeywords,
            onAddFavorite = { kw -> viewModel.addFavorite(kw) },
            onRemoveFavorite = { kw -> viewModel.removeFavorite(kw) },
            onDismiss = { showFavoriteDialog = false }
        )
    }

    if (showReviewDialog) {
        ReviewDialog(
            dateYmd = selectedDate,
            existingReview = currentReview,
            onSave = { rating, memo -> viewModel.saveReview(rating, memo) },
            onDelete = { viewModel.deleteReview() },
            onDismiss = { showReviewDialog = false }
        )
    }
}
