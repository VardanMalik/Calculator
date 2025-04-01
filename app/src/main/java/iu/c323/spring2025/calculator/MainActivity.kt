
// MainActivity.kt - Android Calculator App
// This file contains the main logic for the calculator app, handling both UI interactions and calculations.

package iu.c323.spring2025.calculator

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    // UI display and state variables
    private lateinit var display: TextView
    private var currentNumber = ""   // Stores the current number input
    private var lastNumber = ""      // Stores the last number entered
    private var currentOperator: String? = null  // Stores the selected operator
    private var isNewInput = true     // Tracks if a new input is being entered

    companion object {
        // Constant tags for logging and state keys
        private const val TAG = "CalculatorApp"
        private const val KEY_CURRENT_NUMBER = "currentNumber"
        private const val KEY_LAST_NUMBER = "lastNumber"
        private const val KEY_CURRENT_OPERATOR = "currentOperator"
        private const val KEY_IS_NEW_INPUT = "isNewInput"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        display = findViewById(R.id.display)

        // Restore state if available (important for orientation changes)
        if (savedInstanceState != null) {
            currentNumber = savedInstanceState.getString(KEY_CURRENT_NUMBER, "")
            lastNumber = savedInstanceState.getString(KEY_LAST_NUMBER, "")
            currentOperator = savedInstanceState.getString(KEY_CURRENT_OPERATOR)
            isNewInput = savedInstanceState.getBoolean(KEY_IS_NEW_INPUT, true)
            updateDisplay()
        }

        // Number buttons setup (0-9)
        val numberButtons = listOf(
            R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3,
            R.id.btn_4, R.id.btn_5, R.id.btn_6, R.id.btn_7,
            R.id.btn_8, R.id.btn_9
        )
        for (id in numberButtons) {
            findViewById<Button>(id)?.setOnClickListener {
                val number = (it as Button).text.toString()
                appendNumber(number)  // Handle number input
                Log.d(TAG, "Button pressed: $number")
            }
        }

        // Operator buttons setup
        findViewById<Button>(R.id.btn_add)?.setOnClickListener { logAndSelectOperator("+") }
        findViewById<Button>(R.id.btn_subtract)?.setOnClickListener { logAndSelectOperator("-") }
        findViewById<Button>(R.id.btn_multiply)?.setOnClickListener { logAndSelectOperator("*") }
        findViewById<Button>(R.id.btn_divide)?.setOnClickListener { logAndSelectOperator("/") }

        // Special buttons setup
        findViewById<Button>(R.id.btn_equals)?.setOnClickListener {
            calculateResult()
            Log.d(TAG, "Button pressed: =")
        }
        findViewById<Button>(R.id.btn_clear)?.setOnClickListener {
            clearAll()
            Log.d(TAG, "Button pressed: C")
        }
        findViewById<Button>(R.id.btn_plus_minus)?.setOnClickListener {
            toggleSign()
            Log.d(TAG, "Button pressed: +/-")
        }
        findViewById<Button>(R.id.btn_percent)?.setOnClickListener {
            applyPercentage()
            Log.d(TAG, "Button pressed: %")
        }
        findViewById<Button>(R.id.btn_decimal)?.setOnClickListener {
            appendDecimal()
            Log.d(TAG, "Button pressed: .")
        }

        // Landscape-only buttons setup (scientific functions)
        if (resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
            findViewById<Button>(R.id.btnSin)?.setOnClickListener {
                calculateSingleOperandFunction(Math::sin)
                Log.d(TAG, "Button pressed: sin")
            }
            findViewById<Button>(R.id.btnCos)?.setOnClickListener {
                calculateSingleOperandFunction(Math::cos)
                Log.d(TAG, "Button pressed: cos")
            }
            findViewById<Button>(R.id.btnTan)?.setOnClickListener {
                calculateSingleOperandFunction(Math::tan)
                Log.d(TAG, "Button pressed: tan")
            }
            findViewById<Button>(R.id.btnLog10)?.setOnClickListener {
                calculateSingleOperandFunction(Math::log10)
                Log.d(TAG, "Button pressed: Log 10")
            }
            findViewById<Button>(R.id.btnLn)?.setOnClickListener {
                calculateSingleOperandFunction(Math::log)
                Log.d(TAG, "Button pressed: ln")
            }
        }
    }

    // Save state before orientation change
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_CURRENT_NUMBER, currentNumber)
        outState.putString(KEY_LAST_NUMBER, lastNumber)
        outState.putString(KEY_CURRENT_OPERATOR, currentOperator)
        outState.putBoolean(KEY_IS_NEW_INPUT, isNewInput)
        Log.d(TAG, "State saved: currentNumber=$currentNumber, lastNumber=$lastNumber, operator=$currentOperator")
    }

    // Restore state after orientation change
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        currentNumber = savedInstanceState.getString(KEY_CURRENT_NUMBER, "")
        lastNumber = savedInstanceState.getString(KEY_LAST_NUMBER, "")
        currentOperator = savedInstanceState.getString(KEY_CURRENT_OPERATOR)
        isNewInput = savedInstanceState.getBoolean(KEY_IS_NEW_INPUT, true)
        updateDisplay()
        Log.d(TAG, "State restored: currentNumber=$currentNumber, lastNumber=$lastNumber, operator=$currentOperator")
    }

    // Appends number to currentNumber
    private fun appendNumber(num: String) {
        if (isNewInput) {
            currentNumber = num
            isNewInput = false
        } else {
            currentNumber += num
        }
        updateDisplay()
    }

    // Logs and selects operator
    private fun logAndSelectOperator(operator: String) {
        selectOperator(operator)
        Log.d(TAG, "Operator selected: $operator")
    }

    // Handles operator selection
    private fun selectOperator(operator: String) {
        if (currentNumber.isNotEmpty()) {
            if (lastNumber.isNotEmpty() && currentOperator != null) {
                calculateResult()
            }
            lastNumber = currentNumber
            currentNumber = ""
        }
        currentOperator = operator
        isNewInput = true
    }

    // Performs calculation when equals button is pressed
    private fun calculateResult() {
        if (currentNumber.isNotEmpty() && lastNumber.isNotEmpty() && currentOperator != null) {
            val num1 = lastNumber.toDouble()
            val num2 = currentNumber.toDouble()
            val result = when (currentOperator) {
                "+" -> num1 + num2
                "-" -> num1 - num2
                "*" -> num1 * num2
                "/" -> if (num2 != 0.0) num1 / num2 else Double.NaN
                else -> num2
            }
            currentNumber = formatResult(result)
            lastNumber = ""
            currentOperator = null
            isNewInput = true
            updateDisplay()
            Log.d(TAG, "Result calculated: $currentNumber")
        }
    }

    // Performs single-operand scientific functions like sin, cos, etc.
    private fun calculateSingleOperandFunction(function: (Double) -> Double) {
        if (currentNumber.isNotEmpty()) {
            val num = currentNumber.toDouble()
            currentNumber = formatResult(function(num))
            updateDisplay()
        }
    }

    // Formats result: removes .0 from whole numbers
    private fun formatResult(result: Double): String {
        return if (result % 1 == 0.0) {
            result.toInt().toString()
        } else {
            result.toString()
        }
    }

    // Toggles sign of current number
    private fun toggleSign() {
        if (currentNumber.isNotEmpty()) {
            currentNumber = if (currentNumber.startsWith("-")) {
                currentNumber.substring(1)
            } else {
                "-$currentNumber"
            }
            updateDisplay()
        }
    }

    // Converts current number to percentage
    private fun applyPercentage() {
        if (currentNumber.isNotEmpty()) {
            val percentValue = currentNumber.toDouble() / 100
            currentNumber = formatResult(percentValue)
            updateDisplay()
        }
    }

    // Clears all input and resets state
    private fun clearAll() {
        currentNumber = ""
        lastNumber = ""
        currentOperator = null
        isNewInput = true
        updateDisplay()
    }

    // Adds decimal point if not already present
    private fun appendDecimal() {
        if (!currentNumber.contains(".")) {
            currentNumber += if (currentNumber.isEmpty()) "0." else "."
        }
        updateDisplay()
    }

    // Updates the display text
    private fun updateDisplay() {
        display.text = if (currentNumber.isNotEmpty()) currentNumber else "0"
    }
}


