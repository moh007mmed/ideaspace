package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.ui.IdeaSpaceMainScreen
import com.example.ui.theme.IdeaSpaceTheme
import com.example.ui.theme.IdeaThemes
import com.example.ui.theme.ThemedBackdrop
import com.example.viewmodel.IdeaSpaceViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: IdeaSpaceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val ctx = LocalContext.current
            var themeId by remember { mutableStateOf(IdeaThemes.load(ctx)) }
            val palette = IdeaThemes.byId(themeId)
            IdeaSpaceTheme(palette) {
                Box(Modifier.fillMaxSize()) {
                    ThemedBackdrop(palette, Modifier.fillMaxSize())
                    IdeaSpaceMainScreen(
                        viewModel = viewModel,
                        themeId = themeId,
                        onThemeChange = { themeId = it; IdeaThemes.save(ctx, it) }
                    )
                }
            }
        }
    }
}
