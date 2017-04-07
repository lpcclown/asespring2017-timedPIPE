SET SPEC=%1

spin -a %SPEC%

gcc -DMEMLIM=1024 -O2 -DXUSAFE -w -o pan pan.c

start /wait .\pan -m10000  -a -N f

spin -t -r -s %SPEC%>SpinOutput.txt

