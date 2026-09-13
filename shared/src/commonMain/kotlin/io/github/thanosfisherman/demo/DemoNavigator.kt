package io.github.thanosfisherman.demo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Demo(val name: String, val content: @Composable () -> Unit)

private val DEMO_LABEL_STYLE = TextStyle(color = Color.White, fontSize = 14.sp, fontFamily = FontFamily.Monospace)

@Composable
fun DemoNavigator(demos: List<Demo>) {
    require(demos.isNotEmpty()) { "DemoNavigator needs at least one demo" }
    var index by remember { mutableStateOf(0) }

    Box(modifier = Modifier.fillMaxSize()) {
        demos[index].content()

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .safeDrawingPadding() // keeps this clear of the nav bar / gesture area on Android
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Button(
                onClick = { if (index > 0) index-- },
                enabled = index > 0,
                modifier = Modifier.align(Alignment.CenterVertically),
            ) {
                Text("Previous")
            }

            BasicText(
                text = demos[index].name,
                style = DEMO_LABEL_STYLE,
                modifier = Modifier.align(Alignment.CenterVertically),
            )

            Button(
                onClick = { if (index < demos.lastIndex) index++ },
                enabled = index < demos.lastIndex,
                modifier = Modifier.align(Alignment.CenterVertically),
            ) {
                Text("Next")
            }
        }
    }
}

@Composable
@Preview
fun DemoNavigatorPreview() {
    DemoNavigator(demos = listOf(
        Demo(name = "Demo 1", content = { Text("Demo 1 Content") }),
        Demo(name = "Demo 2", content = { Text("Demo 2 Content") }),
        Demo(name = "Demo 3", content = { Text("Demo 3 Content") }),
    ))
}