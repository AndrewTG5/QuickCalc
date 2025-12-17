package com.example.quickcalc.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.text.DecimalFormat

class CalculatorState {
    private var expression by mutableStateOf("")
    var display by mutableStateOf("")

    private val df = DecimalFormat("#.########")

    // Map standard operators to display operators
    private fun toDisplayString(expr: String): String {
        return expr.replace('*', '×').replace('/', '÷').replace('-', '−')
    }

    // Map display operators to standard operators for evaluation
    private fun toExpressionString(input: String): String {
        return input.replace('×', '*').replace('÷', '/').replace('−', '-')
    }

    fun onInput(input: String) {
        // Prevent multiple consecutive decimals
        if (input == "." && (expression.lastOrNull()?.isDigit() != true || lastNumberContainsDecimal())) {
            return
        }

        // Append input and update the display
        expression += input
        updateDisplay()
    }

    fun onOperation(op: String) {
        // Convert display operators to standard operators for internal expression
        val standardOp = toExpressionString(op)

        if (expression.isEmpty() && standardOp == "-") {
            expression += standardOp
            updateDisplay()
            return
        }

        if (standardOp == "-" && isLastCharOperation() && expression.last() != '-') {
            expression += standardOp
            updateDisplay()
            return
        }

        if (expression.isNotEmpty() && !isLastCharOperation()) {
            expression += standardOp
            updateDisplay()
        }
    }

    fun onCalculate() {
        if (expression.isEmpty() || isLastCharOperation()) {
            return
        }
        try {
            val result = evaluate(expression)
            display = df.format(result)
            expression = display
        } catch (_: Exception) {
            display = "Error"
            expression = ""
        }
    }

    fun onClear() {
        expression = ""
        updateDisplay()
    }

    fun onDelete() {
        if (expression.isNotEmpty()) {
            expression = expression.dropLast(1)
            updateDisplay()
        }
    }

    private fun updateDisplay() {
        display = toDisplayString(expression)
    }

    private fun isLastCharOperation(): Boolean {
        return expression.isNotEmpty() && expression.last() in listOf('+', '-', '*', '/')
    }

    private fun lastNumberContainsDecimal(): Boolean {
        val lastNumber = expression.split(Regex("[-+*/]")).lastOrNull() ?: ""
        return "." in lastNumber
    }

    private fun evaluate(expression: String): Double {
        // Expression already uses standard operators (+, -, *, /)
        return try {
            val result = net.objecthunter.exp4j.ExpressionBuilder(expression).build().evaluate()
            if (result.isInfinite() || result.isNaN()) {
                throw ArithmeticException("Invalid calculation")
            }
            result
        } catch (_: Exception) {
            throw ArithmeticException("Invalid expression")
        }
    }
}
