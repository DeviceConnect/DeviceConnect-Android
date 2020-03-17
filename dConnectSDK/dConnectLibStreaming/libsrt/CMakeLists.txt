# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# リリースビルドに設定しておく
set(CMAKE_BUILD_TYPE Release)

# srt のライブラリへのパス
set(SRT_ROOT ${CMAKE_CURRENT_SOURCE_DIR}/src/main/jniLibs)

# srt の各 so ファイルへのパス
set(SRT_LIBS ${SRT_ROOT}/${ANDROID_ABI})

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library(
        # Sets the name of the library.
        srt-native-interface

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        src/main/cpp/srt-native-interface.cpp)

add_library(srt SHARED IMPORTED)
set_target_properties(srt
        PROPERTIES IMPORTED_LOCATION
        ${SRT_LIBS}/libsrt.so)

target_include_directories(srt-native-interface
        PRIVATE ${SRT_ROOT}/include)

find_library(
        # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries(
        # Specifies the target library.
        srt-native-interface

        srt

        -ljnigraphics
        -landroid

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})