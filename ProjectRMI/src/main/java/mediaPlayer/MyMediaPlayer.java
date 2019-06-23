package mediaPlayer;

import java.io.File;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

public class MyMediaPlayer extends Application {

    private String url;

    public MyMediaPlayer(File file) {
    	
    	url = file.toURI().toString();
    	start(new Stage());
    }
    
    @Override
    public void start(Stage primaryStage) {
        
        primaryStage.setTitle("Media Player");
        Group root = new Group();
        Scene scene = new Scene(root, 540, 241);

        // create media player
        Media media = new Media (url);
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setAutoPlay(true);
        
        MediaControl mediaControl = new MediaControl(mediaPlayer);
        scene.setRoot(mediaControl);
        
        primaryStage.setOnCloseRequest(e -> mediaPlayer.stop());

        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.show();
    }
}