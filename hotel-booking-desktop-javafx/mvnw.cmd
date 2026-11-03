@echo off
if not exist "%JAVA_HOME%\bin\java.exe" (
    if exist "C:\Program Files\Java\jdk-26.0.2\bin\java.exe" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-26.0.2"
    ) else if exist "C:\Program Files\Java\latest\bin\java.exe" (
        set "JAVA_HOME=C:\Program Files\Java\latest"
    )
)
call "C:\Users\patte\.m2\wrapper\dists\apache-maven-3.9.16\0daed3be3ebd1c706f0e69e8b07c6b73f5cc4ea3dfce72a8d0ec2e849ca2ddb0\bin\mvn.cmd" %*

