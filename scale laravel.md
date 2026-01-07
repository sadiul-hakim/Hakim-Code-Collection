 When people say “scale Laravel”, **the first steps are not Kubernetes or microservices**. For a typical Laravel app, the *initial scaling steps* are very practical and boring—but extremely effective.

Below is a **clean, realistic first-stage scaling checklist**, in the right order.

---

## 1️⃣ Fix the Basics (before any infra)

### ✅ Use Production Settings

* `APP_ENV=production`
* `APP_DEBUG=false`
* Proper `.env` values (no defaults)

### ✅ Cache everything Laravel can cache

These give **huge gains** for almost zero effort:

```bash
php artisan config:cache
php artisan route:cache
php artisan view:cache
php artisan event:cache
```

⚠️ Re-run after every deploy.

---

## 2️⃣ Web Server & PHP Setup (very important)

### ✅ Use **Nginx + PHP-FPM** (not Apache + mod_php)

This alone can 2–5× throughput.

### PHP-FPM tuning (example)

```ini
pm = dynamic
pm.max_children = 50
pm.start_servers = 10
pm.min_spare_servers = 10
pm.max_spare_servers = 20
```

Adjust based on RAM:

* Each PHP worker ≈ **20–40 MB**
* 2 GB RAM → ~40–60 workers max

---

## 3️⃣ Use OPcache (mandatory)

In `php.ini`:

```ini
opcache.enable=1
opcache.memory_consumption=256
opcache.max_accelerated_files=20000
opcache.validate_timestamps=0
```

This removes PHP re-parsing overhead almost entirely.

---

## 4️⃣ Database Scaling (this is where most apps die)

### ✅ Add proper indexes

* `where`
* `join`
* `order by`
* `foreign keys`

**No index = no scaling**

### ✅ Avoid N+1 queries

Always use:

```php
User::with('posts')->get();
```

Use **Laravel Debugbar or Telescope** in staging to detect this.

---

## 5️⃣ Cache at the Application Level

### Move cache/session to Redis

```env
CACHE_DRIVER=redis
SESSION_DRIVER=redis
QUEUE_CONNECTION=redis
```

Use Redis for:

* Auth sessions
* Heavy queries
* API rate limiting
* Feature flags

Example:

```php
Cache::remember('products', 60, fn() => Product::all());
```

---

## 6️⃣ Queues (huge win, very early)

Anything slow should be async:

* Emails
* Notifications
* PDF generation
* Image processing
* Webhooks

```env
QUEUE_CONNECTION=redis
```

Run workers:

```bash
php artisan queue:work --daemon
```

This **dramatically reduces request time**.

---

## 7️⃣ Horizontal Scaling (first real “scaling” step)

When one server isn’t enough:

### ✅ Make Laravel stateless

* Sessions in Redis
* Files in S3 (not local disk)
* No local cache

### ✅ Add a load balancer

* Nginx / AWS ALB / Cloudflare
* Round-robin traffic to multiple PHP servers

Laravel works **out of the box** with this setup.

---

## 8️⃣ Database Scaling (next stage)

Only when needed:

* Read replicas
* Separate write/read connections
* Slow query logging

Laravel supports this natively:

```php
'mysql' => [
  'read' => [...],
  'write' => [...],
]
```

---

## 9️⃣ Observability (don’t skip this)

Add early:

* Laravel Telescope (staging only)
* Logs → Loki / ELK
* Metrics → Prometheus / New Relic

You **can’t scale what you can’t see**.

---

## 10️⃣ What NOT to do initially ❌

Avoid at first:

* Microservices
* Kubernetes
* Event sourcing
* CQRS
* Fancy architectures

Laravel can handle:

* **Thousands of RPS**
* **Large e-commerce**
* **Millions of users**

…with the above setup.

---

## Simple Mental Model

**Stage 1:**

> Cache + PHP-FPM + OPcache + DB indexes

**Stage 2:**

> Redis + Queues + Stateless app

**Stage 3:**

> Load balancer + multiple app servers