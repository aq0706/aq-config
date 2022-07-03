# aq-config
This is a simple configuration center, basically no libraries are used, it's just for learning.

## library self build list
- JSON
- JDBC connection pool
- HTTP server

## How to use
Use IDE to run for now.

- Run the mysql script in [mysql.sql](/scripts/db/initialization.sql)
- Start server [Bootstrap](core/src/main/java/com/github/aq0706/config/AqConfig.java)
- Add (namespace, appName, key) = value
```shell script
curl --location --request PUT 'http://127.0.0.1:17060/config' \
--header 'Content-Type: application/json' \
--data-raw '{
    "namespace": "namespace",
    "appName": "appName",
    "key": "key",
    "value": "value"
}'
```
- Run [Example](example/src/main/java/com/github/aq0706/config/example/ExampleApplication.java), and see result.
```
2022-07-03 12:00:30.417  INFO 13912 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''
2022-07-03 12:00:30.417  INFO 13912 --- [           main] c.g.a.config.example.ExampleApplication  : Started ExampleApplication in 1.486 seconds (JVM running for 1.757)
get(namespace, appName, key) = value
```

## Benchmark
local machine:
```
Benchmark                   Mode  Cnt   Score   Error   Units
ConfigClientBenchmark.get  thrpt   10  12.140 ± 0.448  ops/ms
```