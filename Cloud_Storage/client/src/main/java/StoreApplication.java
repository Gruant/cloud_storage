import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class StoreApplication  extends Application {


    public void start(Stage primaryStage) throws Exception {
        Parent parent = FXMLLoader.load(getClass().getResource("storeLayout.fxml"));
        primaryStage.setTitle("Cloud Storage");
        primaryStage.show();
    }
}
