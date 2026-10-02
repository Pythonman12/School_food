package com.example.ui.components

import android.app.DatePickerDialog
import android.content.Context
import com.example.util.DateUtils
import java.util.Calendar

object DatePickerHelper {
    fun showDatePicker(
        context: Context,
        initialYmd: String,
        onDateSelected: (String) -> Unit
    ) {
        val cal = DateUtils.parseYmdToCalendar(initialYmd)
        val dialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                onDateSelected(DateUtils.formatDateToYmd(selectedCal))
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
        dialog.show()
    }
}
