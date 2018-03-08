package server;

import library.Messages;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Created by Михаил Силантьев on 04.02.2018.
 */
public class SqlClient {
    private static Connection connection=null;
    private static Statement statement;
    private static PreparedStatement ps;

    synchronized static void connect(){
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:src/main/resources/dropBoxDB.db");
            statement=connection.createStatement();

        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    synchronized static void disconect(){
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    synchronized static String getNick(String login,String pass){
        String request="SELECT nickname FROM users WHERE login='"+login+"' and password='"+pass+"'";
        try(ResultSet resultSet=statement.executeQuery(request)) {
            if(resultSet.next()) {
                return resultSet.getString("nickname").toString();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
    synchronized static String getUserID(String nickname){
        String request="SELECT ID FROM users WHERE nickname='"+nickname+"';";
        try(ResultSet resultSet=statement.executeQuery(request)) {
            if(resultSet.next()) {
                return resultSet.getString("ID").toString();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
    synchronized static boolean deleteFile(int userID,String nameFile){
        //String request="DELETE FROM files WHERE userID = 16 AND name = '2.txt';";
        String request="DELETE FROM files WHERE userID="+userID+" and name='"+nameFile+"';";
        try{
            connection.setAutoCommit(true);
            return !statement.execute(request);
        } catch (SQLException e) {
            return false;
        }
    }

    synchronized static List getFiles(int userID) {
        String request="SELECT * FROM files WHERE userID="+userID+";";
        List files=new ArrayList();
        try(ResultSet resultSet=statement.executeQuery(request)) {
            while (resultSet.next()) {
                files.add(resultSet.getString("name"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if(files.size()>0) return files;
        else return files;
    }
    synchronized static boolean setFile(String nameFile,double length,int userID) throws SQLException {
        connection.setAutoCommit(false);
        ps = connection.prepareStatement("INSERT INTO files( name, fileSize, userID) VALUES (?,?,?);");
            ps.setString(1, nameFile);
            ps.setDouble(2, length);
            ps.setInt(3, userID);
            ps.executeUpdate();
        connection.commit();
        if(ps!=null){
            return true;
        } else
            return false;
    }

    synchronized static boolean findFile(String nameFile,int userID) {
        String request="SELECT * FROM files WHERE userID="+userID+" and name='"+nameFile+"';";
        try(ResultSet resultSet=statement.executeQuery(request)) {
            while (resultSet.next()) {
                return true;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    synchronized static int updateFile(String nameFile,double length, int userID) throws SQLException {
        connection.setAutoCommit(false);
        ps = connection.prepareStatement("UPDATE files SET fileSize=? WHERE userID=? and name=?");
        ps.setDouble(1, length);
        ps.setInt(2, userID);
        ps.setString(3,nameFile);
        ps.executeUpdate();
        connection.commit();
        if (ps != null) {
            return 1;
        } else return 0;
    }

    synchronized static boolean setNewPerson(String login, String pass,String nickname) {
        //устанавливаем нового пользователя
        try {
            connection.setAutoCommit(false);
            ps = connection.prepareStatement("INSERT INTO users( login, password, nickname) VALUES (?,?,?);");
            ps.setString(1, login);
            ps.setString(2, pass);
            ps.setString(3, nickname);
            ps.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            return false;
        }
        return ps!=null;

    }

    synchronized static boolean getLoginAndNickname(String login,String nickname) {
        String request="SELECT * FROM users WHERE login='"+login+"' OR nickname='"+nickname+"';";
        try(ResultSet resultSet=statement.executeQuery(request)) {
            if(resultSet.next()){
                return true;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}
