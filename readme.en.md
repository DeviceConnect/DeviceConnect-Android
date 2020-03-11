日本語説明は[こちら](README.md)を参照してください。

# About DeviceConnect-Android
Device Connect WebAPI in WebAPI which operates as a virtual server on the smartphone, it can be easy to use in a uniform description of various wearable devices and IoT devices from a Web browser and apps.
Device Connect Android will be the platform of DeviceConnect of Android version.

In this guide I will continue to discuss the following.

* Quick Start
* Project description
* Development of DeviceConnect app
* Generate a Javadoc of Device Connect SDK
* Build Manuals

Click [here](https://github.com/DeviceConnect/DeviceConnect-Docs/wiki)
 about DeviceConnect System.

# Quick Start
This tutorial explain how to build and run the Device Connect Manager.

It assumes that the following commands are installed here.

* cURL
* git
* adb
* Android SDK

## Build Device Connect Manager
### Mac/Linux
Download DeviceConnect-Android source code.

```
$ git clone https://github.com/DeviceConnect/DeviceConnect-Android.git
```

Before building, make sure the path to AndroidSDK is set to the ANDROID_SDK_ROOT environment variable.

```
$ echo 'export ANDROID_SDK_ROOT=<path>' >> ~/.bash_profile
```

For &lt;path&gt;, please specify the path to Android SDK.
<br><br>
Build Device Connect Manager.

```
$ cd DeviceConnect-Android-master/dConnectManager/dConnectManager
$ ./gradlew assembleDebug
```

Install Device Connect Manager.

```
$ adb install dconnect-manager-app/build/outputs/apk/debug/dconnect-manager-app-debug.apk
```


### Windows
Download DeviceConnect-Android source code.

```
> git clone https://github.com/DeviceConnect/DeviceConnect-Android.git
```

Before building, make sure the path to AndroidSDK is set to the ANDROID_HOME environment variable.

```
> setx ANDROID_HOME <path>
```

For &lt;path&gt;, please specify the path to Android SDK.
<br><br>
Build Device Connect Manager.


```
> cd DeviceConnect-Android/dConnectManager/dConnectManager
> gradlew.bat assembleDebug
```


Install Device Connect Manager.

```
> adb install app/build/outputs/apk/app-debug.apk
```

## Starting Device Connect Manager

Please tap the Device Connect Manager icon.

<div>
    <a href="./assets/icon.png" target="_blank">
        <img src="./assets/icon.png" border="0" width="80" alt="" />
    </a>
</div>

By making it ON the field of DeviceConnectManager, you can start.

<div>
    <a href="./assets/manager_setting_ja.png" target="_blank">
        <img src="./assets/manager_setting_ja.png" border="0" width="200" alt="" />
    </a>
</div>

If Host's icon is displayed on the screen, Device Connect Mangaer is successfully started.<br>
If Host's icon cannot be displayed, please click the 'Search Service' button.

<div>
    <a href="./assets/manager_setting_ja.png" target="_blank">
        <img src="./assets/manager_device_list.png" border="0" width="200" alt="" />
    </a>
</div>

If you want to access from an external PC, enable `enable external IP` on the Device Connect Manager setting screen and restart Device Connect Manager.

Set the IP address to the value of Host displayed on the Device Connect Manager setting screen and execute the following CURL command.

```
curl  -X GET \
      -H 'Origin: localhost' \
      http://192.168.xxx.xxx:4035/gotapi/availability
```

If the following response is returned, you can check that Device Connect Manager is running.

```
{
    "result" : 0,
    "product" : "Device Connect Manager",
    "version":"v2.1.0
}
```

# Project description
## Device Plugin
| Project Name|Content  |
|:-----------|:---------|
|[dConnectDeviceAndroidWear](dConnectDevicePlugin/dConnectDeviceAndroidWear)|Device Plug-in for AndroidWear.|
|[dConnectDeviceChromeCast](dConnectDevicePlugin/dConnectDeviceChromeCast)|Device Plug-in for ChromeCast.|
|[dConnectDeviceFaBo](dConnectDevicePlugin/dConnectDeviceFaBo)|Device Plug-in for FaBo.|
|[dConnectDeviceHeartRate](dConnectDevicePlugin/dConnectDeviceHeartRate)|Device Plug-in for HeartRate such as Mio Alpha.|
|[dConnectDeviceHOGP](dConnectDevicePlugin/dConnectDeviceHOGP)|Device Plug-in for HOGP.|
|[dConnectDeviceHost](dConnectDevicePlugin/dConnectDeviceHost)|Device Plug-in for Android.|
|[dConnectDeviceHue](dConnectDevicePlugin/dConnectDeviceHue)|Device Plug-in for Hue.|
|[dConnectDeviceIRKit](dConnectDevicePlugin/dConnectDeviceIRKit)|Device Plug-in for IRKit.|
|[dConnectDeviceLinking](dConnectDevicePlugin/dConnectDeviceLinking)|Device Plug-in for Linking.|
|[dConnectDeviceTheta](dConnectDevicePlugin/dConnectDeviceTheta)|Device Plug-in for THETA.|
|[dConnectDeviceUVC](dConnectDevicePlugin/dConnectDeviceUVC)|Device Plug-in for UVC Camera.|
|[dConnectDeviceTest](dConnectDevicePlugin/dConnectDeviceTest)|Device Plug-in for test of DeviceConnect.|
|[dConnectDevicePluginSDK](dConnectDevicePlugin/dConnectDevicePluginSDK)|SDK for creating DevicePlugin. dConnectSDKForAndroid necessary.|

## Device Connect Manager
| Project Name | Content  |
|:-----------|:---------|
|[dConnectManager](dConnectManager/dConnectManager)|Platform body of DeviceConnect.|
|[dConnectServer](dConnectManager/dConnectServer)|Server interface library of DeviceConnect.|
|[dConnectServerNanoHttpd](dConnectManager/dConnectServerNanoHttpd)|Server of the body of the DeviceConnect.|

## Device Connect SDK
| Project Name | Content |
|:-----------|:---------|
|[dConnectSDKForAndroid](dConnectSDK/dConnectSDKForAndroid)|SDK of DeviceConnect. DevicePlugin development and SDK to be used for application development.|

# Development of DeviceConnect app

Application and using the DeviceConnect, regard the development of the application, please refer to the following pages.

* [Application Development Manual](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/Application-Manual-for-Android-Studio)

If you want to develop a device plug-ins using the Device Connect Manager, please see this Application Development Manual.

* [Device Plug-in Development Manual](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/DevicePlugin-Manual-for-Android-Studio-200)

# Generate a Javadoc of Device Connect SDK
## Mac/Linux

```
$ cd DeviceConnect-Android/dConnectSDK/dConnectSDKForAndroid
$ ./gradlew generateJavadocForSDK
```

## Windows

```
> cd DeviceConnect-Android/dConnectSDK/dConnectSDKForAndroid
> gradlew.bat generateJavadocForSDK
```

`DeviceConnectSDK-Javadoc` is created in the directory where gradle is executed and Javadoc of Device Connect SDK is output.


# Generate a Javadoc of Device Connect Plug-in SDK
# Mac/Linux

```
$ cd DeviceConnect-Android/dConnectDevicePlugin/dConnectDevicePluginSDK
$ ./gradlew generateJavadocForPlugin
```

# Windows

```
> cd DeviceConnect-Android/dConnectSDK/dConnectSDKForAndroid
> gradlew.bat generateJavadocForPlugin
```

`DevicePluginSDK-Javadoc` is created in the directory where gradle was executed and Javadoc of Device Plugin SDK is output.

# Build Manuals
People who want to develop the DeviceConnectManager and device Plug-ins, please build in accordance with this build instructions.

* [DeviceConnectManager](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/DeviceConnectManager-Build)
* [ChromeCast](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/ChromeCast-Build)
* [FaBo](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/FaBo-Build)
* [HeartRate](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/HeartRateDevice-Build)
* [HOGP](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/HOGP-Build)
* [Host](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/Host-Build)
* [Hue](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/Hue-Build)
* [IRKit](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/IRKit-Build)
* [Linking](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/Linking-Build)
* [Theta](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/Theta-Build)
* [UVC](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/UVC-Build)
* [AndroidWear](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/AndroidWear-Build)
