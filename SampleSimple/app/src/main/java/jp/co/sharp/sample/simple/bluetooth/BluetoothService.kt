package jp.co.sharp.sample.simple.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.bluetooth.BluetoothAdapter

import android.os.Bundle
import android.os.Handler
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

private const val TAG = "BLUETOOTH_SERVICE"



class BluetoothService(
    // handler that gets info from Bluetooth service
    private val mHandler: Handler = Handler()
) {
    companion object {
        // Constants that indicate the current connection state
        const val STATE_NONE = 0       // we're doing nothing
        const val STATE_LISTEN = 1     // now listening for incoming connections
        const val STATE_CONNECTING = 2 // now initiating an outgoing connection
        const val STATE_CONNECTED = 3  // now connected to a remote device
    }

    // Unique UUID for this application
    private val MY_UUID_SECURE =  UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private val MY_UUID_INSECURE = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66")



    // Message types sent from the BluetoothService Handler
    private val MESSAGE_STATE_CHANGE: Int = 1
    private val MESSAGE_READ: Int = 2
    private val MESSAGE_WRITE: Int = 3
    private val MESSAGE_DEVICE_NAME: Int = 4
    private val MESSAGE_TOAST: Int = 5


    private val DEVICE_NAME: String = "device_name"
    private val TOAST: String = "toast"

    var mState = STATE_NONE
    var mNewState = mState
    private var mAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    private var mConnectThread: ConnectThread? = null
    private var mConnectedThread: ConnectedThread? = null

    @Synchronized
    fun start() {
        Log.d(TAG, "start")

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread?.cancel()
            mConnectThread = null
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread?.cancel()
            mConnectedThread = null
        }

    }

    /**
     * Update UI title according to the current state of the chat connection
     */
    @Synchronized
    private fun updateUserInterfaceTitle() {
        mState = getState()
        Log.d(TAG, "updateUserInterfaceTitle() $mNewState -> $mState")
        mNewState = mState

        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, mNewState, -1).sendToTarget()
    }

    fun connect(mmDevice: BluetoothDevice, secure: Boolean){
        Log.d(TAG, "starting connect")
        mConnectThread = ConnectThread(mmDevice, secure)
        mConnectThread?.start()

    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    @Synchronized
    fun connected(socket: BluetoothSocket, device: BluetoothDevice, socketType: String) {
        Log.d(TAG, "connected, Socket Type:$socketType")

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread?.cancel()
            mConnectThread = null
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread?.cancel()
            mConnectedThread = null
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = ConnectedThread(socket, socketType)
        mConnectedThread?.start()

        // Send the name of the connected device back to the UI Activity
        val msg = mHandler.obtainMessage()
        val bundle = Bundle()
        bundle.putString(DEVICE_NAME, device.name)
        Log.d(TAG, device.name + " " + device.address)
        msg.data = bundle
        mHandler.sendMessage(msg)

        updateUserInterfaceTitle()

    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private fun connectionFailed() {
        // Send a failure message back to the Activity
        val msg = mHandler.obtainMessage(MESSAGE_TOAST)
        val bundle = Bundle()
        bundle.putString(TOAST, "Unable to connect device")
        msg.data = bundle
        mHandler.sendMessage(msg)

        mState = STATE_NONE

        // Start the service over to restart listening mode
        this@BluetoothService.start()
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private fun connectionLost() {
        // Send a failure message back to the Activity
        val msg = mHandler.obtainMessage(MESSAGE_TOAST)
        val bundle = Bundle()
        bundle.putString(TOAST, "Device connection was lost")
        msg.data = bundle
        mHandler.sendMessage(msg)

        mState = STATE_NONE

        // Start the service over to restart listening mode
        this@BluetoothService.start()
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread.write
     */
    fun write(out: ByteArray) {
        // Create temporary object
        var r: ConnectedThread? = null
        // Synchronize a copy of the ConnectedThread
        synchronized(this) {
            if (mState != STATE_CONNECTED) return
            r = mConnectedThread
        }
        // Perform the write unsynchronized
        r?.write(out)
    }

    fun getState(): Int {
        return mState
    }

    private inner class ConnectThread(private val mmDevice: BluetoothDevice, secure: Boolean) :
        Thread() {
        private val mmSocket: BluetoothSocket?
        private val mSocketType: String

        init {
            var tmp: BluetoothSocket? = null
            mSocketType = if (secure) "Secure" else "Insecure"

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice

            try {
                if (secure) {
                    tmp = mmDevice.createRfcommSocketToServiceRecord(
                        MY_UUID_SECURE
                    )
                } else {
                    tmp = mmDevice.createInsecureRfcommSocketToServiceRecord(
                        MY_UUID_INSECURE
                    )
                }
            } catch (e: IOException) {
                Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e)
            }

            mmSocket = tmp
            mState = STATE_CONNECTING
        }

        override fun run() {
            Log.i(TAG, "BEGIN mConnectThread SocketType:$mSocketType")
            name = "ConnectThread$mSocketType"

            // Always cancel discovery because it will slow down a connection
            mAdapter?.cancelDiscovery()

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket!!.connect()
            } catch (e: IOException) {
                // Close the socket
                try {
                    mmSocket!!.close()
                } catch (e2: IOException) {
                    Log.e(
                        TAG, "unable to close() " + mSocketType +
                                " socket during connection failure", e2
                    )
                }

                connectionFailed()
                return
            }

            // Reset the ConnectThread because we're done
            synchronized(this@BluetoothService) {
                mConnectThread = null
            }

            // Start the connected thread
            connected(mmSocket, mmDevice, mSocketType)
        }

        fun cancel() {
            try {
                mmSocket!!.close()
            } catch (e: IOException) {
                Log.e(TAG, "close() of connect $mSocketType socket failed", e)
            }

        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private inner class ConnectedThread(private val mmSocket: BluetoothSocket, socketType: String) :
        Thread() {
        private val mmInStream: InputStream?
        private val mmOutStream: OutputStream?

        init {
            Log.d(TAG, "create ConnectedThread: $socketType")
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = mmSocket.inputStream
                tmpOut = mmSocket.outputStream
            } catch (e: IOException) {
                Log.e(TAG, "temp sockets not created", e)
            }

            mmInStream = tmpIn
            mmOutStream = tmpOut
            mState = STATE_CONNECTED
        }

        override fun run() {
            Log.i(TAG, "BEGIN mConnectedThread")
            val buffer = ByteArray(1024)
            var bytes: Int

            // Keep listening to the InputStream while connected
            while (mState == STATE_CONNECTED) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream!!.read(buffer)

                    Log.d("aaaaaaaaaaaaaaa", "MESSAGE_READ")

                    // Send the obtained bytes to the UI Activity
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                        .sendToTarget()
                } catch (e: IOException) {
                    Log.e(TAG, "disconnected", e)
                    connectionLost()
                    break
                }

            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        fun write(buffer: ByteArray) {
            try {
                mmOutStream!!.write(buffer)

                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(MESSAGE_WRITE, -1, -1, buffer)
                    .sendToTarget()
            } catch (e: IOException) {
                Log.e(TAG, "Exception during write", e)
            }

        }

        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
                Log.e(TAG, "close() of connect socket failed", e)
            }

        }
    }
}