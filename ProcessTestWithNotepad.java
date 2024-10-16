import java.io.IOException;
import java.util.Optional;

void main() throws IOException, InterruptedException {
        // Start a new process (e.g., notepad.exe)
        Process process = new ProcessBuilder("notepad.exe").start();
        
        // Get the ProcessHandle for the started process
        ProcessHandle handle = process.toHandle();
        
        // Get the PID (Process ID)
        long pid = handle.pid();
        System.out.println("Process ID: " + pid);

        // Get additional information about the process
        ProcessHandle.Info processInfo = handle.info();
        Optional<String[]> arguments = processInfo.arguments();
        Optional<String> command = processInfo.command();
        Optional<java.time.Instant> startTime = processInfo.startInstant();

        System.out.println("Process Command: " + command.orElse("Unknown"));
        startTime.ifPresent(time -> System.out.println("Start Time: " + time));

        // Wait for the process to exit
        handle.onExit().thenRun(() -> System.out.println("Process exited."));

        // Simulate some delay before killing the process
        Thread.sleep(5000);

        // Destroy the process if it’s still alive
        if (handle.isAlive()) {
            handle.destroy();
            System.out.println("Process destroyed.");
        }
    }
