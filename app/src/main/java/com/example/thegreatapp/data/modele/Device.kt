package com.example.thegreatapp.data.modele

import android.bluetooth.BluetoothDevice
    // Représente les données
    data class Device(
        var name: String?,
        var mac: String?,
        var device: BluetoothDevice
    ) {
        override fun equals(other: Any?): Boolean {
            return other is Device && other.mac == this.mac
        }
    }
