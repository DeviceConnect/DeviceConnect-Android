# DeviceConnect Android について


Device Connect WebAPIはスマートフォン上で仮想サーバとして動作するWebAPIで、様々なウェアラブルデバイスやIoTデバイスをWebブラウザやアプリから統一的な記述で簡単に利用することができます。
Device Connect AndroidはAndroid版のDeviceConnectのプラットフォームになります。

このガイドでは以下のことについて解説していきます。

* [プロジェクトの説明](#section1)
* [プロジェクトのインストール](#section2)
* [DeviceConnectManagerの起動](#section3)
* [動作確認](#section4)
* [DeviceConnectアプリの開発](#section5)



# <a name="section1">プロジェクトの説明</a>
## dConnectDevicePlugin
| プロジェクト名|内容  |
|:-----------|:---------|
| dConnectDeviceChromeCast|Chromecastのデバイスプラグイン。 |
| dConnectDeviceHue|Hueのデバイスプラグイン。|
|dConnectDeviceIRKit|IrKitのデバイスプラグイン。|
|dConnectDevicePebble|Pebbleのデバイスプラグイン。|
|dConnectDeviceSonyCamera|QX10などのSonyCameraのデバイスプラグイン。|
|dConnectDeviceSonySW|SonySmartWatchデバイスプラグイン。<br>※SmartWatch3は未対応。|
|dConnectDeviceSphero|Spheroのデバイスプラグイン。|
|dConnectDeviceWear|AndroidWearのデバイスプラグイン。|
|dConnectDeviceHost|Androidのデバイスプラグイン。|
|dConnectDeviceTest|DeviceConnectのテスト用のデバイスプラグイン。|
|dConnectDevicePluginSDK|DevicePluginを作成するためのSDK。dConnectSDKForAndroidが必要。|


## dConnectManager
| プロジェクト名|内容  |
|:-----------|:---------|
|dConnectManager| DeviceConnectのプラットフォーム本体。|
|dConnectManagerTest|DeviceConnectManagerのテスト。|
|dConnectServer|DeviceConnectのServerのインターフェースライブラリ。|
|dConnectServerNanoHttpd|DeviceConnectのServerの本体。|

## dConnectSDK
| プロジェクト名|内容  |
|:-----------|:---------|
|dConnectApp| DeviceConnectの動作確認用アプリ。|
|dConnectSDKForAndroid| DeviceConnectのSDK。DevicePlugin開発とアプリ開発用に使用するSDK。|


# <a name="section2"> アプリケーションのインストール</a>
　DeviceConnectManagerやデバイスプラグインを開発したい人は、こちらのビルド手順書に従ってビルドしてください。

* [DeviceConnectManager](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/2.1.DeviceConnectManager)
* [ChromeCast](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/2.2.ChromeCast)
* [Host](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/2.3.Host)
* [Hue](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/2.4.Hue)
* [IRKit](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/2.5.IRKit)
* [Pebble](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/2.6.Pebble)
* [SonyCamera](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/2.7.SonyCamera)
* [SonySW](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/2.8.SonySW)
* [Sphero](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/2.9.Sphero)
* [AndroidWear](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/2.10.AndroidWear)

　なお、[こちら](https://github.com/DeviceConnect/DeviceConnect-Docs/tree/master/Bin/Android)からもapkを取得することができます。
　インストール方法につきましては、以下のようにコマンドラインからコマンドの実行することができます。<br>
 ここでは、Device Manager本体とHOSTデバイスプラグインのみをインストールします。
他にもデバイスプラグインがありますので、必要に応じてインストールしてください。

```
　adb install dConnectManager.apk
　adb install dConnectDeviceHost.apk
```

adbコマンドにつきましては、Android SDKを確認してください。

　

# <a name="section3">DeviceConnectManagerの起動</a>
 DeviceConnectManagerのアイコンをタップしてください。
 <center><a href="https://raw.githubusercontent.com/wiki/DeviceConnect/DeviceConnect-Android/DevicePluginManual/image35.png" target="_blank">
<img src="https://raw.githubusercontent.com/wiki/DeviceConnect/DeviceConnect-Android/DevicePluginManual/image35.png" border="0"
  width="151" height="212" alt="" /></a><br></center>

DeviceConnectManagerの欄をONにすることによって、起動することができます。

<center><a href="https://raw.githubusercontent.com/wiki/DeviceConnect/DeviceConnect-Android/DevicePluginManual/image39.png" target="_blank">
<img src="https://raw.githubusercontent.com/wiki/DeviceConnect/DeviceConnect-Android/DevicePluginManual/image39.png" border="0"
 width="320" height="550" alt="" /></a></center>
 
 
 
# <a name="section4">動作確認</a>

 Chromeブラウザのアドレスに、「http://localhost:4035/gotapi/availability」を入力してください。
以下のようなJSONのレスポンスが返って来れば、DeviceConnectが動作していることが確認できます。<br>
<br>
 
 <center><a href="https://raw.githubusercontent.com/wiki/DeviceConnect/DeviceConnect-Android/imageX.PNG" target="_blank">
<img src="https://raw.githubusercontent.com/wiki/DeviceConnect/DeviceConnect-Android/imageX.PNG" border="0"
 width="320" height="550" alt="" /></a></center>
 
 
 リクエスト
 
 ```
 GET http://localhost:4035/gotapi/availability
 ```
 
 レスポンス
 
 ```
 {
     "product":"Device Connect Manager",
     "version":"x.x",
     "result":0,
}
 ```
 
 
 
 availability以外のAPIには、基本的にはアクセストークンが必要になるためにchromeブラウザのアドレスでは簡単に確認することができません。
Device Connect の具体的な利用法に関しましては、[こちら](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/1.1.1.Application-Manual)のWikiをご参考にしてください。



# <a name="section5">DeviceConnectアプリの開発</a>
 DeviceConnectを使ったアプリケーションおよび、アプリケーションの開発に関しましては、以下のページを参考にしてください。
 
 * [アプリケーション開発マニュアル(Eclipse)](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/1.1.1.Application-Manual)
 * [アプリケーション開発マニュアル(AndroidStudio)](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/1.2.1.Application-Manual)

<br>
Device Connect Managerを使用したデバイスプラグインを開発したい場合には、こちらのアプリケーション開発マニュアルをご参照ください。
 
 * [デバイスプラグイン開発マニュアル(Eclipse)](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/1.1.2.DevicePlugin-Manual)
 * [デバイスプラグイン開発マニュアル(AndroidStudio)](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/1.2.2.DevicePlugin-Manual)<br>

