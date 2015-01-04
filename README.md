<h1>Website monitoring</h1>

<p>
<a href="http://sitemonitoring.sourceforge.net/" target="_blank">website monitoring homepage</a> SOON WITH TUTORIALS HOW TO USE THE APPLICATION.
</p>

<p>To login use <code>username / password: admin / admin</code></p>

<h2>Standalone application (with HSQL or your custom database)</h2>

<p>Just download <a href="https://sourceforge.net/projects/sitemonitoring/files/latest/download?source=files" target="_blank">compiled file</a>
<br />
Next run: <code>java -jar sitemonitoring.jar</code></p>
</p>

<p>OR run: <code>mvn clean package -P standalone</code>
<br />
Next run: <code>java -jar target/sitemonitoring.war</code></p>

<h2>Standard WAR file (with your custom database)</h2>

<p>
Set database properties in <code>src/main/resources/db.properties</code>, add to classpath JDBC driver and finally run:<code>mvn clean package -P war</code>. Out of the box will be used HSQL embedded database (to validate functionality).
</p>

<h2>Development (with embedded HSQL database)</h2>

<p>To run in development mode: <code>mvn jetty:run -P dev</code></p>

<h2>Heroku (with PostgreSQL database)</h2>

<p>To deploy on Heroku change in pom.xml: <code>&lt;argument&gt;sitemonitoring&lt;/argument&gt;</code> change "sitemonitoring" to your application name in Heroku and run: <code>mvn clean install -P heroku</code>

<p>How to create Heroku application and install Heroku toolbelt is in this <a href="http://www.javavids.com/video/spring-web-app-tutorial-50-heroku.html" target="_blank">video</a></p>

