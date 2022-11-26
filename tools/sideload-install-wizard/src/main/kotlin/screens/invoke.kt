package me.heizi.flashing_tool.sideloader.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Invoke(
    isDone:Boolean = false,
    text:String,
    title:String = if (!isDone)"正在执行" else "完成",
    smallTitle:String,
    closeBtnClick: () -> Unit = {},
    scope: CoroutineScope = rememberCoroutineScope(),
    color: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    whiteBordColor:Color = MaterialTheme.colorScheme.background,
    primary: Color = MaterialTheme.colorScheme.primary,
    onPrimary: Color = MaterialTheme.colorScheme.onPrimary,
) = CompositionLocalProvider(LocalContentColor.provides(color)) {

    Scaffold (
        modifier = Modifier.fillMaxSize().background(backgroundColor),
        containerColor = backgroundColor, contentColor = color,
        topBar = {
            Title(Modifier.wrapContentSize().background(color = backgroundColor), title,smallTitle)
        },
        content = {
            Card(
                modifier = Modifier.padding(it).padding(top = 16.dp).padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = whiteBordColor, contentColor = primary),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize(),Arrangement.spacedBy(4.dp)) {
                    Spacer(Modifier.padding(4.dp))
                    if (!isDone) LinearProgressIndicator(Modifier.fillMaxWidth().padding(horizontal = 16.dp))
                    SelectableTextWithScrolling(Modifier.weight(1f).padding(horizontal = 16.dp),text) {
                        if (!isDone) SideEffect { scope.launch {
                            scrollTo(maxValue)
                         } }
                    }
                }
            }
        },
        floatingActionButton = {
            if (isDone) ExtendedFloatingActionButton(
                modifier = Modifier.padding(end = 36.dp, bottom = 16.dp),
                containerColor = primary, contentColor = onPrimary,
                onClick = closeBtnClick,
                text = {
                    Text("关闭")
                },icon = {
                    Icon(Icons.Default.Close,"exit")
                },
            )
        }
    )
}
@Composable
fun SelectableTextWithScrolling(modifier: Modifier, text: String,afterTextUpdate: @Composable ScrollState.() -> Unit) = Box(modifier) {
    val scrollV = rememberScrollState()
    val scrollH = rememberScrollState()
    SelectionContainer(
        Modifier.fillMaxSize()
    ) {
        Text(text, modifier = Modifier.verticalScroll(scrollV).horizontalScroll(scrollH).fillMaxSize())
        afterTextUpdate(scrollH)
    }
    HorizontalScrollbar(rememberScrollbarAdapter(scrollH), Modifier.align(Alignment.BottomCenter).fillMaxWidth())
    VerticalScrollbar(rememberScrollbarAdapter(scrollV),Modifier.align(Alignment.CenterEnd).fillMaxHeight().padding(vertical =8.dp))
}
@Composable
fun Title(
    modifier: Modifier,
    smallTitle: String,
    title: String,
) = Box(modifier) {
    Column(modifier = Modifier.padding(top = 16.dp,start = 24.dp)) {
        Text(title, style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.padding(4.dp))
        Text(smallTitle,style = MaterialTheme.typography.titleLarge)
    }
}