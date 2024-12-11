<h1>Installation Instructions</h1>

This is a Maven project, so you will need Maven installed on your machine. 
Follow this link to download and install Maven : https://maven.apache.org/download.cgi

You will also need to set your MAVEN_HOME variable in your global environment, and add the Maven ./bin file to your PATH variable.

Once you have your Maven environment set up, run `Server`. 
You must set you server host IP on line 30:

  `sock.bind(new InetSocketAddress("<host IP>", DEFAULT_PORT));`
  
  `"<host IP>"` can be set to `"localhost"` or your network IP address if you want other users on the network to link to your server.


Now, run your `ChatScreen` client program via the terminal. The `pom.xml` has 3 predefined client profiles that have a username and host IP. Be sure this IP parameter matches your server IP. 

Here are the terminal commands for the 3 profiles:

`mvn -Pclient-1 exec:java -Dexec.mainClass=com.networks.ChatScreen`<br>
`mvn -Pclient-2 exec:java -Dexec.mainClass=com.networks.ChatScreen`<br>
`mvn -Pclient-3 exec:java -Dexec.mainClass=com.networks.ChatScreen`

<h2>Troubleshooting</h2>

If you are having issue running this project in Maven, be sure to check the following:<br>

1: Is Maven installed on your computer and global variables set?<br>
2: Is your IDE throwing any configuration warnings?<br>
3: Did you bind the correct IP address to the server socket?<br>
4: Are the client profiles properly configured in `pom.xml`?<br>

Also, use the Maven lifecycle to perform clean install/compile, either through your IDE or with the follwing commands:<br>

`mvn clean install`<br>
`mvn clean compile`<br>
