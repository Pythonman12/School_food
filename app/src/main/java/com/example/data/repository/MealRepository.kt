package com.example.data.repository

import android.util.Log
import com.example.data.api.NeisMealApiService
import com.example.data.api.NetworkClient
import com.example.data.db.AllergyPreferenceEntity
import com.example.data.db.CachedMealEntity
import com.example.data.db.FavoriteFoodEntity
import com.example.data.db.MealDao
import com.example.data.db.UserMealReviewEntity
import com.example.domain.model.MealModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class MealRepository(
    private val apiService: NeisMealApiService,
    private val mealDao: MealDao
) {
    suspend fun fetchMealsForDate(date: String): Result<List<MealModel>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getMealDietInfo(
                mealDate = date
            )
            if (response.isSuccessful) {
                val bodyString = response.body()?.string() ?: ""
                val rawRows = NetworkClient.parseNeisMealJson(bodyString)
                if (rawRows.isNotEmpty()) {
                    val entities = rawRows.map { row ->
                        CachedMealEntity(
                            id = "${row.mealDate}_${row.mealCode}",
                            date = row.mealDate,
                            mealCode = row.mealCode,
                            mealName = row.mealName,
                            rawDishes = row.dishName,
                            calorie = row.calInfo,
                            rawNutrition = row.ntrInfo,
                            originInfo = row.originInfo,
                            mealCount = row.mealCount
                        )
                    }
                    mealDao.insertMeals(entities)
                    return@withContext Result.success(entities.map { it.toDomainModel() })
                } else {
                    // Check if DB already had cache, otherwise empty (e.g. weekend/vacation)
                    val cached = mealDao.getMealsByDate(date)
                    return@withContext Result.success(cached.map { it.toDomainModel() })
                }
            } else {
                // HTTP error -> fallback to cache
                val cached = mealDao.getMealsByDate(date)
                if (cached.isNotEmpty()) {
                    return@withContext Result.success(cached.map { it.toDomainModel() })
                }
                return@withContext Result.failure(Exception("네트워크 응답 오류 (${response.code()})"))
            }
        } catch (e: Exception) {
            Log.e("MealRepository", "Error fetching meals for date: $date", e)
            // Network failure / Offline -> check cache
            val cached = mealDao.getMealsByDate(date)
            if (cached.isNotEmpty()) {
                return@withContext Result.success(cached.map { it.toDomainModel() })
            }
            return@withContext Result.failure(e)
        }
    }

    suspend fun fetchMealsForDateRange(fromDate: String, toDate: String): Result<List<MealModel>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getMealDietInfo(
                fromDate = fromDate,
                toDate = toDate
            )
            if (response.isSuccessful) {
                val bodyString = response.body()?.string() ?: ""
                val rawRows = NetworkClient.parseNeisMealJson(bodyString)
                if (rawRows.isNotEmpty()) {
                    val entities = rawRows.map { row ->
                        CachedMealEntity(
                            id = "${row.mealDate}_${row.mealCode}",
                            date = row.mealDate,
                            mealCode = row.mealCode,
                            mealName = row.mealName,
                            rawDishes = row.dishName,
                            calorie = row.calInfo,
                            rawNutrition = row.ntrInfo,
                            originInfo = row.originInfo,
                            mealCount = row.mealCount
                        )
                    }
                    mealDao.insertMeals(entities)
                    return@withContext Result.success(entities.map { it.toDomainModel() })
                } else {
                    val cached = mealDao.getMealsBetweenDates(fromDate, toDate)
                    return@withContext Result.success(cached.map { it.toDomainModel() })
                }
            } else {
                val cached = mealDao.getMealsBetweenDates(fromDate, toDate)
                if (cached.isNotEmpty()) {
                    return@withContext Result.success(cached.map { it.toDomainModel() })
                }
                return@withContext Result.failure(Exception("주간 식단 조회 실패 (${response.code()})"))
            }
        } catch (e: Exception) {
            Log.e("MealRepository", "Error fetching meals range: $fromDate ~ $toDate", e)
            val cached = mealDao.getMealsBetweenDates(fromDate, toDate)
            if (cached.isNotEmpty()) {
                return@withContext Result.success(cached.map { it.toDomainModel() })
            }
            return@withContext Result.failure(e)
        }
    }

    // Reviews
    fun getReviewForDateFlow(date: String): Flow<UserMealReviewEntity?> = mealDao.getReviewForDateFlow(date)
    fun getAllReviewsFlow(): Flow<List<UserMealReviewEntity>> = mealDao.getAllReviewsFlow()
    suspend fun saveReview(date: String, rating: Int, memo: String) {
        mealDao.insertOrUpdateReview(UserMealReviewEntity(date = date, rating = rating, memo = memo))
    }
    suspend fun deleteReview(date: String) {
        mealDao.deleteReview(date)
    }

    // Favorites
    fun getFavoritesFlow(): Flow<List<String>> = mealDao.getAllFavoritesFlow().map { list -> list.map { it.keyword } }
    suspend fun addFavorite(keyword: String) {
        if (keyword.isNotBlank()) {
            mealDao.insertFavorite(FavoriteFoodEntity(keyword = keyword.trim()))
        }
    }
    suspend fun removeFavorite(keyword: String) {
        mealDao.deleteFavorite(keyword)
    }

    // Allergy Preferences
    fun getActiveAllergiesFlow(): Flow<Set<Int>> = mealDao.getActiveAllergiesFlow().map { list -> list.map { it.allergenCode }.toSet() }
    suspend fun toggleAllergy(allergenCode: Int, enabled: Boolean) {
        if (enabled) {
            mealDao.setAllergyPreference(AllergyPreferenceEntity(allergenCode, true))
        } else {
            mealDao.removeAllergyPreference(allergenCode)
        }
    }
}
