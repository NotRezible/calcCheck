package com.example.calccheck

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.view.View
import android.widget.EditText
import com.example.calccheck.databinding.ActivityMainBinding
import java.lang.Exception
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    class NumberExample(val firstOperand: Int, val secondOperand: Int, val operand: String)
    private var correctAnswers = 0
    private var wrongAnswers = 0
    private var isCorrect = true
    private var isPlayed = false
    private var timerStarted = false
    private lateinit var serviceIntent: Intent
    private var totalTime = 0.0
    private var averageTime = 0.0
    private var maxTime = 0.0
    private var minTime = 0.0
    private var time = 0.0

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        serviceIntent = Intent(applicationContext, TimerService::class.java)
        registerReceiver(updateTime, IntentFilter(TimerService.TIMER_UPDATED))
    }

    @SuppressLint("SetTextI18n")
    fun btnStart(view: View) {
        var firstOperand = (10..99).random()
        var secondOperand = (10..99).random()
        val operators = arrayOf('*', '/', '-', '+')
        val operator = operators.random()
        if (operator == '/')
        {
            while(firstOperand  % secondOperand != 0)
            {
                firstOperand = (10..99).random()
                secondOperand = (10..99).random()
            }
        }
        val example = NumberExample(firstOperand, secondOperand, operator.toString())
        val correctAnswer = getAnswer(example)
        val answer = generateAnswer(correctAnswer)
        binding.taskTextView.text = "$firstOperand $operator $secondOperand = $answer"
        isPlayed = true
        changeBtnEnable()
        binding.taskTextView.setBackgroundColor(Color.WHITE)
        startTimer()
    }

    private fun generateAnswer(correctAnswer: Int): Int {
        val choice = (0..1).random()
        val operators = arrayOf('-', '+')
        val operator = operators.random()
        val randomNumber = (-10..10).random()
        if(choice == 1) {
            isCorrect = false
            val example = NumberExample(correctAnswer, randomNumber, operator.toString())
            return getAnswer(example);
        }
        isCorrect = true
        return correctAnswer
    }


    private fun getAnswer(example: NumberExample): Int {
        var result = 0

        when (example.operand){
            "/" -> result = example.firstOperand / example.secondOperand
            "*" -> result = example.firstOperand * example.secondOperand
            "+" -> result = example.firstOperand + example.secondOperand
            "-" -> result = example.firstOperand - example.secondOperand
        }
        return result
    }

    fun btnCorrect(view: View) {
        isPlayed = false
        if(isCorrect)
            correctAnswers++
        else
            wrongAnswers++

        changeBtnEnable()
        updateStatistics()
        changeBgColorExample()
        resetTimer()
    }

    fun btnWrong(view: View) {
        isPlayed = false
        if(!isCorrect)
            correctAnswers++
        else
            wrongAnswers++

        changeBtnEnable()
        updateStatistics()
        changeBgColorExample()
        resetTimer()
    }

    private fun changeBtnEnable() {
        binding.wrongButton.isEnabled = isPlayed
        binding.correctButton.isEnabled = isPlayed
        binding.startButton.isEnabled = !isPlayed
    }

    private fun updateStatistics() {
        val totalAnswers = correctAnswers+wrongAnswers
        val percent = if (totalAnswers > 0) {
            "%.2f".format(correctAnswers * 100.0 / totalAnswers)
        } else {
            "0.00"
        }
        binding.totalAnswerTextView.text = totalAnswers.toString()
        binding.countWrongAnswerTextView.text = wrongAnswers.toString()
        binding.countCorrectAnswerTextView.text = correctAnswers.toString()
        binding.percentCorrectAnswersTextView.text = percent
    }

    private fun changeBgColorExample() {
        if(isCorrect) {
            binding.taskTextView.setBackgroundColor(Color.GREEN)
        } else {
            binding.taskTextView.setBackgroundColor(Color.RED)
        }
    }

    private fun startTimer()
    {
        serviceIntent.putExtra(TimerService.TIME_EXTRA, time)
        startService(serviceIntent)
        timerStarted = true
    }

    private fun stopTimer()
    {
        stopService(serviceIntent)
        timerStarted = false
    }

    private val updateTime: BroadcastReceiver = object : BroadcastReceiver()
    {
        override fun onReceive(context: Context, intent: Intent)
        {
            time = intent.getDoubleExtra(TimerService.TIME_EXTRA, 0.0)
            binding.timeTextView.text = getTimeStringFromDouble(time)
        }
    }
    private fun makeTimeString(hour: Int, min: Int, sec: Int): String = String.format("%02d:%02d:%02d", hour, min, sec)

    private fun getTimeStringFromDouble(time: Double): String
    {
        val resultInt = time.roundToInt()
        val hours = resultInt % 86400 / 3600
        val minutes = resultInt % 86400 % 3600 / 60
        val seconds = resultInt % 86400 % 3600 % 60

        return makeTimeString(hours, minutes, seconds)
    }

    private fun resetTimer()
    {
        stopTimer()
        totalTime += time
        if (time < minTime || minTime == 0.0) {
            minTime = time
            binding.minTimeTextView.text = time.toString()
        }
        if (time > maxTime) {
            maxTime = time
            binding.maxTimeTextView.text = time.toString()
        }
        averageTime = Math.round(totalTime / (correctAnswers+wrongAnswers)).toDouble()
        binding.avgTimeTextView.text = averageTime.toString()
        time = 0.0
        binding.timeTextView.text = getTimeStringFromDouble(time)
    }

}