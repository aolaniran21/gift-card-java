# Spring Boot + Java Interview Guide for Mid-Level Backend Developer

This guide is designed for a mid-level backend developer interview in Spring Boot and Java. It focuses on the most common questions, what interviewers are really testing, and includes code examples you can study and explain.

---

## 1) What is Spring Boot, and how is it different from the Spring Framework?

### Answer
Spring Framework is the foundational framework for dependency injection, AOP, transactions, and web support. Spring Boot builds on top of it and reduces boilerplate configuration by using sensible defaults, auto-configuration, and embedded servers.

Key benefits of Spring Boot:
- Minimal configuration
- Embedded Tomcat/Jetty server
- Auto-configuration for common libraries
- Production-ready features like health checks and metrics
- Easy dependency management via starters

### Coding example
```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

Without Spring Boot, you would normally need to configure:
- DispatcherServlet
- application context
- component scanning
- bean configuration
- web server setup
- dependency wiring

### What interviewers look for
They want to hear that Spring Boot is opinionated, reduces setup time, and is ideal for fast backend development while still using the Spring ecosystem.

---

## 2) What is Dependency Injection (DI), and why is constructor injection preferred?

### Answer
Dependency Injection is a design pattern where the framework provides dependencies to a class instead of the class creating them itself. This reduces tight coupling, improves testability, and makes class responsibilities clearer.

Constructor injection is preferred because:
- dependencies are explicit
- required dependencies cannot be null
- easy to test
- works well with immutability

### Coding example
```java
@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final PaymentService paymentService;

    public OrderService(OrderRepository orderRepository, PaymentService paymentService) {
        this.orderRepository = orderRepository;
        this.paymentService = paymentService;
    }

    public Order createOrder(Order order) {
        paymentService.processPayment(order.getTotal());
        return orderRepository.save(order);
    }
}
```

### Why not field injection?
```java
@Service
public class BadOrderService {
    @Autowired
    private OrderRepository orderRepository;
}
```

This is less explicit and harder to test. Constructor injection is preferred by the Spring community.

---

## 3) What is the difference between @Component, @Service, @Repository, and @Controller/@RestController?

### Answer
These are stereotypes used to mark Spring-managed beans.

- `@Component`: generic component, used for any class managed by Spring
- `@Service`: service layer logic
- `@Repository`: data access layer, often used with persistence exceptions translation
- `@Controller`: MVC controller for web pages
- `@RestController`: controller for REST endpoints, includes `@ResponseBody`

### Coding example
```java
@Component
public class EmailSender {
    public void send(String email) {
        System.out.println("Sending email to: " + email);
    }
}

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.getUser(id);
    }
}
```

### Interview note
The interviewer wants you to understand that splitting layers makes code easier to maintain and test.

---

## 4) Explain Spring Boot auto-configuration.

### Answer
Spring Boot auto-configuration automatically configures beans based on what is present in the classpath and what properties are set. This reduces manual configuration.

Example:
- If `spring-boot-starter-web` is present, Spring Boot configures MVC and embedded Tomcat.
- If `spring-boot-starter-data-jpa` is present, it configures JPA, transaction manager, and EntityManagerFactory.
- If `spring-boot-starter-security` is present, it creates basic security configuration.

### Coding example
```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

With `application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/appdb
    username: postgres
    password: secret
```

Spring Boot will attempt to configure the necessary DataSource and JPA infrastructure automatically.

### Good answer pattern
"Spring Boot follows the 'opinionated defaults' principle. It tries to do the right thing based on dependencies and properties, but you can override the defaults when needed."

---

## 5) What is the difference between @Controller and @RestController?

### Answer
`@Controller` is used for MVC apps where you may return views or server-side rendered pages.
`@RestController` is a specialized controller that combines `@Controller` and `@ResponseBody`.

### Example
```java
@Controller
public class ViewController {
    @GetMapping("/home")
    public String homePage() {
        return "home";
    }
}

@RestController
@RequestMapping("/api")
public class ApiController {
    @GetMapping("/hello")
    public String hello() {
        return "Hello from API";
    }
}
```

### Why it matters
REST controllers are used to return JSON/XML payloads directly, which is the common backend standard for microservices and modular monoliths.

---

## 6) Explain the Spring MVC request flow.

### Answer
A typical request flow looks like this:
1. Client sends HTTP request
2. DispatcherServlet receives the request
3. HandlerMapping decides which controller method handles it
4. Controller method executes business logic
5. Service layer performs business processing
6. Repository interacts with DB if needed
7. Response is converted to JSON/XML (via HttpMessageConverter)
8. Client receives the response

### Coding example
```java
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@RequestBody CreateOrderRequest request) {
        OrderResponse response = orderService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
```

### Explanation
- `@PostMapping` maps the HTTP POST request
- `@RequestBody` binds JSON request body to a Java object
- `ResponseEntity` allows control over HTTP status and body

---

## 7) How do you validate request payloads in Spring Boot?

### Answer
Use Bean Validation annotations and `@Valid` on controller parameters.

Examples of validation annotations:
- `@NotNull`
- `@NotBlank`
- `@Email`
- `@Size`
- `@Min`, `@Max`

### Coding example
```java
public record RegisterRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password
) {}
```

Controller:
```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok().build();
    }
}
```

Global exception handling:
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }
}
```

### Interview tip
A strong answer includes: validation in DTOs, central exception handling, and explicit error responses to clients.

---

## 8) Explain Spring Security basics.

### Answer
Spring Security is the authentication and authorization framework used in Spring applications. It handles:
- authentication
- authorization
- session management
- CSRF protection
- filtering requests
- password hashing

### Common config example
```java
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                .requestMatchers("/api/orders/**").hasAnyRole("USER", "ADMIN")
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### JWT concept
For stateless APIs, JWT is commonly used:
- user logs in
- server creates JWT containing claims
- client sends JWT in Authorization header
- server validates the token on each request

### Good answer structure
"I use Spring Security to secure endpoints, enforce roles and authorities, and validate JWTs in a stateless architecture. This is common for REST APIs where session-based auth is not ideal."

---

## 9) What is JWT, and how does it work?

### Answer
JWT (JSON Web Token) is a compact, self-contained token used for authentication and authorization. It usually contains:
- header
- payload
- signature

Common claims:
- sub (subject)
- role
- exp (expiration)
- iat (issued at)

### Example JWT flow
```java
String token = Jwts.builder()
    .subject("user@example.com")
    .claim("role", "ROLE_ADMIN")
    .expiration(new Date(System.currentTimeMillis() + 3600000))
    .signWith(Keys.hmacShaKeyFor(secretKey))
    .compact();
```

Validation:
```java
Jws<Claims> claims = Jwts.parser()
    .verifyWith(Keys.hmacShaKeyFor(secretKey))
    .build()
    .parseSignedClaims(token);
```

### Why use it
- stateless auth
- easy to use in mobile/web clients
- reduces server-side session storage

### Trade-offs
- token revocation is harder than session-based auth
- secret management is critical
- expiration and refresh tokens are important

---

## 10) Explain the difference between authentication and authorization.

### Answer
Authentication = verifying who the user is.
Authorization = verifying what the user is allowed to do.

### Simple example
```java
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/admin/statistics")
public Map<String, Object> adminStats() {
    return Map.of("status", "ok");
}
```

### Example flow
- User logs in with email and password
- App checks they are the correct person -> authentication
- App checks whether they have admin privileges -> authorization

### Good interview answer
"Authentication answers the question 'who are you?', while authorization answers 'what are you allowed to do?'"

---

## 11) What is JPA, and how does it differ from JDBC?

### Answer
JDBC is a low-level API for interacting directly with SQL. JPA is a higher-level ORM framework that maps Java objects to relational tables.

### JDBC example
```java
Connection connection = DriverManager.getConnection(url, user, password);
PreparedStatement stmt = connection.prepareStatement("SELECT * FROM users WHERE id = ?");
stmt.setLong(1, 1L);
ResultSet rs = stmt.executeQuery();
```

### JPA example
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String password;
}
```

Repository:
```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
```

### Why JPA is helpful
- less boilerplate
- ORM mapping
- easier domain modeling
- easier transaction handling with Spring Data JPA

### Trade-offs
- more abstraction, may hide performance issues
- you still need to understand SQL and DB behavior
- improper mappings can create performance problems

---

## 12) Explain JPA relationships: @OneToOne, @OneToMany, @ManyToOne, @ManyToMany.

### Answer
These map object relationships to database tables.

### Example: OneToMany / ManyToOne
```java
@Entity
public class Customer {
    @Id
    @GeneratedValue
    private Long id;

    @OneToMany(mappedBy = "customer")
    private List<Order> orders;
}

@Entity
public class Order {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;
}
```

### ManyToMany example
```java
@Entity
public class Student {
    @ManyToMany
    @JoinTable(
        name = "student_course",
        joinColumns = @JoinColumn(name = "student_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private List<Course> courses;
}
```

### Key interview points
- Use `mappedBy` to avoid duplicate foreign keys
- Prefer `ManyToOne` over `OneToMany` as the owning side in most cases
- Be careful with lazy loading and N+1 issues

---

## 13) What is a transaction, and why is @Transactional important?

### Answer
A transaction is a unit of work that either succeeds completely or fails completely. In relational databases, transactions ensure data consistency.

### Example
```java
@Service
public class PaymentService {

    private final AccountRepository accountRepository;

    public PaymentService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public void transferMoney(Long fromId, Long toId, BigDecimal amount) {
        Account from = accountRepository.findById(fromId).orElseThrow();
        Account to = accountRepository.findById(toId).orElseThrow();

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

        accountRepository.save(from);
        accountRepository.save(to);
    }
}
```

### Why it matters
Without transactions, money could be deducted from one account but not added to another if the process fails halfway.

### Transaction attributes
- `REQUIRED` (default)
- `REQUIRES_NEW`
- `READ_ONLY`
- `NEVER`

### Interview note
A strong answer includes the concept of atomicity and failure rollback.

---

## 14) What is the N+1 problem, and how do you avoid it?

### Answer
N+1 happens when a query fetches a list of parent records and then performs one additional query per parent to fetch related data.

Example:
```java
List<Order> orders = orderRepository.findAll();
for (Order order : orders) {
    System.out.println(order.getCustomer().getName());
}
```

This may trigger one query for orders and one query per customer.

### Fixes
- use `JOIN FETCH` in JPA queries
- use `@EntityGraph`
- use pagination carefully
- avoid lazy loading in loops

### Coding example
```java
@Query("SELECT o FROM Order o JOIN FETCH o.customer c")
List<Order> findAllWithCustomers();
```

### What interviewers want to hear
You should understand that ORM can hide query complexity, and that performance issues can appear even when code looks simple.

---

## 15) How do you handle exceptions in a Spring Boot application?

### Answer
Use a global exception handler with `@RestControllerAdvice` to centralize error handling.

### Example
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(ResourceNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}
```

### Why this is important
- consistent API responses
- cleaner controllers
- easier debugging
- avoids leaking technical details to clients

### Common business exceptions
- `ResourceNotFoundException`
- `InvalidCredentialsException`
- `DuplicateUserException`
- `AccessDeniedException`

---

## 16) What are DTOs, and why are they useful?

### Answer
DTOs (Data Transfer Objects) are used to transfer data between layers and to shape API payloads without exposing internal entities directly.

### Example
```java
public record OrderResponse(Long id, String customerName, BigDecimal total) {}
```

Entity:
```java
@Entity
public class Order {
    private Long id;
    private String customerName;
    private BigDecimal total;
}
```

Controller/service mapping:
```java
public OrderResponse toResponse(Order order) {
    return new OrderResponse(order.getId(), order.getCustomerName(), order.getTotal());
}
```

### Why DTOs matter
- prevents leaking entity internals
- decouples API contract from persistence model
- makes validation easier
- reduces accidental serialization of lazy-loaded data

---

## 17) How do you test Spring Boot applications?

### Answer
Use a mix of unit and integration tests:
- Unit tests: test business logic in isolation
- Integration tests: test controller + service + repository interactions
- MockMvc tests: test HTTP endpoints
- Testcontainers: spin up real DBs for integration tests

### Unit test example with JUnit and Mockito
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserService userService;

    @Test
    void shouldReturnUserById() {
        User user = new User("john@example.com", "pass123");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.getUser(1L);

        assertEquals("john@example.com", result.getEmail());
    }
}
```

### Integration test example
```java
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnOkForGetUserEndpoint() throws Exception {
        mockMvc.perform(get("/api/users/1"))
            .andExpect(status().isOk());
    }
}
```

### Good answer
A strong response includes testing at the service layer and real HTTP-level flow tests, not just controller mocks.

---

## 18) How do you structure a Spring Boot project for maintainability?

### Answer
A common structure is layered or modular:

```text
com.example.project
├── application
│   └── service
├── domain
│   ├── model
│   └── repository
├── infrastructure
│   ├── persistence
│   ├── security
│   └── external
├── api
│   └── controller
└── config
```

### Example domain/application boundary
```java
package com.example.orders.domain;
public class Order { ... }
```

```java
package com.example.orders.application;
public class CreateOrderUseCase {
    private final OrderRepository orderRepository;

    public OrderResponse execute(CreateOrderRequest request) {
        // business logic
        return null;
    }
}
```

### Why this matters
It separates:
- domain logic from infrastructure concerns
- API contracts from business rules
- persistence details from application logic

This helps with growth and team collaboration.

---

## 19) Explain how to design a production-ready Spring Boot application.

### Answer
A production-ready app should include:
- environment-specific profiles
- externalized configuration
- health checks
- structured logging
- centralized error handling
- database migrations
- metrics and monitoring
- secure secrets management
- Docker support
- CI/CD pipeline

### Example config
```yaml
spring:
  profiles:
    active: prod

management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus
```

### Why it matters
Production apps need more than just working APIs. They need observability, reliability, and operational safety.

---

## 20) How do you optimize database performance in a Spring Boot app?

### Answer
Key techniques:
- add indexes to commonly filtered columns
- avoid N+1 queries
- use pagination
- use `@Transactional(readOnly = true)` on read operations
- keep query scopes narrow
- avoid loading too much data in a single response
- optimize JPA fetch strategies
- tune connection pool settings

### Example
```java
@Transactional(readOnly = true)
public List<Order> getOrdersForCustomer(Long customerId) {
    return orderRepository.findByCustomerId(customerId);
}
```

### Interview note
The best candidates explain not only the technical fix, but also how they identify bottlenecks and validate improvements.

---

## 21) What is the difference between a modular monolith and microservices?

### Answer
A modular monolith is one deployable application with clear internal modules or feature boundaries. Microservices are multiple deployable services.

### Modular monolith advantages
- simpler deployment
- lower operational complexity
- easier migration path
- good for teams and moderate scale

### Microservices advantages
- independent scaling and deployment
- isolated failure domains
- better fit for large, complex systems

### Good answer
"A modular monolith is often the best first step when you want structure without the operational complexity of distributed systems."

---

## 22) How do you secure passwords?

### Answer
Never store plaintext passwords. Use a strong password hashing algorithm such as BCrypt.

### Example
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

Usage:
```java
String encoded = passwordEncoder.encode(rawPassword);
boolean matches = passwordEncoder.matches(rawPassword, encoded);
```

### Why BCrypt
- adaptive hashing
- resistant to brute-force attacks
- standard in Java/Spring security

---

## 23) What is the difference between `@RequestParam`, `@PathVariable`, and `@RequestBody`?

### Answer
- `@PathVariable`: value taken from URL path
- `@RequestParam`: query parameter or form parameter
- `@RequestBody`: raw JSON body mapped to Java object

### Example
```java
@GetMapping("/users/{id}")
public User getUser(@PathVariable Long id) {
    return userService.getUser(id);
}

@GetMapping("/users")
public List<User> getUsers(@RequestParam(defaultValue = "0") int page) {
    return userService.getUsers(page);
}

@PostMapping("/users")
public User createUser(@RequestBody UserRequest request) {
    return userService.createUser(request);
}
```

---

## 24) Explain something you’ve built recently and how you would talk about it in an interview.

### Example answer
"I built a modular monolith for an auth and order management system using Spring Boot and Java 17. The app had separate modules for auth, orders, and shared infrastructure. I used Spring Security with JWT for authentication and role-based access control for ADMIN and USER roles. I also added Flyway migrations for schema management, integration tests for auth flows, and Docker support for local development and production deployment. The key principle was keeping modules cohesive while maintaining one deployable application."

### What interviewers want to hear
- business problem
- design decisions
- trade-offs made
- testing/validation approach
- operational concerns

---

## 25) What are the most important things to know for a Spring Boot mid-level interview?

### Final checklist
Know these really well:
- Java fundamentals
- Dependency injection and Spring bean lifecycle
- Spring Boot auto-config and application properties
- REST API design
- JPA/Hibernate basics and transactions
- Spring Security and JWT
- Exception handling and validation
- Testing with JUnit and MockMvc
- Database optimization and indexing
- Production readiness: logging, metrics, health checks, Docker

### Strong interview answer pattern
"I focus on writing code that is maintainable, testable, and production-ready. I care about clear boundaries between layers, explicit dependencies, validation, error handling, and operational visibility."

---

## Quick 10-question cram sheet

1. What is Spring Boot and why use it?  
2. What is dependency injection and why constructor injection?  
3. What is the difference between @Controller and @RestController?  
4. How do you validate incoming API requests?  
5. How do you secure a Spring Boot app?  
6. What is JWT and how does it work?  
7. What is JPA and when do you use it?  
8. What is a transaction and why is @Transactional important?  
9. How do you avoid N+1?  
10. How do you make an app production-ready?  

---

## Example final interview answer template

> I’m a backend engineer focused on building maintainable Spring Boot services. I structure code around clear layers: controllers for API boundaries, services for business logic, repositories for persistence, and domain models for core business rules. I prefer constructor injection because it makes dependencies explicit and easier to test. For security, I use Spring Security and JWT for stateless authentication and role-based authorization. I validate all inputs with Bean Validation, centralize exceptions using `@RestControllerAdvice`, and use `@Transactional` around business flows that require atomic writes.
>
> For persistence, I use JPA/Hibernate and pay careful attention to query efficiency, indexing, and N+1 prevention. I also write both unit and integration tests and ensure the app is production-ready through environment-based configuration, actuator health checks, structured logging, and Docker support.

This is a solid mid-level Spring Boot answer because it shows depth, practical experience, and production awareness.
