# JPA 2.1: Overview

JPA 2.1 brings a series of small improvements.

I have written some posts to introduce them one by one.

* [Schema generation scripts configuration](https://github.com/hantsy/ee7-sandbox/wiki/jpa-scripts)
* [Attribute Converter](https://github.com/hantsy/ee7-sandbox/wiki/jpa-converter)
* [Bulk Criterial Update and Delete API](https://github.com/hantsy/ee7-sandbox/wiki/jpa-bulk)
* [Entity Graph](https://github.com/hantsy/ee7-sandbox/wiki/jpa-entitygraph)
* [Synchronized Persistence Contexts](https://github.com/hantsy/ee7-sandbox/wiki/jpa-unsynchronized)
* [Treat](https://github.com/hantsy/ee7-sandbox/wiki/jpa-treat)
* [CDI support](https://github.com/hantsy/ee7-sandbox/wiki/jpa-cdi)
* [Programmatic Named Queries](https://github.com/hantsy/ee7-sandbox/wiki/jpa-programmatic-nq)


JPA 2.1 also includes some RDBMS improvements, such as supports **Store Procedure**, and adds **function** in JPQL. I am not a big fan of RDBMS,  so these are not so attractive for me.

In this version, some features are still not perfect. As I motioned, the schema generation scripts strategy does not include a Hibernate equivalent **UPDATE**. Secondly, the CDI support is omitted in the `@Converter` and `@Entity` classes.

The JPA expert group should consider more about NoSQL support. Hear the voice from Hibernate community(there is a Hibernate OGM project for NoSQL, now supports Mongo, JBoss Infinispan) and other JPA providers, especially [DataNucleus](http://www.datanucleus.org) which has excellent NoSQL support.

The sample codes are hosted on my github.com account, check out and play it yourself.

[https://github.com/hantsy/ee7-sandbox](https://github.com/hantsy/ee7-sandbox)
