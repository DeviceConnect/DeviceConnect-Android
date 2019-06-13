Click [here](readme.en.md) for description of English.

# DeviceConnect-Android について
DeviceConnect-AndroidはAndroid版のDevice Connectシステムのプラットフォームになります。

ここでは、以下のことについて解説していきます。

* クイックスタート
* プロジェクトの説明
* Device Connectアプリケーションの開発
* Device Connect SDKのJavadoc出力
* ビルドマニュアル

Device Connect システムについての詳細な説明は、[こちら](https://github.com/DeviceConnect/DeviceConnect-Docs/wiki)を参照してください。

# クイックスタート
Device Connect Managerをビルドして、簡単に動作させるまでを解説します。

ここでは、以下のコマンドがインストールされていることを前提としています。

* curl
* git
* adb
* Android SDK

## Device Connect Managerのビルド
### Mac/Linux
DeviceConnect-Androidのソースコードをダウンロードしてください。

```
$ git clone https://github.com/DeviceConnect/DeviceConnect-Android.git
```

ビルド前にANDROID_HOMEの環境変数にAndroidSDKへのパスが設定されていることを確認してください。

```
$ echo 'export ANDROID_HOME=<path>' >> ~/.bash_profile
```

&lt;path&gt;には、AndroidSDKへのパスを指定してください。
<br><br>
Device Connect Managerをビルドしてapkを作成します。

```
$ cd DeviceConnect-Android/dConnectManager/dConnectManager
$ ./gradlew assembleDebug
```

Device Connect Managerを端末にインストールします。

```
$ adb install app/build/outputs/apk/app-debug.apk
```

### Windows
DeviceConnect-Androidのソースコードをダウンロードしてください。

```
> git clone https://github.com/DeviceConnect/DeviceConnect-Android.git
```

ビルド前にANDROID_HOMEの環境変数にAndroidSDKへのパスが設定されていることを確認してください。

```
> setx ANDROID_HOME <path>
```

&lt;path&gt;には、AndroidSDKへのパスを指定してください。
<br><br>
Device Connect Managerをビルドしてapkを作成します。

```
> cd DeviceConnect-Android/dConnectManager/dConnectManager
> gradlew.bat assembleDebug
```

Device Connect Managerを端末にインストールします。

```
> adb install app/build/outputs/apk/app-debug.apk
```


## Device Connect Managerの起動
Android端末のアプリケーション一覧画面から、Device Connect Managerのアイコンをタップして、起動します。
<div>
    <a href="./assets/icon.png" target="_blank">
        <img src="./assets/icon.png" border="0" width="80" alt="" />
    </a>
</div>

Device Connect Managerの画面が開いたら、メニューバーに存在するスイッチをONに変更し、Device Connect Managerを起動します。
<div>
    <a href="./assets/manager_setting_ja.png" target="_blank">
        <img src="./assets/manager_setting_ja.png" border="0" width="200" alt="" />
    </a>
</div>

画面にHostが表示されれば、起動したことが確認できます。<br>
Hostが表示されない場合には、`サービスを検索`ボタンを押下してください。

<div>
    <a href="./assets/manager_setting_ja.png" target="_blank">
        <img src="./assets/manager_device_list.png" border="0" width="200" alt="" />
    </a>
</div>

外部のPCからアクセスしたい場合には、Device Connect Managerの設定画面で`外部IPを許可`を有効にしてからDevice Connect Managerを再起動します。

IPアドレスをDevice Connect Managerの設定画面で表示されているHostの値に設定して、以下のCURLコマンドを実行します。

```
curl  -X GET \
      -H 'Origin: localhost' \
      http://192.168.xxx.xxx:4035/gotapi/availability
```

以下のようなレスポンスが返却されれば、Device Connect Managerが起動していることを確認することができます。

```
{
    "result" : 0,
    "product" : "Device Connect Manager",
    "version":"v2.1.0
}
```

# プロジェクトの説明
## デバイスプラグイン
| プロジェクト名|内容  |
|:-----------|:---------|
|[dConnectDeviceAndroidWear](dConnectDevicePlugin/dConnectDeviceAndroidWear)|AndroidWearのデバイスプラグイン。|
|[dConnectDeviceChromeCast](dConnectDevicePlugin/dConnectDeviceChromeCast)|Chromecastのデバイスプラグイン。 |
|[dConnectDeviceFaBo](dConnectDevicePlugin/dConnectDeviceFaBo)|FaBoのデバイスプラグイン。|
|[dConnectDeviceHeartRate](dConnectDevicePlugin/dConnectDeviceHeartRate)|Mio AlphaなどのHeartRateのデバイスプラグイン。|
|[dConnectDeviceHitoe](dConnectDevicePlugin/dConnectDeviceHitoe)|Hitoeのデバイスプラグイン。|
|[dConnectDeviceHOGP](dConnectDevicePlugin/dConnectDeviceHOGP)|HOGPのデバイスプラグイン。|
|[dConnectDeviceHost](dConnectDevicePlugin/dConnectDeviceHost)|Androidのデバイスプラグイン。|
|[dConnectDeviceHue](dConnectDevicePlugin/dConnectDeviceHue)|Hueのデバイスプラグイン。|
|[dConnectDeviceHVC](dConnectDevicePlugin/dConnectDeviceHVC)|HVC-Cのデバイスプラグイン。|
|[dConnectDeviceHVCC2W](dConnectDevicePlugin/dConnectDeviceHVCC2W)|HVC-C2Wのデバイスプラグイン。|
|[dConnectDeviceHVCP](dConnectDevicePlugin/dConnectDeviceHVCP)|HVC-Pのデバイスプラグイン。|
|[dConnectDeviceIRKit](dConnectDevicePlugin/dConnectDeviceIRKit)|IRKitのデバイスプラグイン。|
|[dConnectDeviceLinking](dConnectDevicePlugin/dConnectDeviceLinking)|Linkingのデバイスプラグイン。|
|[dConnectDeviceSphero](dConnectDevicePlugin/dConnectDeviceSphero)|Spheroのデバイスプラグイン。|
|[dConnectDeviceTheta](dConnectDevicePlugin/dConnectDeviceTheta)|THETAのデバイスプラグイン。|
|[dConnectDeviceUVC](dConnectDevicePlugin/dConnectDeviceUVC)|UVCカメラのデバイスプラグイン。|
|[dConnectDeviceTest](dConnectDevicePlugin/dConnectDeviceTest)|DeviceConnectのテスト用のデバイスプラグイン。|
|[dConnectDevicePluginSDK](dConnectDevicePlugin/dConnectDevicePluginSDK)|DevicePluginを作成するためのSDK。<br>dConnectSDKForAndroidが必要。|

## Device Connect Manager
| プロジェクト名|内容  |
|:-----------|:---------|
|[dConnectManager](dConnectManager/dConnectManager)| DeviceConnectのプラットフォーム本体。|
|[dConnectServer](dConnectManager/dConnectServer)|DeviceConnectのWebサーバのインターフェースを定義したライブラリ。|
|[dConnectServerNanoHttpd](dConnectManager/dConnectServerNanoHttpd)|dConnectServerのインターフェースを実装したWebサーバのライブラリ。|

## Device Connect SDK
| プロジェクト名|内容  |
|:-----------|:---------|
|[dConnectSDKForAndroid](dConnectSDK/dConnectSDKForAndroid)| DeviceConnectのSDK。DevicePlugin開発とアプリ開発用に使用するSDK。|


# Device Connectアプリケーションの開発
Android版Device Connectを使用したアプリケーション開発および、デバイスプラグイン開発に関しましては、以下のページを参考にしてください。

* [アプリケーション開発マニュアル](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/Application-Manual-for-Android-Studio)<br>
Device Connect Managerを使用したアプリケーションを開発したい場合には、こちらのアプリケーション開発マニュアルをご参照ください。

* [デバイスプラグイン開発マニュアル](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/DevicePlugin-Manual-for-Android-Studio-200)<br>
Device Connect Managerに対応したデバイスプラグインを開発したい場合には、こちらのデバイスプラグイン開発マニュアルをご参照ください。

# Device Connect SDKのJavadoc出力
## Mac/Linux

```
$ cd DeviceConnect-Android/dConnectSDK/dConnectSDKForAndroid
$ ./gradlew generateJavadocForSDK
```

## Windows

```
> cd DeviceConnect-Android/dConnectSDK/dConnectSDKForAndroid
> gradlew.bat generateJavadocForSDK
```



gradleを実行したディレクトリに`DeviceConnectSDK-Javadoc`が作成され、Device Connect SDKのJavadocが出力されます。

# Device Connect Plug-in SDKのJavadoc出力
## Mac/Linux

```
$ cd DeviceConnect-Android/dConnectDevicePlugin/dConnectDevicePluginSDK
$ ./gradlew generateJavadocForPlugin
```


## Windows

```
> cd DeviceConnect-Android/dConnectDevicePlugin/dConnectDevicePluginSDK
> gradlew.bat generateJavadocForPlugin
```



gradleを実行したディレクトリに`DevicePluginSDK-Javadoc`が作成され、Device Plugin SDKのJavadocが出力されます。

# ビルドマニュアル
Device Connect Managerや各デバイスプラグインを開発したい人は、こちらのビルド手順書に従ってビルドしてください。

* [DeviceConnectManager](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/DeviceConnectManager-Build)
* [ChromeCast](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/ChromeCast-Build)
* [FaBo](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/FaBo-Build)
* [HeartRate](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/HeartRateDevice-Build)
* [Hitoe](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/Hitoe-Build)
* [HOGP](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/HOGP-Build)
* [Host](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/Host-Build)
* [Hue](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/Hue-Build)
* [HVC](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/HVCDevice-Build)
* [HVC-C2W](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/HVCC2WDevice-Build)
* [HVC-P](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/HVCPDevice-Build)
* [IRKit](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/IRKit-Build)
* [Linking](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/Linking-Build)
* [Sphero](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/Sphero-Build)
* [Theta](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/Theta-Build)
* [UVC](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/UVC-Build)
* [AndroidWear](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/AndroidWear-Build)
