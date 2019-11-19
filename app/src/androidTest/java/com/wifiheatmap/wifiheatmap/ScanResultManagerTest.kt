package com.wifiheatmap.wifiheatmap

import android.net.wifi.ScanResult
import org.junit.Test

import org.junit.Assert.*
import org.mockito.Mockito

class ScanResultManagerTest {

    /*class ScanResultWrapper {

        lateinit var scanResult : ScanResult

        fun ScanResultWrapper(scanResult: ScanResult) {
            this.scanResult = scanResult
        }

        fun getSSID(): String {
            return scanResult.SSID
        }

        fun setSSID(ssid : String) {
            this.scanResult.SSID = ssid
        }

        fun getLevel(): Int {
            return scanResult.level
        }

        fun setLevel(level : Int) {
            this.scanResult.level = level
        }

        fun getScan(): ScanResult {
            return this.scanResult
        }

    }*/

    // testing doesn't work. Can't mock final class.
    @Test
    fun removeDuplicatesFromScanResultsTest() {

        val scanResultManager = ScanResultManager()

        // mock the wifi manager start scan function
        var results : MutableList<ScanResult> = arrayListOf()

        // var scanResultX : ScanResultWrapper = ScanResultWrapper()
        // scanResultX.setSSID("Chris's Wi-Fi")
        // scanResultX.setLevel(-70)
        var scanResult1 : ScanResult = Mockito.mock(ScanResult::class.java)
        scanResult1.SSID = "Chris's Wi-Fi"
        scanResult1.level = -70
        results.add(scanResult1)

        var scanResult2 : ScanResult = Mockito.mock(ScanResult::class.java)
        scanResult2.SSID = "Chris's Wi-Fi"
        scanResult2.level = -65
        results.add(scanResult2)

        var scanResult3 : ScanResult = Mockito.mock(ScanResult::class.java)
        scanResult3.SSID = "Collin's Wi-Fi"
        scanResult3.level = -55
        results.add(scanResult3)

        var scanResult4 : ScanResult = Mockito.mock(ScanResult::class.java)
        scanResult4.SSID = "Collin's Wi-Fi"
        scanResult4.level = -30
        results.add(scanResult4)

        var scanResult5 : ScanResult = Mockito.mock(ScanResult::class.java)
        scanResult5.SSID = "Austin's Wi-Fi"
        scanResult5.level = -100
        results.add(scanResult5)

        var scanResult6 : ScanResult = Mockito.mock(ScanResult::class.java)
        scanResult6.SSID = "Austin's Wi-Fi"
        scanResult6.level = -34
        results.add(scanResult6)

        var scanResult7 : ScanResult = Mockito.mock(ScanResult::class.java)
        scanResult7.SSID = "Evan's Wi-Fi"
        scanResult7.level = -88
        results.add(scanResult7)

        var scanResult8 : ScanResult = Mockito.mock(ScanResult::class.java)
        scanResult8.SSID = "Evan's Wi-Fi"
        scanResult8.level = -15

        var scanResult9 : ScanResult = Mockito.mock(ScanResult::class.java)
        scanResult9.SSID = "Turner's Wi-Fi"
        scanResult9.level = -79
        results.add(scanResult9)

        var scanResult10 : ScanResult = Mockito.mock(ScanResult::class.java)
        scanResult10.SSID = "Turner's Wi-Fi"
        scanResult10.level = -44
        results.add(scanResult10)


        assertEquals(5, scanResultManager.removeDuplicatesFromScanResults(results).size)

    }
}