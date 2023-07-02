
### Build .jar application

#### Linux:
```
./gradlew build
```

#### Windows:

```
./gradlew.bat build
```

### Build docker image

#### Linux:
```
./gradlew bootBuildImage
```

#### Windows:

```
./gradlew.bat bootBuildImage
```

### Run docker image

```
docker run -p 8080:8080 rate-limiter-task:1.0.0
```

### Get request

```
curl -i http://localhost:8080/hello
```

### Parametrs:


 Parameter | Available values    | Meaning
-----------------------------------|---------------------|----------------------------------------
`org.zhu4.rate-limiter.cache.type` | caffeine, redisson  | The type of rate limiting cache to use
`org.zhu4.rate-limiter.cache.expireAfterMillis` | 0 - `Long.MAX_VALUE`| The lifetime of objectsin the cache. Should be more than `intervalInMillis`
`org.zhu4.rate-limiter.configs.*.maximumRequests`| 0 - `Long.MAX_VALUE`| The maximum number of requests  for a given interval of time
`org.zhu4.rate-limiter.configs.*.intervalInMillis`| 0 - `Long.MAX_VALUE` | Time interval
`org.zhu4.rate-limiter.configs.*.algorithm`| fixed-window, sliding-window | Rate limiting algorithm
