# HOGPプラグインについて

このHOGPプラグインでは、HOGPを実装して、RESTfulから接続先のマウスやキーボードを操作するための機能を提供します。

HOGP(HID over GATT Profile)とは、Bluetooth low energyを使用してマウスやキーボードなどを接続するためのプロファイルです。

## 開発環境
Android Studio 2.2.1以上

## ビルドマニュアル
- [HOGPビルドマニュアル](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/HOGP-Build)

## サポートするプロファイル
- hogp
- mouse
- keyboard

# HOGPプラグインの使用方法
HOGPプラグインの使用法について簡単に説明を行います。

## HOGPサーバの有効化
HOGPを使用するためには、まずHOGPサーバを有効にする必要があります。

有効化するには、以下の2通りあります。

- HOGPプラグインの設定画面を開いて、HOGPサーバを有効にする
- HOGPプロファイルを用いてHOGPサーバを有効にする

### HOGPプラグインの設定画面から有効にする場合
HOGPプラグインの設定画面を開き「HOGPサーバ」のスイッチをONにします。<br>

HOGPサーバ設定の項目に、Mouse、Keyboardの設定がありますので、必要に応じて設定を行なってください。

### HOGPプロファイルを用いてHOGPサーバを有効にする場合
以下のAPIを使用します。

```http
POST /gotapi/hogp?serviceId=[HOGPのサービスID]&mouse=relative&keyboard=true
```

mouseには、absoluteとrelativeを指定することができます。<br>
mouseを使用しない場合には、noneを指定してください。<br>

Android端末では、absoluteは対応していませんので、absoluteを使用したい場合には、MacBookなど対応しているデバイスから接続を行なってください。<br>

keyboardとして使用する場合はtrue、使用しない場合はfalseを指定してください。

```json
{
    "result" : 1,
    "errorCode" : 1,
    "errorMessage" : "Failed to start HOGP Server. message=Bluetooth is disabled."
}
```

上記のエラーが返却されてきた場合には、Bluetoothが無効になっていますので、以下のコマンドでBluetoothを有効化してください。

```http
PUT /gotapi/connection/ble?serviceId=[HOSTのサービスID]
```

## デバイスとのペアリング

HOGPサーバが有効化されるとマウス・キーボードとしてBluetoothデバイスが公開されます。<br>
Android端末やMacBookなどから、HOGPサーバの端末を見つけてペアリングを行なってください。<br>
端末のデバイス名を確認するには、Android端末の設定画面からBluetooth設定を開き、メニューから「この機器の名前を変更」を選択することでできます。

MacBookから接続する場合は、「システム環境設定」→「マウス」→「Bluetoothマウスを設定」を開きます。
デバイスのリストからHOGPサーバを起動している端末を選択してペアリングを行います。

Android端末から接続する場合は、端末の設定画面からBluetoothを開きます。
使用可能なデバイスからHOGPサーバを起動している端末を選択してペアリングを行います。

Android端末でペアリングを行う場合に、ペアリング確認ダイアログが画面に表示されないことがあります。<br>
その場合には、通知バーに登録されていますので、そちらをクリックしてダイアログを表示してください。<br>
通知バーにもペアリング確認ダイアログがない場合には、もう一度最初からペアリングを行なってください。

## デバイスIDの取得

接続ができた後は、Service Discoveryを行うことで接続しているデバイスを発見することができます。

本来であれば、デバイス名が取得できるのですが、デバイス名が取得できずにBluetoothアドレスを表示することがありますので、ご注意ください。

## マウス操作

マウスとして接続した場合には、マウスカーソルの移動やマウスクリックをAPIで行うことができます。

### relativeの場合
relativeの場合には、相対的にマウスカーソルを移動します。x,yに指定した値の分だけ現在のカーソルの位置から移動します。<br>
移動量は、-1.0から1.0の範囲が指定することができます。<br>

右下に移動する場合には以下のようにxとyを指定します。

```http
POST /gotapi/mouse?serviceId=[操作する端末のID]&x=0.1&y=0.1
```

左上に移動する場合には、以下のようにxとyに負の値を指定します。

```http
POST /gotapi/mouse?serviceId=[操作する端末のID]&x=-0.1&y=-0.1
```

### absoluteの場合
absoluteの場合には、絶対座標でマウスカーソルが移動します。<br>
x座標は、0.0が左端、1.0が右端になり、画面横幅の割合の位置にマウスカーソルが移動します。<br>
y座標は、0.0が上端、1.0が下端になり、画面縦幅の割合の位置にマウスカーソルが移動します。<br>
(デバイスの座標系によっては、反対になることもあります。)

画面の右下にマウスカーソルを移動する場合には以下のようにxとyを指定します。

```http
POST /gotapi/mouse?serviceId=[操作する端末のID]&x=1&y=1
```
画面の左上にマウスカーソルを移動する場合には以下のようにxとyを指定します。

```http
POST /gotapi/mouse?serviceId=[操作する端末のID]&x=0&y=0
```

画面の中央にマウスカーソルを移動する場合には以下のようにxとyを指定します。

```http
POST /gotapi/mouse?serviceId=[操作する端末のID]&x=0.5&y=0.5
```

Android端末では、absoluteがサポートされていませんので動作しません。ご注意ください。

### マウスホイール

マウスホイールでスクロールする場合には、以下のAPIで行います。<br>
wheelに指定した分だけ現在の位置からスクロールします。<br>
移動量は、-1.0から1.0の範囲が指定することができます。<br>

```http
POST /gotapi/mouse?serviceId=[操作する端末のID]&wheel=0.1
```

ホイールの回転の向きは、OSや設定によって異なりますので、ご注意ください。<br>

### マウスクリック

クリックする場合には、以下のAPIで行います。<br>
マウスカーソルがある位置でマウスの左クリックを行います。

```http
POST /gotapi/mouse/click?serviceId=[操作する端末のサービスID]&left=true
```

右クリックを行う場合には、right=trueを指定します。

```http
POST /gotapi/mouse/click?serviceId=[操作する端末のサービスID]&right=true
```

### マウス長押し

マウスの左ボタンを長押しを行う場合には、以下のAPIのようにleftButtonにtrueを指定します。<br>

```http
POST /gotapi/mouse?serviceId=[操作する端末のID]&leftButton=true
```

長押しを解除するには、leftButtonにfalseをしています。<br>
(leftButtonが省略された場合もfalseの扱いになります。)

```http
POST /gotapi/mouse?serviceId=[操作する端末のID]&leftButton=false
```

以下のようにleftButton=trueを送ってから2秒待ってleftButton=falseを送ることで、2秒間の長押しを行うことができます。

```http
POST /gotapi/mouse?serviceId=[操作する端末のID]&leftButton=true
    ↓
  2秒待つ
    ↓
POST /gotapi/mouse?serviceId=[操作する端末のID]&leftButton=false
```

leftButton=trueを送った後にleftButton=falseを送らないとマウスの左ボタンの長押しが解除されませんので、ご注意ください。


## キーボード操作
キーボードとして接続した場合には、文字の入力や削除、カーソルの移動などをAPIで行うことができます。<br>
ただし、入力される側の端末がテキストエディタなどにフォーカスがあっていない場合には何も処理されませんのご注意ください。

HIDの文字コードで入力したい場合には、以下のAPIを使用します。<br>
keyCodeには、HIDで定義されているキーコードを指定します。<br>
modifyには、shift,alt,gui,ctrlを指定します。

以下のAPIを実行すると「A」が入力されます。

```http
POST /gotapi/keyboard?serviceId=[操作する端末のサービスID]&keyCode=0x04&modify=shift
```

以下のように、modifyは、カンマ区切りで送ることでモディファイキーの同時押しとして送ることができます。

```http
POST /gotapi/keyboard?serviceId=[操作する端末のサービスID]&keyCode=0x04&modify=shift,ctrl
```

HIDのキーコードでは分かりづらいので、ASCII文字列で入力できるように以下のAPIを定義しています。<br>
パラメータstringに指定されたASCII文字列を入力します。<br>

```http
POST /gotapi/keyboard/ascii?serviceId=[操作する端末のサービスID]&string=A
```

入力した文字列を削除するには、以下のAPIを使用します。

```http
POST /gotapi/keyboard/del?serviceId=[操作する端末のサービスID]
```

エスケープする場合には、以下のAPIを使用します。<br>
Android端末の場合には、バックキーと同じ効果がありますので、画面を閉じるなどの処理が行えます。

```http
POST /gotapi/keyboard/esc?serviceId=[操作する端末のサービスID]
```

エンターキーを押下する場合には、以下のAPIを使用します。

```http
POST /gotapi/keyboard/enter?serviceId=[操作する端末のサービスID]
```

十字キーを押下する場合には、以下のAPIを使用します。

```http
POST /gotapi/keyboard/upArrow?serviceId=[操作する端末のサービスID]
POST /gotapi/keyboard/downArrow?serviceId=[操作する端末のサービスID]
POST /gotapi/keyboard/leftArrow?serviceId=[操作する端末のサービスID]
POST /gotapi/keyboard/rightArrow?serviceId=[操作する端末のサービスID]
```

# サポート状況
HOGPサーバを起動するには、Android 5.0以上が必要になり、端末のチップセットがペリフェラルに対応している必要があります。<br>
Nexus5などのAndroid 5.0以上でもチップセットがペリフェラルに対応していない端末では、HOGPプラグインは使用することができません。

HOGPクライアントは、Android 4.4からサポートされています。<br>
Nexus5は、HOGPサーバとしては使用できませんが、クライアントとして接続することはできます。

また、Android端末にはBluetooth接続に相性があるために、接続してもHOGPとして使用できないことがあります。

動作検証を行った結果を以下の表にまとめました。

### Nexus9 (7.1.1)

|HOGPクライアント|動作|備考|
|:----|:-----|:-----|
|Nexus5(5.0.1)|○||
|GalaxyS7Edge(7.0)|○||
|ZenPhone(7.0)|○||
|Xperia X compact(6.0.1)|○||
|Xperia XZ Premium(7.1.1)|○||
|Nexus5(4.4.3)|○||
|AQUOS Compact SH-02H(6.0.1)|○||
|ARROWS NX F-05F(5.0.2)|×|ペアリングはできましたが、Reportを送っても動作はしませんでした。|
|MacBook Pro (MacOS Sierra)|○||

### Xperia X compact  (6.0.1)

|HOGPクライアント|動作|備考|
|:----|:-----|:-----|
|Nexus9(7.1.1)|○||
|Nexus5(5.0.1)|○||
|ZenPhone(7.0)|×|ペアリングはできましたが、Reportを送っても動作はしませんでした。|
|GalaxyS7Edge(7.0)|○||
|Xperia XZ Premium(7.1.1)|○|処理が遅いのかマウスポインターの動きやキーボードの動きが遅いです。|
|Nexus5(4.4.3)|○||
|ARROWS NX F-05F(5.0.2)|×|ペアリングはできましたが、Reportを送っても動作はしませんでした。|
|MacBook Pro (MacOS Sierra)|○||

### ZenPhone (7.0)

|HOGPクライアント|動作|備考|
|:----|:-----|:-----|
|Nexus9(7.1.1)|○||
|Nexus5(5.0.1)|○||
|Xperia X compact(6.0.1)|×|ペアリングができませんでした。|
|GalaxyS7Edge(7.0)|○||
|Xperia XZ Premium(7.1.1)|○||
|Nexus5(4.4.3)|○||
|ARROWS NX F-05F(5.0.2)|×|ペアリングはできましたが、Reportを送っても動作はしませんでした。|
|MacBook Pro (MacOS Sierra)|○||

### Galaxy S7 Edge (7.0)

|HOGPクライアント|動作|備考|
|:----|:-----|:-----|
|Nexus9(7.1.1)|○||
|Xperia X compact(6.0.1)|×|ペアリングができませんでした。|
|Nexus5(5.0.1)|×|ペアリングはできましたが、Reportを送っても動作はしませんでした。|
|ZenPhone(7.0)|×|ペアリングもなかなか成功せずに、ペアリングに成功して、Reportを送っても動作しませんでした。|
|ARROWS NX F-05F(5.0.2)|×|ペアリングはできましたが、Reportを送っても動作はしませんでした。|
|Xperia XZ Premium(7.1.1)|×|ペアリングはできましたが、Reportを送っても動作はしませんでした。|
|MacBook Pro (MacOS Sierra)|○||

### Xperia XZ Premium (7.1.1)

|HOGPクライアント|動作|備考|
|:----|:-----|:-----|
|Nexus9(7.1.1)|○||
|Nexus5(5.0.1)|○|ちょっと動きが遅い感じがしました。|
|ZenPhone(7.0)|×|ペアリングはできましたが、Reportを送っても動作はしませんでした。|
|Xperia X compact(6.0.1)|×|ペアリングはできましたが、Reportを送っても動作はしませんでした。|
|GalaxyS7Edge(7.0)|○||
|ARROWS NX F-05F(5.0.2)|×|ペアリングはできましたが、Reportを送っても動作はしませんでした。|

### AQUOS Compact SH-02H(6.0.1)

|HOGPクライアント|動作|備考|
|:----|:-----|:-----|
|Nexus9(7.1.1)|○||
|Xperia X compact(6.0.1)|○||
|Nexus5(4.4.3)|○||
|Nexus5(5.0.1)|○||
|GalaxyS7Edge(7.0)|○||
|Xperia XZ Premium(7.1.1)|○||
|ZenPhone(7.0)|○||
|ARROWS NX F-05F(5.0.2)|×|ペアリングはできましたが、Reportを送っても動作はしませんでした。|
|MacBook Pro (MacOS Sierra)|×|ペアリングができませんでした。ペアリング確認画面が表示されませんでした。|

### Nexus6(7.1.1)
|HOGPクライアント|動作|備考|
|:----|:-----|:-----|
|AQUOS Ever SH-04G(5.0.2)|o||
|Xperia Z4 Tablet(6.0.1)|o||
|GalaxyS5(4.4.2)|x|ペアリングできませんでした|
|ArrowsTablet F-04H(6.0.1)|x|ペアリングはできましたが、マウスポインタなど反応がしませんでした|

### Xperia Z4 Tablet(6.0.1)

|HOGPクライアント|動作|備考|
|:----|:-----|:-----|
|AQUOS Ever SH-04G(5.0.2)|o||
|Nexus6(7.1.1)|o||
|GalaxyS5(4.4.2)|x|ペアリングできませんでした|
|ArrowsTablet F-04H(6.0.1)|x|ペアリングはできましたが、マウスポインタなど反応がしませんでした|

### ArrowsTablet F-04H(6.0.1)

|HOGPクライアント|動作|備考|
|:----|:-----|:-----|
|AQUOS Ever SH-04G(5.0.2)|x|ペアリングはできましたが、マウスポインタなど反応がしませんでした|
|Nexus6(7.1.1)|x|ペアリングはできましたが、マウスポインタなど反応がしませんでした|
|GalaxyS5(4.4.2)|x|ペアリングできませんでした|
|Xperia Z4 Tablet(6.0.1)|x|ペアリングはできましたが、マウスポインタなど反応がしませんでした|