import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;

public class GUI extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Scene scene = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Window Title");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        launch(args);
    }
}