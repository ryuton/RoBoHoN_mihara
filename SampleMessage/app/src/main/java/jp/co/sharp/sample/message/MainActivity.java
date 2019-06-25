package jp.co.sharp.sample.message;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.Arrays;

import jp.co.sharp.android.rb.messaging.MessagingUtil;

public class MainActivity extends Activity {
    public static final String TAG = MainActivity.class.getSimpleName();

    /**
     * ホームボタンイベント検知.
     */
    private HomeEventReceiver mHomeEventReceiver;
    /**
     * メッセージ送信結果通知用Action定義
     * */
    public static final String ACTION_RESULT_MESSAGE = "jp.co.sharp.sample.message.action.RESULT_MESSAGE";
    /**
     * メッセージ送信結果取得用.
     */
    private MessageResultReceiver mMessageResultReceiver;
    /**
     * 宛先、本文のテキストボックス
     */
    private EditText mEditTextAddr = null; //宛先
    private EditText mEditTextBody = null; //本文
    /**
     * 添付画像のファイルパス
     */
    private String mFilePath = null;
    /**
     * ユーザー確認をスキップするか
     */
    private boolean mSkipConfirm = false;
    /**
     * バックグラウンド送信を行うか
     */
    private boolean mBackGround = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate()");
        setContentView(R.layout.activity_main);

        //ホームボタンの検知登録.
        mHomeEventReceiver = new HomeEventReceiver();
        IntentFilter filterHome = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mHomeEventReceiver, filterHome);

        //メッセージ連携起動結果取得用レシーバー登録.
        mMessageResultReceiver = new MessageResultReceiver();
        IntentFilter filterMessage = new IntentFilter(ACTION_RESULT_MESSAGE);
        registerReceiver(mMessageResultReceiver, filterMessage);

        //宛先、本文のEditText.
        mEditTextAddr = (EditText)findViewById(R.id.edittext_addr);
        mEditTextBody = (EditText)findViewById(R.id.edittext_body);

        //[添付画像を選択]ボタンの実装.
        Button attachmentButton = (Button) findViewById(R.id.button_attachment);
        attachmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (android.os.Build.VERSION.SDK_INT < 19) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/jpeg");
                    startActivityForResult(intent, 0);
                } else {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/jpeg");
                    startActivityForResult(intent, 1);
                }
            }
        });

        //[送信実行]ボタンの実装.
        Button sendButton = (Button) findViewById(R.id.button_send);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 送信結果テキストを空にする.
                TextView tv_result = (TextView) findViewById(R.id.text_result);
                tv_result.setText(getResources().getString(R.string.txt_result));
                // メッセージ送信要求.
                sendBroadcast(getIntentForMessage());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy()");

        //ホームボタンの検知破棄.
        this.unregisterReceiver(mHomeEventReceiver);

        //メッセージ送信結果用レシーバーの破棄.
        this.unregisterReceiver(mMessageResultReceiver);
    }

    /**
     * メッセージ送信用のIntentを設定する.
     */
    private Intent getIntentForMessage() {
        String  mail_addr       = (mEditTextAddr != null) ? mEditTextAddr.getText().toString() : null;
        String  body            = (mEditTextBody != null) ? mEditTextBody.getText().toString() : null;
        String  attachment_path = mFilePath;
        boolean skip_confirm    = mSkipConfirm;
        boolean background      = mBackGround;

        Intent intent = new Intent(MessagingUtil.ACTION_SEND_MESSAGE);
        if (mail_addr != null) {
            intent.putExtra(MessagingUtil.EXTRA_EMAIL, mail_addr);
        }
        intent.putExtra(MessagingUtil.EXTRA_SUBJECT, (String) null);
        if (body != null) {
            intent.putExtra(MessagingUtil.EXTRA_TEXT, body);
        }
        if (attachment_path != null) {
            intent.putExtra(MessagingUtil.EXTRA_ATTACHMENT_PATH, attachment_path);
        }
        intent.putExtra(MessagingUtil.EXTRA_SKIP_CONFIRM,   skip_confirm);
        intent.putExtra(MessagingUtil.EXTRA_BACKGROUND,     background);
        intent.putExtra(MessagingUtil.EXTRA_REPLYTO_ACTION, ACTION_RESULT_MESSAGE);
        intent.putExtra(MessagingUtil.EXTRA_REPLYTO_PKG,    getPackageName());

        return intent;
    }

    /**
     * メッセージ送信結果を受け取るためのBroadcastレシーバー クラス.<br>
     * <p/>
     */
    private class MessageResultReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            TextView tv_result = (TextView) findViewById(R.id.text_result);

            int result = intent.getIntExtra(MessagingUtil.EXTRA_RESULT_CODE, MessagingUtil.RESULT_CANCELED);
            if (result == MessagingUtil.RESULT_OK) {
                // 正常に完了した場合.
                Log.d(TAG, "SEND_MESSAGE: Success!!");

                tv_result.setText(getResources().getString(R.string.txt_result) + "成功");
            } else {
                // 中断/キャンセルで終了した場合.
                Log.d(TAG, "SEND_MESSAGE: Canceled...");

                tv_result.setText(getResources().getString(R.string.txt_result) + "失敗/ｷｬﾝｾﾙ");
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

    /**
     * 画像ピッカから戻った際の結果コールバック.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        Log.d(TAG, "onActivityResult: requestCode = " + requestCode + " resultCode = " + resultCode);

        String path = null;

        if (resultCode != RESULT_OK) return;

        if (requestCode == 0) {
            String[] columns = {MediaColumns.DATA};
            Cursor cursor = getContentResolver().query(resultData.getData(), columns, null, null, null);

            if (cursor.moveToFirst()) {
                File file = new File(cursor.getString(0));
                // fileから写真を読み込む
                path = file.getPath();
                Log.d(TAG, "  path = " + file.getPath());
            }
        } else if (requestCode == 1) {
            String id = DocumentsContract.getDocumentId(resultData.getData());
            String selection = "_id=?";
            String[] selectionArgs = new String[]{id.split(":")[1]};

            Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{MediaColumns.DATA}, selection, selectionArgs, null);

            if (cursor.moveToFirst()) {
                File file = new File(cursor.getString(0));
                // fileから写真を読み込む
                path = file.getPath();
                Log.d(TAG, "  path = " + file.getPath());
            }
            cursor.close();
        } else {
            return;
        }
        mFilePath = path;

        // 添付画像を画面に表示
        ImageView imageView = (ImageView) findViewById(R.id.attached_image);
        if (imageView != null) {
            Bitmap bmImg = BitmapFactory.decodeFile(mFilePath);
            imageView.setImageBitmap(bmImg);
            imageView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * チェックボックスのタップリスナー.
     */
    public void onCheckboxClicked(View view) {
        switch(view.getId()) {
        case R.id.chkbx_skip_confirm:
            mSkipConfirm = ((CheckBox) view).isChecked();
            break;
        case R.id.chkbx_background:
            mBackGround = ((CheckBox) view).isChecked();
            break;
        default:
            break;
        }
    }
}
