package br.com.ppd.tuplespace.view;



import br.com.ppd.tuplespace.models.Environment;
import br.com.ppd.tuplespace.models.Message;
import br.com.ppd.tuplespace.models.User;
import br.com.ppd.tuplespace.service.ChatService;
import br.com.ppd.tuplespace.service.JavaSpaceService;
import br.com.ppd.tuplespace.service.ServiceUnavailable;
import br.com.ppd.tuplespace.util.ObservableStringBufferBinding;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.StageStyle;

import java.util.List;

import static br.com.ppd.tuplespace.util.Util.println;

public class ChatView extends VBox {
    private TextField usernameField;
    private TextField envField;
    private Button loginButton;
    private Button envCreateButton;
    private Button envMoveButton;
    private Button envListAll;
    private Button envListSelected;
    private TextArea chatArea;
    private TextField messageField;
    private Label envLabel;
    private ObservableStringBufferBinding chatTextBinding;
    private JavaSpaceService service;
    private ChatService chatService;
    private User loggedUser;

    public ChatView() {
        this.service = JavaSpaceService.getInstance();
        this.setSpacing(5);
        initElements();
        this.getChildren().addAll(envLabel, usernameField, loginButton, envField, envCreateButton, envMoveButton, envListAll, envListSelected, chatArea, messageField);
    }

    private void onLogin() {
        try {
            loggedUser = this.service.searchUser(new User(this.usernameField.getText()));
            if (loggedUser != null) {
                startChatService();
                envLabel.setText("Você está na sala: " + loggedUser.environment.name);
            }
        } catch (ServiceUnavailable serviceUnavailable) {
            serviceUnavailable.printStackTrace();
        }
    }

    private void onCreateEnv(){
        try {
            this.service.send(new Environment(this.usernameField.getText()));
            println(String.format("Sala %s adicionada", this.usernameField.getText()));
        } catch (ServiceUnavailable serviceUnavailable) {
            println("Could not execute command. Error: " + serviceUnavailable.getMessage());
        }
    }

    private void onMoveEnv(){
        try {
            User user = (User) this.service.take(new User(this.usernameField.getText()));
            if (user != null) {
                Environment env = (Environment) this.service.read(new Environment(this.envField.getText()));
                if (env != null) {
                    user.environment = env;
                    this.service.send(user);
                    println(String.format("Usuário %s movido a sala %s", this.usernameField.getText(), this.envField.getText()));
                } else {
                    println(String.format("Sala %s não localizada", this.envField.getText()));
                }
            } else {
                println(String.format("Usuário %s não encontrado", this.usernameField.getText()));
            }
        } catch (ServiceUnavailable serviceUnavailable) {
            println("Could not execute command. Error: " + serviceUnavailable.getMessage());
        }
    }

    private void onListAllEnv(){
        try {
            List<Environment> envList = this.service.listEnvironments();

            StringBuilder sb = new StringBuilder();
            sb.append("Salas:\n");
            for(Environment env:envList){
                sb.append("   - " + env.name + "\n");
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.initStyle(StageStyle.UTILITY);
            alert.setTitle("Salas");
            alert.setHeaderText(null);
            alert.setContentText(sb.toString());
            alert.showAndWait();
        } catch (ServiceUnavailable serviceUnavailable) {
            serviceUnavailable.printStackTrace();
        }
    }

    private void onListSelectedEnv(){
        try {
            List<User> userList = this.service.listUsersByEnv(this.envField.getText());

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Usuários da sala: %s\n", this.envField.getText()));
            for(User user:userList){
                sb.append("   - " + user.name + "\n");
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.initStyle(StageStyle.UTILITY);
            alert.setTitle(String.format("Usuários da sala: %s", this.envField.getText()));
            alert.setHeaderText(null);
            alert.setContentText(sb.toString());
            alert.showAndWait();

        } catch (ServiceUnavailable serviceUnavailable) {
            serviceUnavailable.printStackTrace();
        }
    }

    private void startChatService() {
        this.chatService = new ChatService(this.loggedUser, this.chatTextBinding);
        new Thread(this.chatService).start();
    }

    private void initElements() {
        envLabel = new Label("Digite seu nome:");

        usernameField = new TextField();
        usernameField.setPrefWidth(345);
        usernameField.setPromptText("Nome de usuário");

        envField = new TextField();
        envField.setPrefWidth(345);
        envField.setPromptText("Nome da sala");

        loginButton = new Button("Login");
        loginButton.setOnMouseClicked(v -> onLogin());
        envCreateButton = new Button("Criar sala");
        envCreateButton.setOnMouseClicked(x -> onCreateEnv());
        envMoveButton = new Button("Mudar de sala");
        envMoveButton.setOnMouseClicked(z -> onMoveEnv());
        envListAll = new Button("Listar todas as salas");
        envListAll.setOnMouseClicked(z -> onListAllEnv());
        envListSelected = new Button("Listar sala selecionada");
        envListSelected.setOnMouseClicked(z -> onListSelectedEnv());

        chatTextBinding = new ObservableStringBufferBinding();
        chatArea = new TextArea();
        chatArea.setPrefHeight(400);
        chatArea.textProperty().bind(this.chatTextBinding);

        messageField = new TextField();
        messageField.setPrefHeight(30);
        messageField.setPromptText("Escreva sua msg");
        messageField.setOnKeyTyped(v -> keyTyped(v.getCharacter()));
    }

    private void keyTyped(String code) {
        if (code.equals("\r")) {
            String message = this.messageField.getText();
            Message msg = new Message();
            msg.content = message;
            msg.sender = loggedUser.name;
            msg.env = loggedUser.environment.name;
            try {
                this.service.send(msg);
                this.messageField.clear();
            } catch (ServiceUnavailable serviceUnavailable) {
                serviceUnavailable.printStackTrace();
            }
        }
    }

    public void closeServices() {
        if (chatService != null) this.chatService.stop();
    }
}
