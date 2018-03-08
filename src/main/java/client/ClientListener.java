package client;

import server.ClientThread;

import java.io.File;
import java.util.List;

/**
 * client
 * Created by Михаил Силантьев on 03.02.2018.
 */
public interface ClientListener {
    void onMessageServerLog(Client client,String msg);
    void onUpdateList(Client client, List<File> files);
    void onWindowTitle(String title);
    void getAuthRequest(ClientThread clientThread);
    void onSetJOptionPane(String msg,String title,int optionPanel);
    void onError(Thread t,Exception e);
    void onInitLoginClientGUI();
}
