package server;

import library.Messages;

import java.io.File;

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
                String fullName = "src/main/resources/" + folder;
                File dir = new File(fullName);
                if (dir.mkdir()) {
                    System.out.println("Каталог успешно создан");
                }
                // отправляем сообщение новый пользователь успешно создался

                return Messages.getAuthNewpersonOk(nickname, folder);
            } else {
                //ошибка при создании нового пользователя
                return Messages.getAuthNewpersonError();
            }
        } else {
            // если занят отправляем сообщение логин занят
            return Messages.getAuthNewpersonOff();
        }

    }

}
