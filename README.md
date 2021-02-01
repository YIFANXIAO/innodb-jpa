
# InnoDB的锁和在JPA使用



## 一、悲观锁和乐观锁

### 乐观锁

指对于来自外界的修改，持乐观态度。认为数据在一般情况下不会造成冲突，所以只在更新时，才会对数据是否冲突进行检测。

通常的做法如下

~~~
1、数据库表三个字段，分别是id、value、version
select id,value,version from TABLE where id = #{id}

2、每次更新表中的value字段时，为了防止发生冲突，需要这样操作，version代指我们设计的某个标识，也可以是时间戳等
update TABLE
set value=2,version=version+1
where id=#{id} and version=#{version}
~~~



### 悲观锁

指对于来自外界（对于数据库来说的外界）的修改持悲观态度。所以，在整个数据处理过程当中，将数据处于锁定状态

悲观锁的实现，往往需要依赖数据库进行实现。（例如数据库级别的排他锁。否则即便在本系统中实现了悲观锁，也无法保证外部系统可能会进行的修改）

**对于悲观锁这个概念，其实又可以引申出来两种锁，分别是共享锁和排他锁**



#### 共享锁（S锁）

又称”读锁“，是针对于读取操作而创建的锁。在共享锁状态下，数据的"版本"会被锁定，使用户可以并发的进行读取数据。

共享锁和排他锁互斥，也就是说，在共享锁生效期间，是不可以对数据进行任何修改（insert、update、delete）



#### 排他锁（X锁）

又称”写锁“，相对于共享锁，程度更深。在排他锁状态下，数据”版本“锁定，除当前事务外，其他事物对于锁定的数据，只可以读取，不能进行修改，只有等待当前锁释放。

排他锁会阻塞所有的排他锁和共享锁。



#### autocommit

在数据库中想要使用排他锁，首先需要关闭innoDB默认的自动提交

在自动提交模式下，update、insert和delete都是会默认以排他锁的方式执行，单独的语句就是一个事物。



## 二、如何在mysql InnoDB中使用共享锁和排他锁



### 共享锁、排他锁使用举例

console1：

```
set autocommit=0;

# init student where id = 1
update student set value = 1 where id = 1;

# 共享锁查询1
start transaction;

select * from student where id = 1 lock in share mode;

update student set value = 2 where id = 1;

commit;

# 排他锁更新1
start transaction;

select * from student where id = 1 for update;

update student set value = 4 where id = 1;

commit;


# 排他锁更新2
start transaction;

select * from student where id = 1 for update;

update student set value = 6 where id = 1;

commit;
```

console2：

```
# 共享锁查询1
select * from student where id = 1;

update student set value = 3 where id = 1;

# 排他锁更新对照1
select * from student where id = 1;

update student set value = 5 where id = 1;

# 排他锁更新对照2
start transaction;

select * from student where id = 1 for update;

update student set value = 7 where id = 1;

commit;
```



### 意向锁

InnoDB支持 *multiple granularity locking*（多元粒度锁定）

允许行锁和表锁共存，而在实际的实现多元粒度锁定的过程中，InnoDB就是用到了意向锁（Intention Lock）



#### 作用：

> Intention locks are table-level locks that indicate which type of lock (shared or exclusive) a transaction requires later for a row in a table

Intention Lock是table-level的锁，作用是标识事务接下来会对这个表的**某一行**添加共享锁或者排他锁。



#### 类型：

分为意向共享锁（IS锁）和意向排他锁（IX锁）

使用逻辑如下：

> - Before a transaction can acquire a shared lock on a row in a table, it must first acquire an `IS` lock or stronger on the table.
> - Before a transaction can acquire an exclusive lock on a row in a table, it must first acquire an `IX` lock on the table.

在向某行添加**共享锁**之前，必须先向表添加**意向共享锁**或者更强的锁（S锁）

在向某行添加**排他锁**之前，必须先向表添加**意向排他锁**



四种锁的冲突情况：

|      |    X     |     IX     |     S      |     IS     |
| :--: | :------: | :--------: | :--------: | :--------: |
|  X   | Conflict |  Conflict  |  Conflict  |  Conflict  |
|  IX  | Conflict | Compatible |  Conflict  | Compatible |
|  S   | Conflict |  Conflict  | Compatible | Compatible |
|  IS  | Conflict | Compatible | Compatible | Compatible |

只有当请求的锁，没有和现有的锁冲突的情况，才可以请求成功，否则会一直等待，直到超时。

意向锁不会阻塞除了全表请求以外的任何东西。



### 锁的三种算法：

#### 记录锁（Record Locks，行锁）：

记录锁是一种对于索引的锁。

例如：

```
select c1 from t where c1 = 10 for update
```

会阻止所有针对于c1 = 10的记录的插入、更新或者删除操作，使用索引来对单行的数据记录进行归约。



记录锁永远会通过索引进行加锁，即便某张表并没有索引。

针对于没有索引的情况，InnoDB也会去**创建隐式的聚集索引**，并使用它进行添加记录锁。



#### 间隙锁（Gap Locks）：

间隙锁是一种针对于**索引区间**的一种锁。

example：

```
SELECT c1 FROM t WHERE c1 BETWEEN 10 and 20 FOR UPDATE
```

这里就对索引c1在（10，20）这个**区间**上加了锁，而**不管这个区间内的值是否真实存在**。



> A gap might span a single index value, multiple index values, or even be empty.

gap 内部可能包含单个的索引值、多个索引值或者是完全空的。



**换一种理解方式**：

如果在（10，20）之间存在值，那么对存在的值，实际使用的是类似记录锁的做法。
对于其他不存在值，就形成了单纯的“gap”，使用算法，锁住这些“不存在”的值。

> the gaps between all existing values in the range are locked.

按上述理解，对于任何可能的“gap”，都可以添加间隙锁。



间隙锁是性能和并发度之间的权衡，并且只在**某些事务隔离等级**下使用。

```
eg： SELECT * FROM child WHERE id = 100;
```

对于使用唯一索引的表来说，是不需要使用间隙锁。（不包括只使用了联合主键当中的某几个当做索引条件的语句）。而对于，使用了非索引或者非唯一索引的语句，会对条件前的区间添加上间隙锁。



间隙锁的**唯一目的在于阻止区间被插入**。所以对于一段间隙来说，是允许多个事务同时持有对它的间隙锁的，这里包括了间隙共享锁（gap S-lock）和间隙排他锁（gap X-lock）。

所以，在这里S锁和X锁之间并不相互冲突，并且功能是相同的。



gap lock可以被显示禁止。当在READ COMMITTED等级下时，gap lock被禁止用于索引查找，而只用于外检约束检查和重复键检查。



#### 临键锁（NEXT-KEY-Lock）：

临键锁是记录锁和间隙锁的一种结合。



在InnoDB执行行级锁定的过程当中，会先去搜索或者扫描一个表的索引，然后对满足条件的索引记录添加共享锁或者排他锁。因此行级锁其实是索引记录锁。



而临键锁的影响范围是一个索引记录本身以及索引记录之前的gap范围。



假定，一张表的索引包含10、11、13和20，那按照临键锁划分方式，可以这样划分

```
(负无穷, 10]
(10, 11]
(11, 13]
(13, 20]
(20, 正无穷)  // 这里，临键锁锁定了最大值以上的所有范围
```



默认情况下，InnoDB是在可重复读(REPEATABLE READ)的事务隔离级别下运行的。在这种事务隔离级别下，InnoDB使用临键锁来进行搜索和扫描索引，防止了“幻读”。



### InnoDB的“锁升级”机制

> `InnoDB` performs locking at the row level and runs queries as nonlocking consistent reads by default, in the style of Oracle.
>
> InnoDB 默认执行**行锁**，查询时会在无锁情况下，使用consistent reads（一致性读，利用快照信息进行读取基于某个时间点的数据，不会去管同时运行的其他事物的更新等。如果原始数据更改，也会根据撤销日志的内容来进行重建。同时，一致读取是InnoDB在READ COMMITTED和REPEATABLE READ隔离级别中处理SELECT语句的默认模式。这种模式不会对表进行加锁）。
>
> The lock information in `InnoDB` is stored space-efficiently so that lock escalation is not needed.
>
> InnoDB 锁信息的保存机制对于空间利用率高，所以**不需要锁升级**。
>
> Typically, several users are permitted to lock every row in `InnoDB` tables, or any random subset of the rows, without causing `InnoDB` memory exhaustion
>
> 通常，几个用户对InnoDB 的表中的每行或者是某个子集进行加锁，是不会导致内存耗尽的。



## 三、InnoDB事务隔离级别

### 数据库常见的读取问题

幻读

重复读

脏读等等

### InnoDB都有哪些事务隔离级别



## 四、如何在JPA当中使用共享锁和排他锁



### 乐观锁推荐实现方式：

```
代码实现举例
场景1：手动实现乐观锁机制
场景2：加@Version，使用乐观锁，正常更新，语句会自动转换
场景3：使用EntityMapper的方式，添加事务锁，采用乐观锁模式，进行数据更新
```



#### @Version注解

在实体类当中，标注某个属性为版本控制属性即可。这样之后，再使用save方法等，就会自动去更新版本控制属性



注意，可以添加@Version注解的属性必须满足以下条件：

- 每个实体类别必须只有一个版本属性
- 对于映射到多个表的实体，必须将其放置在主表中
- 版本属性的类型必须是以下之一：*int*，*Integer*，*long*，*Long*，*short*，*Short*，*java.sql.Timestamp*



### 悲观锁推荐实现方式：

#### LockModeType

是JPA定义的一个用来标注，事务锁的一个枚举类型。

```
		// 共享锁
    PESSIMISTIC_READ,
		// 排他锁
    PESSIMISTIC_WRITE,

    NONE
```



推荐在repo方法上添加Lock注解，指定不同的锁。

```
// 自定义查询方法，这里必须使用JPQL，不可以使用原生sql
// 需要指定在事务当中执行
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT c FROM Customer c WHERE c.orgId = ?1")
public List<Customer> fetchCustomersByOrgId(Long orgId);

// 直接添加在默认方法上，不过需要显式对方法进行声明
@Lock(LockModeType.PESSIMISTIC_READ)
public Optional<Customer> findById(Long customerId);
```





### 不推荐的JAP中使用锁的方式：







