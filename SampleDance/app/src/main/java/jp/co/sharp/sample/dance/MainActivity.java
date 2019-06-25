package jp.co.sharp.sample.dance;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import java.util.Arrays;
import java.util.LinkedHashMap;

import jp.co.sharp.android.rb.rbdance.DanceUtil;

public class MainActivity extends Activity {
    public static final String TAG = MainActivity.class.getSimpleName();

    /**
     * ホームボタンイベント検知.
     */
    private HomeEventReceiver mHomeEventReceiver;
    /**
     * ダンス実行結果通知用Action定義
     * */
    public static final String ACTION_RESULT_DANCE = "jp.co.sharp.sample.dance.action.RESULT_DANCE";
    /**
     * ダンス実行結果取得用.
     */
    private DanceResultReceiver mDanceResultReceiver;
    /**
     * ダンスID指定用.
     */
    private NumberPicker numPicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate()");
        setContentView(R.layout.activity_main);

        //ホームボタンの検知登録.
        mHomeEventReceiver = new HomeEventReceiver();
        IntentFilter filterHome = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mHomeEventReceiver, filterHome);
        
        //ダンス連携起動結果取得用レシーバー登録.
        mDanceResultReceiver = new DanceResultReceiver();
        IntentFilter filterDance = new IntentFilter(ACTION_RESULT_DANCE);
        registerReceiver(mDanceResultReceiver, filterDance);

        //ランダムボタンの実装.
        Button ramdomButton = (Button) findViewById(R.id.dance_normal_button);
        ramdomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBroadcast(getIntentForDance(DanceUtil.EXTRA_TYPE_NORMAL));
                dispId("");
                dispName("");
            }
        });

        //最新ボタンの実装.
        Button newButton = (Button) findViewById(R.id.dance_new_button);
        newButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBroadcast(getIntentForDance(DanceUtil.EXTRA_TYPE_NEW));
                dispId("");
                dispName("");
            }
        });

        //繰り返しボタンの実装.
        Button repeatButton = (Button) findViewById(R.id.dance_repeat_button);
        repeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBroadcast(getIntentForDance(DanceUtil.EXTRA_TYPE_REPEAT));
                dispId("");
                dispName("");
            }
        });


        //Dance IDの一覧取得
        LinkedHashMap<Integer,String> idList = DanceUtil.getInfo(this);
        Log.d("Dance List", idList.entrySet().toString());

        //NumberPickerの実装
        numPicker = (NumberPicker)findViewById(R.id.numberPicker);
        numPicker.setMaxValue(idList.size());
        numPicker.setMinValue(1);

        //指定ボタンの実装.
        Button assignButton = (Button) findViewById(R.id.dance_assign_button);
        assignButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBroadcast(getIntentForDance(DanceUtil.EXTRA_TYPE_ASSIGN));
                dispId("");
                dispName("");
            }
        });

        //メドレーボタンの実装.
        Button medleyButton = (Button) findViewById(R.id.dance_medley_button);
        medleyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBroadcast(getIntentForDance(DanceUtil.EXTRA_TYPE_MEDLEY));
                dispId("");
                dispName("");
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy()");

        //ホームボタンの検知破棄.
        this.unregisterReceiver(mHomeEventReceiver);

        //ダンス結果用レシーバーの破棄.
        this.unregisterReceiver(mDanceResultReceiver);

    }

    /**
     * ダンス開始用のIntentを設定する.
     */
    private Intent getIntentForDance(String type) {
        Intent intent = new Intent(DanceUtil.ACTION_REQUEST_DANCE);
        intent.putExtra(DanceUtil.EXTRA_REPLYTO_ACTION, ACTION_RESULT_DANCE);
        intent.putExtra(DanceUtil.EXTRA_REPLYTO_PKG, getPackageName());
        intent.putExtra(DanceUtil.EXTRA_TYPE, type);
        if (type.equals(DanceUtil.EXTRA_TYPE_ASSIGN)) {
            intent.putExtra(DanceUtil.EXTRA_REQUEST_ID, numPicker.getValue());
        } else
        if (type.equals(DanceUtil.EXTRA_TYPE_MEDLEY)) {
            intent.putExtra(DanceUtil.EXTRA_REQUEST_ID_LIST, new int[] {1, 2, 3});
        }
        intent.putExtra(DanceUtil.EXTRA_SKIP_COMMENT, false);
        return intent;
    }

    /**
     * ダンス実行結果を受け取るためのBroadcastレシーバー クラス.<br>
     * <p/>
     */
    private class DanceResultReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int result = intent.getIntExtra(DanceUtil.EXTRA_RESULT_CODE, DanceUtil.RESULT_CANCELED);
            if (result == DanceUtil.RESULT_OK) {
                // 正常に完了した場合.
                // ダンスID.
                String idDisp = null;
                int id = intent.getIntExtra(DanceUtil.EXTRA_RESULT_ID, -1);
                if (id == -1) {
                    idDisp = Arrays.toString(intent.getIntArrayExtra(DanceUtil.EXTRA_RESULT_ID_LIST));
                } else {
                    idDisp = String.valueOf(id);
                }
                dispId(idDisp);

                // ダンス名.
                String nameDisp = null;
                String name = intent.getStringExtra(DanceUtil.EXTRA_RESULT_NAME);
                if (name == null) {
                    nameDisp = Arrays.toString(intent.getStringArrayExtra(DanceUtil.EXTRA_RESULT_NAME_LIST));
                } else {
                    nameDisp = name;
                }
                dispName(nameDisp);
            } else {
                // 中断/キャンセルで終了した場合.
                Log.d(TAG, "Dance is cancelled.");
            }
        }
    }

    /**
     * ホームボタンの押下イベントを受け取るためのBroadcastレシーバークラス.<br>
     * <p/>
     * アプリは必ずホームボタンで終了する.
     */
    private class HomeEventReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG, "Receive Home button pressed");
            // ホームボタン押下でアプリ終了する.
            finish();
        }
    }

    private void dispId(String id) {
        if (id == null) id = new String("");
        TextView tv = (TextView) findViewById(R.id.text_id);
        tv.setText("Dance ID : " + id);
    }

    private void dispName(String name) {
        if (name == null) name = new String("");
        TextView tv = (TextView) findViewById(R.id.text_name);
        tv.setText("Dance NAME : " + name);
    }

}
