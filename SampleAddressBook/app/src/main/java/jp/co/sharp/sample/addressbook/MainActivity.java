package jp.co.sharp.sample.addressbook;


import jp.co.sharp.android.rb.addressbook.AddressBookCommonUtils;
import jp.co.sharp.android.rb.addressbook.AddressBookManager;
import jp.co.sharp.android.rb.addressbook.AddressBookVariable.OwnerProfileData;
import jp.co.sharp.sample.addressbook.customize.ScenarioDefinitions;
import jp.co.sharp.sample.addressbook.util.VoiceUIManagerUtil;
import jp.co.sharp.android.voiceui.VoiceUIManager;
import jp.co.sharp.android.voiceui.VoiceUIVariable;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.util.Log;

import java.util.List;


/**
 * 電話帳データを利用するサンプルActivity.
 */
public class MainActivity extends Activity implements MainActivityVoiceUIListener.MainActivityScenarioCallback {
    public static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQ_CODE_NEW_ADDRESS = 1;
    private static final int REQ_CODE_SEARCH_ADDRESS = 2;
    //VoiceUIManager.
    private VoiceUIManager mVoiceUIManager = null;
    //MainActivityVoiceUIListener.
    private MainActivityVoiceUIListener mMainVoiceUIListener = null;
    //VoiceUIの再起動イベントを受け取るレシーバー.
    private VoiceUIStartReceiver mVoiceUIStartReceiver;
    //ホームボタンの押下イベントを受け取るレシーバー.
    private HomeEventReceiver mHomeEventReceiver;
    //電話帳イベントを受け取るレシーバー.
    private AddressBookReceiver mAddressBookReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate()");

        setContentView(R.layout.activity_main);

        // 情報クラスで取得.
        Button btnGetOwner = (Button) findViewById(R.id.btn_get_owner);
        btnGetOwner.setOnClickListener(new BtnGetOwnerClickListener(this));

        // KEY指定で取得.
        Button btnGetOwnerKey = (Button) findViewById(R.id.btn_get_owner_key);
        btnGetOwnerKey.setOnClickListener(new BtnGetOwnerKeyClickListener(this));

        //オーナー情報画面起動.
        Button btnAppMod = (Button)findViewById(R.id.btn_app_mod);
        btnAppMod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = Intent.makeMainActivity( new ComponentName(AddressBookCommonUtils.AUTHORITY_NAME, AddressBookCommonUtils.AUTHORITY_CLASS_NAME_ENTRY));
                intent.putExtra(AddressBookCommonUtils.KEY_ADDRESSBOOK_ACTIVITY, AddressBookCommonUtils.OWNER_PROFILE_ACTIVITY);
                startActivity(intent);
            }
        });

        //新規登録
        Button btnAppNew = (Button) findViewById(R.id.btn_app_new);
        btnAppNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = Intent.makeMainActivity(new ComponentName(AddressBookCommonUtils.AUTHORITY_NAME, AddressBookCommonUtils.AUTHORITY_CLASS_NAME_ENTRY));
                intent.putExtra(AddressBookCommonUtils.KEY_ADDRESSBOOK_ACTIVITY, AddressBookCommonUtils.ADDRESSBOOK_ACTIVITY);
                intent.putExtra(AddressBookCommonUtils.KEY_ADDRESSBOOK_ENTRY_TYPE, AddressBookCommonUtils.ADDRESSBOOK_ENTRY_NEW);
                intent.putExtra(AddressBookCommonUtils.KEY_FIRST_NAME, getString(R.string.sample_name));
                startActivityForResult(intent, REQ_CODE_NEW_ADDRESS);
            }
        });

        //検索
        Button btnAppSearch = (Button) findViewById(R.id.btn_app_search);
        btnAppSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView textView = (TextView) findViewById(R.id.editText_name);
                String search = textView.getText().toString();

                Intent intent = Intent.makeMainActivity(new ComponentName(AddressBookCommonUtils.AUTHORITY_NAME, AddressBookCommonUtils.AUTHORITY_CLASS_NAME_SEARCH));
                intent.putExtra(AddressBookCommonUtils.KEY_ADDRESSBOOK_SEARCH_VALUE, search);
                intent.putExtra(AddressBookCommonUtils.KEY_ADDRESSBOOK_SEARCH_ITEM, AddressBookCommonUtils.KEY_MAIL_ADDRESS);
                startActivityForResult(intent, REQ_CODE_SEARCH_ADDRESS);
            }
        });

        //削除
        Button btnAppDelete = (Button) findViewById(R.id.btn_app_delete);
        btnAppDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView textView = (TextView) findViewById(R.id.editText_name);
                String search = textView.getText().toString();

                Intent intent = Intent.makeMainActivity(new ComponentName(AddressBookCommonUtils.AUTHORITY_NAME, AddressBookCommonUtils.AUTHORITY_CLASS_NAME_DELETE));
                intent.putExtra(AddressBookCommonUtils.KEY_ADDRESSBOOK_SEARCH_VALUE, search);
                startActivity(intent);
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

        //電話帳Broadcastの検知登録.
        mAddressBookReceiver = new AddressBookReceiver();
        IntentFilter filterAddressBook = new IntentFilter("jp.co.sharp.android.rb.extra.ContactId_ACTION");
        registerReceiver(mAddressBookReceiver, filterAddressBook);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE_NEW_ADDRESS:
                if(resultCode == AddressBookCommonUtils.RESULT_OK) {
                    int contact_id = data.getIntExtra(AddressBookCommonUtils.KEY_ADDRESSBOOK_INTENT_CONTACTID, 0);
                    Log.d(TAG, "AddressBookAdd");
                    TextView textSetting = (TextView)findViewById(R.id.view_contact_id);
                    String text   = "ContactID:" + contact_id;
                    textSetting.setText(text);
                }
                break;
            case REQ_CODE_SEARCH_ADDRESS:
                if((resultCode == AddressBookCommonUtils.RESULT_OK) || (resultCode == AddressBookCommonUtils.RESULT_OK_CHOICE)) {
                    Log.d(TAG, "AddressBookSerch");
                    String main_address = data.getStringExtra(AddressBookCommonUtils.KEY_MAIL_ADDRESS);
                    TextView textSetting = (TextView)findViewById(R.id.view_mail_address);
                    String text   = "Mail Adress:" + main_address;
                    textSetting.setText(text);

                }else if(resultCode == AddressBookCommonUtils.RESULT_FAILED_NO_CONTENTID) {
                    Log.d(TAG, "AddressBookSerch ERR");
                    TextView textSetting = (TextView)findViewById(R.id.view_mail_address);
                    String text   = "選択した名前は登録されてないよ";
                    textSetting.setText(text);

                }
                break;
            default:
                break;
        }
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
        if (mMainVoiceUIListener == null) {
            mMainVoiceUIListener = new MainActivityVoiceUIListener(this);
        }
        //VoiceUIListenerの登録.
        VoiceUIManagerUtil.registerVoiceUIListener(mVoiceUIManager, mMainVoiceUIListener);
        //Scene有効化.
        VoiceUIManagerUtil.enableScene(mVoiceUIManager, ScenarioDefinitions.SCENE);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v(TAG, "onPause()");

        //バックに回ったら発話を中止する.
        VoiceUIManagerUtil.stopSpeech();
        //VoiceUIListenerの解除.
        VoiceUIManagerUtil.unregisterVoiceUIListener(mVoiceUIManager, mMainVoiceUIListener);
        //Scene無効化.
        VoiceUIManagerUtil.disableScene(mVoiceUIManager, ScenarioDefinitions.SCENE);
        //onActivityResult()で起動結果を受け取るためバックに回ってもアプリは終了しない.
        //finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy()");

        //ホームボタンの検知破棄.
        this.unregisterReceiver(mHomeEventReceiver);
        //VoiceUI再起動の検知破棄.
        this.unregisterReceiver(mVoiceUIStartReceiver);
        //電話帳の検知破棄.
        this.unregisterReceiver(mAddressBookReceiver);
        //インスタンスのごみ掃除.
        mVoiceUIManager = null;
        mMainVoiceUIListener = null;
    }

    @Override
    public void onExitCommand(String command, List<VoiceUIVariable> variables) {
        Log.d(TAG, "onExitCommand()" + command);
        switch (command) {
            default:
                break;
        }
    }

    public String GetOwnerBirth() {
        String owner_birthday = getString(R.string.text_description_birthday) + getString(R.string.default_birth);
        try {
            AddressBookManager abm = AddressBookManager.getService(this);
            OwnerProfileData owner = abm.getOwnerProfileData();
            owner_birthday = getString(R.string.text_description_birthday) +  owner.getBirthday_year() + "/"
                    +  owner.getBirthday_month() +"/" +  owner.getBirthday_day();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return owner_birthday;
    }

    // ホームボタンの押下イベントを受け取るためのBroadcastレシーバー クラス.
    private class HomeEventReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Receive Home button pressed");
            // ホームボタン押下でアプリ終了する.
            finish();
        }
    }

    // VoiceUI再起動イベントを受け取るためのBroadcastレシーバー クラス.
    private class VoiceUIStartReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // 音声UIのServiceが再起動したらVoiceUIの再取得とListener再登録をする.
            // 起動初回は必ず再起動が取得できるので注意.
            if (VoiceUIManager.ACTION_VOICEUI_SERVICE_STARTED.equals(action)) {
                Log.d(TAG, "Receive:VOICEUI_SERVICE_STARTED");
                //VoiceUIManagerのインスタンス取得.
                mVoiceUIManager = VoiceUIManager.getService(getApplicationContext());
                if (mMainVoiceUIListener == null) {
                    mMainVoiceUIListener = new MainActivityVoiceUIListener(getApplicationContext());
                }
                //VoiceUIListenerの登録.
                VoiceUIManagerUtil.registerVoiceUIListener(mVoiceUIManager, mMainVoiceUIListener);
            }
        }
    }

    // 電話帳イベントを受け取るためのBroadcastレシーバー クラス.
    private class AddressBookReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int contact_id = intent.getIntExtra
                    (AddressBookCommonUtils.KEY_ADDRESSBOOK_INTENT_CONTACTID, 0);
            int intent_type = intent.getIntExtra
                    (AddressBookCommonUtils.KEY_ADDRESSBOOK_INTENT_TYPE,
                     AddressBookCommonUtils.ADDRESSBOOK_INTENT_UPDATE);
            if ((contact_id == AddressBookCommonUtils.CONTACT_ID_OWNER) &&
                    (intent_type ==  AddressBookCommonUtils.ADDRESSBOOK_INTENT_UPDATE)) {
                Log.d(TAG, "Update CONTACT_ID_OWNER");
                TextView textSetting = (TextView)findViewById(R.id.txt_desc_birthday);
                textSetting.setText(GetOwnerBirth());
            } else {
                Log.d(TAG, "Not CONTACT_ID_OWNER");
            }
        }
    }

    // オーナー名取得(情報クラス)ボタン処理.
    private class BtnGetOwnerClickListener implements View.OnClickListener {
        private Context _context;

        public BtnGetOwnerClickListener(Context context) {
            _context = context;
        }

        @Override
        public void onClick(View view) {
            AddressBookManager addressMng = AddressBookManager.getService(_context);
            String owner_name = _context.getString(R.string.default_name);
            try {
                OwnerProfileData ownerData = addressMng.getOwnerProfileData();
                String nickName = ownerData.getNickname();
                if(nickName != null && !("".equals(nickName)) ) {
                    owner_name = nickName;
                }
                String firstName = ownerData.getFirstname();
                if(firstName != null && !("".equals(firstName)) ) {
                    firstName = firstName + _context.getString(R.string.honorific);
                    owner_name = firstName;
                }
                String lastName = ownerData.getLastname();
                if(lastName != null && !("".equals(lastName)) ) {
                    lastName = lastName + _context.getString(R.string.honorific);
                    owner_name = lastName;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            TextView textView = (TextView)findViewById(R.id.txt_desc_owner_name);
            String text = _context.getString(R.string.text_description_owner_name) + owner_name;
            textView.setText(text);
        }
    }

    // オーナー名取得(KEY)ボタン処理.
    private class BtnGetOwnerKeyClickListener implements View.OnClickListener {
        private Context _context;

        public BtnGetOwnerKeyClickListener(Context context) {
            _context = context;
        }

        @Override
        public void onClick(View view) {
            AddressBookManager addressMng = AddressBookManager.getService(_context);
            String owner_name = _context.getString(R.string.default_name);
            try {
                String nickName =  addressMng.getOwnerProfileDataItemString(AddressBookCommonUtils.KEY_NICKNAME);
                if(nickName != null && !("".equals(nickName)) ) {
                    owner_name = nickName;
                }
                String firstName = addressMng.getOwnerProfileDataItemString(AddressBookCommonUtils.KEY_FIRST_NAME);
                if(firstName != null && !("".equals(firstName)) ) {
                    firstName = firstName + _context.getString(R.string.honorific);
                    owner_name = firstName;
                }
                String lastName = addressMng.getOwnerProfileDataItemString(AddressBookCommonUtils.KEY_LAST_NAME);
                if(lastName != null && !("".equals(lastName)) ) {
                    lastName = lastName + _context.getString(R.string.honorific);
                    owner_name = lastName;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            TextView textView = (TextView)findViewById(R.id.txt_desc_owner_name);
            String text = _context.getString(R.string.text_description_owner_name) + owner_name;
            textView.setText(text);
        }
    }
}
