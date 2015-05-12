<h1>Website monitoring</h1>

<p>
<a href="http://sitemonitoring.sourceforge.net/" target="_blank">website monitoring homepage with video tutorials!</a>
</p>

<h2>Key features</h2>

<ul>
	<li>User friendly</li>
	<li>Platform independent</li>
	<li>Can run standalone or on your custom Java EE server and your database</li>
	<li>Monitor single web page, pages in sitemap and even your whole web site using spider</li>
	<li>Check not only HTTP result codes, but also page contents</li>
	<li>Find broken links on your website</li>
	<li>Microservice (REST APIs) monitoring - XML (using XPath) and JSON (using JSONPath)</li>
	<li>Periodic checking (built-in cron mechanism)</li>
	<li>Email notifications</li>
	<li>Statistics</li>
</ul>

<p>To login use these credentials: <code>username = admin, password = admin</code></p>

<h2>Standalone application (with HSQL database)</h2>

<p>Just download latest <a href="http://sourceforge.net/projects/sitemonitoring/files/Site%20monitoring%202.0/" target="_blank">ZIP file</a>.
<br />
Extract and run: <code>startup.bat</code> (Linux is also supported)</p>
</p>

<p>OR download sources and run: <code>mvn clean install -P standalone</code>
<br />
Next run: <code>java -jar target/sitemonitoring.war</code></p>

<h2>Standard WAR file (with your custom database)</h2>

<p>
Set database properties in <code>src/main/resources/db.properties</code>, add to classpath JDBC driver and finally run:<code>mvn clean package -P war</code>. Out of the box will be used HSQL embedded database (to validate functionality). Supported databases are MySQL, PostgreSQL and Oracle. See video tutorial how to use it.
</p>

<h2>Development (with embedded HSQL database)</h2>

<p>To run in development mode: <code>mvn jetty:run -P dev</code></p>

