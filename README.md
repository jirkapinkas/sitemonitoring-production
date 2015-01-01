<h1>Website monitoring</h1>

<h2>Development</h2>

<p>To run in development mode: <code>mvn jetty:run -P dev</code></p>

<h2>Demo</h2>

<p>To create standalone demo file: <code>mvn clean package -P demo</code> Next run: <code>java -jar target/sitemonitoring.war</code></p>

<p>Or just download <a href="https://sourceforge.net/projects/sitemonitoring/files/latest/download?source=files">compiled file</a></p>

<h2>Heroku</h2>

<p>To deploy on Heroku change in pom.xml: <code>&lt;argument&gt;sitemonitoring&lt;/argument&gt;</code> "sitemonitoring" to your application name in Heroku and run: <code>mvn clean install -P heroku</code>

