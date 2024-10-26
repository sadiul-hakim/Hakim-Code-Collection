import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.opencv_java;
import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;
import org.opencv.videoio.VideoCapture;

// 1. Install OpenCV 4.7 and put the path (C:\Apps\opencv\build\x64\vc16\bin) in Path variable
// 2. add dependency 
/*

        <dependency>
            <groupId>org.bytedeco</groupId>
            <artifactId>opencv</artifactId>
            <version>4.7.0-1.5.9</version>
        </dependency>

        <!-- OpenCV platform dependencies for native libraries -->
        <dependency>
            <groupId>org.bytedeco</groupId>
            <artifactId>opencv-platform</artifactId>
            <version>4.7.0-1.5.9</version>
        </dependency>

        <!-- https://mvnrepository.com/artifact/org.bytedeco/openblas -->
        <dependency>
            <groupId>org.bytedeco</groupId>
            <artifactId>openblas</artifactId>
            <version>0.3.23-1.5.9</version>
        </dependency>

*/
// 3. Run the code

public class WebCam {
    static {
        Loader.load(opencv_java.class);
    }

    public static void main(String[] args) {
        VideoCapture capture = new VideoCapture(0); // 0 for default camera
        if (!capture.isOpened()) {
            System.out.println("Cannot open webcam");
            return;
        }

        Mat frame = new Mat();
        while (true) {
            if (capture.read(frame)) {
                HighGui.imshow("Webcam Feed", frame);
                if (HighGui.waitKey(1) == 27) break; // Press 'Esc' to exit
            } else {
                System.out.println("Failed to capture frame");
                break;
            }
        }

        capture.release();
        HighGui.destroyAllWindows();
    }
}
