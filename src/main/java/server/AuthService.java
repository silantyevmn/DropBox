package server;

import client.Client;
import com.sun.deploy.util.SessionState;
import library.Messages;
import org.apache.commons.compress.archivers.sevenz.CLI;

/**
 * server
 * Created by Михаил Силантьев on 18.02.2018.
 */
public class AuthService {

    public void start() {
        SqlClient.connect();
    }

    public void stop() {
        SqlClient.disconect();
    }

    public String getNickByLoginPass(String login, String pass) {
        return SqlClient.getNick(login, pass);
    }

    public int getUserID(String nickname) {
        return Integer.parseInt(SqlClient.getUserID(nickname));
    }

    public String setNewPerson(String login, String pass, String nickname) {
        //смотрим не занят логин
        if (!SqlClient.getLoginAndNickname(login, nickname)) {
            //заводим нового пользователя на SQL
            if (SqlClient.setNewPerson(login, pass, nickname)) {
                String folder = SqlClient.getUserID(nickname);
                //создаем папку для пользователя
                if (FileProcessor.setNewFolder(folder)) {
                    // отправляем сообщение новый пользователь успешно создался
                    System.out.println("Каталог успешно создан");
                    return Messages.getAuthNewpersonOk(nickname, folder);
                } else {
                    //ошибка при создании нового пользователя
                    System.out.println("Каталог не создан!");
                    return Messages.getAuthNewpersonError();
                }
            }
        }
        // если занят отправляем сообщение логин занят
        return Messages.getAuthNewpersonOff();

    }

    public synchronized static boolean isErrorFieldAuth(Client client,String login, String nickname, String pass1, String pass2, boolean isNewLogin){
        if(isNewLogin){
            if(!pass1.equals(pass2)){
                client.putLog("Пароли не совпадают");
                return true;
            }
            if(login.length()<2 || pass1.length()<2){
                client.putLog("Логин/пароль не может быть менее 2-х символов");
                return true;
            }
            if(nickname.length()<2){
                client.putLog("Ник не может быть менее 2-х символов");
                return true;
            }

        } else {
            if(login.length()<2 || pass1.length()<2){
                client.putLog("Логин/пароль не может быть менее 2-х символов");
                return true;
            }
        }
        return false;
    }

}
