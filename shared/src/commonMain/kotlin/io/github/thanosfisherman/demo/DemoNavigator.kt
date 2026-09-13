package io.github.thanosfisherman.demo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Demo(val name: String, val content: @Composable () -> Unit)

@Composable
fun DemoNavigator(demos: List<Demo>) {
    require(demos.isNotEmpty()) { "DemoNavigator needs at least one demo" }
    var index by remember { mutableStateOf(0) }

    Box(modifier = Modifier.fillMaxSize()) {
        demos[index].content()

        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .safeDrawingPadding() // keeps this clear of the nav bar / gesture area on Android
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        ) {
            Button(
                onClick = { if (index > 0) index-- },
                enabled = index > 0,
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically),
            ) {
                Text("Previous")
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = { if (index < demos.lastIndex) index++ },
                enabled = index < demos.lastIndex,
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically),
            ) {
                Text("Next")
            }
        }
    }
}

@Composable
@Preview
fun DemoNavigatorPreview() {
    DemoNavigator(
        demos = listOf(
            Demo(name = "Demo 1", content = { Text("Demo 1 Content") }),
            Demo(name = "Demo 2", content = { Text("Demo 2 Content") }),
            Demo(name = "Demo 3", content = { Text("Demo 3 Content") }),
        )
    )
}