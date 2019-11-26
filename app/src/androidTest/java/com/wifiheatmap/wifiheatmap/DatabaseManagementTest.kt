package com.wifiheatmap.wifiheatmap

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.LiveData
import androidx.room.Room
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import com.wifiheatmap.wifiheatmap.room.DataAccessObject
import com.wifiheatmap.wifiheatmap.room.LiveDataPromise
import com.wifiheatmap.wifiheatmap.room.WifiDatabase
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseManagementTest {
    var database: WifiDatabase? = null
    var dataAccessObject: DataAccessObject? = null

    @Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    @Before fun setupDb() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().context,
            WifiDatabase::class.java
        ).build()
        dataAccessObject = database?.dataAccessObject()
    }

    @After fun cleanupDb() {
        database?.close()
        database = null
        dataAccessObject = null
    }

    @Test fun scenarioDisplays(){
        val scenario = launchFragmentInContainer<DatabaseManagementFragment>()
        onView(withId(R.id.dbm_fragment_title)).check(matches(isDisplayed()))
    }

    /**
     * Allows us to block until we get information back from the database
     * so that we can check that data is updated correctly.
     * @param liveData a [LiveData] object from the database.
     */
    private fun <T> await(liveData: LiveData<T>): T? {
        val liveDataPromise = LiveDataPromise(liveData)
        return liveDataPromise.await()
    }
}