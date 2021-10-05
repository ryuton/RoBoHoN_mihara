package com.example.robohonreception.voiceui;

/**
 * シナリオファイルで使用する定数の定義クラス.<br>
 * <p/>
 * <p>
 * scene、memory_p(長期記憶の変数名)、resolve variable(アプリ変数解決の変数名)、accostのwordはPackage名を含むこと<br>
 * </p>
 */
public class ScenarioDefinitions {

    //static クラスとして使用する.
    private ScenarioDefinitions() {
    }

    /****************** 共通の定義 *******************/
    /**
     * sceneタグを指定する文字列
     */
    public static final String TAG_SCENE = "scene";
    /**
     * accostタグを指定する文字列
     */
    public static final String TAG_ACCOST = "accost";
    /**
     * memory_pを指定するタグ
     */
    public static final String TAG_MEMORY_P = "memory_p:";
    /**
     * target属性を指定する文字列
     */
    public static final String ATTR_TARGET = "target";
    /**
     * function属性を指定する文字列
     */
    public static final String ATTR_FUNCTION = "function";

    /****************** アプリ固有の定義 *******************/
    /**
     * Package名.
     */
    protected static final String PACKAGE = "com.example.robohonreception";
    /**
     * controlタグで指定するターゲット名.
     */
    public static final String TARGET = PACKAGE;
    /**
     * scene名: アプリ共通のシーン
     */
    public static final String SCENE_COMMON = PACKAGE + ".scene_common";
    /**
     * function名：アプリ終了.
     */
    public static final String FUNC_END_APP = "end_app";
    /**
     * function：発話内容を通知する.
     */
    public static final String FUNC_RECOG_TALK = "recog_talk";
    /**
     * function：発話内容を通知する.
     */
    public static final String FUNC_HVML_ACTION = "hvml_action";

    /**
     * function：発話内容を通知する.
     */
    public static final String FUNC_CALLL_ACTION = "call_action";
    /**
     * accost名：accostテスト発話実行.
     */
    public static final String ACC_ACCOST =  ScenarioDefinitions.PACKAGE + ".accost.t1";
    /**
     * accost名：resolveテスト発話実行.
     */
    public static final String ACC_RESOLVE =  ScenarioDefinitions.PACKAGE + ".variable.t1";
    /**
     * accost名：get_memorypの発話実行.
     */
    public static final String ACC_GET_MEMORYP =  ScenarioDefinitions.PACKAGE + ".get_memoryp.t1";
    /**
     * accost名：アプリ終了発話実行.
     */
    public static final String ACC_END_APP = ScenarioDefinitions.PACKAGE + ".app_end.t2";
    /**
     * accost名：発話実行.
     */
    public static final String ACC_TALK = ScenarioDefinitions.PACKAGE + ".talk.s0";
    public static final String ACC_END_TALK = ScenarioDefinitions.PACKAGE + ".talk.e0";
    public static final String ACC_TALK_FAILED = ScenarioDefinitions.PACKAGE + ".talk.t4";
    public static final String ACC_APPOINT = ScenarioDefinitions.PACKAGE + ".talk.t12";
    public static final String ACC_WAIT = ScenarioDefinitions.PACKAGE + ".talk.t16";
    /**
     * resolve variable：アプリで変数解決する値.
     */
    public static final String RESOLVE_JAVA_VALUE = ScenarioDefinitions.PACKAGE + ":java_side_value";
    /**
     * data key：シナリオ起動時情報1.
     */
    public static final String KEY_TEST_1 = "key_test1";
    /**
     * data key：シナリオ起動時情報2.
     */
    public static final String KEY_TEST_2 = "key_test2";
    /**
     * data key：大語彙認識文言.
     */
    public static final String KEY_LVCSR_BASIC = "Lvcsr_Basic";
    public static final String KEY_LVCSR_BASIC_CHECK = "Lvcsr_Basic_check";
    /**
     * data key：HVML Name.
     */
    public static final String KEY_HVML_ACTION = "topic_id";
    /**
     * data key：HVML Name.
     */
    public static final String KEY_SEARCH_APPOINT = "search_appoint";
    /**
     * data key：call action
     */
    public static final String KEY_CALL_STAFF_ACTION = "call_staff";
    /**
     * data key：call action
     */
    public static final String KEY_CALL_DIRECTOR_ACTION = "call_director";
    /**
     * memory_p：時.
     */
    public static final String MEM_P_HOUR = ScenarioDefinitions.PACKAGE + ".hour";
    /**
     * memory_p：分.
     * */
    public static final String MEM_P_MINUTE = ScenarioDefinitions.PACKAGE + ".minute";
    /**
     * memory_p：患者の名前.
     * */
    public static final String MEM_P_PATIENT_ID = ScenarioDefinitions.PACKAGE + ".patient_id";
    /**
     * memory_p：患者の名前.
     * */
    public static final String MEM_P_PATIENT_NAME = ScenarioDefinitions.PACKAGE + ".patient_name";
    /**
     * memory_p：予約時間.
     * */
    public static final String MEM_P_APPOINT_TIME = ScenarioDefinitions.PACKAGE + ".appoint_time";
    /**
     * memory_p：予約時間.
     * */
    public static final String MEM_P_APPOINT_MINUTE = ScenarioDefinitions.PACKAGE + ".appoint_minute";


    /**
     * static クラスとして使用する.
     */

}
