package com.akirambow.usb.serial.lib

import android.content.Context
import com.akirambow.usb.serial.lib.impl.FT231XSerialDevice

class FTXXSerialDeviceFactory {
    companion object {
        fun create(deviceName : FTXXDeviceName, appContext: Context) : FTXXSerialDevice? {
            return when(deviceName) {
                FTXXDeviceName.FT231X -> {
                    FT231XSerialDevice(appContext)
                }
            }
        }
    }
}