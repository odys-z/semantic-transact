
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.odys-z/semantics.transact/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.odys-z/semantics.transact/)
[![License](http://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

# About

### Building SQL, the semantic-transact solution

Semantic-transact is a sql builder providing structured API.

Traditionally, relational database applications are based upon SQL formatting.
It's not easy if the data becomes more and more complicated and more DB tables
with their relationship / restriction involved.

User, the programmer has to take care of the SQL string structure while appending,
filling in data into a half structured string template. When some more abstracted
SQL composition task must be fulfilled, it's extremely difficult to, if not
impossible, take care of both data and SQL syntax.

This is where a relational DB based application's bug pron module. Especially when
the DB design is changed, even a little, the whole system will take a long time
to become stable again.

Of course there already a lot of solutions exist, e.g. Hibernate if the debating
on over-engineering is not a concern. But the author likes the [SQLBuilder](https://openhms.sourceforge.io/sqlbuilder/)
more before the idea of semantic-transact.

Semantic-transact is another try to solve the problem by separating SQL syntax
maintained from data manipulation. With a structured API, users don't have to
worry about the syntax in the string.

Here is a simple example:

~~~
    // sqls is an ArrayList<String> buffer
    st.insert("a_functions")
        .nv("funcId", "AUTO")
        .nv("funcName", "Test 001")
        .nv("sibling", "10")
        .nv("parentId", "0")
        .commit(null, sqls);

    // fullpath is auto handled by semantics context - ignore it now
    AssertEquals(
        "insert into a_functions  (funcId, funcName, sibling, parentId, fullpath) values ('AUTO', 'Test 001', 10, '0', 'fullpath 0.0 AUTO')",
        sqls.get(0));
~~~

All SQL strings are accumulated in a list buffer. When this process is completed,
all SQL statements can be committed in a batch operation. That's why it's named
"transact".

At first sight, you may dislike this approach because it's somehow anti-intuitive -
all programmers are already comfortable with SQL syntax. But when the SQL composing
tasks become complicated, or have to be done according to data at runtime, remote
request or data relationship, it's immediately showing the advantages - you care
only about data, let semantic-transact handling SQL AST for you.

# Quick Start

Semantic-transact is released as a jar package, which can be found at
[maven central repository](https://search.maven.org/artifact/io.github.odys-z/semantics.transact)

For maven project, to use the latest package, in pom.xml
~~~
    <dependency>
        <groupId>io.github.odys-z</groupId>
        <artifactId>semantics.transact</artifactId>
        <version>[1.1.3,)</version>
    </dependency>
~~~

For examples, see the test cases:

- [Pure transaction building without semantics context](https://github.com/odys-z/semantic-transact/blob/master/semantic.transact/src/test/java/io/odysz/transact/sql/TestTransc.java)

- [With help of semantics context](https://github.com/odys-z/semantic-transact/blob/master/semantic.transact/src/test/java/io/odysz/semantics/SemanticsTest.java)

# Documents

- [javadoc](https://odys-z.github.io/javadoc/semantic.transact/index.html)

- [Semantic-* Document (deprecated)](https://odys-z.github.io/archive)
