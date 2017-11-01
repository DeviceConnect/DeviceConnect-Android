# Device Connect SDK for Android
Device Connect SDK for Android は Device Connectアプリケーション開発用SDKです。
 
## 提供する機能

Device Connect SDK for Android では以下の機能を提供します。

- Device Connect Managerの生存確認
- Device Connect Managerの開始・停止
- Device Connect Managerの認証
- Device Connect Managerからのサービス一覧取得
- Device Connect Managerへのリクエスト送信
- WebSocketの接続管理
- イベントの登録・解除・受信

## アプリケーション開発マニュアル

アプリケーションを開発するためのマニュアルは下記のwikiに記載してあります。

- [アプリケーション開発マニュアル](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/Application-Manual-for-Android-Studio)

## Gradle

build.gradleに以下の記述をすることで、Device Connect SDK for Androidを使用することができます。

```
repositories {
    maven { url 'https://raw.githubusercontent.com/DeviceConnect/DeviceConnect-Android/master/dConnectSDK/dConnectSDKForAndroid/repository/' }
}

dependencies {
    compile 'org.deviceconnect:dconnect-sdk-for-android:2.2.2'
}
```

## SDKの使い方

ここでは、Device Connect SDK for Android の使用方法を説明します。

### DConnectSDKの生成

DConnectSDKのインスタンスを作成して、Device Connect Managerにアクセスを行います。

```Java
DConnectSDK sdk = DConnectSDKFactory.create(context, DConnectSDKFactory.Type.HTTP);
```

### Device Connect Manager生存確認

Device Connect Managerを使用する前に、生存確認を行います。

Device Connect Managerの生存確認には以下のメソッドを使用します。

> DConnectSDK#availability()

#### 同期実行
 
以下の同期でAPIを呼び出す場合には、内部でネットワーク通信を行うためにUIスレッドから呼び出すと例外が発生しますので、ご注意ください。

```Java
DConnectResponseMessage response = sdk.availability();
if (response.getResult() == DConnectMessage.RESULT_OK) {
    // 生存
}
```

#### 非同期実行

非同期で呼び出す場合には、引数にDConnectSDK.OnResponseListenerを指定します。<br>
DConnectSDK内部でスレッドを作成して、命令を実行しますので、リスナーの呼び出しすスレッドはUIスレッドではありません。UI操作を行う場合にはご注意ください。

```Java
sdk.availability(new DConnectSDK.OnResponseListener() {
    @Override
    public void onResponse(DConnectResponseMessage response) {
        if (response.getResult() == DConnectMessage.RESULT_OK) {
            // 生存
        }
    }
});
```

Managerが起動していない場合には、以下のAPIで起動します。

```Java
sdk.startManager(context);
```

このメソッドは、URIスキームを用いてDevice Connect Managerを起動します。<br>
同じ端末にインストールされているDevice Connect Manager以外は起動できません。

### Device Connect Managerの認証

Device Connect Managerを使用するためには、Managerから認証を行い、アクセストークンを取得する必要があります。

アクセストークンの取得には以下のメソッドを使用します。

> DConnectSDK#authorization(appName, scopes)
> 
> - appName: アプリケーション名をしてします。
> - scopes: 使用するプロファイルの一覧を指定します。

#### 同期実行

```Java
String appName = "ExampleApp";
String[] scopes = {
        "serviceDiscovery",
        "serviceInformation",
        "battery"
};

DConnectResponseMessage response = sdk.authorization(appName, scopes);
if (response.getResult() == DConnectMessage.RESULT_OK) {
    String accessToken = response.getString("accessToken");
    sdk.setAccessToken(accessToken);
}
```

#### 非同期実行

```Java
String appName = "ExampleApp";
String[] scopes = {
        "serviceDiscovery",
        "serviceInformation",
        "battery"
};

sdk.authorization(appName, scopes, new DConnectSDK.OnResponseListener() {
    @Override
    public void onResponse(DConnectResponseMessage response) {
        if (response.getResult() == DConnectMessage.RESULT_OK) {
            String accessToken = response.getString("accessToken");
            sdk.setAccessToken(accessToken);
        }
    }
});
```

取得したアクセストークンは、ストレージなどに保存しておき、DConnectSDKを作り直した時に、DConnectSDKに設定します。

不正なアクセストークンでAPIにアクセスした場合には、以下のようなエラーが発生します。

```Java
DConnectResponseMessage response = sdk.serviceDiscovery();
if (response.getResult() != DConnectMessage.RESULT_OK) {
    ErrorCode errorCode = DConnectMessage.ErrorCode.getInstance(response.getInt("errorCode"));
    switch (errorCode) {
        case AUTHORIZATION:
            // 認証エラー
            break;
        case EXPIRED_ACCESS_TOKEN:
            // 有効期限切れ
            break;
        case EMPTY_ACCESS_TOKEN:
            // アクセストークンがない
            break;
        case SCOPE:
            // スコープにないプロファイルにアクセスされた
            break;
        case NOT_FOUND_CLIENT_ID:
            // クライアントIDが見つからない
            break;
    }
}
```

これらのエラーが発生した場合には、アクセストークンを取得し直す必要があります。

### サービス一覧の取得

Device Connect Managerに接続されているサービスの一覧を取得します。<br>
取得したサービスのIDを用いて、各サービスを操作します。

サービス一覧の取得には、以下のメソッドを使用します。

> DConnectSDK#serviceDiscovery()

#### 同期実行

```Java
DConnectResponseMessage response = sdk.serviceDiscovery();
if (response.getResult() == DConnectMessage.RESULT_OK) {
    for (Object obj : response.getList("services")) {
        DConnectMessage service = (DConnectMessage) obj;
        String serviceId = service.getString("id");
        String name = service.getString("name");
    }
}
```

#### 非同期実行

```Java
sdk.serviceDiscovery(new DConnectSDK.OnResponseListener() {
    @Override
    public void onResponse(DConnectResponseMessage response) {
        if (response.getResult() == DConnectMessage.RESULT_OK) {
            for (Object obj : response.getList("services")) {
                DConnectMessage service = (DConnectMessage) obj;
                String serviceId = service.getString("id");
                String name = service.getString("name");
            }
        }
    }
});
```

### サービス情報取得

各サービスが持っているプロファイル情報を取得します。<br>
各サービスが操作できるプロファイルやパラメータなどを確認することができます。

サービス情報の取得には以下のメソッドを使用します。

> DConnectSDK#getServiceInformation(serviceId)
> 
> - serviceId: DConnectSDK#serviceDiscovery()で受け取ったサービスのidを指定します。

#### 同期実行

```Java
DConnectResponseMessage response = sdk.getServiceInformation(serviceId);
if (response.getResult() == DConnectMessage.RESULT_OK) {
    Log.i("Exmaple", response.toString());
}
```

#### 非同期実行

```Java
sdk.getServiceInformation(serviceId, new DConnectSDK.OnResponseListener() {
    @Override
    public void onResponse(DConnectResponseMessage response) {
        if (response.getResult() == DConnectMessage.RESULT_OK) {
            Log.i("Exmaple", response.toString());
        }
    }
});
```

### HOSTのライト一覧を取得 (GET)

HOST(Android端末)のライト一覧を取得します。

ライト一覧の取得には以下のメソッドを使用します。

> DConnectSDK#get(uri)
> 
> - uri: Device Connect ManagerへのリクエストURIを指定します。

#### 同期実行

```Java
DConnectSDK.URIBuilder builder = sdk.createURIBuilder();
builder.setProfile("light");
builder.setServiceId("Host.f16efda156ed2d91b1a82b6fdbbd30.localhost.deviceconnect.org");

DConnectResponseMessage response = sdk.get(builder.toString());
if (response.getResult() == DConnectMessage.RESULT_OK) {
    List lights = response.getList("lights");
    for (Object obj : lights) {
        DConnectMessage light = (DConnectMessage) obj;
        String lightId = light.getString("lightId");
    }
}
```

#### 非同期実行

```Java
DConnectSDK.URIBuilder builder = sdk.createURIBuilder();
builder.setProfile("light");
builder.setServiceId("Host.f16efda156ed2d91b1a82b6fdbbd30.localhost.deviceconnect.org");

sdk.get(builder.toString(), new DConnectSDK.OnResponseListener() {
    @Override
    public void onResponse(DConnectResponseMessage response) {
        if (response.getResult() == DConnectMessage.RESULT_OK) {
            List lights = response.getList("lights");
            for (Object obj : lights) {
                DConnectMessage light = (DConnectMessage) obj;
                String lightId = light.getString("lightId");
            }
        }
    }
});
```

### HOSTのライトを点灯 (POST)

HOSTについているライトを点灯します。<br>
ここでは、lightIdを省略していますので、デフォルトのライトが点灯します。

ライト点灯には以下のメソッドを使用します。

> DConnectSDK#post(uri, entity)
> 
> - uri: Device Connect ManagerへのリクエストURIを指定します。
> - entity: POSTのBodyに送るエンティティを指定します。不要な場合にはnullを指定します。

#### 同期実行

```Java
DConnectSDK.URIBuilder builder = sdk.createURIBuilder();
builder.setProfile("light");
builder.setServiceId("Host.f16efda156ed2d91b1a82b6fdbbd30.localhost.deviceconnect.org");
DConnectResponseMessage response = sdk.post(builder.toString(), null);
if (response.getResult() == DConnectMessage.RESULT_OK) {
}
```

#### 非同期実行

```Java
DConnectSDK.URIBuilder builder = sdk.createURIBuilder();
builder.setProfile("light");
builder.setServiceId("Host.f16efda156ed2d91b1a82b6fdbbd30.localhost.deviceconnect.org");
sdk.post(builder.toString(), null, new DConnectSDK.OnResponseListener() {
    @Override
    public void onResponse(DConnectResponseMessage response) {
        if (response.getResult() == DConnectMessage.RESULT_OK) {
        }
    }
});
```

Android端末のライトは、カメラに付随していますので、他のカメラアプリなどとは同時に使用することができませんので、ご注意ください。

### HOSTのライトを消灯 (DELETE)

HOSTについているライトを消灯します。

ライト消灯には以下のメソッドを使用します。

> DConnectSDK#delete(uri)
> 
> - uri: Device Connect ManagerへのリクエストURIを指定します。


#### 同期実行

```Java
DConnectSDK.URIBuilder builder = sdk.createURIBuilder();
builder.setProfile("light");
builder.setServiceId("Host.f16efda156ed2d91b1a82b6fdbbd30.localhost.deviceconnect.org");
DConnectResponseMessage response = sdk.delete(builder.toString());
```

#### 非同期実行

```Java
DConnectSDK.URIBuilder builder = sdk.createURIBuilder();
builder.setProfile("light");
builder.setServiceId("Host.f16efda156ed2d91b1a82b6fdbbd30.localhost.deviceconnect.org");
sdk.delete(builder.toString(), new DConnectSDK.OnResponseListener() {
    @Override
    public void onResponse(DConnectResponseMessage response) {
    }
});
```

### POSTで送信するデータの作成

POSTでデータを送るには、以下の方法があります。

- 文字列
- バイナリ
- マルチパート

#### 文字列データ作成

```Java
StringEntity entity = new StringEntity("key=value");

DConnectResponseMessage response = sdk.post(uri, entity);
```
#### バイナリデータ作成

```Java
byte[] value = { ... };
BinaryEntity entity = new BinaryEntity(value);

DConnectResponseMessage response = sdk.post(uri, entity);
```

#### マルチパートデータ作成

```Java
MultipartEntity data = new MultipartEntity();
data.add("key", new StringEntity("value"));
data.add("data", new FileEntity(new File(path)));

DConnectResponseMessage response = sdk.post(uri, entity);
```

### WebSocketの接続

Device Connect Manager からイベントを受信するためには、Device Connect Manager に WebSocket を接続する必要があります。

```Java
sdk.connectWebSocket(new DConnectSDK.OnWebSocketListener() {
    @Override
    public void onOpen() {
        // WebSocketの接続した時に呼び出されます。
    }

    @Override
    public void onClose() {
        // WebSocketが切断した時に呼び出されます。
    }

    @Override
    public void onError(Exception e) {
        // WebSocketでエラーが発生した時に呼び出されます。
    }
});
```

イベント開始後にWebSocketが切断された場合には、同じオリジンで登録されているイベントが全て解除されます。<br>
よって、WebSocketが切断された場合には、再開する場合には再度イベントを登録する必要があります。

オリジンは、デフォルトではアプリのパッケージ名が指定されています。<br>
必要に応じて、DConnectSDK#setOrigin()で変更することができます。

### WebSocketの切断

WebSocketを接続した際には、最後に必ずWebSocketを切断する必要があります。

```Java
sdk.disconnectWebSocket();
```

WebSocketを切断せずに終了した場合にはメモリリークになる恐れがありますので、ご注意ください。

### 加速度センサーのイベント登録

HOSTについている加速度センサーの値をイベントとして受け取ります。<br>
WebSocketが接続されていない場合には、イベントを開始してもイベントの受信は行えません。

```Java
DConnectSDK.URIBuilder builder = sdk.createURIBuilder();
builder.setProfile("deviceOrientation");
builder.setAttribute("onDeviceOrientation");
builder.setServiceId("Host.f16efda156ed2d91b1a82b6fdbbd30.localhost.deviceconnect.org");
sdk.addEventListener(builder.build(), new DConnectSDK.OnEventListener() {
    @Override
    public void onMessage(DConnectEventMessage message) {
        Log.i("Example", "Event: " + message.toString());
    }

    @Override
    public void onResponse(DConnectResponseMessage response) {
        if (response.getResult() == DConnectMessage.RESULT_OK) {
            // イベントの開始に成功
        } else {
            // イベントの開始に失敗
        }
    }
});
```

イベント開始の成否は、DConnectSDK.OnEventListener#onResponse()に通知されます。


### 加速度センサーのイベント解除

以下のようにDConnectSDK#removeEventListener()を呼び出すことで、イベントの停止を行います。

```Java
DConnectSDK.URIBuilder builder = sdk.createURIBuilder();
builder.setProfile("deviceOrientation");
builder.setAttribute("onDeviceOrientation");
builder.setServiceId("Host.f16efda156ed2d91b1a82b6fdbbd30.localhost.deviceconnect.org");
sdk.removeEventListener(builder.build());
```
