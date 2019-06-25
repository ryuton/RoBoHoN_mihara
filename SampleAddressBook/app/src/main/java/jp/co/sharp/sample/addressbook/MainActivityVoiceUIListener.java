package jp.co.sharp.sample.addressbook;


import jp.co.sharp.android.voiceui.VoiceUIListener;
import jp.co.sharp.android.voiceui.VoiceUIVariable;

import android.content.Context;
import android.util.Log;
import java.util.List;


/**
 * 音声UIからの通知を処理する.
 * Callbackの中では重い処理をしないこと.
 */
public class MainActivityVoiceUIListener implements VoiceUIListener {
    private static final String TAG = MainActivityVoiceUIListener.class.getSimpleName();

    private MainActivityScenarioCallback mCallback;

    /**
     * Activityへの通知用IFクラス.
     */
    public static interface MainActivityScenarioCallback{
        /**
         * 実行されたcontrolの通知.
         * @param   function  実行された操作コマンド種別.
         */
        public void onExitCommand(String function, List<VoiceUIVariable> variables);
    }

    /**
     * Activity側でのCallback実装チェック（実装してないと例外発生）.
     */
    public MainActivityVoiceUIListener(Context context) {
        super();
        try{
            mCallback = (MainActivityScenarioCallback) context;
        }catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement " + TAG);
        }
    }

    @Override
    public void onVoiceUIEvent(List<VoiceUIVariable> variables) {
        //controlタグからの通知(シナリオ側にcontrolタグのあるActionが開始されると呼び出される).
    }

    @Override
    public void onVoiceUIActionEnd(List<VoiceUIVariable> variables) {
        //Actionの完了通知(シナリオ側にcontrolタグを書いたActionが完了すると呼び出される).
        Log.d(TAG, "onVoiceUIActionEnd");
    }

    @Override
    public void onVoiceUIResolveVariable(List<VoiceUIVariable> variables) {
        //アプリ側での変数解決用コールバック(シナリオ側にパッケージ名をつけた変数を書いておくと呼び出される).
        Log.d(TAG, "onVoiceUIResolveVariable");
    }

    @Override
    public void onVoiceUIActionCancelled(List<VoiceUIVariable> variables) {
        //priorityが高いシナリオに割り込まれた場合の通知.
        Log.d(TAG, "onVoiceUIActionCancelled");
    }

    @Override
    public void onVoiceUIRejection(VoiceUIVariable variable) {
        //priority負けなどで発話が棄却された場合のコールバック.
        Log.d(TAG, "onVoiceUIRejection");
    }

    @Override
    public void onVoiceUISchedule(int i) {
        //処理不要(リマインダーアプリ以外は使われない).
    }

}
