package br.com.ppd.tuplespace.service;

import br.com.ppd.tuplespace.models.Environment;
import br.com.ppd.tuplespace.models.Message;
import br.com.ppd.tuplespace.models.User;
import br.com.ppd.tuplespace.util.Lookup;
import net.jini.core.entry.Entry;
import net.jini.core.entry.UnusableEntryException;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.TransactionException;
import net.jini.space.JavaSpace;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;

public class JavaSpaceService {
    private static JavaSpaceService INSTANCE = null;

    private JavaSpace space;
    private long lease = 300*1000; // 5 minutos
    private long lease10 = 600*1000; // 10 minutos

    private JavaSpaceService() {
        init();
    }

    private void init() { // Cria um objeto com a instancia do servidor de nomes do Apache
        this.space = (JavaSpace) new Lookup(JavaSpace.class).getService();
    }

    public static JavaSpaceService getInstance() {
        if (INSTANCE == null)
            INSTANCE = new JavaSpaceService();
        return INSTANCE;
    }

    public void send(Entry entry) throws ServiceUnavailable{ // Manda msg para o servidor de nomes
        try {
            this.space.write(entry, null, Lease.FOREVER);
        } catch (RemoteException|TransactionException e) {
            e.printStackTrace();
            throw new ServiceUnavailable(e.getMessage());
        }
    }

    public Entry read(Entry template) throws ServiceUnavailable {
        Entry entry = null;
        try {
            entry = this.space.readIfExists(template, null, lease);
        } catch (RemoteException|
                TransactionException|
                InterruptedException|
                UnusableEntryException e) {
            e.printStackTrace();
            throw new ServiceUnavailable(e.getMessage());
        }
        return entry;
    }

    public Entry take(Entry template) throws ServiceUnavailable { // Tentar ler a msg do servidor de nomes e caso tenha ele remove e retorna o objeto.
        Entry entry = null;
        try {
            entry = this.space.takeIfExists(template, null, lease);
        } catch (RemoteException|
                TransactionException|
                InterruptedException|
                UnusableEntryException e) {
            e.printStackTrace();
            throw new ServiceUnavailable(e.getMessage());
        }
        return entry;
    }

    private void write(List<? extends Entry> listEnv) throws ServiceUnavailable {
        for(Entry env : listEnv) {
            send(env);
        }
    }

    public List<Environment> listEnvironments() throws ServiceUnavailable {
        List<Environment> listEnv = new LinkedList<Environment>();

        Environment env = null;
        Environment template = new Environment();
        do {
            env = (Environment) take(template);
            if (env != null) listEnv.add(env);
        } while(env != null);
        write(listEnv);
        return listEnv;
    }

    public List<User> listUsersByEnv(String env) throws ServiceUnavailable {
        List<User> listEnv = new LinkedList<User>();

        User user = null;
        User template = new User();
        Float x = 1.0F;
        Float y = 2.0F;
        template.environment = new Environment(env);
        do {
            user = (User) take(template);
            if (user != null) listEnv.add(user);
        } while(user != null);
        write(listEnv);
        return listEnv;
    }

    public List<Message> getMessages(User user) throws ServiceUnavailable {
        List<Message> messages = new LinkedList<Message>();

        Message message = null;
        Message template = new Message();
        template.env = user.environment.name;
        do {
            message = (Message) take(template);
            if (message != null) messages.add(message);
        } while(message != null);
        write(messages);
        return messages;
    }

    public Environment findEnvironment(String arg) throws ServiceUnavailable{
        return (Environment) read(new Environment(arg));
    }

    public User searchUser(User user) throws ServiceUnavailable {
        return (User) read(user);
    }


}
