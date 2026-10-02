package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.data.api.NetworkClient
import com.example.data.db.AppDatabase
import com.example.data.repository.MealRepository
import com.example.ui.MealScreen
import com.example.ui.MealViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MealViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = MealRepository(
            apiService = NetworkClient.apiService,
            mealDao = database.mealDao()
        )
        MealViewModel.Factory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MealScreen(viewModel = viewModel)
            }
        }
    }
}
