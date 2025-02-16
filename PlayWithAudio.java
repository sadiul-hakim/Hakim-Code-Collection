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


import javax.sound.sampled.*;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

class AudioRecorder {

    private static final Logger LOGGER = Logger.getLogger(AudioRecorder.class.getName());
    private TargetDataLine microphone;
    private AudioInputStream audioStream;
    private boolean isRecording = false;
    private File outputFile;

    private JButton stopButton;
    private JButton startButton;
    private JTextField fileNameField;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AudioRecorder().createGUI());
    }

    public void createGUI() {
        // Set up the main JFrame
        JFrame frame = new JFrame("Audio Recorder");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);

        // Create components
        JLabel fileLabel = new JLabel("Enter file name (without extension): ");
        fileNameField = new JTextField(20);
        startButton = new JButton("Start");
        stopButton = new JButton("Stop");
        JLabel statusLabel = new JLabel("Status: Idle");

        // Layout setup
        JPanel panel = new JPanel();
        panel.add(fileLabel);
        panel.add(fileNameField);
        panel.add(startButton);
        panel.add(stopButton);
        panel.add(statusLabel);
        frame.add(panel);

        // Action listeners for buttons
        startButton.addActionListener(e -> startRecording(fileNameField, statusLabel));
        stopButton.addActionListener(e -> stopRecording(statusLabel));

        // Disable the stop button initially
        stopButton.setEnabled(false);

        // Show the frame
        frame.setVisible(true);
    }

    private void startRecording(JTextField fileNameField, JLabel statusLabel) {
        String fileName = fileNameField.getText();
        if (fileName.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please enter a valid file name.");
            return;
        }

        outputFile = new File(fileName + ".wav");

        // Set up the format for the audio
        AudioFormat format = new AudioFormat(16_000, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        try {
            // Open the microphone line
            microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);
            microphone.start();

            // Create an audio stream from the microphone
            audioStream = new AudioInputStream(microphone);

            // Start a new thread to capture the audio and write it to the file
            Thread recordingThread = new Thread(() -> {
                try {
                    System.out.println("Recording audio...");
                    statusLabel.setText("Status: Recording...");
                    AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, outputFile);
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, "Error during recording", ex);
                }
            });

            // Start recording in the background
            recordingThread.start();

            // Disable start button and enable stop button
            stopButton.setEnabled(true);
            startButton.setEnabled(false);
            fileNameField.setEnabled(false);
            statusLabel.setText("Status: Recording...");
            isRecording = true;
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            statusLabel.setText("Status: Error");
            JOptionPane.showMessageDialog(null, "Error starting recording.");
        }
    }

    private void stopRecording(JLabel statusLabel) {
        if (!isRecording) {
            JOptionPane.showMessageDialog(null, "Recording is not in progress.");
            return;
        }

        // Stop recording
        microphone.stop();
        microphone.close();
        statusLabel.setText("Status: Recording stopped.");

        // Enable start button and disable stop button
        isRecording = false;
        stopButton.setEnabled(false);
        startButton.setEnabled(true);
        fileNameField.setEnabled(true);
        fileNameField.setText("");
    }
}


class AudioPlayer {

    private Clip audioClip;
    private AudioInputStream audioStream;
    private boolean isPlaying = false;
    private boolean isPaused = false;

    private JButton playButton;
    private JButton pauseButton;
    private JButton resumeButton;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AudioPlayer().createGUI());
    }

    public void createGUI() {

        // Set up the main JFrame
        JFrame frame = new JFrame("Audio Player");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(400, 200);

        // Create components
        playButton = new JButton("Play");
        pauseButton = new JButton("Pause");
        resumeButton = new JButton("Resume");
        JButton fileChooserButton = new JButton("Choose Audio File");
        JLabel statusLabel = new JLabel("Status: Idle");

        // Layout setup
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        panel.add(fileChooserButton);
        panel.add(playButton);
        panel.add(pauseButton);
        panel.add(resumeButton);
        panel.add(statusLabel);
        frame.add(panel);

        // Action listeners for buttons
        fileChooserButton.addActionListener(e -> chooseFile(statusLabel));
        playButton.addActionListener(e -> playAudio(statusLabel));
        pauseButton.addActionListener(e -> pauseAudio(statusLabel));
        resumeButton.addActionListener(e -> resumeAudio(statusLabel));

        // Disable pause and resume initially
        pauseButton.setEnabled(false);
        resumeButton.setEnabled(false);

        // Show the frame
        frame.setVisible(true);
    }

    private void chooseFile(JLabel statusLabel) {
        // Open file chooser for audio files
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Audio Files", "wav", "mp3", "ogg", "aiff"));

        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {

                // Load the selected audio file
                audioStream = AudioSystem.getAudioInputStream(selectedFile);
                audioClip = AudioSystem.getClip();
                audioClip.open(audioStream);
                statusLabel.setText("Status: Ready to Play");
            } catch (Exception ex) {
                statusLabel.setText("Error: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private void playAudio(JLabel statusLabel) {
        if (audioClip == null) {
            statusLabel.setText("Error: No audio file loaded");
            return;
        }

        if (!isPlaying) {
            audioClip.start();
            isPlaying = true;
            statusLabel.setText("Status: Playing");

            playButton.setEnabled(false);
            pauseButton.setEnabled(true);
        }
    }

    private void pauseAudio(JLabel statusLabel) {
        if (isPlaying && !isPaused) {
            audioClip.stop();
            isPaused = true;
            statusLabel.setText("Status: Paused");

            pauseButton.setEnabled(false);
            resumeButton.setEnabled(true);
        }
    }

    private void resumeAudio(JLabel statusLabel) {
        if (isPlaying && isPaused) {
            audioClip.start();
            isPaused = false;
            statusLabel.setText("Status: Resumed");

            pauseButton.setEnabled(true);
            resumeButton.setEnabled(false);
        }
    }
}

