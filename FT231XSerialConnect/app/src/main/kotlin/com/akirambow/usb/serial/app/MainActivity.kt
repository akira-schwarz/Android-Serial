package com.akirambow.usb.serial.app

import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.akirambow.usb.serial.lib.FTXXDeviceName
import com.akirambow.usb.serial.lib.FTXXDriverParameters
import com.akirambow.usb.serial.lib.FTXXSerialDevice
import com.akirambow.usb.serial.lib.FTXXSerialDeviceFactory

class MainActivity : AppCompatActivity() {
    private var mainHandler : Handler? = null
    private var mainHandlerThread : HandlerThread? = null

    private var serialDevice : FTXXSerialDevice? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onResume() {
        super.onResume()
        mainHandlerThread = HandlerThread("ft231x-serial-connect")
        mainHandlerThread?.let { thread ->
            thread.start()
            mainHandler = Handler(thread.looper)
        }
        mainHandler?.post(openDeviceRunnable)
    }

    override fun onPause() {
        super.onPause()
        mainHandlerThread?.quitSafely()
        mainHandlerThread = null
        mainHandler = null
    }

    private val openDeviceRunnable = Runnable {
        serialDevice = FTXXSerialDeviceFactory.create(FTXXDeviceName.FT231X, applicationContext)
        serialDevice?.let { device ->
            val driverParam = FTXXDriverParameters.Builder().build()
            val result = device.open(driverParam)
            if (FTXXSerialDevice.Result.OK == result) {
                Log.i(TAG, "Device opened successfully.")
                mainHandler?.postDelayed(writeFirstDataRunnable, 3000L)
            }
            else {
                Log.e(TAG, "Fail to open device because ${result.toString()}")
            }
        }
    }

    private val writeFirstDataRunnable = Runnable {
        val data = "FirstData\n".encodeToByteArray()
        val result = serialDevice?.write(data) ?: Int.MIN_VALUE
        if (0 < result) {
            Log.i(TAG, "FirstData may be written successfully. : count=$result")
            mainHandler?.postDelayed(writeSecondDataRunnable, 3000L)
        }
        else {
            Log.e(TAG, "Fail to write FirstData because of code $result")
            mainHandler?.postDelayed(closeDeviceRunnable, 1000L)
        }
    }

    private val writeSecondDataRunnable = Runnable {
        val data = "SecondData\n".encodeToByteArray()
        val result = serialDevice?.write(data) ?: Int.MIN_VALUE
        if (0 < result) {
            Log.i(TAG, "SecondData may be written successfully. : count=$result")
        }
        else {
            Log.e(TAG, "Fail to write SecondData because of code $result")
        }
        mainHandler?.postDelayed(closeDeviceRunnable, 1000L)
    }

    private val closeDeviceRunnable = Runnable {
        serialDevice?.close()
        finishAndRemoveTask()
    }

    private companion object {
        const val TAG = "FT231XSerialConnect"
    }
}