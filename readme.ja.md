# DeviceConnect Android について


Device Connect WebAPIはスマートフォン上で仮想サーバとして動作するWebAPIで、様々なウェアラブルデバイスやIoTデバイスをWebブラウザやアプリから統一的な記述で簡単に利用することができます。
Device Connect AndroidはAndroid版のDeviceConnectのプラットフォームになります。

動作確認などに関しては、[こちら](https://github.com/DeviceConnect/DeviceConnect-Docs)を参照してください。

このガイドでは以下のことについて解説していきます。

* プロジェクトの説明



# プロジェクトの説明
## dConnectDevicePlugin
| プロジェクト名|内容  |
|:-----------|:---------|
| dConnectDeviceChromeCast|Chromecastのデバイスプラグイン。 |
| dConnectDeviceHue|Hueのデバイスプラグイン。|
|dConnectDeviceIrKit|IrKitのデバイスプラグイン。|
|dConnectDevicePebble|Pebbleのデバイスプラグイン。|
|dConnectDeviceSonyCamera|QX10などのSonyCameraのデバイスプラグイン。|
|dConnectDeviceSonySW|SonySmartWatchデバイスプラグイン。※SmartWatch3は未対応。|
|dConnectDeviceSphero|Spheroのデバイスプラグイン。|
|dConnectDeviceWear|AndroidWearのデバイスプラグイン。|
|dConnectHost|Androidのデバイスプラグイン。|
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
