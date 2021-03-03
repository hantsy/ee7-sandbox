#JSF 2.2: File Upload#


Before 2.2, you have to use [Richfaces](http://www.jboss.org/projects/richfaces), [Primefaces](http://www.primefaces.org) like JSF components to provide file upload feature.

Or you must write a custom file upload component yourself.

Luckily this long waiting feature was finally added in JSF 2.2.

##An example

In JSF 2.2, the *FacesServlet* is annotated with *@MultipartConfig*, which causes JSF can handle multpart form data correctly.

<pre>
@MultipartConfig
public final class FacesServlet implements Servlet {}
</pre>

The following is a simple facelets page, which includes a h:form and h:inputFile components.

<pre>
&lt;h:form enctype="multipart/form-data">
    &lt;h:inputFile id="file" value="#{fileUploadBean.file}" />
    &lt;h:commandButton value="Upload" action="#{fileUploadBean.upload()}"/>
&lt;/h:form>
</pre>

The new *inputFile* component is provided for file upload purpose. The usage is simple. Like other input components, put it into a h:form,  the *enctype* attribute must be changed to *multipart/form-data* explicitly , it is a must for file upload.

<pre>
@Model
public class FileUploadBean {
    
    @Inject Logger log;
    
    private Part file;
    
    public void upload(){
        log.info("call upload...");      
        log.log(Level.INFO, "content-type:{0}", file.getContentType());
        log.log(Level.INFO, "filename:{0}", file.getName());
        log.log(Level.INFO, "submitted filename:{0}", file.getSubmittedFileName());
        log.log(Level.INFO, "size:{0}", file.getSize());
        try {
            
            byte[] results=new byte[(int)file.getSize()];
            InputStream in=file.getInputStream();
            in.read(results);         
        } catch (IOException ex) {
           log.log(Level.SEVERE, " ex @{0}", ex);
        }
        
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Uploaded!"));
    }

    public Part getFile() {
        return file;
    }

    public void setFile(Part file) {
        this.file = file;
    }
    
}
</pre>

In the backend bean, use a standard Servlet 3 *Part* as converted type to warp the uploaded file info.

It is easy to read the uploaded file info from *Part* API.

NOTE: The *getName* method will return the client id, and *getSubmittedFileName* returns the original file name.

Uploaded file content can be read through the *getInputStream* method of *Part*.

##Add a validator

As an example, a FileValidator is added to limit the uploaded file size.

<pre>
@FacesValidator
public class FileValidator implements Validator {

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        Part part = (Part) value;
        if(part.getSize()>1024){
           throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "file is too large", "file is too large"));
        }
    }

}
</pre>

This is a standard JSF validator, which prevents a large-sized file to be uploaded. In the above example, if the size of the file to be uploaded is greater than 1024 bytes, a validation error will be displayed when file is being uploaded.

Update the faceslet page, apply the validtor to the inputFile component.

<pre>
&lt;h:inputFile id="file" value="#{fileUploadBean.file}" >
	&lt;f:validator validatorId="fileValidator"/>
&lt;/h:inputFile>
&lt;h:message for="file"/>
</pre>

When the form is submitted, if the file size is greater than 1024 bytes, an error will be displayed near the input file field.

As you see, a small improvement is added to *@FacesValidator*. In JSF 2.2, it is not required to specify a validator id, and a default id will be assigned at runtime according to the validator classname. The improvement is existed in *@FacesConverter* as well.

##Ajax support

Like other JSF component, JSF inputFile component supports Ajax update.

<pre>
&lt;h:form enctype="multipart/form-data">
	&lt;h:inputFile id="file" value="#{fileUploadBean.file}" >
		&lt;f:validator validatorId="fileValidator"/>
		&lt;f:ajax execute="@this" render="@form"/>
	&lt;/h:inputFile>
	&lt;h:message for="file"/>
	&lt;h:commandButton value="Upload" action="#{fileUploadBean.upload()}"/>
&lt;/h:form>
</pre>

Update the facelets page, add an ajax event to the *inputFile* component, which causes the validation error displayed instantly when the uploaded file is chosen.

## The sample codes

The sample codes are hosted on my github.com account, check out and play it yourself.

[https://github.com/hantsy/ee7-sandbox](https://github.com/hantsy/ee7-sandbox)
