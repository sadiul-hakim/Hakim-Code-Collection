# 🐧 Basic WSL Commands Cheat Sheet

## 🚀 WSL Basics

| Command | Description |
|---------|-------------|
| `wsl` | Launch default WSL distro |
| `wsl -l -v` | List all installed distros with version info |
| `wsl --set-default <distro>` | Set default distro (e.g., `Ubuntu`) |
| `wsl -d <distro>` | Run a specific distro (e.g., `wsl -d Ubuntu`) |
| `wsl --install` | Installs WSL with the default distro (WSL 2) |
| `wsl --shutdown` | Gracefully shut down all running distros |

---

## 📂 File System Access

| Command | Description |
|---------|-------------|
| `cd /mnt/c/Users/<yourname>` | Access Windows `C:\` drive from WSL |
| `explorer.exe .` | Open current WSL directory in Windows File Explorer |
| `code .` | Open current folder in VS Code (if installed in PATH) |

---

## 🛠️ Common Linux Commands in WSL

| Command | Description |
|---------|-------------|
| `pwd` | Print current directory |
| `ls` | List files |
| `cd <folder>` | Change directory |
| `mkdir <folder>` | Make a directory |
| `rm <file>` | Delete file |
| `rm -r <folder>` | Delete folder recursively |
| `cp <src> <dest>` | Copy files |
| `mv <src> <dest>` | Move or rename files |
| `touch <file>` | Create a new empty file |
| `nano <file>` | Simple text editor |
| `sudo apt update && sudo apt upgrade` | Update installed packages |
| `sudo apt install <package>` | Install software package |

---

## 🧠 Tips

- Access WSL home from Windows via:
