
# Bean Scope 란?

먼저 `Scope` 란 빈이 존재할 수 있는 범위를 의미한다.
Spring 에서는 다음과 같은 다양한 Scope 를 지원한다.


- `Singleton (default)` : Spring Container 시작부터 종료까지 유지되는 가장 넓은 범위의 Scope

- `Prototype` : 빈 생성, 의존관계 주입 그리고 초기화 메서드 호출까지만 관여하고 더는 관리하지 않는 Scope

- `Request` : 웹 관련 scope 이다. 웹 요청이 들어오고 나갈때 까지 유지되는 Scope

- `session` : 웹 관련 scope 이다. 웹 세션이 생성되고 종료될때 까지 유지되는 Scope

- `application` : 웹 관련 scope 이다. 웹 서블릿 컨텍스트와 같은 범위로 유지되는 Scope


> 💡 참고
> web 관련 scope들은 spring web 과 관련된 기능이 들어가야 사용할 수 있다.


# Prototype Scope

Singleton Scope 빈을 조회할 경우 항상 같은 인스턴스를 반환한다. 하지만 prototype scope 인 bean 을 조회할 경우 spring container 는 항상 다른 인스턴스를 생성해서 반환한다.

먼저 singleton scope 를 가진 bean 을 테스트 해보자.
```java
public class SingletonTest {  
  
    @Test  
    void singletonBeanFind() {  
        AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(SingletonBean.class);  
        System.out.println("container 생성 완료");  
          
        SingletonBean singletonBean1 = ac.getBean(SingletonBean.class);  
        SingletonBean singletonBean2 = ac.getBean(SingletonBean.class);  
  
        System.out.println("singletonBean1 = " + singletonBean1);  
        System.out.println("singletonBean2 = " + singletonBean2);  
  
        assertThat(singletonBean1).isSameAs(singletonBean2); // == 비교  
  
        ac.close();  
    }  
  
    @Scope("singleton")  
    static class SingletonBean {  
  
        @PostConstruct  
        public void init() {  
            System.out.println("SingletonBean.init");  
        }  
  
        @PreDestroy  
        public void destroy() {  
            System.out.println("SingletonBean.destroy");  
        }  
    }  
  
}
```

실행 결과는 다음과 같다.

```text
SingletonBean.init
container 생성 완료
singletonBean1 = hello.core.scope.SingletonTest$SingletonBean@5e21e98f
singletonBean2 = hello.core.scope.SingletonTest$SingletonBean@5e21e98f
SingletonBean.destroy

```
즉 스프링 컨테이너 내부에 Bean 으로 등록될 때 초기화 메서드까지 실행되며 이후 Bean 조회 요청이 들어올 경우 항상 같은 인스턴스를 반환한다.

또한 spring container 가 종료되기 직전 종료 메서드까지 호출해 준다.

다음 예시는 prototype scope 를 가진 bean을 테스트 한다.


```java
public class PrototypeTest {  
  
    @Test  
    void prototypeTest (){  
        AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(PrototypeBean.class);  
        System.out.println("container 생성 완료");  
  
        System.out.println("find prototypeBean1");  
        PrototypeBean prototypeBean1 = ac.getBean(PrototypeBean.class);  
        System.out.println("prototypeBean1 = " + prototypeBean1);  
  
        System.out.println("find prototypeBean2");  
        PrototypeBean prototypeBean2 = ac.getBean(PrototypeBean.class);  
        System.out.println("prototypeBean2 = " + prototypeBean2);  
  
        assertThat(prototypeBean1).isNotSameAs(prototypeBean2);  
        ac.close();  
    }  
  
    @Scope("prototype")  
    static class PrototypeBean {  
  
        @PostConstruct  
        public void init() {  
            System.out.println("prototypeBean.init");  
        }  
  
        @PreDestroy  
        public void destroy() {  
            System.out.println("prototypeBean.destroy");  
        }  
    }  
}
```

곌과는 다음과 같다.

```text
container 생성 완료
find prototypeBean1
prototypeBean.init
prototypeBean1 = hello.core.scope.PrototypeTest$PrototypeBean@791d1f8b
find prototypeBean2
prototypeBean.init
prototypeBean2 = hello.core.scope.PrototypeTest$PrototypeBean@2415fc55
```

위 결과를 보면 다음을 알 수 있다.

- bean 조회 요청이 올 때 bean 이 생성되며 초기화 메서드가 실행된다.
- 조회할 때 마다 다른 인스턴스를 반환한다.
- 초기화 메서드까지만 호출하고 관리하지 않기 때문에 종료 메서드는 실행되지 않는다.
- 즉 빈을 조회한 클라이언트가 프로토타입 빈을 관리해야한다. 종료 메서드 호출이 필요할 경우 클라이언트가 직접 호출해야 한다.


# 싱글톤 빈 과 프토로타입 빈 함께 사용시 문제점

Prototype Scope 빈은 보통 사용할 때마다 새로운 빈을 생성하기 위해서 사용할 것이다. 하지만 Singleton Bean 내부에서 Prototype Bean 을 사용한다면 예상치 못하게 동작하게 된다.

```java
public class SingletonWithPrototypeTest1 {  
  
    @Test  
    void prototypeFind() {  
        AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(PrototypeBean.class);  
  
        PrototypeBean prototypeBean1 = ac.getBean(PrototypeBean.class);  
        prototypeBean1.addCount();  
  
        PrototypeBean prototypeBean2 = ac.getBean(PrototypeBean.class);  
        prototypeBean2.addCount();  
  
        assertThat(prototypeBean1.getCount()).isEqualTo(1);  
        assertThat(prototypeBean2.getCount()).isEqualTo(1);  
    }  
  
    @Test  
    void singletonClientUsePrototype() {  
        AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(PrototypeBean.class, ClientBean.class);  
  
        ClientBean client1 = ac.getBean(ClientBean.class);  
        ClientBean client2 = ac.getBean(ClientBean.class);  
  
        int count1 = client1.logic();  
        int count2 = client2.logic();  
  
        assertThat(count1).isEqualTo(1);  
        assertThat(count2).isEqualTo(2);  
  
    }  
  
    @Scope("singleton")  
    static class ClientBean {  
        private final PrototypeBean prototypeBean;  
  
        public ClientBean(PrototypeBean prototypeBean) {  
            this.prototypeBean = prototypeBean;  
        }  
  
        public int logic() {  
            prototypeBean.addCount();  
            return prototypeBean.getCount();  
        }  
    }  
  
    @Scope("prototype")  
    static class PrototypeBean {  
        private int count;  
  
        public void addCount() {  
            count++;  
        }  
  
        public int getCount() {  
            return count;  
        }  
  
        @PostConstruct  
        public void init() {  
            System.out.println("PrototypeBean.init " + this);  
        }  
  
        @PreDestroy  
        public void destroy() {  
            System.out.println("PrototypeBean.destroy");  
        }  
    }
```


```java
    @Test  
    void prototypeFind() {  
        AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(PrototypeBean.class);  
  
        PrototypeBean prototypeBean1 = ac.getBean(PrototypeBean.class);  
        prototypeBean1.addCount();  
  
        PrototypeBean prototypeBean2 = ac.getBean(PrototypeBean.class);  
        prototypeBean2.addCount();  
  
        assertThat(prototypeBean1.getCount()).isEqualTo(1);  
        assertThat(prototypeBean2.getCount()).isEqualTo(1);  
    }  
```

prototypeBean 은 내부에 count 필드를 가지며 `addCount()` 메서드를 통해 1씩 증가할 수 있다.

```java
    @Test  
    void singletonClientUsePrototype() {  
        AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(PrototypeBean.class, ClientBean.class);  
  
        ClientBean client1 = ac.getBean(ClientBean.class);  
        ClientBean client2 = ac.getBean(ClientBean.class);  
  
        int count1 = client1.logic();  
        int count2 = client2.logic();  
  
        assertThat(count1).isEqualTo(1);  
        assertThat(count2).isEqualTo(2);  
  
    }  
```

그런데 이 프로토타입 빈을 싱글톤 빈에 주입 받아 사용할 경우 의도한 바와 다르게 동작하게 된다. 원래 의도는 client 의 `logic()` 을 호출할때 마다 새로운 프로토타입 빈이 생성되어 주입된 후 동작해 각각 결과가 1 이되는 것이었다.

하지만 두번 요청을 하니 최종 결과가 2가 되는 것을 확인할 수 있다.

이는 Singleton Bean 이 최초 생성시 Spring Container 에게 프로토타입 빈을 요청하는데 이때 단 한번 주입되고 싱글톤 빈은 계속해서 같은 프로토타입 빈을 참조하기 때문이다.

그러면 사용할때 마다 새로 프로토타입빈을 주입받을 수는 없을까?


# 문제 해결


이러한 문제를 해결하기 위해서는 3가지 방법을 사용할 수 있다. 먼저 다음과 같이 테스트 코드를 수정하자.

```java
@Test  
void singletonClientUsePrototype() {  
    AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(PrototypeBean.class, ClientBean.class);  
  
    ClientBean client1 = ac.getBean(ClientBean.class);  
    ClientBean client2 = ac.getBean(ClientBean.class);  
  
    int count1 = client1.logic();  
    int count2 = client2.logic();  
  
    assertThat(count1).isEqualTo(1);  
    assertThat(count2).isEqualTo(1);  
  
}
```

## 1.Spring Container 직접 조회

```java
@Scope("singleton")  
static class ClientBean {  
  
    @Autowired  
    ApplicationContext ac;  
  
    public int logic() {  
        PrototypeBean prototypeBean = ac.getBean(PrototypeBean.class);  
        prototypeBean.addCount();  
        return prototypeBean.getCount();  
    }  
}
```

직접 Spring Container 를 조회하는 방법이다. Prototype scope bean 이라서 조회할 때마다 새로운 Bean 이 생성되어 반환된다. 

이렇게 Spring Container 를 직접 주입받게 될 경우 Spring Container 에 종속적인 코드가 되며 단위 테스트 또한 어려워진다.

> 💡 참고
> 의존성을 외부에서 주입받는게 아닌 직접 찾는 것을 DL (Dependency Lookup) 즉 의존관계 조회 라고 한다.


단위 테스트를 쉽게 하기 위해서는 DL 역할을 하는 객체를 외부에서 주입받으면 된다.

## 2. ObjectFactory, ObjectProvider

위 같이 DL 기능을 해주는 것이 바로 `ObjectProvider` 이다. 참고로 ObjectProvider는 ObjectFactory 를 상속 받는다.

```java
@Scope("singleton")  
static class ClientBean {  
  
    @Autowired  
    ObjectProvider<PrototypeBean> provider;  
  
    public int logic() {  
        PrototypeBean prototypeBean = provider.getObject();  
        prototypeBean.addCount();  
        return prototypeBean.getCount();  
    }  
}
```

위와 같이 `ObjectProvider` 를 외부에서 주입받으면 된다. 원하는 빈을 찾기 위해서는 `getObject()` 메서드를 사용하면 된다.

### 특징

#### ObjectFactory

1. 별도의 라이브러리 필요 없음
2. 스프링 의존적

#### ObjectProvider

1. ObjectFactory 상속 
2. 옵션, 스트림 처리등 편의 기능이 많고 별도 라이브러리 필요 없음
3. 스프링에 의존


## 3. JSR-330 Provider


해당 방법을 사용하기 위해서는 별도의 라이브러리가 필요하다.

- Spring Boot 3.0 미만

	``` text
	javax.inject:javax.inject:1
	```


- Spring Boot 3.0 이상

	``` text
	jakarta.inject:jakarta.inject-api:2.0.1`
	```

스프링 부트 버전에 맞추어 `build.gradle` 에 추가해야 한다.


사용법은 다음과 같다.

```java
@Scope("singleton")  
static class ClientBean {  
  
    @Autowired  
    Provider<PrototypeBean> provider;  
  
    public int logic() {  
        PrototypeBean prototypeBean = provider.get();  
        prototypeBean.addCount();  
        return prototypeBean.getCount();  
    }  
}
```

`package jakarta.inject;` 의 `Provider` 를 사용해야 하며 단순히 `get()` 을 통해 원하는 빈을 조회할 수 있다.

### 특징

- `get()` 하나로 기능이 매우 단순하다.
- 별도 라이브러리가 필요하다
- 자바 표준이므로 다른 컨테이너에서도 사용할 수 있다.

# Web Scope

## 특징

Web scope 는 웹 환경에서만 동작하며 프로토타입과 다르게 해당 스코프 종료 시점까지 관리한다. 따라서 종료 메서드가 호출된다.

## 종류

- `request` : `http request` 하나가 들어오고 나갈때 까지 유지되는 scope. 각 요청별로 별도의 bean instance 가 생성되고 관리된다.

- `session` : Http Session 과 동일한 생명 주기를 가지는 Scope

- `application` : Servlet Context 와 동일한 생명 주기를 가지는 Scope

- `websocket` : `websocket` 과 동일한 생명 주기를 가지는 scope