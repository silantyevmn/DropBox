package client;

import library.Messages;
import network.FileMessage;
import network.SocketThread;
import network.SocketThreadListener;
import server.ClientThread;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * client
 * Created by Михаил Силантьев on 02.02.2018.
 */
public class Client implements SocketThreadListener {
    private ClientThread clientThread = null;
    private ClientListener listener;
    private File file;

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

    private void putLog(String msg) {
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
        if (obj instanceof List) {
            List files = (List) obj;
            putLog("Получен список файлов-" + files.size());
            //обновляем список на форме
            listener.onUpdateList(this, files);

        }
        if (obj instanceof FileMessage) {
            FileMessage fileMessage = (FileMessage) obj;
            //сохранить переданный файл
            String title = "Сохранение файла";
            File newFile=file;
            String fileName=newFile.getName();
            if (fileMessage.getData(newFile.getAbsolutePath())) {
                String msg = fileName + " успешно сохранен на клиенте";
                putLog(msg);
                listener.onSetJOptionPane(msg, title, 1);

            } else {
                String msg = fileName + " -ошибка при сохранении!!!";
                putLog(msg);
                listener.onSetJOptionPane(msg, title, 0);
            }
        }
        if (obj instanceof String) {
            String question​ = (String) obj;
            handleMessage(socketThread, question​);
        }
    }

    private synchronized void handleMessage(SocketThread socketThread, String value) {
        putLog("Клиент получил сообщение :" + value);
        String[] arr = value.split(Messages.DELIMITER);
        String typeMessage = arr[0];
        switch (typeMessage) {
            case Messages.AUTH_ACCEPT: {
                putLog("Авторизация прошла успешно!\nДобро пожаловать: " + arr[1]);
                listener.onWindowTitle(" вход под ником " + arr[1]);
                //запрашиваем файлы на сервере
                ClientThread clientThread = (ClientThread) socketThread;
                clientThread.authAccept(arr[1], Integer.parseInt(arr[2]));
                clientThread.getListFiles();
                //clientThread.sendMessage(Messages.getFiles(clientThread.getNickname()));
                break;
            }
            case Messages.AUTH_NEWPERSON_OK: {
                //auth_newperson_ok&nickname&folder
                putLog("Новый пользователь " + arr[1] + " успешно создан");
                //запрос на авторизацию

                //clientThread.sendMessage(Messages.getAuthRequest(arr[1],arr[2]));
                clientThread.authAccept(arr[1], Integer.parseInt(arr[2]));
                putLog("Авторизация прошла успешно!\nДобро пожаловать: " + arr[1]);
                listener.onWindowTitle(" вход под ником " + arr[1]);
                break;
            }
            case Messages.AUTH_NEWPERSON_OFF: {
                //auth_newperson_off
                putLog("Ошибка! Такой логин/ник уже присутствует!");
                clientThread.close();
                break;
            }
            case Messages.AUTH_NEWPERSON_ERROR: {
                //auth_newperson_error
                putLog("Ошибка, при создании нового пользователя ");
                clientThread.close();
                break;
            }
            case Messages.AUTH_DENIED: {
                putLog("Авторизация не прошла! :" + value);
                putLog("Не верный логин/пароль!");
                break;
            }
            case Messages.DELETE_FILE_OK: {
                String msg = "Файл успешно удален из БД";
                putLogAndJOptionPanel(msg, "Удаление файла", 1);
                break;
            }
            case Messages.DELETE_FILE_ERROR: {
                String msg = "При удалении файла возникла ошибка! Попробуйте еще раз!";
                putLogAndJOptionPanel(msg, "Удаление файла", 0);
                break;
            }
            case Messages.FILECLIENT_SAVE_ON: {
                String msg = "Файл : " + arr[1] + " успешно сохранен на сервер.";
                putLogAndJOptionPanel(msg, "Сохранение файла", 1);
                break;
            }
            case Messages.FILECLIENT_SAVE_OFF: {
                String msg = "При сохранении файла: " + arr[1] + " на сервере произошла ошибка.";
                putLogAndJOptionPanel(msg, "Сохранение файла", 0);
                break;
            }
        }
    }

    private synchronized void putLogAndJOptionPanel(String msg, String title, int keyOptionPanel) {
        putLog(msg);
        listener.onSetJOptionPane(msg, title, keyOptionPanel);
        clientThread.getListFiles();
    }

}
