package client;

import network.SocketThread;
import network.SocketThreadListener;
import server.ClientThread;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

/**
 * client
 * Created by Михаил Силантьев on 02.02.2018.
 */
public class Client implements SocketThreadListener {
    private ClientThread clientThread = null;
    private ClientListener listener;
    private File file;

    public ClientListener getListener() {
        return listener;
    }

    public ClientThread getClientThread() {
        return clientThread;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public Client(ClientListener listener) {
        this.listener = listener;
    }

    public void start(String textIP, int port, int timeout) {
        if (clientThread != null && clientThread.isAlive()) {
            putLog("socketThread был запущен ранее, сначала остановите его!");
        } else
            try {
                clientThread = new ClientThread(this, new ArrayList<>(), "Client", new Socket(textIP, port));
            } catch (IOException e) {
                listener.onError(Thread.currentThread(), e);
            }
    }

    public void stop() {
        if (clientThread != null && clientThread.isAlive()) {
            clientThread.close();
        }
    }

    public void putLog(String msg) {
        listener.onMessageServerLog(this, msg);
    }

    @Override
    public synchronized void onStartSocketThread(SocketThread socketThread, Socket socket) {
        putLog("SocketThread стартовал");
    }

    @Override
    public synchronized void onSocketIsReady(SocketThread socketThread, Socket socket) {
        putLog("SocketThread готов к работе.");
        //todo отправляем серверу авторизацию
        listener.getAuthRequest((ClientThread) socketThread);

    }

    @Override
    public synchronized void onException(SocketThread socketThread, Exception e) {
        listener.onError(Thread.currentThread(), e);
        //e.printStackTrace();
    }

    @Override
    public synchronized void onStopSocketThread(SocketThread socketThread) {
        putLog("SocketThread остановился");
        socketThread.close();
        //todo возможно открыть еще раз окно авторизации
        listener.onInitLoginClientGUI();
    }

    @Override
    public synchronized void onReadObject(SocketThread socketThread, Object obj) {
        HadleMessageClient.readObject(this,socketThread,obj);
    }

    public synchronized void putLogAndJOptionPanel(String msg, String title, int keyOptionPanel) {
        putLog(msg);
        listener.onSetJOptionPane(msg, title, keyOptionPanel);
        clientThread.getListFiles();
    }

}
