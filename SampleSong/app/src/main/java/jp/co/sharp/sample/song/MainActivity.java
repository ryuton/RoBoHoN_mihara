package jp.co.sharp.sample.song;

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

import jp.co.sharp.android.rb.song.SongUtil;

public class MainActivity extends Activity {
    public static final String TAG = MainActivity.class.getSimpleName();

    /**
     * ホームボタンイベント検知.
     */
    private HomeEventReceiver mHomeEventReceiver;
    /**
     * 歌実行結果通知用Action定義.
     * */
    public static final String ACTION_RESULT_SONG = "jp.co.sharp.sample.song.action.RESULT_SONG";
    /**
     * 歌実行結果取得用.
     */
    private SongResultReceiver mSongResultReceiver;

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

        //歌実行結果取得用レシーバー登録.
        mSongResultReceiver = new SongResultReceiver();
        IntentFilter filterSong = new IntentFilter(ACTION_RESULT_SONG);
        registerReceiver(mSongResultReceiver, filterSong);

        //歌の一覧取得
        LinkedHashMap<Integer,String> idList = SongUtil.getInfo(this);
        Log.d("Song List", idList.entrySet().toString());

        //NumberPickerの実装
        numPicker = (NumberPicker)findViewById(R.id.numberPicker);
        numPicker.setMaxValue(idList.size());
        numPicker.setMinValue(1);

        //ランダムボタンの実装.
        Button ramdomButton = (Button) findViewById(R.id.song_normal_button);
        ramdomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBroadcast(getIntentForSong(SongUtil.EXTRA_TYPE_NORMAL));
                dispId("");
                dispName("");
            }
        });

        //繰り返しボタンの実装.
        Button repeatButton = (Button) findViewById(R.id.song_repeat_button);
        repeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBroadcast(getIntentForSong(SongUtil.EXTRA_TYPE_REPEAT));
                dispId("");
                dispName("");
            }
        });

        //停止ボタンの実装.
        Button stopButton = (Button) findViewById(R.id.song_stop_button);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBroadcast(getIntentForSong(SongUtil.EXTRA_TYPE_STOP));
                dispId("");
                dispName("");
            }
        });

        //指定ボタンの実装.
        Button assignButton = (Button) findViewById(R.id.song_assign_button);
        assignButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBroadcast(getIntentForSong(SongUtil.EXTRA_TYPE_ASSIGN));
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

        //歌結果用レシーバーの破棄.
        this.unregisterReceiver(mSongResultReceiver);

    }

    /**
     * 歌開始用のIntentを設定する.
     */
    private Intent getIntentForSong(String type) {
        Intent intent = new Intent(SongUtil.ACTION_REQUEST_SONG);
        intent.putExtra(SongUtil.EXTRA_REPLYTO_ACTION, ACTION_RESULT_SONG);
        intent.putExtra(SongUtil.EXTRA_REPLYTO_PKG, getPackageName());
        intent.putExtra(SongUtil.EXTRA_TYPE, type);
        if (type.equals(SongUtil.EXTRA_TYPE_ASSIGN)) {
            intent.putExtra(SongUtil.EXTRA_REQUEST_ID, numPicker.getValue());
        }
        return intent;
    }

    /**
     * 歌実行結果を受け取るためのBroadcastレシーバー クラス.<br>
     * <p/>
     */
    private class SongResultReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int result = intent.getIntExtra(SongUtil.EXTRA_RESULT_CODE, SongUtil.RESULT_CANCELED);
            if (result == SongUtil.RESULT_OK) {
                // 正常に完了した場合.
                // 歌ID.
                String idDisp = null;
                int id = intent.getIntExtra(SongUtil.EXTRA_RESULT_ID, -1);
                idDisp = String.valueOf(id);
                dispId(idDisp);

                // 歌名.
                String nameDisp = null;
                String name = intent.getStringExtra(SongUtil.EXTRA_RESULT_NAME);
                    nameDisp = name;
                dispName(nameDisp);
            } else {
                // 中断/キャンセルで終了した場合.
                Log.d(TAG, "Song is cancelled.");
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
        tv.setText("Song ID : " + id);
    }

    private void dispName(String name) {
        if (name == null) name = new String("");
        TextView tv = (TextView) findViewById(R.id.text_name);
        tv.setText("Song NAME : " + name);
    }

}
