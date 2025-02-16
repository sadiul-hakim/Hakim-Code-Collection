// No need of a library 

class PlayWithAudio{

  public static Logger LOGGER = Logger.getLogger(Microphone.class.getName());

    public static void capture() {

        // Set up the format for the audio
        AudioFormat format = new AudioFormat(16_000, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        // Open the microphone line
        try (TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(info)) {
            microphone.open(format);
            microphone.start();

            // Set up an output file
            File outputFile = new File("F:\\audio\\output.wav");
            AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;

            // Create an audio stream from the microphone
            AudioInputStream audioStream = new AudioInputStream(microphone);

            // Start a new thread to capture the audio and write it to the file
            Thread.ofVirtual().name("#Audio_Writer_Thread").start(() -> {
                try {
                    System.out.println("Recording audio...");
                    AudioSystem.write(audioStream, fileType, outputFile);
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, "Error during recording", ex);
                }
            });

            Thread.sleep(10000); // Record for 10 seconds

            // Stop recording
            microphone.stop();
            microphone.close();
            System.out.println("Recording stopped.");
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
        }
    }
}
