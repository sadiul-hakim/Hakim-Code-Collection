class FileWatcher{
  private static void watch() {
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            Path path = Path.of("C:\\Users\\Sadiul Hakim\\OneDrive\\Desktop\\");
            path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);

            System.out.println("Watching directory for changes...");
            while (true) {
                // Retrieve and process events
                WatchKey key;
                try {
                    key = watchService.take(); // Blocks until events are available
                } catch (InterruptedException ex) {
                    System.out.println("Watch service interrupted");
                    return;
                }

                // Process the events
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    Path filename = (Path) event.context();
                    System.out.println(kind.name() + ": " + filename);
                }

                // Reset the key to receive further events
                boolean valid = key.reset();
                if (!valid) {
                    System.out.println("WatchKey no longer valid");
                    break;
                }
            }
        } catch (IOException ignore) {
        }
    }
}
