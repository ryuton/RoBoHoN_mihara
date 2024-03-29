package jp.co.sharp.sample.simple.util;


import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jp.co.sharp.android.voiceui.VoiceUIListener;
import jp.co.sharp.android.voiceui.VoiceUIManager;
import jp.co.sharp.android.voiceui.VoiceUIVariable;
import jp.co.sharp.sample.simple.customize.ScenarioDefinitions;

/**
 * VoiceUIManager関連のUtilityクラス.
 */
public class VoiceUIManagerUtil {
    public static final String TAG = VoiceUIManagerUtil.class.getSimpleName();

    //static クラスとして使用する.
    private VoiceUIManagerUtil(){}

    /**
     * {@link VoiceUIManager#registerVoiceUIListener} のラッパー関数
     * @param vm VoiceUIManagerインスタンス
     * @param listener {@link VoiceUIListener}
     * @return 関数の実行結果
     * @see VoiceUIManager#registerVoiceUIListener(VoiceUIListener)
     */
    static public int registerVoiceUIListener (VoiceUIManager vm, VoiceUIListener listener) {
        int result = VoiceUIManager.VOICEUI_ERROR;
        if (vm != null) {
            try {
                result = vm.registerVoiceUIListener(listener);
            } catch (RemoteException e) {
                Log.e(TAG, "Failed registerVoiceUIListener.[" + e.getMessage() + "]");
            }
        }
        return result;
    }

    /**
     * {@link VoiceUIManager#unregisterVoiceUIListener} のラッパー関数
     * @param vm VoiceUIManagerインスタンス
     * @param listener {@link VoiceUIListener}
     * @return 関数の実行結果
     * @see VoiceUIManager#unregisterVoiceUIListener(VoiceUIListener)
     */
    public static int unregisterVoiceUIListener (VoiceUIManager vm, VoiceUIListener listener) {
        int result = VoiceUIManager.VOICEUI_ERROR;
        if (vm != null) {
            try {
                result = vm.unregisterVoiceUIListener(listener);
            } catch (RemoteException e) {
                Log.e(TAG, "Failed unregisterVoiceUIListener.[" + e.getMessage() + "]");
            }
        }
        return result;
    }

    /**
     * sceneを有効にする.
     * <br>
     * 指定のsceneを1つだけ有効化するのみであり、複数指定も発話指定もしない.
     *
     * @param vm VoiceUIManagerインスタンス.
     *            {@code null}の場合は {@code VoiceUIManager.VOICEUI_ERROR} を返す.
     * @param scene 有効にするscene名.
     *              {@code null}や空文字の場合は {@code VoiceUIManager.VOICEUI_ERROR} を返す.
     * @return updateAppInfoの実行結果
     */
    static public int enableScene(VoiceUIManager vm, final String scene) {
        int result = VoiceUIManager.VOICEUI_ERROR;
        // 引数チェック.
        if (vm == null || scene == null || "".equals(scene)) {
            return result;
        }
        VoiceUIVariable variable = new VoiceUIVariable(ScenarioDefinitions.TAG_SCENE, VoiceUIVariable.VariableType.STRING);
        variable.setStringValue(scene);
        variable.setExtraInfo(VoiceUIManager.SCENE_ENABLE);
        ArrayList<VoiceUIVariable> listVariables = new ArrayList<>();
        listVariables.add(variable);
        try {
            result = vm.updateAppInfo(listVariables);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed updateAppInfo.[" + e.getMessage() + "]");
        }
        return result;
    }

    /**
     * sceneを無効にする.
     * <br>
     * 指定のsceneを1つだけ無効にするのみであり、複数指定も発話指定もしない.
     *
     * @param vm VoiceUIManagerインスタンス.
     *            {@code null}の場合は {@code VoiceUIManager.VOICEUI_ERROR} を返す.
     * @param scene 有効にするscene名.
     *              {@code null}や空文字の場合は {@code VoiceUIManager.VOICEUI_ERROR} を返す.
     * @return updateAppInfoの実行結果
     */
    static public int disableScene(VoiceUIManager vm, final String scene) {
        int result = VoiceUIManager.VOICEUI_ERROR;
        // 引数チェック.
        if (vm == null || scene == null || "".equals(scene)) {
            return result;
        }
        VoiceUIVariable variable = new VoiceUIVariable(ScenarioDefinitions.TAG_SCENE, VoiceUIVariable.VariableType.STRING);
        variable.setStringValue(scene);
        variable.setExtraInfo(VoiceUIManager.SCENE_DISABLE);
        ArrayList<VoiceUIVariable> listVariables = new ArrayList<VoiceUIVariable>();
        listVariables.add(variable);
        try {
            result = vm.updateAppInfo(listVariables);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed updateAppInfo.[" + e.getMessage() + "]");
        }
        return result;
    }

    /**
     * {@link VoiceUIManager#updateAppInfo}と {@link VoiceUIManager#updateAppInfoAndSpeech} のラッパー関数
     * @param vm VoiceUIManagerインスタンス
     * @param listVariables variableリスト
     * @param speech 発話するかどうか
     * @return 関数の実行結果
     */
    static public int updateAppInfo(VoiceUIManager vm, final List<VoiceUIVariable> listVariables, final boolean speech) {
        int result = VoiceUIManager.VOICEUI_ERROR;
        // 引数チェック.
        if (vm == null || listVariables == null) {
            return result;
        }
        try {
            if (speech) {
                result = vm.updateAppInfoAndSpeech(listVariables);
            } else {
                result = vm.updateAppInfo(listVariables);
            }
        } catch (RemoteException e) {
            if (speech) {
                Log.e(TAG, "Failed updateAppInfoAndSpeech.[" + e.getMessage() + "]");
            } else {
                Log.e(TAG, "Failed updateAppInfo.[" + e.getMessage() + "]");
            }
        }
        return result;
    }

    /**
     * {@link VoiceUIManager#stopSpeech} のラッパー関数.
     * <br>
     * RemoteExceptionをthrowせずにerrorログを出力する.
     */
    static public void stopSpeech() {
        try {
            VoiceUIManager.stopSpeech();
        } catch (RemoteException e) {
            Log.e(TAG, "Failed StopSpeech.[" + e.getMessage() + "]");
        }
    }/**
     * memory_pに値を記憶する.<br>
     *
     * @param vm {@link VoiceUIManager}
     * @param key memory_pのkey名(${memory_p:key}のkey部分).
     * @param value memory_pに記憶する値.
     * @return updateAppInfoの実行結果
     * @see VoiceUIManager#updateAppInfo(List)
     */
    public static int setMemory(VoiceUIManager vm, final String key, final String value) {
        int result = VoiceUIManager.VOICEUI_ERROR;
        String name;
        if (vm == null || key == null || "".equals(key)) {
            return result;
        }else{
            name = ScenarioDefinitions.TAG_MEMORY_P + key;
        }
        VoiceUIVariable variable = new VoiceUIVariable(name, value);
        ArrayList<VoiceUIVariable> variables = new ArrayList<VoiceUIVariable>();
        variables.add(variable);
        try {
            result = vm.updateAppInfo(variables);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed updateAppInfo.[" + e.getMessage() + "]");
        }
        return result;

    }

    /**
     * memory_pの値を削除する.<br>
     * {@link VoiceUIManager#removeVariable} のラッパー関数.
     * <br>
     * @param vm {@link VoiceUIManager}
     * @param key memory_pのkey名(${memory_p:key}のkey部分).
     * @return removeVariableの実行結果
     * @see VoiceUIManager#updateAppInfo(List)
     */
    public static int clearMemory(VoiceUIManager vm, final String key) {
        int result = VoiceUIManager.VOICEUI_ERROR;
        if (vm != null) {
            try {
                result = vm.removeVariable(key);
            } catch (RemoteException e) {
                Log.e(TAG, "Failed removeVariable.[" + e.getMessage() + "]");
            }
        }
        return result;
    }

    /**
     * {@link VoiceUIManager#setAsrLanguage}のラッパー関数
     *
     * @param vm     VoiceUIManagerインスタンス
     * @param locale 指定言語種別({@link Locale})
     */
    static public void setAsr(VoiceUIManager vm, Locale locale) {
        String setLang = VoiceUIManager.LANG_JAPANESE;
        if (locale.equals(Locale.JAPAN) || locale.equals(Locale.JAPANESE)) {
            setLang = VoiceUIManager.LANG_JAPANESE;
        } else if (locale.equals(Locale.US) || locale.equals(Locale.ENGLISH) || locale.equals(Locale.UK)) {
            setLang = VoiceUIManager.LANG_ENGLISH;
        } else if (locale.equals(Locale.CHINA) || locale.equals(Locale.CHINESE) || locale.equals(Locale.SIMPLIFIED_CHINESE)) {
            setLang = VoiceUIManager.LANG_CHINESE;
        }
        try {
            vm.setAsrLanguage(setLang);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed setAsrLanguage.[" + e.getMessage() + "]");
        }
    }

    /**
     * {@link VoiceUIManager#setTtsLanguage}のラッパー関数
     *
     * @param vm     VoiceUIManagerインスタンス
     * @param locale 指定言語種別({@link Locale})
     */
    static public void setTts(VoiceUIManager vm, Locale locale) {
        String setLang = VoiceUIManager.LANG_JAPANESE;
        if (locale.equals(Locale.JAPAN) || locale.equals(Locale.JAPANESE)) {
            setLang = VoiceUIManager.LANG_JAPANESE;
        } else if (locale.equals(Locale.US) || locale.equals(Locale.ENGLISH) || locale.equals(Locale.UK)) {
            setLang = VoiceUIManager.LANG_ENGLISH;
        } else if (locale.equals(Locale.CHINA) || locale.equals(Locale.CHINESE) || locale.equals(Locale.SIMPLIFIED_CHINESE)) {
            setLang = VoiceUIManager.LANG_CHINESE;
        }
        try {
            vm.setTtsLanguage(setLang);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed setTtsLanguage.[" + e.getMessage() + "]");
        }
    }

}
