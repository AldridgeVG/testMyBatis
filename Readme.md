# Study Note of MyBatis

- This is the learning path of MyBatis of mine.
- Goal: 
    1. understand and use MyBatis skillfully
    2. reconstruct *Macrohard_SBV* programme (replace JDBC Dao )



## Preparing

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

​    

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



setup Session Factory: (hunger type singleton mode:)

```java
public class MyBatisUtil{
    private static SqlSessionFactory sqlSessionFactory=null;
    public static SqlSessionFactory getSqlSessionFactory(){
        InputStream ipts = null;
        if(sqlSessionFactory == null){
            try{
                String resource = "mybatis_config.xml";
                //read xml file
                sqlSessionFactoy = new SqlSessionFactoryBuilder().build(Resource.getResourceAsStream(resource));
            	return sqlSessionFactory;
            }catch(Exception e){
                e.printStacktrace();
            }
        }
        return sqlSessionFactory;
    }
}
```



After these setup, we can use Mybatis to access DB:

```java
public class MyBatisSample{
    public static void main(String[]args){
        SqlSession sqlSession = null;
        try{
            sqlSession=MyBatisUtil.getSqlSessionFactory().openSession();
            Class1Mapper class1Mapper = sqlSession.getMapper(Class1Mapper.class);
            Class1 class1 = class1Mapper.getClass1)(1L);
        }finally{
            sqlSession.close();
        }
        return;
    }
}
```



## MyBatis Basic

- start a mybatis project



### Components

- SqlSessionFactoryBuilder(Constructor): 

    generate SqlSessionFactory according to configuration

    

- **SqlSessionFactory(Interface):**

    generate SqlSession by factory info

    

- SqlSession(Dialect):

    an interface can both send SQL to execute and get Mapper

    

- SQL Mapper(new in Mybatis):

    a component form by Java interface and xml file, requiring providing coordinate SQL and mapping rules. Send SQL to execute and retrieve result set.



### Initialization

- MyBatis apps’ core: SqlSessionFactory
- Construct a SqlSessionFactory: by xml config / by java coding

- Recommended: implements by xml configuration, reasons:
    - Java annotation in class has limited function 
    - Dynamic SQL and complex SQL implements in java file has bad readability, making it harder to maintenance.



#### Generate SqlSessionFactory by XML

analyze xml config once and store information in a Configuration instance, in order to accelerate access. (Configuration class: org.apache.ibatis.session.Configurtion, as a class instance means DRAM storage, fast access). 



SqlSession implement class in MyBatis: DefaultSqlSessionFactory



STEPS:

1. configure mybatis-config.xml;
2. config pojo, mapper(java interface, xml)  --  (MAPPER)
3. create SqlSessionFactory / SqlSession to use

**! ! ! getResourceAsStream :  *import org.apache.ibatis.io.Resources; ! ! !***

**and mybatis-cong.xml should be placed in root dir:**

**![image-20200627102345716](C:\Users\caxus\AppData\Roaming\Typora\typora-user-images\image-20200627102345716.png)**

4. exe SqlSession



#### Lifespan of Components

1. SqlSessionFactoryBuilder:

    only be used to create (multiple) SqlSessionFactory, once factory is constructed, SqlSessionFactoryBuilder should be put into GC process



2. SqlSessionFactory:

    used to create SqlSession(similar with the connection in JDBC), every time accessing DB,  a SqlSession should be created, which clearly requires an implement of singleton mode. Every DB has its own and only SqlSessionFactory.



3. SqlSession:

    it’s lifespan is only the DB curd procedure, recycle immediately when curd is done. not thread secured, can execute several SQL.



4. Mapper

    mapper is a interface without any implement class. execute SQL like a method. It’s hard to control, should be dumped with the SqlSession use it.



### CAUTIONS

- in mybatis-config.xml, DB(MySQL) url is different:

    - **JDBC:**

        Jdbc:mysql://localhost:3306/xxx?useSSL=false&serverTimezone=UTC 

    - **MyBatis XML:** 

      jdbc:mysql://localhost:3306/xxx?useSSL=FALSE&amp;serverTimezone=UTC;
    
    use “&amp”+”;” instead of &. because &+amp+; means transformed &.
    
    
    
- in IntelliJ IDEA, xml files should be put in resources dir, sub dir name same as package in java dir.



- log4j -- how to use it ?





## MyBatis Configuration

- structure:
    - properties
    - settings
    - typeAliases
    - typeHandlers
    - objectFactory
    - plugins
    - environments
        - environment(环境变量)
            - transactionManager
            - dataSource
    - databaseIdProvider
    - mappers



### properties

3 ways to configure properties:

- **property sub elem**(1st priority)

    traditional way

    

- **properties config file**(2nd)

    set *jdbc.properties* file in resources dir:

    ```properties
    driver = com.mysql.cj.jdbc.Driver
    url = jdbc:mysql://localhost:3306/testmb?useSSL=FALSE&amp;serverTimezone=UTC
    username = root
    password = galland990531
    ```

    use the configuration  by using

    ```xml 
     <properties resource = “jdbc.properties">
    ```

    

- **programme param**(3rd)

    used as a safe way to access DB, dev team only get encoded string, use decoder to get true usr/pwd to access DB.



### settings (important)

several settings need to be known:

| name                     | default | usage                                                        |
| ------------------------ | ------- | ------------------------------------------------------------ |
| cacheEnabled             | true    | a switch of global mapper cache                              |
| lazyLoadingEnabled       | false   | a switch of global linked objs’ lazy loading                 |
| aggresiveLazyloading     | true    | call of any lazy property will lead to full loading of Objects with lazy loading properties |
| multipleResultSetEnabled | true    | allow SQL to return several result set(compatible driver required) |
| useColumnLabel           | true    | use column label to replace column name                      |
| autoMappingBehavior      | PARTIAL | NONE: cancel auto mapping; PARTIAL: auto map result set without a nesting result set defined |
| jdbcTypeForNull          | OTHER   | assign jdbc type(NULL, VARCHAR, OTHER) for null when param hasn’t provide specified jdbc type |
| callSettersOnNulls       | false   | whether to call the setter when there’s nulls in result set or not. |
| ...                      | ...     | ...                                                          |



### typeAliases

system defines some aliases:

- _byte, _int, _char, _long, _double, _boolean, _float... (array supported, for basic datatype)

- string, byte, int/integer, short, float, long, double, boolean, date, decimal/bigdecimal...(array supported, for packed type)
- map, hashmap, list, arraylist, ResultSet, collection, iterator (array not supported, for container)git



alias allow us to access class by using its alias instead of full path name

e.g. 

```xml
<typeAliases>
    <typeAlias alias="msquare" type="pojo.Square"/>
</typeAliases>
```



under certain circumstance such as there’re too many POJOs, MyBatis allow us to define custom alias by scan whole package:

```xml
<typeAliases>
<package name="pojo.Square"/>
</typeAliases>
```

this requires Alias annotation in POJO:

```java
package pojo;

@Alias("square")
public class Square {
    private int length;
    private int height;
}
```

P.S. when there’s no annotation, MyBatis will automatically turn the first char of class name into lowercase mode and use as alias ! (prevent duplicate naming problem !)



### typeHandler

when setting a param in PreparedStatement or retrieve a value from ResultSet, MyBatis will use the Registered typeHandler.



- custom typeHandler:

    DB: VARCHAR, Java: string param

    (should use MyStringTypeHandler, but MyBatis will not automatically use it on this one)

    => implements typeHandler to realize:

    ``` java
    @MappedTypes({String.class})
    @MappedJdbcTypes(Jdbc.VARCHAR)
    public class MyStringTypeHandler implements TypeHandler<String>{
        @Override
        public void setParameter(PreparedStatement ps, int index, tring value, JdbcType jt){
     		ps.setString(index,value);
        }
        
        @Override
        public String getResult(ResultSet rs, String colName) throws SQLException{
            return rs.getString(colName);
        }
        
        @Override
        public String getResult(ResultSet rs, index int) throws SQLException{
            return rs.getString(index);
        }
        
        @Override
        public String getResult(CallableStatement cs, int index) throws SQLException{
        	return cs.getString(index);
        }
    }
    ```

    

### ObjectFactory

when MyBatis need to construct and return a ResultSet, ObjectFactory will be used to create POJO. Default implement usually.



### Plugins



### environments Config

multiple dataSource can be registered in environments. A dataSource can be divided into 2 major parts: dataSource/transactionManager. 

- transactionManager: 

    - JDBC
    - MANAGED
    - custom

- dataSource type:

    - UNPOOLED: connection non-pool DB

    - POOLED: connection pool DB

    - JNDI: JNDI data source

    - custom

        

#### DB transaction

controlled by SqlSession in MyBatis, use the SqlSession commit() when success, or rollback() when failure.



#### dataSource





## Mapper

MyBatis is a light framework facing Mappers-constructed SQL, which support dynamic-binding and java bean automatic binding(requires SQL return field name be the same as javaBean property name)



### mapper elements

- select (return result set)
- insert (return insert lines total count)
- update (above)
- delete (above)
- sql (allows to define partial SQL to quote elsewhere)
- **resultMap**(describe how to load Obj from result set)

- cache (cache config on certain namespace)
- cache-ref (the quote of cache config of other namespace)



#### select

define param before execute select, param can be int/float.../Map/JavaBean...

- select elems:
    - id : (Mapper’s namespace , id) should be UNIQUE
    - parameterType: classes’ full name (when giving alias, alias should be MyBatis internal or custom)
    - resultType: classes’ full name or int/float... 
    - resultMap: the quotation of Mapper, execute auto mapping (resultMap/resultType can’t be set at the same time)
    - fetchSize: personalize total of records will be retrieved
    - statementType: using which kind of stat( STATEMENT / PREPARED / CallableStatement )





# MyBatis in SpringBoot

using mybatis in SpringBoot needs this starter:

```xml
        <dependency>
            <groupId>org.mybatis.spring.boot</groupId>
            <artifactId>mybatis-spring-boot-starter</artifactId>
            <version>2.1.3</version>
        </dependency>
```



### **config datasource**

(test using druid dataSource, needs configuration (Maven dependency, DruidConfig) )



p.s. use schema: to execute sql on booting of spring boot app, add configuration “initialization-mode: always” in application.yml



### create bean according to database tables

column better be the same to DB



### use MyBatis to access DB

several ways, just like normal mybatis



#### by annotation

use MyBatis by annotation is simple and clear, do not need to configure anything, all automatic

but if manual config is preferred, create config class will do.



add a configuration customizer to the container

```java
import org.apache.ibatis.session.Configuration;

import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class MyBatisConfig {

    @Bean
    public ConfigurationCustomizer configuartionCustomizer(){
        return new ConfigurationCustomizer() {

            @Override
            public void customize(Configuration configuration) {
                //use camel case naming
                configuration.setMapUnderscoreToCamelCase(true);
            }
        };
    }
}
```





1. create according mapper interface

    ```java
    @Mapper
    public interface DepartmentMapper {
    
        @Select("select * from department where id = #{deptId}")
        public Department getDeptById(Integer deptId);
    
        @Delete("delete from department where id = #{deptId}")
        public int deleteDeptById(Integer deptId);
    
        //in department, id is configured as auto-increment
        //use option "useGeneratedKeys" to show the id which in auto-increasingly generated
        //here the dN in #{} means department.departmentName (get the field from the param obj)
        @Options(useGeneratedKeys = true, keyProperty = "id")
        @Insert("insert into department(departmentName) values(#{departmentName})")
        public int insertDept(Department department);
    
        //here the dN and id in #{} means department.departmentName and department.id (get the field from the param obj)
        @Update("update department set departname=#{departmentName} where id=#{id}")
        public int updateDept(Department department);
    }
    ```

    

2. create a controller to use the methods in the interface under certain url request

    ```java
    @RestController
    public class DepartmentController {
    
        @Autowired
        DepartmentMapper departmentMapper;
    
        @GetMapping("/dept/{id}")
        public Department getDept(@PathVariable("id") Integer id){
            return departmentMapper.getDeptById(id);
        }
    
        //use url like "/dept?departmentName=AA"
        @GetMapping("/dept")
        public Department insertDept(Department department){
            departmentMapper.insertDept(department);
            return department;
        }
    }
    ```



if there’s too many mappers, add mapper annotation above every interface can be annoying.

use “@MapperScan(value = “package_name”)” annotation in SpringBoot Main App to add all interfaces in a package as mappers