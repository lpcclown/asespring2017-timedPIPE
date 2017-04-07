SET SPEC=%1

gcc %SPEC% -I".\z3win32\include" -L".\z3win32\bin" -o"Z3Checker.exe" -lz3

start Z3Checker.exe 