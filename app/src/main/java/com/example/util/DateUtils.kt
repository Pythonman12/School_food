package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val ymdFormat = SimpleDateFormat("yyyyMMdd", Locale.KOREA)
    private val displayFormat = SimpleDateFormat("yyyy년 M월 d일 (E)", Locale.KOREA)
    private val monthDayFormat = SimpleDateFormat("M월 d일 (E)", Locale.KOREA)
    private val simpleDayFormat = SimpleDateFormat("d", Locale.KOREA)

    fun getTodayYmd(): String {
        return ymdFormat.format(Date())
    }

    fun formatDateToYmd(calendar: Calendar): String {
        return ymdFormat.format(calendar.time)
    }

    fun parseYmdToCalendar(ymd: String): Calendar {
        val cal = Calendar.getInstance()
        try {
            val date = ymdFormat.parse(ymd)
            if (date != null) {
                cal.time = date
            }
        } catch (_: Exception) {}
        return cal
    }

    fun formatToDisplay(ymd: String): String {
        return try {
            val date = ymdFormat.parse(ymd)
            if (date != null) displayFormat.format(date) else ymd
        } catch (_: Exception) {
            ymd
        }
    }

    fun formatToMonthDay(ymd: String): String {
        return try {
            val date = ymdFormat.parse(ymd)
            if (date != null) monthDayFormat.format(date) else ymd
        } catch (_: Exception) {
            ymd
        }
    }

    fun getDayNumber(ymd: String): String {
        return try {
            val date = ymdFormat.parse(ymd)
            if (date != null) simpleDayFormat.format(date) else ""
        } catch (_: Exception) {
            ""
        }
    }

    fun getDayOfWeekKorean(ymd: String): String {
        val cal = parseYmdToCalendar(ymd)
        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "월"
            Calendar.TUESDAY -> "화"
            Calendar.WEDNESDAY -> "수"
            Calendar.THURSDAY -> "목"
            Calendar.FRIDAY -> "금"
            Calendar.SATURDAY -> "토"
            Calendar.SUNDAY -> "일"
            else -> ""
        }
    }

    fun isWeekend(ymd: String): Boolean {
        val cal = parseYmdToCalendar(ymd)
        val dow = cal.get(Calendar.DAY_OF_WEEK)
        return dow == Calendar.SATURDAY || dow == Calendar.SUNDAY
    }

    fun offsetDate(ymd: String, days: Int): String {
        val cal = parseYmdToCalendar(ymd)
        cal.add(Calendar.DAY_OF_MONTH, days)
        return ymdFormat.format(cal.time)
    }

    /**
     * Returns a list of 5 YMD strings for Monday through Friday of the given date's week.
     */
    fun getWeekdaysForDate(ymd: String): List<String> {
        val cal = parseYmdToCalendar(ymd)
        // Set to Monday of this week
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val weekDays = mutableListOf<String>()
        for (i in 0 until 5) {
            weekDays.add(ymdFormat.format(cal.time))
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        return weekDays
    }
}
