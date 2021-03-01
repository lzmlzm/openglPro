IF(NOT EXISTS "C:/Users/nxf65025/AndroidStudioProjects/openglPro/app/.cxx/cmake/debug/x86/Assimp/install_manifest.txt")
  MESSAGE(FATAL_ERROR "Cannot find install manifest: \"C:/Users/nxf65025/AndroidStudioProjects/openglPro/app/.cxx/cmake/debug/x86/Assimp/install_manifest.txt\"")
ENDIF(NOT EXISTS "C:/Users/nxf65025/AndroidStudioProjects/openglPro/app/.cxx/cmake/debug/x86/Assimp/install_manifest.txt")

FILE(READ "C:/Users/nxf65025/AndroidStudioProjects/openglPro/app/.cxx/cmake/debug/x86/Assimp/install_manifest.txt" files)
STRING(REGEX REPLACE "\n" ";" files "${files}")
FOREACH(file ${files})
  MESSAGE(STATUS "Uninstalling \"$ENV{DESTDIR}${file}\"")
  EXEC_PROGRAM(
    "C:/Users/nxf65025/AppData/Local/Android/Sdk/cmake/3.10.2.4988404/bin/cmake.exe" ARGS "-E remove \"$ENV{DESTDIR}${file}\""
    OUTPUT_VARIABLE rm_out
    RETURN_VALUE rm_retval
    )
  IF(NOT "${rm_retval}" STREQUAL 0)
    MESSAGE(FATAL_ERROR "Problem when removing \"$ENV{DESTDIR}${file}\"")
  ENDIF(NOT "${rm_retval}" STREQUAL 0)
ENDFOREACH(file)
