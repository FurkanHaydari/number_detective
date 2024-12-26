package com.brainfocus.numberdetective.game

import kotlin.random.Random

class Game {
    private val numbers = mutableListOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
    private val pathSelection = Random.nextInt(1, 4)

    private val firstAChoices = listOf("xax", "xxa")
    private val firstBChoices = listOf("bxx", "xxb")
    private val firstCChoices = listOf("cxx", "xcx")

    private val onlyAAndItIsCorrectChoices = listOf("axx")
    private val onlyBAndItIsCorrectChoices = listOf("xbx")
    private val onlyCAndItIsCorrectChoices = listOf("xxc")

    private val abFalse = listOf("bax", "bxa", "xab")
    private val acFalse = listOf("cax", "xca", "cxa")
    private val cbFalse = listOf("bcx", "cxb", "xcb")

    private val aTrueButRemainCFalse = listOf("acx")
    private val aTrueButRemainBFalse = listOf("axb")

    private val bTrueButRemainAFalse = listOf("xba")
    private val bTrueButRemainCFalse = listOf("cbx")

    private val cTrueButRemainAFalse = listOf("xac")
    private val cTrueButRemainBFalse = listOf("bxc")

    private lateinit var firstChoices: List<String>
    private lateinit var secondChoices: List<String>
    private lateinit var thirdChoices: List<String>
    private lateinit var fourthChoices: List<String>
    private lateinit var fifthChoices: List<String>

    private var hundredDigit = 0
    private var tenDigit = 0
    private var oneDigit = 0

    var targetNumber: Int = 0
        private set

    var firstHint: String = ""
        private set
    var secondHint: String = ""
        private set
    var thirdHint: String = ""
        private set
    var fourthHint: String = ""
        private set
    var fifthHint: String = ""
        private set

    fun play() {
        generateNumberWith3Digits()
        generateHintChoices()
        generateHints()
        generateReadableHints()
    }

    private fun generateNumberWith3Digits() {
        hundredDigit = numbers.random().also { numbers.remove(it) }
        tenDigit = numbers.random().also { numbers.remove(it) }
        oneDigit = numbers.random().also { numbers.remove(it) }
        targetNumber = hundredDigit * 100 + tenDigit * 10 + oneDigit
    }

    private fun generateHintChoices() {
        val temp = Random.nextBoolean()
        when (pathSelection) {
            1 -> {
                firstChoices = firstAChoices
                secondChoices = onlyAAndItIsCorrectChoices
                thirdChoices = if (temp) abFalse else acFalse
                fourthChoices = cbFalse
                fifthChoices = if (temp) bTrueButRemainCFalse else cTrueButRemainBFalse
            }
            2 -> {
                firstChoices = firstBChoices
                secondChoices = onlyBAndItIsCorrectChoices
                thirdChoices = if (temp) abFalse else cbFalse
                fourthChoices = acFalse
                fifthChoices = if (temp) aTrueButRemainCFalse else cTrueButRemainAFalse
            }
            else -> {
                firstChoices = firstCChoices
                secondChoices = onlyCAndItIsCorrectChoices
                thirdChoices = if (temp) acFalse else cbFalse
                fourthChoices = abFalse
                fifthChoices = if (temp) aTrueButRemainBFalse else bTrueButRemainAFalse
            }
        }
    }

    private fun generateHints() {
        firstHint = firstChoices.random()
        secondHint = secondChoices.random()
        thirdHint = thirdChoices.random()
        fourthHint = fourthChoices.random()
        fifthHint = fifthChoices.random()
    }

    private fun generateReadableHints() {
        firstHint = makeReadable(firstHint)
        secondHint = makeReadable(secondHint)
        thirdHint = makeReadable(thirdHint)
        fourthHint = makeReadable(fourthHint)
        fifthHint = makeReadable(fifthHint)
    }

    private fun makeReadable(hint: String): String {
        val uniqueIncorrectDigits = numbers.filterNot { 
            it == hundredDigit || it == tenDigit || it == oneDigit 
        }.shuffled().take(3)
        
        var xIndex = 0
        val switch = mapOf(
            'x' to { uniqueIncorrectDigits[xIndex++].toString() },
            'a' to { hundredDigit.toString() },
            'b' to { tenDigit.toString() },
            'c' to { oneDigit.toString() }
        )
        return hint.map { switch[it]?.invoke() ?: "" }.joinToString("")
    }
} 