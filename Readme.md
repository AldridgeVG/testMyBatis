# Study Note of MyBatis

- This is the learning path of MyBatis of mine.
- Goal: 
    1. understand and use MyBatis skillfully
    2. reconstruct *Macrohard_SBV* programme (replace JDBC Dao )



### ORM/Hibernate

- **ORM: Object - Relation Mapping**

    model of mapping between TABLE in DB and POJO(Plain Ordinary Java Object).



- **Hibernate**

  Hibernate is a framework that provides HQL instead of SQL to finish CURD. Realized by *hbm.xml* file, which includes configuration of rules of mapping between some POJOs and TABLEs in DB. This is a Hibernate Mapping XML config file, e.g. 


  ```xml
  <hibernate-mapping>
    	<class name = "com.A.a.Class1" table="tab1" catalog="mybatis" optimistic-lock="version">
            <id name="id" type="int">
                <column name="ID"/>
                <generator class="assigned"/>
            </id>
            <property name="roleName" type="string">
                <column name="ROLENAME" length="20"/>
            </property>
            <property name="note" type="string">
            	<column name="NOTE" length="60"/>
            </property>
        </class>
  </hibernate-mapping>	
  ```

    

  Global config of Hibernate e.g.

  ```xml
  <hibernate-configuration>
      <session-factory>
      	<property name="hibernate.dialect">
          	org.hibernate.dialect.MySQLDialect
          </property>
          <property name="hibernate.connection.driver_class">
          	com.mysql.cj.jdbc.Driver
          </property>
          <property name="hibernate.connection.url">
          	jdbc://localhost:3306/xxx?useSSL=false&serverTimezone=UTC
          </property>        
          <property name="hibernate.connection.username">
          	usr
          </property>
          <property name="hibernate.connection.password">
          	pwd
          </property>
          
          <mapping resource="com/A/a/po/Class1.hbm.xml"></mapping>
      </session-factory>
  </hibernate-configuration>
  ```

  

  After a global xml Hibernate configuration is set, establish the SessionFactory of Hibernate:

  (SessionFactory is used as the global OBJ which provides Session interfaces. We use these interfaces to access DB)

  ```java
  public class HibernateUtil{
      private static final SessionFactory sessionFactory;
      
      static{
          try{
              Configuration config = new Configuration().configure("hibernate.config.xml");
          }catch(Throwable e){
              System.out.println("init sf failure");
              throw new ExceptionInInitializerError(e);
          }
      }
      
      public static SessionFactory getSessionFactory(){
          return seesionFactory;
      }
  }
  ```

  

  To use Session Factory in Java programme, form e.g.

  ```java
  try{
      Session session = HibernateUtil.getSessionFactory.openSession();
      Class1 class1 = (Class1)session.get(Class1.class, 1L);
      class1.get();	//call methods in POJO to manage data in DB
  }finally{
      if(session != null) session.close();
  }
  ```

  

  advantage:

  - all mapping rules and conn config have been moved into xml files
  - operate on one session instead of multiple obj in a dialect
  - close resources by close a session only

  disadvantage:

  - whole table mapping - requires sending all role name when update
  - cannot assemble different SQL under different circumstance
  - poor support of complex SQL and stored procedure
  - low efficiency of HQL



### MyBatis

- a semi-automatic mapping framework aiming at solve problems in Hibernate
- semi-auto: manually set POJO, SQL and mapping rules
- a persistence layer framework based on Java, including SQL Maps and Dao
- requires manually code SQL, allows dynamic SQL configuration, supports stored procedures



to realize a JDBC function with MyBatis, here’s an example:
configuration file mybatis_config.xml

```xml
<configuration>
    <environments default="development">
    	<environment id="development">
        	<transactionManager type="JDBC"/>
            <dataSource type="POOLED">
            	<property name="driver" value="com.mysql.cj.jdbc.Driver"/>
                <property name="url" value="jdbc://localhost:3306/xxx?useSSL=false&serverTimezone=UTC"/>
                <property name="username" value="usr"/>
                <property name="password" value="pwd"/>
            </dataSource>
        </environment>
    </environments>
    
    <mappers>
    	<mapper resource="com\A\a\pojo\class1.xml"/>
    </mappers>
</configuration>
```



class1.xml:

(here is a SQL without mapping rules, when SQL role name coordinates with POJO property name, MyBatis will provide auto mapping -- similar with auto-binding rules of Spring MVC)

```xml
<mapper namespace="com.A.a.mapper.Class1Mapper">
	<select id="getClass1" parameterType="int" resultType="com.A.a.pojo.Class1">
    	select prop1, prop2 as Prop1, Prop2 from tab1 where prop1=#{id}
    </select>
</mapper>
```



Class1Mapper.java:

(an interface)

```java
public interface Class1Mapper{
    public Class1 getClass1(){};
}
```

