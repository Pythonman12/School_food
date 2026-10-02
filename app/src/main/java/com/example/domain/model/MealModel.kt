package com.example.domain.model

data class ParsedDish(
    val rawText: String,
    val cleanName: String,
    val allergenCodes: List<Int>
)

data class NutritionItem(
    val name: String,
    val value: String
)

data class MealModel(
    val id: String, // date_mealCode
    val date: String, // YYYYMMDD
    val mealCode: String, // 1: 조식, 2: 중식, 3: 석식
    val mealName: String, // 중식 등
    val dishes: List<ParsedDish>,
    val calorie: String,
    val nutritionList: List<NutritionItem>,
    val originInfo: String,
    val mealCount: Double?
) {
    companion object {
        fun parseDishes(rawDdish: String): List<ParsedDish> {
            if (rawDdish.isBlank()) return emptyList()

            val lines = rawDdish
                .replace("<br/>", "\n")
                .replace("<br>", "\n")
                .split("\n")
                .map { it.trim() }
                .filter { it.isNotBlank() }

            val allergyRegex = Regex("""\(([0-9.]+)\)""")

            return lines.map { line ->
                val codes = mutableListOf<Int>()
                allergyRegex.findAll(line).forEach { match ->
                    val inner = match.groupValues[1]
                    inner.split(".").forEach { codeStr ->
                        codeStr.trim().toIntOrNull()?.let { codes.add(it) }
                    }
                }
                val cleanName = allergyRegex.replace(line, "").trim()
                ParsedDish(
                    rawText = line,
                    cleanName = cleanName,
                    allergenCodes = codes.distinct().sorted()
                )
            }
        }

        fun parseNutrition(rawNutrition: String): List<NutritionItem> {
            if (rawNutrition.isBlank()) return emptyList()

            return rawNutrition
                .replace("<br/>", "\n")
                .replace("<br>", "\n")
                .split("\n")
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .mapNotNull { line ->
                    val parts = line.split(":")
                    if (parts.size >= 2) {
                        NutritionItem(
                            name = parts[0].trim(),
                            value = parts.subList(1, parts.size).joinToString(":").trim()
                        )
                    } else null
                }
        }
    }
}
