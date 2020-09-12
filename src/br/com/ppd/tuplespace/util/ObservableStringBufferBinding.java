package br.com.ppd.tuplespace.util;

import br.com.ppd.tuplespace.models.Message;
import javafx.beans.binding.StringBinding;

import java.util.List;

public class ObservableStringBufferBinding extends StringBinding {
    private StringBuffer buffer = new StringBuffer();

    @Override
    protected String computeValue() {
        return this.buffer.toString();
    }

    public void append(List<Message> messages) {
        this.buffer = new StringBuffer();
        for (Message msg : messages) {
            this.buffer.append(msg.sender + ": " + msg.content);
            this.buffer.append("\n");
        }
        invalidate();
    }
}
