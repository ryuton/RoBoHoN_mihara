package jp.co.sharp.sample.scenario.customize;

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
    protected static final String PACKAGE = "jp.co.sharp.sample.scenario";
    /**
     * シナリオ共通:controlタグで指定するターゲット名.
     */
    public static final String TARGET = PACKAGE;
    /**
     * scene名: アプリ共通シーン
     */
    public static final String SCENE_COMMON = PACKAGE + ".scene_common";
    /**
     * function：アプリ終了を通知する.
     * */
    public static final String FUNC_END_APP = "end_app";
    /**
     * function：トースト表示を通知する.
     * */
    public static final String FUNC_TEST_CONTROL = "Test_Control";
    /**
     * data key：トースト表示内容
     * */
    public static final String KEY_TOAST_CONTENTS = "Toast_Contents";
    /**
     * accost名：アプリ終了発話実行.
     * */
    public static final String ACC_END_APP =  ScenarioDefinitions.PACKAGE + ".app_end.t2";
    /**
     * accost名：YesNo.聞き返し実行
     * */
    public static final String ACC_YESNO =  ScenarioDefinitions.PACKAGE + ".yesno.t1";
    /**
     * accost名：テストrule実行.
     * */
    public static final String ACC_TAG_RULE =  ScenarioDefinitions.PACKAGE + ".tag_rule.t1";
    /**
     * accost名：テストa実行.
     * */
    public static final String ACC_TAG_A =  ScenarioDefinitions.PACKAGE + ".tag_a.t1";
    /**
     * accost名：テストnext実行.
     * */
    public static final String ACC_TAG_NEXT =  ScenarioDefinitions.PACKAGE + ".tag_next.t1";
    /**
     * accost名：テストspeech実行.
     * */
    public static final String ACC_TAG_SPEECH =  ScenarioDefinitions.PACKAGE + ".tag_speech.t1";
    /**
     * accost名：テストmemory実行.
     * */
    public static final String ACC_TAG_MEMORY =  ScenarioDefinitions.PACKAGE + ".tag_memory.t1";
    /**
     * accost名：テストcontrol実行.
     * */
    public static final String ACC_TAG_CONTROL =  ScenarioDefinitions.PACKAGE + ".tag_control.t1";
    /**
     * accost名：テストbehavior実行.
     * */
    public static final String ACC_TAG_BEHAVIOR =  ScenarioDefinitions.PACKAGE + ".tag_behavior.t1";
    /**
     * accost名：テスト演算子実行.
     * */
    public static final String ACC_OPERATOR =  ScenarioDefinitions.PACKAGE + ".operator.t1";
    /**
     * accost名：テスト変数実行.
     * */
    public static final String ACC_VARIABLE =  ScenarioDefinitions.PACKAGE + ".variable.t1";
    /**
     * accost名：テストname_for_speech実行.
     * */
    public static final String ACC_NAME_FOR_SPEECH =  ScenarioDefinitions.PACKAGE + ".name_for_speech.t1";
    /**
     * static クラスとして使用する.
     */
    private ScenarioDefinitions() {
    }
}
