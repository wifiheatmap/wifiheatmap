package com.wifiheatmap.wifiheatmap

import android.net.wifi.ScanResult
import android.net.wifi.WifiManager

class ScanResultManager {


    fun removeDuplicatesFromScanResults(results: List<ScanResult>): List<ScanResult> {

        var nonDuplicatedFinalResults: List<ScanResult> = arrayListOf()

        var nonDuplicatedResults2: HashMap<String, ScanResult> = hashMapOf()
        for (scanResult in results) {
            // check to make sure the SSID is not ""
            if (scanResult.SSID != "") {
                println(
                    scanResult.SSID + " | Strength: " + WifiManager.calculateSignalLevel(
                        scanResult.level,
                        5
                    )
                )
                // if it is not yet contained in the list add it
                if (!nonDuplicatedResults2.contains(scanResult.SSID)) {

                    // nonDuplicatedResults.add(scanResult.SSID)
                    // always add 110 to the scanResult.level to make the scale be from 0 to +.
                    var wifiStrength = scanResult.level

                    nonDuplicatedResults2.put(scanResult.SSID, scanResult)

                } else {
                    var newWifiStrength = scanResult.level
                    var strengthOfExistingWiFi = nonDuplicatedResults2.getValue(scanResult.SSID).level
                    if (strengthOfExistingWiFi < newWifiStrength) {
                        // if new wifi strength is greater than the old one
                        // replace old entry with this new one.
                        nonDuplicatedResults2.remove(scanResult.SSID)
                        nonDuplicatedResults2.put(scanResult.SSID, scanResult)

                    }
                }
            }
        }
        nonDuplicatedFinalResults = nonDuplicatedResults2.values.toList()
        return nonDuplicatedFinalResults
    }

}