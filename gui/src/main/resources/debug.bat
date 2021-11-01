rem Debug script for Windows OS

%JAVA_HOME%\bin\java.exe -jar ${project.build.finalName}.jar -Xdebug -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=0.0.0.0:5005
