package server;

import library.Messages;
import network.FileMessage;
import network.SocketThread;

import java.io.File;
import java.io.Serializable;
import java.util.List;

/**
 * server
 * Created by Михаил Силантьев on 10.03.2018.
 */
public class HandleMessageServer {
    public synchronized static void readObject(Server server, SocketThread socketThread, Object obj) {
        ClientThread clientThread = (ClientThread) socketThread;
        if (obj instanceof FileMessage) {
            FileMessage fileMessage = (FileMessage) obj;
            FileProcessor.setNewFile(server, clientThread, fileMessage);
        }

        if (obj instanceof String) {
            String question​ = (String) obj;
            textMessage(server,clientThread, question​);
        }
    }

    private static void textMessage(Server server,ClientThread clientThread, String value) {
        server.putLog("Сервер получил сообщение :" + value);
        String[] arr = value.split(Messages.DELIMITER);
        String typeMessage = arr[0];
        switch (typeMessage) {
            case Messages.AUTH_REQUEST: {
                String nickname = server.getAuthService().getNickByLoginPass(arr[1], arr[2]);
                if (nickname != null) {
                    //int userID= Integer.parseInt(SqlClient.getUserID(nickname));
                    int userID = server.getAuthService().getUserID(nickname);
                    clientThread.authAccept(nickname, userID);
                    server.putLog("Клиенту отправлено сообщение :" + Messages.getAuthAccept(nickname, userID));
                } else {
                    clientThread.authDenied();
                    server.putLog("Клиенту отправлено сообщение :" + Messages.getAuthDenied());
                }
                break;
            }
            case Messages.AUTH_NEWPERSON: {
                //auth_newperson$logig$pass$nickname
                //создаем нового пользователя на SQL
                String nickname = arr[3];
                String msg = server.getAuthService().setNewPerson(arr[1], arr[2], nickname);
                int userID = server.getAuthService().getUserID(nickname);
                clientThread.authNewAccept(nickname, userID);
                clientThread.sendMessage(msg);
                server.putLog("Клиенту отправлено сообщение :" + msg);
                break;

            }
            case Messages.GET_LIST_FILES: {
                ///get_list_files$userID
                int userID = Integer.parseInt(arr[1]);
                List filePath = SqlClient.getFiles(userID);
                clientThread.sendMessage(filePath);
                server.putLog("Клиенту " + clientThread.getNickname() + " отправлен список файлов, size=" + filePath.size());
                break;
            }
            case Messages.GET_FILE: {
                //возвращаем клиенту файл
                int userID = clientThread.getFolder();
                String nameFile = arr[1];
                File file = FileProcessor.getFile(userID, nameFile);
                FileMessage fileMessage = new FileMessage(file);
                clientThread.sendMessage(fileMessage);
                server.putLog(nameFile + " отправлен клиенту ");
                break;
            }
            case Messages.DELETE_FILE: {
                ///delete_file$userID$nameFile
                int userID = Integer.parseInt(arr[1]);
                String nameFile = arr[2];
                //удаляем файл из SQL
                if (SqlClient.deleteFile(userID, nameFile)) {
                    server.putLog(nameFile + "- удален из SQL");
                } else {
                    server.putLog(nameFile + "- ошибка при удалении SQL");
                    clientThread.sendMessage(Messages.DELETE_FILE_ERROR);
                    return;
                }
                //удаляем файл с сервера(диска)
                if (FileProcessor.deleteFile(userID, nameFile)) {
                    server.putLog(nameFile + "- удален из сервера");
                } else {
                    server.putLog(nameFile + "- ошибка при удалении с сервера");
                    clientThread.sendMessage(Messages.DELETE_FILE_ERROR);
                    return;
                }
                clientThread.sendMessage(Messages.DELETE_FILE_OK);
                break;
            }
        }
    }
}
