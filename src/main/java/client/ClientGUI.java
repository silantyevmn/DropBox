package client;

import library.Messages;
import network.FileMessage;
import server.ClientThread;

import javax.imageio.plugins.jpeg.JPEGHuffmanTable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static javax.swing.GroupLayout.Alignment.BASELINE;
import static javax.swing.GroupLayout.Alignment.LEADING;

/**
 * client
 * Created by Михаил Силантьев on 02.02.2018.
 */
public class ClientGUI extends JFrame implements ActionListener, Thread.UncaughtExceptionHandler, ClientListener {
    private final int timeout = 200;
    private final int WIDTH = 800;
    private final int HEIGHT = 600;
    private final String WINDOW_TITLE = "DropBox клиент";
    private JTextField textIP = new JTextField("localhost");
    private JTextField textPort = new JTextField("8189");
    private JButton btnStart = new JButton("Войти");
    private JButton btnCancel=new JButton("Отмена");
    private JTextField textLogin = new JTextField("nik1");
    private JPasswordField textPass = new JPasswordField("123");
    private JTextField textNick=new JTextField("nickname");
    private JButton btnAddFile = new JButton("addFile");
    private JButton btnRemoveFile = new JButton("removeFile");
    private JButton btnSaveFile = new JButton("saveFile");
    private JList jList;
    private DefaultListModel listModel;
    private JTextArea textLog = new JTextArea();;
    private Client client;
    private JFrame loginGUI;
    private Label lab_pass2,lab_nick;
    private JPasswordField text_pass2;
    private JRadioButton btn_radio_login,btn_radio_newLogin;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Thread.currentThread().setName("clientGUI");
                new ClientGUI();
            }
        });
    }
    private void setNewLogin(boolean flag){
        text_pass2.setVisible(flag);
        lab_pass2.setVisible(flag);
        lab_nick.setVisible(flag);
        textNick.setVisible(flag);
    }

    private void initClientLoginGUI(){
        loginGUI=new JFrame();
        loginGUI.setSize(400,200);
        loginGUI.setTitle(WINDOW_TITLE+" подключение");
        loginGUI.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        loginGUI.setLocationRelativeTo(null);

        JPanel panelUp = new JPanel(new GridLayout(1,4));
        JLabel labIP = new JLabel("Введите адрес сервера");
        JLabel labPort = new JLabel("Введите порт");
        panelUp.add(labIP);
        panelUp.add(textIP);
        panelUp.add(labPort);
        panelUp.add(textPort);
        loginGUI.add(panelUp,BorderLayout.PAGE_START);

        JPanel jPanelLoginCenter=new JPanel(new GridLayout(5,2));
        ButtonGroup group = new ButtonGroup();
        btn_radio_login = new JRadioButton("Войти", true);
        btn_radio_newLogin = new JRadioButton("Новый пользователь", false);
        group.add(btn_radio_login);
        group.add(btn_radio_newLogin);

        jPanelLoginCenter.add(btn_radio_login);
        jPanelLoginCenter.add(btn_radio_newLogin);
        JLabel labLogin = new JLabel("Login");
        JLabel labPass = new JLabel("Password");
        textLogin.setPreferredSize(new Dimension(100,20));
        textPass.setPreferredSize(new Dimension(100,20));
        jPanelLoginCenter.add(labLogin);
        jPanelLoginCenter.add(textLogin);
        jPanelLoginCenter.add(labPass);
        jPanelLoginCenter.add(textPass);

        lab_pass2=new Label("повторите пароль");
        text_pass2=new JPasswordField();
        text_pass2.setPreferredSize(new Dimension(100,20));
        jPanelLoginCenter.add(lab_pass2);
        jPanelLoginCenter.add(text_pass2);

        lab_nick=new Label("Введите ник");
        textNick=new JTextField();
        textNick.setPreferredSize(new Dimension(100,20));
        jPanelLoginCenter.add(lab_nick);
        jPanelLoginCenter.add(textNick);

        btn_radio_login.setEnabled(true);
        setNewLogin(btn_radio_newLogin.isSelected());

        loginGUI.add(jPanelLoginCenter,BorderLayout.CENTER);

        JPanel jPanel_button=new JPanel();
        jPanel_button.add(btnStart);
        jPanel_button.add(btnCancel);
        loginGUI.add(jPanel_button,BorderLayout.PAGE_END);

        loginGUI.setVisible(true);
        btnStart.addActionListener(this);
        btnCancel.addActionListener(this);
        btn_radio_newLogin.addActionListener(this);
        btn_radio_login.addActionListener(this);
    }

    public ClientGUI() {
        client = new Client(this);
        Thread.setDefaultUncaughtExceptionHandler(this);
        initClientLoginGUI();
    }

    private void init(){
        setSize(WIDTH, HEIGHT);
        setTitle(WINDOW_TITLE);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        JPanel panelRight = new JPanel();
        JPanel tempPanel = new JPanel();
        tempPanel.setLayout(new GridLayout(3, 1));
        tempPanel.add(btnAddFile);
        tempPanel.add(btnRemoveFile);
        tempPanel.add(btnSaveFile);
        panelRight.add(tempPanel);

        add(panelRight, BorderLayout.LINE_END);

        JPanel jpCenter = new JPanel(new GridLayout(2, 1));
        listModel = new DefaultListModel();
        jList = new JList(listModel);
        jpCenter.add(jList, BorderLayout.CENTER);

        textLog.setEnabled(false);
        textLog.setLineWrap(true);
        JScrollPane scrollLog = new JScrollPane(textLog);
        jpCenter.add(scrollLog, BorderLayout.PAGE_END);

        add(jpCenter, BorderLayout.CENTER);

        setVisible(true);
        btnAddFile.addActionListener(this);
        btnRemoveFile.addActionListener(this);
        btnSaveFile.addActionListener(this);

    }

    private synchronized void putLog(String msg) {
        textLog.append(msg + "\n");
        System.out.println(msg);
    }


    @Override
    public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();
        StackTraceElement[] stackTraceElements = e.getStackTrace();
        String message;
        if (stackTraceElements.length == 0) {
            message = "Empty StackTrace";
        } else {
            message = e.getClass().getCanonicalName() + ": " +
                    e.getMessage() + "\n" +
                    "\t at " + stackTraceElements[0];
        }
        JOptionPane.showMessageDialog(this, message, "Exception", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();
        if (obj == btnStart) {
            //проверить все поля заполнены правильно
            if(!isErrorFieldAuth()) {
                client.start(textIP.getText(), Integer.parseInt(textPort.getText()), timeout);
            } else client.stop();
        }else if(obj==btn_radio_login) {
            setNewLogin(false);
        }else if(obj==btn_radio_newLogin) {
            setNewLogin(true);
        }
        else if(obj==btnCancel){
            loginGUI.dispose();
            dispose();
        } else if (obj == btnAddFile) {
            //проверка на соединение
            JFileChooser open = new JFileChooser(".");
            open.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int result = open.showDialog(null, "Выбрать файл");
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = open.getSelectedFile();
                //отправить файл на сервер
                FileMessage fileMessage=new FileMessage(file);
                client.getClientThread().sendMessage(fileMessage);
                //client.clientThread.sendMessage(Messages.getFileclientProp(file.getName(), String.valueOf(file.length())));
            }
        } else if (obj == btnRemoveFile) {
            //удаление файла
            int userID = client.getClientThread().getFolder();
            String currentNameFile = jList.getSelectedValue().toString();
            client.getClientThread().sendMessage(Messages.getDeleteFile(userID, currentNameFile));
        } else if (obj == btnSaveFile) {
            //сохрание файла на клиенте диск
            int userID = client.getClientThread().getFolder();
            String currentNameFile = jList.getSelectedValue().toString();
            if(currentNameFile==null){
                onSetJOptionPane("не выбран файл из списка","ошибка выбора",JOptionPane.OK_OPTION);
                return;
            }

            JFileChooser save = new JFileChooser(".");
            save.setDialogTitle("Сохранение файла");
            // Определение режима - только файл
            save.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int result = save.showSaveDialog(this);
            // Если файл выбран, то представим его в сообщении
            if (result == JFileChooser.APPROVE_OPTION) {
                client.setFile(save.getSelectedFile());
                //отправляем команду запроса файла с сервера
                client.getClientThread().sendMessage(Messages.GET_FILE+Messages.DELIMITER+currentNameFile);
            }

        }
    }

    private boolean isErrorFieldAuth() {
        String login=textLogin.getText();
        String nickname=textNick.getText();
        String pass1=textPass.getText();
        String pass2=text_pass2.getText();

        if(btn_radio_newLogin.isSelected()){
            if(!pass1.equals(pass2)){
                putLog("Пароли не совпадают");
                return true;
            }
            if(login.length()<2 || pass1.length()<2){
                putLog("Логин/пароль не может быть менее 2-х символов");
                return true;
            }
            if(nickname.length()<2){
                putLog("Ник не может быть менее 2-х символов");
                return true;
            }

        } else {
            if(login.length()<2 || pass1.length()<2){
                putLog("Логин/пароль не может быть менее 2-х символов");
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized void onMessageServerLog(Client client, String msg) {
        putLog(msg);
    }

    @Override
    public synchronized void onUpdateList(Client client, List files) {
        listModel.clear();
        for (int i = 0; i < files.size(); i++) {
            listModel.addElement(files.get(i));
        }
    }

    @Override
    public void onWindowTitle(String title) {
        loginGUI.setVisible(false);
        loginGUI.dispose();
        init();
        setTitle(WINDOW_TITLE + title);

    }

    @Override
    public void getAuthRequest(ClientThread clientThread) {
        if(btn_radio_newLogin.isSelected()){
            clientThread.sendMessage(Messages.getAuthNewperson(textLogin.getText(),textPass.getText(),textNick.getText()));
            //clientThread.sendMessage(Messages.getAuthNewperson("test1","123","test1"));
        } else {
            clientThread.sendMessage(Messages.getAuthRequest(textLogin.getText(), textPass.getText()));
        }

    }

    @Override
    public void onSetJOptionPane(String msg, String title, int optionPanel) {
        JOptionPane.showMessageDialog(this,msg,title,optionPanel);
    }

    @Override
    public void onError(Thread t,Exception e) {
        uncaughtException(t,e);
    }

    @Override
    public void onInitLoginClientGUI() {
        initClientLoginGUI();
        setVisible(false);
        repaint();
    }


}
