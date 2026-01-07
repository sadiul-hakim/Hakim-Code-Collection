Perfect. Let’s build a **clean, production-grade API auth flow** in Laravel:

> **Controller → Service → Policy (Role/Permission based)**
> **API only (Sanctum tokens)**
> **No session / no views**

I’ll structure this exactly like a **Spring Security mental model**, not “Laravel magic”.

---

# 🔐 Target Architecture

```
Request
  ↓
AuthController
  ↓
AuthService
  ↓
Token (Sanctum)
  ↓
auth:sanctum middleware
  ↓
Policy / Gate (Authorization)
  ↓
Service (Business logic)
```

---

## 1️⃣ Database Design (Minimal & Clean)

### users table

```sql
users
- id
- name
- email
- password
```

### roles

```sql
roles
- id
- name   (admin, user, moderator)
```

### permissions

```sql
permissions
- id
- name   (create-post, delete-post)
```

### pivot tables

```sql
role_user
- user_id
- role_id

permission_role
- role_id
- permission_id
```

📌 **Why role → permission?**
This scales better than assigning permissions directly to users.

---

## 2️⃣ Models & Relationships

### User.php

```php
class User extends Authenticatable
{
    use HasApiTokens;

    public function roles()
    {
        return $this->belongsToMany(Role::class);
    }

    public function hasRole(string $role): bool
    {
        return $this->roles()->where('name', $role)->exists();
    }

    public function hasPermission(string $permission): bool
    {
        return $this->roles()
            ->whereHas('permissions', fn ($q) =>
                $q->where('name', $permission)
            )
            ->exists();
    }
}
```

---

### Role.php

```php
class Role extends Model
{
    public function permissions()
    {
        return $this->belongsToMany(Permission::class);
    }
}
```

---

## 3️⃣ Auth Service (Login / Token Creation)

### Contract

```php
namespace App\Services\Contracts;

interface AuthServiceInterface
{
    public function login(array $credentials): array;
}
```

---

### Implementation

```php
class AuthService implements AuthServiceInterface
{
    public function login(array $credentials): array
    {
        if (!Auth::attempt($credentials)) {
            throw new AuthenticationException();
        }

        $user = Auth::user();

        $token = $user->createToken(
            'api-token',
            $this->resolveAbilities($user)
        )->plainTextToken;

        return [
            'user' => $user,
            'token' => $token,
        ];
    }

    private function resolveAbilities($user): array
    {
        return $user->roles
            ->flatMap(fn ($role) => $role->permissions)
            ->pluck('name')
            ->unique()
            ->values()
            ->toArray();
    }
}
```

📌 **Abilities = permissions**
Sanctum will embed them inside the token.

---

## 4️⃣ Auth Controller (Thin)

```php
class AuthController extends Controller
{
    public function __construct(
        private AuthServiceInterface $authService
    ) {}

    public function login(Request $request)
    {
        $data = $request->validate([
            'email' => 'required|email',
            'password' => 'required',
        ]);

        return response()->json(
            $this->authService->login($data)
        );
    }
}
```

---

## 5️⃣ API Middleware Protection

### routes/api.php

```php
Route::middleware('auth:sanctum')->group(function () {

    Route::post('/posts', [PostController::class, 'store']);
    Route::delete('/posts/{post}', [PostController::class, 'destroy']);

});
```

This is equivalent to:

```java
http.authorizeRequests().authenticated();
```

---

## 6️⃣ Authorization Layer (Policy)

### PostPolicy.php

```php
class PostPolicy
{
    public function create(User $user): bool
    {
        return $user->hasPermission('create-post');
    }

    public function delete(User $user, Post $post): bool
    {
        return $user->hasPermission('delete-post');
    }
}
```

---

### Register Policy

```php
protected $policies = [
    Post::class => PostPolicy::class,
];
```

---

## 7️⃣ Controller → Policy → Service

### PostController

```php
class PostController extends Controller
{
    public function __construct(
        private PostService $postService
    ) {}

    public function store(Request $request)
    {
        $this->authorize('create', Post::class);

        return response()->json(
            $this->postService->create(
                $request->validated()
            ),
            201
        );
    }

    public function destroy(Post $post)
    {
        $this->authorize('delete', $post);

        $this->postService->delete($post);

        return response()->noContent();
    }
}
```

📌 **Authorization happens BEFORE business logic**

---

## 8️⃣ Service Layer (Business Logic)

```php
class PostService
{
    public function create(array $data)
    {
        return Post::create($data);
    }

    public function delete(Post $post): void
    {
        $post->delete();
    }
}
```

---

## 9️⃣ Ability-Based Middleware (Optional but Powerful)

### Route-level permission

```php
Route::post('/posts',
    [PostController::class, 'store']
)->middleware('abilities:create-post');
```

Equivalent to:

```java
@PreAuthorize("hasAuthority('create-post')")
```

---

## 🔑 Role-based Alternative (Simpler)

If you **only want roles**, skip permissions:

```php
Gate::define('admin-only', fn (User $user) =>
    $user->hasRole('admin')
);
```

Usage:

```php
$this->authorize('admin-only');
```

---

## 10️⃣ Mental Map (Spring Comparison)

| Spring Security  | Laravel            |
| ---------------- | ------------------ |
| JWT claims       | Sanctum abilities  |
| @PreAuthorize    | Policy / abilities |
| SecurityContext  | Auth::user()       |
| UserDetails      | User model         |
| GrantedAuthority | Permission         |

---

## 11️⃣ Why This Is Scalable

✔ Stateless API
✔ Token-based
✔ Testable services
✔ No fat controllers
✔ Clean separation
✔ Easy microservice split later