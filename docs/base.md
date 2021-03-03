# What is REST

I have created several sample applications to demonstrate different backend solutions for producing REST API. 

In this post, I will create the REST API using JAXRS which is part of Java EE platform. On different application servers, the JAXRS implementation is different. Jersey is the official reference implementation which is shipped with [Glassfish](http://glassfish.java.net) by default, and Resteasy is the JBoss alternative JAXRS implementation on [JBoss platform](http://www.jboss.org)(Jboss AS has been named to [Wildfly](http://www.wildfly.org)). 

## REST API design and implementation

REST API design should embrace the HTTP verbs, for example.

<table>
	<tr>
		<th>URI</th>
		<th>HTTP METHOD</th>
		<th>REQUEST</th>
		<th>RESPONSE/HTTP STATUS</th>
		<th>DESCRIPTION</th>
	</tr>
	<tr>
		<td>/posts</td>
		<td>GET</td>
		<td></td>
		<td>200, [{'id':1, 'title':'my first post'}, {}]</td>
		<td>Get all posts</td>
	</tr>
	<tr>
		<td>/posts</td>
		<td>POST</td>
		<td>{ 'title':'my first post'}</td>
		<td>201</td>
		<td>Creat a post</td>
	</tr>
	<tr>
		<td>/posts/{id}</td>
		<td>GET</td>
		<td></td>
		<td>200, {'id':1, 'title':'my first post'}</td>
		<td>Get post by id</td>
	</tr>
	<tr>
		<td>/posts/{id}</td>
		<td>PUT</td>
		<td>{ 'title':'my first post'}</td>
		<td>205</td>
		<td>Update the post by id</td>
	</tr>
	<tr>
		<td>/posts/{id}</td>
		<td>DELETE</td>
		<td></td>
		<td>205</td>
		<td>Delete the post by id</td>
	</tr>
</table>