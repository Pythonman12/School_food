package com.example

import com.example.domain.model.Allergen
import com.example.domain.model.MealModel
import com.example.util.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun parseDishes_correctlyExtractsAllergenCodesAndCleanNames() {
        val rawDdish = "기장밥 <br/>순두부짬뽕 (5.6.9.13.17.18)<br/>콩나물무침 (5)<br/>배추김치 (9)"
        val parsed = MealModel.parseDishes(rawDdish)

        assertEquals(4, parsed.size)

        assertEquals("기장밥", parsed[0].cleanName)
        assertTrue(parsed[0].allergenCodes.isEmpty())

        assertEquals("순두부짬뽕", parsed[1].cleanName)
        assertEquals(listOf(5, 6, 9, 13, 17, 18), parsed[1].allergenCodes)

        assertEquals("콩나물무침", parsed[2].cleanName)
        assertEquals(listOf(5), parsed[2].allergenCodes)

        assertEquals("배추김치", parsed[3].cleanName)
        assertEquals(listOf(9), parsed[3].allergenCodes)
    }

    @Test
    fun parseNutrition_correctlyParsesValues() {
        val rawNtr = "탄수화물(g) : 134.4<br/>단백질(g) : 27.2<br/>지방(g) : 28.4"
        val parsed = MealModel.parseNutrition(rawNtr)

        assertEquals(3, parsed.size)
        assertEquals("탄수화물(g)", parsed[0].name)
        assertEquals("134.4", parsed[0].value)
        assertEquals("단백질(g)", parsed[1].name)
        assertEquals("27.2", parsed[1].value)
        assertEquals("지방(g)", parsed[2].name)
        assertEquals("28.4", parsed[2].value)
    }

    @Test
    fun allergen_mappingHas19Items() {
        assertEquals(19, Allergen.ALL_ALLERGENS.size)
        assertEquals("난류(달걀)", Allergen.getDisplayName(1))
        assertEquals("우유", Allergen.getDisplayName(2))
        assertEquals("잣", Allergen.getDisplayName(19))
    }

    @Test
    fun dateUtils_weekdaysReturnsFiveDays() {
        val weekdays = DateUtils.getWeekdaysForDate("20261002")
        assertEquals(5, weekdays.size)
        assertEquals("월", DateUtils.getDayOfWeekKorean(weekdays[0]))
        assertEquals("금", DateUtils.getDayOfWeekKorean(weekdays[4]))
    }
}
