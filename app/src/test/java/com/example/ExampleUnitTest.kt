package com.example

import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testVirtual10DigitCallerIdGeneration() {
        val firstDigit = Random.nextInt(6, 10)
        val remaining9Digits = (1..9).map { Random.nextInt(0, 10) }.joinToString("")
        val tenDigits = "$firstDigit$remaining9Digits"
        val fullCallerId = "+1 $tenDigits"

        assertEquals(10, tenDigits.length)
        assertTrue(tenDigits.all { it.isDigit() })
        assertTrue(fullCallerId.startsWith("+1 "))
    }
}
