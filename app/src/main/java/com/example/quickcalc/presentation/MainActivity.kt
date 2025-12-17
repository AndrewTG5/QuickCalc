package com.example.quickcalc.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.example.quickcalc.presentation.theme.QuickCalcTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalculatorApp()
        }
    }
}

@Composable
fun CalculatorApp() {
    val calculatorState = remember { CalculatorState() }
    val scrollState = rememberScrollState()
    val focusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()
    val view = LocalView.current

    QuickCalcTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(top = 18.dp, bottom = 0.dp, start = 12.dp, end = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier
                    .horizontalScroll(scrollState)
                    .onRotaryScrollEvent { event ->
                        coroutineScope.launch {
                            val delta = if (event.verticalScrollPixels != 0f) {
                                event.verticalScrollPixels
                            } else {
                                event.horizontalScrollPixels
                            }
                            scrollState.scrollBy(delta)
                        }
                        true
                    }
                    .focusRequester(focusRequester)
                    .focusable()
                    .padding(top = 2.dp, bottom = 0.dp, start = 24.dp, end = 24.dp)
                    .height(32.dp)
            ) {
                Text(
                    text = calculatorState.display,
                    style = MaterialTheme.typography.display3
                )
            }

            // Request focus for rotary input
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }

            // Auto-scroll to end when display changes
            LaunchedEffect(calculatorState.display) {
                scrollState.animateScrollTo(scrollState.maxValue)
            }

            CalculatorButtons(
                onButtonClick = { input ->
                    // Haptic feedback
                    view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)

                    when (input) {
                        "C" -> calculatorState.onClear()
                        "⌫" -> calculatorState.onDelete()
                        "＝" -> calculatorState.onCalculate()
                        "+", "−", "×", "÷" -> calculatorState.onOperation(input)
                        else -> calculatorState.onInput(input)
                    }
                }
            )
        }
    }
}

@Composable
fun CalculatorButtons(onButtonClick: (String) -> Unit) {
    val row1 = listOf("7", "8", "9", "÷", "C")
    val row2 = listOf("4", "5", "6", "×", "⌫")
    val row3 = listOf("1", "2", "3", "−", "＝")
    val row4 = listOf("", "0", ".", "+", "")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 0.dp, bottom = 0.dp),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        ButtonRow(row1, onButtonClick)
        ButtonRow(row2, onButtonClick)
        ButtonRow(row3, onButtonClick)

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            ButtonRow(row4, onButtonClick, isBottomRow = true)
        }
    }
}

@Composable
fun ButtonRow(buttons: List<String>, onButtonClick: (String) -> Unit, isBottomRow: Boolean = false) {
    val numberColor = Color(0xFF2d2d2d)
    val operatorColor = Color(0xFF3b3c4e)
    val equalsColor = Color(0xFF242545)
    val clearColor = Color(0xFF5F3C52)
    val whiteColor = Color(0xFFFFFFFF)

    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.fillMaxWidth().padding(0.dp).height(0.dp)
    ) {
        for ((index, button) in buttons.withIndex()) {
            val backgroundColor = when (button) {
                in listOf("+", "−", "×", "÷") -> operatorColor
                "C" -> clearColor
                "＝" -> equalsColor
                "⌫" -> equalsColor
                else -> numberColor
            }

            val textColor = if (button in listOf("+", "−", "×", "÷", "＝", "C", "⌫")) whiteColor else Color.White

            val alpha = if (isBottomRow && (index == 0 || index == buttons.lastIndex)) 0f else 1f

            Button(
                onClick = { if (alpha > 0) onButtonClick(button) },
                modifier = Modifier
                    .padding(0.5.dp)
                    .aspectRatio(1.25f)
                    .weight(1f)
                    .alpha(alpha),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = backgroundColor.copy(alpha = alpha),
                    contentColor = textColor
                )
            ) {
                if (alpha > 0) {
                    Text(button, color = textColor)
                }
            }
        }
    }
}
