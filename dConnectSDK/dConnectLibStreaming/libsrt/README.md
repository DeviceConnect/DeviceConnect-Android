# SRT ビルドマニュアル

SRT を Android 向けにビルドするための手順をまとめます。<br>
SRT のビルドには、VirtualBox と Vagrant を用います。

## 動作環境

- OS X Mojave バージョン 10.14.6
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

<b>bento/ubuntu-18.04 の追加</b>

下記のコマンドで ubuntu を追加することができますが、ダウンロードにはかなりの時間がかかりますので注意してください。

```
$ vagrant box add bento/ubuntu-18.04
```

### Vagrantfile の作成

ubuntu フォルダ内に Vagrantfile を作成しますので、フォルダを作成して移動します。

```
$ cd srt-build/ubuntu
$ vagrant init bento/ubuntu-18.04
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
    apt-get install tclsh pkg-config cmake libssl-dev build-essential
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

### patchelf のビルド

Android OS 5.0 以前では、ライブラリの soname とそのパスの違いを認識できません。<br>
soname を変更するのに、patchelf を使用します。

下記のサイトからソースコードをダウンロードします。<br>
[http://nixos.org/releases/patchelf/](http://nixos.org/releases/patchelf/)

最新版があれば、そちらをダウンロードしてください。<br>

```
$ wget http://nixos.org/releases/patchelf/patchelf-0.10/patchelf-0.10.tar.bz2
$ tar xfa patchelf-0.10.tar.bz2 
```

カレントディレクトリを移動して、configure を実行し Makefile を作成します。

```sh
$ cd patchelf-0.10
$ ./configure --prefix=/usr/local
checking for a BSD-compatible install... /usr/bin/install -c
checking whether build environment is sane... yes
checking for a thread-safe mkdir -p... /bin/mkdir -p
checking for gawk... gawk
checking whether make sets $(MAKE)... yes
checking whether make supports nested variables... yes
checking whether make supports the include directive... yes (GNU style)
checking for gcc... gcc
checking whether the C compiler works... yes
checking for C compiler default output file name... a.out
checking for suffix of executables... 
checking whether we are cross compiling... no
checking for suffix of object files... o
checking whether we are using the GNU C compiler... yes
checking whether gcc accepts -g... yes
checking for gcc option to accept ISO C89... none needed
checking whether gcc understands -c and -o together... yes
checking dependency style of gcc... gcc3
checking for g++... g++
checking whether we are using the GNU C++ compiler... yes
checking whether g++ accepts -g... yes
checking dependency style of g++... gcc3
Setting page size to 4096
checking that generated files are newer than configure... done
configure: creating ./config.status
config.status: creating Makefile
config.status: creating src/Makefile
config.status: creating tests/Makefile
config.status: creating patchelf.spec
config.status: executing depfiles commands
```

make を実行して、patchelf をビルドします。

```sh
$ make
Making all in src
make[1]: Entering directory '/home/vagrant/workspace/patchelf-0.10/src'
g++ -DPACKAGE_NAME=\"patchelf\" -DPACKAGE_TARNAME=\"patchelf\" -DPACKAGE_VERSION=\"0.10\" -DPACKAGE_STRING=\"patchelf\ 0.10\" -DPACKAGE_BUGREPORT=\"\" -DPACKAGE_URL=\"\" -DPACKAGE=\"patchelf\" -DVERSION=\"0.10\" -DPAGESIZE=4096 -I.    -Wall -std=c++11 -D_FILE_OFFSET_BITS=64 -g -O2 -MT patchelf.o -MD -MP -MF .deps/patchelf.Tpo -c -o patchelf.o patchelf.cc
mv -f .deps/patchelf.Tpo .deps/patchelf.Po
g++ -Wall -std=c++11 -D_FILE_OFFSET_BITS=64 -g -O2   -o patchelf patchelf.o  
make[1]: Leaving directory '/home/vagrant/workspace/patchelf-0.10/src'
Making all in tests
make[1]: Entering directory '/home/vagrant/workspace/patchelf-0.10/tests'
make[1]: Nothing to be done for 'all'.
make[1]: Leaving directory '/home/vagrant/workspace/patchelf-0.10/tests'
make[1]: Entering directory '/home/vagrant/workspace/patchelf-0.10'
make[1]: Nothing to be done for 'all-am'.
make[1]: Leaving directory '/home/vagrant/workspace/patchelf-0.10'
```

ルート権限で、ビルドした patchelf をインストールします。

```sh
$ sudo make install
Making install in src
make[1]: Entering directory '/home/vagrant/workspace/patchelf-0.10/src'
make[2]: Entering directory '/home/vagrant/workspace/patchelf-0.10/src'
 /bin/mkdir -p '/usr/local/bin'
  /usr/bin/install -c patchelf '/usr/local/bin'
make[2]: Nothing to be done for 'install-data-am'.
make[2]: Leaving directory '/home/vagrant/workspace/patchelf-0.10/src'
make[1]: Leaving directory '/home/vagrant/workspace/patchelf-0.10/src'
Making install in tests
make[1]: Entering directory '/home/vagrant/workspace/patchelf-0.10/tests'
make[2]: Entering directory '/home/vagrant/workspace/patchelf-0.10/tests'
make[2]: Nothing to be done for 'install-exec-am'.
make[2]: Nothing to be done for 'install-data-am'.
make[2]: Leaving directory '/home/vagrant/workspace/patchelf-0.10/tests'
make[1]: Leaving directory '/home/vagrant/workspace/patchelf-0.10/tests'
make[1]: Entering directory '/home/vagrant/workspace/patchelf-0.10'
make[2]: Entering directory '/home/vagrant/workspace/patchelf-0.10'
make[2]: Nothing to be done for 'install-exec-am'.
 /bin/mkdir -p '/usr/local/share/doc/patchelf'
 /usr/bin/install -c -m 644 README '/usr/local/share/doc/patchelf'
 /bin/mkdir -p '/usr/local/share/man/man1'
 /usr/bin/install -c -m 644 patchelf.1 '/usr/local/share/man/man1'
make[2]: Leaving directory '/home/vagrant/workspace/patchelf-0.10'
make[1]: Leaving directory '/home/vagrant/workspace/patchelf-0.10'
```

patchelf を実行して、インストールされていることを確認します。

```sh
$ /usr/local/bin/patchelf
syntax: /usr/local/bin/patchelf
  [--set-interpreter FILENAME]
  [--page-size SIZE]
  [--print-interpreter]
  [--print-soname]		Prints 'DT_SONAME' entry of .dynamic section. Raises an error if DT_SONAME doesn't exist
  [--set-soname SONAME]		Sets 'DT_SONAME' entry to SONAME.
  [--set-rpath RPATH]
  [--remove-rpath]
  [--shrink-rpath]
  [--allowed-rpath-prefixes PREFIXES]		With '--shrink-rpath', reject rpath entries not starting with the allowed prefix
  [--print-rpath]
  [--force-rpath]
  [--add-needed LIBRARY]
  [--remove-needed LIBRARY]
  [--replace-needed LIBRARY NEW_LIBRARY]
  [--print-needed]
  [--no-default-lib]
  [--debug]
  [--version]
  FILENAME
```

### SRT のビルド

SRT は、下記のサイトに公開されています。<br>
[https://github.com/Haivision/srt](https://github.com/Haivision/srt)

下記のコマンドでソースコードをクローンすることができます。

```
$ cd /vagrant_data
$ git clone https://github.com/Haivision/srt.git
```

### ビルド用のスクリプトの改修

Android 向けのビルドを行うために NDK のパスを編集します。

```
$ cd /vagrant_data/srt/docs/Android
$ vi mkall
```

NDK のパスを書き直します。<br>
ドキュメントには、 r19 以降でビルドすることができると書かれています。

```
NDK=/opt/android-ndk-r19
   ↓
NDK=/vagrant_data/ndk/android-ndk-r19c
```

srt のバージョンを最新のバージョンに設定します。<br>
srt のバージョンはアップされていくので、バージョンの確認を行う必要があります。<br>
バージョンが異なる場合には、packjni を実行したときにエラーが発生します。

```
srt_version=1.3.1
   ↓
srt_version=x.x.x
```

古いバージョンでビルドを行いたい場合には、git からソースコードを取得する箇所をバージョン指定するように変更します。

```
if [ ! -d $BASE_DIR/srt ]; then
 git clone https://github.com/Haivision/srt srt
# git -C $BASE_DIR/srt checkout v${srt_version}
   ↓
 git clone https://github.com/Haivision/srt srt
 git -C $BASE_DIR/srt checkout v${srt_version}
```

packjni の設定を修正します。

```
$ cd /vagrant_data/srt/docs/Android
$ vi packjni
```

srt のバージョンを mkall に合わせて設定します。

```
srt_version=1.3.1
   ↓
srt_version=x.x.x
```

### ビルドの実行

NDK のパスを編集後に、下記のコマンドを実行して SRT のビルドを行います。

```
$ ./mkall
```

make がインストールされていない場合には下記のコマンドを実行します。

```
$ sudo apt install --reinstall build-essential
```

tclsh がインストールされていない場合には下記のコマンドを実行します。

```
$ sudo apt-get install tclsh
```

cmake がインストールされていない場合には下記のコマンドを実行します。

```
$ sudo apt-get install cmake
```

cmake を apt-get でインストールしても反映されない場合があります。
その場合には下記のようにソースコードからビルドしてインストールする必要があります。

```
$ wget https://cmake.org/files/v3.4/cmake-3.4.0-rc3.tar.gz
$ tar xvf cmake-3.4.0-rc3.tar.gz 
$ cd cmake-3.4.0-rc3/
$ ./configure
$ make
$ sudo make install
$ export PATH="/usr/local/bin:$PATH"
```

### jniLibs の作成

SRT ビルドの完了後に下記のコマンドを実行し、ライブラリに必要な so ファイルをまとめます。

```
$ ./packjni
/vagrant_data/srt/docs/Android/jniLibs/armeabi-v7a/libsrt.so:     file format elf32-little
  SONAME               libsrt.so
/vagrant_data/srt/docs/Android/jniLibs/arm64-v8a/libsrt.so:     file format elf64-little
  SONAME               libsrt.so
/vagrant_data/srt/docs/Android/jniLibs/x86/libsrt.so:     file format elf32-i386
  SONAME               libsrt.so
/vagrant_data/srt/docs/Android/jniLibs/x86_64/libsrt.so:     file format elf64-x86-64
  SONAME               libsrt.so
```

コマンドの実行に成功すると jniLibs というファイルが作成されます。

また、ビルドに必要なヘッダーファイル `arm64_v8a/include` も jniLibs にコピーします。<br>
ヘッダーファイルはビルドしたフォルダに作成されていますので、どれをコピーしても問題ありません。

下記のようなフォルダが作成されます。

```
/Android
 └─ /jniLibs
     ├─ /arm64-v8a
     |    └─ libsrt.so
     ├─ /armeabi-v7a
     |    └─ libsrt.so
     ├─ /include
     |    ├─ /openssl
     |    └─ /srt
     ├─ /x86
     |    └─ libsrt.so
     └─ /x86_64
          └─ libsrt.so
```

## SRT 

作成した SRT のライブラリを Android Project の `src/main/jniLibs` にコピーします。<br>

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
