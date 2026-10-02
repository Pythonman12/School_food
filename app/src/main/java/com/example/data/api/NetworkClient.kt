package com.example.data.api

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object NetworkClient {
    const val BASE_URL = "https://open.neis.go.kr/hub/"

    // 대진전자통신고등학교 코드
    const val DAEJIN_OFFICE_CODE = "C10" // 부산광역시교육청
    const val DAEJIN_SCHOOL_CODE = "7150597" // 대진전자통신고등학교
    const val DAEJIN_SCHOOL_NAME = "대진전자통신고등학교"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    val apiService: NeisMealApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .build()
            .create(NeisMealApiService::class.java)
    }

    data class RawMealRow(
        val officeCode: String,
        val officeName: String,
        val schoolCode: String,
        val schoolName: String,
        val mealCode: String,
        val mealName: String,
        val mealDate: String,
        val mealCount: Double?,
        val dishName: String,
        val originInfo: String,
        val calInfo: String,
        val ntrInfo: String
    )

    fun parseNeisMealJson(jsonString: String): List<RawMealRow> {
        val list = mutableListOf<RawMealRow>()
        try {
            val root = JSONObject(jsonString)
            if (!root.has("mealServiceDietInfo")) {
                if (root.has("RESULT")) {
                    val result = root.getJSONObject("RESULT")
                    Log.d("NetworkClient", "NEIS Result: ${result.optString("CODE")} - ${result.optString("MESSAGE")}")
                }
                return emptyList()
            }

            val array = root.getJSONArray("mealServiceDietInfo")
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                if (obj.has("row")) {
                    val rows = obj.getJSONArray("row")
                    for (j in 0 until rows.length()) {
                        val rowObj = rows.getJSONObject(j)
                        val row = RawMealRow(
                            officeCode = rowObj.optString("ATPT_OFCDC_SC_CODE", ""),
                            officeName = rowObj.optString("ATPT_OFCDC_SC_NM", ""),
                            schoolCode = rowObj.optString("SD_SCHUL_CODE", ""),
                            schoolName = rowObj.optString("SCHUL_NM", ""),
                            mealCode = rowObj.optString("MMEAL_SC_CODE", ""),
                            mealName = rowObj.optString("MMEAL_SC_NM", "중식"),
                            mealDate = rowObj.optString("MLSV_YMD", ""),
                            mealCount = if (rowObj.has("MLSV_FGR")) rowObj.optDouble("MLSV_FGR") else null,
                            dishName = rowObj.optString("DDISH_NM", ""),
                            originInfo = rowObj.optString("ORPLC_INFO", ""),
                            calInfo = rowObj.optString("CAL_INFO", ""),
                            ntrInfo = rowObj.optString("NTR_INFO", "")
                        )
                        list.add(row)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("NetworkClient", "Failed to parse NEIS JSON", e)
        }
        return list
    }
}
