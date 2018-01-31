<h1>Website monitoring</h1>

<p>
<a href="http://sitemonitoring.sourceforge.net/" target="_blank">website monitoring homepage with video tutorials!</a>
</p>

<h2>Key features</h2>

<ul>
	<li>User friendly</li>
	<li>Platform independent</li>
	<li>Runs as a standalone Java application</li>
	<li>Monitor single web page, pages in sitemap and even your whole web site using spider</li>
	<li>Check not only HTTP result codes, but also page contents; Can specify HTTP header for web page request;</li>
	<li>Find broken links on your website</li>
	<li>Microservice (REST APIs) monitoring - XML (using XPath and XSD) and JSON (using JSONPath)</li>
	<li>Periodic checking (built-in cron mechanism)</li>
	<li>Email notifications</li>
	<li>Statistics</li>
</ul>

<p>To login use these credentials: <code>username = admin, password = admin</code></p>

<h2>Standalone application (with HSQL database)</h2>

<p>Just download latest <a href="https://github.com/jirkapinkas/sitemonitoring-production/releases" target="_blank">ZIP file</a>. 
Extract and run: <code>startup.bat</code> (Linux and Mac are also supported)</p>
</p>

<p>Note for developers: to build the application run: <code>mvn clean package</code>

<p>How to build binary distribution: <code>mvn clean install</code></p>

<h2>Development (with embedded HSQL database)</h2>

<p>To run in development mode run this class: <code>net.sf.sitemonitoring.Main</code> <br />
with these VM arguments: <code>-Djava.io.tmpdir=sitemonitoring-temp -Dspring.profiles.active=dev -Ddbport=9001 -Dserver.port=8081</code></p>

<h2>My server:</h2>

build:

`mvn clean package -P myserver`

run (with postgresql server):

`java -jar -Dlog4j.debug -Dlog4j.configuration=file:/hosting/sitemonitoring/log4j-standalone.properties -Dspring.profiles.active=myserver sitemonitoring.war --server.port=TODO_PORT --spring.datasource.url=jdbc:postgresql://localhost:5432/TODO_DB_NAME --spring.datasource.username=TODO_USERNAME --spring.datasource.password=TODO_PASSWORD`
