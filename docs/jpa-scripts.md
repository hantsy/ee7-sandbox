# JPA 2.1 Schema generation properties

If you have some experience of Hibernate before, you must have used Hibernate *hibernate.hbm2ddl.auto* property to maintain the database schema for your project and used *import.sql* to initialize test data into the database.

Luckily this feature is standardized in JPA 2.1

JPA 2.1 added a series of *properties* for database schema maintenance and generation.

##Maintain database schema

Let's create a simple Entity as example.

<pre>
@Entity
@Table(name="POSTS")
public class Post implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="ID")
    private Long id;
    
    @Column(name="TITLE")
    private String title;
    
    @Column(name="BODY")
    private String body;
    
    @Temporal(javax.persistence.TemporalType.DATE)
    @Column(name="CREATED")
    private Date created;
}
</pre>

A simple `Post` class, includes three properties, title, body, created, and additional id.

An example of *persitence.xml*.

<pre>
&lt;?xml version="1.0" encoding="UTF-8"?>
&lt;persistence version="2.1" 
             xmlns="http://xmlns.jcp.org/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
             xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/persistence http://xmlns.jcp.org/xml/ns/persistence/persistence_2_1.xsd">
    &lt;persistence-unit name="postpu" transaction-type="JTA">
        &lt;properties>
            &lt;property name="javax.persistence.schema-generation.database.action" value="drop-and-create"/>
            &lt;property name="javax.persistence.schema-generation.create-source" value="script"/>
            &lt;property name="javax.persistence.schema-generation.drop-source" value="script"/>
            &lt;property name="javax.persistence.schema-generation.drop-script-source" value="META-INF/drop-script.sql"/>
            &lt;property name="javax.persistence.schema-generation.create-script-source" value="META-INF/create-script.sql"/>
            &lt;property name="javax.persistence.sql-load-script-source" value="META-INF/load-script.sql"/>
            &lt;property name="eclipselink.logging.level" value="FINE"/> 
        &lt;/properties>
    &lt;/persistence-unit>
&lt;/persistence>
</pre>

**javax.persistence.schema-generation.database.action** defines the strategy will perform on database artifacts(tables, constraints etc). The value options are none, create, drop, create-and-drop.

**javax.persistence.schema-generation.create-source** specifies how to create these artifacts, the value could be script, metadata, script-then-metadata, metadata-then-script, which indicates the artifacts will be created from scripts or entity metadata, or scripts firstly then metadata, or metadata firstly then scripts. If script is specified, you can define another **javax.persistence.schema-generation.create-script-source**  property to locate the script.

**javax.persistence.schema-generation.drop-source** and **javax.persistence.schema-generation.drop-script-source** are easy to understand, which define the strategy to drop the artifacts.

**javax.persistence.sql-load-script-source** will initialize some test data into the database.

The following are samples of the three scripts.

The content of *create-script.sql*.

<pre>
CREATE TABLE POSTS("ID" INTEGER NOT NULL PRIMARY KEY, "TITLE" VARCHAR(255), "BODY" VARCHAR(2000), "CREATED" DATE)
</pre>


The content of *drop-script.sql*.

<pre>
DROP TABLE POSTS
</pre>

The content of *load-script.sql*.

<pre>
INSERT INTO POSTS("ID", "TITLE", "BODY", "CREATED") VALUES (1, 'First Post', 'Body of first post', '2013-11-27')
INSERT INTO POSTS("ID", "TITLE", "BODY", "CREATED") VALUES (2, 'Second Post', 'Body of second post', '2013-11-27')
INSERT INTO POSTS("ID", "TITLE", "BODY", "CREATED") VALUES (3, 'Third Post', 'Body of third post', '2013-11-27')
</pre>

When you run this project on Glassfish 4, you will see the sample data is loaded successfully at runtime.

**NOTE**, you could notice we have not defined a DataSource in the *persistence.xml*. Java EE 7 specs added a series of **default** resources, including Jdbc, JMS etc. If there is no Jdbc DataSource specified, the application will search the default Jdbc DataSource via JNDI name *java:/comp/DefaultDataSource*, it should be provided in any Java EE certificated application servers.

In the above example we use scripts to manage the tables. If you prefer  models(aka metadata) to manage the database schema, there is an example.

<pre>
<?xml version="1.0" encoding="UTF-8"?>
&lt;persistence version="2.1" 
             xmlns="http://xmlns.jcp.org/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
             xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/persistence http://xmlns.jcp.org/xml/ns/persistence/persistence_2_1.xsd">
    &lt;persistence-unit name="postpu" transaction-type="JTA">
        &lt;properties>
            &lt;property name="javax.persistence.schema-generation.database.action" value="drop-and-create"/>
            &lt;property name="javax.persistence.schema-generation.create-source" value="metadata"/>
            &lt;property name="javax.persistence.schema-generation.drop-source" value="metadata"/>
            &lt;property name="javax.persistence.sql-load-script-source" value="META-INF/load-script.sql"/>
            &lt;property name="eclipselink.logging.level" value="FINE"/> 
        &lt;/properties>
    &lt;/persistence-unit>
&lt;/persistence>
</pre>

This example is similar with Hibernate drop-create strategy. 

## Generate database schema

JPA 2.1 provides capability to generate the existing database schema into external resource.

There is an example of the usage from EclipseLink project wiki.

<pre>
Map properties = new HashMap();
properties.put("javax.persistence.database-product-name", "Oracle");
properties.put("javax.persistence.database-major-version", 12);
properties.put("javax.persistence.database-minor-version", 1);
properties.put("javax.persistence.schema-generation.scripts.action", "drop-and-create");
properties.put("javax.persistence.schema-generation.scripts.drop-target", "jpa21-generate-schema-no-connection-drop.jdbc");
properties.put("javax.persistence.schema-generation.scripts.create-target", "jpa21-generate-schema-no-connection-create.jdbc");
    
Persistence.generateSchema("default", properties);
</pre>

Try it yourself

## Summary

This feature is good, but it lack an equivalent action to Hibernate *update* strategy. And the naming of these properties is very bad, I have to research the JPA document to differentiate their usage.

The sample codes are hosted on my github.com account, check out and play it yourself.

[https://github.com/hantsy/ee7-sandbox](https://github.com/hantsy/ee7-sandbox)

