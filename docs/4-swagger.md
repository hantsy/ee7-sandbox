# Visualize APIs with Swagger UI

I have motioned in the API design section of before posts, there are some debate on **Contract First** and **Code First**.

Originally Swagger provides capability of visualizing APIs from codes, it is **Code First**. 

Swagger is evolved, in the version 2.0, it provides **Contract First** capability, you can design the APIs in **Swagger Spec** json file, and generate API skeleton codes and API document from this file.

There are some competitor in the API design area.

* [RAML(RESTful API Modeling Language)](http://raml.org)
* [Open API](https://openapis.org/)

All of above provide visual editor to compose API details.

## Swagger Spec

Swagger provides standard suppprt for Jaxrs, you can integrate Swagger Jaxrs into your Jaxrs project and generate Swagger Spec at runtime. Swagger UI is a static UI for visualizing APIs according to the Swagger Spec.

Spring also has a variant named [SpringFox](http://springfox.org) to support this feature.

Alternatively, you can generate a copy of Swagger Spec from the codes, and use Swagger UI to visualize it. The benifit is the swagger.json is generated at compile time. 

There are some maven plugins support this feature.

* [Kongchen's swagger-maven-plugin](https://github.com/kongchen/swagger-maven-plugin)
* [sdaschner's jaxrs-analyzer-maven-plugin](https://github.com/sdaschner/jaxrs-analyzer-maven-plugin)

In this sample, I used the `jaxrs-analyzer-maven-plugin` to generate the static swagger.json file and then use **Swagger UI** to visualize the result.

Why I do not integrate the Swagger Jaxrs into the project directly and let it generate swagger.json at runtime? Swagger Jaxrs has a bit more dependencies, and I got bad performance after integrated it in before experience.

## Integrate Swagger

Add a new profile in the `pom.xml`.

	<profile>
		<id>swagger</id>
		<build>
			<plugins>
				<plugin>
					<groupId>org.apache.maven.plugins</groupId>
					<artifactId>maven-war-plugin</artifactId>
					<configuration>
						<webResources>
							<resource>
								<!-- this is relative to the pom.xml directory -->
								<directory>${project.build.directory}/jaxrs-analyzer</directory>
							</resource>
						</webResources>
					</configuration>
				</plugin>
				<plugin>
					<groupId>com.sebastian-daschner</groupId>
					<artifactId>jaxrs-analyzer-maven-plugin</artifactId>
					<version>0.9</version>
					<executions>
						<execution>
							<goals>
								<goal>analyze-jaxrs</goal>
							</goals>
							<configuration>
								<!-- Available backends are plaintext (default), swagger and asciidoc -->
								<backend>swagger</backend>
								<!-- Domain of the deployed project, defaults to example.com -->
								<deployedDomain>localhost</deployedDomain>
							</configuration>
						</execution>
					</executions>
				</plugin>
			</plugins>
		</build>
		<dependencies>
			<dependency>
				<groupId>org.webjars</groupId>
				<artifactId>swagger-ui</artifactId>
				<version>2.1.2</version>
			</dependency>
		</dependencies>
	</profile>

1. There are three backend engine in the `jaxrs-analyzer-maven-plugin`, plaintext(it is the default value), swagger, asciidoc, they can be used to generate the APIs in plain text, swagger.json, and asciidoc format.
2. I add **${project.build.directory}/jaxrs-analyzer** as web resource folder, thus the **swagger.json** file will be packaged into the war root folder.
3. **swagger-ui** is added as a dependency, it is a webjars sepecific jar and satify the Servlet 3.0 resource convention, which can be recognized by container directly. 

Compile and package the project codes with **swagger** profile.

	mvn clean package -Pdefault,swagger
	
It will package a deployment archive(war) in the target folder.

Drop it into a Wildly deployment folder, when Wildfly starts, it will be deployed automaticially.

Alternatively, you can use wildfly maven plugin to deploy it directly.

	mvn clean wildfly:deploy -Pdefault,swagger
	
Please go to [Wildfly maven plugin](https://docs.jboss.org/wildfly/plugins/maven/latest/) website for more details.

Open browser, navigate to [http://localhost:8080/blog-api/webjars/swagger-ui/2.1.2/index.html](http://localhost:8080/blog-api/webjars/swagger-ui/2.1.2/index.html).

In the explore locaiton, input the value **http://localhost:8080/blog-api/swagger.json**, and click the explore.

You will see the following screen.

![Swagger UI](https://github.com/hantsy/angularjs-ee7-sample/wiki/swagger-ui.png)

Open [Swagger online editor](http://editor.swagger.io), import the swagger.json, it tranforms the file to YAML format in editor, and you can modify it and preview the generated document.

![Swagger IO editor](https://github.com/hantsy/angularjs-ee7-sample/wiki/swagger-io.png)
 
In this sample, the maven plugin does not recognize the return type of the post list. I added `PostDetails` definition in the editor, you can see the result immediately in the preview panel.
