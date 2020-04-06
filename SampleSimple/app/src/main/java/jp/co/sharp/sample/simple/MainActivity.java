package jp.co.sharp.sample.simple;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jp.co.sharp.android.voiceui.VoiceUIManager;
import jp.co.sharp.android.voiceui.VoiceUIVariable;
import jp.co.sharp.sample.simple.api.MDnsServerDiscoveryListener;
import jp.co.sharp.sample.simple.bluetooth.BluetoothService;
import jp.co.sharp.sample.simple.customize.ScenarioDefinitions;
import jp.co.sharp.sample.simple.util.VoiceUIManagerUtil;
import jp.co.sharp.sample.simple.util.VoiceUIVariableUtil;
import jp.co.sharp.sample.simple.util.VoiceUIVariableUtil.VoiceUIVariableListHelper;
import jp.co.sharp.sample.simple.bluetooth.Constants;
import jp.co.sharp.sample.simple.api.APIConstants;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends Activity implements MainActivityVoiceUIListener.MainActivityScenarioCallback, MDnsServerDiscoveryListener.MDnsServerDiscoveryCallback {
    public static final String TAG = MainActivity.class.getSimpleName();

    /**
     * 音声UI制御.
     */
    private VoiceUIManager mVoiceUIManager = null;
    /**
     * 音声UIイベントリスナー.
     */
    private MainActivityVoiceUIListener mMainActivityVoiceUIListener = null;
    /**
     * 音声UIの再起動イベント検知.
     */
    private VoiceUIStartReceiver mVoiceUIStartReceiver = null;
    /**
     * ホームボタンイベント検知.
     */
    private HomeEventReceiver mHomeEventReceiver;
    /**
     * UIスレッド処理用.
     */
    private Handler mHandler = new Handler();

    /**
     * mDNSのリスナー
     */
    private MDnsServerDiscoveryListener mDnsServerDiscoveryListener = null;

    /**
     * bluetooth var
     */
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothService mBluetoothService = null;

    private BluetoothDevice mBluetoothDevice = null;

    private final static Integer REQUEST_ENABLE_BT = 1;
    private final static String RASP3_MAC_ADDRESS = "B8:27:EB:D9:8F:13";
    private final static String RASP0_MAC_ADDRESS = "B8:27:EB:C4:B3:3B";
    private final static String mConnectedDeviceName = "RASPZERO";
    private String mHostname;

    private ProgressDialog progressDialog = null;
    Context activity = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        progressDialog = new ProgressDialog(this);
        activity = this;
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate()");
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        if (intent != null) {
            //ホームシナリオから"mode"の値を受け取る.
            String modeVal = intent.getStringExtra("mode");
            if (modeVal != null) {
                ((TextView)findViewById(R.id.mode_value)).setText(modeVal);
            }
            //ホームシナリオから任意の値を受け取る.
            List<VoiceUIVariable> variables = intent.getParcelableArrayListExtra("VoiceUIVariable");
            if (variables != null) {
                String test1 = VoiceUIVariableUtil.getVariableData(variables, ScenarioDefinitions.KEY_TEST_1);
                String test2 = VoiceUIVariableUtil.getVariableData(variables, ScenarioDefinitions.KEY_TEST_2);
                ((TextView)findViewById(R.id.test_value)).setText(test1 + ", " + test2);
            }else{
                Log.d(TAG, "VoiceUIVariable is null");
            }
        }

        // accostボタン
        Button voiceAccostButton = (Button)findViewById(R.id.voice_accost_button);
        voiceAccostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVoiceUIManager != null) {
                    VoiceUIVariableListHelper helper = new VoiceUIVariableListHelper().addAccost(ScenarioDefinitions.ACC_ACCOST);
                    VoiceUIManagerUtil.updateAppInfo(mVoiceUIManager, helper.getVariableList(), true);
                }
            }
        });

        // resolve variableボタン
        Button resolveButton = (Button)findViewById(R.id.resolve_variable_button);
        resolveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVoiceUIManager != null) {
                    VoiceUIVariableListHelper helper = new VoiceUIVariableListHelper().addAccost(ScenarioDefinitions.ACC_RESOLVE);
                    VoiceUIManagerUtil.updateAppInfo(mVoiceUIManager, helper.getVariableList(), true);
                }
            }
        });

        // set memory_pボタン
        Button getMemoryPButton = (Button)findViewById(R.id.set_memoryP);
        getMemoryPButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();

                final String hour = String.valueOf(now.get(Calendar.HOUR_OF_DAY));
                final String minute = String.valueOf(now.get(Calendar.MINUTE));
                int ret = VoiceUIVariableUtil.setVariableData(mVoiceUIManager, ScenarioDefinitions.MEM_P_HOUR, hour);
                if(ret == VoiceUIManager.VOICEUI_ERROR){
                    Log.d(TAG, "setVariableData:VARIABLE_REGISTER_FAILED");
                }
                ret = VoiceUIVariableUtil.setVariableData(mVoiceUIManager, ScenarioDefinitions.MEM_P_MINUTE, minute);
                if(ret == VoiceUIManager.VOICEUI_ERROR){
                    Log.d(TAG, "setVariableData:VARIABLE_REGISTER_FAILED");
                }
                String text = "Set " + hour + ":" + minute;
                TextView textSetting = (TextView)findViewById(R.id.ViewTime);
                textSetting.setText(text);
            }
        });

        // get memory_pボタン
        Button setMemoryPButton = (Button)findViewById(R.id.get_memoryP);
        setMemoryPButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVoiceUIManager != null) {
                    VoiceUIVariableListHelper helper = new VoiceUIVariableListHelper().addAccost(ScenarioDefinitions.ACC_GET_MEMORYP);
                    VoiceUIManagerUtil.updateAppInfo(mVoiceUIManager, helper.getVariableList(), true);
                }
            }
        });

        // finish app：アプリ終了ボタン
        Button finishAppButton = (Button)findViewById(R.id.finish_app_button);
        finishAppButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVoiceUIManager != null) {
                    VoiceUIVariableListHelper helper = new VoiceUIVariableListHelper().addAccost(ScenarioDefinitions.ACC_END_APP);
                    VoiceUIManagerUtil.updateAppInfo(mVoiceUIManager, helper.getVariableList(), true);
                }
            }
        });

        //ホームボタンの検知登録.
        mHomeEventReceiver = new HomeEventReceiver();
        IntentFilter filterHome = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mHomeEventReceiver, filterHome);

        //VoiceUI再起動の検知登録.
        mVoiceUIStartReceiver = new VoiceUIStartReceiver();
        IntentFilter filter = new IntentFilter(VoiceUIManager.ACTION_VOICEUI_SERVICE_STARTED);
        registerReceiver(mVoiceUIStartReceiver, filter);

        /*
        // Register for broadcasts when a device is discovered.
        IntentFilter btFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, btFilter);

        setmBluetoothService();
        */


        progressDialog.setTitle("searching raspberry");
        progressDialog.setMessage("");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
        connectPairedDevice(RASP0_MAC_ADDRESS);


    }
    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG, "onResume()");

        //VoiceUIManagerのインスタンス取得.
        if (mVoiceUIManager == null) {
            mVoiceUIManager = VoiceUIManager.getService(getApplicationContext());
        }
        //MainActivityVoiceUIListener生成.
        if (mMainActivityVoiceUIListener == null) {
            mMainActivityVoiceUIListener = new MainActivityVoiceUIListener(this);
        }
        if (mDnsServerDiscoveryListener == null) {
            mDnsServerDiscoveryListener = new MDnsServerDiscoveryListener(this, "rasp0");
        }
        //VoiceUIListenerの登録.
        VoiceUIManagerUtil.registerVoiceUIListener(mVoiceUIManager, mMainActivityVoiceUIListener);

        //Scene有効化.
        VoiceUIManagerUtil.enableScene(mVoiceUIManager, ScenarioDefinitions.SCENE_COMMON);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v(TAG, "onPause()");

        //バックに回ったら発話を中止する.
        VoiceUIManagerUtil.stopSpeech();

        //VoiceUIListenerの解除.
        VoiceUIManagerUtil.unregisterVoiceUIListener(mVoiceUIManager, mMainActivityVoiceUIListener);

        //Scene無効化.
        VoiceUIManagerUtil.disableScene(mVoiceUIManager, ScenarioDefinitions.SCENE_COMMON);

        //単一Activityの場合はonPauseでアプリを終了する.
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy()");

        //ホームボタンの検知破棄.
        this.unregisterReceiver(mHomeEventReceiver);

        //VoiceUI再起動の検知破棄.
        this.unregisterReceiver(mVoiceUIStartReceiver);

        //インスタンスのごみ掃除.
        mVoiceUIManager = null;
        mMainActivityVoiceUIListener = null;
    }

    /**
     * VoiceUIListenerクラスからのコールバックを実装する.
     */
    @Override
    public void onExecCommand(String command, List<VoiceUIVariable> variables) {
        Log.v(TAG, "onExecCommand() : " + command);
        switch (command) {
            case ScenarioDefinitions.FUNC_END_APP:
                finish();
                break;
            case ScenarioDefinitions.FUNC_RECOG_TALK:
//                final String lvcsr = VoiceUIVariableUtil.getVariableData(variables, ScenarioDefinitions.KEY_LVCSR_BASIC);
//                mHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        if(!isFinishing()) {
//                            ((TextView) findViewById(R.id.recog_text)).setText("Lvcsr:"+lvcsr);
//                        }
//                    }
//                });
                Log.i(TAG, "recog");
                break;
            default:
                break;
        }
    }
    @Override
    public void onExecCommand(String command, VoiceUIVariable variable) {
        Log.v(TAG, "onExecCommand() : " + command);
        switch (command) {
            case ScenarioDefinitions.FUNC_RECOG_TALK:
                Map<String,String> headers=new HashMap<String,String>();
                try {
                    String response = get(APIConstants.BASE_URL + APIConstants.APPOINT + variable.getStringValue(), headers);
                    JSONObject responseJson = new JSONObject(response);
                    String yoyakutm = responseJson.getJSONArray("results")
                            .getJSONObject(0)
                            .getString("YOYAKUTM");
                    String kanjyakananm = responseJson.getJSONArray("results")
                            .getJSONObject(0)
                            .getString("KANJYAKANANM");
                    int ret = VoiceUIManagerUtil.setMemory(mVoiceUIManager, ScenarioDefinitions.MEM_P_APPOINT,  yoyakutm);
                    ret = VoiceUIManagerUtil.setMemory(mVoiceUIManager, ScenarioDefinitions.MEM_P_KANJYAKANANM, kanjyakananm);
                    Log.e(TAG,
                            responseJson.getJSONArray("results")
                                    .getJSONObject(0)
                                    .getString("KANJYAKANANM")
                    );
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
        }

    }

    private static String get(String endpoint, Map<String, String> headers) throws IOException {

        final int TIMEOUT_MILLIS = 0;// タイムアウトミリ秒：0は無限

        final StringBuilder stringBuilder = new StringBuilder();

        HttpURLConnection httpURLConnection = null;
        BufferedReader bufferedReader = null;
        InputStream inputStream = null;
        InputStreamReader isr = null;

        try {
            URL url = new URL(endpoint);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setConnectTimeout(TIMEOUT_MILLIS);// 接続にかかる時間
            httpURLConnection.setReadTimeout(TIMEOUT_MILLIS);// データの読み込みにかかる時間
            httpURLConnection.setRequestMethod("GET");// HTTPメソッド
            httpURLConnection.setUseCaches(false);// キャッシュ利用
            httpURLConnection.setDoOutput(false);// リクエストのボディの送信を許可(GETのときはfalse,POSTのときはtrueにする)
            httpURLConnection.setDoInput(true);// レスポンスのボディの受信を許可

            // HTTPヘッダをセット
            if (headers != null) {
                for (String key : headers.keySet()) {
                    httpURLConnection.setRequestProperty(key, headers.get(key));// HTTPヘッダをセット
                }
            }
            httpURLConnection.connect();

            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                inputStream = httpURLConnection.getInputStream();
                isr = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                bufferedReader = new BufferedReader(isr);
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }
            } else {
                // If responseCode inputStream not HTTP_OK
            }

        } catch (IOException e) {
            throw e;
        } finally {
            // fortify safeかつJava1.6 compliantなclose処理
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                }
            }
            if (isr != null) {
                try {
                    isr.close();
                } catch (IOException e) {
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
        return stringBuilder.toString();
    }

    private void setBluetoothService() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null){
            Log.e( TAG,"Device doesn't support Bluetooth");
            return;
        }

        if (!mBluetoothAdapter.isEnabled()){
            Intent enabledBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enabledBtIntent, REQUEST_ENABLE_BT);
        }

        mBluetoothService = new BluetoothService(mBluetoothHandler);

        mBluetoothService.startDiscovery();
    }

    private void connectPairedDevice(String deviceAddress) {

        setBluetoothService();

        mBluetoothService.cancelDiscovery();

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        for (BluetoothDevice device : pairedDevices) {
            String deviceHardwareAddress = device.getAddress();
            if ( deviceHardwareAddress.equals(deviceAddress)){
                mBluetoothService.connect(device, true);
            }

        }

    }


    @SuppressLint("HandlerLeak")
    private final Handler mBluetoothHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            //setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            //mConversationArrayAdapter.clear();
                            Log.d(TAG, msg.toString());
                            progressDialog.cancel();

                            break;
                        case BluetoothService.STATE_CONNECTING:
                            //setStatus(R.string.title_connecting);
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            //setStatus(R.string.title_not_connected);
                            break;
                        default:
                            throw new IllegalStateException("Unexpected value: " + msg.arg1);
                    }
                case Constants.MESSAGE_WRITE:

                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name

                    Toast.makeText(activity, "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    if (mVoiceUIManager != null) {
                        VoiceUIVariableListHelper helper = new VoiceUIVariableListHelper().addAccost(ScenarioDefinitions.ACC_ACCOST);
                        VoiceUIManagerUtil.updateAppInfo(mVoiceUIManager, helper.getVariableList(), true);
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    if (mVoiceUIManager != null) {
                        VoiceUIVariableListHelper helper = new VoiceUIVariableListHelper().addAccost(ScenarioDefinitions.ACC_ACCOST);
                        VoiceUIManagerUtil.updateAppInfo(mVoiceUIManager, helper.getVariableList(), true);
                    }
                    break;
                case Constants.MESSAGE_READ:
                    if (mVoiceUIManager != null) {
                        VoiceUIVariableListHelper helper = new VoiceUIVariableListHelper().addAccost(ScenarioDefinitions.ACC_Talk);
                        VoiceUIManagerUtil.updateAppInfo(mVoiceUIManager, helper.getVariableList(), true);
                    }
                    break;
                default:

            }

            Log.d(TAG, msg.toString());
        }
    };

    @Override
    public void call() {
        String number = "7777777777";
        Uri call = Uri.parse("tel:" + number);
        Intent surf = new Intent(Intent.ACTION_CALL, call);
        startActivity(surf);

    }

    @Override
    public void getHostName(@NotNull InetAddress hostName) {
        mHostname = hostName.toString();
    }

    /**
     * ホームボタンの押下イベントを受け取るためのBroadcastレシーバークラス.<br>
     * <p/>
     * アプリは必ずホームボタンで終了する..
     */
    private class HomeEventReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG, "Receive Home button pressed");
            // ホームボタン押下でアプリ終了する.
            finish();
        }
    }

    /**
     * 音声UI再起動イベントを受け取るためのBroadcastレシーバークラス.<br>
     * <p/>
     * 稀に音声UIのServiceが再起動することがあり、その場合アプリはVoiceUIの再取得とListenerの再登録をする.
     */
    private class VoiceUIStartReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (VoiceUIManager.ACTION_VOICEUI_SERVICE_STARTED.equals(action)) {
                Log.d(TAG, "VoiceUIStartReceiver#onReceive():VOICEUI_SERVICE_STARTED");
                //VoiceUIManagerのインスタンス取得.
                mVoiceUIManager = VoiceUIManager.getService(getApplicationContext());
                if (mMainActivityVoiceUIListener == null) {
                    mMainActivityVoiceUIListener = new MainActivityVoiceUIListener(getApplicationContext());
                }
                //VoiceUIListenerの登録.
                VoiceUIManagerUtil.registerVoiceUIListener(mVoiceUIManager, mMainActivityVoiceUIListener);
            }
        }
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d("main", "ended");
            }
        }
    };



}
