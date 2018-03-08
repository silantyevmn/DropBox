package server;

import library.Messages;
import network.SocketThread;
import network.SocketThreadListener;

import java.net.Socket;
import java.util.List;

/**
 * server
 * Created by Михаил Силантьев on 08.02.2018.
 */
public class ClientThread extends SocketThread {
    private String nickname = "noname";
    private boolean isAutorization;
    private int folder;

    public ClientThread(SocketThreadListener listener, List files, String name, Socket socket) {
        super(listener, files, name, socket);
    }

    public synchronized String getNickname() {
        return nickname;
    }

    public synchronized int getFolder() {
        return folder;
    }

    public synchronized void authAccept(String nickname, int folder) {
        this.isAutorization = true;
        this.nickname = nickname;
        this.folder = folder;
        sendMessage(Messages.getAuthAccept(nickname, folder));
    }

    public synchronized void authNewAccept(String nickname, int folder) {
        this.isAutorization = true;
        this.nickname = nickname;
        this.folder = folder;
    }

    public synchronized void authDenied() {
        isAutorization = false;
        nickname = "noname";
        sendMessage(Messages.getAuthDenied());
        close();
    }

    public synchronized void getListFiles() {
        sendMessage(Messages.getListFiles(folder));
    }


}
