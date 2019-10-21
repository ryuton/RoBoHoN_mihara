package com.example.testbluetooth

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.util.Log
import android.widget.Button
import android.widget.Toast


const val REQUEST_ENABLE_BT = 1

class MainActivity : AppCompatActivity() {

    val TAG = "MainActivity"

    private val RASP3_MAC_ADDRESS = "B8:27:EB:D9:8F:13"

    var bluetoothAdapter: BluetoothAdapter? = null
    var mBluetoothService: BluetoothService? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        connect()

        val btn_click_me = findViewById(R.id.button) as Button
        // set on-click listener
        btn_click_me.setOnClickListener {
            sendMessage("cccccc")
        }
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private fun sendMessage(message: String) {
        // Check that we're actually connected before trying anything


        // Check that there's actually something to send
        if (message.length > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            val send = message.toByteArray()
            mBluetoothService?.write(send)

            // Reset out string buffer to zero and clear the edit text field
            //mOutStringBuffer.setLength(0)
            //mOutEditText.setText(mOutStringBuffer)
        }
    }

    private fun connect(){
        //bluetoothAdapterの取得
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Log.e( TAG,"Device doesn't support Bluetooth")
        }

        //Bluetoothの許可取り
        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

        mBluetoothService = BluetoothService(mHandler)

        //pairedDevices
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        pairedDevices?.forEach { device ->
            val deviceName = device.name
            val deviceHardwareAddress = device.address // MAC address

            if(deviceHardwareAddress == RASP3_MAC_ADDRESS) {
                mBluetoothService?.connect(device, true)
            }
        }
    }


    private val mHandler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            Log.d(TAG, msg.toString())
        }
    }

}
