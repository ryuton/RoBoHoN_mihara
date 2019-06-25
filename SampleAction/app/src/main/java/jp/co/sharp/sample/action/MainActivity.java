package jp.co.sharp.sample.action;

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

import java.util.LinkedHashMap;

import jp.co.sharp.android.rb.action.ActionUtil;

public class MainActivity extends Activity {
    public static final String TAG = MainActivity.class.getSimpleName();

    /**
     * ホームボタンイベント検知.
     */
    private HomeEventReceiver mHomeEventReceiver;
    /**
     * アクション実行結果通知用Action定義
     * */
    public static final String ACTION_RESULT_ACTION = "jp.co.sharp.sample.action.action.RESULT_ACTION";
    /**
     * アクション実行結果取得用.
     */
    private ActionResultReceiver mActionResultReceiver;

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

        //アクション連携起動結果取得用レシーバー登録.
        mActionResultReceiver = new ActionResultReceiver();
        IntentFilter filterAction = new IntentFilter(ACTION_RESULT_ACTION);
        registerReceiver(mActionResultReceiver, filterAction);

        LinkedHashMap<Integer,String> idList = ActionUtil.getInfo(this);
        Log.d("Action List", idList.entrySet().toString());

        //NumberPickerの実装
        numPicker = (NumberPicker)findViewById(R.id.numberPicker);
        numPicker.setMaxValue(idList.size());
        numPicker.setMinValue(1);

        //指定ボタンの実装.
        Button assignButton = (Button) findViewById(R.id.action_assign_button);
        assignButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendBroadcast(getIntentForAction(numPicker.getValue()));
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

        //アクション結果用レシーバーの破棄.
        this.unregisterReceiver(mActionResultReceiver);

    }

    /**
     * アクション開始用のIntentを設定する.
     */
    private Intent getIntentForAction(int id) {
        Intent intent = new Intent(ActionUtil.ACTION_REQUEST_ACTION);
        intent.putExtra(ActionUtil.EXTRA_REPLYTO_ACTION, ACTION_RESULT_ACTION);
        intent.putExtra(ActionUtil.EXTRA_REPLYTO_PKG, getPackageName());
        intent.putExtra(ActionUtil.EXTRA_REQUEST_ID, id);
        return intent;
    }

    /**
     * アクション実行結果を受け取るためのBroadcastレシーバー クラス.<br>
     * <p/>
     */
    private class ActionResultReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int result = intent.getIntExtra(ActionUtil.EXTRA_RESULT_CODE, ActionUtil.RESULT_CANCELED);
            if (result == ActionUtil.RESULT_OK) {
                // 正常に完了した場合.
                // アクションID.
                String idDisp = null;
                int id = intent.getIntExtra(ActionUtil.EXTRA_RESULT_ID, -1);
                idDisp = String.valueOf(id);
                dispId(idDisp);

                // アクション名.
                String nameDisp = null;
                String name = intent.getStringExtra(ActionUtil.EXTRA_RESULT_NAME);
                    nameDisp = name;
                dispName(nameDisp);
            } else {
                // 中断/キャンセルで終了した場合.
                Log.d(TAG, "Action is cancelled.");
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
        tv.setText("Action ID : " + id);
    }

    private void dispName(String name) {
        if (name == null) name = new String("");
        TextView tv = (TextView) findViewById(R.id.text_name);
        tv.setText("Action NAME : " + name);
    }

}
