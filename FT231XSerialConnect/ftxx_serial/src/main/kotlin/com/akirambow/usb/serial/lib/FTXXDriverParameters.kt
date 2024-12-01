package com.akirambow.usb.serial.lib

class FTXXDriverParameters private constructor(
    val maxBufferSize : Int,
    val maxTransferSize : Int,
    val baudRate: Int,
){
    class Builder {
        private var maxBufferSize = 0x10000
        private var maxTransferSize = 4096
        private var baudRate = 115200

        fun setMaxBufferSize(bufferSize : Int) : Builder {
            if (0 < bufferSize) {
                this.maxBufferSize = bufferSize
            }
            return this
        }

        fun setMaxTransferSize(transferSize : Int) : Builder {
            if ((0 < transferSize) && (0 == transferSize % 1024)) {
                this.maxTransferSize = transferSize
            }
            return this
        }

        fun setBaudRate(baudRate : Int) : Builder {
            val foundValue = SUPPORTED_BAUD_RATE.find { it == baudRate }
            foundValue?.let { rate ->
                this.baudRate = rate
            } ?: {
                throw IllegalArgumentException("Unsupported value for baud rate")
            }
            return this
        }

        fun build() : FTXXDriverParameters {
            return FTXXDriverParameters(
                this.maxBufferSize,
                this.maxTransferSize,
                this.baudRate)
        }
    }

    companion object {
        val SUPPORTED_BAUD_RATE = intArrayOf(
            300, 600, 1200, 2400, 4800, 9600, 14400, 19200,
            38400, 56000, 57600, 115200, 128000, 256000)
    }
}
