package server;

import library.Messages;
import network.*;

import java.io.File;
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
        ClientThread clientThread = (ClientThread) socketThread;
        if (obj instanceof FileMessage) {
            FileMessage fileMessage = (FileMessage) obj;
            FileProcessor.setNewFile(this, clientThread, fileMessage);
        }

        if (obj instanceof String) {
            String question​ = (String) obj;
            handleMessage(clientThread, question​);
        }
    }

    private synchronized void handleMessage(ClientThread clientThread, String value) {
        putLog("Сервер получил сообщение :" + value);
        String[] arr = value.split(Messages.DELIMITER);
        String typeMessage = arr[0];
        switch (typeMessage) {
            case Messages.AUTH_REQUEST: {
                String nickname = authService.getNickByLoginPass(arr[1], arr[2]);
                if (nickname != null) {
                    //int userID= Integer.parseInt(SqlClient.getUserID(nickname));
                    int userID = authService.getUserID(nickname);
                    clientThread.authAccept(nickname, userID);
                    putLog("Клиенту отправлено сообщение :" + Messages.getAuthAccept(nickname, userID));
                } else {
                    clientThread.authDenied();
                    putLog("Клиенту отправлено сообщение :" + Messages.getAuthDenied());
                }
                break;
            }
            case Messages.AUTH_NEWPERSON: {
                //auth_newperson$logig$pass$nickname
                //создаем нового пользователя на SQL
                String nickname = arr[3];
                String msg = authService.setNewPerson(arr[1], arr[2], nickname);
                int userID = authService.getUserID(nickname);
                clientThread.authNewAccept(nickname, userID);
                clientThread.sendMessage(msg);
                putLog("Клиенту отправлено сообщение :" + msg);
                break;

            }
            case Messages.GET_LIST_FILES: {
                ///get_list_files$userID
                int userID = Integer.parseInt(arr[1]);
                List filePath = SqlClient.getFiles(userID);
                clientThread.sendMessage(filePath);
                putLog("Клиенту " + clientThread.getNickname() + " отправлен список файлов, size=" + filePath.size());
                break;
            }
            case Messages.GET_FILE: {
                //возвращаем клиенту файл
                int userID = clientThread.getFolder();
                String nameFile = arr[1];
                File file = FileProcessor.getFile(userID, nameFile);
                FileMessage fileMessage = new FileMessage(file);
                clientThread.sendMessage(fileMessage);
                putLog(nameFile + " отправлен клиенту ");
                break;
            }
            case Messages.DELETE_FILE: {
                ///delete_file$userID$nameFile
                int userID = Integer.parseInt(arr[1]);
                String nameFile = arr[2];
                //удаляем файл из SQL
                if (SqlClient.deleteFile(userID, nameFile)) {
                    putLog(nameFile + "- удален из SQL");
                } else {
                    putLog(nameFile + "- ошибка при удалении SQL");
                    clientThread.sendMessage(Messages.DELETE_FILE_ERROR);
                    return;
                }
                //удаляем файл с сервера(диска)
                if (FileProcessor.deleteFile(userID, nameFile)) {
                    putLog(nameFile + "- удален из сервера");
                } else {
                    putLog(nameFile + "- ошибка при удалении с сервера");
                    clientThread.sendMessage(Messages.DELETE_FILE_ERROR);
                    return;
                }
                clientThread.sendMessage(Messages.DELETE_FILE_OK);
                break;
            }
        }
    }
}
