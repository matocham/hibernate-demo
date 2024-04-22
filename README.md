# Hibernate and Spring Data capabilities investigation

The purpose of this repository is to explore Hibernate and Spring Data internals in a set of test that try-out various features.

A simple validation framework based on Hibernates `StatementInterceptor` was implemented.
It collects all the CRUD operations counts and sequence access for individual 
tables/sequences. At the end of each test it is expected that all CRUD operations 
counts were validated. If there is an outstanding query left, then test fails.

Test cases covered:
- different behavior for `cascade` option on `@OneToMany` relationships
- `orphanRemoval` tests
- eager and lazy fetch types
- set vs list in one-to-many and many-to-many relationships
- `@ElementCollection` annotation examples for simple and complex types
- differences between identity and sequence id generation
- unidirectional parent-to-child relationships
- optimistic locking tests with version as primitive and boxed types
- manual id generation with primitive and boxed types
- persists and merge behavior for new and existing entities
- Spring Data `save` method algorithm for checking if entity is new
- `@ManyToMany` annotation comparison with 2 composed `@OneToMany` annotations
- transaction isolation levels and pessimistic locking tests

There is a problem with cleaning up state after each test as deletes run in `@AfterEach` 
are counted by `StatementInterceptor` and cause errors because of unchecked db operations.
On the other hand having it as JUnit extension allows to check for missed db operations only
when no other exception was thrown in the test body which gives nice user experience
