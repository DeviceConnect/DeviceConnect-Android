# dConnectDemoLib

プラグインのデモアプリを Android 端末上にインストールするための機能を提供する Android ライブラリです。

## 用語説明
|用語|説明|
|:--|:--|
|プラグイン| Android 版 Device Connect Manager のデバイスプラグイン。|
|デモアプリ|プラグインの提供する REST API を利用したHTMLアプリケーション。プラグインの APK に zip として格納されます。|
|インストール|Android 端末のストレージ上に、デモアプリの zip を解凍すること。|
|ショートカット|Android 端末のホーム画面から Web ブラウザでデモアプリを表示するためのショートカットアイコン。|

## ライブラリのインポート
プラグインを実装するモジュールの `build.gradle` でライブラリをインポートする設定を行います。

`repositories` に dConnectDemoLib の maven リポジトリを追加してください。

``` gradle
repositories {
    maven { url 'https://raw.githubusercontent.com/DeviceConnect/DeviceConnect-Android/master/dConnectSDK/dConnectDemoLib/repository/' }
    ...
}
```

`dependencies` に dConnectDemoLib への依存関係を追加してください。

``` gradle
dependencies {
    implementation 'org.deviceconnect:dconnect-demo-lib:1.0.0'
    ...
}
```

## プラグインのビルド設定
プラグインのデモアプリは、プラグインのビルド時にzipに圧縮し、APKに格納します。以下、そのための設定を行います。

プラグインを実装するモジュールの `build.gradle` に、下記の定数を追加してください。

``` gradle
    def DEMO_ZIP_NAME = "demo.zip"

    defaultConfig {
        ...

        buildConfigField "String", "PACKAGE_NAME", "\"<プラグインのパッケージ名>\""
        buildConfigField "String", "DEMO_DIR", "\"demo\""
        buildConfigField "String", "DEMO_ZIP", "\"" + DEMO_ZIP_NAME + "\""
    }
```

さらに、`build.gradle` の末尾に下記の記述を追加してください。

``` gradle
task zipDemo(type:Zip) {
    File demoDir = new File(projectDir, '../demo')
    File assetsDir = new File(projectDir, 'src/main/assets')

    from demoDir
    destinationDir assetsDir
    archiveName = DEMO_ZIP_NAME
}

tasks.preBuild.dependsOn(zipDemo)
```

## パーミッションの宣言
dConnectDemoLib ではデモアプリへのショートカットアイコンをホーム画面上に作成する UI を提供しますが、そのためにはパーミッションが必要です。

以下のパーミッションを、プラグインの `AndroidManifest.xml` に追加してください。

``` xml
    <uses-permission android:name="com.android.launcher.permission.INSTALL_SHORTCUT"/>
```

## デモアプリの配置
プラグインのプロジェクト直下に `demo` フォルダを作成し、そこにデモアプリのファイル一式をコピーしてください。

Host プラグインでは以下のように配置しています。

```
dConnectDeviceHost/
    app/
    demo/    <---- デモアプリ用のフォルダ
        camera/
            index.html
            ...
```

## インストーラーの組み込み
以下、プラグイン側の実装について説明します。

ここでは、デモアプリのインストーラーをプラグイン側に組み込みます。

### DemoInstaller の拡張
デモアプリのインストールを管理する機能は以下のクラスで既に実装されています。

```
org.deviceconnect.android.deviceplugin.demo.DemoInstaller
```

この `DemoInstaller` を拡張して、`build.gradle`で定義した定数を基底クラスのコンストラクタに指定します。

- BuildConfig.PACKAGE_NAME
- BuildConfig.DEMO_DIR
- BuildConfig.DEMO_ZIP

例えば、Host プラグインでは `DemoInstaller ` を  `HostDemoInstaller ` として拡張しています。

``` java
package org.deviceconnect.android.deviceplugin.host.demo;

import android.content.Context;
import org.deviceconnect.android.deviceplugin.demo.DemoInstaller;
import org.deviceconnect.android.deviceplugin.host.BuildConfig;

public class HostDemoInstaller extends DemoInstaller {

    public HostDemoInstaller(final Context context) {
        super(context, BuildConfig.PACKAGE_NAME, BuildConfig.DEMO_DIR, BuildConfig.DEMO_ZIP);
    }
}
```

### 初期化・自動更新

プラグイン起動時に、インストーラーの初期化と、デモアプリのバージョンをチェックします。バージョンが上がっていると判断した場合、自動更新を実行します。自動更新の結果は、端末の通知バーに表示します。

例えば、Host プラグインの本体 `HostDevicePlugin` では、以下のように実装しています。

プラグイン起動時に初期化と自動更新を行っています。

``` java
    // インストーラーの初期化
    mDemoInstaller = new HostDemoInstaller(getContext());
    
    // 自動更新通知の初期化
    mDemoNotification = new DemoInstaller.Notification(
            1,
            getContext().getString(R.string.app_name_host),
            R.drawable.dconnect_icon,
            "org.deviceconnect.android.deviceconnect.host.channel.demo",
            "Host Plugin Demo Page",
            "Host Plugin Demo Page"
    );
    
    // 自動更新通知の登録
    registerDemoNotification();
    
    // 自動更新の開始
    updateDemoPageIfNeeded();
    
```

自動更新通知の登録は、以下のように実装します。

``` java
    private void registerDemoNotification() {
        IntentFilter filter  = new IntentFilter();
        filter.addAction(DemoInstaller.Notification.ACTON_CONFIRM_NEW_DEMO);
        filter.addAction(DemoInstaller.Notification.ACTON_UPDATE_DEMO);
        getContext().registerReceiver(mDemoNotificationReceiver, filter);
    }
```

自動更新は、以下のように実装します。

``` java
    private void updateDemoPageIfNeeded() {
        final Context context = getContext();
        if (mDemoInstaller.isUpdateNeeded()) {
            updateDemoPage(context);
        }
    }
    
    private void updateDemoPage(final Context context) {
        mDemoInstaller.update(new DemoInstaller.UpdateCallback() {
            @Override
            public void onBeforeUpdate(final File demoDir) {
                // 自動更新を実行する直前
            }

            @Override
            public void onAfterUpdate(final File demoDir) {
                // 自動更新に成功した直後
                mDemoNotification.showUpdateSuccess(context);
            }

            @Override
            public void onFileError(final IOException e) {
               // 自動更新時にファイルアクセスエラーが発生した場合
               mDemoNotification.showUpdateError(context);
            }

            @Override
            public void onUnexpectedError(final Throwable e) {
                // 自動更新時に不明なエラーが発生した場合
                mDemoNotification.showUpdateError(context);
            }
        }, new Handler(Looper.getMainLooper()));
    }
```

## ContentProvider の定義
Android 10 以降、ファイルの保存場所はプライベートな領域に限られます。デモアプリを外部に公開するためには、ContentProvider が必要になります。

以下のように、AndroidManifest.xml に `<provider>` の定義を追加してください。

``` xml
        <provider
            android:name="org.deviceconnect.android.provider.FileProvider"
            android:authorities="<プラグインのパッケージ名>.provider"
            android:exported="true">
            <meta-data
                android:name="filelocation"
                android:resource="@xml/filelocation" />
        </provider>
```

また、プラグインの `res/xml` フォルダに、以下の内容の `filelocation.xml` を追加してください。

``` xml
<?xml version="1.0" encoding="UTF-8"?>
<file-locations>
    <external-location path="" />
</file-locations>
``` 

## インストール用画面の組み込み
デモアプリを端末にインストール・アンインストールするためのUIは、以下の `Fragment` クラスで提供されます。

```
org.deviceconnect.android.deviceplugin.demo.DemoSettingFragment
```
以下、DemoSettingFragment の拡張クラスで実装すべきメソッドについて説明します。

なお、例示しているソースコードは、Host プラグインの実装から抜粋したものです。

### createDemoInstaller
`DemoInstaller` の実装を返します。

``` java
    @Override
    protected DemoInstaller createDemoInstaller(final Context context) {
        return new HostDemoInstaller(context);
    }
```

### getDemoDescription
インストール画面に表示する説明文を返します。

``` java
    @Override
    protected String getDemoDescription(DemoInstaller demoInstaller) {
        return getString(R.string.demo_page_description);
    }
```
### getShortcutUri
デモアプリをインテント経由で表示するための URI を返します。

``` java
    private static final String FILE_PROVIDER_AUTHORITY = "<Manifestで定義したFilePrpviderのauthorities名>";

    @Override
    protected String getShortcutUri(final DemoInstaller installer) {
        String rootPath;
        String filePath = "/demo/<トップページのhtmlファイルへの相対パス>";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            rootPath = "/" + FILE_PROVIDER_AUTHORITY;
        } else {
            rootPath = "/" + installer.getPluginPackageName();
        }
        return "gotapi://shortcut" + rootPath + filePath;
    }
```

### getShortcutIconResource
デモアプリのショートカットアイコンの画像リソースを返します。

``` java
    @Override
    protected int getShortcutIconResource(final DemoInstaller installer) {
        return R.drawable.dconnect_icon;
    }
```

### getShortcutShortLabel
デモアプリのショートカットにつけるショートラベル名を返します。
端末のホーム画面上で、アイコンの下に表示されます。

``` java
    @Override
    protected String getShortcutShortLabel(final DemoInstaller installer) {
        return getString(R.string.demo_page_shortcut_label);
    }
```

### getShortcutLongLabel
デモアプリのショートカットにつけるロングラベル名を返します。
基本的には、ショートラベル名と同じで問題ありません。

``` java
    @Override
    protected String getShortcutLongLabel(final DemoInstaller installer) {
        return getString(R.string.demo_page_shortcut_label);
    }
```

### getMainActivity
プラグインで定義されている画面のうち、主に使われる画面のコンポーネント名を返します。

例えば、`SettingActivity` というクラスで設定画面を作成している場合は、以下のようにコンポーネント名を返してください。

``` java
    @Override
    protected ComponentName getMainActivity(final Context context) {
        return new ComponentName(context, SettingActivity.class);
    }
```

これは、Android フレームワークのショートカット API でエラーが出ないようにするための措置です。（よって、画面を開くために使われるものではありません）

なお、Host プラグインのように、Device Connect Manager に直接組み込むプラグインの場合は、以下のように `null` を返してください。

``` java
    @Override
    protected ComponentName getMainActivity(final Context context) {
        return null;
    }
```

### onInstall
ユーザーがインストール画面画面上で「インストール」ボタンを押したタイミングで呼ばれます。

ファイルの書き込みを行うため、まず `WRITE_EXTERNAL_STORAGE` パーミッションを確認します。すでに許可されている、または許可ダイアログで許可された場合は、onSuccess() が呼ばれます。そこで、`install` メソッドを実行してください。


``` java
    @Override
    protected void onInstall(final Context context, final boolean createsShortcut) {
        requestPermission(context, new PermissionUtility.PermissionRequestCallback() {
            @Override
            public void onSuccess() {
                install(createsShortcut);
            }

            @Override
            public void onFail(final @NonNull String deniedPermission) {
                showInstallErrorDialog("Denied permission: " + deniedPermission);
            }
        });
    }
    
    private static final String[] PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    
    private void requestPermission(final Context context, final PermissionUtility.PermissionRequestCallback callback) {
        PermissionUtility.requestPermissions(context, getMainHandler(), PERMISSIONS, callback);
    }
```

### onOverwrite
ユーザーがインストール画面画面上で「上書き」ボタンを押したタイミングで呼ばれます。

onIntall の場合と同様、ファイル操作が許可されている場合に、`overwrite` メソッドを実行してください。

``` java
    @Override
    protected void onOverwrite(final Context context) {
        requestPermission(context, new PermissionUtility.PermissionRequestCallback() {
            @Override
            public void onSuccess() {
                overwrite();
            }

            @Override
            public void onFail(final @NonNull String deniedPermission) {
                showOverwriteErrorDialog("Denied permission: " + deniedPermission);
            }
        });
    }
```

### onUninstall
ユーザーがインストール画面画面上で「削除」ボタンを押したタイミングで呼ばれます。

onIntall の場合と同様、ファイル操作が許可されている場合に、`uninstall` メソッドを実行してください。

``` java
    @Override
    protected void onUninstall(final Context context) {
        requestPermission(context, new PermissionUtility.PermissionRequestCallback() {
            @Override
            public void onSuccess() {
                uninstall();
            }

            @Override
            public void onFail(final @NonNull String deniedPermission) {
                showUninstallErrorDialog("Denied permission: " + deniedPermission);
            }
        });
    }
```