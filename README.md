# prometheus_auto_timing

[Prometheus](https://prometheus.io/) is nice open-source monitoring tools which covers applications and infrastructures.
Prometheus has many client libraries that matches the language in which your application is written.(See [here](https://prometheus.io/docs/instrumenting/clientlibs/)).

Prometheus client libraries include [Java client library](https://github.com/prometheus/client_java) which supports spring framework, spring boot.
[The instrument by Spring AOP](instrument) is nice solution,
however, it is a lot of cost to implement for collecing duration metrics of all methods in all `RestController` or `Controller` classes. 

This repository is a spring boot library for this solution to make it easy and simple.
You only add dependency and one annotation,
then, you can collect the duration metrics of all methods in all `RestController` or `Controller` classes.

# How to use

Now, this library is not uploaded in maven repository.(You make [issue](https://github.com/aha-oretama/prometheus_auto_timing/issues/new) if you want. I'll upload soon.)

As a temporary measure, you must install library as follows.
```bash
$ git clone https://github.com/aha-oretama/prometheus_auto_timing.git
$ cd prometheus_auto_timing.git
$ mvn install
``` 

You only add dependency.
```xml:pom.xml
		<dependency>
			<groupId>jp.aha-oretama</groupId>
			<artifactId>prometheus_auto_timing</artifactId>
			<version>0.1.0-RELEASE</version>
		</dependency>
```

And you only add one annotaion `@EnablePrometheusAutoTiming`.

```java:Application.java
@SpringBootApplication
@EnablePrometheusEndpoint
@EnableSpringBootMetricsCollector
@EnablePrometheusAutoTiming
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
```

*Notification: You do not need to implement for collecting duration metrics of all methods in all `RestController` or `Controller` classes, but you need to implement for exposing prometheus API by spring boot. It is realised by [simpleclient_spring_boot](https://github.com/prometheus/client_java/tree/master/simpleclient_spring_boot).*
