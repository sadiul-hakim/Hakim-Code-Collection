1. What Laravel Dependency Injection actually is
2. Service Container (Laravel’s “IoC”) – Spring comparison
3. Creating **Service classes** for business logic
4. **Constructor injection** in controllers
5. Binding interfaces → implementations (important)
6. Using services for **Auth logic (AuthService)**
7. When to use Service Providers
8. Common mistakes (Laravel devs make these a lot)

---

## 1️⃣ What is Laravel Dependency Injection?

**Dependency Injection (DI)** means:

> A class does NOT create its own dependencies.
> They are **provided (injected)** from outside.

### Bad (tight coupling)

```php
class OrderController
{
    public function store()
    {
        $service = new OrderService(); // ❌ hard dependency
        $service->create();
    }
}
```

### Good (DI)

```php
class OrderController
{
    public function __construct(
        private OrderService $orderService
    ) {}

    public function store()
    {
        $this->orderService->create();
    }
}
```

Laravel automatically creates `OrderService` for you via its **Service Container**.

---

## 2️⃣ Laravel Service Container (Spring analogy)

| Spring Boot              | Laravel               |
| ------------------------ | --------------------- |
| `@Component`, `@Service` | Plain PHP class       |
| `@Autowired`             | Constructor type-hint |
| ApplicationContext       | Service Container     |
| `@Bean`                  | `$this->app->bind()`  |

📌 **Key difference**:
Laravel does **NOT** require annotations.
Type-hinting is enough.

---

## 3️⃣ Creating Service Classes (Business Logic)

### Folder structure (recommended)

```
app/
 ├── Http/
 │    └── Controllers/
 ├── Services/
 │    ├── UserService.php
 │    ├── AuthService.php
 │    └── OrderService.php
 ├── Repositories/
 │    └── UserRepository.php (optional)
```

---

### Example: `UserService`

```php
namespace App\Services;

use App\Models\User;
use Illuminate\Support\Facades\Hash;

class UserService
{
    public function create(array $data): User
    {
        return User::create([
            'name' => $data['name'],
            'email' => $data['email'],
            'password' => Hash::make($data['password']),
        ]);
    }
}
```

✅ Business logic
❌ No HTTP / Request / Response logic
❌ No `Request` object

---

## 4️⃣ Injecting Service into Controller (Authwire)

### Controller using constructor injection

```php
namespace App\Http\Controllers;

use App\Services\UserService;
use Illuminate\Http\Request;

class UserController extends Controller
{
    public function __construct(
        private UserService $userService
    ) {}

    public function store(Request $request)
    {
        $user = $this->userService->create(
            $request->validated()
        );

        return response()->json($user, 201);
    }
}
```

Laravel resolves `UserService` automatically.

This is equivalent to:

```java
@RequiredArgsConstructor
@RestController
public class UserController {
    private final UserService userService;
}
```

---

## 5️⃣ Interface-based DI (Very Important)

For **testability and clean design**, use interfaces.

### Step 1: Interface

```php
namespace App\Services\Contracts;

interface AuthServiceInterface
{
    public function login(array $credentials): array;
}
```

---

### Step 2: Implementation

```php
namespace App\Services;

use App\Services\Contracts\AuthServiceInterface;
use Illuminate\Support\Facades\Auth;

class AuthService implements AuthServiceInterface
{
    public function login(array $credentials): array
    {
        if (!Auth::attempt($credentials)) {
            throw new \Exception("Invalid credentials");
        }

        $user = Auth::user();
        $token = $user->createToken('api')->plainTextToken;

        return [
            'user' => $user,
            'token' => $token,
        ];
    }
}
```

---

### Step 3: Bind Interface → Implementation

📍 **Service Provider** (usually `AppServiceProvider`)

```php
use App\Services\AuthService;
use App\Services\Contracts\AuthServiceInterface;

public function register(): void
{
    $this->app->bind(
        AuthServiceInterface::class,
        AuthService::class
    );
}
```

Spring equivalent:

```java
@Bean
public AuthServiceInterface authService() {
    return new AuthService();
}
```

---

### Step 4: Inject Interface in Controller

```php
use App\Services\Contracts\AuthServiceInterface;

class AuthController extends Controller
{
    public function __construct(
        private AuthServiceInterface $authService
    ) {}

    public function login(Request $request)
    {
        return response()->json(
            $this->authService->login(
                $request->only('email', 'password')
            )
        );
    }
}
```

🔥 This is **real DI**, not Laravel “magic”.

---

## 6️⃣ Auth Logic: Where Things Go (Best Practice)

| Layer      | Responsibility                     |
| ---------- | ---------------------------------- |
| Controller | HTTP, request, response            |
| Service    | Business rules                     |
| Model      | Data structure                     |
| Middleware | Auth checks                        |
| Policy     | Authorization (roles, permissions) |

❌ **Do NOT** put auth logic in controller
❌ **Do NOT** call `Auth::attempt()` everywhere

---

## 7️⃣ When to Use Service Providers?

Use them when:

✔ Binding interfaces
✔ Registering external services (Redis, SDKs)
✔ Config-based services

You **do NOT** need a provider for every service.

Laravel auto-resolves **concrete classes**.

---

## 8️⃣ Common Laravel DI Mistakes

### ❌ Fat Controllers

```php
public function store(Request $request)
{
    User::create([...]);
    Mail::send(...);
    Log::info(...);
}
```

### ✅ Thin Controller

```php
$this->userService->register($data);
```

---

### ❌ Static Facades Everywhere

```php
Auth::attempt();
DB::transaction();
```

### ✅ Wrap inside services

```php
$this->authService->login();
```

---

## 9️⃣ Mental Model (Remember This)

> **Laravel DI = Spring DI without annotations**

• Constructor injection
• Interface binding
• Service container
• Clean separation