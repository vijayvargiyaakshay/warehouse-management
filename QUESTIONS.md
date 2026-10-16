# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
```
Yes, I would. Right now there are three different ways of talking to the
database. Store is an active record (the entity extends PanacheEntity and saves
itself), Product uses a plain Panache repository, and Warehouse has its own
domain model, a port (WarehouseStore) and a repository adapter around the
DbWarehouse entity.

The problem isn't that any of them is wrong. It's that a new developer has to
guess which one to follow, and the business rules end up in different places:
inside the REST resource for Store and Product, but inside use cases for
Warehouse. That makes the Store and Product rules harder to unit test, because
you need Quarkus and a database just to check a simple validation.

If I were maintaining this, I would pick the Warehouse style as the standard for
anything with real business logic, and move Store towards it step by step. I
would write the tests first so I know the behaviour doesn't change. Product I
would probably leave alone. It is basic CRUD, and adding ports and mappers there
would be more code with no real gain.

I also liked how the Store fix turned out. Sending the legacy update from an
AFTER_SUCCESS observer instead of from the resource keeps the transaction
concern out of the endpoint, and that is the direction I would take the rest of
the code in.

```
----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
```
Both have a place. The nice thing about the spec-first approach we used for
Warehouse is that the yaml file is the contract. The resource has to implement
the generated interface, so the code can't quietly drift away from the
documentation, and other teams can start building against the API before the
backend is ready. The downside is the extra build step. Generated code is a bit
awkward to work with, the IDE sometimes doesn't see it until you compile, and
for a small endpoint it can feel like a lot of ceremony. I ran into a bit of
this myself when mapping our own errors to status codes that the generated
interface doesn't describe.

Writing the endpoints by hand, like Product and Store, is much faster to start
with and easy to change. The risk shows up later: the documentation gets out of
date, and each endpoint ends up handling errors in its own way. You can already
see that here.

My choice would be spec-first for anything other teams or systems consume,
because a stable contract matters more than a bit of setup time. For purely
internal or throwaway endpoints I would accept code-first, but then I would
generate the OpenAPI document from the code and check it in the pipeline so it
can't drift without anyone noticing. In the long run I would like all three APIs
to work the same way.
```
----
3. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```
I would start with the places where a bug costs the most, which here is the
Warehouse business rules: unique business unit code, valid location, the maximum
number of warehouses per location, capacity against the location limit and the
stock, and the replace rules. These are best covered with plain unit tests using
Mockito. They run in milliseconds, need no database, and I can cover the happy
path and every failure for each rule, including the boundary values. The same
goes for the fulfilment limits in the bonus task.

On top of that I would add a smaller set of @QuarkusTest tests that call the real
endpoints with RestAssured against a real PostgreSQL (Dev Services makes that
easy). They check status codes, error messages and that things actually reach
the database. For the Store, I would specifically test that the legacy system is
only called after the commit and is not called when the transaction rolls back,
since that is the riskiest behaviour in that flow. End-to-end tests I would keep
to a handful of key flows, like create, replace and archive.

To keep it healthy over time, the GitHub Actions pipeline runs everything on each
push and pull request, and JaCoCo fails the build if coverage drops under 80%.
I leave out generated code and simple model classes so the number means
something. I also don't treat the percentage as the goal. When I looked at the
JaCoCo report, the stores package was the weakest, so I added tests there
instead of just chasing the total. Every bug fix should come with a test that
would have caught it, and if the team wants more confidence, mutation testing
with PIT on the use cases would show whether the tests really check behaviour.
```