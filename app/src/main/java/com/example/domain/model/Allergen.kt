package com.example.domain.model

data class Allergen(
    val code: Int,
    val name: String,
    val emoji: String
) {
    companion object {
        val ALL_ALLERGENS = listOf(
            Allergen(1, "난류(달걀)", "🥚"),
            Allergen(2, "우유", "🥛"),
            Allergen(3, "메밀", "🌾"),
            Allergen(4, "땅콩", "🥜"),
            Allergen(5, "대두(콩)", "🫘"),
            Allergen(6, "밀", "🍞"),
            Allergen(7, "고등어", "🐟"),
            Allergen(8, "게", "🦀"),
            Allergen(9, "새우", "🦐"),
            Allergen(10, "돼지고기", "🥓"),
            Allergen(11, "복숭아", "🍑"),
            Allergen(12, "토마토", "🍅"),
            Allergen(13, "아황산류", "🍷"),
            Allergen(14, "호두", "🌰"),
            Allergen(15, "닭고기", "🍗"),
            Allergen(16, "쇠고기", "🥩"),
            Allergen(17, "오징어", "🦑"),
            Allergen(18, "조개류", "🦪"),
            Allergen(19, "잣", "🌲")
        )

        private val ALLERGEN_MAP = ALL_ALLERGENS.associateBy { it.code }

        fun fromCode(code: Int): Allergen? = ALLERGEN_MAP[code]

        fun getDisplayName(code: Int): String {
            return ALLERGEN_MAP[code]?.name ?: "알레르기($code)"
        }
    }
}
