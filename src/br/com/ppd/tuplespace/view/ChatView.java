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

import java.io.*;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

import static br.com.ppd.tuplespace.util.Util.println;
import static java.lang.Float.parseFloat;

public class ChatView extends VBox {
    private TextField usernameField;
    private TextField xField;
    private TextField yField;
    private TextField envField;
    private TextField ipField;
    private TextField portField;
    private TextField pathField;

    private Button addButton;
    private Button userListAll;
    private Button envListAll;
    private Button envListSelected;

    private Button movUser;
    private Button uploadButton;

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
        this.getChildren().addAll(usernameField, xField, yField, ipField, portField, pathField, addButton, movUser, uploadButton ,envField,envListSelected,envListAll,userListAll);
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
                sb.append("   - " + user.name + " ( Ambiente: " + user.environment.name + " | Latitude: " + user.latitude + " | Longitude: " + user.longitude + " | IP: " + user.IP + " | Porta: " + user.Porta + " | Path: " + user.PATH + " )\n");
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
                        Integer porta = null;
                        if(!this.portField.getText().isEmpty()){
                            porta = Integer.parseInt(this.portField.getText());
                        }
                        this.service.send(new User(this.usernameField.getText(), env, parseFloat(this.xField.getText()), parseFloat(this.yField.getText()), this.ipField.getText(), porta,this.pathField.getText()));
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
                        Integer porta = null;
                        if(!this.portField.getText().isEmpty()){
                            porta = Integer.parseInt(this.portField.getText());
                        }
                        this.service.send(new User(this.usernameField.getText(), env, parseFloat(this.xField.getText()), parseFloat(this.yField.getText()), this.ipField.getText(), porta,this.pathField.getText()));
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
                        Integer porta = null;
                        if(!this.portField.getText().isEmpty()){
                            porta = Integer.parseInt(this.portField.getText());
                        }
                        this.service.send(new User(this.usernameField.getText(), env, parseFloat(this.xField.getText()), parseFloat(this.yField.getText()), this.ipField.getText(), porta,this.pathField.getText()));
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
                        Integer porta = null;
                        if(!this.portField.getText().isEmpty()){
                            porta = Integer.parseInt(this.portField.getText());
                        }
                        this.service.send(new User(this.usernameField.getText(), env, parseFloat(this.xField.getText()), parseFloat(this.yField.getText()), this.ipField.getText(), porta,this.pathField.getText()));
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

    private void onUpload() throws IOException {
        // Abre uma conexão socket com o endereço e a porta especificados
        String ip = this.ipField.getText();
        Integer port = Integer.parseInt(this.portField.getText());
        Socket socket = new Socket(ip, port);

        File arquivoSeraTransferido = new File(this.pathField.getText());
        byte[] arrayDeBytesDoArquivo = new byte[(int) arquivoSeraTransferido.length()];

        FileInputStream fis = new FileInputStream(arquivoSeraTransferido);
        BufferedInputStream bis = new BufferedInputStream(fis);
        DataInputStream dis = new DataInputStream(bis);
        dis.readFully(arrayDeBytesDoArquivo, 0, arrayDeBytesDoArquivo.length);

        OutputStream os = socket.getOutputStream();

        // Enviando o NOME e o TAMANHO do arquivo para o server
        DataOutputStream dos = new DataOutputStream(os);
        dos.writeUTF(arquivoSeraTransferido.getName());
        dos.writeLong(arrayDeBytesDoArquivo.length);
        dos.write(arrayDeBytesDoArquivo, 0, arrayDeBytesDoArquivo.length);
        dos.flush();

        // Enviando o ARQUIVO para o server
        os.write(arrayDeBytesDoArquivo, 0, arrayDeBytesDoArquivo.length);
        os.flush();

        // Fecha as conexões com o socket
        os.close();
        dos.close();
        socket.close();
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

    private void initElements() {
        usernameField = new TextField();
        usernameField.setPromptText("Dispositivo");
        usernameField.setLayoutX(0);
        usernameField.setLayoutY(0);
        usernameField.setTranslateX(0);
        usernameField.setTranslateY(20);
        usernameField.setMaxWidth(100);

        xField = new TextField();
        xField.setPromptText("Latitude");
        xField.setLayoutX(0);
        xField.setLayoutY(0);
        xField.setTranslateX(110);
        xField.setTranslateY(-10);
        xField.setMaxWidth(70);

        yField = new TextField();
        yField.setPromptText("Longitude");
        yField.setLayoutX(0);
        yField.setLayoutY(0);
        yField.setTranslateX(190);
        yField.setTranslateY(-40);
        yField.setMaxWidth(70);

        ipField = new TextField();
        ipField.setPromptText("IP");
        ipField.setLayoutX(0);
        ipField.setLayoutY(0);
        ipField.setTranslateX(270);
        ipField.setTranslateY(-70);
        ipField.setMaxWidth(70);

        portField = new TextField();
        portField.setPromptText("Porta");
        portField.setLayoutX(0);
        portField.setLayoutY(0);
        portField.setTranslateX(350);
        portField.setTranslateY(-100);
        portField.setMaxWidth(70);

        pathField = new TextField();
        pathField.setPromptText("Path arquivo");
        pathField.setLayoutX(0);
        pathField.setLayoutY(0);
        pathField.setTranslateX(430);
        pathField.setTranslateY(-130);
        pathField.setMaxWidth(70);

        addButton = new Button("Adicionar");
        addButton.setOnMouseClicked(x -> onAddUser());
        addButton.setLayoutX(0);
        addButton.setLayoutY(0);
        addButton.setTranslateX(0);
        addButton.setTranslateY(-120);

        movUser = new Button("Mover");
        movUser.setOnMouseClicked(x -> onMoveUser());
        movUser.setLayoutX(0);
        movUser.setLayoutY(0);
        movUser.setTranslateX(80);
        movUser.setTranslateY(-150);

        uploadButton = new Button("Enviar arquivo");
        uploadButton.setOnMouseClicked(x -> {
            try {
                onUpload();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        uploadButton.setLayoutX(0);
        uploadButton.setLayoutY(0);
        uploadButton.setTranslateX(140);
        uploadButton.setTranslateY(-180);

        envField = new TextField();
        envField.setPromptText("Ambiente");
        envField.setLayoutX(0);
        envField.setLayoutY(0);
        envField.setTranslateX(0);
        envField.setTranslateY(-170);
        envField.setMaxWidth(70);

        envListSelected = new Button("Listar dispositivos do ambiente");
        envListSelected.setOnMouseClicked(x -> onListSelectedEnv());
        envListSelected.setLayoutX(0);
        envListSelected.setLayoutY(0);
        envListSelected.setTranslateX(0);
        envListSelected.setTranslateY(-160);

        envListAll = new Button("Listar Ambientes");
        envListAll.setOnMouseClicked(x -> onListAllEnv());
        envListAll.setTranslateX(200);
        envListAll.setTranslateY(-190);

        userListAll = new Button("Listar Dispositivos");
        userListAll.setOnMouseClicked(x -> onListAllUsers());
        userListAll.setTranslateX(320);
        userListAll.setTranslateY(-220);
    }

    public void closeServices() {
        if (chatService != null) this.chatService.stop();
    }
}
