@echo off
SET tomcatpackage="C:\Program Files\Apache Software Foundation\Tomcat 9.0\webapps\invoicebinder.war"
SET buildpackage="C:\Users\mon2\Projects\InvoiceBinder\invoicebinder\target"
echo "Cleanup...."
del %tomcatpackage% | more
echo "Cleanup Completed...."
ping localhost -n 10
echo f | xcopy /f /y %buildpackage% %tomcatpackage% | more
echo "Deploy Completed...."
