package com.example.robohonreception.mDNS;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import java.net.InetAddress;

public class mDNSHandler {
    private NsdManager nsdManager;
    private NsdManager.DiscoveryListener discoveryListener;
    private NsdManager.ResolveListener resolveListener;
    private NsdServiceInfo mService;
    private String TAG = "mDNSHandler";
    private String reIPAdress = "/[0-9]+.[0-9]+.[0-9]+.[0-9]+";

    private String serviceName = "rasp0";
    private String hostIP = "";

    public String getHostIP() {
        if (hostIP.isEmpty()) return "";
        else                  return "http:/" + hostIP + ":8080/";
    }

    // raspberry service type
    private static final String SERVICE_TYPE = "_workstation._tcp.";

    public mDNSHandler(Context context) {
        nsdManager = (NsdManager)(context.getSystemService(Context.NSD_SERVICE));
        initializeResolveListener();
        initializeDiscoveryListener();
        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
    }

    public void initializeDiscoveryListener() {

        discoveryListener = new NsdManager.DiscoveryListener() {

            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                //見つかるのが<hostname>.local[<macアドレス>]みたいな感じだったのでcontainsで対応
                if (service.getServiceName().contains(serviceName)) {
                    nsdManager.resolveService(service, resolveListener);
                    Log.d(TAG, "Same machine: " + serviceName);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                Log.e(TAG, "service lost: " + service);
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                nsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                nsdManager.stopServiceDiscovery(this);
            }
        };
    }


    public void initializeResolveListener() {
        resolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Called when the resolve fails. Use the error code to debug.
                Log.e(TAG, "Resolve failed: " + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.d(TAG, "Resolve Succeeded. " + serviceInfo);

                if (serviceInfo.getServiceName().equals(serviceName)) {
                    Log.d(TAG, "Same IP.");
                    return;
                }
                mService = serviceInfo;
                int port = mService.getPort();
                InetAddress host = mService.getHost();
                if (host.toString().matches(reIPAdress)) hostIP = host.toString();
            }
        };
    }
}
