# dConnectLibStreaming

dConnectLibStreaming は、映像配信などを行うためのライブラリになります。

## 各モジュールの説明

|モジュール|備考|
|:--|:--|
|libmedia|映像・音声を配信する機能を提供するモジュール。|
|libopus|opus エンコードとデコードの機能を提供するモジュール。|
|libsrt|SRT 配信用サーバとプレイヤーの機能を提供するモジュール。libmedia に依存します。|
|rtsp-player-app|RTSP 確認用プレイヤーのアプリ。|
|rtsp-server-app|RTSP 確認用サーバのアプリ。|
|srt-player-app|SRT 確認用プレイヤーのアプリ。|
|srt-server-app|SRT 確認用サーバのアプリ。|


## フォルダ構成

```
/dConnectLibStream
   ├─ /build
   │   └─ /outputs
   │       ├─ libmedia-{build-type}-{version}.aar
   │       └─ libsrt-{build-type}-{version}.aar
   ├─ /libmedia
   ├─ /libopus
   ├─ /libsrt
   ├─ /rtsp-player-app
   ├─ /rtsp-server-app
   ├─ /srt-player-app
   ├─ /srt-server-app
   └─ README.md
```

# インストール方法
libmedia と libsrt を AndroidStudio プロジェクトにインストールする方法を説明します。

まず、libmedia と libsrt の aar を下記のページからダウンロードします。<br>
[https://github.com/DeviceConnect/DeviceConnect-Android/releases](https://github.com/DeviceConnect/DeviceConnect-Android/releases)

以下のように、aar をプロジェクト直下の `libs` フォルダにコピーします。

```
/YourProject
   ├── /libs
   │    ├─ libmedia-release-{version}.aar
   │    └─ libsrt-release-{version}.aar
   └── /your-module
        └─ build.gradle
```

ライブラリを使用するモジュールの build.gradle で、以下の依存関係を追加します。

```
dependencies {
    implementation fileTree(dir: 'libs', include: ['*.aar'])
}
```

# モジュールのビルド

モジュールの開発を行う場合には、SRT をビルドしておく必要があります。

SRTのビルドマニュアルは、dConnectLibStreaming/libsrt/README.md にありますので、参照してください。

## モジュールのビルド

下記のコマンドを実行することで、libmedia と libsrt の aar ファイルが作成されます。

```
$ ./gradlew assembleRelease
```

dConnectLibStreaming/build/outputs/aar に aar ファイルは作成されます。

# 各モジュールの依存している外部ライブラリ
## libsrt
libsrt モジュールは、以下の外部ライブラリに依存しています。

|ライブラリ名|著作者|ライセンス|
|:--|:--|:--|
|[Secure Reliable Transport (SRT)](https://github.com/Haivision/srt)|Haivision Systems Inc.|[Mozilla Public License Version 2.0](https://github.com/Haivision/srt/blob/master/LICENSE)|