package com.example.thegreatapp.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.example.thegreatapp.R
import com.example.thegreatapp.bluetooth.Bluetooth_Activity
import com.example.thegreatapp.internet.InternetActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_bluetooth.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.setting).setOnClickListener {
            val targetIntent = Intent().apply {
                action = Settings.ACTION_SETTINGS
            }
            startActivity(targetIntent);
        }

        //Bouton pour accéder à la fonction bluetooth
        findViewById<Button>(R.id.bluetooth).setOnClickListener {
            startActivity(Bluetooth_Activity.getStartIntent(this))
        }

        //Bouton pour accéder google map
        findViewById<Button>(R.id.earth).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:37.1406,115.4840")));
        }

        //Bouton un peu inutile mais pour montrer un toast
        findViewById<Button>(R.id.uselessButton).setOnClickListener {
            Toast.makeText(this, "I know you didn't say it...", Toast.LENGTH_SHORT).show()
        }

        //Bouton pour accéder au controle par internet
        findViewById<Button>(R.id.internet).setOnClickListener {
            startActivity(InternetActivity.getStartIntent(this,));
        }

        MaterialDialog(this).show {
            title(R.string.app_name)
            message(R.string.welcome) {
                html() // format, color, etc. with tags in string
                html { link ->  // same as above, but...
                    // Invokes a callback when a URL is clicked instead of auto opening it in a browser
                }
                lineSpacing(2.0f)


            }
            positiveButton(R.string.enter) { dialog ->
                //Ne fait rien
            }
            negativeButton(R.string.out) { dialog ->
                //On ferme les activity
                finishAffinity()
            }

            icon(R.mipmap.icone_app)
        }
    }

    companion object {
            fun getStartIntent(context: Context): Intent {
                return Intent(context, MainActivity::class.java)
            }
    }
}