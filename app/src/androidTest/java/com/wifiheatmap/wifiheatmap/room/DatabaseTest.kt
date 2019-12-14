package com.wifiheatmap.wifiheatmap.room

import androidx.lifecycle.LiveData
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.Assert.fail
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class DatabaseTest {
    var database: WifiDatabase? = null
    var dataAccessObject: DataAccessObject? = null

    @Before
    fun setupDb() {
        database = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().context, WifiDatabase::class.java)
            .build()
        dataAccessObject = database?.dataAccessObject()
    }

    private fun <T> await(liveData: LiveData<T>): T? {
        val liveDataPromise = LiveDataPromise(liveData)
        return liveDataPromise.await()
    }

    @After
    fun cleanupDb() {
        database?.close()
        database = null
        dataAccessObject = null
    }

    @Test
    fun testDb() {
        val dao = dataAccessObject
        if(dao == null) {
            fail()
            return
        }
        val testNetwork = Network("Test Network 1", false)
        dao.insertNetwork(testNetwork)
        Assert.assertEquals(await(dao.getNetworks())?.size, 1)
        Assert.assertEquals(await(dao.getNetworks())?.get(0), testNetwork)

        val testNetwork2 = Network("Test Network 2", false)
        dao.insertNetwork(testNetwork2)
        Assert.assertEquals(await(dao.getNetworks())?.size, 2)
        Assert.assertEquals(await(dao.getNetworks())?.get(1), testNetwork2)

        val testData = arrayOf(
            Data(1, "Test Network 2", 12.0, 42.0, 15, Date()),
            Data(2, "Test Network 2", 1.0, 1.0, 16, Date()),
            Data(3, "Test Network 1", 2.0, 2.0, 13, Date()),
            Data(4, "Test Network 2", 3.0, 3.0, 14, Date()),
            Data(5, "Test Network 1", 4.0, 4.0, 10, Date()),
            Data(6, "Test Network 2", 5.0, 5.0, 10, Date())
        )

        val testNetworkTwoData = arrayOf(
            testData[0],
            testData[1],
            testData[3],
            testData[5]
        )

        val testNetworkOneData = arrayOf(
            testData[2],
            testData[4]
        )

        val testBoundedData = arrayOf(
            testData[2],
            testData[4]
        )

        dao.insertData(*testData)

        assert(await(dao.getData("Test Network 2"))?.containsAll(testNetworkTwoData.toList()) ?: false)
        assert(await(dao.getData("Test Network 1"))?.containsAll(testNetworkOneData.toList()) ?: false)

        assert(await(dao.getData("Test Network 1", 1.5, 1.5, 4.5, 4.5))?.containsAll(testBoundedData.toList()) ?: false)

        val newNetworkZero = Network("Network Zero", true)

        dao.insertNetwork(newNetworkZero)
        assert(await(dao.getNetworks())?.contains(newNetworkZero) ?: false)

    }
}