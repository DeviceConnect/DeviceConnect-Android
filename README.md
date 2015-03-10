# DeviceConnect-Android
* 日本語説明はこちら
https://github.com/DeviceConnect/DeviceConnect-Android/blob/master/readme.ja.md

# About DeviceConnect WebAPI
"DeviceConnect WebAPI" is WebAPI which operates as a virtual server on a smart phone. It can use easily various wearable devices and an IoT device by unific description from a web browser or an application. 

# About DeviceConnect Android


Device Connect WebAPI in WebAPI which operates as a virtual server on the smartphone, it can be easy to use in a uniform description of various wearable devices and IoT devices from a Web browser and apps.
Device Connect Android will be the platform of DeviceConnect of Android version.

In this guide I will continue to discuss the following.

* [Project description](#section1)
* [Installation of project](#section2)
* [Start of DeviceConnectManager](#section3)
* [Operation check](#section4)
* [Development of DeviceConnect app](#section5)



# <a name="section1">Project description</a>
## dConnectDevicePlugin
| Project Name|Content  |
|:-----------|:---------|
| dConnectDeviceChromeCast|Device plug-in Chromecast. |
| dConnectDeviceHue|Device plug-in Hue.|
|dConnectDeviceIRKit|Device plug-in IRKit.|
|dConnectDevicePebble|Device plug-in Pebble.|
|dConnectDeviceSonyCamera|Device plug-ins SonyCamera such as QX10.|
|dConnectDeviceSonySW|SonySmartWatch device plug-ins. <br>※SmartWatch3 is not supported.|
|dConnectDeviceSphero|Device plug-in Sphero.|
|dConnectDeviceWear|Device plug-in AndroidWear.|
|dConnectDeviceHost|Device plug-in for Android.|
|dConnectDeviceTest|Device plug-in for test of DeviceConnect.|
|dConnectDevicePluginSDK|SDK for creating DevicePlugin. dConnectSDKForAndroid necessary.|


## dConnectManager
| プロジェクト名|内容  |
|:-----------|:---------|
|dConnectManager| Platform body of DeviceConnect.|
|dConnectManagerTest|Test of DeviceConnectManager.|
|dConnectServer|Server interface library of DeviceConnect.|
|dConnectServerNanoHttpd|Server of the body of the DeviceConnect.|

## dConnectSDK
| プロジェクト名|内容  |
|:-----------|:---------|
|dConnectApp| DeviceConnect operation check for app.|
|dConnectSDKForAndroid| SDK of DeviceConnect. DevicePlugin development and SDK to be used for application development.|


# <a name="section2"> Installation of the application</a>
People who want to develop the DeviceConnectManager and device plug-ins, please build in accordance with this build instructions.

* [DeviceConnectManager](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/2.1.DeviceConnectManager)
* [ChromeCast](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/2.2.ChromeCast)
* [Host](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/2.3.Host)
* [Hue](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/2.4.Hue)
* [IRKit](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/2.5.IRKit)
* [Pebble](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/2.6.Pebble)
* [SonyCamera](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/2.7.SonyCamera)
* [SonySW](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/2.8.SonySW)
* [Sphero](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/2.9.Sphero)
* [AndroidWear](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/2.10.AndroidWear)

　In addition, you can get the apk from [here] (https://github.com/DeviceConnect/DeviceConnect-Docs/tree/master/Bin/Android).
　With regard to the installation method, you can run from the command line of the command as follows.<br>
　Here, I will install only the Device Manager body and HOST device plug-ins.
　Because there are other devices plug-in, please install it if necessary.

```
　adb install dConnectManager.apk
　adb install dConnectDeviceHost.apk
```

With regard to the adb command, please make sure the Android SDK.

　

# <a name="section3">Start of DeviceConnectManager</a>
 Please tap the DeviceConnectManager icon.
 <center><a href="https://raw.githubusercontent.com/wiki/DeviceConnect/DeviceConnect-Android/DevicePluginManual/image35.png" >
<img src="https://raw.githubusercontent.com/wiki/DeviceConnect/DeviceConnect-Android/DevicePluginManual/image35.png" border="0"
  width="151" height="212" alt="" /></a><br></center>

By making it ON the field of DeviceConnectManager, you can start.

<center><a href="https://raw.githubusercontent.com/wiki/DeviceConnect/DeviceConnect-Android/DevicePluginManual/image39.png" >
<img src="https://raw.githubusercontent.com/wiki/DeviceConnect/DeviceConnect-Android/DevicePluginManual/image39.png" border="0"
 width="320" height="550" alt="" /></a></center>
 
 
 
# <a name="section4">Operation check</a>

The address of the Chrome browser, "http://localhost:4035/gotapi/availability" Please enter.
If Kore response is returned in the following, such as JSON, I can confirm that DeviceConnect is running.<br>
<br>
 
 <center><a href="https://raw.githubusercontent.com/wiki/DeviceConnect/DeviceConnect-Android/imageX.PNG" target="_blank">
<img src="https://raw.githubusercontent.com/wiki/DeviceConnect/DeviceConnect-Android/imageX.PNG" border="0"
 width="320" height="550" alt="" /></a></center>
 
 
 Request
 
 ```
 GET http://localhost:4035/gotapi/availability
 ```
 
 Response
 
 ```
 {
     "product":"Device Connect Manager",
     "version":"x.x",
     "result":0,
}
 ```
 
 
 
The API of the non-availability, basically you will not be able to check easily is the address of the chrome browser in order to access token is required.
Regard specific use of Device Connect, please refer to us the Wiki of [here] (https://github.com/DeviceConnect/DeviceConnect-Android/wiki/1.1.1.Application-Manual).


# <a name="section5">Development of DeviceConnect app</a>
Application and using the DeviceConnect, regard the development of the application, please refer to the following pages.
 
 * [Application Development Manual(Eclipse)](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/1.1.1.Application-Manual)
 * [Application Development Manual(AndroidStudio)](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/1.2.1.Application-Manual)

<br>
If you want to develop a device plug-ins using the Device Connect Manager, please see this Application Development Manual.
 
 * [Device plug-in development manual(Eclipse)](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/1.1.2.DevicePlugin-Manual)
 * [Device plug-in development manual(AndroidStudio)](https://github.com/DeviceConnect/DeviceConnect-Android/wiki/1.2.2.DevicePlugin-Manual)<br>

