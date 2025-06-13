class SemaphoreTest{
  public static void main(String[] args) {
        Semaphore semaphore = new Semaphore(3); // only 3 threads allowed

        for (int i = 1; i <= 10; i++) {
            int threadId = i;
            new Thread(() -> {
                try {
                    System.out.println("Thread " + threadId + " waiting for permit...");
                    semaphore.acquire();
                    System.out.println("Thread " + threadId + " got permit!");

                    // Critical section
                    Thread.sleep(2000); // simulate work

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("Thread " + threadId + " releasing permit.");
                    semaphore.release();
                }
            }).start();
        }
    }
}

// Best Practise
public class SafeResourcePool {
    private final Semaphore semaphore;
    private final String resourceName;
    
    public SafeResourcePool(String resourceName, int maxConcurrent) {
        this.resourceName = resourceName;
        this.semaphore = new Semaphore(maxConcurrent);
    }
    
    public <T> T useResource(Callable<T> task) throws Exception {
        // Always acquire before try block
        semaphore.acquire();
        try {
            // Virtual thread safely accesses the limited resource
            return task.call();
        } finally {
            // This ALWAYS executes, even if task throws an exception
            semaphore.release();
        }
    }
    
    public int availablePermits() {
        return semaphore.availablePermits();
    }
}
