package jp.co.sharp.sample.simple.bluetooth

interface Constants {

    // Message types sent from the BluetoothService Handler
    val MESSAGE_STATE_CHANGE: Int
        get() = 1
    val MESSAGE_READ: Int
        get() = 2
    val MESSAGE_WRITE: Int
        get() = 3
    val DEVICE_NAME: String
        get() = "device_name"
    val MESSAGE_DEVICE_NAME: Int
        get() = 4
    val MESSAGE_TOAST: Int
        get() = 5
    // Key names received from the BluetoothService Handler

    val TOAST: String
        get() = "toast"

}