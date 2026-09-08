# Spring Boot Mock Interview Script for Mid-Level Backend Developer

This is a practical mock interview script you can use to rehearse. It is designed to feel realistic: interviewer asks a question, candidate answers, then follow-up questions dig into trade-offs, implementation details, and production readiness.

Use it like this:
- Read the interviewer prompt
- Answer out loud as if in the interview
- Then review the sample answer and improve your own version
- Repeat with follow-up questions

---

## 1) Warm-up: Tell me about yourself and your experience as a backend developer.

### Interviewer prompt
Tell me about yourself and your backend experience.

### Strong sample answer
"I’m a backend developer with experience building REST APIs in Java and Spring Boot. My work has centered on creating secure, maintainable services for business workflows, especially where authentication, authorization, and persistence are important. In my recent work, I built modular monolith services using Spring Boot, Spring Security, JWT, JPA, and PostgreSQL. I focus on writing clean layered code, making sure APIs are testable, and validating the behavior with integration tests. I also care about production readiness: logging, health checks, migrations, and deployment concerns."

### Follow-up questions
- What part of the stack do you enjoy most?
- What was the hardest bug you fixed recently?
- What do you care about most in code quality?

### What they are checking
- communication
- clarity
- ability to talk about architecture decisions, not just frameworks

---

## 2) Spring Boot basics: What is Spring Boot, and why do you use it?

### Interviewer prompt
What is Spring Boot, and why would you choose it for backend development?

### Strong sample answer
"Spring Boot is a framework built on top of the Spring ecosystem that reduces boilerplate configuration and helps developers start quickly. It uses opinionated defaults, auto-configuration, embedded servers, and dependency starters. In a backend project, that means I can focus on business logic rather than wiring up data sources, web servers, and configuration manually. It also gives me a mature ecosystem for security, persistence, testing, and monitoring."

### Follow-up questions
- What is the difference between Spring and Spring Boot?
- What is auto-configuration, and why is it useful?
- When would you avoid Boot?

### Sample follow-up answer
"Spring is the foundational framework. Spring Boot adds convention-based setup and reduces configuration overhead. Auto-configuration is useful because if the necessary dependencies are in the classpath, Spring Boot will configure beans for you. For example, if I add Spring Data JPA and a datasource, it will configure JPA infrastructure automatically. I would avoid Boot only when I need very custom infrastructure or a highly custom runtime setup where explicit configuration is necessary."

---

## 3) Dependency Injection: What is dependency injection, and why is constructor injection preferred?

### Interviewer prompt
Explain dependency injection and why constructor injection is preferred.

### Strong sample answer
"Dependency injection is a design pattern where dependencies are supplied into a class instead of being created inside it. This reduces coupling, makes code more testable, and improves maintainability. Constructor injection is preferred because it makes dependencies explicit and required at object creation time. It also supports immutability and makes unit tests straightforward. In contrast, field injection hides dependencies and makes the class harder to reason about."

### Code example
```java
@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final PaymentService paymentService;

    public OrderService(OrderRepository orderRepository, PaymentService paymentService) {
        this.orderRepository = orderRepository;
        this.paymentService = paymentService;
    }
}
```

### Follow-up questions
- Why not use field injection?
- What if a dependency is optional?

### Sample answer to follow-up
"Field injection works, but it’s less explicit and can make tests more brittle. For required dependencies, constructor injection is cleaner. If a dependency is truly optional, you can design for that explicitly via default values or an interface implementation, rather than relying on field injection."

---

## 4) REST API design: How do you design a clean REST API?

### Interviewer prompt
How do you design a clean REST API?

### Strong sample answer
"I start by defining a clear domain model and resource structure. I prefer meaningful endpoints and resource-oriented URLs, for example `/api/orders/{id}` rather than action-based names like `/api/getOrder`. I use proper HTTP methods: GET for retrieval, POST for creation, PUT/PATCH for updates, DELETE for deletion. I also keep request/response models separate from persistence entities with DTOs, which helps keep contracts stable and prevents leaking internal data."

### Code example
```java
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrder(id));
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
```

### Follow-up questions
- How do you handle versioning?
- How do you handle validation and error responses?

### Sample answer
"I use DTOs and centralized validation. If I need API evolution, I can version the route or media type. Error responses should be consistent and structured, not raw exceptions. That makes clients easier to consume and helps debugging."

---

## 5) Validation: How do you validate incoming request bodies?

### Interviewer prompt
How do you validate request inputs in Spring Boot?

### Strong sample answer
"I validate input at the API boundary using Bean Validation annotations like `@NotBlank`, `@Email`, `@Size`, and `@NotNull`, and I apply `@Valid` to the request parameter. I also have a global exception handler to convert validation failures into structured API errors. This keeps controllers clean and gives clients consistent feedback."

### Code example
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
@PostMapping("/register")
public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
    return ResponseEntity.ok().build();
}
```

Exception handler:
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

### Follow-up question
"What about business validation?"

### Sample answer
"I usually keep framework validation for syntax and contract validation, then I add domain-level validation in the service layer for rules like duplicate email or invalid state transitions."

---

## 6) Error handling: How do you handle exceptions in a production API?

### Interviewer prompt
How would you handle exceptions in a Spring Boot REST application?

### Strong sample answer
"I prefer a centralized exception handler using `@RestControllerAdvice`. It allows me to convert domain and framework exceptions into consistent HTTP responses. That keeps the controller code clean and gives clients predictable, safe errors. I also log unexpected exceptions with context, but I avoid exposing internal stack traces or implementation details to end users."

### Code example
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(ResourceNotFoundException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }
}
```

### Follow-up questions
- Should you return stack traces to clients?
- How do you differentiate validation and server errors?

### Sample answer
"No, I wouldn’t expose stack traces. Client-facing errors should be concise and safe. I use 400 for validation or bad input, 401/403 for auth issues, 404 for not found, and 500 only for unexpected internal failures."

---

## 7) Spring Security: How do you secure a Spring Boot application?

### Interviewer prompt
How would you secure a REST application using Spring Security?

### Strong sample answer
"I would configure Spring Security to enforce authentication and authorization rules. I’d use stateless JWT authentication for REST APIs, restrict public endpoints like login/register, and require authentication for business endpoints. I’d also define role-based access checks such as USER and ADMIN. Passwords should be encoded using BCrypt."

### Code example
```java
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
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

### Follow-up questions
- What is the difference between authentication and authorization?
- Why use stateless sessions?

### Sample answer
"Authentication answers who the user is. Authorization answers what they’re allowed to do. Stateless JWT-based auth is common for APIs because it scales more easily and avoids server-side session storage."

---

## 8) JWT: What is JWT, and how does it work in a Spring Boot app?

### Interviewer prompt
Explain JWT and how it is used in authentication.

### Strong sample answer
"JWT is a compact token that carries encoded claims. In a Spring Boot REST API, the server usually issues a JWT after a successful login. The client stores the token and sends it in the Authorization header. The server validates the token on each request and extracts user identity and roles from the claims. JWT is useful because it is stateless and works well for APIs and mobile apps."

### Code example
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

### Follow-up questions
- How do you handle token refresh and logout?
- How do you revoke JWTs?

### Sample answer
"JWTs are difficult to revoke by default because they are stateless. A common pattern is to issue short-lived access tokens and longer-lived refresh tokens. Refresh tokens can be stored and revoked in a database, which gives you a practical way to log users out and rotate tokens."

---

## 9) JPA/Hibernate: Explain JPA and why we use it.

### Interviewer prompt
What is JPA, and why do developers use it?

### Strong sample answer
"JPA is the Java Persistence API, a specification for object-relational mapping. Hibernate is a popular implementation. It lets us map Java objects to database tables, so we can work with entities instead of writing all the SQL manually. This reduces boilerplate and makes domain modeling easier."

### Code example
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;
}
```

Repository:
```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
```

### Follow-up question
- What are the trade-offs?

### Sample answer
"JPA hides a lot of SQL complexity, which is great for productivity, but it can also hide performance problems if you don’t think about query efficiency, lazy loading, and mapping choices. You still need to understand SQL and database behavior as a backend developer."

---

## 10) JPA relationships: Explain the common mapping types.

### Interviewer prompt
Explain OneToOne, OneToMany, ManyToOne, and ManyToMany with JPA.

### Strong sample answer
"`@ManyToOne` and `@OneToMany` are the most common relationships. Usually the many side owns the foreign key. For example, a `Order` belongs to a `Customer`, so the `Order` table has a `customer_id` column. `@OneToOne` is used when one row in one table corresponds to one row in another table. `@ManyToMany` is used for more complex associations and requires a join table."

### Code example
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

### Follow-up questions
- What is the owning side?
- How do you avoid N+1?

### Sample answer
"The owning side is the entity that contains the foreign key column. For `Order -> Customer`, `Order` is the owning side. To avoid N+1, I use fetch joins, `@EntityGraph`, or query DTO projections when a relationship is needed in a list query."

---

## 11) Transactions: What is @Transactional, and why does it matter?

### Interviewer prompt
What is a transaction, and why do we use `@Transactional`?

### Strong sample answer
"A transaction is a unit of work that either completes successfully or is rolled back if something fails. It ensures consistency, especially when multiple database writes must succeed together. In Spring, `@Transactional` marks the boundary for that transaction. For example, transferring money between accounts should be atomic: either both updates succeed or neither does."

### Code example
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

### Follow-up questions
- Would you mark read-only methods as `@Transactional(readOnly = true)`?
- What is propagation?

### Sample answer
"Yes, read-only methods can be marked as `@Transactional(readOnly = true)` to signal intent and allow optimization. Propagation defines how a transaction behaves when called inside another transaction, for example `REQUIRES_NEW` for a new transaction or `REQUIRED` for reusing the current one."

---

## 12) N+1 problem: What is it, and how do you fix it?

### Interviewer prompt
What is the N+1 problem, and how do you fix it?

### Strong sample answer
"N+1 occurs when a query fetches a list of parent rows and then triggers additional queries for each item to load related data. This often happens with lazy loading in loops. It can severely affect performance, especially with large collections."

### Code example
```java
List<Order> orders = orderRepository.findAll();
for (Order order : orders) {
    System.out.println(order.getCustomer().getName());
}
```

This can trigger 1 query for orders and then one query per order.

### Better approach
```java
@Query("SELECT o FROM Order o JOIN FETCH o.customer c")
List<Order> findAllWithCustomers();
```

### Follow-up question
- What else can you do?

### Sample answer
"I use fetch joins, `@EntityGraph`, DTO projections, and query optimization. I also review logs and query plans when performance issues appear."

---

## 13) Testing: How do you test Spring Boot services and APIs?

### Interviewer prompt
How do you test Spring Boot applications?

### Strong sample answer
"I write both unit tests and integration tests. Unit tests focus on business logic using JUnit and Mockito. Integration tests validate that the application stack works, including HTTP endpoints, security, and database interactions. For API testing, MockMvc is very useful, and for database integration testing, Testcontainers is great when you need a real database environment."

### Unit test example
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

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

### Follow-up question
- What is the difference between unit tests and integration tests?

### Sample answer
"Unit tests isolate logic and are fast. Integration tests validate the actual application behavior in a more realistic environment, including HTTP, security, and database flow. A strong backend quality strategy uses both."

---

## 14) Database performance: How would you optimize a slow query?

### Interviewer prompt
How do you optimize a slow database query in a Spring Boot application?

### Strong sample answer
"I start by identifying whether the query is truly slow in the database or caused by N+1 or poor fetch strategy. Then I inspect the query plan, indexes, joins, and data volume. I would add the appropriate indexes, reduce unnecessary selected columns, limit data early, and avoid repeated round-trips. I’d also review if pagination or projection is needed."

### Example
```java
@Query("SELECT new com.example.orders.api.OrderSummary(o.id, o.customerName, o.total) FROM Order o WHERE o.customerId = :customerId")
List<OrderSummary> findSummariesByCustomerId(Long customerId);
```

### Follow-up question
- What kind of index would you add?

### Sample answer
"I’d add indexes on the columns used in filters, joins, and ordering. For example, if we frequently query by `customer_id` and `status`, I’d add a composite index on `(customer_id, status)`."

---

## 15) Production readiness: How do you make a Spring Boot app production-ready?

### Interviewer prompt
What does a production-ready Spring Boot application include?

### Strong sample answer
"A production-ready app needs more than just a working service. I’d ensure config is profile-based, secrets are externalized, logging is structured, health endpoints are exposed, metrics are available, and deployment is safe. I’d also ensure database migrations are tracked, app health is validated by the orchestrator, and error handling is consistent."

### Example config
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus
```

### Follow-up question
- Why do metrics and health checks matter?

### Sample answer
"They help with monitoring, alerting, and graceful deployment. Liveness and readiness endpoints tell orchestrators when the app is safe to receive traffic and when it should restart or remain out of rotation."

---

## 16) Modular monolith: What is a modular monolith, and when would you choose it?

### Interviewer prompt
What is a modular monolith, and why would you build one instead of microservices?

### Strong sample answer
"A modular monolith is a single deployable application organized into cohesive internal modules or feature areas. It keeps the deployment simplicity of a monolith while creating boundaries between domains like auth, orders, and billing. It is a good fit when you want maintainability and modularity without the operational complexity of distributed systems."

### Example structure
```text
com.example.app
├── auth
├── orders
├── shared
└── bootstrap
```

### Follow-up questions
- What are the pros and cons?
- When would you choose microservices instead?

### Sample answer
"The benefit is simplicity in deployment and communication. The drawback is that the app can become monolithic over time if boundaries are not enforced. I’d choose microservices when the system has independently scaling concerns, very different deployment lifecycles, or a high need for team autonomy."

---

## 17) Design patterns and architecture: What architecture patterns do you use?

### Interviewer prompt
What architectural patterns do you use in backend development?

### Strong sample answer
"I usually apply layered architecture or clean architecture ideas. That means separation between API, application, domain, and infrastructure layers. This keeps business logic independent from framework-specific code and makes the system easier to evolve. I also use common design patterns like dependency injection, repository pattern, DTO mapping, and sometimes strategy or factory patterns for branching logic."

### Coding example
```java
public class CreateOrderUseCase {
    private final OrderRepository orderRepository;

    public CreateOrderUseCase(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public OrderResponse execute(CreateOrderRequest request) {
        Order order = new Order(request.customerName(), request.product(), request.total());
        Order saved = orderRepository.save(order);
        return new OrderResponse(saved.getId(), saved.getCustomerName(), saved.getProduct(), saved.getTotal());
    }
}
```

### Why it matters
This separates business logic from HTTP and persistence implementation.

---

## 18) SQL fundamentals: What SQL topics do you need to know for backend interviews?

### Interviewer prompt
What SQL concepts do you need to know as a backend developer?

### Strong sample answer
"I need to know joins, group by, aggregate functions, indexes, filtering, ordering, transactions, and database constraints. I also need to understand how query performance changes with indexes and data volume."

### Example query
```sql
SELECT customer_id, COUNT(*) AS total_orders
FROM orders
WHERE status = 'PAID'
GROUP BY customer_id
HAVING COUNT(*) > 1;
```

### Follow-up question
- Why are indexes important?

### Sample answer
"Indexes allow the database to locate rows faster, especially on fields used in `WHERE`, `JOIN`, and `ORDER BY` clauses. Without indexes, scans become much more expensive as the table grows."

---

## 19) Explain a project you delivered recently.

### Interviewer prompt
Tell me about a backend project you built and the design decisions behind it.

### Strong sample answer
"I recently built a modular monolith with Spring Boot for authentication and order management. The app was divided into auth and orders modules with a shared module for common configuration and utilities. I used Spring Security with JWT, role-based access control, Flyway migrations, JPA for persistence, and Docker for deployment. The design choice was to keep a single deployable application but enforce module boundaries, which reduced operational complexity while keeping the code maintainable."

### Follow-up question
- What was the hardest part?

### Sample answer
"The most difficult part was balancing modularity with simplicity. We wanted module boundaries, but we still needed one application to deploy and manage. The answer was strong package organization, clear use-case boundaries, and validation with integration tests."

---

## 20) Final challenge: Answer this in 2 minutes

### Interviewer prompt
Walk me through the flow of a login request in a Spring Boot app with JWT.

### Strong sample answer
"A user submits credentials to `/api/auth/login`. The controller receives the request and passes it to the auth service. The service loads the user from the repository and verifies the password using BCrypt. If it matches, the app creates an access token and a refresh token. The JWT contains claims like the user email and role, and it is signed using a server secret. The server returns the tokens to the client. On subsequent requests, the client includes the access token in the Authorization header. Spring Security parses the token in a filter, validates it, and sets the authentication in the security context. If the user hits an endpoint requiring a role, method security checks the authorities and allows or denies access. If the access token expires, the client calls the refresh endpoint using the refresh token, and the server rotates it if valid."

### Why this is a good answer
It shows understanding of:
- controller/service/repository flow
- security
- JWT
- validation
- authorization
- refresh tokens

---

## Quick checklist before the interview

Make sure you can speak confidently about:
- Spring Boot basics
- Dependency injection and constructor injection
- REST controller design and validation
- Spring Security + JWT
- JPA + transactions
- SQL basics and performance
- Testing strategy
- Error handling
- Production readiness and monitoring

---

## Best way to practice

1. Answer each question aloud without notes
2. Keep answers under 2–3 minutes
3. Add a small code snippet when relevant
4. Explain trade-offs and why you choose a pattern
5. Practice with follow-up questions to show depth

This makes you sound like a mid-level engineer who understands both implementation and system thinking.
