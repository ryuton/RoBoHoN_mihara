 package jp.co.sharp.sample.simple;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import jp.co.sharp.android.voiceui.VoiceUIManager;
import jp.co.sharp.android.voiceui.VoiceUIVariable;
import jp.co.sharp.sample.simple.api.MDnsServerDiscoveryListener;
import jp.co.sharp.sample.simple.bluetooth.BluetoothService;
import jp.co.sharp.sample.simple.customize.ScenarioDefinitions;
import jp.co.sharp.sample.simple.hvmlParser.HVMLParser;
import jp.co.sharp.sample.simple.hvmlParser.HVMLPlacement;
import jp.co.sharp.sample.simple.util.VoiceUIManagerUtil;
import jp.co.sharp.sample.simple.util.VoiceUIVariableUtil;
import jp.co.sharp.sample.simple.util.VoiceUIVariableUtil.VoiceUIVariableListHelper;
import jp.co.sharp.sample.simple.bluetooth.Constants;
import jp.co.sharp.sample.simple.api.APIConstants;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.widget.Toast;

import jp.co.sharp.sample.simple.hvmlParser.HvmlModel;
import jp.co.sharp.sample.simple.hvmlParser.Topic;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;


public class MainActivity extends Activity implements MainActivityVoiceUIListener.MainActivityScenarioCallback,
        MDnsServerDiscoveryListener.MDnsServerDiscoveryCallback,
        HVMLPlacement.HVMLPlacementListener {
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
//    /**
//     * UIスレッド処理用.
//     */
//    private Handler mHandler = new Handler();
    /**
     * mDNSのリスナー
     */
    private MDnsServerDiscoveryListener mDnsServerDiscoveryListener = null;

    /**
     * bluetooth var
     */
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothService mBluetoothService = null;
//    private BluetoothDevice mBluetoothDevice = null;

    private final static Integer REQUEST_ENABLE_BT = 1;
    private final static String RASP3_MAC_ADDRESS = "B8:27:EB:D9:8F:13";
    private final static String RASP0_MAC_ADDRESS = "B8:27:EB:C4:B3:3B";
    private final static String mConnectedDeviceName = "RASPZERO";
    private String mHostname;

    private ProgressDialog progressDialog = null;
    Context activity = null;

    /**
     * HVML Placement
     */
    private LinearLayout topicView;
    HvmlModel hvmlModel = null;

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
                ((TextView)findViewById(R.id.test_value)).setText(String.format("%s, %s", test1, test2));
            }else{
                Log.d(TAG, "VoiceUIVariable is null");
            }
        }

        //ホームボタンの検知登録.
        mHomeEventReceiver = new HomeEventReceiver();
        IntentFilter filterHome = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mHomeEventReceiver, filterHome);

        //VoiceUI再起動の検知登録.
        mVoiceUIStartReceiver = new VoiceUIStartReceiver();
        IntentFilter filter = new IntentFilter(VoiceUIManager.ACTION_VOICEUI_SERVICE_STARTED);
        registerReceiver(mVoiceUIStartReceiver, filter);

//        progressDialog.setTitle("searching raspberry");
//        progressDialog.setMessage("");
//        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//        progressDialog.show();
//        connectPairedDevice(RASP0_MAC_ADDRESS);

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

        topicView = (LinearLayout) findViewById(R.id.TopicLayout);

        //hvml parser
        HVMLParser parser = new HVMLParser(getResources(), "hvml/other/jp_co_sharp_sample_simple_talk.hvml" );
        try {
            hvmlModel = parser.parse();
            TextView topicTextView = (TextView) topicView.findViewById(R.id.TopicName);
            topicTextView.setText(Objects.requireNonNull(hvmlModel.getHead()).getDescription());

        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        assert hvmlModel != null;

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

            //発話の取得アクション
            case ScenarioDefinitions.FUNC_RECOG_TALK:
                for (final VoiceUIVariable variable: variables){
                    Log.e(TAG, variable.toString());
                    if ("Lvcsr_Basic".equals(variable.getName())) {

                        AsyncTestTask testTask = new AsyncTestTask(variable);
                        testTask.execute();
                    }
                }
                Log.i(TAG, "recog");
                break;

            //ロボホンの発話ごとに呼ばれる
            case ScenarioDefinitions.FUNC_HVML_ACTION:
                for (final VoiceUIVariable variable: variables){
                    Log.e(TAG, variable.toString());
                    if ("topic_id".equals(variable.getName())) {
                        String topicId = variable.getStringValue();
                        Topic topic = hvmlModel.topicFromId(topicId);
                        this.changeTargetView(topic);
                    }
                }
                Log.d(TAG, "action");

            default:
                break;
        }
    }

    @Override
    public void call() {
        String number = "7777777777";
        Uri call = Uri.parse("tel:" + number);
        Intent surf = new Intent(Intent.ACTION_CALL, call);
        startActivity(surf);

    }

    private class AsyncTestTask extends AsyncTask<Void, Integer, String> {
        VoiceUIVariable variable;

        public AsyncTestTask(VoiceUIVariable variable) {
            this.variable = variable;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {

                JSONObject responseJson = new JSONObject(s);
                String yoyakutm = responseJson.getJSONArray("results")
                        .getJSONObject(0)
                        .getString("YOYAKUTM");
                String kanjyakananm = responseJson.getJSONArray("results")
                        .getJSONObject(0)
                        .getString("KANJYAKANANM");
                int ret = VoiceUIVariableUtil.setVariableData(mVoiceUIManager, ScenarioDefinitions.MEM_P_APPOINT,  yoyakutm);
                if(ret == VoiceUIManager.VOICEUI_ERROR){
                    Log.e(TAG, "setVariableData:VARIABLE_REGISTER_FAILED");
                }
                ret = VoiceUIVariableUtil.setVariableData(mVoiceUIManager, ScenarioDefinitions.MEM_P_KANJYAKANANM, kanjyakananm);
                if(ret == VoiceUIManager.VOICEUI_ERROR){
                    Log.e(TAG, "setVariableData:VARIABLE_REGISTER_FAILED");
                }

                Log.e(TAG,
                        responseJson.getJSONArray("results")
                                .getJSONObject(0)
                                .getString("KANJYAKANANM")
                );
            } catch (JSONException e) {
                e.printStackTrace();
            }

            VoiceUIVariableListHelper helper = new VoiceUIVariableListHelper().addAccost(ScenarioDefinitions.ACC_APPOINT);
            VoiceUIManagerUtil.updateAppInfo(mVoiceUIManager, helper.getVariableList(), true);
        }

        @Override
        protected String doInBackground(Void... voids) {

            Map<String,String> headers= new HashMap<>();
            try {
                return get(APIConstants.BASE_URL + APIConstants.APPOINT + variable.getStringValue(), headers);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        private String get(String endpoint, Map<String, String> headers) throws IOException {

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
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                }

            } catch (IOException e) {
                Log.d(TAG, "get: " + httpURLConnection.getResponseCode());
                throw e;

            } finally {
                // fortify safeかつJava1.6 compliantなclose処理
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException ignored) {
                    }
                }
                if (isr != null) {
                    try {
                        isr.close();
                    } catch (IOException ignored) {
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException ignored) {
                    }
                }
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            }
            return stringBuilder.toString();
        }

    }

    @Override
    public void onExecCommand(String command, VoiceUIVariable variable) {
        Log.v(TAG, "onExecCommand() : " + command);
        switch (command) {
            case ScenarioDefinitions.FUNC_RECOG_TALK:
            case ScenarioDefinitions.FUNC_HVML_ACTION:
            default:
                break;
        }

    }

    //PlacementListener
    @NotNull
    @Override
    public View newTopicLayout(@NotNull Topic topic) {
        View topicLayout = getLayoutInflater().inflate(R.layout.layout_topic, null);
        topicLayout.setId(View.generateViewId());
        topicLayout.setLayoutParams(new FrameLayout.LayoutParams(500, 500));

        TextView topicTextView = (TextView) topicLayout.findViewById(R.id.TopicName);
        if (!topic.getActions().isEmpty()) {
            topicTextView.setText(topic.getActions().get(0).getSpeech());
        }

//        TextView anchorTextView = (TextView) topicLayout.findViewById(R.id.AnchorName);
//        StringBuilder anchorText = new StringBuilder();
//        for (Topic.Anchor anchor: topic.getAnchors()) {
//            anchorText.append(anchor.getHref());
//        }
//        anchorTextView.setText(anchorText);

//        TextView nextTextView = (TextView) topicLayout.findViewById(R.id.NextName);
//        StringBuilder nextText = new StringBuilder();
//        for (Topic.Next next: topic.getNexts()) {
//            nextText.append(next.getHref());
//        }
//        nextTextView.setText(nextText);
//
        return topicLayout;
    }

    @NotNull
    @Override
    public View newArrowView() {
        View arrowView = getLayoutInflater().inflate(R.layout.layout_arrow, null);
        arrowView.setLayoutParams(new FrameLayout.LayoutParams(400, 400));
        arrowView.setId(View.generateViewId());

        return arrowView;
    }

    private void changeTargetView(final Topic topic) {

        final Handler mainHandler = new Handler(Looper.getMainLooper());
        /* 処理 */
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                TextView topicNameTextView = (TextView) topicView.findViewById(R.id.TopicName);
                topicNameTextView.setText(topic.getActions().get(0).getSpeech());
            }
        });

    }

    @NotNull
    @Override
    public ConstraintLayout getRootLayout() {
        return (ConstraintLayout) findViewById(R.id.root);
    }


    // Bluetooth
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
                        //接続された
                        case BluetoothService.STATE_CONNECTED:
                            //setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            //mConversationArrayAdapter.clear();
                            Log.d(TAG, msg.toString());
                            progressDialog.cancel();

                            break;
                        //接続中
                        case BluetoothService.STATE_CONNECTING:
                            break;
                        //検索中
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
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

    /**
     * Create a BroadcastReceiver for ACTION_FOUND.
     */
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
