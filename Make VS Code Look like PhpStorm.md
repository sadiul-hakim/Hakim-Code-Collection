
## 1️⃣ Theme: IntelliJ / PhpStorm style

### Best choices

Install **one** of these:

#### ✅ **IntelliJ IDEA Theme**

* Extension: **IntelliJ IDEA Theme**
* Very close to PhpStorm default colors

OR

#### ✅ **JetBrains Darcula Theme**

* Extension: **JetBrains Darcula Theme**
* If you like dark mode (most JetBrains users do)

📌 After install:

```
Ctrl + K → Ctrl + T → select theme
```

---

## 2️⃣ Font: JetBrains Mono (important)

PhpStorm uses this by default.

### Install font

Download:
👉 [https://www.jetbrains.com/lp/mono/](https://www.jetbrains.com/lp/mono/)

### VS Code settings

Open **settings.json** and add:

```json
{
  "editor.fontFamily": "JetBrains Mono, Consolas, 'Courier New', monospace",
  "editor.fontLigatures": true,
  "editor.fontSize": 14,
  "editor.lineHeight": 22
}
```

---

## 3️⃣ Icons: JetBrains-like file tree

### Best option

✅ **Material Icon Theme**

Settings:

```
Ctrl + Shift + P → File Icon Theme → Material Icon Theme
```

(Optional: enable folders in settings)

```json
"material-icon-theme.folders.theme": "classic"
```

---

## 4️⃣ Layout tweaks (very IntelliJ-like)

### Recommended settings

```json
{
  "workbench.editor.showTabs": "multiple",
  "editor.tabSizing": "shrink",
  "editor.minimap.enabled": false,
  "breadcrumbs.enabled": true,
  "editor.renderLineHighlight": "all",
  "editor.cursorSmoothCaretAnimation": "on",
  "editor.smoothScrolling": true
}
```

---

## 5️⃣ Keybindings like IntelliJ (optional but powerful)

### Install

✅ **IntelliJ IDEA Keybindings**

This gives:

* `Ctrl + Alt + L` → Reformat code
* `Shift + Shift` → Search everywhere
* `Ctrl + N` → Go to class
* `Ctrl + B` → Go to definition

---

## 6️⃣ PHP / Laravel intelligence (VERY important)

To feel like PhpStorm, install these:

### Core PHP

✅ **PHP Intelephense**

Settings:

```json
"intelephense.environment.phpVersion": "8.2"
```

---

### Laravel

✅ **Laravel Extra Intellisense**
✅ **Laravel Blade Snippets**
✅ **Laravel Blade Formatter**

---

## 7️⃣ Code formatting like PhpStorm

Install:

* **PHP CS Fixer**
* **Prettier** (for JS/CSS/HTML)

Then add:

```json
"editor.formatOnSave": true
```

---

## 8️⃣ Optional: VS Code UI tweaks

For *extra* JetBrains feel:

```json
{
  "workbench.activityBar.location": "left",
  "workbench.sideBar.location": "left",
  "workbench.statusBar.visible": true
}
```

---

## 🔥 Result

With this setup:

* Colors ✔
* Fonts ✔
* Keybindings ✔
* PHP/Laravel intelligence ✔

You’ll get **~85–90% PhpStorm experience**, but **faster + lighter**.