package com.example.thegreatapp.internet

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.thegreatapp.R
import com.example.thegreatapp.data.ApiService
import com.example.thegreatapp.data.modele.LedStatus
import com.example.thegreatapp.data.local.LocalPreferences
import kotlinx.android.synthetic.main.activity_internet.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InternetActivity : AppCompatActivity() {

    var ledStatus = LedStatus()
    var API = ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_internet)

        if (LocalPreferences.getInstance(this).lastConnectedDeviceName() == null) {
            finish()
        } else {
            LocalPreferences.getInstance(this).lastConnectedDeviceName()?.let {
                ledStatus.setIdentifier(it)
            }
        }

        getStatus()

        //Bouton changement d'état de la LED
        ledButton.setOnClickListener {
            changeLed();
        }

        //Bouton refresh
        refresh.setOnClickListener {
            getStatus()
        }

    }

    private fun setVisualState() {
        if (ledStatus.status) {
            ledStatus2.setImageResource(R.drawable.light_logo_foreground)
        } else {
            ledStatus2.setImageResource(R.drawable.dark_logo_foreground)
        }
    }

    //Fonction appelée à l'initialisation et au refresh
    private fun getStatus() {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                val readStatus = ApiService.instance.readStatus(ledStatus.identifier)
                ledStatus.setStatus(readStatus.status)
                setVisualState() // Change image par light ou dark
            }
        }
    }

    private fun changeLed() {
        ledStatus.reverseStatus()
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                API.instance.writeStatus(ledStatus)
                setVisualState() // Change image par light ou dark
            }
        }
    }
}
