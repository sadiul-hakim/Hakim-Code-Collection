
# 🔐 Laravel MVC Auth Flow (Web App)

> **Controller → Service → Policy → View**
> **Session-based auth (no API tokens)**

```
Browser
  ↓
Web Controller
  ↓
AuthService
  ↓
Session (cookie)
  ↓
auth middleware
  ↓
Policy / Gate
  ↓
Service (Business Logic)
  ↓
Blade View
```

---

## 1️⃣ Web Auth Basics (Important Difference from API)

| API            | Web               |
| -------------- | ----------------- |
| Sanctum token  | Session cookie    |
| JSON           | Redirect + Blade  |
| Stateless      | Stateful          |
| `auth:sanctum` | `auth` middleware |

Laravel handles **session + cookie** automatically.

---

## 2️⃣ Routes (web.php)

```php
use App\Http\Controllers\AuthController;
use App\Http\Controllers\PostController;

Route::get('/login', [AuthController::class, 'showLogin'])
    ->name('login');

Route::post('/login', [AuthController::class, 'login']);

Route::post('/logout', [AuthController::class, 'logout'])
    ->middleware('auth');

Route::middleware('auth')->group(function () {

    Route::get('/dashboard', fn () => view('dashboard'));

    Route::post('/posts', [PostController::class, 'store']);
    Route::delete('/posts/{post}', [PostController::class, 'destroy']);
});
```

---

## 3️⃣ Auth Service (Session-Based)

### Contract

```php
namespace App\Services\Contracts;

interface AuthServiceInterface
{
    public function login(array $credentials): void;
    public function logout(): void;
}
```

---

### Implementation

```php
class AuthService implements AuthServiceInterface
{
    public function login(array $credentials): void
    {
        if (!Auth::attempt($credentials)) {
            throw new AuthenticationException();
        }

        session()->regenerate(); // 🔒 session fixation protection
    }

    public function logout(): void
    {
        Auth::logout();

        session()->invalidate();
        session()->regenerateToken();
    }
}
```

📌 **No token creation**
📌 Laravel writes session ID to cookie

---

## 4️⃣ Auth Controller (Thin + Redirects)

```php
class AuthController extends Controller
{
    public function __construct(
        private AuthServiceInterface $authService
    ) {}

    public function showLogin()
    {
        return view('auth.login');
    }

    public function login(Request $request)
    {
        $credentials = $request->validate([
            'email' => 'required|email',
            'password' => 'required',
        ]);

        try {
            $this->authService->login($credentials);
            return redirect()->intended('/dashboard');
        } catch (AuthenticationException $e) {
            return back()
                ->withErrors(['email' => 'Invalid credentials'])
                ->withInput();
        }
    }

    public function logout()
    {
        $this->authService->logout();
        return redirect('/login');
    }
}
```

Equivalent Spring MVC flow:

```java
@PostMapping("/login")
public String login(...) {
    return "redirect:/dashboard";
}
```

---

## 5️⃣ Middleware: Authentication

Laravel auto-registers:

```php
->middleware('auth')
```

This:

* Checks session
* Redirects to `login` route if unauthenticated

📌 **Where does Laravel know the login page?**

```php
// app/Http/Middleware/Authenticate.php
protected function redirectTo($request)
{
    return route('login');
}
```

---

## 6️⃣ Authorization (Policy Layer)

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

Same policy works for **web + api**.

---

## 7️⃣ Controller → Policy → Service

### PostController (MVC)

```php
class PostController extends Controller
{
    public function __construct(
        private PostService $postService
    ) {}

    public function store(Request $request)
    {
        $this->authorize('create', Post::class);

        $post = $this->postService->create(
            $request->validate([
                'title' => 'required',
                'body' => 'required',
            ])
        );

        return redirect()
            ->route('posts.show', $post)
            ->with('success', 'Post created');
    }

    public function destroy(Post $post)
    {
        $this->authorize('delete', $post);

        $this->postService->delete($post);

        return redirect()
            ->back()
            ->with('success', 'Post deleted');
    }
}
```

📌 Authorization is enforced **before** business logic.

---

## 8️⃣ Role / Permission Check in Views (Blade)

### Role check

```blade
@auth
    @if(auth()->user()->hasRole('admin'))
        <a href="/admin">Admin Panel</a>
    @endif
@endauth
```

---

### Permission check

```blade
@can('create', App\Models\Post::class)
    <button>Create Post</button>
@endcan
```

Spring equivalent:

```jsp
<sec:authorize access="hasAuthority('create-post')">
```

---

## 9️⃣ Gate (Simple Role-Based)

For **simple rules**, Gates are enough.

```php
Gate::define('admin-only', fn (User $user) =>
    $user->hasRole('admin')
);
```

Usage:

```php
@can('admin-only')
```

or

```php
$this->authorize('admin-only');
```

---

## 🔑 Role-based vs Permission-based (Web)

| Use Case   | Choose                    |
| ---------- | ------------------------- |
| Small app  | Roles                     |
| Medium app | Roles + permissions       |
| Large app  | Permissions               |
| Enterprise | Policy + Permission cache |

---

## 10️⃣ Common MVC Mistakes (Avoid These)

❌ Auth logic in controller
❌ Policy logic in service
❌ Database queries in Blade
❌ Checking role in every controller method

---

## 11️⃣ Mental Mapping (Spring → Laravel)

| Spring MVC          | Laravel MVC     |
| ------------------- | --------------- |
| HttpSession         | Laravel Session |
| SecurityFilterChain | Middleware      |
| @PreAuthorize       | Policy / Gate   |
| UserDetails         | User model      |
| RedirectView        | redirect()      |

---

## ✅ Final Rule of Thumb

> **Controllers talk to Services**
> **Services never talk to HTTP**
> **Policies never talk to DB directly**
> **Views only ask permissions**