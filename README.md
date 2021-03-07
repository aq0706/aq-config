# aq-config
This is a simple configuration center, basically no libraries are used.

## How to use
Use IDE to run for now.

- Step 1
Run the mysql script in [mysql.sql](/scripts/db/initialization.sql)
- Step 2
Run [Bootstrap](core/src/main/java/com/github/aq0706/config/Bootstrap.java)
- Step 3
Put a config
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
-Step 4
Run [Example](example/src/main/java/com/github/aq0706/config/example/ExampleApplication.java), and see result.