package application.client;

import application.Main;
import application.controller.Controller;
import javafx.application.Platform;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client{

  public static void change(Socket socket){
        Controller.TURN = !Controller.TURN;
    }

  public static Socket socket;

  public static void init() {
        try {

            socket = new Socket("localhost", 1234);

            new Thread(new Client_listen(socket)).start();
//            new Thread(new Client_send(socket, outputStream)).start();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

class Client_listen implements Runnable{
    private Socket socket;
    Client_listen(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run() {
        try{
            while(true) {
                System.out.println("我在工作");
                InputStream inputStream = socket.getInputStream();
                byte[] buf = new byte[1024];
                int readlen = 0;
                while ((readlen = inputStream.read(buf)) != -1) {
                    Controller xx = Main.fxmlLoader.getController();
                    System.out.println(new String(buf, 0, readlen));
                    String xinxi = new String(buf, 0, readlen);
                    if(xinxi.startsWith("ID")){
                        Platform.runLater(() -> {
                            xx.id = xinxi.charAt(2) - '0';
                            xx.ID.setText(String.valueOf("用户 " + xx.id));
                            xx.label.setText("等待连接");
                        });
                        if (xinxi.length() > 3) {
                            String xinxi2 = xinxi.substring(3);
                            int n = xinxi2.charAt(7) - '0';
                            Platform.runLater(() -> {
                                xx.chooseId.getItems().clear();
                            });
                            for (int i = 8; i < 8 + n; i++) {
                                int j = i;
                                Platform.runLater(() -> {
                                    if (xinxi2.charAt(j) - '0' != xx.id) {
                                        xx.chooseId.getItems().add(xinxi2.charAt(j) - '0');
                                    }
                                });
                            }
                        }
                    }else if(xinxi.startsWith("可连接用户列表")){
                        int n = xinxi.charAt(7) - '0';
                        System.out.println(n);
                        Platform.runLater(() -> {
                            xx.chooseId.getItems().clear();
                        });
                        for(int i = 8; i < 8 + n; i++){
                            int j = i;
                            System.out.println(j);
                            Platform.runLater(() -> {
                                if(xinxi.charAt(j) - '0' != xx.id) {
                                    xx.chooseId.getItems().add(xinxi.charAt(j) - '0');
                                }
                            });
                        }
                    }else if(xinxi.startsWith("连接成功")){
                        Platform.runLater(() -> {
                            xx.label.setText(xinxi);
                        });
                    }else if(xinxi.equals("正在等待连接")){
                        Platform.runLater(() -> {
                            xx.label.setText(xinxi);
                        });
                    }else if (xinxi.equals("圈")) {
                        System.out.println(555);
                        Platform.runLater(() -> {
                            xx.actor = false;
                            xx.box.setValue("叉");
                            xx.gaisheixia.setText("该你下");
                        });
                    } else if (xinxi.equals("叉")) {
                        System.out.println(666);
                        Platform.runLater(() -> {
                            xx.actor = true;
                            xx.box.setValue("圈");
                            xx.gaisheixia.setText("该对方下");
                        });
                    } else if (xinxi.equals("对方断开连接")){
                        Platform.runLater(() -> {
                            xx.tishi();
                            xx.ing = false;
                        });
                    }else {
                        {
                            {
                                Scanner input = new Scanner(xinxi);
                                int x = input.nextInt();
                                int y = input.nextInt();

                                Platform.runLater(() -> {
                                    xx.refreshBoard(x, y);
                                    xx.checkwinner();
                                });
                            }
                        }
                    }
                }
                inputStream.close();
            }

        } catch (Exception e) {
            Platform.runLater(() -> {
                Controller xx = Main.fxmlLoader.getController();
                xx.setTEXT("已和服务器断开连接");
                xx.ing = false;
            });
            throw new RuntimeException(e);
        }
    }
}
