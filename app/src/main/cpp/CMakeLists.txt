cmake_minimum_required(VERSION 3.10.2)

project("opengl")

include_directories(
        util
        render
        sample
        glm
        inc
        inc/assimp
        model
        opencv_3_0_0
)
file(GLOB src-files
        ${CMAKE_SOURCE_DIR}/*.cpp
        ${CMAKE_SOURCE_DIR}/render/*.cpp
        ${CMAKE_SOURCE_DIR}/util/*.cpp
        ${CMAKE_SOURCE_DIR}/sample/*.cpp)

add_library( # Sets the name of the library.
        native-render

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        ${src-files}
        )

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log )

add_library(assimp SHARED IMPORTED )
set_target_properties(assimp PROPERTIES IMPORTED_LOCATION ${jnilibs}/${ANDROID_ABI}/libassimp.so)

add_library(opencv_java3 SHARED IMPORTED )
set_target_properties(opencv_java3 PROPERTIES IMPORTED_LOCATION ${jnilibs}/${ANDROID_ABI}/libopencv_java3.so)


#ASAN 需要 NDK20 编译，Android P 以上运行
#target_compile_options(native-render PUBLIC -fsanitize=address -fno-omit-frame-pointer)
#set_target_properties(native-render PROPERTIES LINK_FLAGS -fsanitize=address)

#SET(CMAKE_CXX_FLAGS  "${CMAKE_CXX_FLAGS} -fno-rtti -fno-exceptions")
set(third-party-libs
        assimp
        opencv_java3)

target_link_libraries( # Specifies the target library.
        native-render

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib}
        ${third-party-libs}
        EGL
        GLESv3
        )