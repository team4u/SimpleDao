# SimpleDao - A Data Access Object framework

[TOC]

SimpleDao 是一个轻量级的数据库操作工具类，可以快速方便地对数据库进行各种操作。

## System Requirements

* JDK 1.7+

## Features

* 易上手 - API简单易用,你可以在几分钟之内上手使用
* 轻量级 - 整体代码结构简单,你可以在很短的时间内理解并进行扩展，支持Android
* 快速 - 仅对原生JDBC进行一层薄封装

## Maven

```xml
<dependency>
    <groupId>org.team4u.dao</groupId>
    <artifactId>simple-dao</artifactId>
    <version>1.0.4</version>
</dependency>

<dependency>
    <groupId>org.team4u</groupId>
    <artifactId>team-kit-core</artifactId>
    <version>1.0.5</version>
</dependency>
<dependency>
    <groupId>cn.hutool</groupId>
    <artifactId>hutool-core</artifactId>
    <version>4.0.5</version>
</dependency>
<dependency>
    <groupId>cn.hutool</groupId>
    <artifactId>hutool-log</artifactId>
    <version>4.0.5</version>
</dependency>
```

添加仓库：

```xml
<repositories>
    <repository>
        <snapshots>
            <enabled>false</enabled>
        </snapshots>
        <id>bintray-team4u</id>
        <name>bintray</name>
        <url>https://dl.bintray.com/team4u/team4u</url>
    </repository>
</repositories>
```

## 快速开始

### 创建测试数据库

```sql
CREATE TABLE client (
    client_id varchar(32) NOT NULL PRIMARY KEY,
    name varchar(150) DEFAULT NULL
);
```

### 创建实体类

```java
@Table(name = "client")
public class TestEntity {

    // 标记为主键
    @Id
    @Column(name = "client_id")
    private String id;

    // 若不指定@Table或者@Column的name,
    // 则将采用默认的规则对数据库的表名或者字段名称进行映射:
    // 将驼峰命名的Java属性,映射为下划线隔开的小写命名数据库表名或者字段,如:
    // nameAndAge 映射为 name_and_age
    @Column
    private String name;

    // 省略getter/setter
}
```

### 实体类CRUD

```java
// 创建Dao是重量级操作,请生成后全局共享
Dao dao = new SimpleDao(dataSource);

// 插入对象到数据库
TestEntity entity = new TestEntity();
entity.setId("1");
entity.setName("x");
dao.insert(entity);

// 根据主键从数据库中获取数据,并组装返回实体对象
entity = dao.queryWithPkForObject(TestEntity.class, "1");

// 根据查询条件获取数据,并组装返回实体对象
entity = dao.queryForObject(SqlBuilders.select(TestEntity.class)
                              .where("name", "=", "x"));

// 更新对象到数据库
entity.setName("y");
dao.update(entity);

// 从数据库中删除记录
dao.delete(entity);
```

### 创建普通类

```java
public class TestBean {

    private String clientId;

    private String name;

    // 省略getter/setter
}
```

### 普通类CRUD

```java
// 创建Dao是重量级操作,请生成后全局共享
Dao dao = new SimpleDao(dataSource);

// 插入数据库
dao.insert(SqlBuilders.insert("client")
               .set("client_id", 1)
               .set("name", "2")
               .create(););

// 根据查询条件获取数据,并组装返回实体对象
TestBean bean = dao.queryForObject(TestBean.class,
                                    SqlBuilders.select("client")
                                      .where("name", "=", "x")
                                      .create());

// 更新数据库
dao.update(SqlBuilders.update("client")
               .set("name", "x")
               .where("client_id", "=", 1)
               .create());

// 从数据库中删除记录
dao.delete(SqlBuilders.delete("client")
               .where("name", "=", "x")
               .create());
```

### 自定义SQL

```java
// 创建Dao是重量级操作,请生成后全局共享
Dao dao = new SimpleDao(dataSource);

// 查询结果返回Map,无需定义Bean
Dict map = dao.queryForObject(Dict.class,
                   SqlBuilders.sql("select * from client where name = :name")
                        .setParameter("name", "x")
                        .create());

// 可执行insert/update/delete等SQL
dao.execute(SqlBuilders.sql("delete from client where name = :name")
               .setParameter("name", "x")
               .create());
```

## SqlBuilder

SimpleDao基于[SqlBuilder](https://github.com/team4u/SqlBuilder)构建,使用了其中的实体类注解与SqlBuilders API.

例如:

```java
@Table(name = "client")
public class TestEntity {

    // 标记为主键
    @Id
    @Column(name = "client_id")
    private String id;

    // 省略getter/setter

}
```

```java
dao.queryForObject(TestEntity.class,
                   SqlBuilders.select(TestEntity.class)
                     .where("name", "=", "x")
                     .create());
```

以上代码中:

* TestEntity使用了SqlBuilder的实体类注解.

* 查询条件部分直接使用了SqlBuilders的API

关于SqlBuilder的使用,请参考[SqlBuilder](https://github.com/team4u/SqlBuilder)项目中的详细文档,此处不在重复.

## Dao接口

Dao接口中包含了所有功能,具体用法请参考方法中的注释.

```java
 /**
     * 根据主键查询对象
     *
     * @return 对象
     */
    <T> T queryWithPkForObject(Class<T> resultClass, Object... ids);

    /**
     * 查询单个对象
     *
     * @return 对象
     */
    <T> T queryForObject(Class<T> resultClass, Sql sql);

    /**
     * 查询单个对象
     *
     * @return 对象
     */
    <T> T queryForObject(EntitySqlBuilder<T> sqlBuilder);

    /**
     * 查询一组对象
     *
     * @return 对象集合
     */
    <T> List<T> queryForList(Class<T> resultClass, Sql sql);

    /**
     * 查询一组对象
     *
     * @return 对象集合
     */
    <T> List<T> queryForList(EntitySqlBuilder<T> sqlBuilder);

    /**
     * 根据分页查询一组对象
     *
     * @return 对象集合
     */
    <T> List<T> queryForList(Class<T> resultClass, Sql sql, Pager pager);

    /**
     * 根据分页查询一组对象
     *
     * @return 对象集合
     */
    <T> List<T> queryForList(EntitySqlBuilder<T> sqlBuilder, Pager pager);

    /**
     * 查询数据,返回结果由handler进行处理
     *
     * @return 结果对象
     */
    <T> T query(ResultSetHandler<T> handler, Sql sql);

    /**
     * 查询记录数量
     *
     * @return 记录数
     */
    int count(Sql sql);

    /**
     * 对一组对象进行迭代，适用于大量数据的集合
     */
    <T> void each(Class<T> resultClass, Sql sql, Callback<T> callback);

    /**
     * 对一组对象进行迭代，适用于大量数据的集合
     */
    <T> void each(EntitySqlBuilder<T> sqlBuilder, Callback<T> callback);

    /**
     * 对一组对象按指定分页进行迭代，适用于大量数据的集合
     */
    <T> void each(Class<T> resultClass, Sql sql, Pager pager, Callback<T> callback);

    /**
     * 对一组对象按指定分页进行迭代，适用于大量数据的集合
     */
    <T> void each(EntitySqlBuilder<T> sqlBuilder, Pager pager, Callback<T> callback);

    /**
     * 将一个对象插入到一个数据源。
     * <p>
     * 如果你的字段声明了 '@Id(auto=true)'，则填充插入后最新的 ID 值
     *
     * @return 当前对象
     */
    <T> T insert(T entity);

    /**
     * 将一个对象插入到一个数据源，可忽略null值。
     * <p>
     * 如果你的字段声明了 '@Id(auto=true)'，则填充插入后最新的 ID 值
     *
     * @param entity           实体对象
     * @param activatedColumns 可插入字段正则表达式，如a|b表示只插入a和b字段，留空则插入所有字段
     * @param ignoreNull       null值是否不更新
     * @return 当前对象
     */
    <T> T insert(T entity, String activatedColumns, boolean ignoreNull);

    /**
     * 将一个对象集合插入到一个数据源。
     * <p>
     * 如果你的字段声明了 '@Id(auto=true)'，则填充插入后最新的 ID 值;若auto=false,则内部采用fastInsert处理
     *
     * @return 已插入记录数
     */
    <T> int[] insert(List<T> entities);

    /**
     * 将一个对象集合插入到一个数据源，可忽略null值。
     * <p>
     * 如果你的字段声明了 '@Id(auto=true)'，则填充插入后最新的 ID 值;若auto=false,则内部采用fastInsert处理
     *
     * @param entities         实体对象集合
     * @param activatedColumns 可插入字段正则表达式，如a|b表示只插入a和b字段，留空则插入所有字段
     * @param ignoreNull       null值是否不更新
     * @return 已插入记录数集合
     */
    <T> int[] insert(List<T> entities, String activatedColumns, boolean ignoreNull);

    /**
     * 快速插入一个对象
     * <p>
     * '@Id(auto=true)'将不起作用,内部统一采用 batch 的方法插入
     *
     * @return 影响的行数
     */
    <T> int[] fastInsert(List<T> entities);

    /**
     * 快速插入一个对象，可忽略null值。
     * <p>
     * '@Id(auto=true)'将不起作用,内部统一采用 batch 的方法插入
     *
     * @param entities         实体对象集合
     * @param activatedColumns 可插入字段正则表达式，如a|b表示只插入a和b字段，留空则插入所有字段
     * @param ignoreNull       null值是否不更新
     * @return 影响的行数
     */
    <T> int[] fastInsert(List<T> entities, String activatedColumns, boolean ignoreNull);

    /**
     * 更新对象
     */
    int update(Object entity);

    /**
     * 更新对象
     *
     * @param entity           实体对象
     * @param activatedColumns 可更新字段正则表达式，如a|b表示只更新a和b字段，留空则更新所有字段
     * @param ignoreNull       null值是否不更新
     * @return 影响的行数
     */
    int update(Object entity, String activatedColumns, boolean ignoreNull);

    /**
     * 批量更新对象
     */
    <T> int[] update(List<T> entities);

    /**
     * 批量更新对象
     *
     * @param entities         实体对象集合
     * @param activatedColumns 可更新字段正则表达式，如a|b表示只更新a和b字段，留空则更新所有字段
     * @param ignoreNull       null值是否不更新
     * @return 影响的行数集合
     */
    <T> int[] update(List<T> entities, String activatedColumns, boolean ignoreNull);

    /**
     * 删除对象
     *
     * @return 影响的行数
     */
    int delete(Object entity);

    /**
     * 根据主键删除对象
     *
     * @return 影响的行数
     */
    <T> int deleteWithPK(Class<T> resultClass, Object... ids);

    /**
     * 批量删除对象
     *
     * @return 影响的行数
     */
    <T> int[] delete(List<T> entities);

    /**
     * 执行SQL语句
     *
     * @return 影响的行数
     */
    int execute(Sql sql);

    /**
     * 批量执行SQL语句
     *
     * @return 影响的行数
     */
    int[] execute(List<Sql> sql);

    /**
     * 通过ConnectionCallback得到一个 Connection 接口实例
     * <p>
     * 请注意，不要手工关闭这个连接，方法执行完毕后会自动关闭连接。
     * <p>
     * 如果你从当前连接对象中创建了ResultSet对象或者 Statement对象，请自行关闭。
     *
     * @return ConnectionCallback返回结果
     */
    <T> T execute(ConnectionCallback<T> connectionCallback);
```

## 关于主键

@Id中的auto属性默认为FALSE,如果数据库中设置了自增长主键（如MySQL）,需要将auto设置为TRUE.

调用dao.insert()方法后,SimpleDao将自动把该记录的主键值赋值到实体类的@Id所在字段.

## 结果转换

Dao中的所有查询接口中,都可以方便地转成为指定类型（resultClass）,默认支持的类型有:

* Java基本类型

  Integer/Long/Double/Float/Byte/Short/String/Boolean

* Entity

  使用了SqlBuilder注解（如@Table等）的对象,则按照注解规则进行字段名映射.

* Java Bean

  很多时候，查询的结果并非实体类，SimpleDao支持直接将表数据映射为普通的JavaBean，表中字段的下划线名称将自动匹配JavaBean中的驼峰命名属性。

  注意，类必须符合Java Bean的规定。

  例如：

  表结构

  ```sql
  CREATE TABLE my_table (
      client_id varchar(32) NOT NULL PRIMARY KEY,
      name varchar(150) DEFAULT NULL
  );
  ```

  Java Bean

  ```java
  public class MyTable {
     private String clientId;
     private String name;
     // 省略getter/setter
  }
  ```

  此时可以直接将查询结果集合映射为MyTable对象集合

  ```java
  List<MyTable> results = dao.queryForList(MyTable.class, 						
                                           SqlBuilders.select("my_table").create());
  ```

* Map

  将查询结果转换为指定的Map类型，其Key为小写的字段名，value为该字段对应的数据。

  ```java
  // 不指定具体实现Map时，内部采用HashMap.class
  dao.queryForList(Map.class, 						
                   SqlBuilders.select("my_table").create());

  // 推荐使用Dict，里面包含了大量便捷、常用的方法
  dao.queryForList(Dict.class, 						
  			    SqlBuilders.select("my_table").create());
  ```

* ResultSetHandler

  如果上述结果类型都无法满足需求，你可以自定义结果转换处理器ResultSetHandler：

  ```java
  public interface ResultSetHandler<T> {
      T handle(ResultSet rs) throws SQLException;
  }
  ```

  事实上SimpleDao默认的结果转换也是通过ResultSetHandler实现的，可以参考com.asiainfo.dao.resultset.handler包下的各种handler实现。

  最后使用自定义的resultSetHandler进行查询：

  ```java
  dao.query(resultSetHandler, SqlBuilders.select("my_table").create());
  ```

## 事务

SimpleDao实现了简单但较为常用的事务处理:

* 支持多数据源事务
* 支持嵌套事务

通过Transactions.execute可以快速开启事务.

```java
Transactions.execute(Runnable... runnableList)
```

以上代码中,runnableList只要出现一个异常,则所有runnableList都将回滚;若存在多数据源,则所有数据源都将回滚.

### 嵌套事务

```java
public void doA() {
    Transactions.execute(new Runnable(){
        public void run(){
            doB();
            doC());
        }
    });
}

public void doB() {
    Transactions.execute(new Runnable(){
        public void run(){
            // do someting...
        }
    });
}

public void doC() {
    Transactions.execute(new Runnable(){
        public void run(){
           // do someting...
        }
    });
}
```

以上代码中三个函数都声明了事务,但在doA方法中,只有最外层的事务起作用，即以doA事务为准.

### 事务托管

如果SimpleDao提供的事务管理不能满足需求，可以结合第三方框架进行事务管理（如Spring）,实现ConnectionHandler，对事务进行完整控制：

```java
public interface ConnectionHandler {

    /**
     * 根据数据源获取数据库连接
     */
    Connection getConnection(DataSource dataSource) throws SQLException;

    /**
     * 释放数据库连接
     */
    void releaseConnection(Connection connection, DataSource dataSource);
}
```

除了自带的事务处理类（DefaultConnectionHandler）,SimpleDao还内置了Spring的事务处理类（SpringConnectionHandler）,可以方便将事务处理交由Spring管理.

SimpleDao提供的默认事务管理功能,与Spring的事务传播属性PROPAGATION_REQUIRED效果一致.

如果你想完全自定义ConnectionHandler,请参考DefaultConnectionHandler的实现.

在创建SimpleDao时传入自定义的connectionHandler,即可以完成事务托管:

```java
Dao dao = new SimpleDao(dataSource, connectionHandler)
```