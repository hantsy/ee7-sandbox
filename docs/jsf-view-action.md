#JSF 2.2: View Action#

JSF 2.2 introduced a new *view action* feature, which had been existed in JBoss Seam 2 and Seam 3 for a long time.

In fact, JSF 2.2 copied the Seam 3 *view action* exactly.


##An example

An example is better than thousands of words.

<pre>
@Model
public class ViewActionBean {
    
    @Inject Logger log;
    
    private String flag="page1";
    
    public String init(){
        log.info("call init");
        switch(flag){
            case "page1":
                return "page1";
            default:
                return "page2";  
        } 
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }
    
}

</pre>

Create a facelets view and use a view action to invoke the *init* method.

<pre>
&lt;!DOCTYPE html>
&lt;html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:h="http://xmlns.jcp.org/jsf/html" 
      xmlns:f="http://xmlns.jcp.org/jsf/core">
    &lt;f:view>
        &lt;f:metadata>
            &lt;f:viewParam name="flag" value="#{viewActionBean.flag}"/>
            &lt;f:viewAction action="#{viewActionBean.init()}"/>
        &lt;/f:metadata>
        &lt;h:head>
            &lt;title>View Action&lt;/title>
            &lt;meta name="viewport" content="width=device-width" />
        &lt;/h:head>
        &lt;h:body>
            &lt;p> View Action&lt;/p>
        &lt;/h:body>
    &lt;/f:view>
&lt;/html>
</pre>

I created two empty views, page1.xhtml and page2.xhtml for test purpose.

Run this project on Glassfish4, go to [http://localhost:8080/ee7-sandbox/viewAction.faces](http://localhost:8080/ee7-sandbox/viewAction.faces). It will switch to "page1", and if you append a *flag=page2* parameter, [http://localhost:8080/ee7-sandbox/viewAction.faces?flag=page2](http://localhost:8080/ee7-sandbox/viewAction.faces?flag=page2), it will redirect to page2 view.

We have *preRenderView* event listener to load resource for view, why we need view action?

As you see, there are some difference between view action and *preRenderView* event listener, view action has some advantage which is not available in *preRenderView* event listener.

1. view action is a generic JSF action, it has navigation capability, *preRenderView* has not.

 In *preRenderView* listener method, you have to use JSF API to navigate to a different view explicitly.
 
 <pre>
 if("page2".equals(flag)){
            ConfigurableNavigationHandler handler=(ConfigurableNavigationHandler)  
                    FacesContext.getCurrentInstance().getApplication().getNavigationHandler();      
            handler.performNavigation("page2");
 }
 </pre>

2. by default, view action will not be executed on *postback* phase, but  in *preRenderView* event listener you have to code and overcome it by the following like code in your method.

 <pre>
  if(!FacesContext.getCurrentInstance().isPostback()){
  
  }
 </pre>

3. By default the *view action* will be executed in *INVOKE_APPLICATION* phase, it has a *immediate* attribute as other actions in JSF, if it is true it can provides a shortcut of the JSF request lifecycle and executes the action method in *APPLY_REQUEST_VALUES* phase.

4. view action also provides a *phase* attribute which can specify the JSF phase you are going to execute the action.

## Before JSF 2.2

Before JSF 2.2, you have some alternatives for the view action.

1. Seam 3 Faces invented view action, of course it provides the view action feature. 

2. [Prettyfaces](http://ocpsoft.org/prettyfaces/) also provides similar features, which is called url action.

 <pre>
 &lt;url-mapping id="viewAction" outbound="true">
 	&lt;pattern value="/viewAction" />
 	&lt;view-id value="/viewAction.xhtml" />
 	&lt;action>#{viewActionBean.init()}&lt;/action>
 &lt;/url-mapping>
 </pre>

 By default, the action is executed after *RESTORE_VIEW* and before *APPLY_REQUEST_VALUES*, which is slightly different from the view action.


## The sample codes

The sample codes are hosted on my github.com account, check out and play it yourself.

[https://github.com/hantsy/ee7-sandbox](https://github.com/hantsy/ee7-sandbox)

I assume you have installed [Glassfish 4](http://glassfish.java.net) and the latest [Oracle Java 7](http://java.oracle.com) and [NetBeans IDE](http://www.netbeans.org) 7.4 which has excellent Java EE 7 development support.

*NOTE: There is a known issue in the JSF reference implementation Mojarra 2.2.0, which can not recognize the newest Facelets taglib namespace(http://xmlns.jcp.org/jsf) correctly when using f:viewAction and f:viewParam . This issue is fixed in Mojarra 2.2.1, you can get a new copy from Maven central repository or [Glassfish website](http://glassfish.java.net), and replace the existed one in Glassfish.*
