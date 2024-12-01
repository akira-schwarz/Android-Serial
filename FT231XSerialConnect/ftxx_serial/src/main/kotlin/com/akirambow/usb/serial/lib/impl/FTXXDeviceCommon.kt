package com.akirambow.usb.serial.lib.impl

import android.content.Context
import android.util.Log
import com.akirambow.usb.serial.lib.FTXXDriverParameters
import com.akirambow.usb.serial.lib.FTXXSerialDevice
import com.ftdi.j2xx.D2xxManager
import com.ftdi.j2xx.FT_Device
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

internal open class FTXXDeviceCommon constructor(
    private val appContext: Context
) : FTXXSerialDevice {

    protected enum class DeviceState {
        IDLE,
        OPENED,
        CONFIGURED,
        ERROR,
    }

    private val ftdiContext : D2xxManager = D2xxManager.getInstance(appContext)
    protected val interfaceLock = ReentrantLock()
    protected var state = DeviceState.IDLE
    protected var ftDevice : FT_Device? = null

    override fun open(param: FTXXDriverParameters): FTXXSerialDevice.Result {
        return interfaceLock.withLock {
            openInternal(param)
        }
    }

    private fun openInternal(param: FTXXDriverParameters) : FTXXSerialDevice.Result {
        val stateResult = when(state) {
            DeviceState.IDLE -> {
                FTXXSerialDevice.Result.OK
            }
            else -> {
                FTXXSerialDevice.Result.ERROR_ALREADY_OPEN
            }
        }
        if (FTXXSerialDevice.Result.OK != stateResult) {
            return stateResult
        }

        val deviceCount = ftdiContext.createDeviceInfoList(appContext)
        if (0 >= deviceCount) {
            return FTXXSerialDevice.Result.ERROR_DEVICE_NOT_FOUND
        }

        val deviceList = Array(deviceCount) { _ -> D2xxManager.FtDeviceInfoListNode() }
        ftdiContext.getDeviceInfoList(deviceCount, deviceList)

        Log.i(TAG, "DeviceInfo : ID     : ${deviceList[0].id}")
        Log.i(TAG, "DeviceInfo : Type   : ${deviceList[0].type}")
        Log.i(TAG, "DeviceInfo : Serial : ${deviceList[0].serialNumber}")
        Log.i(TAG, "DeviceInfo : Desc   : ${deviceList[0].description}")

        val deviceParam = D2xxManager.DriverParameters()
        deviceParam.setMaxBufferSize(param.maxBufferSize)
        deviceParam.setMaxTransferSize(param.maxTransferSize)

        var isOpen: Boolean
        ftDevice = ftdiContext.openBySerialNumber(appContext, deviceList[0].serialNumber, deviceParam).also { device ->
            isOpen = device.isOpen
        }

        if (null == ftDevice) {
            state = DeviceState.ERROR
            return FTXXSerialDevice.Result.ERROR_FAIL_TO_OPEN_DEVICE
        }

        if (!isOpen) {
            ftDevice?.close()
            return FTXXSerialDevice.Result.ERROR_FAIL_TO_OPEN_DEVICE
        }
        state = DeviceState.OPENED
        return FTXXSerialDevice.Result.OK
    }

    override fun write(data: ByteArray, offset : Int, count : Int): Int {
        return 0
    }

    override fun write(data: ByteArray, count : Int): Int {
        return 0
    }

    override fun write(data: ByteArray): Int {
        return 0
    }

    override fun close() {
        return interfaceLock.withLock {
            closeInternal()
        }
    }

    private fun closeInternal() {
        if ((DeviceState.OPENED != state) && (DeviceState.CONFIGURED != state)){
            return
        }
        ftDevice?.close()
        ftDevice = null
        state = DeviceState.IDLE
    }

    private companion object {
        const val TAG = "FTXXDeviceCommon"
    }

}