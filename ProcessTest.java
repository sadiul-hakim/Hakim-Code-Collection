class ProcessTest{

  public void test(){

    try {
            // Create a ProcessBuilder to execute a system command
            ProcessBuilder builder = new ProcessBuilder("ping", "-c", "3", "google.com");

            // Start the process
            Process process = builder.start();

            // Get the process output (stdout)
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // Wait for the process to finish and check the exit value
            int exitCode = process.waitFor();
            System.out.println("Exit code: " + exitCode);

        } catch (Exception e) {
            e.printStackTrace();
        }
    
  }
  
}
