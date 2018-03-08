package network;

import java.io.*;

/**
 * network
 * Created by Михаил Силантьев on 13.02.2018.
 */
public class FileMessage implements Serializable {
    private String name;
    private double lenght;
    private byte[] data;

    public String getName() {
        return name;
    }

    public double getLenght() {
        return lenght;
    }

    public FileMessage(File file){
        this.name=file.getName();
        this.lenght=(double)file.length();
        this.data=new byte[(int)lenght];
        setData(file);
    }

    private synchronized void setData(File file){
        //запись файла в массив байт
        try(FileInputStream f = new FileInputStream(file)) {
            f.read(data);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public synchronized boolean getData(String fullName){
        try (FileOutputStream f = new FileOutputStream(fullName)) {
            f.write(data);
            f.flush();
            return true;
        } catch (IOException e){
            e.printStackTrace();
        }
        return false;
    }

}
