package jp.co.sharp.sample.addressbook.customize;

/**
 * シナリオファイルで使用する定数の定義クラス.<br>
 *
 * <p>
 * producerタグとcontrolタグのtargetにはPackage名を設定すること<br>
 * scene、memory_p(長期記憶の変数名)、resolve variable(アプリ変数解決の変数名)、accostのwordはPackage名を含むこと<br>
 * </p>
 */
public class ScenarioDefinitions {

    //static クラスとして使用する.
    private ScenarioDefinitions(){}

    /** sceneタグを指定する文字列 */
    public static final String TAG_SCENE = "scene";
    /** accostタグを指定する文字列 */
    public static final String TAG_ACCOST = "accost";
    /** target属性を指定する文字列 */
    public static final String ATTR_TARGET = "target";
    /** function属性を指定する文字列 */
    public static final String ATTR_FUNCTION = "function";
    /** memory_pを指定するタグ */
    public static final String TAG_MEMORY_PERMANENT = "memory_p:";

    /** Package名. */
    protected static final String PACKAGE = "jp.co.sharp.sample.addressbook";
    /** シナリオ共通:プロデューサー名. */
    protected static final String PRODUCER = PACKAGE;
    /** シナリオ共通:controlタグで指定するターゲット名. */
    public static final String TARGET = PACKAGE;

    /** scene名. */
    public static final String SCENE = "jp.co.sharp.sample.addressbook";

}
