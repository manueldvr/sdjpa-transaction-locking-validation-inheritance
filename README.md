# Spring Data JPA Order Service
# Transactions - Locking - Validation - Inheritance

<br>

__content__

- Transactions
- - Overview
- - Transaction Isolation Levels
- - Declared with the @Transactional Annotation
- Locking
- - Lost Update Scenario
- - JPA Pessimistic Locking
- - JPA Optimistic Locking
- Lazy Initialize Error
- Transactional Proxy Mode
- Validation
- - Java Bean Validation
- - Adding Validation
- Inheritance

<br>

---

## Overview of Database Transactions

### ACID

__Atomicity :__

all ops are  completed successfully or database is returned to its previous state.

__Consistency :__

operations  do not violate system integrity constrains.

__Isolated :__

results are independent of concurrent transactions.

__Durable :__

results are made persistent in case of system failure.


### Important terms

__Transaction__ A unit of work. <br>
Typically DML (and __Not__ DDL) statements which update data.

__Commit__ indicates end of transaction. <br>
And tells the database make the operations permanent. <br>
More efficient to do multiple operations in a transaction. There is a 'cost' with commits.

> Note: about __commits__:  When the transaction is complete and it tells the database to make those changes permanent.
So from a database perspective, it is typical more efficient to do multiple operations in a transaction because there is a cost with commits does cause the database some work to start a transaction, monitor, everything and then actually complete that transaction.
So if you had 10,000 update statements, it's more efficient.
Do those in a single transaction, assuming that you want all 10,000 to complete or rollback, and that
comes up to the point of a rollback statement.

__Rollback__ reverts all the changes of the transaction.

__Save Point__ programmatically point you can set, which allows to rollback.

> Note: about __Save Points__: working with 100,000 statements into it.
An error occurs and they all roll back.
So the database rolls back to the previous date of that and then this gets a little complex, but you can do save points.
That is a use case that you can do with the JDBC drivers where you do a save point in the transaction.

__Database Lock__

DBs will lock the records (tables or database) to prevent other processes for changing.
- With ACID compliance.

Within a transaction the following DML statements lock records of the affected rows:<br>
SELECT FOR UPDATE, UPDATE, DELETE

During the transaction other sessions attempting to modify locked records will by default wait for the lock to be released.

__Deadlocks__ occurs when 2 transactions lock each other and can never complete.
Both fail and rollback.

### Transaction Isolation Levels

__Repeatable Read__<br>
Default Isolation Level, statements receives a consistent view of the database, even if other transactions are commited during my transaction.
+ + My transaction gets a snapshot of the data, which does not change.

> the snapshot is an image of the database at a point in time, and that image in not going to change.


__Read commit__<br>
reads whithin my transaction will receive a fresh sanpshot of the data as commits occur while my transaction is running.

__Read Uncommited__<br>
reads are not consistent. but may avoid additional database lock.  Aka "Dirty Read".

__Serializable__<br>
smiliar to Repeatable Read, but may lock rows selected in transaction.

### Pragmatic Concepts

Using the default transaction Isolation level, my transaction see's a snapshot of the database as it is at the start of the transaction.
- Changes made in other sessions and commited __WILL NOT__ be visible.
- Changes made by my session __WILL NOT__ be visible to other sessions until commit.
Most modern RDBMS do a good job of ACID compliance.
- Support for ACID with NoSQL database varies widely by vendor.
- ACID compliance is complex and costly.

### Lost Update Scenario
> This is handled by the database, so Session A is going to have a lock. Session
B can actually do the read and also issue a change, but Session B is going to be blocked from doing
any work.
So it will wait for a Session A to complete.
So basically what we want to say here is we would want that value to be 20 if Session B saw the quantity
was 15, but it read 10.
So what happens, Session A commits a record releasing the lock.
The database record is updated 15. Session B also issued an update to 15 because it thought it was 10
and was adding 5, updated at 15.
So now we have 15 in the database record and not 20, which is what we want in a perfect world.
So the update of Session A is lost.

How deal with this:

__JDBC Locking Modes__ JDBC drivers support several different locking modes. Mode applies to lifespan of the connection. Configuration is very vendor dependent.

Rarely used in practice.

JPA/Hibernate is generally favored.

#### JPA locking

__Pessimistic Locking__

- Database mechanism are used to lock records for updates.
- Capabilities vary widely depending on database and version of JDBC driver used.
- Simplest version is "SELECT FOR UPDATE..." -Locks row or rows until commit or rollback is issued.

What happens is that Select will only run when I can obtain an exclusive lock on those database records. So if there's another transaction happening, I will wait until I can get an exclusive lock and then I will get committed data and then my transaction starts.

This prevent a lost update.

__Optimistic Locking__

Done by checking a version attribute of the entity.

For read more often than update.

### Multi-Request Conversations

Occurs in web form applications, or possible RESTfull too, where the update logic is over 1 or more requests, thus leaving a larger window of time. So Pessimistic Locking will be very fast.

And Optimistic would be the best way to detect that situation.

### JPA Pessimistic Locking

Now going back to JPA Pessimistic Locking. It does have several lock modes to be aware of:
- PESSIMISTIC_READ, that is going to use a shared lock.
This is going to prevent data from being updated or deleted.
- PESSIMISTIC_WRITE uses an exclusive lock and that is going to prevent data from being read and some isolation levels, updated or deleted. And then
- PESSIMISTIC_FORCE_INCREMENT.
That is going to be an exclusive lock and also requires an increment of the version property of the
entity.

Most databases are going to support
PESSIMISTIC_WRITE.<br>
That's an option typically use.
And if you are using a version property, a good option is to use PESSIMISTIC_FORCE_INCREMENT.

Again, this is for Pessimistic Locking.
Next is something that you're going to see quite commonly is the Optimistic Locking.
This is going to use a version property on the entity.
So this is going to be a database column that is specifically there for the version.
It can be an int primitive, an integer, primitive, long, long, short, short or a timestamp.
By far the most common that I see in practice is going to be using the integer, the box, primitive
type.
So that's typically what you're going to use.
Most use cases.

### JPA Optimistic Locking

This is going to use a version property on the entity.<br>
So this is going to be a database column that is specifically there for the version.
It can be an int primitive, an integer, primitive, long, long, short, short or a timestamp.
By far the most common that I see in practice is going to be using the integer, the box, primitive type.

Prior to an update, Hibernate will read the corresponding database record. If the version does not match, an exception is thrown.

So we do have several different JPA Optimistic Lock Modes:
- OPTIMISTIC.
That is going to do an optimistic reading lock for all entities with a version attribute. And then force
increment.
- OPTIMISTIC_FORCE_INCREMENT,
That is going to be the same as OPTIMISTIC, but that is going to guarantee an increment of the version
value.
- Then left over from JPA 1
is READ.
That's going to be the same as OPTIMISTIC.
- WRITE,
that is going to be the same as OPTIMISTIC_FORCE_INCREMENT.

<br>

---

## Database Locking demo

Using `testDBLock`.

SELECT * FROM orderservice.order_header where id = 1 for update;
and do not commit or rollback.

---

## Spring Data JPA Transaction

By default, Spring Data JPA is going to perform an implicit transaction.
This means that the repository methods themselves will create a transaction.

So when you call that out of a transactional context, when you're calling that repository method, it will create and commit a transaction and if there's a data being changed.

So, JPA have two types of implicit transaction:
- Read ops  are done in a _read only context_.

> ex: `findBy` methods.

> NOTE:  I do want to point out the _Read Only_, use that with caution.
What Hibernate is going to do, It's going to do some dirty checks that are going to get skipped.
It does help in some cases making Hibernate a little bit more performant.
__However__, if you take an object from a read only context and then update it and save, because the dirty checks are being skipped, you might encounter issues.<br>
So __there are use cases where bad things can happen__.
So just be aware of that if you're going to be using a _read only context_ and then updating the object.


- Updates and Deletes are done with the _default transactional context_.

### Now for testing Spring Boot transactions.

> Spring Boot by default will create a transaction for _tests_ and roll it back.

> So everything that we're doing in a Spring Boot test is running under a transactional context.<br>
What this means is the Spring Data JPA Implicit transactions are NOT used in a test context.<br>
They are only used outside of a transactional context.

So meaning when we do run a JUnit test under Spring Boot, they are going to be using the test context of Spring Boot itself.

So f you have a method as doing one or more repository calls, you

might see different results when it's run outside of the test context because you are going to be working with Hibernate objects outside the Hibernate session.

And if you have something like a lazy load list and you're trying to do that outside of the Hibernate context, you will get an error so you can get unexpected results because of this.


So remember, _Spring Boot_ is creating a _transactional context_ that everything is going to run in, including your method under test when you are running and the container you're not running it under a transactional
context, only transactions that you declare or implicitly by Spring Data.

### Declared  with the @Transactional Annotation

Spring Framework does provide this in a package called `org.springframework.transaction.annotation`.

Easily confused is JEE, also has a transactional annotation in the package `javax.transaction`. Spring does support either option.

> Spring Framework’s consistent programming model. <br>
Spring resolves the disadvantages of global and local transactions. It enables application developers to use a consistent programming model in any environment. You write your code once, and it can benefit from different transaction management strategies in different environments. The Spring Framework provides both declarative and programmatic transaction management. Most users prefer declarative transaction management, which is recommended in most cases.



#### Transactional Annotation Attributes:
from springframework:

- __value / transactionManager__ - the name of the Transaction Manager to use
- __label__ String to describe a transaction
- __Propagation__ - The Transaction Propagation Type
-__Isolation__ - Transaction Isolation Level
- __timeout__ Timeout for Transaction to complete
- __readOnly__ is read only.
- __rollbackFor / rollbackforClassName__ Exceptions to rollback for
- __NoRollbackFor / noRollbackforClassName__ Exceptions to NOT rollback for.


#### @Transactional - Transaction Manager

Spring Boot will auto-configure an instance of a Transaction Manager depending on your dependencies.
- Spring Framework provides an interface called `PlatformTransactionManager`.
  -  Implementations available for JDBC, JTA (JEE), Hibernate, etc
  - Spring Boot auto-configures the appropriate implementation.
- The auto-Configured instance named ‘transactionManager’.

#### @Transactional - Transaction Propagation

This control how the transaction is created, the life cycle.

- REQUIRED - (Default) - use existing, or create new transaction
- SUPPORTS - Use existing, or execute non-transactionally if none exists
- MANDATORY - Support current, throw exception in none exists
- REQUIRES_NEW - Create new, suspend current
- NOT_SUPPORTED - Execute non-transactionally, suspend current transaction if exists
- NEVER - Execute non-transactionally, throw exception if transaction exists
- NESTED - Use nested transaction if transaction exists, create if not


#### Implicit Transactions

example:

```
  1️⃣
+ public void doSomething() {
  Customer customer = getCustomerMethod();
  updateCustomer(customer);
}

  2️⃣
+ public void getCustomerMethod() {
  return customerRepository.getById(33L);
}

  3️⃣
+ public void updateCustomer(Customer customer) {
  customerRepository.setCustomerName(" New Name");
}
```
1️⃣: The 'doSomething' method does not have any transactional scope.
It does not have access to the Hibernate session state.
So that is completely working out of scope of the Hibernate transaction and transactional scope.

2️⃣: However, the private methods that are called a  `getCustomerMethod`, from customer repository, that method call is an implicit transaction.

3️⃣: the `updateCustomerMethod`. Again, that is an implicit transaction.
> It specifically on the update method when we were using the implicit transaction that would have been limited to the `save` action.


Returning to the example:
```
+ public void doSomething() {
  Customer customer = getCustomerMethod();
  updateCustomer(customer);
}

  2️⃣
@Transactional  
+ public void getCustomerMethod() {
  return customerRepository.getById(33L);
}

  3️⃣
@Transactional
+ public void updateCustomer(Customer customer) {
  customerRepository.setCustomerName(" New Name");
}
```
Now, effectively the method that you are annotating that is going to be guaranteed to be contained in the transactional scope.

#### Inherit Transactions
Now how things get inherited.
You can see here now I've added transactional to the top level method 1️⃣ of `doSomething`.

```
1️⃣
@Transactional                 <- parent transaction
+ public void doSomething() {
  Customer customer = getCustomerMethod();
  updateCustomer(customer);
}

2️⃣ > use parent transaction
@Transactional  
+ public void getCustomerMethod() {
  return customerRepository.getById(33L);
}

3️⃣ > use parent transaction
@Transactional
+ public void updateCustomer(Customer customer) {
  customerRepository.setCustomerName(" New Name");
}
```
Having those annotated with transactional and the default behavior of the transactional is going to go ahead and use the existing transaction.

Last case:

```
1️⃣
@Transactional                 <- parent transaction
+ public void doSomething() {
  Customer customer = getCustomerMethod();
  updateCustomer(customer);
}

2️⃣ > use parent transaction
+ public void getCustomerMethod() {
  return customerRepository.getById(33L);
}

3️⃣ > use parent transaction
+ public void updateCustomer(Customer customer) {
  customerRepository.setCustomerName(" New Name");
}
```
And then when it calls the child method because of the way they are configured, they will go ahead and inherit the parent transaction.

#### Child Transactions

```
1️⃣
@Transactional                 <- parent transaction
+ public void doSomething() {
  Customer customer = getCustomerMethod();
  updateCustomer(customer);
}

2️⃣ > use parent transaction
@Transactional (propagation = Propagation.REQUIRED)
+ public void getCustomerMethod() {
  return customerRepository.getById(33L);
}

3️⃣ > creates new child transaction
@Transactional (propagation = Propagation.REQUIRED_NEW)
+ public void updateCustomer(Customer customer) {
  customerRepository.setCustomerName(" New Name");
}
```
At 2️⃣ added a propagation required. So this use the existing transaction in this case, it does have an existing transaction, so we'll use that. So really no functional change there.

But if this was called by something else that did not have a transaction, this in this case, it would create one.

3️⃣ Here the parent transaction will be suspended and new transaction will start the work of that method will complete in that transactional scope and then control will return back to the parent transaction when that work is complete.


---

## Create Bootstrap Class

In order to run some experiments with the transactional context, I'm doing kind of a workaround, so to speak, from the Spring test context.

Using what Spring Boot calls as a __Command Line Runner__.
This is a class that will execute on startup and in the normal Spring Context.

So it's not going to be used in test, it's going to run as part of the Spring application on startup and it will allow us to run some different scenarios in it.

A new class is created: `bootstrap.Bootstrap`, and to get this to run at startup It will extends  `CommandLineRunner`. Spring Boot is going to go through and look for beans that implement command line runner and then run them.

And we do have to annotate this as a component or a service.

> And going forward in the class, I am going to be using this to allow us to work with the Spring Data Repositories in a normal Spring Context. Remembering tests. This is a workaround because the Spring Boot Test are going to get a transaction wrapped around it by Spring Boot.

---

## Lazy Initialize Error

Happens when we work outside the Hibernate context.

code example:

```
*️⃣
@Override
public void run(String... args) throws Exception {
    System.out.println("[*] Called at bootstrap...");
    // find will run inside an implicit transaction
    OrderHeader orderHeader = this.orderHeaderRepository.findById(44L).orElseGet(OrderHeader::new);
    orderHeader.getOrderLines().forEach(orderLine -> {
        System.out.println("Description of every o.line : " + orderLine.getProduct().getDescription());
  🚩    orderLine.getProduct().getCategories().forEach(category -> {
            System.out.println("Category : " + category.getDescription());
        });
    });
}
```
will show an exception `LazyInitializationException`:
```
java.lang.IllegalStateException: Failed to execute CommandLineRunner
...
Caused by: org.hibernate.LazyInitializationException: failed to lazily initialize a collection of role:
guru.springframework.orderservice.domain.Product.categories,
could not initialize proxy - no Session
...
```
What happens?

That is because the category property is not initialized and Hibernate needs to go out.

And so basically there's a proxy for the categories and it's a Hibernate proxy and it needs a session to work. So what's happening there 🚩 is we are outside the transactional context, we're outside of the session.

I don't have any way to talk to the database. So it's a Hibernate proxy that is going to handle getting categories. It needs to issue another database statement, but we're outside the session. So the solution to this is to go ahead and mark this as transactional *️⃣.

Now all execution within the `run` method will be inside a transactional context. So in this case, when we get down here, we will still have the Hibernate session available for that proxy to work.

Before annotate `run` logs shown two queries:
```
Hibernate:
    select
        orderheade0_.id as id1_3_0_,
        orderheade0_.created_date as created_2_3_0_,
        ...
        orderheade0_.shipping_zip_code as shippin12_3_0_
    from
        order_header orderheade0_
    where
        orderheade0_.id=?
Hibernate:
    select
        orderlines0_.order_header_id as order_he5_4_0_,
        ...
        product1_.id as id1_5_2_,
        product1_.created_date as created_2_5_2_,
        product1_.last_modified_date as last_mod3_5_2_,
        product1_.description as descript4_5_2_,
        product1_.product_status as product_5_5_2_
    from
        order_line orderlines0_
    left outer join
        product product1_
            on orderlines0_.product_id=product1_.id
    where
        orderlines0_.order_header_id=?
```
After, with `@Transactional`:
```
Hibernate:
    select
        orderheade0_.id as id1_3_0_,
        ...
        orderheade0_.shipping_zip_code as shippin12_3_0_
    from
        order_header orderheade0_
    where
        orderheade0_.id=?
Hibernate:
    select
        orderlines0_.order_header_id as order_he5_4_0_,
          ...
        product1_.id as id1_5_2_,
        product1_.created_date as created_2_5_2_,
        product1_.last_modified_date as last_mod3_5_2_,
        product1_.description as descript4_5_2_,
        product1_.product_status as product_5_5_2_
    from
        order_line orderlines0_
    left outer join
        product product1_
            on orderlines0_.product_id=product1_.id
    where
        orderlines0_.order_header_id=?
Description of every o.line : Product 3
Hibernate:
    select
        categories0_.product_id as product_1_6_0_,
        categories0_.category_id as category2_6_0_,
        category1_.id as id1_0_1_,
        category1_.created_date as created_2_0_1_,
        category1_.last_modified_date as last_mod3_0_1_,
        category1_.description as descript4_0_1_
    from
        product_category categories0_
    inner join
        category category1_
            on categories0_.category_id=category1_.id
    where
        categories0_.product_id=?
Description of every o.line : Product 1
Hibernate:
    select
        categories0_.product_id as product_1_6_0_,
        categories0_.category_id as category2_6_0_,
        category1_.id as id1_0_1_,
        category1_.created_date as created_2_0_1_,
        category1_.last_modified_date as last_mod3_0_1_,
        category1_.description as descript4_0_1_
    from
        product_category categories0_
    inner join
        category category1_
            on categories0_.category_id=category1_.id
    where
        categories0_.product_id=?
Description of every o.line : Product 2
Hibernate:
    select
        categories0_.product_id as product_1_6_0_,
        categories0_.category_id as category2_6_0_,
        category1_.id as id1_0_1_,
        category1_.created_date as created_2_0_1_,
        category1_.last_modified_date as last_mod3_0_1_,
        category1_.description as descript4_0_1_
    from
        product_category categories0_
    inner join
        category category1_
            on categories0_.category_id=category1_.id
    where
        categories0_.product_id=?
```

---

## Transactional Proxy Mode

Clarification about how transactional scope works.

If I extract the `run` logic to a new method like:
```
@Override
public void run(String... args) throws Exception {
    getCategoryInOrder();
}

@Transactional
public void getCategoryInOrder() {
    System.out.println("[*] Called at bootstrap...");
    OrderHeader orderHeader = this.orderHeaderRepository.findById(44L).orElseGet(OrderHeader::new);
    orderHeader.getOrderLines().forEach(orderLine -> {
        System.out.println("Description of every o.line : " + orderLine.getProduct().getDescription());
        orderLine.getProduct().getCategories().forEach(category -> {
            System.out.println("Category : " + category.getDescription());
        });
    });
}
```
Even thought it is annotated as transactional it fails:

```
Caused by: org.hibernate.LazyInitializationException: failed to lazily initialize a collection of role: guru.springframework.orderservice.domain.Product.categories, could not initialize proxy - no Session
```
With same reason than before, no Session.

> So what's happening here is we are in a __proxy mode__ for the external call, so only external method calls coming through the proxy or intercepted for transactional.

the external method calls are methods like `run`, but `getCategoryInOrder` is an internal call. So the `@Transactional`  annotation / transactional aspect of our programming  does not get picked up because of how it is implemented by default.

#### Solution

Refactoring by creating a class dependency:
`guru.springframework.orderservice.bootstrap.GetCategoryByOrderService` with the transactional method `getCategoryInOrder`, that now is an  external method.


---

## Adding Version Property

In this part, we are going to take a look at our first few steps of setting up optimistic locking. <br>
And for that, we need to set up a version column and a version property on the entity.<br>
So we are going to be working with the customer entity and can see here on the screen I have the SQL statement to alter the table, to add in the column of a version so creatively named version, and it is an integer (see V13.0).

So, `Customer` will have a version int property annotated with `@Version`.




---

## Optimistic Locking Demo

Context to show the context of the demo:
```
@Autowired
GetCategoryByOrderService getCategoryByOrderService;

@Autowired
CustomerRepository  customerRepository;


@Override
public void run(String... args) throws Exception {
    getCategoryByOrderService.getCategoryByOrderService();

    Customer customer = new Customer();
    customer.setCustomerName("Testing Version 1 first");
    Customer savedCustomer = customerRepository.save(customer);
    System.out.println("first Version is: " + savedCustomer.getVersion());

    savedCustomer.setCustomerName("Testing Version 2");
    customerRepository.save(savedCustomer);
    System.out.println("2 - Version is: " + savedCustomer.getVersion());

    savedCustomer.setCustomerName("Testing Version 3");
    customerRepository.save(savedCustomer);
    System.out.println("3 - Version is: " + savedCustomer.getVersion());
}
```

this code generates an exception:

```
...
Caused by: org.springframework.orm.ObjectOptimisticLockingFailureException: Object of class [guru.springframework.orderservice.domain.Customer] with identifier [3]: optimistic locking failed; nested exception is org.hibernate.StaleObjectStateException: Row was updated or deleted by another transaction (or unsaved-value mapping was incorrect) : [guru.springframework.orderservice.domain.Customer#3]
...
Caused by: org.hibernate.StaleObjectStateException: Row was updated or deleted by another transaction (or unsaved-value mapping was incorrect) : [guru.springframework.orderservice.domain.Customer#3]
...
```

#### Reason

And I expect it's going to error because I intentionally made a mistake because I want to demonstrate the optimistic locking. You can see that we did in fact get this error. So we can see state exception.

You can see Row was updated or deleted by another transaction or unsaved value mapping was incorrect.

So basically it's saying that the expected version did not match the provided version. And why that is, is I intentionally saved the customer and I continue to use that object rather than taking the property back from it.

#### Solution

create new objects at every step:

```
@Override
public void run(String... args) throws Exception {
    getCategoryByOrderService.getCategoryByOrderService();

    Customer customer1 = new Customer();
    customer1.setCustomerName("Testing Version 1 first");
    Customer savedCustomer2 = customerRepository.save(customer1);
    System.out.println("first Version is: " + savedCustomer2.getVersion());

    savedCustomer2.setCustomerName("Testing Version 2");
    Customer savedCustomer3 =customerRepository.save(savedCustomer2);
    System.out.println("2 - Version is: " + savedCustomer3.getVersion());

    savedCustomer3.setCustomerName("Testing Version 3");
    Customer savedCustomer4 = customerRepository.save(savedCustomer3);
    System.out.println("3 - Version is: " + savedCustomer4.getVersion());

(*) customerRepository.deleteById(savedCustomer2.getId());
}
```
(*) this last line, will fail if it is replaced by:
`customerRepository.delete(savedCustomer);`
That is because it is an stale object.

But will success when I use the last version:
`customerRepository.delete(savedCustomer4);`

 So very important toolset that we have with Hibernate as far as detecting stale data.

---

## What is considered a "long lived" transaction?

A long-lived transaction is a transaction that spans multiple database transactions. The transaction is considered "long-lived" because its boundaries must, by necessity of business logic, extend past a single database transaction.

A long-lived transaction can be thought of as a sequence of database transactions grouped to achieve a single atomic result.
A common example is a multi-step sequence of requests and responses of an interaction with a user through a web client.

A transaction such as a user input form, that can require a period of time waiting for user input for the transaction to complete. This can be seconds or minutes, compared to a database transaction which typically completes in milliseconds.

---

## Optimistic Locking to Orders  & Add Quantity On Hand to Product

__Optimistic Locking to Orders__

- Add Optimistic Locking to Order Header and Order Line.
- Create flyway migration script for version property.
- Add Version Property and annotation to entities.
- Verify Tests still pass.


__Alter the Product entity to include a property for Quantity on Hand (quantityOnHand)__

- Use Integer for the property
- Create flyway migration script
- Set existing products to have quantity of 10
- Alter Product Entity for new property
- Write test to set quantity, then update the quantity


---

## Version Property cannot be Null

changing SQL:

```
update order_header
    set version = 0 where version = null;

update order_line
    set version = 0 where version = null;
```

And
```
update order_header
    set version = 0 where version = null;

update order_line
    set version = 0 where version = null;
```



---

## Pessimistic Locking

At `ProductRepositoryTest.testSaveAndUpdateProduct` I use the `ProductService` implementation:

```
@Autowired
ProductService productService;

@Test
void testSaveAndUpdateProduct() {
    Product product = new Product();
    product.setDescription("My Product");
    product.setProductStatus(ProductStatus.NEW);
    Product savedProduct = productService.saveProduct(product);
    Product updatedProduct = productService.updateQOH(savedProduct.getId(), 25);
    assertNotNull(updatedProduct);
    assertNotNull(updatedProduct.getDescription());
}
```
As is generates the following Hibernate output, insert and update:
```
...
Hibernate:
    insert
    into
        product
        (created_date, last_modified_date, description, product_status, quantity_on_hand)
    values
        (?, ?, ?, ?, ?)
Hibernate:
    update
        product
    set
        last_modified_date=?,
        description=?,
        product_status=?,
        quantity_on_hand=?
    where
        id=?
...
```
__Problem__

❗️ But but we are not getting this. We don't have the select for update.

__Solution__

Let's go to the product repository.<br>
`...repository.CrudRepository.findById` should be Override  and annotated with `@Lock(LockModeType.PESSIMISTIC_WRITE)`.

Checking console:
```
Hibernate:
    insert
    into
        product
        (created_date, last_modified_date, description, product_status, quantity_on_hand)
    values
        (?, ?, ?, ?, ?)
Hibernate:
    select
        id
    from
        product
    where
        id =? for update    ⬅️
Hibernate:
    update
        product
    set
        last_modified_date=?,
        description=?,
        product_status=?,
        quantity_on_hand=?
    where
        id=?
```
Hibernate operations:  __insert -> select for update -> update__. <br>
So this is the insert. And I'm doing a select from product by ID for update.<br>
That is going to lock that road exclusively to our process with the database that is going to create the database lock. And then here we can go ahead and perform the update as expected.


---

## Fix Pessimistic Locking Error

Folloging the last chapter, at `Boostrap`, the `updateProduct` method will show an error:
```
Caused by: java.sql.SQLException: Cannot execute statement in a READ ONLY transaction.
```

__Question to be unswer: Why did the error occur in the bootstrap service, and not in the Spring Boot test?__

__Answer:__
The Spring Boot Test creates a transaction by default, thus statements run within a transactional context (provided by Spring Boot). Error occurred from running outside of a transaction.

__1__

At this code `findById` is executed within an implicit read only transaction.
```
@Override
public Product updateQOH(Long id, Integer quantity) {
    Product product =  this.productRepository.findById(id).orElseThrow();
    product.setQuantityOnHand(quantity);
    return productRepository.saveAndFlush(product);
}
```
__2__

Also `productRepository.saveAndFlush(product)` run in a transaction, a different transaction.

__3__

On the contrary at test  `ProductRepositoryTest.testSaveAndUpdateProduct` runs successfully in a transaction, as log shows:
```
...
...  o.s.t.c.transaction.TransactionContext   : Began transaction (1) for test context [DefaultTestContext@7f2cfe3f testClass = ProductRepositoryTest,  ...
...
```

__4__

Solution involves that at `ProductServiceImpl` method should be `@Transactional`.

```
...
@Transactional ⬅️
@Override
public Product updateQOH(Long id, Integer quantity) {
    Product product =  this.productRepository.findById(id).orElseThrow();  1️⃣
    product.setQuantityOnHand(quantity);
    return productRepository.saveAndFlush(product);  2️⃣
}
...
```
Now both operations 1️⃣ (read only transaction) and 2️⃣ (normal transaction) run within a __parent transaction__.

---

## Validation

- JSR 303 Introduced __Java Bean Validation (Version 1.0)__
- - Set of annotations used to validate Java Bean properties.
- Approved on November 16th, 2009.
- Part of JEE v6 and above.
- JSR 303 Supported by Spring since version 3
- Primary focus was to define annotations for data validation.
- - Largely field level properties
- JSR 349  __Java Bean Validation 1.1__ released on April 10th, 2013.
- - JEE v7, Spring Framework 4
- Builds upon 1.0 specification
- Expanded to method level validation
- - To validate input parameters
- Includes dependency injection for bean validation components.
- JSR 380 __Bean Validation 2.0__ *this project
- Approved August 2017
- Added to Spring Framework 5.0 RC2
- Available in Spring Boot 2.0.0 +
- Uses Hibernate Validator 6.0 + (Implementation of Bean Validation 2.0)
- Primary goal of Bean Validation 2.0 is Java 8 language features
- Added ~11 new built in validation annotations
- Remainder of presentation will focus on Bean Validation 2.0

### Java Bean Validation Overview

#### Built In Constraint Definitions

- @Null - Checks value is null
- @NotNull - Checks value is not null
- @AssertTrue - Value is true
- @AssertFalse - Value is false
- @Min - Number is equal or higher
- @Max - Number is equal or less
- @DecimalMin - Value is larger
- @DecimalMax - Value is less than
- @Negative - Value is less than zero. Zero invalid.
- @NegativeOrZero - Value is zero or less than zero
- @Positive - Value is greater than zero. Zero invalid.
- @PositiveOrZero - Value is zero or greater than zero.
- @Size - checks if string or collection is between a min and max
- @Digits - check for integer digits and fraction digits
- @Past - Checks if date is in past
- @PastOrPresent - Checks if date is in past or present
- @Future - Checks if date is in future
- @FutureOrPresent - Checks if date is present or in future
- @Pattern - checks against RegEx pattern
- @NotEmpty - Checks if value is not null nor empty (whitespace characters or empty
collection)
- @NonBlank - Checks string is not null or not whitespace characters
- @Email - Checks if string value is an email address

#### Hiberate Validator Constraints

- @ScriptAssert - Class level annotation, checks class against script
- @CreditCardNumber - Verifies value is a credit card number
- @Currency - Valid currency amount
- @DurationMax - Duration less than given value
- @DurationMin - Duration greater than given value
- @EAN - Valid EAN barcode
- @ISBN - Valid ISBN value
- @Length - String length between given min and max
- @CodePointLength - Validates that code point length of the annotated character
sequence is between min and max included.
- @LuhnCheck - Luhn check sum
- @Mod10Check - Mod 10 check sum
- @Mod11Check - Mod 11 check sum
- @Range - checks if number is between given min and max (inclusive)
- @SafeHtml - Checks for safe HTML
- @UniqueElements - Checks if collection has unique elements
- @Url - checks for valid URL
- @CNPJ - Brazilian Corporate Tax Payer Registry Number
- @CPF - Brazilian Individual Taxpayer Registry Number
- @TituloEleitoral - Brazilian voter ID
- @NIP - Polish VAR ID
- @PESEL - Polish National Validation Number
- @REGON - Polish Taxpayer ID

#### Validation and Spring Framework

- Spring Framework has robust support for bean validation
- Validation support can be used in controllers, and services, and other Spring managed
components
- Focus in this course will be on support with in Spring Data JPA
- Annotated entities will be validated before persistence operations
- Runtime exception is thrown if there is a validation constraint error

#### Spring Boot and Validation

Spring Boot will auto-configure validation when the validation implementation is found on
classpath<br>
If API is only on classpath (with no implementation) you can use the annotations, BUT
validation will NOT occur. <br>
• Prior to Spring Boot 2.3, validation was included in starter dependencies<br>
• After Spring Boot 2.3, you must include the Spring Boot validation starter.

#### What to Validate?
Generally, validation constraints should reflect the database constraints<br>
• Validation Constraint Errors are much more friendly that database constraint errors<br>
• Also, you will receive info on all constraint errors (vs DB which is just first error)<br>
If a database string has a max length of 50, the entity should also reflect this<br>
Use @NonEmpty or @NonBlank for required String properties - a space is a valid string<br>
Generally DO NOT validate Hibernate managed properties<br>
• ie requiring a database managed id property or version property could cause errors<br>


### Java Bean Validation Maven Dependencies

Update my Maven dependency for the validation API. To get that brought in.
And with that being on the class path now Spring Boot will auto configure the validation for us and
then Spring Data JPA, the persistence layer will automatically validate the entities at persistence time.

refresh Maven
```
<dependency>
     <groupId>org.springframework.boot</groupId>
     <artifactId>spring-boot-starter-validation</artifactId>
 </dependency>
 ```

### Adding Validation

```
@Entity
public class Customer extends BaseEntity {

    @Length(max = 50)
    private String customerName;
```

test at

```
@Test
void testSaveOrder() {
    OrderHeader orderHeader = new OrderHeader();
    Customer customer = new Customer();
    customer.setCustomerName("New Customer01234567890123456789012345678901234567890123456789");
    Customer savedCustomer = customerRepository.save(customer);
```

will show an error with a list of constrains validations:

```
jakarta.validation.ConstraintViolationException: Validation failed for classes [guru.springframework.orderservice.domain.Customer] during persist time for groups [jakarta.validation.groups.Default, ]
List of constraint violations:[
	ConstraintViolationImpl{interpolatedMessage='la longitud debe estar entre 0 y 50', propertyPath=customerName, rootBeanClass=class guru.springframework.orderservice.domain.Customer, messageTemplate='{org.hibernate.validator.constraints.Length.message}'}
]
```
### Code example

Complete adding validation constraints to Customer Entity and Address Entity.
- - Refactor to use @Size (Bean Validation 2.0) vs @Length (Hibernate)

Match @Size to maximum database column size<br>
Verify email is a valid email address

Optional: Write Tests to verify constraints are working
- - Best practice is to have test coverage for constraints
- - At a minimum verify expected exception is thrown


### When And Where to Use validation?


---





---


# References

InfoQ book that provides a well-paced introduction to transactions. [Java Transaction Design Strategies](https://www.infoq.com/minibooks/JTDS/)

[Spring Framework’s consistent programming model](https://docs.spring.io/spring-framework/docs/4.2.x/spring-framework-reference/html/transaction.html#transaction-programming-model)

[Understanding the Spring Framework transaction abstraction](https://docs.spring.io/spring-framework/docs/4.2.x/spring-framework-reference/html/transaction.html#transaction-strategies)



## Connect with Spring Framework Guru
* Spring Framework Guru [Blog](https://springframework.guru/)
