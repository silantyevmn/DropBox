package client;

import library.Messages;
import network.FileMessage;
import network.SocketThread;
import server.ClientThread;

import java.io.File;
import java.util.List;

/**
 * client
 * Created by Михаил Силантьев on 10.03.2018.
 */
public class HadleMessageClient {
    private static ClientListener listener;

    public synchronized static void readObject(Client client, SocketThread socketThread, Object obj) {
        listener = client.getListener();
        //если пришел массив файлов
        if (obj instanceof List) {
            List files = (List) obj;
            client.putLog("Получен список файлов-" + files.size());
            //обновляем список на форме
            listener.onUpdateList(client, files);
        }
        //если пришел файл
        if (obj instanceof FileMessage) {
            FileMessage fileMessage = (FileMessage) obj;
            //сохранить переданный файл
            String title = "Сохранение файла";
            File newFile = client.getFile();
            String fileName = newFile.getName();
            if (fileMessage.getData(newFile.getAbsolutePath())) {
                String msg = fileName + " успешно сохранен на клиенте";
                client.putLog(msg);
                listener.onSetJOptionPane(msg, title, 1);

            } else {
                String msg = fileName + " -ошибка при сохранении!!!";
                client.putLog(msg);
                listener.onSetJOptionPane(msg, title, 0);
            }
        }
        //если пришло текстовое сообщение
        if (obj instanceof String) {
            String question​ = (String) obj;
            textMessage(client, socketThread, question​);
        }
    }

    private synchronized static void textMessage(Client client, SocketThread socketThread, String value) {
        client.putLog("Клиент получил сообщение :" + value);
        ClientThread clientThread = (ClientThread) socketThread;
        String[] arr = value.split(Messages.DELIMITER);
        String typeMessage = arr[0];
        switch (typeMessage) {
            case Messages.AUTH_ACCEPT: {
                client.putLog("Авторизация прошла успешно!\nДобро пожаловать: " + arr[1]);
                listener.onWindowTitle(" вход под ником " + arr[1]);
                //запрашиваем файлы на сервере
                clientThread.authAccept(arr[1], Integer.parseInt(arr[2]));
                clientThread.getListFiles();
                break;
            }
            case Messages.AUTH_NEWPERSON_OK: {
                //auth_newperson_ok&nickname&folder
                client.putLog("Новый пользователь " + arr[1] + " успешно создан");
                //запрос на авторизацию
                clientThread.authAccept(arr[1], Integer.parseInt(arr[2]));
                client.putLog("Авторизация прошла успешно!\nДобро пожаловать: " + arr[1]);
                listener.onWindowTitle(" вход под ником " + arr[1]);
                break;
            }
            case Messages.AUTH_NEWPERSON_OFF: {
                //auth_newperson_off
                client.putLog("Ошибка! Такой логин/ник уже присутствует!");
                clientThread.close();
                break;
            }
            case Messages.AUTH_NEWPERSON_ERROR: {
                //auth_newperson_error
                client.putLog("Ошибка, при создании нового пользователя ");
                clientThread.close();
                break;
            }
            case Messages.AUTH_DENIED: {
                client.putLog("Авторизация не прошла! :" + value);
                client.putLog("Не верный логин/пароль!");
                break;
            }
            case Messages.DELETE_FILE_OK: {
                String msg = "Файл успешно удален из БД";
                client.putLogAndJOptionPanel(msg, "Удаление файла", 1);
                break;
            }
            case Messages.DELETE_FILE_ERROR: {
                String msg = "При удалении файла возникла ошибка! Попробуйте еще раз!";
                client.putLogAndJOptionPanel(msg, "Удаление файла", 0);
                break;
            }
            case Messages.FILECLIENT_SAVE_ON: {
                String msg = "Файл : " + arr[1] + " успешно сохранен на сервер.";
                client.putLogAndJOptionPanel(msg, "Сохранение файла", 1);
                break;
            }
            case Messages.FILECLIENT_SAVE_OFF: {
                String msg = "При сохранении файла: " + arr[1] + " на сервере произошла ошибка.";
                client.putLogAndJOptionPanel(msg, "Сохранение файла", 0);
                break;
            }
        }
    }
}
