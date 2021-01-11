package com.example.thegreatapp.internet

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.thegreatapp.R
import com.example.thegreatapp.data.ApiService
import com.example.thegreatapp.main.MainActivity
import kotlinx.android.synthetic.main.activity_internet.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InternetActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_internet)


       // CoroutineScope(Dispatchers.IO).launch {
         //runCatching {
        //val readStatus = ApiService.instance.readStatus(ledStatus.identifier)
          //      ledStatus.setStatus(readStatus.status)
            //    setVisualState()
            //}
        //}

        ledStatus2.setOnClickListener{

        }

       // private fun getIdentifiant(): String? {
         //   return intent.extras?.getString(IDENTIFIANT_ID, null)
        //}
    }
        companion object {
            fun getStartIntent(context: Context): Intent {
                return Intent(context, MainActivity::class.java)
            }
        }
    }
