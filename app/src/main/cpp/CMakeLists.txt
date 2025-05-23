# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html.
# For more examples on how to use CMake, see https://github.com/android/ndk-samples.

# Sets the minimum CMake version required for this project.
cmake_minimum_required(VERSION 3.22.1)

# Declares the project name. The project name can be accessed via ${ PROJECT_NAME},
# Since this is the top level CMakeLists.txt, the project name is also accessible
# with ${CMAKE_PROJECT_NAME} (both CMake variables are in-sync within the top level
# build script scope).
project("motophosaique")

file(GLOB SOURCE_FILES
        "*.cpp" "*.hpp"
)

add_library( photomosaic-lib
        SHARED
        ${SOURCE_FILES} )

target_include_directories( photomosaic-lib
        PRIVATE ${CMAKE_CURRENT_SOURCE_DIR} )

find_library( log-lib log )

target_link_libraries( photomosaic-lib
        ${log-lib} )
