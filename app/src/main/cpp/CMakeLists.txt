cmake_minimum_required(VERSION 3.22.1)
project(audioengine)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Oboe is consumed as a prebuilt prefab module (com.google.oboe:oboe AAR).
find_package(oboe REQUIRED CONFIG)

add_library(audioengine SHARED
    native-lib.cpp
    audio/AudioEngine.cpp
)

target_include_directories(audioengine PRIVATE ${CMAKE_CURRENT_SOURCE_DIR})

target_link_libraries(audioengine
    oboe::oboe
    android
    log
)
