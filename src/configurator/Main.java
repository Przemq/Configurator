package configurator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import server.Parameters;
import server.ServerRequest;
import server.ServiceType;

public class Main extends Application {

    public static String APP_TITLE = "FIND YOUR WAY - configurator";

    @Override
    public void start(Stage primaryStage) throws Exception{

        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        Scene scene = new Scene(root);
       // scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        primaryStage.setTitle(APP_TITLE);
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
       /*new ServerRequest(ServiceType.GET,new server.Parameters()).setServerRequestListener(new ServerRequest.ServerRequestListener() {
            @Override
            public void onSuccess(String json) {
                System.out.println(json);
            }
        }).start();*/


    }


    public static void main(String[] args) {
        launch(args);
    }
}
