package sample;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;




public class Main extends Application {
    private static final int TILE_SIZE = 250;
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int NUM_TILE_X = WIDTH/TILE_SIZE;
    private static final int NUM_TILE_Y = HEIGHT/TILE_SIZE;

    private int numBombs = 0;
    private int numFlaggedBombs = 0;
    private int numFlags = 0;


    private Scene scene;

    private SmallSquare[][] point = new SmallSquare[NUM_TILE_X][NUM_TILE_Y];

    private void showAlertWithHeaderText(Boolean win) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        if(win){
            alert.setTitle("Congratulations!");
            alert.setHeaderText("You won!");
            alert.setContentText("Wanna play again?");
        }
        else{
            alert.setTitle(";((((((((");
            alert.setHeaderText("Sad enough, but game is over");
            alert.setContentText("Wanna play again?");
        }
        Optional<ButtonType> option = alert.showAndWait();

        if (option.get() == ButtonType.CANCEL) {
            System.exit(1);
        }
        else if(option.get() == ButtonType.OK){
            scene.setRoot(create());
        }
    }


    private List<SmallSquare> showNearbyBombs(SmallSquare square){
        List<SmallSquare> nearBombs = new ArrayList<>();
        int[] stepsArround = new int[]{
                1,  1,
                1, -1,
                1,  0,
                -1, 1,
                -1,-1,
                -1, 0,
                0,  1,
                0, -1,

        };

        /**********
         * \ | /
         * - 0 -(at least I tried to visualize how they can move :3
         * / | \
         **********/

        for(int i = 0; i < stepsArround.length; i++){
            int dx = stepsArround[i];
            int dy = stepsArround[++i];

            int newX = square.x + dx;
            int newY = square.y + dy;

            if(newX >= 0 && newY >= 0 && newX < NUM_TILE_X && newY < NUM_TILE_Y){
                nearBombs.add(point[newX][newY]);
            }

        }
        return nearBombs;
    }

    private void checkBombs(){
        if(numBombs == numFlaggedBombs&&numFlaggedBombs == numFlags)
            showAlertWithHeaderText(true);
    }

    private class SmallSquare extends StackPane{
        private int x, y;
        private Boolean hasBomb;
        private Boolean isOpened = false;
        private Boolean hasFlag = false;

        private Rectangle rect = new Rectangle(TILE_SIZE-2,TILE_SIZE-2);
        private Text text = new Text();

        public SmallSquare(int x, int y, Boolean hasBomb){
            this.x = x;
            this.y = y;
            this.hasBomb = hasBomb;

            rect.setStroke(Color.GREEN);

            text.setText(hasBomb ? "X" : "");
            text.setVisible(false);

            getChildren().addAll(rect, text);

            setTranslateX(x * TILE_SIZE);
            setTranslateY(y * TILE_SIZE);

            setOnMouseClicked(t -> {
                if(t.getButton()== MouseButton.PRIMARY){
                    showContent();
                }
                else if(t.getButton() == MouseButton.SECONDARY){
                    setFlag();
                }
            });
        }

        public void setFlag() {
            if (isOpened) return;
            else if (!hasFlag) {
                rect.setFill(Color.RED);
                hasFlag = true;

                if(hasBomb){
                    numFlaggedBombs++;
                }
            }
            else{
                rect.setFill(Color.BLACK);
                hasFlag = false;

                if(hasBomb){
                    numFlaggedBombs--;
                }
            }
            System.out.print(numFlaggedBombs+"-"+numBombs+"\n");
            checkBombs();
            if(hasFlag) numFlags++;
            if(!hasFlag)numFlags--;
            //System.out.print("numFLagged "+numFlaggedBombs+" bombs "+numBombs+" numFlags "+numFlags+"\n");
        }

        public void showContent(){
            if(isOpened) return;
            if(hasBomb){
                rect.setFill(Color.BLUE);
                showAlertWithHeaderText(false);
                scene.setRoot(create());
                return;
            }
            isOpened = true;
            text.setVisible(true);
            rect.setFill(null);

            if(text.getText().isEmpty())
                showNearbyBombs(this).forEach(SmallSquare::showContent);
            checkBombs();
        }
    }

    private Parent create(){
        Pane pane = new Pane();
        pane.setPrefSize(WIDTH, HEIGHT);


        for(int j = 0; j < NUM_TILE_Y; j++) {
            for (int i = 0; i < NUM_TILE_X; i++) {
                Boolean b = Math.random()<0.2;
                if(b)
                    numBombs++;
                //System.out.print(numBombs+"=======\n");
                SmallSquare square = new SmallSquare(i, j, b);

                point[i][j] = square;
                pane.getChildren().add(square);
            }
        }
        for (int j = 0; j < NUM_TILE_Y; j++) {
            for(int i = 0; i < NUM_TILE_X; i++) {
                SmallSquare sq = point[i][j];

                if (sq.hasBomb) continue;

                long mines = showNearbyBombs(sq).stream().filter(t -> t.hasBomb).count();
                if (mines > 0)
                    sq.text.setText(String.valueOf(mines));

            }
        }

        return pane;
    }


    @Override
    public void start(Stage primaryStage) throws Exception{

        scene = new Scene(create());
        primaryStage.setTitle("Minesweeper compilation");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
