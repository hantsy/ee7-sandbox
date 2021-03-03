#JSF 2.2: Theme Support#

[Richfaces](http://www.jboss.org/projects/richfaces) and [Primefaces](http://www.primefaces.org) have theme support, it is an attractive feature of the skin-able JSF components projects. 

JSF 2.2 introduces a new feature named *Resource Library Contracts* which supports to apply different resources(css and js) and facelets template at runtime.

*contracts* resources files can be placed in */contracts* folder under the web application root or */META-INF/contracts* on classpath.

## Using contracts in a web application

The following is an example of contracts resources structure in a web application.

<pre>
/contracts
	/default
		/css
			defautl.css
			cssLayout.css
		template.xhtml
	/alternative
		/css
			defautl.css
			cssLayout.css
		template.xhtml
</pre>

There are two *contracts* defined in the project, *default* and *alternative*.

The following is the content of */contracts/default/template.xhtml*.

<pre>
&lt;?xml version='1.0' encoding='UTF-8' ?>
&lt;!DOCTYPE html>

&lt;html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:ui="http://xmlns.jcp.org/jsf/facelets"
      xmlns:h="http://xmlns.jcp.org/jsf/html">
    
    &lt;h:head>
        &lt;meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        &lt;link href="#{request.contextPath}/contracts/default/css/default.css" rel="stylesheet" type="text/css" />
        &lt;link href="#{request.contextPath}/contracts/default/css/cssLayout.css" rel="stylesheet" type="text/css" />
        &lt;title>Facelets Template&lt;/title>
    &lt;/h:head>
    
    &lt;h:body>
        
        &lt;div id="top" class="top">
            &lt;ui:insert name="top">Top&lt;/ui:insert>
        &lt;/div>
        
        &lt;div id="content" class="center_content">
            &lt;ui:insert name="content">Content&lt;/ui:insert>
        &lt;/div>
        
    &lt;/h:body>
    
&lt;/html>
</pre>

The content of */contracts/alternative/template.xhtml* is similar, only the css reference path is changed.

<pre>
&lt;?xml version='1.0' encoding='UTF-8' ?>
&lt;!DOCTYPE html>
&lt;html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:ui="http://xmlns.jcp.org/jsf/facelets"
      xmlns:h="http://xmlns.jcp.org/jsf/html">
    
    &lt;h:head>
        &lt;meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        &lt;link href="#{request.contextPath}/contracts/alternative/css/default.css" rel="stylesheet" type="text/css" />
        &lt;link href="#{request.contextPath}/contracts/alternative/css/cssLayout.css" rel="stylesheet" type="text/css" />
        &lt;title>Facelets Template&lt;/title>
    &lt;/h:head>
    
    &lt;h:body>
        
        &lt;div id="top" class="top">
            &lt;ui:insert name="top">Top&lt;/ui:insert>
        &lt;/div>
        
        &lt;div id="content" class="center_content">
            &lt;ui:insert name="content">Content&lt;/ui:insert>
        &lt;/div>
        
    &lt;/h:body>
    
&lt;/html>
</pre>

In your facelets base template, you can use the *contracts* template directly. 

<pre>
&lt;ui:composition xmlns="http://www.w3.org/1999/xhtml"
      xmlns:h="http://xmlns.jcp.org/jsf/html" 
      xmlns:f="http://xmlns.jcp.org/jsf/core"
      xmlns:ui="http://xmlns.jcp.org/jsf/facelets">
        &lt;f:view contracts="alternative">
            &lt;ui:composition template="/template.xhtml"

                &lt;ui:define name="content">    
                    &lt;h1>Theme Switcher&lt;/h1>
                    &lt;p>Sample of applying alternative.&lt;/p>
                &lt;/ui:define>
            &lt;/ui:composition>
        &lt;/f:view>
&lt;/ui:composition>
</pre>

A new attribute *contracts* is added to *f:view* in JSF 2.2, which can specify the *contracts* name you will use, here the options are *default* and *alternative*.

*NOTE: The template path is /template.xhtml, and there is no any contract prefix needed.*

You can switch between different *contracts* dynamically through a backend bean.

<pre>
&lt;ui:composition xmlns="http://www.w3.org/1999/xhtml"
      xmlns:h="http://xmlns.jcp.org/jsf/html" 
      xmlns:f="http://xmlns.jcp.org/jsf/core"
      xmlns:ui="http://xmlns.jcp.org/jsf/facelets">
        &lt;f:view contracts="#{themeSwitcher.theme}">
            &lt;ui:composition template="/template.xhtml">
                &lt;ui:define name="content">    
                    &lt;h1>Theme Switcher&lt;/h1>
                    &lt;p>Current Theme:#{themeSwitcher.theme}&lt;/p>
                    &lt;h:form>
                        &lt;h:commandButton value="Default Theme" action="#{themeSwitcher.changeTheme('default')}">
                        &lt;/h:commandButton>    
                        &lt;h:commandButton value="Alternative Theme" action="#{themeSwitcher.changeTheme('alternative')}">
                        &lt;/h:commandButton>    
                    &lt;/h:form>
                &lt;/ui:define>
            &lt;/ui:composition>
        &lt;/f:view>
&lt;/ui:composition>
</pre>

In the template file, use two *h:commandButton* buttons to switch the *contracts* dynamically.

<pre>
@Named
@SessionScoped
public class ThemeSwitcher implements Serializable {

    @Inject
    transient Instance&lt;Logger> log;

    private String theme = "default";

    
    public void changeTheme( String _theme){
        log.get().log(Level.INFO, "call changeTheme{0}", _theme);
        switch (_theme) {
            case "alternative":
                this.theme = "alternative";
                break;
            default:
                this.theme = "default";
                break;
        }
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }
    
}
</pre>

You can also configure the *contracts* in *faces-config.xml* file.

<pre>
&lt;application>
        &lt;resource-library-contracts>
            &lt;contract-mapping>
                &lt;url-pattern>/themed-alt/*&lt;/url-pattern>
                &lt;contracts>alternative&lt;/contracts>
            &lt;/contract-mapping>
            &lt;contract-mapping>
                &lt;url-pattern>/themed-default/*&lt;/url-pattern>
                &lt;contracts>default&lt;/contracts>
            &lt;/contract-mapping>
        &lt;/resource-library-contracts>
&lt;/application>	
</pre>

The */themed-alt* will use the *alternative* and */themed-default* will use *default* contracts.

Unfortunately, JSF 2.2 does not support EL as the value of  *contracts* in *faces-config.xml*.

<pre>
&lt;contract-mapping>
	&lt;url-pattern>/themed-dyn/*&lt;/url-pattern>
	&lt;contracts>#{themeSwitcher.theme}&lt;/contracts>
&lt;/contract-mapping>
</pre>

This feature is motioned in [jdevelopment.nl website](http://jdevelopment.nl/jsf-22/), and could be supported in a future version.

## Packing contracts resources in a jar

Packaging the *resources library contracts* files into a jar file is easy to share the contracts among various projects.

Move the *contracts* resources into a standalone Maven module, and refactor the above war project into three Maven modules.

<pre>
theme-library
	/theme-client
	/theme-resources
</pre>

*theme-library* is a parent POM module, includes two sub modules, *theme-client* and *theme-resources*. 

*theme-resources* is a jar module, including all *resources library contracts files*.

<pre>
theme-resources
	/src
		/main
			/resoruces
				/META-INF
					/contracts
						/default
							/css
							template.xhtml
							javax.faces.contract.xml
						/alternative
							/css
							template.xhtml
							javax.faces.contract.xml
</pre>

*javax.faces.contract.xml* is an empty marker file, which is required to identify *resource library contracts* in a jar file.

*theme-client* is a war project, which will apply the *contracts* defined in *theme-resources* module. Include *theme-resources* as a Maven dependency in the pom.xml of *theme-client*.

<pre>
&lt;dependency>
            &lt;groupId>com.hantsylabs.example.ee7&lt;/groupId>
            &lt;artifactId>jsf-theme-resources&lt;/artifactId>
            &lt;version>1.0-SNAPSHOT&lt;/version>
&lt;/dependency>
</pre>

*NOTE: In the Glassfish 4(Mojarra is upgraded to 2.2.1), I have tried package the contracts files with and without an empty javax.faces.contract.xml. If packaging them without the empty javax.faces.contract.xml marker file, the predefined contracts can not be recognized when applying the contract rule in faces-config.xml, but they can be used in Themeswitcher backend bean. It looks a little weird.*

##Sample codes

*NOTE: In order to demonstrate the usage of Resources Library Contracts, I only changed the background color style in the css file. In the real world application, it could be much difference. For example, you can use different css and template layout before and after user is logged in.*

Check out the complete codes from my github.com, and play it yourself.

[https://github.com/hantsy/ee7-sandbox](https://github.com/hantsy/ee7-sandbox)
