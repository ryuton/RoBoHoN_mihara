package jp.co.sharp.sample.simple.api

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdManager.DiscoveryListener
import android.net.nsd.NsdServiceInfo
import android.util.Log
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import java.net.NetworkInterface

class MDnsServerDiscoveryListener(context: Context, serviceName: String) {

    val TAG = "MDnsServerDiscovery"
    val SERVICE_TYPE = "_workstation._tcp."
    var mServiceName: String? = null

    private var mCallback: MDnsServerDiscoveryCallback? = null
    private var nsdManager: NsdManager? = null
    private var mService: NsdServiceInfo? = null

    private val discoveryListener = object : DiscoveryListener {

        override fun onDiscoveryStarted(regType: String) {
            Log.d(TAG, "Service discovery started")
        }

        override fun onServiceFound(service: NsdServiceInfo) {
            // A service was found! Do something with it.
            Log.d(TAG, "Service discovery success$service")
            when {
                service.serviceType != SERVICE_TYPE ->
                    Log.d(TAG, "Unknown Service Type: ${service.serviceType}")

                service.serviceName.contains(mServiceName!!.toRegex()) -> {
                    Log.d(TAG, "Same machine: $mServiceName")
                    nsdManager?.resolveService(service, resolveListener)
                }

            }
        }

        override fun onServiceLost(service: NsdServiceInfo) {
            // When the network service is no longer available.
            // Internal bookkeeping code goes here.
            Log.e(TAG, "service lost: $service")
        }

        override fun onDiscoveryStopped(serviceType: String) {
            Log.i(TAG, "Discovery stopped: $serviceType")
        }

        override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(TAG, "Discovery failed: Error code:$errorCode")
            nsdManager?.stopServiceDiscovery(this)
        }

        override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(TAG, "Discovery failed: Error code:$errorCode")
            nsdManager?.stopServiceDiscovery(this)
        }
    }

    private val resolveListener = object : NsdManager.ResolveListener {

        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            // Called when the resolve fails. Use the error code to debug.
            Log.e(TAG, "Resolve failed: $errorCode")
        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
            Log.e(TAG, "Resolve Succeeded. $serviceInfo")

            if (serviceInfo.serviceName == mServiceName) {
                Log.d(TAG, "Same IP.")
                return
            }
            mService = serviceInfo
            val host: InetAddress = serviceInfo.host
            mCallback?.getHostName(host)

            val hostInet = InetAddress.getByName(serviceInfo.host.hostAddress)
            val addressBytes = hostInet.address
        }
    }

    init{
        try {
            mCallback = context as MDnsServerDiscoveryCallback
            mServiceName = serviceName

            nsdManager = (context.getSystemService(Context.NSD_SERVICE) as NsdManager)
            nsdManager?.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)

        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement $TAG")
        }
    }

    interface MDnsServerDiscoveryCallback {
        /**
         * 実行されたcontrolの通知.
         *
         * @param function 実行された操作コマンド種別.
         */
        fun getHostName(hostName: InetAddress)
    }
}