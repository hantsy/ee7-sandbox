#JSF 2.2: Flow#


JSF 2.2 introduces a standard *flow* concept. The flow concept have been existed in several web solutions for several years, such as [Spring Web Flow](http://www.springsource.org/webflow) and [Seam 2 page flow](http://www.seamfamework.org).

##An introduction

The flow definition follows the convention over configuration.

For example, in order to define a flow named *flow1*, you could create a folder named *flow1* in the web root to group all views in this flow. By default, you must provide a view which name is same with the flow name as start node. In this example, it is *flow1.xhtml*. In order to start the flow, you should specify the value of h:commandButton or h:commandLink same as the flow name, it will activate the *Window Context* support(by default it is disabled) and search the matched flow and navigate the start node of the flow.

<pre>
/flow1
	/flow1.xhtml
	/flow1a.xhtml
	/flow1b.xhtml
</pre>

Besides the view files, a related flow description is required.

There are two approaches to define a flow.

1. Create a CDI bean and use *@Produces* to produces a *Flow*.
 
 <pre>
 @Produces @FlowDefinition
 public Flow defineFlow(@FlowBuilderParameter FlowBuilder flowBuilder) {
	String flowId = "flow1";
	flowBuilder.id("", flowId);
	flowBuilder.viewNode(flowId, "/" + flowId + "/" + flowId + ".xhtml").markAsStartNode();

	flowBuilder.returnNode("taskFlowReturn1").
	fromOutcome("#{flow1Bean.returnValue}");

	flowBuilder.inboundParameter("param1FromFlow2", "#{flowScope.param1Value}");
	flowBuilder.inboundParameter("param2FromFlow2", "#{flowScope.param2Value}");

	flowBuilder.flowCallNode("call2").flowReference("", "flow2").
	outboundParameter("param1FromFlow1", "param1 flow1 value").
	outboundParameter("param2FromFlow1", "param2 flow1 value");

	return flowBuilder.getFlow();
 }
 </pre>

 *Personally, I can not understand why I have to add a @FlowDefintion annotation with @Produces on the method and a @FlowBuilderParameter annotation on the argument *FlowBuilder*.

2. Create a &lt;flowName>-flow.xml in the flow node folder to describe the flow.

 <pre>
 &lt;faces-config version="2.2" xmlns="http://xmlns.jcp.org/xml/ns/javaee"
	      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	      xsi:schemaLocation="
	      http://xmlns.jcp.org/xml/ns/javaee
	      http://xmlns.jcp.org/xml/ns/javaee/web-facesconfig_2_2.xsd">

    &lt;flow-definition id="flow2">
	&lt;flow-return id="taskFlowReturn1">
	    &lt;from-outcome>#{flow2Bean.returnValue}&lt;/from-outcome>
	&lt;/flow-return>

	&lt;inbound-parameter>
	    &lt;name>param1FromFlow1&lt;/name>
	    &lt;value>#{flowScope.param1Value}&lt;/value>
	&lt;/inbound-parameter>
	&lt;inbound-parameter>
	    &lt;name>param2FromFlow1&lt;/name>
	    &lt;value>#{flowScope.param2Value}&lt;/value>
	&lt;/inbound-parameter>

	&lt;flow-call id="callFlow1">
	    &lt;flow-reference>
		&lt;flow-id>flow1&lt;/flow-id>
	    &lt;/flow-reference>
	    &lt;outbound-parameter>
		&lt;name>param1FromFlow2&lt;/name>
		&lt;value>param1 flow2 value&lt;/value>
	    &lt;/outbound-parameter>
	    &lt;outbound-parameter>
		&lt;name>param2FromFlow2&lt;/name>
		&lt;value>param2 flow2 value&lt;/value>
	    &lt;/outbound-parameter>
	&lt;/flow-call>
    &lt;/flow-definition>
 &lt;/faces-config>
 </pre>

 This is a standard faces-config.xml.


The above two forms of the flow definition are equivalent. The Java class version just translate the XML one.

Let's have a look at the basic concept in the flow definition.

* **flow-return** specifies an outcome that is returned to the calling flow
* **inbound-parameter** defines the parameters passed from another flow
* **outbound-parameter** defines the parameters should be passed to another flow
* **flow-call** call another flow in the current flow
* **method-call** execute a method and could perform navigation
* **view** the regular view document
* **switch** provides a list of EL as switch cases, and evaluate them one by one, and turn the first node when the evaluation result is `true`, else go to the next switch case.

There is an implicit object named *flowScope*(which indicates the current flow) which can be used as a container to share data through the whole flow.

For example, the value of the *h:inputText* component can be put into the *flowScope*.  

<pre>
&lt;h:form prependId="false">
	&lt;h1>Second Page in Flow 1&lt;/h1>
	&lt;p>Flow bean name: #{flow1Bean.name}&lt;/p>

	&lt;p>value: &lt;h:inputText id="input" value="#{flowScope.value}" />&lt;/p>

	&lt;p>&lt;h:commandButton id="start" value="back" action="flow1" />&lt;/p>
	&lt;p>&lt;h:commandButton id="flow1b" value="next" action="flow1b" />&lt;/p>
	&lt;p>&lt;h:commandButton id="index" value="home" action="taskFlowReturn1" />&lt;/p>
&lt;/h:form>
</pre>

And in the next view, the input value can be fetched from EL *#{flowScope.value}*.

<pre>
&lt;p>value: #{flowScope.value}&lt;/p>
</pre>

Besides use *flowScope* to store data in the flow, you can define a *@FlowScoped* bean for this purpose. *@FlowScoped* is a CDI compatible scope.

<pre>
@Named
@FlowScoped("flow1")
public class Flow1Bean implements Serializable {

    public String getName() {
        return this.getClass().getSimpleName();
    }

    public String getReturnValue() {
        return "/return1";
    }
}
</pre>

The *@FlowScoped* bean accepts a *value* attribute, it should be the flow name which it will be used in.

Either *flowScope* or *@FlowScoped* bean, they should be destroyed when leaves the flow.

*I found all none flow command actions did not work when entered a flow, such as  `<h:commandButton value="/nonFlow"/>` in the above example. If it is designated as expected, it is terrible in the real projects. Imagine there are hundreds of h:commandLink used in a facelets view outside of a flow, when the flow is activated, all none flow links will be blind links.* 

**I reported this as an issue, but it was rejected by JSF Team immediately, after a long and difficult communication with the JSF guys, it is finally marked as a bug and had been fixed in the Majorra 2.2.3.**

## Registration flow: an example

In the real world application, a registration progress can be split into two parts, registration and activation by the link in email.

Two flows can be defined, *registration* and the followed *activation*.

<pre>
/registration
	/registration-flow.xml
	/registraion.xhtml
	/account.xhtml
	/confirm.xhtml
/activation
	/activation-flow.xml
	/activation.xhtml
	/ok.xhtml
</pre>

Besides the web resources, two *@FlowScoped* beans are declared to process the logic the associate flow.

The *registration* includes three steps.

*registration.xhtml* contains license info and  a checkbox, user has to accept the license and moves forward.

<pre>
&lt;h:form prependId="false">	
	&lt;h:messages globalOnly="true" showDetail="false" showSummary="true"/>
	&lt;p>LICENSE CONTENT HERE&lt;/p>
	&lt;p>&lt;h:selectBooleanCheckbox id="input" value="#{registrationBean.licenseAccepted}" />&lt;/p>
	&lt;p>&lt;h:commandButton id="next" value="next" action="#{registrationBean.accept}" />&lt;/p>
&lt;/h:form>
</pre>

*account.xhtml* includes the basic info of an user account.

<pre>
&lt;h:form prependId="false">
	&lt;h:messages globalOnly="true" showDetail="false" showSummary="true"/>
	&lt;p>First Name: &lt;h:inputText id="firstName" value="#{registrationBean.user.firstName}" />&lt;h:message for="firstName"/>&lt;/p>
	&lt;p>Last Name: &lt;h:inputText id="lastName" value="#{registrationBean.user.lastName}" />&lt;h:message for="lastName"/>&lt;/p>
	&lt;p>Email: &lt;h:inputText id="email" value="#{registrationBean.user.email}" />&lt;h:message for="email"/>&lt;/p>
	&lt;p>Password: &lt;h:inputSecret id="password" value="#{registrationBean.user.password}" />&lt;h:message for="password"/>&lt;/p>
	&lt;p>Confirm Password: &lt;h:inputSecret id="passwordConfirm" value="#{registrationBean.passwordConfirm}" />&lt;/p>

	&lt;p>&lt;h:commandButton id="pre" value="back" action="registration" immediate="true"/>&lt;/p>
	&lt;p>&lt;h:commandButton id="next" value="next" action="#{registrationBean.confirm}" />&lt;/p>
&lt;/h:form>
</pre>

*confirm.xhtml* is a basic summary of the registration.

<pre>
&lt;h:form prependId="false">
	&lt;h:messages globalOnly="true" showDetail="false" showSummary="true"/>
	&lt;p>User info #{registrationBean.user}&lt;/p>
	&lt;p>&lt;h:commandButton id="back" value="back" action="account"/>&lt;/p>
	&lt;p>&lt;h:commandButton id="callActivationFlow" value="Enter Activation Flow" action="callActivationFlow" />&lt;/p>
&lt;/h:form>
</pre>

A *RegistrationBean* is provided to process the registration logic, such as check if the checkbox is checked in the first step, and check the email existence in the second step.

<pre>
Named
@FlowScoped("registration")
public class RegistrationBean implements Serializable {

    @Inject
    transient Logger log;

    @Inject
    UserService userService;

    private boolean licenseAccepted = false;

    private User user = null;

    private String passwordConfirm;

    @PostConstruct
    public void init() {
        log.info("call init...");
        user = new User();
    }

    public boolean isLicenseAccepted() {
        return licenseAccepted;
    }

    public void setLicenseAccepted(boolean licenseAccepted) {
        this.licenseAccepted = licenseAccepted;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getPasswordConfirm() {
        return passwordConfirm;
    }

    public void setPasswordConfirm(String passwordConfirm) {
        this.passwordConfirm = passwordConfirm;
    }

    public String getName() {
        return this.getClass().getSimpleName();
    }

    public String getReturnValue() {
        return "/return1";
    }

    public String accept() {
        log.info("call accept...");
        if (this.licenseAccepted) {
            return "account";
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "You have to read and accept the license!", "You have to read and accept the license!"));
            return null;
        }
    }

    public String confirm() {
        log.info("call confirm...");
        
        if (!user.getPassword().equals(passwordConfirm)) {
            FacesContext.getCurrentInstance().addMessage("password", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Password is mismatched!", "Password is mismatched!"));
            return null;
        }
        
        if (userService.findByEmail(this.user.getEmail()) != null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "User " + this.user.getEmail() + " is existed!", "User " + this.user.getEmail() + " is existed!"));
            return null;
        }

        userService.save(this.user);
        
        return "confirm";
    }
}
</pre>

A dummy *UserService* is also provided for storing the *User* data into and fetch the data from "database"(I used a *Map* for test purpose).

<pre>
@ApplicationScoped
@Named
public class UserService implements Serializable{
    
    @Inject transient Logger log;
   
    private Map&lt;String, User> users=new HashMap&lt;String, User>();
    
    public void save(User user){
        log.info("user list size@"+users.size());
        log.info("saving user...");
        users.put(user.getEmail(), user);
    }
    
    public User findByEmail(String email){
        log.info("user list size@"+users.size());
        log.info("find by email...@"+email);
        return users.get(email);
    }
    
    public List&lt;User> getUserList(){
        log.info("user list size@"+users.size());
        return new ArrayList(this.users.values());
    }
}
</pre>

The *registration-flow.xml* defines a *flow-call* to call the *activation* flow. In the *confirm.xhtml* view there is a h:commandButton available for this purpose.

<pre>
&lt;flow-call id="callActivationFlow">
	&lt;flow-reference>
		&lt;flow-id>activation&lt;/flow-id>
	&lt;/flow-reference>
	&lt;outbound-parameter>
		&lt;name>email&lt;/name>
		&lt;value>#{registrationBean.user.email}&lt;/value>
	&lt;/outbound-parameter>
&lt;/flow-call>
</pre>

The *activation* flow is simple and stupid.

It inclues a *activaton* page and successful page(the view *ok*).

The backend bean *ActivationBean* provides a basic dummy logic to check the activation token value and change the *activated* property to *true* of the user and store the activated date. 

<pre>
@Named
@FlowScoped("activation")
public class ActivationBean implements Serializable {

    @Inject
    Logger log;

    @Inject
    UserService userService;

    private String email;
    private String tokenValue;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTokenValue() {
        return tokenValue;
    }

    public void setTokenValue(String tokenValue) {
        this.tokenValue = tokenValue;
    }

    public String getName() {
        return this.getClass().getSimpleName();
    }

    public String getReturnValue() {
        return "/return1";
    }

    public String activate() {
        log.info("call activate....");

        User user = userService.findByEmail(this.email);
        if (user == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "User " + this.getEmail() + " not found!", "User " + this.getEmail() + " not found!"));
            return null;
        }

        if ("123".equals(this.tokenValue)) {
            user.setActivated(true);
            user.setActivatedOn(new Date());
            userService.save(user);
            return "ok";
        }

        return null;
    }
}
</pre>

##Packaging flow resources in a Jar

Like the *resources*, *resource library contracts*, in JSF 2.2, the flow resources are allowed to be packaged in a jar.

Let's refactory the registration example. Move the flow resource into the */META-INF/flows* in the jar root.

<pre>
/META-INF
	/flows
		/registration
			registration.xhtml
			account.xhtml
			confirm.xhtml
		/activation
			activation.xhtml
			ok.xhtml
	faces-config.xml
	beans.xml
</pre>

All related classes files are also packaged in the jar. A  *beans.xml* in the */META-INF* folder is required to activate CDI beans. 

Unlike the flows in web application, all flow definitions are moved to *faces-config.xml*. It is a must, or the flows do not work as expected. **Yeah, it is a weird design. Scanning the flow definition files in the /META-INF/flows is so difficult?**.

The content of *faces-config.xml* looks similar with the following.

<pre>
&lt;?xml version='1.0' encoding='UTF-8'?>
&lt;faces-config version="2.2"
              xmlns="http://xmlns.jcp.org/xml/ns/javaee"
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-facesconfig_2_2.xsd">
    &lt;lifecycle>
        &lt;phase-listener>com.hantsylabs.example.ee7.jsf.DebugPhaseListener&lt;/phase-listener>
    &lt;/lifecycle>
    
    &lt;flow-definition id="registration">
        &lt;flow-return id="taskFlowReturn1">
            &lt;from-outcome>#{registrationBean.returnValue}&lt;/from-outcome>
        &lt;/flow-return>

        &lt;flow-call id="callActivationFlow">
            &lt;flow-reference>
                &lt;flow-id>activation&lt;/flow-id>
            &lt;/flow-reference>
            &lt;outbound-parameter>
                &lt;name>email&lt;/name>
                &lt;value>#{registrationBean.user.email}&lt;/value>
            &lt;/outbound-parameter>
        &lt;/flow-call>
    &lt;/flow-definition>
    &lt;flow-definition id="activation">
        &lt;inbound-parameter>
            &lt;name>email&lt;/name>
            &lt;value>#{activationBean.email}&lt;/value>
        &lt;/inbound-parameter>
        &lt;flow-return id="taskFlowReturn1">
            &lt;from-outcome>#{activationBean.returnValue}&lt;/from-outcome>
        &lt;/flow-return>
    &lt;/flow-definition>
&lt;/faces-config>
</pre>

In fact, I do not think the flows packaged in a jar is useful in the real projects, it is very difficult to be reused as *resources*.

## Summary

I have used [Spring Web Flow](http://www.springsource.org/webflow) and [Seam 2 page flow](http://www.seamfamework.org) in before projects.

Spring Web Flow supports Spring MVC and JSF, the flow definition is written in XML format. JBoss Seam 2 provides JBoss JBPM 3 based page flow(XML format) and long run conversation bean, I like the later.

Compare the new JSF 2.2 Faces Flow and Spring Web Flow, Faces Flow lacks lots of features provided in Spring Web Flow.

1. Spring Web Flow provides extended persistence support in the whole flow, so you never worry about the LazyInitializationException thrown by Hibernate.
2. Spring Web Flow supports events on enter and exit point in every node.
3. Spring Web Flow supports Ajax, thus the page fragment can participate into the flow.
4. Spring Web Flow supports subflows in the main flow, and allows you share the data state between flows directly. The inbound-parameter/outbound-parameter in the Faces Flow is very stupid and ugly.
5. Spring Web Flow is designated to remove any backend beans, and use flow to hold the state, and use stateless Spring bean to sync data with database. 

In JBoss Seam 2, the most attractive feature is it's long run conversation. Compare Faces Flow, it has some advantage.

1. *@Begin* and *@End* are provided to declare the boundaries of a conversation, no need extra flow definition.
2. Multi propagate types are provided to enter a conversation, such as *new*, *nest*, *join* etc.
3. Conversation scoped persistence is provided, so you never worry about the classic LazyInitializationException in the conversation scope.
4. It supports manual flush, and transaction can be committed or rollback int the last exit node.
5. It supports nested conversation which behaves as the subflow in Spring Web Flow.

Personally, the new Faces Flow is neither good nor bad, and I am bitterly disappointed with it. **JSF EG declared it was inspired by ADF Task Flow and Spring Web Flow, but it seems they have just copied ADF Task Flow and made it standardized.**

1. The elements of the flow definition are tedious and not clear as the Spring Web Flow.
2. The command action, especially, h:commandButton and h:commandLink became difficult to understand, the *action* value could be an implicit outcome, a flow name, a method call id, a flow call id etc.
3. The Java based flow definition(@FlowDefinition and FlowBuilder) is every ugly, just converts the xml tag. It is not fluent API at all.
4. The @FlowScoped bean does not work as the Seam 2 conversation scoped bean and can not group the facelets views freely without a flow definition.

Personally, I would like stay on Spring Web Flow if JSF 2.2 is chosen in Spring based projects. For Java EE 6/7 application, I hope [Apache DeltaSpike project](http://deltaspike.apache.org) could import *Conversation* feature from [Apache MyFaces CODI](http://myfaces.apache.org/extensions/cdi/) as soon as possible.

##Sample codes

Check out the complete codes from my github.com, and play it yourself.

[https://github.com/hantsy/ee7-sandbox](https://github.com/hantsy/ee7-sandbox)
