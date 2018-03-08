package server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * server
 * Created by Михаил Силантьев on 02.02.2018.
 */
public class ServerGUI extends JFrame implements ActionListener,Thread.UncaughtExceptionHandler, ServerListener {
    private final int timeout=200;
    private final int WIDTH=800;
    private final int HEIGHT=600;
    private JTextField textIP=new JTextField("localhost");
    private JTextField textPort=new JTextField("8189");
    private JButton btnStart=new JButton("Start");
    private JButton btnStop=new JButton("Stop");
    private Server server;
    private JTextArea textLog;
    private JPanel panelUp;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Thread.currentThread().setName("serverGUI");
                new ServerGUI();
            }
        });
    }

    public ServerGUI(){
        server =new Server(this);
        Thread.setDefaultUncaughtExceptionHandler(this);
        setSize(WIDTH,HEIGHT);
        setTitle("DropBox сервер");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        textLog=new JTextArea();
        textLog.setEnabled(false);
        textLog.setLineWrap(true);
        JScrollPane scrollLog=new JScrollPane(textLog);
        add(scrollLog, BorderLayout.CENTER);

        panelUp=new JPanel();
        panelUp.setLayout(new GridLayout(1,5));
        JLabel label=new JLabel("Введите сервер и порт");
        btnStart=new JButton("Start");
        btnStart.addActionListener(this);

        btnStop.addActionListener(this);
        panelUp.add(label);
        panelUp.add(textIP);
        panelUp.add(textPort);
        panelUp.add(btnStart);
        panelUp.add(btnStop);
        add(panelUp,BorderLayout.PAGE_START);

        setVisible(true);

    }

    public void actionPerformed(ActionEvent e) {
        Object obj=e.getSource();
        if(obj==btnStart){
            server.start("ServerSocket",Integer.parseInt(textPort.getText()),timeout);
        }
        else if(obj==btnStop) server.stop();
        else {
            throw new RuntimeException("Unexpected source "+obj);
        }
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();
        StackTraceElement[] stackTraceElements=e.getStackTrace();
        String message;
        if(stackTraceElements.length==0){
            message="Empty StackTrace";
        } else{
            message=e.getClass().getCanonicalName()+": "+
                    e.getMessage()+"\n"+
                    "\t at "+stackTraceElements[0];
        }
        JOptionPane.showMessageDialog(this,message,"Exception",JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void onMessageServerLog(Server server, String msg) {
        textLog.append(msg+"\n");
    }
}
