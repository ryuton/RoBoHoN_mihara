package jp.co.sharp.sample.multilingual.customize;

/**
 * シナリオファイルで使用する定数の定義クラス.<br>
 * <p>
 * <p>
 * controlタグのtargetにはPackage名を設定すること<br>
 * scene、memory_p(長期記憶の変数名)、resolve variable(アプリ変数解決の変数名)、accostのwordはPackage名を含むこと<br>
 * </p>
 */
public class ScenarioDefinitions {

    /**
     * sceneタグを指定する文字列
     */
    public static final String TAG_SCENE = "scene";
    /**
     * accostタグを指定する文字列
     */
    public static final String TAG_ACCOST = "accost";
    /**
     * target属性を指定する文字列
     */
    public static final String ATTR_TARGET = "target";
    /**
     * function属性を指定する文字列
     */
    public static final String ATTR_FUNCTION = "function";
    /**
     * memory_pを指定するタグ
     */
    public static final String TAG_MEMORY_PERMANENT = "memory_p:";
    /**
     * function：発話内容を通知する.
     */
    public static final String FUNC_RECOG_TALK = "recog_talk";
    /**
     * data key：大語彙認識文言.
     */
    public static final String KEY_LVCSR_BASIC = "Lvcsr_Basic";
    /**
     * Package名.
     */
    protected static final String PACKAGE = "jp.co.sharp.sample.multilingual";
    /**
     * シナリオ共通: controlタグで指定するターゲット名.
     */
    public static final String TARGET = PACKAGE;
    /**
     * scene名: アプリ共通シーン
     */
    public static final String SCENE_COMMON = PACKAGE + ".scene_common";
    /**
     * accost名：発話実行.
     */
    public static final String ACC_HELLO = ScenarioDefinitions.PACKAGE + ".accost.hello";
    /**
     * static クラスとして使用する.
     */
    private ScenarioDefinitions() {
    }
}
