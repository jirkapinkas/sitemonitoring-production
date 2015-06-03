
****************************************************************
Make sure you run all bat files with administrator privileges!!!
****************************************************************

How to install Sitemonitoring as a Windows service:

1. register-service.bat
2. start-service.bat

Sitemonitoring will start running automatically when you start 
your computer. This can be changed in service.ini.

How to uninstall Sitemonitoring:

1. stop-service.bat
2. unregister-service.bat

NOTE: Running sitemonitoring as a Windows service won't 
automatically start your browser! You need to manually open 
browser on your own!

Customizing input parameters (currently http and database 
port) is done in service.ini, not in startup.bat!!!