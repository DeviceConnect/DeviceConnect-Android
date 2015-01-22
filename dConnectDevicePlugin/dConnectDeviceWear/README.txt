AndroidWear Device Plugin
----------------------------------------------------------------------------
本プロジェクトをビルドするためには、下記の準備が必要です。

1. google-play-services_lib 
2. dConnectSDKAndroid
3. dConnectDevicePluginSDK

Project → Properties → Android → Library に参照を追加します。
  
dConnectDevicePluginSDK
  - dConnectSDKAndroid

dConnectDeviceChromeCast
  - dConnectDevicePluginSDK
  - google-play-services_lib

以上