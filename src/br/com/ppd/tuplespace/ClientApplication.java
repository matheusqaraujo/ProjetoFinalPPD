package br.com.ppd.tuplespace;

import br.com.ppd.tuplespace.view.ChatView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("PPD - Espaço de Tuplas");
        ChatView view = new ChatView();
        Scene scene = new Scene(view, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest((v) -> view.closeServices());
        primaryStage.show();
    }
}
