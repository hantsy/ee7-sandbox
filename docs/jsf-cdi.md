#JSF 2.2: Embrace CDI?#

CDI 1.0 is part of Java EE 6, it is the standard Dependency Injection specification for Java EE platform. But unfortunately, JSF 2.0 which is also part of Java EE 6 did not adopt CDI as its Dependency Injection/IOC container, but invented its IOC container.


##Dependency injection in JSF 2.0

In the package *javax.faces.bean*, there are several annotations provided.

You can annotate your JSF backend bean with annotation *@ManagedBean* and put it in a reasonable scope( *@ApplicationScoped*, *@RequestScoped*, *@SessionScoped*, *@ViewScoped*, *@NoneSopced*). 

<pre>
@ManagedBean(name="foo")
@RequestScoped
public class FooBean{

}
</pre>

The attribute *name* specified in the *@ManagedBean* can be accessed via EL in facelets views.

You can inject it in other beans via *@ManagedProperty*. 

<pre>
@ManagedBean(name="bar")
@RequestScoped
public class BarBean{
	@ManagedPropertiy("foo")
	FooBean foo;
}
</pre>

In JSF 2.0, it also supports to annotate the fields with @Resource and @EJB annotations to inject Java EE resources and EJB. 

<pre>
@ManagedBean(name="bar")
@RequestScoped
public class BarBean{
	@Resource("")
	DataSource ds;
	
	@EJB
	HelloEJB hello;
}
</pre>

## CDI 1.0 and JSF 2.0

When you are using Java EE 6, you maybe have confused about the JSF IOC and CDI, we need two types of IOC mechanism in projects?

CDI provides a complete list of scopes for Java EE platform, including *@ApplicationScoped*, *@RequestScoped*, *@SessionScoped*, *@ConversationScoped*, which are provided in the package *javax.enterprise.context*. As you see, they are very similar with JSF scopes. The difference is JSF has a specific *@ViewScoped* and there is no equivalent concept in CDI, and CDI provides a *@ConversationScoped* which is also designated for JSF in CDI 1.0.

But due to some unknown reason, JSF EG refused to reuse CDI in Java EE 6. It was explained the JSF 2.0 features had been frozen and would not be changed, even there were some months left before Java EE 6 was released. <del>The most reasonable explanation is that it was the result of political fight between the Java EE giants, such as Oracle, RedHat etc. </del> (This is only my personal idea, it maybe result in confusion).

Luckily, CDI beans can be accessed through EL in faceslet views directly. And some third party projects such as [JBoss Seam 3](http://seamframework.org/), [Apache MyFaces CODI](http://myfaces.apache.org/extensions/cdi/index.html) etc are born and try to fill the gap between CDI and JSF. In JBoss Seam 3, you can use CDI beans freely as JSF backend beans. Apache MyFaces CODI provides more, it mapped all JSF scopes to CDI scopes internally which allow you use JSF scopes but it is processed by CDI container, and redesigned a new powerful Conversation which support conversation group, multi conversation, nested conversation etc, and provides some extra scopes, such as *@ViewAccessScoped*, Window scope etc.


##CDI 1.1 and JSF 2.2

In Java EE 7, the JSF specific *ManagedBean* specification is discontinued. 

In JSF 2.2, all JSF scopes defined in JSF 2.0(in the package *javax.faces.bean*) should be abandoned, you can use CDI beans as JSF backend beans freely. JSF 2.2 also introduces two new CDI compatible scopes, *@ViewScoped* and *@FlowScoped*. 

NOTE, the new *@ViewScoped* is located in the package *javax.faces.view*, it is a new annotation.

*@FlowScoped* is designated for another new feature shipped with JSF 2.2, the Faces Flow support.

Almost all JSF artifacts support *@Inject* now. For example, the *DebugPhaseListener* provided in the sample codes is a custom *PhaseListener* which is use for tracking the JSF request lifecycle.

<pre>
public class DebugPhaseListener implements PhaseListener{
    
    @Inject Logger log;

    @Override
    public void afterPhase(PhaseEvent event) {
       log.info(" afer phase @"+ event.getPhaseId());
    }

    @Override
    public void beforePhase(PhaseEvent event) {
       log.info( " before phase @"+ event.getPhaseId());
    }

    @Override
    public PhaseId getPhaseId() {
       return PhaseId.ANY_PHASE;
    }
    
}
</pre>

But unfortunately, there are some exceptions, such as JSF converter and validator still do not support *@Inject*, which are canceled in the last milestone before JSF 2.2 was finalized. 

And many features I expected are not provided in JSF 2.2, such as bridge the JSF system events to CDI events, expose the JSF FacesContext related resources as CDI beans and allow you inject them directly instead of programmatic code.

**We have to wait another 4 years?**




