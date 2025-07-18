package com.retrivedmods.wclient.activity

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.*
import com.retrivedmods.wclient.navigation.Navigation
import com.retrivedmods.wclient.ui.component.LoadingScreen // ✅ use LoadingScreen
import com.retrivedmods.wclient.ui.theme.MuCuteClientTheme

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalFoundationApi::class)
    @SuppressLint("BatteryLife")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MuCuteClientTheme {
                var showLoading by remember { mutableStateOf(true) }


                if (showLoading) {
                    LoadingScreen(onDone = { showLoading = false })
                } else {
                    Navigation()
                }
            }
        }
    }
}
