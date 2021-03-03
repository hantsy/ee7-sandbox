# Create a Java EE project from Maven archetype

Apache Maven officially provides a few templates to create general purpose Java EE projects, in the Maven world, they are named **archetype**. There are several archetypes under group **org.codehaus.mojo.archetypes** can be used to create EAR, appclient, or simple web applications. 

The following is an example of creating a Java EE 7 web applicaiton.

```
mvn archetype:generate
-DarchetypeGroupId=org.codehaus.mojo.archetypes 
-DarchetypeArtifactId=webapp-javaee7 
-DarchetypeVersion=1.0
-DgroupId=com.hantsylab.sample.ee7api
-DartifactId=ee7-api 
-Dversion=0.1-SNAPSHOT
-Dpackage=com.hantsylab.sample.ee7api 
```
    
Read [this article](https://dzone.com/articles/java-ee-7-maven-archetype) from DZone for more details.	

NetBeans IDE provides built-in support for these archetypes when create a Maven based Java EE project from **New Project** wizard.

[Wildfly](http://www.wildfly.org) also provide a few archetype which are ready for Java EE 7 and Wildfly application server.

Most of time, I used Wildfly as target application server in projects. In the following sample I will use the archetype **wildfly-javaee7-webapp-blank-archetype** under group id **org.wildfly.archetype** to create the project skeleton.

## Create project

Open your terminal, use command `mvn archetype:generate` to generate the project skeleton.

```
mvn archetype:generate 
-Darchetype.interactive=false 
-DarchetypeGroupId=org.wildfly.archetype 
-DarchetypeArtifactId=wildfly-javaee7-webapp-blank-archetype 
-DarchetypeVersion=8.2.0.Final 
-DgroupId=com.hantsylab.example.ee7 
-DartifactId=blog-api
-Dversion=0.1-SNAPSHOT 
-Dpackage=com.hantsylab.example.ee7 
```

Before you run the command, you can browse **org.wildfly.archtype** in [mvnrepository.org](http://mvnrepository.com/artifact/org.wildfly.archetype).

Luckily, almost all of the popular IDEs, such as Intellij IDEA, NetBeans, JBoss Developer Studio have provided friendly wizards to create project from Maven archetypes step by step.

## Explore the generated codes

Import the project you have just created into JBoss Developer Studio.	

Open **Package** view, you will see the following project structure.

![empty java ee project](https://github.com/hantsy/angularjs-ee7-sample/wiki/mvn-javaee7.png)

Beside the *pom.xml* file, it just includes some specific files for different Java EE specification.

1. **src/main/resources/META-INF/persistence.xml** is the JPA configuration file.
2. **src/main/webapp/WEB-INF/beans.xml** is the default CDI configuration file. **NOTE: in Java EE 7, CDI is enabled by default. So beans.xml is not required to activiate it when use CDI in Java EE 7.**.
3. **src/main/webapp/WEB-INF/blog-api-ds.xml** is a Wildfly specific DataSource definition file. NOTE: the postfix **-ds** in the name is a must, else Wildfly can not detect it when deploy it into the server.
4. **src/main/webapp/WEB-INF/faces-config.xml** is the JSF configuration file. But we will focus on JAXRS to implement RESTful APIs in this sample, so this file is no use and can be deleted.

Open the **pom.xml** file.

Unlike the former posts which used JBoss Enterprise repository by default, in this project, we generated the project skeleton from a wildfly specific maven archetype, all dependencies are Wildfly specific. 

1. **wildfly-maven-plugin** allows you deploy and undeploy via maven command line.

	```xml
	<plugin>
		<groupId>org.wildfly.plugins</groupId>
		<artifactId>wildfly-maven-plugin</artifactId>
		<version>${version.wildfly.maven.plugin}</version>
	</plugin>
	```

2. There are several maven profiles defined. **arq-wildfly-remote** and **arq-wildfly-managed** are ready for JBoss Arquillian test. **openshift** for OpenShift Cloud deployment.

	```xml
	<profiles>
		<profile>
			 <id>arq-wildfly-managed</id>
			 ...
		</profile>
		<profile>
			 <id>arq-wildfly-remote</id>
			 ...
		</profile>
		<profile>
			 <id>openshift</id>
			 ...
		</profile>
	</profiles>
	```
		
The difference between **arq-wildfly-remote** and **arq-wildfly-managed** is the former requires a running Wildfly server before run the test codes, and the later Arquillian will manage the Wildfly itself. 		
		
## Update to Wildfly 10

We will use the latest Wildfly, aka 10.0.0.Final. We have to the dependencies to Wildfly 10.0.0.Final.

Some changes you have to be careful about.

The Wildfly Arquillian dependency groupId was changed to `org.wildfly.arquillian`.

Add the latest **Arquillian BOM** in the dependencyManagement fragment, and change the **arq-wildfly-managed** profile to the following.

```xml
<profile>
	<!-- An optional Arquillian testing profile that executes tests in your 
	WildFly instance -->
	<!-- This profile will start a new WildFly instance, and execute the test, 
	shutting it down when done -->
	<!-- Run with: mvn clean test -Parq-wildfly-managed -->
	<id>arq-wildfly-managed</id>
	<properties>
		<serverProfile>standalone-full.xml</serverProfile>
		<serverRoot>${project.build.directory}/wildfly-${version.wildfly}</serverRoot>
	</properties>
	<dependencies>
		<dependency>
			<groupId>org.wildfly.arquillian</groupId>
			<artifactId>wildfly-arquillian-container-managed</artifactId>
			<version>${version.org.wildfly.arquillian}</version>
			<scope>test</scope>
		</dependency>
	</dependencies>
	<build>
		<plugins>
			<plugin>
				<artifactId>maven-dependency-plugin</artifactId>
				<version>2.8</version>
				<configuration>
					<skip>${maven.test.skip}</skip>
				</configuration>
				<executions>
					<execution>
						<id>unpack</id>
						<phase>process-test-classes</phase>
						<goals>
							<goal>unpack</goal>
						</goals>
						<configuration>
							<artifactItems>
								<artifactItem>
									<groupId>org.wildfly</groupId>
									<artifactId>wildfly-dist</artifactId>
									<version>${version.wildfly}</version>
									<type>zip</type>
									<overWrite>false</overWrite>
									<outputDirectory>${project.build.directory}</outputDirectory>
								</artifactItem>
							</artifactItems>
						</configuration>
					</execution>
				</executions>
			</plugin>
			<plugin>
				<artifactId>maven-surefire-plugin</artifactId>
				<version>2.17</version>
				<configuration>
					<environmentVariables>
						<JBOSS_HOME>${project.build.directory}/wildfly-${version.wildfly}</JBOSS_HOME>
					</environmentVariables>
				</configuration>
			</plugin>
		</plugins>
	</build>
</profile>
```

It will download Wildfly 10 and start it before run a test, and stop it after the test running is complete. No need an external Wilfly server.

## Deploy and run

The wildfly maven plugin is available in pom.xml, you can use it to deploy, undeploy this project into our Wildfly application server.

1. Make sure your Wildfly application server is running.
2. Execute `mvn wildfly:deploy -DskipTests` to deploy the project into Wildfly application server.
3. You can run `mvn wildfly:undeploy` to undeploy it.
	
In the next steps, I will try to create the similar APIs in [angularjs-springmvc-sample-boot](https://github.com/hantsy/angularjs-springmvc-sample-boot), but use Java EE 7/Wildfly/JAXRS to implement it. As the angularjs-springmvc-sample-boot, the test codes will be available. We will use Wildfly as default server, and use JBoss Arquillian to test the Java EE components in Wildfly container.

	