package library;

/**
 * library
 * Created by Михаил Силантьев on 04.02.2018.
 */
public class Messages {
    //auth_request$login$password
    //auth_accept$nick$folder
    //auth_denied
    public static final String AUTH_REQUEST="/auth_request"; //отправить логин и пароль
    public static final String AUTH_ACCEPT="/auth_accept"; // авторизация прошла успешно
    public static final String AUTH_DENIED="/auth_denied"; // авторизация не прошла

    public static String getAuthRequest(String login,String pass){
        return AUTH_REQUEST+DELIMITER+login+DELIMITER+pass;
    }
    public static String getAuthAccept(String nickname,int userID){
        return AUTH_ACCEPT+DELIMITER+nickname+DELIMITER+userID;
    }
    public static String getAuthDenied(){
        return AUTH_DENIED;
    }

    //auth_newperson$logig$pass$nickname
    //auth_newperson_ok&nickname&folder
    //auth_newperson_off
    //auth_newperson_error
    public static final String AUTH_NEWPERSON="/auth_newperson";
    public static final String AUTH_NEWPERSON_OK="/auth_newperson_ok";
    public static final String AUTH_NEWPERSON_OFF="/auth_newperson_off";
    public static final String AUTH_NEWPERSON_ERROR="/auth_newperson_error";

    public static String getAuthNewperson(String login,String pass,String nickname) {
        return AUTH_NEWPERSON+DELIMITER+login+DELIMITER+pass+DELIMITER+nickname;
    }
    public static String getAuthNewpersonOk(String nickname,String folder) {
        return AUTH_NEWPERSON_OK+DELIMITER+nickname+DELIMITER+folder;
    }
    public static String getAuthNewpersonOff() {
        return AUTH_NEWPERSON_OFF;
    }
    public static String getAuthNewpersonError() {
        return AUTH_NEWPERSON_ERROR;
    }

    public static final String DELETE_FILE="/delete_file"; //удалить файл
    public static final String DELETE_FILE_OK="/delete_file_ok";
    public static final String DELETE_FILE_ERROR="/delete_file_error";
    public static String getDeleteFile(int userID,String nameFile){
        return DELETE_FILE+DELIMITER+userID+DELIMITER+nameFile;
    }
    ///message_error$value
    ///delete_file$userID$nameFile
    ///get_list_files$userID

    //клиент передает файл на сервер
        //клиент отправляет fileMessage
        //сервер читает файл if obj==fileMessage,
        //если файл прочитан
            //fileClient_save_on
            //запрос на новый список
        //fileClient_save_off
            //выдача сообщения об ошибке

    //клиент запрашавает файл с сервера
        //клиент отправляет команеду get_file&nameFile
        //сервер отправляет fileMessage серверу
        //клиент получив его сохраняет на диске

    public static final String DELIMITER="#"; //разделитель

    public static final String MESSAGE_ERROR="/message_error"; //ошибка в сообщении
    public static final String GET_LIST_FILES ="/get_list_files"; //вернуть файлы

    public static final String FILECLIENT_SAVE_ON="/fileClient_save_on";
    public static final String FILECLIENT_SAVE_OFF="/fileClient_save_off";

    public static final String GET_FILE="/get_file";



    public static String getMessageError(String value){
        return MESSAGE_ERROR+DELIMITER+value;
    }

    public static String getListFiles(int userID){
        return GET_LIST_FILES +DELIMITER+userID;
    }

}
