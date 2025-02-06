
---

# Bean 생명주기 콜백 시작

`DB Connection Pool` 또는 `Network socket` 처럼 Application 시작시 필요한 연결을 미리 해두고 Application 종료 시점에 연결을 모두 해제하는 작업을 진행하기 위해서 객체의 `초기화와` `종료 작업`이 필요하다. Spring 을 통해 초기화 작업과 종료 작업을 어떻게 진행하는지 알아보자.

외부와 소통하는 NetworkClient 를 하나 만들어 보자.

```java
package hello.core.lifecycle;  
  
public class NetworkClient {  
  
    private String url;  
  
    public NetworkClient() {  
        System.out.println("생성자 호출 url = " + url);  
        connect();  // 초기화 작업
        call("초기화 연결 메시지");  
    }  
  
    public void setUrl(String url) {  
        this.url = url;  
    }  
  
    // 서비스 시작시 호출  
    public void connect() {  
        System.out.println("connect: " + url);  
    }  
  
    public void call(String message) {  
        System.out.println("call: " + url + " message = " + message);  
    }  
  
    // 서비스 종료시 호출  
    public void disconnect() {  
        System.out.println("close " + url);  
    }  
}
```

테스트 코드는 다음과 같다.

```java 
public class BeanLifeCycleTest {  
  
    @Test  
    void lifeCycleTest() {  
        AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(LifeCycleConfig.class);  
        NetworkClient client = ac.getBean(NetworkClient.class);  
        ac.close();  
    }  
  
    @Configuration  
    static class LifeCycleConfig {  
        @Bean  
        public NetworkClient networkClient() {  
            NetworkClient networkClient = new NetworkClient();  
            networkClient.setUrl("http://hello-spring.dev");  
            return networkClient;  
        }  
    }  
}
```

해당 테스트의 실행 결과는 다음과 같다.

```text
생성자 호출 url = null
connect: null
call: null message = 초기화 연결 메시지


기본적으로 Spring Bean 은 다음과 같은 LifeCycle 을 가진다.

```text
객체 생성 -> 의존관계 주입
```

> 🔥 예외
> 단 생성자 주입은 객체 생성과 동시에 의존관계 주입을 한다.


기본적으로 Spring Bean 은 기본적으로 객체가 생성된 후 의존관계 주입이 끝나야 필요한 데이터를 사용할 준비가 완료된다. 

따라서 생성과 초기화를 분리 해야하는데 Spring Bean은 어떻게 초기화 시점을 알 수 있을까? 이를 해결하기 위해 Spring 은 객체의 의존관계 주입이 완료될 때 **초기화 콜백**을 준다.

또한 빈의 안전한 종료를 위해 빈 소멸 직전 **소멸 콜백** 을 준다. 

### Spring Bean Event LifeCycle

1. Spring IOC Container 생성

2. Spring Bean 생성

3. 의존관계 주입

4. 초기화 콜백 -> 빈이 생성되고 의존관계 주입이 완료된 후 호출

5. 동작

6. 소멸전 콜백 -> 빈이 소멸되기 직전 호출

7. Spring 종료


# 스프링의 빈 생명주기 콜백 지원 방법

## 1. InitializingBean, DisposableBean

NetworkClient 가 `InitializingBean` 그리고 `DisposableBean` 을 구현하도록 해보자.

```java
public class NetworkClient implements InitializingBean, DisposableBean {  
  
    private String url;  
  
    public NetworkClient() {  
        System.out.println("생성자 호출 url = " + url);  
    }  
  
    public void setUrl(String url) {  
        this.url = url;  
    }  
  
    // 서비스 시작시 호출  
    public void connect() {  
        System.out.println("connect: " + url);  
    }  
  
    public void call(String message) {  
        System.out.println("call: " + url + " message = " + message);  
    }  
  
    // 서비스 종료시 호출  
    public void disconnect() {  
        System.out.println("close " + url);  
    }  
  
     //의존관계 주입이 끝나면 호출해 주겠다는 메서드명  
    @Override  
    public void afterPropertiesSet() throws Exception {  
        System.out.println("NetworkClient.afterPropertiesSet");  
        connect();  
        call("초기화 연결 메시지");  
    }  
  
    @Override  
    public void destroy() throws Exception {  
        System.out.println("NetworkClient.destroy");  
        disconnect();  
    }  

}
```
`InitializingBean` interface 는 `afterPropertiesSet()` 메서드를 통해 초기화 를 지원하며 `DisposableBean` interface 는 `destroy()` 메서드를 통해 소멸을 지원한다.

실행결과는 다음과 같다.

```java
생성자 호출 url = null
NetworkClient.afterPropertiesSet
connect: http://hello-spring.dev
call: http://hello-spring.dev message = 초기화 연결 메시지
NetworkClient.destroy
close http://hello-spring.dev
```

출력 결과를 보면 다음을 알 수 있다.

- 초기화 메서드는 의존관계 주입 이후 호출됨
- Spring container 종료 직전 소멸 메서드 호출됨

### 단점

`InitializingBean` 과 `DisposableBean` 는 초기화, 소멸 메서드의 이름을 변경할 수 없으며 외부 라이브러리에 적용할 수 없다는 단점이 존재한다.


> 💡 참고
> 이 두 인터페이스는 거의 사용하지 않는다.


## 2. 빈 등록시 초기화, 소멸 메서드 지정

```java
public class NetworkClient {  
  
    private String url;  
  
    public NetworkClient() {  
        System.out.println("생성자 호출 url = " + url);  
    }  
  
    public void setUrl(String url) {  
        this.url = url;  
    }  
  
    // 서비스 시작시 호출  
    public void connect() {  
        System.out.println("connect: " + url);  
    }  
  
    public void call(String message) {  
        System.out.println("call: " + url + " message = " + message);  
    }  
  
    // 서비스 종료시 호출  
    public void disconnect() {  
        System.out.println("close " + url);  
    }  
  
  
    public void init() {  
        System.out.println("NetworkClient.init");  
        connect();  
        call("초기화 연결 메세지");  
    }  
  
    public void close() {  
        System.out.println("NetworkClient.close");  
        disconnect();  
    }  
  
}
```

위 처럼 그냥 초기화 메서드와 소멸 메서드를 직접 만든뒤 

```java
public class BeanLifeCycleTest {  
  
    @Test  
    void lifeCycleTest() {  
        AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(LifeCycleConfig.class);  
        NetworkClient client = ac.getBean(NetworkClient.class);  
        ac.close();  
    }  
  
    @Configuration  
    static class LifeCycleConfig {  
        @Bean(initMethod = "init", destroyMethod = "close")  
        public NetworkClient networkClient() {  
            NetworkClient networkClient = new NetworkClient();  
            networkClient.setUrl("http://hello-spring.dev");  
            return networkClient;  
        }  
    }  
}
```

빈 등록시 `@Bean` 어노테이션에 `initMethod` 를 통해 초기화 메서드를, `destroyMethod` 를 통해 소멸 메서드를 지정할 수 있다.

실행결과는 다음과 같다.

```text
생성자 호출 url = null
NetworkClient.init
connect: http://hello-spring.dev
call: http://hello-spring.dev message = 초기화 연결 메세지
NetworkClient.close
close http://hello-spring.dev
```

> 💡 종료 메서드 추론
> 종료 메서드의 이름을 `close` 또는 `shutdown` 으로 지정할 경우 따로 등록하지 않아도 빈 소멸직전에 종료 메서드가 호출된다. 추론기능을 사용하기 싫을 경우 `destroyMethod=""` 를 사용하자.


## 3. @PostConstruct, @PreDestroy

```java
public class NetworkClient {  
  
    private String url;  
  
    public NetworkClient() {  
        System.out.println("생성자 호출 url = " + url);  
    }  
  
    public void setUrl(String url) {  
        this.url = url;  
    }  
  
    // 서비스 시작시 호출  
    public void connect() {  
        System.out.println("connect: " + url);  
    }  
  
    public void call(String message) {  
        System.out.println("call: " + url + " message = " + message);  
    }  
  
    // 서비스 종료시 호출  
    public void disconnect() {  
        System.out.println("close " + url);  
    }  
  
  
    @PostConstruct  
    public void init() {  
        System.out.println("NetworkClient.init");  
        connect();  
        call("초기화 연결 메세지");  
    }  
  
    @PreDestroy  
    public void close() {  
        System.out.println("NetworkClient.close");  
        disconnect();  
    }  
  
}
```

이런식으로 초기화 메서드에 `@PostConstruct` 종료 메서드에 `@PreDestroy` 어노테이션을 달아주면 가장 편리하게 초기화와 종료를 실행할 수 있다.


### 특징

- 가장 권장하는 방법
- 어노테이션 하나만 붙이면 됨
- 자바 표준 기술이다. 따라서 스프링이 아닌 다른 컨테이너에서도 동작한다.
- 외부라이브러리에는 적용하지 못한다. 
	- 이때는 @Bean 의 기능을 이용하자.

