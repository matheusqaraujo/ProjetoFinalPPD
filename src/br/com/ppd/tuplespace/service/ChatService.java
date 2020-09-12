package br.com.ppd.tuplespace.service;

import br.com.ppd.tuplespace.models.Message;
import br.com.ppd.tuplespace.models.User;
import br.com.ppd.tuplespace.util.ObservableStringBufferBinding;

import java.util.List;

public class ChatService implements Runnable {
    private ObservableStringBufferBinding buffer;
    private JavaSpaceService service;
    private User user;
    private boolean running;
    private List<Message> lastMessages;

    public ChatService(User user, ObservableStringBufferBinding buffer) {
        this.running = true;
        this.user = user;
        this.buffer = buffer;
        this.service = JavaSpaceService.getInstance();
    }

    @Override
    public void run() {
        while(running) {
            try {
                List<Message> messages = this.service.getMessages(user);
                if (lastMessages == null || messages.size() > lastMessages.size()) {
                    this.buffer.append(messages);
                    lastMessages = messages;
                }
            } catch (ServiceUnavailable serviceUnavailable) {
                serviceUnavailable.printStackTrace();
            }
            sleep();
        }
    }

    public void stop() {
        this.running = false;
    }

    private void sleep() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
