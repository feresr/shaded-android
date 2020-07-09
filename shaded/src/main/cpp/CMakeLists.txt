cmake_minimum_required(VERSION 3.4.1)


file(GLOB src ${CMAKE_CURRENT_SOURCE_DIR}/src/*.cpp)
add_library( # Specifies the name of the library.
        shaded

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        ${src})

target_include_directories(shaded PUBLIC ${CMAKE_CURRENT_SOURCE_DIR}/include/)

target_link_libraries( # Specifies the target library.
        shaded
        android
        log
        GLESv3
        jnigraphics)
