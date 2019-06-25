■■■■■■■■■■■■■
■ 各プロジェクトの概要 ■
■■■■■■■■■■■■■

SampleSimple ----------- 音声UIを使った基本的な機能だけを実装したサンプルアプリ
SampleScenario --------- シナリオで使える変数やタグなどを利用したサンプルアプリ
SampleProjector -------- ロボホンで追加されている機能（プロジェクター）を利用したサンプルアプリ
SampleAddressBook ------ ロボホンで追加されている機能（電話帳）を利用したサンプルアプリ
SampleCamera ----------- ロボホンで追加されている機能（カメラ）を利用したサンプルアプリ
SampleDance ------------ ロボホンで追加されている機能（ダンス）を利用したサンプルアプリ
SampleMultilingual ----- 音声UIを使った多言語対応を実装したサンプルアプリ
SampleMessage ---------- ロボホンのメッセージアプリからメール送信するサンプルアプリ
SampleAction ----------- ロボホンで追加されている機能（アクション）を利用したサンプルアプリ
SampleSong ------------- ロボホンで追加されている機能（歌）を利用したサンプルアプリ



■■■■■■■
■ 動作環境 ■
■■■■■■■

各サンプルプロジェクトは、以下の環境で動作することを確認しています。
AndroidStudioにImportしてご確認ください。

Windows 7/8.1
Mac OS X 10.11.1/10.11.6(El Capitan)
JDK 7/8
Android Studio 2.1.2/2.2.3/2.3.1/3.0/3.1


■■■■■■■■■■■■
■ ファイル構成の概要 ■
■■■■■■■■■■■■

SampleSimple
 │  
 └app
    ├─graphml ・・・ オーサリングツールで作成したファイル
    │      jp_co_sharp_sample_simple_accost.graphml
    │      jp_co_sharp_sample_simple_app_end.graphml
    │      jp_co_sharp_sample_simple_get_memoryp.graphml
    │      jp_co_sharp_sample_simple_home.graphml
    │      jp_co_sharp_sample_simple_main.graphml
    │      jp_co_sharp_sample_simple_variable.graphml
    │      
    ├─jar ・・・ 音声UIのjarファイル
    │      jp.co.sharp.android.voiceui.framework.jar
    │      
    └─src
        └─main
            │  AndroidManifest.xml
            │  
            ├─assets
            │  ├─home ・・・ HOME用シナリオファイル
            │  │      jp_co_sharp_sample_simple_home.hvml
            │  │      
            │  └─other ・・・ HOME用以外のシナリオファイル
            │          jp_co_sharp_sample_simple_accost.hvml
            │          jp_co_sharp_sample_simple_app_end.hvml
            │          jp_co_sharp_sample_simple_get_memoryp.hvml
            │          jp_co_sharp_sample_simple_talk.hvml
            │          jp_co_sharp_sample_simple_variable.hvml
            │          
            ├─java ・・・ javaソースファイル
            │
            └─res ・・・ リソースファイル



■■■■■■■■■■■■■
■ 著作権および免責事項 ■
■■■■■■■■■■■■■

本サンプルアプリの著作権はシャープ株式会社(以下、当社)が保有しています。
本サンプルアプリの内容物はロボホンアプリの開発に利用する目的に限り、流用、改変が可能です。
本サンプルアプリの内容物を利用したことによって生じたすべての障害・損害・不具合等に関して、当社は一切責任を負いません。
本サンプルアプリの内容物は事前の通知なく全部又は一部を変更する可能性があります。


■■■■■■■■■■■■■
■ 更新履歴　　　　　　 ■
■■■■■■■■■■■■■
1.0.0	2016/9/15	初版
1.0.1	2016/11/11	プロジェクタサンプルの修正、他軽微な修正
1.1.0	2017/2/17	カメラ/ダンスサンプルの追加
1.2.0	2017/5/24	多言語対応サンプルの追加、各サンプルのjarファイルおよび共通コード更新
　　　　　　　　　　シナリオ登録時にファイル命名規則のチェック処理の追加
1.3.0	2017/12/13	メッセージサンプルの追加、ダンスサンプルの修正
1.4.0	2018/5/31	アクションサンプルの追加、歌サンプルの修正、ダンスサンプルの修正、多言語対応サンプルの修正
