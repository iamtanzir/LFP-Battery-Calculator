package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.BatteryCalculatorScreen
import com.example.ui.BatteryViewModel
import com.example.ui.BatteryViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val viewModel: BatteryViewModel = viewModel(
          factory = BatteryViewModelFactory(application)
        )
        Surface(modifier = Modifier.fillMaxSize()) {
          BatteryCalculatorScreen(viewModel = viewModel)
        }
      }
    }
  }
}
