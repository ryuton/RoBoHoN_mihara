package jp.co.sharp.sample.projector.customize;

/**
 * シナリオファイルで使用する定数の定義クラス.<br>
 * <p/>
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
     * Package名.
     */
    protected static final String PACKAGE = "jp.co.sharp.sample.projector";
    /**
     * シナリオ共通: controlタグで指定するターゲット名.
     */
    public static final String TARGET = PACKAGE;
    /**
     * scene名: アプリ共通シーン
     */
    public static final String SCENE_COMMON = PACKAGE + ".scene_common";
    /**
     * function：アプリ終了を通知する.
     */
    public static final String FUNC_END_APP = "end_app";
    /**
     * function：プロジェクタ起動を通知する.
     */
    public static final String FUNC_START_PROJECTOR = "start_projector";
    /**
     * accost名：アプリ終了発話実行.
     */
    public static final String ACC_END_APP = ScenarioDefinitions.PACKAGE + ".app_end.execute";
    //static クラスとして使用する.
    private ScenarioDefinitions() {
    }
}
