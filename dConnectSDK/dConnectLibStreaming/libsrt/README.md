# SRT ビルドマニュアル

SRT を Android 向けにビルドするための手順をまとめます。<br>
SRT のビルドには、VirtualBox と Vagrant を用います。

## 動作環境

- OS X Mojave バージョン 10.14.6 or Windows11 22H2
- ハードディスク 64GB以上推奨
- メモリ 8GB以上推奨

##### フォルダ構成

```
/libsrt
 └─ /srt-build
     ├─ /data
     │   ├─ /ndk
     │   └─ /srt
     └─ /ubuntu
         ├─ /.vagrant
         └─ Vagrantfile
```

## VirtualBox のインストール

下記のサイトからダウンロードして、インストーラの手順に沿ってインストールを行なってください。<br>
[https://www.virtualbox.org/wiki/Downloads](https://www.virtualbox.org/wiki/Downloads)


## Vagrant のインストール

下記のサイトからダウンロードして、インストーラの手順に沿ってインストールを行なってください。<br>
[https://www.vagrantup.com/downloads.html](https://www.vagrantup.com/downloads.html)

下記のようなエラーが発生した場合には、Vagrant のバージョンが古いので、上記のサイトから最新版をダウンロードして、インストールし直してください。

```
$ vagrant up
No usable default provider could be found for your system.

Vagrant relies on interactions with 3rd party systems, known as
"providers", to provide Vagrant with resources to run development
environments. Examples are VirtualBox, VMware, Hyper-V.

The easiest solution to this message is to install VirtualBox, which
is available for free on all major platforms.

If you believe you already have a provider available, make sure it
is properly installed and configured. You can see more details about
why a particular provider isn't working by forcing usage with
`vagrant up --provider=PROVIDER`, which should give you a more specific
error message for that particular provider.
```

### VirtualBox へ Linux OS を追加

下記のコマンドで VirtualBox にインストールされている OS の一覧を確認することができます。

```
$ vagrant box list
hashicorp/precise32 (virtualbox, 1.0.0)
ubuntu/xenial64     (virtualbox, 20170922.0.0)
```

使用する OS がインストールされていない場合には下記のコマンドでインストールします。

```
$ vagrant box add {VM名} {boxファイルダウンロードURL}
```

<b>bento/ubuntu-22.04 の追加</b>

下記のコマンドで ubuntu を追加することができますが、ダウンロードにはかなりの時間がかかりますので注意してください。

```
$ vagrant box add bento/ubuntu-22.04
```

### Vagrantfile の作成

ubuntu フォルダ内に Vagrantfile を作成しますので、フォルダを作成して移動します。

```
$ cd srt-build/ubuntu
$ vagrant init bento/ubuntu-22.04
```

コマンドの実行に成功すると Vagrantfile が作成されます。


<b>Vagrantfile の編集</b>

MacOS とファイルを共有するために config.vm.synced_folder のコメントを外します。

```ruby
  # Share an additional folder to the guest VM. The first argument is
  # the path on the host to the actual folder. The second argument is
  # the path on the guest to mount the folder. And the optional third
  # argument is a set of non-required options.
  config.vm.synced_folder "../data", "/vagrant_data"
```

config.vm.synced_folder は下記のような構成になっています。<br>
必要に応じて、パスを編集してください。

```ruby
config.vm.synced_folder {MacOS のパス}, {VM 内のパス}
```

外部から VM 上のサーバにアクセスさせたい場合には、config.vm.network のコメントを外します。

DHCP で IP が割り振られますので、IP でアクセスできるようになります。<br>
サーバを使用しない場合には、ここはコメントアウトしなくても問題ありません。

```ruby
  # Create a public network, which generally matched to bridged network.
  # Bridged networks make the machine appear as another physical device on
  # your network.
  config.vm.network "public_network"
```

virtualbox の設定のコメントアウトを外します。<br>
メモリサイズも 1024 から 4096 に変更します。メモリサイズが小さいとビルドに時間がかかります。

```ruby
  config.vm.provider "virtualbox" do |vb|
    # # Display the VirtualBox GUI when booting the machine
    # vb.gui = true
  
    # Customize the amount of memory on the VM:
    vb.memory = "4096"
  end
```

インストールしておくべきライブラリを追加します。

```ruby
  # Enable provisioning with a shell script. Additional provisioners such as
  # Puppet, Chef, Ansible, Salt, and Docker are also available. Please see the
  # documentation for more information about their specific syntax and use.
  config.vm.provision "shell", inline: <<-SHELL
    apt-get update
    apt-get upgrade
    apt-get install tclsh pkg-config cmake libssl-dev build-essential unzip
    # apt-get install -y apache2
  SHELL
```

### Vagrant の起動

Vagrantfile を編集後に、下記のコマンドを実行して、Vagrant を起動します。

使用する OS がダウンロードされていない場合には、ダウンロードが発生し時間がかかりますので、ご注意ください。

```
$ vagrant up
```

vagrant up を実行時に下記のようなエラーが多発します。<br>
これは、通信が遅くてOSのイメージのダウンロードに失敗した場合に発生するようです。

このエラーが発生した場合には、繰り返し、 vagrant up を実行してください。<br>
前回ダウンロードした分はキャッシュしているので、いつかはダウンロードが完了します。

```
An error occurred while downloading the remote file. The error
message, if any, is reproduced below. Please fix this error and try
again.

OpenSSL SSL_read: SSL_ERROR_SYSCALL, errno 54
```

ダウンロードしたファイルは、下記にのフォルダに保存されています。<br>
何度繰り返しても成功しない場合には、ここのファイルを削除してからもう一度試してください。

```
~/.vagrant.d/tmp/
```

どうしても、ダウンロードが遅い場合には、vagrant up を行なった時の Downloading: に表示されている URL を wget などで取得します。<br>
wget などのコマンドを使用してダウンロードすると vagrant up で取得するよりも速くダウンロードが完了します。

```
$ vagrant up
Bringing machine 'default' up with 'virtualbox' provider...
==> default: Box 'bento/ubuntu-18.04' could not be found. Attempting to find and install...
    default: Box Provider: virtualbox
    default: Box Version: >= 0
==> default: Loading metadata for box 'bento/ubuntu-18.04'
    default: URL: https://vagrantcloud.com/bento/ubuntu-18.04
==> default: Adding box 'bento/ubuntu-18.04' (v201910.20.0) for provider: virtualbox
    default: Downloading: https://vagrantcloud.com/bento/boxes/ubuntu-18.04/versions/201910.20.0/providers/virtualbox.box
==> default: Box download is resuming from prior download progress
    default: Download redirected to host: vagrantcloud-files-production.s3.amazonaws.com
```

wget で virtualbox のファイルをダウンロードします。

```
$ wget {BOX への URL}
```

vagrant box add を使用して追加します。

```
$ vagrant box add bento/ubuntu-18.04 {ダウンロードした BOX のパス}
```

上記の処理を行ってから再度 vagrant up を行います。


ネットワークを求められた場合には、必要に応じたネットワークを入力します。<br>
Wi-Fi を使用する場合には 1 を入力します。

```
==> default: Available bridged network interfaces:
1) en0: Wi-Fi (AirPort)
2) en5: USB Ethernet(?)
3) p2p0
4) awdl0
5) en3: Thunderbolt 1
6) en1: Thunderbolt 2
7) en4: Thunderbolt 3
8) en2: Thunderbolt 4
9) bridge0
==> default: When choosing an interface, it is usually the one that is
==> default: being used to connect to the internet.
    default: Which interface should the network bridge to? 1
```

### Vagrant へのログイン

vagrant up で virtualbox が起動した後に、下記のコマンドを実行してログインします。

```
$ vagrant ssh
```

### Vagrant からログアウト

ログインした状態で exit コマンドを実行することでログアウトすることができます。

```
$ exit
```

### Vagrant の終了

```
$ vagrant halt
```

### Vagrant の状態

```
$ vagrant status
```

## SRT のビルド手順

Vagrantfile のあるフォルダで下記のコマンドを実行して、Ubuntu にログインします。

```
$ vagrant ssh
```

### NDK のダウンロード

以下のサイトから NDK をダウンロードすることができます。<br>
[https://developer.android.com/ndk/downloads?hl=ja](https://developer.android.com/ndk/downloads?hl=ja)

コマンドでインストールする場合

```
$ cd /vagrant_data
$ wget https://dl.google.com/android/repository/android-ndk-r19c-linux-x86_64.zip
$ unzip android-ndk-r19c-linux-x86_64.zip
```

unzip がインストールされていない場合には以下のコマンドを実行します。

```
$ sudo apt install unzip
```

### SRT のビルド

SRT は、下記のサイトに公開されています。<br>
[https://github.com/Haivision/srt](https://github.com/Haivision/srt)

下記のコマンドでソースコードをクローンすることができます。
vx.x.xにクローンするバージョンを指定してください。

```
$ cd /vagrant_data
$ git clone -b vx.x.x https://github.com/Haivision/srt.git
```

### ビルドの実行

[https://github.com/Haivision/srt/blob/master/docs/build/build-android.md](https://github.com/Haivision/srt/blob/master/docs/build/build-android.md)

こちらを参考にAndroid用のビルドを行っていきます。

```
$ cd /vagrant_data/srt/scripts/build-android
$ ./build-android -n /path/to/ndk
```

/path/to/ndkには、wgetしてきたndkのディレクトリを指定してください。
上記コマンドによりビルドが実行されます。


### jniLibs の作成

SRT ビルド完了後、prebuiltというディレクトリが作成されます。
その中に、srt.soファイルが生成されます。

また、ビルドに必要なヘッダーファイル `arm64_v8a/include` も jniLibs にコピーします。<br>
ヘッダーファイルはビルドしたフォルダに作成されていますので、どれをコピーしても問題ありません。

## SRT 

作成した SRT のライブラリ(prebuilt直下のフォルダ一式)を Android Project の `src/main/jniLibs` にコピーします。<br>

##### フォルダ構成

```
/libsrt
 ├─ build.gralde
 ├─ CMakeLists.txt
 ├─ /libs
 ├─ README.md
 └─ /src
     ├─ /androiTest
     ├─ /main
     |    ├─ AndroidManifest.xml
     |    ├─ /cpp
     |    ├─ /java
     |    ├─ /jniLibs <---- ここにコピーします。
     |    |    ├─ /arm64-v8a
     |    |    ├─ /armeabi-v7a
     |    |    ├─ /include
     |    |    ├─ /x86
     |    |    └─ /x86_64
     |    └─ /res
     └─ /test
```
