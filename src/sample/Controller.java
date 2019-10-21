package sample;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Controller {
    @FXML
    private Button start_btn;
    @FXML
    private ImageView current_frame;

    private VideoCapture capture = new VideoCapture();

    private ScheduledExecutorService timer;
    private boolean camera_active = false;
    private int CameraId = 0;


    @FXML
    protected void startCamera(ActionEvent event) {
        if (!this.camera_active){
            this.capture.open(CameraId);

            if (this.capture.isOpened()){
                this.camera_active = true;

                Runnable frameGrabber = new Runnable() {
                    @Override
                    public void run() {
                        Mat frame = grabFrame();
                        Image imageToShow = Utils.mat2Image(frame);

                        updateImageView(current_frame, imageToShow);

                    }
                };

                this.timer = Executors.newSingleThreadScheduledExecutor();
                this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

                this.start_btn.setText("Stop Camera");
            } else {
                System.err.println("Impossible camera connection");
            }
        } else {
            this.camera_active = false;
            this.start_btn.setText("Start Camera");

            this.stopAcquisition();
        }
    }

    private Mat grabFrame() {
        Mat frame = new Mat();

        if (this.capture.isOpened()) {
            try {
                this.capture.read(frame);

                if (frame.empty()) {
                    Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
                }
            } catch (Exception e) {
                System.err.println("Exception during the image elaboration: " + e.toString());
            }
        }

        return frame;
    }

    private void stopAcquisition() {
        if (this.timer != null && !this.timer.isShutdown()) {
            try {
                this.timer.shutdown();
                this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                System.err.println("Exception in stopping the frame capture, trying to release camera now");
            }
        }

        if (this.capture.isOpened()) {
            this.capture.release();
        }
    }

    private void updateImageView(ImageView view, Image image) {
        Utils.onFXThread(view.imageProperty(), image);
    }

    protected void setClosed() {
        this.stopAcquisition();
    }
}
