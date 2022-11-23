package server;

import application.Main;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Server {
  public static ArrayList<Socket> sockets = new ArrayList<>();
  public static ArrayList<Integer> ids = new ArrayList<>();
  public static int cnt = 0;
  public static void main(String[] args) throws IOException {
    System.out.println("服务器启动");
    ServerSocket server = new ServerSocket(Main.PORT_NUMBER);
        while(true) {
      Socket p = server.accept();

      Server_listen2 l = new Server_listen2(p);
      Thread l2 = new Thread(l);
      l2.start();
      l2.interrupt();


      sockets.add(p);
      ids.add(cnt++);
      System.out.println(cnt);

      OutputStream outputStream = p.getOutputStream();
      byte[] msg = ("ID" + (cnt - 1)).getBytes();
      outputStream.write(msg);

            for (Socket i : sockets) {
                OutputStream outputStream1 = i.getOutputStream();
                String xvanze = "可连接用户列表";
                xvanze = xvanze + (ids.size());
                for (int j = 0; j < ids.size(); j++) {
                    xvanze = xvanze + ids.get(j);
                }
                System.out.println(xvanze);
                byte[] msg2 = xvanze.getBytes();
                outputStream1.write(msg2);

            }
        }

    }

    public static void game(Socket p1, Socket p2, int index1, int index2) throws IOException {
//        Socket p1 = server.accept();

//        Socket p2 = server.accept();
        p1.getOutputStream().write(("连接成功(vs用户" + index2+ ")").getBytes());
        p2.getOutputStream().write(("连接成功(vs用户" + index1+ ")").getBytes());

        new Thread(new Server_listen(p1, p2)).start();
        new Thread(new Server_listen(p2, p1)).start();

    }
}

class Server_listen2 implements Runnable {

    private Socket socket;


    Server_listen2(Socket socket) {
        this.socket = socket;
    }

    @Override
    public synchronized void run() {
        try {
            String xinxi = "";
            InputStream inputStream = socket.getInputStream();
            byte[] buf = new byte[1024];
            int readlen = 0;
            while ((readlen = inputStream.read(buf)) != -1) {
                xinxi = new String(buf, 0, readlen);
                System.out.println("收到信息   " + xinxi);
                if (xinxi.startsWith("开房")) {
                    Scanner input = new Scanner(xinxi);
                    String www = input.next();
                    int x = input.nextInt();
                    int y = input.nextInt();
                    int index1 = Server.ids.indexOf(x);
                    int index2 = Server.ids.indexOf(y);
                    Server.game(Server.sockets.get(index1), Server.sockets.get(index2), x, y);

                    Server.sockets.remove(index1);
                    Server.ids.remove(index1);
                    index2 = Server.ids.indexOf(y);

                    Server.sockets.remove(index2);
                    Server.ids.remove(index2);


                    for (Socket i : Server.sockets) {
                        System.out.println(555);
                        OutputStream outputStream = i.getOutputStream();
                        String xvanze = "可连接用户列表";
                        System.out.println(xvanze);
                        xvanze = xvanze + (Server.ids.size());
                        for (int j = 0; j < Server.ids.size(); j++) {
                            xvanze = xvanze + Server.ids.get(j);
                        }
                        System.out.println(xvanze);
                        byte[] msg = xvanze.getBytes();
                        outputStream.write(msg);
                    }
                }
                break;
            }
        } catch (IOException e) {
            int index = Server.sockets.indexOf(socket);
            Server.sockets.remove(index);
            Server.ids.remove(index);
            for (Socket i : Server.sockets) {
                System.out.println(555);
                OutputStream outputStream = null;
                try {
                    outputStream = i.getOutputStream();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                String xvanze = "可连接用户列表";
                System.out.println(xvanze);
                xvanze = xvanze + (Server.ids.size());
                for (int j = 0; j < Server.ids.size(); j++) {
                    xvanze = xvanze + Server.ids.get(j);
                }
                System.out.println(xvanze);
                byte[] msg = xvanze.getBytes();
                try {
                    outputStream.write(msg);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }



            throw new RuntimeException(e);
        }
    }
}

class Server_listen implements Runnable {

    private Socket socket;
    private Socket to;

    Server_listen(Socket socket, Socket to){
        this.socket = socket;
        this.to = to;
    }

    @Override
    public synchronized void run(){
        try {
            String xinxi = "";
            while(true) {
                InputStream inputStream = socket.getInputStream();
                byte[] buf = new byte[1024];
                int readlen = 0;
                while ((readlen = inputStream.read(buf)) != -1) {
                    xinxi = new String(buf, 0, readlen);
                    if (xinxi.startsWith("开房")) {
                        Scanner input = new Scanner(xinxi);
                        String www = input.next();
                        int x = input.nextInt();
                        int y = input.nextInt();
                        int index1 = Server.ids.indexOf(x);
                        int index2 = Server.ids.indexOf(y);
                        Server.game(Server.sockets.get(index1), Server.sockets.get(index2), index1, index2);

                        Server.sockets.remove(index1);
                        Server.ids.remove(index1);

                        Server.sockets.remove(index2);
                        Server.ids.remove(index2);

                        for (Socket i : Server.sockets) {
                            System.out.println(555);
                            OutputStream outputStream = i.getOutputStream();
                            String xvanze = "可连接用户列表";
                            System.out.println(xvanze);
                            xvanze = xvanze + (Server.ids.size());
                            for (int j = 0; j < Server.ids.size(); j++) {
                                xvanze = xvanze + Server.ids.get(j);
                            }
                            System.out.println(xvanze);
                            byte[] msg = xvanze.getBytes();
                            outputStream.write(msg);
                        }
                    }else {
                        System.out.println(xinxi);
                        OutputStream outputStream = to.getOutputStream();
                        byte[] msg = xinxi.getBytes();
                        outputStream.write(msg);
                    }
                }
            }
        } catch (IOException e) {
            OutputStream outputStream = null;
            try {
                outputStream = to.getOutputStream();
                byte[] msg = "对方断开连接".getBytes();
                outputStream.write(msg);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            throw   new RuntimeException(e);
        }
    }
}

