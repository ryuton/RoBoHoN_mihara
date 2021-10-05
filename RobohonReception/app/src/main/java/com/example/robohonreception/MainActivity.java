package com.example.robohonreception;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import jp.co.sharp.android.voiceui.VoiceUIManager;
import jp.co.sharp.android.voiceui.VoiceUIVariable;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import com.example.robohonreception.hvml.HVMLParser;
import com.example.robohonreception.hvml.tag.Topic;
import com.example.robohonreception.mDNS.mDNSHandler;
import com.example.robohonreception.patient.Appoint;
import com.example.robohonreception.voiceui.ScenarioDefinitions;
import com.example.robohonreception.voiceui.VoiceUIListenerImpl;
import com.example.robohonreception.voiceui.VoiceUIManagerUtil;
import com.example.robohonreception.voiceui.VoiceUIVariableUtil;
import com.google.gson.Gson;

import static com.example.robohonreception.voiceui.ScenarioDefinitions.FUNC_CALLL_ACTION;
import static com.example.robohonreception.voiceui.ScenarioDefinitions.FUNC_END_APP;
import static com.example.robohonreception.voiceui.ScenarioDefinitions.FUNC_HVML_ACTION;


public class MainActivity extends Activity implements VoiceUIListenerImpl.ScenarioCallback {
    public static final String TAG = MainActivity.class.getSimpleName();

//    private String BASE_URL = "http://192.168.11.35:8080/";
    private String BASE_URL = "";
    private String PATIENT_URL = "patient/";
    private String APPOINT_URL = "appoint/";
    private String GPIO_URL = "gpio/";

    /**
     * 音声UI制御.
     */
    private VoiceUIManager mVUIManager = null;
    /**
     * 音声UIイベントリスナー.
     */
    private VoiceUIListenerImpl mVUIListener = null;
    /**
     * ホームボタンイベント検知.
     */
    private HomeEventReceiver mHomeEventReceiver;
    /**
     * UIスレッド処理用.
     */
    private Handler mHandler = new Handler();

    private HVMLParser mHVMLParser = null;
    private mDNSHandler mmDNSHandler = null;

    private Handler handler = null;
    private Runnable r =null;

    /**
     * 画面表示切り替え用
     */
    private int isHideHVMLView = View.VISIBLE;
    private int isHideKeyInputView = View.INVISIBLE;
    private View hvmlView = null;
    private View keyInputView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate()");
        setContentView(R.layout.activity_main);

//        Intent intent = getIntent();
//        if (intent != null) {
//            //ホームシナリオから"mode"の値を受け取る.
//            String modeVal = intent.getStringExtra("mode");
//            if (modeVal != null) {
//                ((TextView)findViewById(R.id.mode_value)).setText(modeVal);
//            }
//            //ホームシナリオから任意の値を受け取る.
//            List<VoiceUIVariable> variables = intent.getParcelableArrayListExtra("VoiceUIVariable");
//            if (variables != null) {
//                String test1 = VoiceUIVariableUtil.getVariableData(variables, ScenarioDefinitions.KEY_TEST_1);
//                String test2 = VoiceUIVariableUtil.getVariableData(variables, ScenarioDefinitions.KEY_TEST_2);
//                ((TextView)findViewById(R.id.test_value)).setText(test1 + ", " + test2);
//            }else{
//                Log.d(TAG, "VoiceUIVariable is null");
//            }
//        }


        //HVMLParse
        try {
            AssetManager assetManager = getResources().getAssets();
            InputStream inputStream= assetManager.open("hvml/other/com_example_robohonreception_talk.hvml");
            mHVMLParser = new HVMLParser(inputStream);

            ((TextView) findViewById(R.id.TopicName)).setText(mHVMLParser.getHead().description);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mmDNSHandler = new mDNSHandler(getApplicationContext());


        handler = new Handler();
        r = new Runnable() {
            int count = 0;
            @Override
            public void run() {
                // UIスレッド
                getGPIO();
                handler.postDelayed(this, 1000);
            }
        };

        //Viewのインスタンス取得
        this.hvmlView = findViewById(R.id.contentHVML);
        this.keyInputView = findViewById(R.id.contentKeyinput);
        this.hvmlView.setVisibility(this.isHideHVMLView);
        this.keyInputView.setVisibility(this.isHideKeyInputView);

        findViewById(R.id.appointSendButtun).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText appointNumberInput = (EditText) findViewById(R.id.appointNumberInput);
                try {
                    String appointNum = appointNumberInput.getText().toString();
                    VoiceUIManagerUtil.clearMemory(mVUIManager, ScenarioDefinitions.MEM_P_PATIENT_ID);
                    int ret = VoiceUIManagerUtil.setMemory(mVUIManager, ScenarioDefinitions.MEM_P_PATIENT_ID, appointNum);

                    VoiceUIManagerUtil.startSpeech(mVUIManager, ScenarioDefinitions.ACC_WAIT);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(!isFinishing()) {
                                isHideHVMLView = View.VISIBLE;
                                isHideKeyInputView = View.INVISIBLE;
                                hvmlView.setVisibility(isHideHVMLView);
                                keyInputView.setVisibility(isHideKeyInputView);
                            }
                        }
                    });
                    appointNumberInput.getEditableText().clear();

                } catch (NumberFormatException e) {
                    return;
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG, "onResume()");

        //VoiceUIManagerインスタンス生成.
        if (mVUIManager == null) {
            mVUIManager = VoiceUIManager.getService(getApplicationContext());
        }
        //VoiceUIListenerインスタンス生成.
        if (mVUIListener == null) {
            mVUIListener = new VoiceUIListenerImpl(this);
        }
        //VoiceUIListenerの登録.
        VoiceUIManagerUtil.registerVoiceUIListener(mVUIManager, mVUIListener);

        //Scene有効化.
        VoiceUIManagerUtil.enableScene(mVUIManager, ScenarioDefinitions.SCENE_COMMON);

        handler.post(r);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v(TAG, "onPause()");

        //バックに回ったら発話を中止する.
        VoiceUIManagerUtil.stopSpeech();

        //VoiceUIListenerの解除.
        VoiceUIManagerUtil.unregisterVoiceUIListener(mVUIManager, mVUIListener);

        //Scene無効化.
        VoiceUIManagerUtil.disableScene(mVUIManager, ScenarioDefinitions.SCENE_COMMON);

        mVUIManager = null;
        mVUIListener = null;

        //単一Activityの場合はonPauseでアプリを終了する.
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy()");

        //インスタンスのごみ掃除.
        mVUIManager = null;
        mVUIListener = null;
    }

    /**
     * VoiceUIListenerクラスからのコールバックを実装する.
     */
    @Override
    public void onScenarioEvent(int event, List<VoiceUIVariable> variables) {
        Log.v(TAG, "onScenarioEvent() : " + event);
        String function = VoiceUIVariableUtil.getVariableData(variables, ScenarioDefinitions.ATTR_FUNCTION);
        switch (event) {
            case VoiceUIListenerImpl.ACTION_START:
                if(FUNC_HVML_ACTION.equals(function)) {
                    final String action = VoiceUIVariableUtil.getVariableData(variables, ScenarioDefinitions.KEY_HVML_ACTION);
                    final Topic topic =  mHVMLParser.getTopicFromID(action);
                    if(topic.getActions() != null) {
                        final String speechText = topic.getActions().get(0).getSpeech().getValue();
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if(!isFinishing()) {
                                    ((TextView) findViewById(R.id.TopicName)).setText(speechText);
                                }
                            }
                        });
                    } else if (topic.getCases() != null) {
//                        final String speechText = topic.getCases().get(0).getActions().get(0).getSpeech().getValue();
                        final String speechText = topic.getCaseFromData().getActions().get(0).getSpeech().getValue();
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if(!isFinishing()) {
                                    ((TextView) findViewById(R.id.TopicName)).setText(speechText);
                                }
                            }
                        });
                    }
                }
                break;
            //必要なイベント毎に実装.
            case VoiceUIListenerImpl.ACTION_END:
                if(FUNC_END_APP.equals(function)) {
                    finish();
                    break;
                }else if(FUNC_HVML_ACTION.equals(function)){
                    final String lvcsr = VoiceUIVariableUtil.getVariableData(variables, ScenarioDefinitions.KEY_LVCSR_BASIC);
                    if (!lvcsr.isEmpty()) {
                        Log.d(TAG, "lvcsr: " + lvcsr);
//                        VoiceUIManagerUtil.clearMemory(mVUIManager, ScenarioDefinitions.MEM_P_PATIENT_ID);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if(!isFinishing()) {
                                    try {
                                        Log.d(TAG, lvcsr);
                                        int appointID = Integer.parseInt(lvcsr);
                                        Log.d(TAG, String.valueOf(appointID));
                                        getAppoint(appointID);

                                    } catch (Exception e) {
                                        //パースできなかった場合はもう一度発話頼む
                                        Log.e(TAG, e.toString());
                                        VoiceUIManagerUtil.stopSpeech();
                                        VoiceUIManagerUtil.startSpeech(mVUIManager, ScenarioDefinitions.ACC_TALK_FAILED);

                                    }
                                }
                            }
                        });
                    }
                    final String searchAppoint = VoiceUIVariableUtil.getVariableData(variables, ScenarioDefinitions.KEY_SEARCH_APPOINT);
                    if (!searchAppoint.isEmpty()) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if(!isFinishing()) {
                                    Log.d(TAG, "search appoint: " + searchAppoint);
                                    isHideHVMLView = View.INVISIBLE;
                                    isHideKeyInputView = View.VISIBLE;
                                    hvmlView.setVisibility(isHideHVMLView);
                                    keyInputView.setVisibility(isHideKeyInputView);
                                }
                            }
                        });

                    }

//                    final String lvcsrCheck = VoiceUIVariableUtil.getVariableData(variables, ScenarioDefinitions.KEY_LVCSR_BASIC_CHECK);
//                    if (!lvcsrCheck.isEmpty()) {
//                        Log.d(TAG, "check: " + lvcsrCheck);
//                        VoiceUIManagerUtil.setMemory(mVUIManager, ScenarioDefinitions.MEM_P_PATIENT_ID, lvcsrCheck);
//                    }

                    final String callStaffAction = VoiceUIVariableUtil.getVariableData(variables, ScenarioDefinitions.KEY_CALL_STAFF_ACTION);
                    if (!callStaffAction.isEmpty()) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if(!isFinishing()) {
                                    String number = "0138473163";
                                    Uri call = Uri.parse("tel:" + number);
                                    Intent surf = new Intent(Intent.ACTION_CALL, call);
                                    startActivity(surf);
                                }
                            }
                        });
                    }


                    final String callDirectorAction = VoiceUIVariableUtil.getVariableData(variables, ScenarioDefinitions.KEY_CALL_DIRECTOR_ACTION);
                    if (!callDirectorAction.isEmpty()){
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if(!isFinishing()) {
                                    String number = "07026632734";

                                    Uri call = Uri.parse("tel:" + number);
                                    Intent surf = new Intent(Intent.ACTION_CALL, call);
                                    startActivity(surf);
                                }
                            }
                        });
                    }

                } else if(FUNC_CALLL_ACTION.equals(function)) {
                    final String callAction = VoiceUIVariableUtil.getVariableData(variables, ScenarioDefinitions.KEY_CALL_DIRECTOR_ACTION);
                    if (callAction.isEmpty()) break;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(!isFinishing()) {
                                String number = "07026632734";
                                Uri call = Uri.parse("tel:" + number);
                                Intent surf = new Intent(Intent.ACTION_CALL, call);
                                startActivity(surf);
                            }
                        }
                    });
                }
                break;
            case VoiceUIListenerImpl.RESOLVE_VARIABLE:
                for (VoiceUIVariable variable : variables) {
                    String key = variable.getName();
                    if (ScenarioDefinitions.RESOLVE_JAVA_VALUE.equals(key)) {
                        variable.setStringValue("java");
                    }
                }
                break;
            default:
                break;
        }
    }

    public void getGPIO() {
        if (!mmDNSHandler.getHostIP().isEmpty()) BASE_URL = mmDNSHandler.getHostIP();
        else return;
        Request request = new Request.Builder()
                .url(BASE_URL + GPIO_URL)
                .get()
                .build();

        OkHttpClient client = new OkHttpClient();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("PatientController", e.toString());

            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                Gson gson = new Gson();
                ParameterizedType type = new GenericOf<>(com.example.robohonreception.patient.Response.class, Integer.class);
                com.example.robohonreception.patient.Response<Integer> res = null;
                res = gson.fromJson(response.body().string(), type);
                Log.d(TAG, res.result.toString());
                if (res.result == 1) VoiceUIManagerUtil.startSpeech(mVUIManager, ScenarioDefinitions.ACC_TALK);
                else if (res.result == 2) VoiceUIManagerUtil.startSpeech(mVUIManager, ScenarioDefinitions.ACC_END_TALK);
            }
        });
    }

    public void getAppoint(int id) {
        if (!mmDNSHandler.getHostIP().isEmpty()) BASE_URL = mmDNSHandler.getHostIP();
        Request request = new Request.Builder()
                .url(BASE_URL + APPOINT_URL + id)
                .get()
                .build();

        OkHttpClient client = new OkHttpClient();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("PatientController", e.toString());
                VoiceUIManagerUtil.startSpeech(mVUIManager, ScenarioDefinitions.ACC_TALK_FAILED);
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                Gson gson = new Gson();
                ParameterizedType type = new GenericOf<>(com.example.robohonreception.patient.Response.class, Appoint.class);
                com.example.robohonreception.patient.Response<Appoint> res = null;
                res = gson.fromJson(response.body().string(), type);
                if (res.result.patientName.isEmpty()) {
                    VoiceUIManagerUtil.startSpeech(mVUIManager, ScenarioDefinitions.ACC_TALK_FAILED);
                } else {
                    Log.d("PatientController", res.result.patientName);
                    Log.d("PatientController", res.result.appointTime);
                    VoiceUIManagerUtil.clearMemory(mVUIManager, ScenarioDefinitions.MEM_P_APPOINT_TIME);
                    int ret = VoiceUIManagerUtil.setMemory(mVUIManager, ScenarioDefinitions.MEM_P_APPOINT_TIME, res.result.appointTime);
//                if (ret == VoiceUIManager.VOICEUI_ERROR) throw new RemoteException("failed to set memory");
                    VoiceUIManagerUtil.clearMemory(mVUIManager, ScenarioDefinitions.MEM_P_PATIENT_NAME);
                    ret = VoiceUIManagerUtil.setMemory(mVUIManager, ScenarioDefinitions.MEM_P_PATIENT_NAME, res.result.patientKanaName);
//                if (ret == VoiceUIManager.VOICEUI_ERROR) throw new RemoteException("failed to set memory");

                    if (!res.result.patientID1.isEmpty()) VoiceUIManagerUtil.setMemory(mVUIManager, ScenarioDefinitions.MEM_P_APPOINT_MINUTE, "0");
                    else if (!res.result.patientID2.isEmpty()) VoiceUIManagerUtil.setMemory(mVUIManager, ScenarioDefinitions.MEM_P_APPOINT_MINUTE, "15");
                    else if (!res.result.patientID3.isEmpty()) VoiceUIManagerUtil.setMemory(mVUIManager, ScenarioDefinitions.MEM_P_APPOINT_MINUTE, "30");
                    else if (!res.result.patientID4.isEmpty()) VoiceUIManagerUtil.setMemory(mVUIManager, ScenarioDefinitions.MEM_P_APPOINT_MINUTE, "45");
                    else VoiceUIManagerUtil.setMemory(mVUIManager, ScenarioDefinitions.MEM_P_APPOINT_MINUTE, "0");

                    VoiceUIManagerUtil.startSpeech(mVUIManager, ScenarioDefinitions.ACC_APPOINT);
                }
            }
        });
    }

    class GenericOf<X, Y> implements ParameterizedType {

        private final Class<X> container;
        private final Class<Y> wrapped;

        GenericOf(Class<X> container, Class<Y> wrapped) {
            this.container = container;
            this.wrapped = wrapped;
        }

        @Override
        public Type[] getActualTypeArguments() {
            return new Type[]{wrapped};
        }

        @Override
        public Type getRawType() {
            return container;
        }

        @Override
        public Type getOwnerType() {
            return null;
        }
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

}
