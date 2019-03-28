# Sets the minimum version of CMake required to build your native library.
# This ensures that a certain set of CMake features is available to
# your build.

cmake_minimum_required(VERSION 3.4.1)

# Specifies a library name, specifies whether the library is STATIC or
# SHARED, and provides relative paths to the source code. You can
# define multiple libraries by adding multiple add.library() commands,
# and CMake builds them for you. When you build your app, Gradle
# automatically packages shared libraries with your APK.

include_directories(src/main/jni)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O2 -Wall")

add_library( # Specifies the name of the library.
             rtp-native-lib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             src/main/jni/rtp-native-lib.cpp
             src/main/jni/rtp-socket.c
             src/main/jni/rtp-jpeg-packetize.c
             src/main/jni/rtp-h264-packetize.c
             src/main/jni/rtcp-socket.c)

find_library( # Defines the name of the path variable that stores the
              # location of the NDK library.
              log-lib

              # Specifies the name of the NDK library that
              # CMake needs to locate.
              log)


# Links your native library against one or more other native libraries.
target_link_libraries( # Specifies the target library.
                       rtp-native-lib

                       # Links the log library to the target library.
                       jnigraphics
                       ${log-lib})

