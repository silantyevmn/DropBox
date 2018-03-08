package network;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

/**
 * ru.silantyevmn.dropbox.network
 * Created by Михаил Силантьев on 02.02.2018.
 */
public class SocketThread extends Thread {
    private Socket socket;
    private SocketThreadListener listener;
    private List<File> files;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;

    public SocketThread(SocketThreadListener listener, List files, String name, Socket socket) {
        super(name);
        this.files = files;
        this.socket = socket;
        this.listener = listener;
        start();
    }

    @Override
    public void run() {
        try {
            listener.onStartSocketThread(this, socket);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
            Object obj = new Object();
            listener.onSocketIsReady(this, socket);
            while (!isInterrupted()) {
                obj = inputStream.readObject();
                //проверка на приходящие объекты
                listener.onReadObject(this, obj);
            }
        } catch (SocketException e) {
            //listener.onException(this, e);
        } catch (IOException e) {
            listener.onException(this, e);
        } catch (ClassNotFoundException e) {
            listener.onException(this, e);
        } finally {
            listener.onStopSocketThread(this);
            //close();
        }
    }

    public synchronized boolean sendMessage(Object obj) {
        try {
            outputStream.writeObject(obj);
            outputStream.flush();
            return true;
        } catch (IOException e) {
            listener.onException(this, e);
            close();
            return false;
        }
    }

    public synchronized void close() {
        interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            listener.onException(this, e);
        }
    }

}
