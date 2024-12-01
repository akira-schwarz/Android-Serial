package com.akirambow.usb.serial.lib.impl

import android.content.Context
import android.util.Log
import com.akirambow.usb.serial.lib.FTXXDriverParameters
import com.akirambow.usb.serial.lib.FTXXSerialDevice
import com.ftdi.j2xx.D2xxManager
import kotlin.concurrent.withLock
import kotlin.experimental.or

internal class FT231XSerialDevice(appContext: Context) : FTXXDeviceCommon(appContext) {
    override fun open(param: FTXXDriverParameters): FTXXSerialDevice.Result {
        val result = super.open(param)
        if (FTXXSerialDevice.Result.OK != result) {
            return result
        }

        interfaceLock.withLock {
            ftDevice?.let { device ->
                device.setBitMode(0x00, D2xxManager.FT_BITMODE_RESET)
                device.setBaudRate(param.baudRate)
                device.setDataCharacteristics(
                    D2xxManager.FT_DATA_BITS_8,
                    D2xxManager.FT_STOP_BITS_1,
                    D2xxManager.FT_PARITY_NONE)
                device.setFlowControl(D2xxManager.FT_FLOW_NONE, 0x0B, 0x0D)
                device.purge(D2xxManager.FT_PURGE_TX or D2xxManager.FT_PURGE_RX)
                device.restartInTask()
                val testData = "FT231XSerialDevice"
                val writeCount = device.write(testData.encodeToByteArray())
                Log.i(TAG, "writeCount=$writeCount")
                state = DeviceState.CONFIGURED
            }
        }
        return FTXXSerialDevice.Result.OK
    }

    override fun write(data: ByteArray, offset: Int, count: Int) : Int {
        if (data.isEmpty() || (0 > offset) || (0 >= count)) {
            throw IllegalArgumentException("Valid data, offset, count must be specified.")
        }

        if (data.size <= offset) {
            throw IllegalArgumentException("offset and/or count indicates out of range of data.")
        }

        val writeCount = if (data.size < offset + count) {
            data.size - offset
        } else {
            count
        }

        return interfaceLock.withLock {
            writeInternal(data, offset, writeCount)
        }
    }

    override fun write(data: ByteArray, count: Int): Int
    {
        return write(data, 0, count)
    }

    override fun write(data: ByteArray): Int {
        return write(data, 0, data.size)
    }

    private fun writeInternal(data: ByteArray, offset: Int, count: Int): Int {
        val stateResult = when(state) {
            DeviceState.ERROR -> { FTXXSerialDevice.Result.ERROR_IN_FAILURE }
            DeviceState.IDLE -> { FTXXSerialDevice.Result.ERROR_DEVICE_IS_NOT_OPEN }
            DeviceState.OPENED -> { FTXXSerialDevice.Result.ERROR_IS_NOT_RUNNING }
            else -> {
                FTXXSerialDevice.Result.OK
            }
        }
        if (FTXXSerialDevice.Result.OK != stateResult) {
            return -stateResult.ordinal
        }
        val writeData = data.copyOfRange(offset, offset + count)
        return ftDevice?.write(writeData) ?: 0
    }
/*
    override fun close() {
        super.close()
    }
     */

    private companion object {
        const val TAG = "FT231XSerialDevice"
    }
}