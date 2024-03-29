package me.heizi.flashing_tool.sideloader.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.splitpane.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
operator fun HomeViewModel.invoke() {
    LaunchedEffect("launchOnHome") {
        onStart()
    }
    DisposableEffect("DisposableOnHome") {
        onDispose {
            onStop()
        }
    }
    Scaffold(
        floatingActionButton = { fab() },
        content = { content(it) },
        snackbarHost = { snackbar() }
    )
}
@Composable
fun HomeViewModel.snackbar() {
    SnackbarHost(snacks)
}

@Composable
fun HomeViewModel.fab() {
    val isShow = !isWaiting && selected.isNotEmpty()
    if (isShow) ExtendedFloatingActionButton(
        onClick = ::nextStep,
        text = { Text("安装") },
        icon = { Icon(Icons.Default.Send,"start") },
    )
}

@OptIn(ExperimentalSplitPaneApi::class) @Composable
fun HomeViewModel.content(padding:PaddingValues) = BoxWithConstraints(Modifier.padding(padding)) {
    if (maxWidth> 420.dp) HorizontalSplitPane(splitPaneState = rememberSplitPaneState(0.6f)) {
        first(176.dp,) { first() }
        defaultSplitter()
        second { second(PaddingValues(8.dp)) }
    } else VerticalSplitPane(modifier = Modifier.padding(horizontal = 8.dp)) {
        first((176+36+64).dp) { first() }
        defaultSplitter()
        second { second(PaddingValues(horizontal = 8.dp)) }
    }

}
@Composable
fun HomeViewModel.first() = Column {
    Info(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        packageDetail =packageDetails,
        icon = icon,
        titleName = titleName,
        packageName = packageName,
        version = version,
        switchMode = ::switchMode
    )
}
@Composable
fun HomeViewModel.second(padding:PaddingValues){
    DeviceScreen(
        modifier = Modifier.padding(padding),
        devices = devices,
        selected = selected,
        isWaiting = isWaiting,
        onSelecting = ::onSelecting,
        addDevice = ::addDevice,
        onConnectRequest = ::onConnectRequest
    )
}


