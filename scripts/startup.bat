@echo off
rem -Xmx512m ~ Maximum available memory
rem -Dserver.port=8081 ~ On which port will the application run
rem -Ddbport=9001 ~ Application uses database, which runs on this port
rem don't change anything else

java -jar -Djava.io.tmpdir=sitemonitoring-temp -Dspring.profiles.active=standalone -Ddbport=9001 -Dserver.port=8081 -Xmx512m sitemonitoring.war