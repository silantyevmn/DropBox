package server;

import library.Messages;
import network.FileMessage;

import java.io.File;
import java.sql.SQLException;

/**
 * server
 * Created by Михаил Силантьев on 22.02.2018.
 */
public class FileProcessor {
    private static final String DIR="src/main/resources/";
    synchronized static void setNewFile(Server server, ClientThread clientThread, FileMessage fileMessage) {
        String nameFile = fileMessage.getName();
        double fileLenght = fileMessage.getLenght();
        int userID = clientThread.getFolder();
        //проверить есть такой файл в базе?
        try {
            if (SqlClient.findFile(nameFile, userID)) {
                //если есть обновляем базу
                SqlClient.updateFile(nameFile, fileLenght, userID);
                server.putLog ("Новый файл " + nameFile + " был обновлен в базе SQL");
            } else {
                //если нет, добавляем в базу новый
                SqlClient.setFile(nameFile, fileLenght, userID);
                server.putLog ("Новый файл " + nameFile + " добавлен в базу SQL");
            }
        } catch (SQLException e) {
            server.putLog ("ошибка при записи файла: " + nameFile + " на SQL");
        }

        String fullName=DIR + userID+ File.separator+nameFile;
        //сохранеем файл
        if(fileMessage.getData(fullName)){
            //говорим все ок
            server.putLog("Новый файл "+nameFile+" был успешно сохранен на диск");
            clientThread.sendMessage(Messages.FILECLIENT_SAVE_ON+ Messages.DELIMITER+nameFile);
        } else {
            server.putLog("Возникла ошибка при сохранении файла: " + nameFile);
            clientThread.sendMessage(Messages.FILECLIENT_SAVE_OFF + Messages.DELIMITER + nameFile);
        }
    }
    synchronized static boolean deleteFile(int userID,String nameFile){
        File tempFile = new File(DIR + userID+File.separator+nameFile);
        return tempFile.delete();
    }
    synchronized static File getFile(int userID, String nameFile) {
        return new File(DIR+userID+File.separator+nameFile);
    }
    synchronized static boolean setNewFolder(String folder){
        String fullName = DIR + folder;
        File dir = new File(fullName);
        return dir.mkdir();
    }
}
