package com.akirambow.usb.serial.lib

interface FTXXSerialDevice {
    enum class Result {
        OK,
        ERROR_DEVICE_NOT_FOUND,
        ERROR_ALREADY_OPEN,
        ERROR_ALREADY_RUNNING,
        ERROR_IS_NOT_RUNNING,
        ERROR_UNKNOWN_SERIAL,
        ERROR_FAIL_TO_OPEN_DEVICE,
        ERROR_DEVICE_IS_NOT_OPEN,
        ERROR_NOT_IMPLEMENTED,
        ERROR_IN_FAILURE,
        UNDEFINED,
    }

    fun open(param : FTXXDriverParameters) : Result
    fun write(data: ByteArray, offset : Int, count : Int): Int
    fun write(data: ByteArray, count : Int): Int
    fun write(data : ByteArray) : Int
    fun close()
}
