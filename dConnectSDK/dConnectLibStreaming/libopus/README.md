# Opus ビルド手順

Opus を Android 向けにビルドするための手順を説明します。

<b>動作確認環境</b>

- OS: macOS Mojave バージョン 10.14.4
- opus 1.3.1
- NDK リビジョン r20

## Opus ソースコード取得

以下のサイトから Opus のソースコードをダウンロードします。<br>
[http://opus-codec.org/downloads/](http://opus-codec.org/downloads/)

<b>フォルダ構成</b>

```
└─ /OpusCodec
    └─ /src
        └─ /main
            ├─ /java
            ├─ /jni
            │   ├─ /opus
            │   │   ├─ [ここにソースコードを格納]
            │   ├─ Android.mk
            │   ├─ jni.mk
            │   ├─ opus_jni_decoder.c
            │   ├─ opus_jni_encoder.c
            │   ├─ opus_jni.h
            │   └─ opus.mk
            └─ /res
```

## NDK のダウンロード

Android NDK、リビジョン r20 の NDK を使用します。

以下のページからダウンロードします。<br>
[https://developer.android.com/ndk/downloads/revision_history](https://developer.android.com/ndk/downloads/revision_history)

### 環境変数の設定

.bash_profile に以下の2行を追加して、環境変数に NDK のパスを設定します。

```
export NDK_ROOT={NDK}/android-ndk-r16b
export PATH=$PATH:$NDK_ROOT
```

後の項目で、build_android.sh に環境変数を設定する場合には、この項目はスキップしてください。
