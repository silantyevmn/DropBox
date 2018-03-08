package server;

/**
 * server
 * Created by Михаил Силантьев on 02.02.2018.
 */
public interface ServerListener {
    void onMessageServerLog(Server server, String msg);
}
