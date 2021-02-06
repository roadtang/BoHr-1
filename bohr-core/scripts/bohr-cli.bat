@echo off

set java_bin=.\jvm\bin\java

%java_bin% -cp bohr.jar org.bohr.JvmOptions --cli > jvm_options.txt
set /p jvm_options=<jvm_options.txt

%java_bin% %jvm_options% -cp bohr.jar org.bohr.Main --cli %*
