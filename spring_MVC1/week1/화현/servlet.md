
## 서블릿 컨테이너

### 서블릿 컨테이너란

#### 서블릿(Servlet) 이란?

서블릿은 자바 기반의 웹 애플리케이션에서 클라이언트 요청을 처리하고 동적인 웹페이지를 생성하는 프로그램입니다. HTTP 요청을 받아 특정 로직을 실행한 후 HTTP 응답을 반환하는 역할을 합니다.

#### 서블릿 컨테이너의 역할

서블릿 컨테이너는 서블릿을 실행하고 관리하는 환경입니다. 즉, 서블릿이 정상 동작할 수 있도록 지원하는 역할을 합니다.

- HTTP 요청을 받아 서블릿에 전달
- 서블릿이 응답을 생성하면 이를 HTTP 응답으로 반환하여 클라이언트에 전달
- 여러 서블릿 간의 요청과 응답을 적절히 분배 및 관리

### 서블릿 컨테이너 동작 원리

스프링 부트를 실행하면 ==내장 톰캣 서버==가 함께 실행됩니다. 내장 톰캣이 실행되면서 ==서블릿 컨테이너도 생성==됩니다. 

#### 서블릿 컨테이너 생성과 HTTP 요청 처리 과정

1. 내장 톰캣과 서블릿 컨테이너의 생성
	스프링 부트가 실행되면, 내장 톰캣이 ==서블릿 컨테이너==를 생성합니다.  이 컨테이너는 애플리케이션에서 정의한 서블릿을 관리하며, 요청이 들어오면 해당 서블릿을 실행하는 역할을 합니다. 예를 들어 `HelloServlet` 이라는 서블릿이 있다고 가정해 보겠습니다.
	
2. 웹 브라우저의 요청
	사용자가 다음과 같은 요청을 보냈다고 가정해봅시다. 이 요청은 내장 톰캣 서버로 전달됩니다.
```
	 GET /hello?username=songhwahyeon HTTP/1.1
	 Host: localhost:8080
```

3. 서블릿 호출 과정
	내장톰캣 서버는 요청을 받으면, `Request`와 `Response`객체를 생성한 뒤, `HelloServlet`을 호출합니다. 이때, HelloServlet의 `service()`메서드가 실행되며, 요청과 응답 객체가 전달됩니다.
	
4. 응답 생성 및 반환
	HelloServlet에서 요청을 처리한 후, 다음과 같은 HTTP 응답을 생성하여 클라이언트(웹 브라우저)로 반환합니다. 
```
	HTTP/1.1 200 OK
	Content-Type: text/plain;charset=utf-8
	Content-Length: 18

	hello songhwahyeon
```

5. 웹 브라우저에 결과 표시
	브라우저는 서버로부터 받은 응답을 해석하여 화면에 `hello songhwahyeon` 을 출력합니다. 
	
> [!NOTE] 내장 톰캣 서버 (Embedded Tomcat Server)
> 내장 톰캣은 스트링 부트 애플리케이션 내부에서 실행되는 웹 서버 입니다. 일반적으로 웹 애플리케이션을 실행하려면 웹 요청을 받아 자바 웹 애플리케이션이 처리할 수 있도록 돕는 서버인 톰캣을 별도로 설치하고 설정해야 했지만, 스프링 부트는 톰캣을 애플리케이션 내부에 내장하여 따로 설치할 필요 없이 바로 실행할 수 있도록 해줍니다. 

## HttpServletRequest 

### HttpServletRequest의 역할

web app을 개발할 때, 클라이언트에서 서버로 요청을 보낼 때마다 HTTP 요청 메시지가 전달됩니다. 하지만 이 메시지를 하나하나 파싱해서 사용하기 매우 번거롭고 복잡합니다.

그래서 서블릿에서는 `HttpServletRequest`객체를 제공하여 HTTP 요청을 쉽게 다룰 수 있도록 도와줍니다.

### HttpServletRequest가 하는 일

클라이언트가 서버로 요청을 보낼 때, 다음과 같은 HTTP 요청 메시지가 전달됩니다.
```
POST /save HTTP/1.1
Host: localhost:8080
Content-Type: application/x-www-form-urlencoded

username=kim&age=20
```
이 요청을 HttpServletRequest 객체를 통해 쉽게 조회할 수 있습니다.

#### 요청 정보 조회

- HTTP 메서드 : GET, POST 등 요청 방식을 조회
- URL, URI : 요청한 URL, URI 조회
- 쿼리 파라미터 : ?username=kim&age=20 조회
- 헤더 : Content-Type 조회
- 바디 : 요펑 본문 데이터 조회

#### 추가 기능

##### 임시 저장소 기능 (setAttribute) 

- 서블릿에서 요청 처리 중 데이터를 임시로 저장할 수 있습니다.
- 이 데이터는 요청이 끝날 때까지만 유지됩니다.

##### 세션 관리 기능 (getSession)

- 웹 브라우저가 서버와 상태를 유지하도록 세션을 제공할 수 있습니다. 
- 세션을 사용하면 로그인 정보같은 데이터를 유지할 수 있습니다.

### HTTP 요청 데이터

#### GET - 쿼리 파라미터

- 데이터를 URL에 포함해서 전송하는 방식입니다.
- ? 뒤에 키=값 형태로 데이터를 추가하여 요청을 보낼 수 있습니다.
- 검색, 필터링, 페이징과 같은 기능에 자주 사용되며, 데이터가 URL에 노출되기에 민감한 정보 전송에는 적합하지 않습니다.
$$http://localhost:8080/request-param?username=hello&age=20$$
- `request.getParameter`를 통해 단일 파라미터의 값을 알 수 있으며, 이름이 같은 파라미터가 있을 경우에는 `request.getParmeterValues`를 통해 확인할 수 있습니다.

#### POST - HTML Form

- HTML에서 Form 태그를 사용해 데이터를 전송할 때 주로 사용됩니다.
- 데이터를 메시지 바디에 포함하여 보냅니다.
- 생성한 Form에서 데이터를 입력하고 전송 버튼을 누르면 브라우저가 자동으로 HTTP 요청을 생성하여 서버로 보냅니다.
$$
http://localhost:8080/basic/hello-form.html
$$
- Content-Type: application/x-www-form-urlencoded 형식은 GET방식의 쿼리 파라미터 형식과 동일합니다.
	- GET 요청 : `http://localhost:8080/request-param?username=hello&age=20`
	- POST 요청 바디 : `username=hello&age=20`
- 즉, GET 요청, POST 요청 둘 다 상관 없이 같은 방식으로 데이터를 읽을 수 있습니다.
- request.getParameter()를 사용하면 GET,POST방식 모두 동일한 방식으로 데이터를 조회할 수 있습니다. 
	- GET 방식의 경우, 데이터가 URL 쿼리 파라미터에 위치합니다.
	- POST 방식의 경우, 데이터가 HTTP 바디에 위치합니다.

#### API 메시지 바디 

- POST, PUT, PATCH 같은 메서드를 사용하면 HTTP 메시지 바디에 데이터를 포함해 보낼 수 있습니다.
- 이 방식은 보통 API에서 사용되며, JSON, XML, TEXT 등 다양한 형식의 데이터를 전송할 수 있습니다.
##### 단순 텍스트

- 메시지 바디에 텍스트 데이터를 직접 담아 전송할 수 있습니다.
```
ServletInputStream inputStream = request.getInputStream(); String messageBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
```
- 다음과 같은 코드를 통해 메시지 바디를 읽을 수 있습니다.

##### JSON 데이터

- 실제 API에서는 JSON 형식의 데이터를 주로 사용합니다.
```
{
  "username": "hello",
  "age": 20
}
```
- 다음과 같은 JSON 형식이 존재한다고 가정해 봅시다.
```
@Getter @Setter
public class HelloData {
    private String username;
    private int age;
}
```
- JSON 데이터를 자바 객체로 변환하기 위해 새로운 HelloData 클래스를 만들어 줍니다.
```
// JSON 데이터를 문자열로 변환 
ServletInputStream inputStream = request.getInputStream(); 
String messageBody =StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);

// JSON -> Java 객체 변환 
HelloData helloData = objectMapper.readValue(messageBody, HelloData.class);
```
- 이후, RequestBodyJsonServlet을 통해, JSON 데이터를 메시지 바디에서 읽고, 앞서 생성한 HelloData객체로 변환하는 코드를 작성해줍니다.
- ObjectMapper를 사용해 JSON을 자바 객체로 변환시켜줍니다.
- request.getInputStream()을 사용해 JSON 데이터를 파싱합니다.
- readValue()를 사용해 JSON 데이터를 HelloData 객체로 변환시킵니다.

### HttpServletResponse 

웹 애플리케이션에서 서버는 클라이언트의 요청을 처리한 후 HTTP 응답을 생성하여 보냅니다. 이때 HttpServletResponse객체를 사용하면 응답 코드 설정, 헤더 추가, 바디 생성, 편의 기능(쿠키, 리다이렉트 등)을 쉽게 처리할 수 있습니다.

#### HttpServletResponse의 역할

서버는 클라이언트의 요청을 처리한 후 HTTP 응답을 만들어 반환해야 합니다. `HttpServletResponse`는 다음과 같은 역할을 합니다.

HTTP 응답 메시지 생성
- 응답 코드 지정 (`setStatus()`, `SC_OK` 등) : response.setStatus(HttpServletResponse.SC_OK);
- 응답 헤더 생성 (`setHeader()`) :
	response.setHeader("Content-Type", "text/plain;charset=utf-8");
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setHeader("my-header", "hello");
- 응답 바디 작성 (`getWriter()` 사용)
	PrintWriter writer = response.getWriter();
    writer.println("ok");

편의 기능 제공
- `Content-Type`, `Character Encoding` 설정
- 쿠키 설정 (`addCookie()`)  : 쿠기 전송할 수 있음
- 리다이렉트 (`sendRedirect()`) : 특정 URL로 이동시키고 싶을 때 사용

#### HTML 페이지 응답 보내기

- HTML 페이지를 응답으로 보낼 때는 Content-Type을 text/html로 설정해야 합니다.
	response.setContentType("text/html");
    response.setCharacterEncoding("utf-8");

#### JSON 응답 보내기 (API 응답)

- API 서버는 보통 JSON 데이터를 응답으로 보냅니다.
- API 개발 할 때, JSON응답을 기본으로 하기에, Contetn-Type은 application/json으로 설정해야 합니다.
	response.setHeader("content-type", "application/json");  
	response.setCharacterEncoding("utf-8");  
	HelloData data = new HelloData();  
	data.setUsername("kim");  
	data.setAge(20);  
  
	String result = objectMapper.writeValueAsString(data);  
	response.getWriter().write(result);
- ObjectMapper.writeValueAsString()을 사용하여 객체를 JSON으로 변환할 수 있습니다.
