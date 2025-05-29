cmake_minimum_required(VERSION 3.22.1)
project("motophosaique")

# 明确地列出所有 .cpp
add_library( photomosaic-lib
        SHARED
        photomosaic-lib.cpp
        moyen.cpp
        histo.cpp
        distribution.cpp
        moyen_cou.cpp
        histo_cou.cpp
        distribution_cou.cpp
        moyen_cou_cheat.cpp
        moyen_cheat.cpp
)

target_include_directories( photomosaic-lib
        PRIVATE ${CMAKE_CURRENT_SOURCE_DIR}
        ${CMAKE_CURRENT_SOURCE_DIR}/third_party/stb
)

target_include_directories( photomosaic-lib
        PRIVATE ${CMAKE_CURRENT_SOURCE_DIR} )

find_library( log-lib log )

target_link_libraries( photomosaic-lib
        ${log-lib} )
