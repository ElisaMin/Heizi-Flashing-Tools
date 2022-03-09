package me.heizi.flashing_tool.image

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

interface ViewModel
interface Component<T: ViewModel> {
    val title:String
    val subtitle:String
    val viewModel:T
    @Composable
    fun rememberViewModel() = remember {
        viewModel
    }
    @Composable
    fun render()
}