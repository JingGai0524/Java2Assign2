package application.controller;

import application.Main;
import application.client.Client;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    public static int id;
    private static final int PLAY_1 = 1;
    private static final int PLAY_2 = 2;
    private static final int EMPTY = 0;
    private static final int BOUND = 90;
    private static final int OFFSET = 15;

    @FXML
    private Pane base_square;

    @FXML
    private Rectangle game_panel;
    public void setTEXT(String s){
        label.setText(s);
    }
    public boolean ing = true;

    public Label gaisheixia = new Label();
    public void drawSheixia(){
        gaisheixia.setLayoutX(-30);
        gaisheixia.setLayoutY(180);
        base_square.getChildren().add(gaisheixia);
    }


    public static boolean TURN = false;// TURN 为 true player1（圆圈） 下棋 反之为player2（叉） 下棋

    public Label label = new Label();
    public Label ID = new Label();
    public void drawID(){
        base_square.getChildren().add(ID);
        ID.setLayoutX(-100);
        ID.setLayoutY(-30);
        ID.setText("用户 " + String.valueOf(id));
    }
    public ChoiceBox<Integer> chooseId = new ChoiceBox<>();
    public void drawId(){
        base_square.getChildren().add(chooseId);
        chooseId.setLayoutX(-100);
        chooseId.setLayoutY(200);

        chooseId.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
                OutputStream os = null;
                try {
                    os = Client.socket.getOutputStream();
                    String sss = "开房";
                    sss += " " + id + " " + newValue;
                    System.out.println(sss);
                    byte[] msg = sss.getBytes();
                    os.write(msg);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
    public  boolean actor = true;
    private static final int[][] chessBoard = new int[3][3];
    private static final boolean[][] flag = new boolean[3][3];
    public ChoiceBox<String> box = new ChoiceBox<>();
    public void shezhiTex(){
        label.setLayoutX(-100);
        label.setLayoutY(80);
        label.setText("这里是消息提示框");
        base_square.getChildren().add(label);
    }
    public void tishi(){
        label.setText("对方已断开连接");
    }
    public void drawBox(){
        box.setLayoutX(-50);
        box.setLayoutY(-20);
        box.getItems().add("圈");
        box.getItems().add("叉");
        base_square.getChildren().add(box);
        box.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if(newValue.equals("圈")){
                    gaisheixia.setText("该对方下");
                    actor = true;
                    OutputStream os = null;
                    try {
                        os = Client.socket.getOutputStream();
                        byte[] msg = newValue.getBytes();
                        os.write(msg);
                        System.out.println("发送了圈");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }else{
                    gaisheixia.setText("该你下");
                    actor = false;
                    OutputStream os = null;
                    try {
                        os = Client.socket.getOutputStream();
                        byte[] msg = newValue.getBytes();
                        os.write(msg);
                        System.out.println("发送了叉");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        drawID();
        shezhiTex();
        drawBox();
        drawId();
        drawSheixia();
        game_panel.setOnMouseClicked(event -> {
            int x = (int) (event.getX() / BOUND);
            int y = (int) (event.getY() / BOUND);
            if (ing && (TURN == actor) && refreshBoard(x, y)) {
                try {
                    OutputStream os = Client.socket.getOutputStream();

                    String x1 = String.valueOf(x);
                    String y1 = String.valueOf(y);
                    byte[] msg = (x1 + " " + y1).getBytes();
                    os.write(msg);

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                checkwinner();

            }
        });
    }
    public boolean refreshBoard (int x, int y) {
        if (chessBoard[x][y] == EMPTY) {
            chessBoard[x][y] = TURN ? PLAY_1 : PLAY_2;
            TURN = !TURN;
            drawChess();
            if(TURN == actor){
                gaisheixia.setText("该你下");
            }else{
                gaisheixia.setText("该对方下");
            }
            return true;
        }
        return false;
    }
    private void drawChess () {
        for (int i = 0; i < chessBoard.length; i++) {
            for (int j = 0; j < chessBoard[0].length; j++) {
                if (flag[i][j]) {
                    // This square has been drawing, ignore.
                    continue;
                }
                switch (chessBoard[i][j]) {
                    case PLAY_1:
                        drawCircle(i, j);
                        break;
                    case PLAY_2:
                        drawLine(i, j);
                        break;
                    case EMPTY:
                        // do nothing
                        break;
                    default:
                        System.err.println("Invalid value!");
                }
            }
        }
    }
    private void drawCircle (int i, int j) {
        Circle circle = new Circle();
        base_square.getChildren().add(circle);
        circle.setCenterX(i * BOUND + BOUND / 2.0 + OFFSET);
        circle.setCenterY(j * BOUND + BOUND / 2.0 + OFFSET);
        circle.setRadius(BOUND / 2.0 - OFFSET / 2.0);
        circle.setStroke(Color.RED);
        circle.setFill(Color.TRANSPARENT);
        flag[i][j] = true;
    }
    private void drawLine (int i, int j) {
        Line line_a = new Line();
        Line line_b = new Line();
        base_square.getChildren().add(line_a);
        base_square.getChildren().add(line_b);
        line_a.setStartX(i * BOUND + OFFSET * 1.5);
        line_a.setStartY(j * BOUND + OFFSET * 1.5);
        line_a.setEndX((i + 1) * BOUND + OFFSET * 0.5);
        line_a.setEndY((j + 1) * BOUND + OFFSET * 0.5);
        line_a.setStroke(Color.BLUE);

        line_b.setStartX((i + 1) * BOUND + OFFSET * 0.5);
        line_b.setStartY(j * BOUND + OFFSET * 1.5);
        line_b.setEndX(i * BOUND + OFFSET * 1.5);
        line_b.setEndY((j + 1) * BOUND + OFFSET * 0.5);
        line_b.setStroke(Color.BLUE);
        flag[i][j] = true;
    }
    public void checkwinner(){
        if(checkqvan()){
            if(actor){
                label.setText("游戏结束，您胜利了");
            }else{
                label.setText("游戏结束，您失败了");
            }
            ing = false;
        }else if(checkcha()){
            if(actor){
                label.setText("游戏结束，您失败了");
            }else{
                label.setText("游戏结束，您胜利了");
            }
            ing = false;
        }else if(checkman()){
            label.setText("游戏结束，平局");
            ing = false;
        }
    }
    public boolean checkqvan(){
        boolean flag = false;
        for(int i = 0; i < 3; i++){
            if(chessBoard[i][0] == 1 && chessBoard[i][1] == 1 && chessBoard[i][2] == 1)flag = true;
        }
        for(int i = 0; i < 3; i++){
            if(chessBoard[0][i] == 1 && chessBoard[1][i] == 1 && chessBoard[2][i] == 1)flag = true;
        }
        if(chessBoard[0][0] == 1 && chessBoard[1][1] == 1 && chessBoard[2][2] == 1)flag = true;
        if(chessBoard[0][2] == 1 && chessBoard[1][1] == 1 && chessBoard[2][0] == 1)flag = true;
        return flag;
    }
    public boolean checkcha(){
        boolean flag = false;
        for(int i = 0; i < 3; i++){
            if(chessBoard[i][0] == 2 && chessBoard[i][1] == 2 && chessBoard[i][2] == 2)flag = true;
        }
        for(int i = 0; i < 3; i++){
            if(chessBoard[0][i] == 2 && chessBoard[1][i] == 2 && chessBoard[2][i] == 2)flag = true;
        }
        if(chessBoard[0][0] == 2 && chessBoard[1][1] == 2 && chessBoard[2][2] == 2)flag = true;
        if(chessBoard[0][2] == 2 && chessBoard[1][1] == 2 && chessBoard[2][0] == 2)flag = true;
        return flag;
    }
    public boolean checkman(){
        boolean flag = true;
        for(int i = 0; i < 3; i++){
            for(int j = 0; j < 3; j++){
                if(chessBoard[i][j] == 0)flag = false;
            }
        }
        return flag;
    }
}
