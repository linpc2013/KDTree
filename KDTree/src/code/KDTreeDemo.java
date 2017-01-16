package code;

import code.KDTree;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
/*
 * 
 * A Visual Demo for KDTree when K is equals to 2;
 * JavaFX Application
 * Please use JAVA 8 which has JAVAFX feature to test it
 * Author : linpc2013
 *
 * */
public class KDTreeDemo extends Application {
    private static TwoDTree instance;
    private static double XSIZE = 300, YSIZE = 300;
    private static GraphicsContext gc;

    static class TwoDTree extends KDTree {

        public TwoDTree() {
            super(2);
        }

        public void draw() {
            draw(root, 0);
        }

        private void draw(Node root, int depth) {
            if (root == null)
                return;
            if (depth % 2 == 1) {
                gc.setStroke(Color.RED);
                double xmin = root.hs.min.coords[0], xmax = root.hs.max.coords[0];
                double py = root.p.coords[1];
                gc.strokeLine(xmin * XSIZE, py * YSIZE, xmax * XSIZE, py * YSIZE);
            } else {
                gc.setStroke(Color.BLUE);
                double ymin = root.hs.min.coords[1], ymax = root.hs.max.coords[1];
                double px = root.p.coords[0];
                gc.strokeLine(px * XSIZE, ymin * YSIZE, px * XSIZE, ymax * YSIZE);
            }
            draw(root.left, depth + 1);
            draw(root.right, depth + 1);
        }
    }

    public static void main(String[] args) {
        instance = new TwoDTree();
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("2DTree Demo");
        Group root = new Group();
        Canvas canvas = new Canvas(XSIZE, YSIZE);
        gc = canvas.getGraphicsContext2D();
        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                gc.fillOval(e.getX(), e.getY(), 3, 3);
                double[] coords = { e.getX() / XSIZE, e.getY() / YSIZE };
                instance.insert(new HyperPoint(coords));
                instance.draw();
            }
        });

        root.getChildren().add(canvas);
        Scene sc = new Scene(root);
        primaryStage.setScene(sc);
        primaryStage.show();
    }

}
