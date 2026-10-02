package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.db.UserMealReviewEntity
import com.example.data.repository.MealRepository
import com.example.domain.model.MealModel
import com.example.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface MealUiState {
    object Loading : MealUiState
    data class Success(val meals: List<MealModel>) : MealUiState
    data class Empty(val date: String, val message: String) : MealUiState
    data class Error(val message: String) : MealUiState
}

sealed interface WeeklyMealUiState {
    object Loading : WeeklyMealUiState
    data class Success(val weekMap: Map<String, List<MealModel>>) : WeeklyMealUiState
    data class Error(val message: String) : WeeklyMealUiState
}

enum class ViewMode {
    DAILY,
    WEEKLY,
    REVIEWS
}

class MealViewModel(
    private val repository: MealRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(DateUtils.getTodayYmd())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _viewMode = MutableStateFlow(ViewMode.DAILY)
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()

    private val _dailyState = MutableStateFlow<MealUiState>(MealUiState.Loading)
    val dailyState: StateFlow<MealUiState> = _dailyState.asStateFlow()

    private val _weeklyState = MutableStateFlow<WeeklyMealUiState>(WeeklyMealUiState.Loading)
    val weeklyState: StateFlow<WeeklyMealUiState> = _weeklyState.asStateFlow()

    private val _showAllergyTags = MutableStateFlow(true)
    val showAllergyTags: StateFlow<Boolean> = _showAllergyTags.asStateFlow()

    private val _currentReview = MutableStateFlow<UserMealReviewEntity?>(null)
    val currentReview: StateFlow<UserMealReviewEntity?> = _currentReview.asStateFlow()

    val allReviews: StateFlow<List<UserMealReviewEntity>> = repository.getAllReviewsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteKeywords: StateFlow<List<String>> = repository.getFavoritesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userAllergies: StateFlow<Set<Int>> = repository.getActiveAllergiesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    init {
        loadDailyMeal(_selectedDate.value)
        loadReviewForDate(_selectedDate.value)
    }

    fun selectDate(ymd: String) {
        _selectedDate.value = ymd
        loadDailyMeal(ymd)
        loadReviewForDate(ymd)
    }

    fun setViewMode(mode: ViewMode) {
        _viewMode.value = mode
        if (mode == ViewMode.WEEKLY) {
            loadWeeklyMeals(_selectedDate.value)
        }
    }

    fun goToToday() {
        selectDate(DateUtils.getTodayYmd())
    }

    fun goToTomorrow() {
        selectDate(DateUtils.offsetDate(_selectedDate.value, 1))
    }

    fun goToPrevDay() {
        selectDate(DateUtils.offsetDate(_selectedDate.value, -1))
    }

    fun goToNextDay() {
        selectDate(DateUtils.offsetDate(_selectedDate.value, 1))
    }

    fun refresh() {
        loadDailyMeal(_selectedDate.value)
        if (_viewMode.value == ViewMode.WEEKLY) {
            loadWeeklyMeals(_selectedDate.value)
        }
    }

    fun toggleShowAllergyTags() {
        _showAllergyTags.value = !_showAllergyTags.value
    }

    private fun loadDailyMeal(date: String) {
        viewModelScope.launch {
            _dailyState.value = MealUiState.Loading
            val result = repository.fetchMealsForDate(date)
            result.onSuccess { meals ->
                if (meals.isEmpty()) {
                    val msg = if (DateUtils.isWeekend(date)) {
                        "주말에는 급식이 운영되지 않습니다."
                    } else {
                        "해당 날짜에 등록된 급식 식단 정보가 없습니다.\n(공휴일, 방학 또는 미급식일)"
                    }
                    _dailyState.value = MealUiState.Empty(date, msg)
                } else {
                    _dailyState.value = MealUiState.Success(meals)
                }
            }.onFailure { err ->
                _dailyState.value = MealUiState.Error(err.localizedMessage ?: "급식 정보를 불러오지 못했습니다.")
            }
        }
    }

    fun loadWeeklyMeals(referenceDate: String) {
        viewModelScope.launch {
            _weeklyState.value = WeeklyMealUiState.Loading
            val weekdays = DateUtils.getWeekdaysForDate(referenceDate)
            if (weekdays.isEmpty()) {
                _weeklyState.value = WeeklyMealUiState.Error("날짜 계산 오류")
                return@launch
            }
            val fromDate = weekdays.first()
            val toDate = weekdays.last()

            val result = repository.fetchMealsForDateRange(fromDate, toDate)
            result.onSuccess { meals ->
                val grouped = meals.groupBy { it.date }
                val weekMap = weekdays.associateWith { ymd -> grouped[ymd] ?: emptyList() }
                _weeklyState.value = WeeklyMealUiState.Success(weekMap)
            }.onFailure { err ->
                _weeklyState.value = WeeklyMealUiState.Error(err.localizedMessage ?: "주간 급식 조회 실패")
            }
        }
    }

    private fun loadReviewForDate(date: String) {
        viewModelScope.launch {
            repository.getReviewForDateFlow(date).collect { review ->
                _currentReview.value = review
            }
        }
    }

    fun saveReview(rating: Int, memo: String) {
        viewModelScope.launch {
            repository.saveReview(_selectedDate.value, rating, memo)
        }
    }

    fun deleteReview() {
        viewModelScope.launch {
            repository.deleteReview(_selectedDate.value)
        }
    }

    fun addFavorite(keyword: String) {
        viewModelScope.launch {
            repository.addFavorite(keyword)
        }
    }

    fun removeFavorite(keyword: String) {
        viewModelScope.launch {
            repository.removeFavorite(keyword)
        }
    }

    fun toggleAllergy(allergenCode: Int, enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleAllergy(allergenCode, enabled)
        }
    }

    class Factory(private val repository: MealRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MealViewModel::class.java)) {
                return MealViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
