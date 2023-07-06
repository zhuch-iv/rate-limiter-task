#### Тестовое задание ecom market

> - Написать spring-boot приложение, которое будет содержать 1 контроллер с одним методом, который возвращает HTTP 200 и пустое тело.
> - Написать функционал, который будет ограничивать количество запросов с одного IP адреса на этот метод в размере N штук в X минут. Если количество запросов больше, то должен возвращаться 502 код ошибки, до тех пор, пока количество обращений за заданный интервал не станет ниже N.
> Должна быть возможность настройки этих двух параметров через конфигурационный файл.
> - Сделать так, чтобы это ограничение можно было применять быстро к новым методам и не только к контроллерам, а также к методам классов сервисного слоя.
>
> Реализация должна учитывать многопоточную высоконагруженную среду исполнения и потреблять как можно меньше ресурсов (важно!).
> - Также написать простой JUnit-тест, который будет эмулировать работу параллельных запросов с разных IP.
> !!! Не использовать сторонних библиотек для троттлинга.
>
> Список технологий и инструментов:
> Код должен быть описан на Java 11 (или выше)
> Фреймворки: Spring + Spring Boot
> Для сборки использовать Gradle. Возможны другие вспомогательные библиотеки.
>
> - Написать JUnit тест с использованием JUnit 5.x (Junit Jupiter)
> - Написать простой dockerfile для обёртки данного приложения в докер



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


| Parameter                                          | Available values                | Meaning                                                                     |
|----------------------------------------------------|---------------------------------|-----------------------------------------------------------------------------|
| `org.zhu4.rate-limiter.cache.type`                 | caffeine, redisson, fromScratch | The type of rate limiting cache to use                                      |
| `org.zhu4.rate-limiter.cache.expireAfterMillis`    | 1 - `Long.MAX_VALUE`            | The lifetime of objectsin the cache. Should be more than `intervalInMillis` |
| `org.zhu4.rate-limiter.configs.*.maximumRequests`  | 1 - `Long.MAX_VALUE`            | The maximum number of requests  for a given interval of time                |
| `org.zhu4.rate-limiter.configs.*.intervalInMillis` | 1 - `Long.MAX_VALUE`            | Time interval                                                               |
| `org.zhu4.rate-limiter.configs.*.algorithm`        | fixed-window, sliding-window    | Rate limiting algorithm                                                     |       
