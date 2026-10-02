package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.MealModel

@Entity(tableName = "cached_meals")
data class CachedMealEntity(
    @PrimaryKey val id: String, // date_mealCode
    val date: String, // YYYYMMDD
    val mealCode: String,
    val mealName: String,
    val rawDishes: String,
    val calorie: String,
    val rawNutrition: String,
    val originInfo: String,
    val mealCount: Double?,
    val cachedAt: Long = System.currentTimeMillis()
) {
    fun toDomainModel(): MealModel {
        return MealModel(
            id = id,
            date = date,
            mealCode = mealCode,
            mealName = mealName,
            dishes = MealModel.parseDishes(rawDishes),
            calorie = calorie,
            nutritionList = MealModel.parseNutrition(rawNutrition),
            originInfo = originInfo,
            mealCount = mealCount
        )
    }
}

@Entity(tableName = "meal_reviews")
data class UserMealReviewEntity(
    @PrimaryKey val date: String, // YYYYMMDD
    val rating: Int, // 1 to 5
    val memo: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorite_foods")
data class FavoriteFoodEntity(
    @PrimaryKey val keyword: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "allergy_preferences")
data class AllergyPreferenceEntity(
    @PrimaryKey val allergenCode: Int,
    val isAlertEnabled: Boolean = true
)
