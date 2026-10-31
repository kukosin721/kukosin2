package com.kukosin.app.playback

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UsbDacInfo(
    val hasUsbDevice: Boolean = false,
    val deviceName: String = "",
    val vendorId: Int = 0,
    val productId: Int = 0,
    val hasPermission: Boolean = false,
    val isDirectHardwareAccess: Boolean = false,
    val usbDevice: UsbDevice? = null,
)

object UsbDacManager {
    private const val ACTION_USB_PERMISSION = "com.kukosin.app.USB_PERMISSION"

    private val statusFlow = MutableStateFlow(UsbDacInfo())
    val current: StateFlow<UsbDacInfo> = statusFlow.asStateFlow()

    private var receiverRegistered = false
    private var permissionRequested = false

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (ACTION_USB_PERMISSION == action) {
                synchronized(this) {
                    val device: UsbDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                    }
                    val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                    if (device != null) {
                        scanDevices(context)
                    }
                }
            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED == action || UsbManager.ACTION_USB_DEVICE_DETACHED == action) {
                scanDevices(context)
            }
        }
    }

    fun init(context: Context) {
        if (!receiverRegistered) {
            val filter = IntentFilter().apply {
                addAction(ACTION_USB_PERMISSION)
                addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
                addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(usbReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                context.registerReceiver(usbReceiver, filter)
            }
            receiverRegistered = true
        }
        scanDevices(context)
    }

    fun requestPermission(context: Context) {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as? UsbManager ?: return
        val device = findAudioDevice(usbManager) ?: return
        if (usbManager.hasPermission(device)) {
            permissionRequested = false
            scanDevices(context)
            return
        }
        if (permissionRequested) return
        permissionRequested = true

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val permissionIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(ACTION_USB_PERMISSION),
            flags,
        )
        usbManager.requestPermission(device, permissionIntent)
    }

    fun scanDevices(context: Context) {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as? UsbManager
        if (usbManager == null) {
            permissionRequested = false
            statusFlow.value = UsbDacInfo()
            return
        }

        val device = findAudioDevice(usbManager)
        if (device == null) {
            permissionRequested = false
            statusFlow.value = UsbDacInfo()
            return
        }

        val name = device.productName?.trim().orEmpty().ifBlank { device.deviceName }
        val hasPerm = usbManager.hasPermission(device)

        statusFlow.value = UsbDacInfo(
            hasUsbDevice = true,
            deviceName = name,
            vendorId = device.vendorId,
            productId = device.productId,
            hasPermission = hasPerm,
            isDirectHardwareAccess = hasPerm,
            usbDevice = device,
        )
    }

    private fun findAudioDevice(usbManager: UsbManager): UsbDevice? {
        val devices = usbManager.deviceList.values
        return devices.firstOrNull { isAudioUsbDevice(it) }
    }

    private fun isAudioUsbDevice(device: UsbDevice): Boolean {
        if (device.deviceClass == UsbConstants.USB_CLASS_AUDIO) return true
        for (i in 0 until device.interfaceCount) {
            val iface = device.getInterface(i)
            if (iface.interfaceClass == UsbConstants.USB_CLASS_AUDIO) return true
        }
        return false
    }
}
