package com.mucheng.mucute.client.router.main

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mucheng.mucute.client.util.LocalSnackbarHostState
import com.mucheng.mucute.client.util.SnackbarHostStateScope

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AboutPageContent() {
    SnackbarHostStateScope {
        val snackbarHostState = LocalSnackbarHostState.current
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("About Us", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = Color.White
                    )
                )
            },
            bottomBar = {
                SnackbarHost(
                    snackbarHostState,
                    modifier = Modifier.animateContentSize()
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) {
            Box(
                Modifier
                    .padding(it)
                    .fillMaxSize()
            ) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ClientInformationCard()
                    SocialLinksCard()
                    CopyrightCard()
                }
            }
        }
    }
}

@Composable
private fun ClientInformationCard() {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("WClient - The Ultimate Bedrock Client", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "WClient is a feature-rich Minecraft Bedrock Edition modded client offering " +
                        "high-performance enhancements, premium mod features, and full server compatibility. " +
                        "With advanced modules like Killaura, OPFightBot, MotionFly, and premium-only access, " +
                        "it provides the best gameplay experience.",
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Version: Test", fontWeight = FontWeight.SemiBold)
            Text("Developer: RetrivedMods", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SocialLinksCard() {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Connect With Us", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Join our community and stay updated:", fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))
            SocialLink("YouTube", "https://youtube.com/@retrivedmodsofficial")
            SocialLink("Discord", "https://discord.gg/N2Gejr8Fbp")
        }
    }
}

@Composable
private fun SocialLink(name: String, url: String) {
    val context = LocalContext.current
    Text(
        text = name,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            }
            .padding(4.dp)
    )
}

@Composable
private fun CopyrightCard() {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Legal & Copyright", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "WClient is a third-party modification for Minecraft Bedrock Edition. " +
                        "All rights reserved. Unauthorized distribution or resale of this client is strictly prohibited.",
                fontSize = 14.sp,
                textAlign = TextAlign.Justify
            )
        }
    }
}
