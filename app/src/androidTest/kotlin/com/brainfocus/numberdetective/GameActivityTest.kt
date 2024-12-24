package com.brainfocus.numberdetective

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(GameActivity::class.java)

    @Test
    fun testInitialUIState() {
        // Başlangıç durumunda UI elemanlarının kontrolü
        onView(withId(R.id.scoreText))
            .check(matches(isDisplayed()))
            .check(matches(withText("Skor: 1000")))

        onView(withId(R.id.attemptsText))
            .check(matches(isDisplayed()))
            .check(matches(withText("Kalan Hak: 3")))

        onView(withId(R.id.guessButton))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
    }

    @Test
    fun testInvalidInput() {
        // Geçersiz giriş testi
        onView(withId(R.id.digit1)).perform(typeText(""))
        onView(withId(R.id.digit2)).perform(typeText("1"))
        onView(withId(R.id.digit3)).perform(typeText("2"))

        onView(withId(R.id.guessButton)).perform(click())

        // Snackbar hata mesajının kontrolü
        onView(withText("Lütfen 3 basamaklı bir sayı girin"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testValidInput() {
        // Geçerli giriş testi
        onView(withId(R.id.digit1)).perform(typeText("1"))
        onView(withId(R.id.digit2)).perform(typeText("2"))
        onView(withId(R.id.digit3)).perform(typeText("3"))

        onView(withId(R.id.guessButton)).perform(click())

        // Kalan hak sayısının azaldığını kontrol et
        onView(withId(R.id.attemptsText))
            .check(matches(withText("Kalan Hak: 2")))
    }

    @Test
    fun testGameOver() {
        // 3 yanlış tahmin ile oyunun bitmesi testi
        repeat(3) {
            onView(withId(R.id.digit1)).perform(typeText("1"))
            onView(withId(R.id.digit2)).perform(typeText("2"))
            onView(withId(R.id.digit3)).perform(typeText("3"))
            onView(withId(R.id.guessButton)).perform(click())
            
            // Her tahminden sonra input alanlarını temizle
            onView(withId(R.id.digit1)).perform(clearText())
            onView(withId(R.id.digit2)).perform(clearText())
            onView(withId(R.id.digit3)).perform(clearText())
        }

        // Tahmin butonunun devre dışı kaldığını kontrol et
        onView(withId(R.id.guessButton))
            .check(matches(not(isEnabled())))
    }
}
