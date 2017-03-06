package ui;

import client.FtpClientDemo;

import javax.swing.*;
import java.awt.*;
import java.awt.List;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Created by renmingxu on 2017/3/4.
 */
public class FtpFrame extends JFrame{
    public JTextField addrField;
    public JTextField userField;
    public JPasswordField passField;
    public DefaultListModel filesList;
    public JList jList;
    public JButton loginBtn;
    public JButton downloadBtn;
    public JButton uploadBtn;
    public JButton upperBtn;
    public JButton openBtn;
    public JButton deleBtn;
    public JButton updateBtn;
    public String host;
    public int port;
    FtpClientDemo ftpClient;

    public FtpFrame() {
        this.setBounds(300, 200, 700, 500);
        this.setResizable(false);
        Font font = new Font("宋体", Font.PLAIN, 14);
        JPanel panel = new JPanel();
        panel.setLayout(null);
        JLabel lb1 = new JLabel("Addr:");
        lb1.setBounds(5,5,40,30);
        lb1.setFont(font);
        panel.add(lb1);
        addrField = new JTextField();
        addrField.setBounds(45, 5, 250, 30);
        addrField.setFont(font);
        panel.add(addrField);
        JLabel lb2 = new JLabel("User:");
        lb2.setBounds(300,5,40,30);
        lb2.setFont(font);
        panel.add(lb2);
        userField = new JTextField();
        userField.setFont(font);
        userField.setBounds(340, 5, 80, 30);
        panel.add(userField);
        JLabel lb3 = new JLabel("Pass:");
        lb3.setBounds(430, 5, 40, 30);
        lb3.setFont(font);
        panel.add(lb3);
        passField = new JPasswordField();
        passField.setFont(font);
        passField.setBounds(470, 5, 80, 30);
        panel.add(passField);
        loginBtn = new JButton("登陆");
        loginBtn.setFont(font);
        loginBtn.setBounds(600, 5, 90, 30);
        loginBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                login();
            }
        });
        panel.add(loginBtn);
        openBtn = new JButton("打开");
        openBtn.setFont(font);
        openBtn.setBounds(600, 50, 90,30);
        openBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                open();
            }
        });
        panel.add(openBtn);
        downloadBtn = new JButton("下载");
        downloadBtn.setFont(font);
        downloadBtn.setBounds(600, 100,90,30);
        downloadBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                download();
            }
        });
        panel.add(downloadBtn);
        upperBtn = new JButton("上一级");
        upperBtn.setFont(font);
        upperBtn.setBounds(600, 150, 90, 30);
        upperBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                upper();
            }
        });
        panel.add(upperBtn);
        deleBtn = new JButton("删除");
        deleBtn.setFont(font);
        deleBtn.setBounds(600, 200, 90, 30);
        deleBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dele();
            }
        });
        panel.add(deleBtn);
        updateBtn = new JButton("刷新");
        updateBtn.setFont(font);
        updateBtn.setBounds(600, 250, 90,30);
        updateBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updatelist();
            }
        });
        panel.add(updateBtn);
        filesList = new DefaultListModel();
        jList = new JList(filesList);
        jList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    open();
                }
            }
        });
        JScrollPane ps = new JScrollPane(jList);
        ps.setBounds(5, 40, 580, 400);
        panel.add(ps);
        drag(ps);
        this.setContentPane(panel);
        this.setVisible(true);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public void drag(JScrollPane panel) {
        new DropTarget(panel, DnDConstants.ACTION_COPY_OR_MOVE, new DropTargetAdapter()
        {
            @Override
            public void drop(DropTargetDropEvent dtde)//重写适配器的drop方法
            {
                try
                {
                    if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor))//如果拖入的文件格式受支持
                    {
                        dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);//接收拖拽来的数据
                        java.util.List<File> list =  (java.util.List<File>) (dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor));
                        String temp="";
                        boolean flag = true;
                        for(File file:list) {
                            temp += file.getAbsolutePath() + ";";
                            if (ftpClient.upload(file.getName(), file.getAbsolutePath())) {
                                System.out.println(file.getAbsoluteFile());
                                temp += "上传成功\n";
                            } else {
                                temp += "上传失败\n";
                            }
                        }
                        JOptionPane.showMessageDialog(null, temp);
                        updatelist();
                        dtde.dropComplete(true);//指示拖拽操作已完成
                    }
                    else
                    {
                        dtde.rejectDrop();//否则拒绝拖拽来的数据
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    public void updatelist() {
        String[] list = ftpClient.list().split("\r\n");
        FtpFrame.this.filesList.removeAllElements();
        for (String l:list) {
            System.out.println(l);
            if (l != null) {
                FtpFrame.this.filesList.addElement(l);
            }
        }
        if (port != 21) {
            this.addrField.setText("ftp://" + host + ":" + port + ftpClient.pwd());
        } else {
            this.addrField.setText("ftp://" + host + ftpClient.pwd());
        }
    }

    public void login() {
        String user = FtpFrame.this.userField.getText();
        String pass = new String(FtpFrame.this.passField.getPassword());
        String addr = FtpFrame.this.addrField.getText();
        if ("ftp://".equals(addr.substring(0,6))) {
            host = addr.split("/")[2].split(":")[0];
            System.out.println("host:" + host);
            port = 21;
            if (addr.split("/")[2].contains(":")) {
                port = Integer.valueOf(addr.split("/")[2].split(":")[1]);
            }
            ftpClient = new FtpClientDemo(host, port, user, pass);
            System.out.println(ftpClient.login());
            updatelist();
            JOptionPane.showMessageDialog(null, "登陆成功");
        }
    }

    public void open(){
        String select = (String) FtpFrame.this.jList.getSelectedValue();
        if (select == null) {
            JOptionPane.showMessageDialog(null, "请选择一个文件夹");
            return;
        }
        if (select.length() <= 1) {
            return;
        }
        if ("d".equals(select.substring(0,1))) {
            String dir =  select.split("( ){1,}")[8];
            System.out.println(dir);
            ftpClient.cwd(dir);
            updatelist();
        } else {
            JOptionPane.showMessageDialog(null, "不能打开一个文件，你可以点击下载");
        }
    }

    public void dele(){
        Object[] selects = FtpFrame.this.jList.getSelectedValues();
        String result = "";
        if (selects.length == 0) {
            JOptionPane.showMessageDialog(null, "请选择一个文件夹或文件");
            return;
        }
        for (Object selecto: selects) {
            String select = (String)selecto;
            if (select.length() <= 1) {
                return;
            }
            String filename = select.substring(select.indexOf(select.split("( ){1,}")[8]));
            System.out.println(filename);
            if (ftpClient.dele(filename)) {
                result += filename + ";删除完成\n";
            } else {
                result += filename + ";删除失败\n";

            }
        }
        JOptionPane.showMessageDialog(null, result);
        updatelist();
    }

    public void download() {
        Object[] selects =  FtpFrame.this.jList.getSelectedValues();
        if (selects.length == 0) {
            JOptionPane.showMessageDialog(null, "请选择一个文件夹或文件");
            return;
        }
        JFileChooser fd = new JFileChooser();
        fd.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fd.showOpenDialog(null);
        File f = fd.getSelectedFile();
        if(f != null){
            String result = "";
            for (Object selecto: selects) {
                String select = (String)selecto;
                String filename = select.substring(select.indexOf(select.split("( ){1,}")[8]));
                if (select.length() <= 1) {
                    return;
                }
                if (ftpClient.download(filename, f.getAbsolutePath() + "/" + filename)) {
                    result += filename + ";下载完成\n";
                } else {
                    result += filename + ";下载失败\n";
                }
            }
            JOptionPane.showMessageDialog(null, result);
        }
        updatelist();
    }

    public void upper() {
        String pwd = ftpClient.pwd();
        if ("/".equals(pwd)) {
            JOptionPane.showMessageDialog(null, "这是根目录，没有上一级");
            return;
        }
        if (pwd.endsWith("/")) {
            pwd = pwd.split("/$")[0];
        }
        pwd = pwd.split("[^/]*$")[0];
        ftpClient.cwd(pwd);
        updatelist();
    }


    public static void main(String[] args) {
        FtpFrame ftpFrame = new FtpFrame();

    }
}
