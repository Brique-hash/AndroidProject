package com.example.thegreatapp.data

import android.bluetooth.*
import java.util.*

class BluetoothLEManager {
    companion object {
        var currentDevice: BluetoothDevice? = null

        val DEVICE_UUID: UUID = UUID.fromString("795090c7-420d-4048-a24e-18e60180e23c")
        val CHARACTERISTIC_LED_PIN_UUID: UUID =
            UUID.fromString("31517c58-66bf-470c-b662-e352a6c80cba")
        val CHARACTERISTIC_BUTTON_PIN_UUID: UUID =
            UUID.fromString("0b89d2d4-0ea6-4141-86bb-0c5fb91ab14a")
        val CHARACTERISTIC_TOGGLE_LED_UUID: UUID =
            UUID.fromString("59b6bf7f-44de-4184-81bd-a0e3b30c919b")
        val CHARACTERISTIC_NOTIFY_STATE: UUID =
            UUID.fromString("d75167c8-e6f9-4f0b-b688-09d96e195f00")
    }

    open class GattCallback(
        val onConnect: () -> Unit,
        val onNotify: (characteristic: BluetoothGattCharacteristic) -> Unit,
        val onDisconnect: () -> Unit
    ) : BluetoothGattCallback() {

        /**
         * Méthode appelé au moment ou les « services » ont été découvert
         */
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                onConnect()
            } else {
                onDisconnect()
            }
        }

        /**
         * Méthode appelé au moment du changement d'état de la stack BLE
         */
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            when (newState) {
                BluetoothGatt.STATE_CONNECTED -> gatt.discoverServices()
                BluetoothProfile.STATE_DISCONNECTED -> onDisconnect()
            }
        }

        /**
         * Méthodes appelée à chaque notifications BLE (en cas de changement de la led)
         */
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
            if (characteristic.uuid == CHARACTERISTIC_NOTIFY_STATE) {
                onNotify(characteristic)
            }
        }
    }

}