# Using UNNEST with Postgres to improve bulk insert performance

Sample Spring Boot application to demonstrate the speed improvement of UNNEST function in bulk inserts

# References
This project was inspired by [a blog post by James Blackwood-Sewell](https://www.timescale.com/blog/boosting-postgres-insert-performance).
I also found helpful and interesting [this blog post by Forbes Lindesay](https://dev.to/forbeslindesay/postgres-unnest-cheat-sheet-for-bulk-operations-1obg)

# Setup

Just launch a Postgres container (or use your own database server):
```bash
docker run --name pg-bulk-insert --memory 1g -p 54320:5432 -e POSTGRES_PASSWORD=ingestor -d postgres:16.4-alpine3.20
```
And then start the application:
```bash
mvn spring-boot:run
```

The application will continuously perform bulk inserts using both the standard `repository.saveAll()` approach 
and the proposed `INSERT INTO ... SELECT * FROM UNNEST(...)` approach, reporting the speed of each.