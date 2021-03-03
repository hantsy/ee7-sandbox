# JPA 2.1: Attribute Converter

If you are using Hibernate, and want a customized type is supported in  your Entity class, you could have to write a custom Hibernate Type.

JPA 2.1 brings a new feature named attribute converter, which can help you convert your custom class type to JPA supported type.

## Create an Entity 

Reuse the `Post` entity class as example.

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

    @Column(name="TAGS")
    private List&lt;String> tags=new ArrayList&lt;>();
}
</pre>


## Create an attribute converter

In this example, we try to store the tags property into one column instead of an external table.


Create an attribute converter, it should be annotated with `@Converter` and implements `AttributeConverter` interface.

<pre>
@Converter
public class ListToStringConveter implements AttributeConverter&lt;List&lt;String>, String> {

    @Override
    public String convertToDatabaseColumn(List&lt;String> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "";
        }
        return StringUtils.join(attribute, ",");
    }

    @Override
    public List&lt;String> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().length() == 0) {
            return new ArrayList&lt;String>();
        }

        String[] data = dbData.split(",");
        return Arrays.asList(data);
    }
}
</pre>

It is easy to understand, the tags property will be converted into a comma based string when it is stored into database, and tags field value of *POSTS* table will be converted into a List when it is fetched from database.

## Apply Converter

You can use the *autoApply* attribute of the `Converter` to apply the converter to any supported type.

<pre>
@Converter(autoApply=true)
public class ListToStringConveter implements AttributeConverter&lt;List&lt;String>, String> {...}
</pre>

It is dangerous in a real world project when there are some List you do not want to be converted.

Alternatively, you can apply it on the property via a `@Convert` annotation.

<pre>
@Column(name="TAGS")
@Convert(converter = ListToStringConveter.class)
private List&lt;String> tags=new ArrayList&lt;>();
</pre>

You can also place it on class.

<pre>
@Converts(value={
    @Convert(attributeName="tags", converter = ListToStringConveter.class)
})
public class Post implements Serializable {...}
</pre>

An extra *attributeName* must be specified. You can declare several Converters for properties on an Entity class.

Converters also can be applied on:

1. @Embeddable key of a *OneToMany* Map type property.
2. @Embeded property
3. @ElementCollection property


## Summary

This feature is very useful when you want use a JPA supported type to store your custom class, especially, convert an unsupported type to JPA support type, for example, such as Joda Datetime/Java 8 new Date objects are not supported in JPA 2.1 yet, you can use a converter to convert it to java.util.Date type which is supported by JPA.

The sample codes are hosted on my github.com account, check out and play it yourself.

[https://github.com/hantsy/ee7-sandbox](https://github.com/hantsy/ee7-sandbox)

When you run the project(jpa-converter) on Glassfish 4.0 and you could get an exception.

<pre>
java.lang.RuntimeException: unable to create policy context directory.
</pre>

There is a known issue in Glassfish 4.0, the fix should be included in the next release. I am using a [Nightly version](http://dlc.sun.com.edgesuite.net/glassfish/4.0.1/) to overcome this barrier temporarily.
