package server;

import library.Messages;
import network.*;

import java.io.File;
import java.io.Serializable;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * ru.silantyevmn.dropbox.server.core
 * Created by Михаил Силантьев on 02.02.2018.
 */
public class Server implements ServerSocketThreadListener, SocketThreadListener {
    private ServerSocketThread serverSocketThread = null;
    private final DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss ");
    private ServerListener listener;
    private List<ClientThread> clients;
    private AuthService authService;

    public AuthService getAuthService() {
        return authService;
    }

    public Server(ServerListener listener) {
        this.listener = listener;
        clients = new ArrayList<>();
        authService = new AuthService();
    }

    public void start(String name, int port, int timeout) {
        //старт серверсокет
        if (serverSocketThread != null && serverSocketThread.isAlive()) {
            putLog("ServerSocketThread был запущен ранее, сначала остановите его!");
        } else {
            serverSocketThread = new ServerSocketThread(this, name, port, timeout);
            authService.start();

            //putLog(authService.getNickByLoginPass("admin","admin"));
//            SqlClient.connect();
//            putLog("Nick " + SqlClient.getNick("nik1", "123"));
        }
    }

    public synchronized void putLog(String msg) {
        msg = dateFormat.format(System.currentTimeMillis()) +
                Thread.currentThread().getName() + ":" + msg;
        listener.onMessageServerLog(this, msg);
        System.out.println(msg);
    }

    public void stop() {
        //стоп серверсокет
        if (serverSocketThread != null && serverSocketThread.isAlive()) {
            serverSocketThread.interrupt();
            authService.stop();
        } else {
            putLog("ServerSocketThread не был запущен!");
        }
    }

    public synchronized void onStartServerSocketThread(ServerSocketThread thread) {
        putLog("ServerSocketThread стартовал");
    }

    public synchronized void onStopServerSocketThread(ServerSocketThread thread) {
        putLog("ServerSocketThread остановился");
    }

    public synchronized void onExceptionServerSocketThread(ServerSocketThread thread, Exception e) {
        e.printStackTrace();
    }

    public synchronized void onTimeoutException(ServerSocketThread thread, Exception e) {
        //заглушка
    }

    public synchronized void onCreateServerSocket(ServerSocketThread thread) {
        //создался Serversocket
        putLog("Новый serverSocket создался");
    }

    public synchronized void onCreateSocket(ServerSocketThread thread, Socket socket) {
        //создался новый Socket
        ClientThread clientThread = new ClientThread(this, new ArrayList<>(), "Server", socket);
        putLog("Новый Socket создался");
    }

    /*
    * Методы SocketThread
     */
    @Override
    public synchronized void onStartSocketThread(SocketThread socketThread, Socket socket) {
        putLog("SocketThread стартовал");
        //добавляем клиента
        clients.add((ClientThread) socketThread);
    }

    @Override
    public synchronized void onSocketIsReady(SocketThread socketThread, Socket socket) {
        putLog("SocketThread готов к работе.");
    }


    @Override
    public synchronized void onException(SocketThread socketThread, Exception e) {
        e.printStackTrace();
    }

    @Override
    public synchronized void onStopSocketThread(SocketThread socketThread) {
        putLog("SocketThread остановился");
        //удаляем клиента
        ClientThread clientThread = (ClientThread) socketThread;
        putLog("Клиента " + clientThread.getNickname() + " удалили из коллекции");
        clients.remove(clientThread);
    }

    @Override
    public synchronized void onReadObject(SocketThread socketThread, Object obj) {
        HandleMessageServer.readObject(this,socketThread,obj);
    }
}
