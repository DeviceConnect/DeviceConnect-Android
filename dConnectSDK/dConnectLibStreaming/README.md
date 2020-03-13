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

# インストール
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

# チュートリアル

## SRT サーバー
libsrt の `SRTServer` クラスは SRT のサーバー機能を提供します。

### インスタンス生成
サーバーのポート番号を指定して、インスタンスを作成します。

``` java
SRTServer server = new SRTServer(12345);
```

### オプション設定
サーバーを起動する前に、SRT についてのオプションを指定します。

``` java
Map<Integer, Object> socketOptions = new HashMap<>();
socketOptions.put(SRT.SRTO_PEERLATENCY, 120);
socketOptions.put(SRT.SRTO_LOSSMAXTTL, 1000);
socketOptions.put(SRT.SRTO_CONNTIMEO, 1000);
socketOptions.put(SRT.SRTO_PEERIDLETIMEO, 120);
server setSocketOptions(socketOptions);
```

### コールバック設定
サーバーから実際に映像と音声を配信するためには、`SRTServer.Callback` を設定する必要があります。

```
mSRTServer.setCallback(new SRTServer.Callback() {
    @Override
    public void createSession(SRTSession session) {
        // 必要なリソースを確保し、session に設定。
    }

    @Override
    public void releaseSession(SRTSession session) {
        // リソースを解放。
    }
});
```

セッションで映像と音声のリソースのライフサイクルを管理します。
詳しくは以降の節をご参照ください。


### セッション作成処理
`createSession(SRTSession)` コールバックは、クライアントとの接続が確立した時に呼び出されます。すでに1つ以上の接続がある場合は呼び出されないようになっています。ここで映像・音声のエンコーダを確保し、`session` に対して設定します。

映像・音声のエンコーダは libmedia の `VideoEncoder` と `AudioEncoder` のクラスとして定義されています。映像・音声の具体的なソースによって、`VideoEncoder` と `AudioEncoder` の拡張クラスが実装されています。例えば、Android 端末のカメラ映像を Camera2 API で取得する場合は、`CameraSurfaceVideoEncoder` をセッションに設定します。

以下、セッションに映像のエンコーダを設定するサンプルコードです。

```
// Android 端末のカメラ映像のエンコーダを作成
CameraSurfaceVideoEncoder videoEncoder = new CameraSurfaceVideoEncoder(context, "video/avc");
videoEncoder.addSurface(surface);

// 映像のパラメータを設定
CameraVideoQuality videoQuality.setFacing = (CameraVideoQuality) videoEncoder.getVideoQuality()
videoQuality.setFacing(facing);
videoQuality.setBitRate(biteRate);
videoQuality.setFrameRate(fps);
videoQuality.setVideoWidth(previewSize.getWidth());
videoQuality.setVideoHeight(previewSize.getHeight());

// 映像のエンコーダをセッションに対して設定
session.setVideoEncoder(videoEncoder);
```

音声のエンコーダも同様に設定可能です。

なお、配信しないメディアについては、セッションへの設定を省略することで配信をオフにできます。

### セッション解放処理
`releaseSession(SRTSession)` コールバックは、すべてのクライアントとの接続が切断された時に呼び出されます。ここで必要に応じてリソースの解放を行います。

### SRT サーバー起動
```
server.start();
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