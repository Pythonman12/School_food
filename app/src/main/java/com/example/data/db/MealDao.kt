package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    // --- Cached Meals ---
    @Query("SELECT * FROM cached_meals WHERE date = :date ORDER BY mealCode ASC")
    fun getMealsByDateFlow(date: String): Flow<List<CachedMealEntity>>

    @Query("SELECT * FROM cached_meals WHERE date = :date ORDER BY mealCode ASC")
    suspend fun getMealsByDate(date: String): List<CachedMealEntity>

    @Query("SELECT * FROM cached_meals WHERE date BETWEEN :fromDate AND :toDate ORDER BY date ASC, mealCode ASC")
    fun getMealsBetweenDatesFlow(fromDate: String, toDate: String): Flow<List<CachedMealEntity>>

    @Query("SELECT * FROM cached_meals WHERE date BETWEEN :fromDate AND :toDate ORDER BY date ASC, mealCode ASC")
    suspend fun getMealsBetweenDates(fromDate: String, toDate: String): List<CachedMealEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeals(meals: List<CachedMealEntity>)

    // --- Reviews ---
    @Query("SELECT * FROM meal_reviews WHERE date = :date LIMIT 1")
    fun getReviewForDateFlow(date: String): Flow<UserMealReviewEntity?>

    @Query("SELECT * FROM meal_reviews ORDER BY date DESC")
    fun getAllReviewsFlow(): Flow<List<UserMealReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateReview(review: UserMealReviewEntity)

    @Query("DELETE FROM meal_reviews WHERE date = :date")
    suspend fun deleteReview(date: String)

    // --- Favorite Foods ---
    @Query("SELECT * FROM favorite_foods ORDER BY addedAt DESC")
    fun getAllFavoritesFlow(): Flow<List<FavoriteFoodEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(food: FavoriteFoodEntity)

    @Query("DELETE FROM favorite_foods WHERE keyword = :keyword")
    suspend fun deleteFavorite(keyword: String)

    // --- Allergy Preferences ---
    @Query("SELECT * FROM allergy_preferences WHERE isAlertEnabled = 1")
    fun getActiveAllergiesFlow(): Flow<List<AllergyPreferenceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setAllergyPreference(preference: AllergyPreferenceEntity)

    @Query("DELETE FROM allergy_preferences WHERE allergenCode = :allergenCode")
    suspend fun removeAllergyPreference(allergenCode: Int)
}
