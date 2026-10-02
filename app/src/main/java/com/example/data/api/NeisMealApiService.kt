package com.example.data.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NeisMealApiService {

    @GET("mealServiceDietInfo")
    suspend fun getMealDietInfo(
        @Query("Type") type: String = "json",
        @Query("pIndex") pIndex: Int = 1,
        @Query("pSize") pSize: Int = 100,
        @Query("ATPT_OFCDC_SC_CODE") officeCode: String = NetworkClient.DAEJIN_OFFICE_CODE,
        @Query("SD_SCHUL_CODE") schoolCode: String = NetworkClient.DAEJIN_SCHOOL_CODE,
        @Query("MLSV_YMD") mealDate: String? = null,
        @Query("MLSV_FROM_YMD") fromDate: String? = null,
        @Query("MLSV_TO_YMD") toDate: String? = null,
        @Query("KEY") apiKey: String? = null
    ): Response<ResponseBody>
}
