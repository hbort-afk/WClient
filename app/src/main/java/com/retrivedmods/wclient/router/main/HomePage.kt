package com.retrivedmods.wclient.router.main

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.retrivedmods.wclient.R
import com.retrivedmods.wclient.service.Services
import com.retrivedmods.wclient.util.LocalSnackbarHostState
import com.retrivedmods.wclient.util.SnackbarHostStateScope
import com.retrivedmods.wclient.viewmodel.MainScreenViewModel
import kotlinx.coroutines.launch
import java.net.Inet4Address
import java.net.NetworkInterface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePageContent() {
    SnackbarHostStateScope {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        val snackbarHostState = LocalSnackbarHostState.current
        val mainScreenViewModel: MainScreenViewModel = viewModel()

        val onPostPermissionResult: (Boolean) -> Unit = block@{ isGranted: Boolean ->
            if (!isGranted) {
                coroutineScope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(
                        message = context.getString(R.string.notification_permission_denied)
                    )
                }
                return@block
            }

            if (mainScreenViewModel.selectedGame.value === null) {
                coroutineScope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(
                        message = context.getString(R.string.select_game_first)
                    )
                }
                return@block
            }

            Services.toggle(context, mainScreenViewModel.captureModeModel.value)
        }

        val postNotificationPermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted -> onPostPermissionResult(isGranted) }

        val overlayPermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (!Settings.canDrawOverlays(context)) {
                coroutineScope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(
                        message = context.getString(R.string.overlay_permission_denied)
                    )
                }
                return@rememberLauncherForActivityResult
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                postNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return@rememberLauncherForActivityResult
            }
            onPostPermissionResult(true)
        }

        var isActiveBefore by rememberSaveable { mutableStateOf(Services.isActive) }
        var showConnectionDialog by remember { mutableStateOf(false) }

        LaunchedEffect(Services.isActive) {
            if (Services.isActive == isActiveBefore) {
                return@LaunchedEffect
            }

            isActiveBefore = Services.isActive
            if (Services.isActive) {
                showConnectionDialog = true
                snackbarHostState.currentSnackbarData?.dismiss()
                val result = snackbarHostState.showSnackbar(
                    message = context.getString(R.string.backend_connected),
                    actionLabel = context.getString(R.string.start_game)
                )
                val selectedGame = mainScreenViewModel.selectedGame.value
                if (result == SnackbarResult.ActionPerformed && selectedGame != null) {
                    val intent = context.packageManager.getLaunchIntentForPackage(selectedGame)
                    if (intent != null) {
                        context.startActivity(intent)
                    } else {
                        snackbarHostState.showSnackbar(
                            message = context.getString(R.string.failed_to_launch_game),
                        )
                    }
                }
                return@LaunchedEffect
            }

            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(
                message = context.getString(R.string.backend_disconnected)
            )
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "WClient",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.primary
                    )
                )
            },
            bottomBar = {
                SnackbarHost(
                    snackbarHostState,
                    modifier = Modifier.animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.surface
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    // Main Content Section
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {

                        AnimatedVisibility(
                            visible = true,
                            enter = slideInVertically(
                                initialOffsetY = { -40 },
                                animationSpec = tween(600)
                            ) + fadeIn(animationSpec = tween(600))
                        ) {
                            GameSelectorCard()
                        }

                        // Connection Button
                        AnimatedVisibility(
                            visible = true,
                            enter = slideInVertically(
                                initialOffsetY = { -40 },
                                animationSpec = tween(700)
                            ) + fadeIn(animationSpec = tween(700))
                        ) {
                            ConnectionButton(
                                onToggle = {
                                    if (!Settings.canDrawOverlays(context)) {
                                        Toast.makeText(
                                            context,
                                            R.string.request_overlay_permission,
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        overlayPermissionLauncher.launch(
                                            Intent(
                                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                "package: ${context.packageName}".toUri()
                                            )
                                        )
                                        return@ConnectionButton
                                    }

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        postNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        return@ConnectionButton
                                    }

                                    onPostPermissionResult(true)
                                }
                            )
                        }


                        AnimatedVisibility(
                            visible = Services.isActive,
                            enter = slideInVertically(
                                initialOffsetY = { 40 },
                                animationSpec = tween(500)
                            ) + fadeIn(animationSpec = tween(500)),
                            exit = slideOutVertically(
                                targetOffsetY = { 40 },
                                animationSpec = tween(500)
                            ) + fadeOut(animationSpec = tween(500))
                        ) {

                        }
                    }


                    AnimatedVisibility(
                        visible = !Services.isActive,
                        enter = slideInVertically(
                            initialOffsetY = { 40 },
                            animationSpec = tween(800)
                        ) + fadeIn(animationSpec = tween(800)),
                        exit = slideOutVertically(
                            targetOffsetY = { 40 },
                            animationSpec = tween(400)
                        ) + fadeOut(animationSpec = tween(400))
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {

                        }
                    }
                }
            }
        }

        if (showConnectionDialog) {
            val ipAddress = remember {
                runCatching {
                    NetworkInterface.getNetworkInterfaces().asSequence()
                        .flatMap { it.inetAddresses.asSequence() }
                        .filterIsInstance<Inet4Address>()
                        .firstOrNull { !it.isLoopbackAddress }
                        ?.hostAddress
                }.getOrNull() ?: "127.0.0.1"
            }

            AlertDialog(
                onDismissRequest = { showConnectionDialog = false },
                title = {
                    Text(
                        "Connection Instructions",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Connect to WClient by joining through Minecraft's LAN tab, or manually add the server using the credentials below:",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "IP Address:",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                    Text(
                                        ipAddress,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Port:",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                    Text(
                                        "19132",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { showConnectionDialog = false }
                    ) {
                        Text("Got It")
                    }
                }
            )
        }
    }
}

@Composable
private fun GameSelectorCard() {
    val context = LocalContext.current
    val mainScreenViewModel: MainScreenViewModel = viewModel()
    val selectedGame by mainScreenViewModel.selectedGame.collectAsStateWithLifecycle()
    var showGameSelectorDialog by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
        onClick = { showGameSelectorDialog = true }
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                painter = painterResource(R.mipmap.minecraft_icon),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (selectedGame != null) "Selected Game" else "Select Game",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    text = selectedGame?.let {
                        context.packageManager.getPackageInfo(it, 0).applicationInfo?.loadLabel(context.packageManager)
                            .toString()
                    } ?: "Tap to select Minecraft",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                )
            }

            Icon(
                Icons.Rounded.KeyboardArrowDown,
                contentDescription = "Select",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }

    if (showGameSelectorDialog) {
        GameSelectorDialog(
            onDismiss = { showGameSelectorDialog = false },
            onGameSelected = { packageName ->
                mainScreenViewModel.selectGame(packageName)
                showGameSelectorDialog = false
            }
        )
    }
}

@Composable
private fun ConnectionButton(onToggle: () -> Unit) {
    val buttonScale by animateFloatAsState(
        targetValue = if (Services.isActive) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "button_scale"
    )

    Button(
        onClick = onToggle,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .graphicsLayer {
                scaleX = buttonScale
                scaleY = buttonScale
            },
        shape = RoundedCornerShape(20.dp),
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = if (Services.isActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            contentColor = if (Services.isActive) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary
        )
    ) {
        AnimatedContent(
            targetState = Services.isActive,
            label = "button_content"
        ) { isActive ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    if (isActive) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (isActive) "Disconnect" else "Connect",
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = if (isActive) "Disconnect" else "Connect",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun GameSelectorDialog(
    onDismiss: () -> Unit,
    onGameSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val mainScreenViewModel: MainScreenViewModel = viewModel()
    val packageInfos by mainScreenViewModel.packageInfos.collectAsStateWithLifecycle()
    val packageInfoState by mainScreenViewModel.packageInfoState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        mainScreenViewModel.fetchPackageInfos()
    }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.padding(vertical = 24.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    "Select Minecraft Version",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )

                LazyColumn(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        if (packageInfoState === MainScreenViewModel.PackageInfoState.Loading) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .padding(vertical = 16.dp)
                                    .fillMaxWidth()
                            )
                        }
                    }

                    items(packageInfos.size) { index ->
                        val packageInfo = packageInfos[index]
                        val applicationInfo = packageInfo.applicationInfo!!
                        val packageManager = context.packageManager
                        val icon = remember {
                            applicationInfo.loadIcon(packageManager).toBitmap().asImageBitmap()
                        }
                        val name = remember {
                            applicationInfo.loadLabel(packageManager).toString()
                        }
                        val packageName = packageInfo.packageName
                        val versionName = packageInfo.versionName ?: "Unknown"

                        ElevatedCard(
                            onClick = { onGameSelected(packageName) },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Icon(
                                    bitmap = icon,
                                    contentDescription = null,
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(40.dp)
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        name,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Medium
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        "Version: $versionName",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

