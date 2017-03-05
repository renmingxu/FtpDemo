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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.*;

/**
 * Created by renmingxu on 2017/3/4.
 */
public class FtpFrame extends JFrame{
    public JTextField addrField;
    public JTextField userField;
    public JPasswordField passField;
    public DefaultListModel filesList;
    public JButton loginBtn;
    public JButton downloadBtn;
    public JButton uploadBtn;
    public JButton openBtn;
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
        loginBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    String user = FtpFrame.this.userField.getText();
                    String pass = new String(FtpFrame.this.passField.getPassword());
                    String addr = FtpFrame.this.addrField.getText();
                    if (!"ftp://".equals(addr.substring(0,6))) {

                    }
                }
            }
        });
        panel.add(loginBtn);
        openBtn = new JButton("打开");
        openBtn.setFont(font);
        openBtn.setBounds(600, 50, 90,30);
        panel.add(openBtn);
        downloadBtn = new JButton("下载");
        downloadBtn.setFont(font);
        downloadBtn.setBounds(600, 100,90,30);
        panel.add(downloadBtn);
        DefaultListModel listModel = new DefaultListModel();
        JList jlist = new JList(listModel);
        JScrollPane ps = new JScrollPane(jlist);
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
                        for(File file:list)
                            temp+=file.getAbsolutePath()+";\n";
                        JOptionPane.showMessageDialog(null, temp);
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
    public static void main(String[] args) {
        FtpFrame ftpFrame = new FtpFrame();

    }
}
