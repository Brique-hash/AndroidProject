package com.example.thegreatapp.bluetooth

import android.bluetooth.le.BluetoothLeScanner
import android.Manifest
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.os.Handler
import android.os.ParcelUuid
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.recyclical.datasource.emptyDataSourceTyped
import com.afollestad.recyclical.setup
import com.afollestad.recyclical.withItem
import com.example.thegreatapp.data.manager.BluetoothLEManager
import com.example.thegreatapp.data.holder.DeviceViewHolder
import com.example.thegreatapp.data.modele.Device
import com.example.thegreatapp.R
import com.example.thegreatapp.data.local.LocalPreferences
import kotlinx.android.synthetic.main.activity_bluetooth.*

class Bluetooth_Activity : AppCompatActivity() {
    /**Gère l'action après la demande de permission. 2 cas possibles :
     * - Réussite 🎉.
     * - Échec (refus utilisateur).*/
    companion object {
        fun getStartIntent(context: Context): Intent {
            return Intent(context, Bluetooth_Activity::class.java)
        }

        const val PERMISSION_REQUEST_LOCATION = 9999
        const val REQUEST_ENABLE_BLE = 9997
    }

    // Gestion du Bluetooth
    // L'Adapter permettant de se connecter
    private var bluetoothAdapter: BluetoothAdapter? = null

    // La connexion actuellement établie
    private var currentBluetoothGatt: BluetoothGatt? = null

    // « Interface système nous permettant de scanner »
    private var bluetoothLeScanner: BluetoothLeScanner? = null

    /**Gestion du SCAN, recherche des device BLE à proximité*/

    // Parametrage du scan BLE
    private val scanSettings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()

    // On ne retourne que les « Devices » proposant le bon UUID
    private var scanFilters: List<ScanFilter> = arrayListOf(
            //Enlever les addresses MAC
            ScanFilter.Builder().setServiceUuid(ParcelUuid(BluetoothLEManager.DEVICE_UUID)).build()
    )

    // Variable de fonctionnement
    private var mScanning = false
    private val handler = Handler()

    // Adapter
    private val bleDevicesFoundList = emptyDataSourceTyped<Device>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth)
        setUiMode(false)

        Recycler_Bluetooth.setup {
            withDataSource(bleDevicesFoundList)
            withItem<Device, DeviceViewHolder>(R.layout.item_bluetooth) {
                onBind(::DeviceViewHolder) { _, item -> name.text = (item.name.takeIf { !it.isNullOrEmpty() } ?: run { item.mac })
                }
                onClick {
                    Toast.makeText(this@Bluetooth_Activity, getString(R.string.connection_to, item.name), Toast.LENGTH_SHORT).show()
                    BluetoothLEManager.currentDevice = item.device
                    connectToCurrentDevice()
                }
            }
        }
        //on scan les peripheriques quand on clique
        startScan.setOnClickListener {
            askForPermission()
            if (hasPermission()) {
                setupBLE()
            }
        }

        //On se deconnecte du peripherique quand on clique
        disconnect.setOnClickListener {
            disconnectFromCurrentDevice()
        }
        //On change l'état de la led quand on clique
        toggleLed.setOnClickListener {
            toggleLed()
        }
    }

    /* Gère l'action après la demande de permission. 2 cas possibles :
     * - Réussite 🎉.
     * - Échec (refus utilisateur).*/
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST_LOCATION && grantResults.size == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && locationServiceEnabled()) {
                // Permission OK => Lancer SCAN
                setupBLE()
            } else if (!locationServiceEnabled()) {
                // Inviter à activer la localisation
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                //Sinon toast pour dire que cela ne fonctionne pas
            } else {
                Toast.makeText(this@Bluetooth_Activity, getString(R.string.nolocalisation), Toast.LENGTH_SHORT).show()
                finish()
                    }
                }
            }

    /*Permet de vérifier si l'application possede la permission « Localisation ». OBLIGATOIRE pour scanner en BLE*/
    private fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    /*Demande de la permission (ou des permissions) à l'utilisateur.*/
    private fun askForPermission() {
        ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_LOCATION
        )
    }


    private fun locationServiceEnabled(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // This is new method provided in API 28
            val lm = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            lm.isLocationEnabled
        } else {
            // This is Deprecated in API 28
            val mode = Settings.Secure.getInt(
                    this.contentResolver,
                    Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF
            )
            mode != Settings.Secure.LOCATION_MODE_OFF
        }
    }

    /**
     * Récupération de l'adapter Bluetooth & vérification si celui-ci est actif
     */
    private fun setupBLE() {
        (getSystemService(BLUETOOTH_SERVICE) as BluetoothManager?)?.let { bluetoothManager ->
            bluetoothAdapter = bluetoothManager.adapter
            if (bluetoothAdapter != null && !bluetoothManager.adapter.isEnabled) {
                startActivityForResult(
                        Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                        REQUEST_ENABLE_BLE
                )
            } else {
                scanLeDevice()
            }
        }
    }

    // Le scan va durer 10 secondes seulement, sauf si vous passez une autre valeur comme paramètre.
    private fun scanLeDevice(scanPeriod: Long = 30000) {
        if (!mScanning) {
            bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner

            // On vide la liste qui contient les devices actuellement trouvés
            bleDevicesFoundList.clear()

            mScanning = true

            // On lance une tache qui durera « scanPeriod » à savoir donc de base
            // 10 secondes
            handler.postDelayed({
                mScanning = false
                bluetoothLeScanner?.stopScan(leScanCallback)
                Toast.makeText(this, getString(R.string.scan_over), Toast.LENGTH_SHORT).show()
            }, scanPeriod)

            // On lance le scan
            bluetoothLeScanner?.startScan(scanFilters, scanSettings, leScanCallback)
        }
    }

    // Callback appelé à chaque périphérique trouvé.
    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)

            // C'est ici que nous allons créer notre « device » et l'ajouter dans le RecyclerView (Datasource)

            val device = Device(result.device.name, result.device.address, result.device)
            if (!bleDevicesFoundList.contains(device)) {
                bleDevicesFoundList.add(device)
            }
        }
    }

    private fun connectToCurrentDevice() {
        BluetoothLEManager.currentDevice?.let { device -> Toast.makeText(this, "Almost connected to $device", Toast.LENGTH_SHORT).show()
            currentBluetoothGatt = device.connectGatt(
                    this,
                    false,
                    BluetoothLEManager.GattCallback(
                            onConnect = {
                                // On indique à l'utilisateur que nous sommes correctement connecté
                                runOnUiThread {
                                    // Nous sommes connecté au device, on active les notifications pour être notifié si la LED change d'état.

                                    // À IMPLÉMENTER
                                    // Vous devez appeler la méthode qui active les notifications BLE
                                    enableListenBleNotify()

                                    // On change la vue « pour être en mode connecté »
                                    setUiMode(true)

                                    // On sauvegarde dans les « LocalPréférence » de l'application le nom du dernier préphérique
                                    // sur lequel nous nous sommes connecté
                                    LocalPreferences.getInstance(this).lastConnectedDeviceName(device.name)

                                    // À IMPLÉMENTER EN FONCTION DE CE QUE NOUS AVONS DIT ENSEMBLE
                                }
                            },
                            onNotify = {
                                runOnUiThread {
                                    // VOUS DEVEZ APPELER ICI LA MÉTHODE QUI VA GÉRER LE CHANGEMENT D'ÉTAT DE LA LED DANS L'INTERFACE
                                    handleToggleLedNotificationUpdate(it)
                                }
                            },
                            onDisconnect = { runOnUiThread { disconnectFromCurrentDevice() } })
            )
        }
    }

    private fun enableListenBleNotify() {
        getMainDeviceService()?.let { service ->
            Toast.makeText(this, getString(R.string.bluetooth_notifications), Toast.LENGTH_SHORT).show()
            // Indique que le GATT Client va écouter les notifications sur le charactérisque
            val notification = service.getCharacteristic(BluetoothLEManager.CHARACTERISTIC_NOTIFY_STATE)
            currentBluetoothGatt?.setCharacteristicNotification(notification, true)
        }
    }


    private fun handleToggleLedNotificationUpdate(characteristic: BluetoothGattCharacteristic) {
        if (characteristic.getStringValue(0).equals("on", ignoreCase = true)) {
            ledStatus.setImageResource(R.drawable.light_logo_foreground)
        } else {
            ledStatus.setImageResource(R.drawable.dark_logo_foreground)
        }
    }

    /**
     * On demande la déconnexion du device
     */
    private fun disconnectFromCurrentDevice() {
        currentBluetoothGatt?.disconnect()
        BluetoothLEManager.currentDevice = null
        setUiMode(false)
    }

    private fun setUiMode(isConnected: Boolean) {
        if (isConnected) {
            // Connecté à un périphérique
            //On vide la liste une fois connecté
            bleDevicesFoundList.clear()
            Recycler_Bluetooth.visibility = View.GONE
            startScan.visibility = View.GONE
            ledStatus.visibility = View.VISIBLE
            currentConnexion.visibility = View.VISIBLE
            currentConnexion.text = getString(R.string.connected_to, BluetoothLEManager.currentDevice?.name)
            disconnect.visibility = View.VISIBLE
            toggleLed.visibility = View.VISIBLE
        } else {
            // Non connecté, reset de la vue.
            Recycler_Bluetooth.visibility = View.VISIBLE
            startScan.visibility = View.VISIBLE
            ledStatus.visibility = View.GONE
            currentConnexion.visibility = View.GONE
            disconnect.visibility = View.GONE
            toggleLed.visibility = View.GONE
        }
    }

    /**
     * Récupération de « service » BLE (via UUID) qui nous permettra d'envoyer / recevoir des commandes
     */
    private fun getMainDeviceService(): BluetoothGattService? {
        return currentBluetoothGatt?.let { bleGatt ->
            val service = bleGatt.getService(BluetoothLEManager.DEVICE_UUID)
            service?.let {
                return it
            } ?: run {
                Toast.makeText(this, getString(R.string.uuid_not_found), Toast.LENGTH_SHORT).show()
                return null
            }
        } ?: run {
            Toast.makeText(this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show()
            return null
        }
    }
    /**
     * On change l'état de la LED (via l'UUID de toggle)
     */
    private fun toggleLed() {
        getMainDeviceService()?.let { service ->
            val toggleLed = service.getCharacteristic(BluetoothLEManager.CHARACTERISTIC_TOGGLE_LED_UUID)
            toggleLed.setValue("1")
            currentBluetoothGatt?.writeCharacteristic(toggleLed)
        }
    }
}

