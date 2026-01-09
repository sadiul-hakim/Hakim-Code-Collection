
# A) **Queues / Jobs** (Laravel’s PRIMARY concurrency standard)

> **This is the most accepted, most used, and safest concurrency model in Laravel.**

## 1️⃣ What queues actually do

* Each **job runs in a separate PHP process**
* Multiple workers = **true parallelism**
* Your HTTP request stays fast
* Failures, retries, delays are built-in

Think of it as:

> “Don’t make one request do everything — split work into workers.”

---

## 2️⃣ Basic mental diagram

```
HTTP Request
     |
     | dispatch()
     ▼
 Queue (Redis / DB / SQS)
     |
     | picked by workers
     ▼
Worker #1   Worker #2   Worker #3
 (Job A)     (Job B)     (Job C)
```

Each worker is a **separate PHP process**.

---

## 3️⃣ Simple example (your DB + API case)

### Step 1: Create jobs

```bash
php artisan make:job FetchDBJob
php artisan make:job FetchApiJob
```

---

### Step 2: DB Job

```php
class FetchDBJob implements ShouldQueue
{
    public function handle()
    {
        sleep(5); // simulate DB
        echo "DB done\n";
    }
}
```

---

### Step 3: API Job

```php
class FetchApiJob implements ShouldQueue
{
    public function handle()
    {
        sleep(3); // simulate API
        echo "API done\n";
    }
}
```

---

### Step 4: Dispatch both jobs

```php
FetchDBJob::dispatch();
FetchApiJob::dispatch();
```

---

### Step 5: Run workers (2 workers)

```bash
php artisan queue:work --queue=default
php artisan queue:work --queue=default
```

✔ DB job runs in worker #1
✔ API job runs in worker #2
✔ Total time ≈ **5 seconds**, not 8

---

## 4️⃣ Job Chaining & Batching (advanced)

### Chain (sequential but async)

```php
Bus::chain([
    new FetchDBJob(),
    new ProcessDataJob(),
    new NotifyUserJob(),
])->dispatch();
```

---

### Batch (parallel jobs + final callback)

```php
Bus::batch([
    new FetchDBJob(),
    new FetchApiJob(),
])->then(function () {
    echo "All done!";
})->dispatch();
```

✔ Parallel execution
✔ Final callback after all finish

---

## 5️⃣ When to use Queues (community rule)

✅ Use queues when:

* DB / API calls are slow
* Email, SMS, notifications
* Reports, exports, imports
* Anything > **200ms**

❌ Don’t use queues when:

* You need immediate response data
* Task is very fast

---

---

# B) **Parallel Helper / Child Processes**

> This is **process-level parallelism**, mostly for **CLI / batch jobs**.

## 1️⃣ What `Parallel::run()` actually does

* Forks **child PHP processes**
* Each closure runs in **its own process**
* Parent process waits for all to finish
* Uses `pcntl` or `parallel` under the hood

---

## 2️⃣ Basic example

```php
use Illuminate\Support\Parallel;

$results = Parallel::run([
    fn () => sleep(5),
    fn () => sleep(3),
]);

echo "Done";
```

✔ Total time ≈ **5 seconds**

---

## 3️⃣ Real example (DB + API)

```php
use Illuminate\Support\Parallel;

$results = Parallel::run([
    fn () => DB::table('users')->count(),
    fn () => Http::get('https://api.example.com')->json(),
]);

$userCount = $results[0];
$apiData   = $results[1];
```

---

## 4️⃣ Important limitations

⚠ Each closure:

* Has **no shared memory**
* Must re-open DB connections
* Cannot access request state safely

⚠ Heavy for HTTP requests:

* Process spawning is slow
* Can exhaust server memory

---

## 5️⃣ When to use Parallel

✅ Use Parallel when:

* CLI commands
* Batch processing
* Data migration
* Image/video processing
* CPU-heavy tasks

❌ Avoid Parallel when:

* Handling web requests
* High traffic APIs
* Shared state is needed

---

# 🔥 Comparison (Cheat Sheet)

| Feature              | Queues     | Parallel Helper |
| -------------------- | ---------- | --------------- |
| True parallelism     | ✅ Yes      | ✅ Yes           |
| HTTP request safe    | ✅ Yes      | ⚠ Risky         |
| Background execution | ✅ Yes      | ❌ No            |
| Retry / failure mgmt | ✅ Built-in | ❌ Manual        |
| Community standard   | ⭐⭐⭐⭐⭐      | ⭐⭐⭐             |

---

# 🧠 Final Laravel Community Wisdom

> **“If it doesn’t need to block the response — queue it.”**
> **“If it’s heavy & batch-based — parallelize it in CLI.”**

Laravel **intentionally avoids async-in-request complexity** and embraces **process-based concurrency**, which scales **cleanly and predictably**.