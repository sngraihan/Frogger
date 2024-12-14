    package frogger;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

abstract class GameElement {
    protected double x;
    protected double y;

    public GameElement(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public abstract Node getNode();
}

class Car extends GameElement {
    private static final double DEFAULT_SPEED = 3.0;
    private static final int DEFAULT_DIRECTION = 1;
    private double speed;
    private int direction;
    private ImageView imageView;

    public Car(double x, double y, double speed, int direction, Image image) {
        super(x, y);
        this.speed = speed;
        this.direction = direction;
        this.imageView = new ImageView(image);
        initializeCar();
    }

    public static Car createRandomCar(List<Image> carImages, double[] lanes) {
        Image carImage = carImages.get((int) (Math.random() * carImages.size()));
        boolean fromRightToLeft = Math.random() < 0.5;
        double speed = Math.random() * 4 + 1;
        double laneY = lanes[(int) (Math.random() * lanes.length)];
        double startX = fromRightToLeft ? 800 : -50;
        return new Car(startX, laneY, speed, fromRightToLeft ? -1 : 1, carImage);
    }

    private void initializeCar() {
        imageView.setFitWidth(60);
        imageView.setFitHeight(40);
        imageView.setPreserveRatio(true);
        imageView.setTranslateX(x);
        imageView.setTranslateY(y);
        if (direction == -1) {
            imageView.setScaleX(-1);
        }
    }

    @Override
    public Node getNode() {
        return imageView;
    }

    public double getSpeed() {
        return speed;
    }

    public int getDirection() {
        return direction;
    }

    public void updatePosition() {
        x += speed * direction;
        imageView.setTranslateX(x);
    }
}

class Frog extends GameElement {
    private Rectangle rectangle;

    public Frog(double x, double y, Image image) {
        super(x, y);
        this.rectangle = new Rectangle(28, 23);
        this.rectangle.setFill(new javafx.scene.paint.ImagePattern(image));
        this.rectangle.setTranslateX(x);
        this.rectangle.setTranslateY(y);
    }

    @Override
    public Node getNode() {
        return rectangle;
    }

    public void resetPosition(double startX, double startY) {
        x = startX;
        y = startY;
        rectangle.setTranslateX(x);
        rectangle.setTranslateY(y);
    }

    public void move(double dx, double dy) {
        x += dx;
        y += dy;
        rectangle.setTranslateX(x);
        rectangle.setTranslateY(y);
    }

    public boolean intersects(Node other) {
        return rectangle.getBoundsInParent().intersects(other.getBoundsInParent());
    }
    
    @Override
    public void setX(double x) {
        this.x = x;
        this.rectangle.setTranslateX(x);
    }

    @Override
    public void setY(double y) {
        this.y = y;
        this.rectangle.setTranslateY(y);
    }
}


public class FXMLDocumentController implements Initializable {
    @FXML
    private AnchorPane ruang;

    @FXML
    private ImageView tombolStart;

    @FXML
    private ImageView menu;

    private Frog frog;

    @FXML
    private ImageView jalan1;
    
    private List<Car> cars = new ArrayList<>();
    private boolean gameStarted = false;

    private Stage stage;
    private Scene scene;
    private Parent root;
    
    private static final double SCREEN_WIDTH = 800;
    private static final double SCREEN_HEIGHT = 600;
    private static final double FROG_WIDTH = 28;
    private static final double FROG_HEIGHT = 23;

    private final BooleanProperty wPressed = new SimpleBooleanProperty();
    private final BooleanProperty aPressed = new SimpleBooleanProperty();
    private final BooleanProperty sPressed = new SimpleBooleanProperty();
    private final BooleanProperty dPressed = new SimpleBooleanProperty();

    private AnimationTimer gameTimer;
    private static final int MOVEMENT_VARIABLE = 2;

    private static final int MAX_CARS = 35;
    private List<Image> carImages = new ArrayList<>();
    
    private Label timerLabel;
    private long startTime;
    private long elapsedTime;

    private MediaPlayer sndtrk;
    private MediaPlayer dth;
    private MediaPlayer win;
    private MediaPlayer clk;

    private static final Media SOUNDTRACK = new Media(FXMLDocumentController.class.getResource("/frogger/sfx/soundtrack.mp3").toExternalForm());
    private static final Media DEATH = new Media(FXMLDocumentController.class.getResource("/frogger/sfx/death.mp3").toExternalForm());
    private static final Media WINNING = new Media(FXMLDocumentController.class.getResource("/frogger/sfx/win.mp3").toExternalForm());
    private static final Media CLICK = new Media(FXMLDocumentController.class.getResource("/frogger/sfx/click.mp3").toExternalForm());

    private int lives = 3;
    private Label livesLabel;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        movementSetup();
        setupLivesDisplay();
        setupTimerDisplay();
        loadCarImages();
        setupFrog();

        sndtrk = new MediaPlayer(SOUNDTRACK);
        sndtrk.setVolume(0.7);
        sndtrk.setOnEndOfMedia(() -> sndtrk.seek(Duration.ZERO));

        dth = new MediaPlayer(DEATH);
        dth.setVolume(0.7);

        win = new MediaPlayer(WINNING);
        win.setVolume(0.7);
        
        clk = new MediaPlayer(CLICK);
        clk.setVolume(0.7);

        gameTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (gameStarted) {
                    updateTimerDisplay();
                    onUpdate();
                }
                handleFrogMovement();
            }
        };
        gameTimer.start();
    }

    private void setupFrog() {
        Image frogImage = new Image(getClass().getResourceAsStream("/frogger/images/frog.png"));
        frog = new Frog(400 - 14, 600 - 23, frogImage);
        ruang.getChildren().add(frog.getNode());
    }

    private void loadCarImages() {
        carImages.add(new Image(getClass().getResourceAsStream("/frogger/images/van.png")));
        carImages.add(new Image(getClass().getResourceAsStream("/frogger/images/taxi.png")));
        carImages.add(new Image(getClass().getResourceAsStream("/frogger/images/smalltruck.png")));
        carImages.add(new Image(getClass().getResourceAsStream("/frogger/images/orangecar.png")));
        carImages.add(new Image(getClass().getResourceAsStream("/frogger/images/police.png")));
    }
    
}
