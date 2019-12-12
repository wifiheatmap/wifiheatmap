package com.wifiheatmap.wifiheatmap

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    var mainActivity = ActivityTestRule(MainActivity::class.java)

    @Test
    fun testOpeningSettingsDialog() {
        onView(withId(R.id.settings_fab)).perform(click())
        assert(withId(R.id.toggle_color_blind_switch).matches(isDisplayed()))
    }

    @Test
    fun testTogglingDarkMode(){
        onView(withId(R.id.settings_fab)).perform(click())
        onView(withId(R.id.toggle_dark_mode_switch)).perform(click())
        onView(withText(R.string.apply)).perform(click())
        assert(withId(R.id.map).matches(isDisplayed()))
    }

}