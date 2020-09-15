package br.com.ppd.tuplespace.view;



import br.com.ppd.tuplespace.models.Environment;
import br.com.ppd.tuplespace.models.Message;
import br.com.ppd.tuplespace.models.User;
import br.com.ppd.tuplespace.service.ChatService;
import br.com.ppd.tuplespace.service.JavaSpaceService;
import br.com.ppd.tuplespace.service.ServiceUnavailable;
import br.com.ppd.tuplespace.util.ObservableStringBufferBinding;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.StageStyle;

import java.util.LinkedList;
import java.util.List;

import static br.com.ppd.tuplespace.util.Util.println;
import static java.lang.Float.parseFloat;

public class ChatView extends VBox {
    private TextField usernameField;
    private TextField xField;
    private TextField yField;
    private TextField envField;

    private Button addButton;
    private Button userListAll;
    private Button envListAll;
    private Button envListSelected;

    private Button movUser;

    private Button loginButton;
    private Button envCreateButton;
    private Button envMoveButton;


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
        this.getChildren().addAll(usernameField, xField, yField, addButton, movUser ,envField,envListSelected,envListAll,userListAll);
        //this.getChildren().addAll(envLabel, usernameField, loginButton, envField, envCreateButton, envMoveButton, envListAll, envListSelected, chatArea, messageField);
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
            sb.append("Ambientes:\n");
            for(Environment env:envList){
                sb.append("   - " + env.name + " ( Latitude: " + env.latitude + " | Longitude: " + env.longitude + ")\n");
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.initStyle(StageStyle.UTILITY);
            alert.setTitle("Ambientes");
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
            sb.append(String.format("Dispositivos do ambiente: %s\n", this.envField.getText()));
            for(User user:userList){
                sb.append("   - " + user.name + " ( Ambiente: " + user.environment.name + " | Latitude: " + user.latitude + " | Longitude: " + user.longitude + " )\n");
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.initStyle(StageStyle.UTILITY);
            alert.setTitle(String.format("Dispositivos do ambiente: %s", this.envField.getText()));
            alert.setHeaderText(null);
            alert.setContentText(sb.toString());
            alert.showAndWait();

        } catch (ServiceUnavailable serviceUnavailable) {
            serviceUnavailable.printStackTrace();
        }
    }

    private void onListAllUsers(){
        try {
            List<User> userList = this.service.listAllUsers();

            StringBuilder sb = new StringBuilder();
            sb.append("Dispositivos:\n");
            for(User user:userList){
                sb.append("   - " + user.name + " ( Ambiente: " + user.environment.name + " | Latitude: " + user.latitude + " | Longitude: " + user.longitude + " )\n");
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.initStyle(StageStyle.UTILITY);
            alert.setTitle("Dispositivos");
            alert.setHeaderText(null);
            alert.setContentText(sb.toString());
            alert.showAndWait();
        } catch (ServiceUnavailable serviceUnavailable) {
            serviceUnavailable.printStackTrace();
        }
    }

    private void onAddUser(){
        try {
            StringBuilder sb = new StringBuilder();
            boolean existe = false;

            List<User> listUser = this.service.listAllUsers();
            for (User item: listUser) {
                if (item.name.equals(this.usernameField.getText())){
                    existe = true;
                }
            }
            if(existe == true){
                sb.append(String.format("\nDispositivo %s já está registrado!\n", this.usernameField.getText()));
            }
            else{
                List<User> lista = new LinkedList<User>();
                for (User item: listUser) {
                    double distance = Math.sqrt(Math.pow(item.latitude - parseFloat(this.xField.getText()), 2) + Math.pow(item.longitude - parseFloat(this.yField.getText()), 2));
                    println(String.format("Distancia entre o dispositivo %s -> %s = %.2fm", this.usernameField.getText(), item.name, distance));
                    sb.append(String.format("Distancia entre o dispositivo %s -> %s = %.2fm \n", this.usernameField.getText(), item.name, distance));
                    if (distance <= 10){
                        lista.add(item);
                    }
                }

                if(!lista.isEmpty()){ // Se tiver alguem a 10m ou menos:
                    // Encontra o ambiente do primeiro dispositivo
                    Environment env = lista.get(0).environment;

                    // Cria dispositivo com o ambiente encontrado
                    User user = new User();
                    user.name = this.usernameField.getText();
                    user.latitude = parseFloat(this.xField.getText());
                    user.longitude = parseFloat(this.yField.getText());
                    if (this.service.searchUser(user) == null) {
                        user.environment = env;
                        this.service.send(new User(this.usernameField.getText(), env, parseFloat(this.xField.getText()), parseFloat(this.yField.getText())));
                        println(String.format("Dispositivo %s adicionado ao ambiente %s!", this.usernameField.getText(), env.name));
                        sb.append(String.format("Dispositivo %s adicionado ao ambiente %s!\n", this.usernameField.getText(), env.name));
                    } else {
                        println(String.format("Dispositivo %s já está em outro ambiente!", this.usernameField.getText()));
                        sb.append(String.format("Dispositivo %s já está em outro ambiente!\n", this.usernameField.getText()));
                    }
                }
                else{ // Caso não tenha ninguem:
                    // Cria ambiente
                    String envName = "";
                    Environment envAux = null;
                    do {
                        envName = getAlphaNumericString(2);
                        envAux = this.service.findEnvironment(envName);
                    } while(envAux != null);

                    Float envLatitude = parseFloat(this.xField.getText());
                    Float envLongitude = parseFloat(this.yField.getText());
                    try {
                        this.service.send(new Environment(envName, envLatitude, envLongitude));
                        println(String.format("Ambiente %s adicionado", envName));
                        sb.append(String.format("\nAmbiente %s adicionado\n", envName));
                    } catch (ServiceUnavailable serviceUnavailable) {
                        println("Could not execute command. Error: " + serviceUnavailable.getMessage());
                        sb.append("\nCould not execute command. Error: \n" + serviceUnavailable.getMessage());
                    }

                    // Cria dispositivo
                    Environment env = this.service.findEnvironment(envName);
                    if (env == null) throw new IllegalArgumentException(String.format("Ambiente %s não encontrado.", envName));

                    User user = new User();
                    user.name = this.usernameField.getText();
                    user.latitude = parseFloat(this.xField.getText());
                    user.longitude = parseFloat(this.yField.getText());
                    if (this.service.searchUser(user) == null) {
                        user.environment = env;
                        this.service.send(new User(this.usernameField.getText(), env, parseFloat(this.xField.getText()), parseFloat(this.yField.getText())));
                        println(String.format("Usuário %s adicionado ao ambiente %s!", this.usernameField.getText(), envName));
                        sb.append(String.format("\nUsuário %s adicionado ao ambiente %s!\n", this.usernameField.getText(), envName));
                    } else {
                        println(String.format("Usuário %s já está em outro ambiente!", this.usernameField.getText()));
                        sb.append(String.format("\nUsuário %s já está em outro ambiente!\n", this.usernameField.getText()));
                    }
                }
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.initStyle(StageStyle.UTILITY);
            alert.setTitle("Adicionando: " + this.usernameField.getText());
            alert.setHeaderText(null);
            alert.setContentText(sb.toString());
            alert.showAndWait();
        } catch (ServiceUnavailable serviceUnavailable) {
            serviceUnavailable.printStackTrace();
        }
    }

    private void onMoveUser(){
        try {
            User user = (User) this.service.take(new User(this.usernameField.getText()));
            // Procura o user selecionado
            // Faz um take
            // Se encontra o user
            StringBuilder sb = new StringBuilder();

            if (user != null) {
                // A partir da nova posicao calcula se tem alguem a 10m ou menos
                List<User> listUser = this.service.listAllUsers();
                List<User> lista = new LinkedList<User>();
                for (User item: listUser) {
                    double distance = Math.sqrt(Math.pow(item.latitude - parseFloat(this.xField.getText()), 2) + Math.pow(item.longitude - parseFloat(this.yField.getText()), 2));
                    println(String.format("Distancia entre o dispositivo %s -> %s = %.2fm", this.usernameField.getText(), item.name, distance));
                    sb.append(String.format("Distancia entre o dispositivo %s -> %s = %.2fm\n", this.usernameField.getText(), item.name, distance));
                    if (distance <= 10){
                        lista.add(item);
                    }
                }

                // Se tiver alguem perto cria um novo User com o ambiente do primeiro encontrado
                // Se n tiver ninguem perto, cria novo ambiente
                if(!lista.isEmpty()){ // Se tiver alguem a 10m ou menos:
                    // Encontra o ambiente do dispositivo mais proximo
                    Environment env = lista.get(0).environment;

                    // Cria dispositivo com o ambiente encontrado
                    //User user = new User();
                    //user.name = args[2];
                    user.latitude = parseFloat(this.xField.getText());
                    user.longitude = parseFloat(this.yField.getText());
                    if (this.service.searchUser(user) == null) {
                        user.environment = env;
                        this.service.send(new User(this.usernameField.getText(), env, parseFloat(this.xField.getText()), parseFloat(this.yField.getText())));
                        println(String.format("Dispositivo %s adicionado ao ambiente %s!", this.usernameField.getText(), env.name));
                        sb.append(String.format("\nDispositivo %s adicionado ao ambiente %s!\n", this.usernameField.getText(), env.name));
                    } else {
                        println(String.format("Dispositivo %s já está em outro ambiente!", this.usernameField.getText()));
                        sb.append(String.format("\nDispositivo %s já está em outro ambiente!\n", this.usernameField.getText()));
                    }
                }
                else{ // Caso não tenha ninguem:
                    // Cria ambiente
                    String envName = "";
                    Environment envAux = null;
                    do {
                        envName = getAlphaNumericString(2);
                        envAux = this.service.findEnvironment(envName);
                    } while(envAux != null);
                    Float envLatitude = parseFloat(this.xField.getText());
                    Float envLongitude = parseFloat(this.yField.getText());
                    try {
                        this.service.send(new Environment(envName, envLatitude, envLongitude));
                        println(String.format("Ambiente %s adicionado", envName));
                        sb.append(String.format("\nAmbiente %s adicionado\n", envName));
                    } catch (ServiceUnavailable serviceUnavailable) {
                        println("Could not execute command. Error: " + serviceUnavailable.getMessage());
                        sb.append("\nCould not execute command. Error: \n" + serviceUnavailable.getMessage());
                    }

                    // Cria dispositivo
                    Environment env = this.service.findEnvironment(envName);
                    if (env == null) throw new IllegalArgumentException(String.format("Sala %s não encontrada.", envName));

                    //User user = new User();
                    //user.name = args[2];
                    user.latitude = parseFloat(this.xField.getText());
                    user.longitude = parseFloat(this.yField.getText());
                    if (this.service.searchUser(user) == null) {
                        user.environment = env;
                        this.service.send(new User(this.usernameField.getText(), env, parseFloat(this.xField.getText()), parseFloat(this.yField.getText())));
                        println(String.format("Dispositivo %s adicionado ao ambiente %s!", this.usernameField.getText(), envName));
                        sb.append(String.format("\nDispositivo %s adicionado ao ambiente %s!\n", this.usernameField.getText(), envName));
                    } else {
                        println(String.format("Dispositivo %s já está em outro ambiente!", this.usernameField.getText()));
                        sb.append(String.format("\nDispositivo %s já está em outro ambiente!\n", this.usernameField.getText()));
                    }
                }


            } else {
                println(String.format("Não foi possivel encontrar o dispositivo %s", this.usernameField.getText()));
                sb.append(String.format("\nNão foi possivel encontrar o dispositivo %s\n", this.usernameField.getText()));
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.initStyle(StageStyle.UTILITY);
            alert.setTitle("Movendo: " + this.usernameField.getText());
            alert.setHeaderText(null);
            alert.setContentText(sb.toString());
            alert.showAndWait();
        } catch (ServiceUnavailable serviceUnavailable) {
            println("Could not execute command. Error: " + serviceUnavailable.getMessage());
        }
    }

    static String getAlphaNumericString(int n)
    {

        // chose a Character random from this String
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";

        // create StringBuffer size of AlphaNumericString
        StringBuilder sb = new StringBuilder(n);

        for (int i = 0; i < n; i++) {

            // generate a random number between
            // 0 to AlphaNumericString variable length
            int index
                    = (int)(AlphaNumericString.length()
                    * Math.random());

            // add Character one by one in end of sb
            sb.append(AlphaNumericString
                    .charAt(index));
        }

        return sb.toString();
    }

    private void startChatService() {
        this.chatService = new ChatService(this.loggedUser, this.chatTextBinding);
        new Thread(this.chatService).start();
    }

    private void initElements() {
        usernameField = new TextField();
        usernameField.setPromptText("Dispositivo");
        usernameField.setLayoutX(0);
        usernameField.setLayoutY(0);
        usernameField.setTranslateX(0);
        usernameField.setTranslateY(20);
        usernameField.setMaxWidth(100);
        //usernameField.setPrefHeight(0);

        xField = new TextField();
        xField.setPromptText("Latitude");
        xField.setLayoutX(0);
        xField.setLayoutY(0);
        xField.setTranslateX(110);
        xField.setTranslateY(-10);
        xField.setMaxWidth(70);
        //xField.setPrefHeight(0);

        yField = new TextField();
        yField.setPromptText("Longitude");
        yField.setLayoutX(0);
        yField.setLayoutY(0);
        yField.setTranslateX(190);
        yField.setTranslateY(-40);
        yField.setMaxWidth(70);

        addButton = new Button("Adicionar");
        addButton.setOnMouseClicked(x -> onAddUser());
        addButton.setLayoutX(0);
        addButton.setLayoutY(0);
        addButton.setTranslateX(0);
        addButton.setTranslateY(-30);

        movUser = new Button("Mover");
        movUser.setOnMouseClicked(x -> onMoveUser());
        movUser.setLayoutX(0);
        movUser.setLayoutY(0);
        movUser.setTranslateX(80);
        movUser.setTranslateY(-60);

        envField = new TextField();
        envField.setPromptText("Ambiente");
        envField.setLayoutX(0);
        envField.setLayoutY(0);
        envField.setTranslateX(0);
        envField.setTranslateY(-20);
        envField.setMaxWidth(70);

        envListSelected = new Button("Listar dispositivos do ambiente");
        envListSelected.setOnMouseClicked(x -> onListSelectedEnv());
        envListSelected.setLayoutX(0);
        envListSelected.setLayoutY(0);
        envListSelected.setTranslateX(0);
        envListSelected.setTranslateY(-10);

        envListAll = new Button("Listar Ambientes");
        envListAll.setOnMouseClicked(x -> onListAllEnv());
        envListAll.setTranslateX(200);
        envListAll.setTranslateY(-40);

        userListAll = new Button("Listar Dispositivos");
        userListAll.setOnMouseClicked(x -> onListAllUsers());
        userListAll.setTranslateX(320);
        userListAll.setTranslateY(-70);
        /*
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
        envListAll.setTranslateX(100);
        //envListAll.setTranslateY(100);

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

         */
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
