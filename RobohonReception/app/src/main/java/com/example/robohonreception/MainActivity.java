package com.example.robohonreception;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;

import jp.co.sharp.android.voiceui.VoiceUIManager;
import jp.co.sharp.android.voiceui.VoiceUIVariable;
import com.example.robohonreception.R;

import com.example.robohonreception.voiceui.ScenarioDefinitions;
import com.example.robohonreception.voiceui.VoiceUIListenerImpl;
import com.example.robohonreception.voiceui.VoiceUIManagerUtil;
import com.example.robohonreception.voiceui.VoiceUIVariableUtil;

import static com.example.robohonreception.voiceui.ScenarioDefinitions.FUNC_END_APP;
import static com.example.robohonreception.voiceui.ScenarioDefinitions.FUNC_HVML_ACTION;
import static com.example.robohonreception.voiceui.ScenarioDefinitions.FUNC_RECOG_TALK;


/**
 * 音声UIを利用した基本的な機能だけ実装したActivity.
 */
public class MainActivity extends Activity implements VoiceUIListenerImpl.ScenarioCallback {
    public static final String TAG = MainActivity.class.getSimpleName();

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        //ホームボタンの検知登録.
        mHomeEventReceiver = new HomeEventReceiver();
        IntentFilter filterHome = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mHomeEventReceiver, filterHome);

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

        //単一Activityの場合はonPauseでアプリを終了する.
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy()");

        //ホームボタンの検知破棄.
        this.unregisterReceiver(mHomeEventReceiver);

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
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(!isFinishing()) {
                                ((TextView) findViewById(R.id.TopicName)).setText("Topic:" + action);
                            }
                        }
                    });
                }
            //必要なイベント毎に実装.
            case VoiceUIListenerImpl.ACTION_END:
                if(FUNC_END_APP.equals(function)) {
                    finish();
                    break;
                }else if(FUNC_RECOG_TALK.equals(function)){
                    final String lvcsr = VoiceUIVariableUtil.getVariableData(variables, ScenarioDefinitions.KEY_LVCSR_BASIC);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(!isFinishing()) {
//                                ((TextView) findViewById(R.id.recog_text)).setText("Lvcsr:"+lvcsr);
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
