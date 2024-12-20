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

     private void onUpdate() {
        if (gameStarted) {
            for (int i = cars.size() - 1; i >= 0; i--) {
                Car car = cars.get(i);
                car.updatePosition();
                if (car.getX() > 800 || car.getX() < -50) {
                    ruang.getChildren().remove(car.getNode());
                    cars.remove(i);
                }
            }

            while (cars.size() < MAX_CARS) {
                Car car = Car.createRandomCar(carImages, new double[]{40, 83, 127, 169, 208, 252, 291, 329, 370, 412, 451, 489});
                cars.add(car);
                ruang.getChildren().add(car.getNode());
            }

            checkState();
        }
    }
    
    private void setupTimerDisplay() {
        timerLabel = new Label("Time: 0.00s");
        timerLabel.setFont(javafx.scene.text.Font.font(20));
        timerLabel.setTextFill(Color.BLACK);
        timerLabel.setTranslateX(650); // Positioned in top right corner
        timerLabel.setTranslateY(10);
        ruang.getChildren().add(timerLabel);
    }

    private void checkState() {
        for (Car car : cars) {
            if (frog.intersects(car.getNode())) {
                resetFrogPosition();
                loseLife();
                break;
            }
        }

        if (frog.getY() <= 0 && gameStarted) { // Adjusted winning condition
            gameStarted = false;
            showWinMessage();
        }
    }

    private void loseLife() {
        lives--;
        livesLabel.setText("Lives: " + lives);
        resetFrogPosition();

        if (lives <= 0) {
            showGameOverMessage();
        }
    }

    private void resetFrogPosition() {
        frog.resetPosition(400 - 14, 600 - 23);
    }

    private void showGameOverMessage() {
        gameStarted = false;
        sndtrk.stop();
        dth.play();
        loadFXML("/frogger/Menu.fxml");
    }

    private void showWinMessage() {
        gameStarted = false;
        sndtrk.stop();
        win.play();
        int score = calculateScore();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/frogger/Win.fxml"));
            root = loader.load();
            
            // If Win screen controller has a method to set score, use it
            WinController winController = loader.getController();
            winController.setScore(score, elapsedTime, lives);

            stage = (Stage) ruang.getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error loading Win FXML: " + e.getMessage());
        }
    }

    private void loadFXML(String fxmlPath) {
        try {
            root = FXMLLoader.load(getClass().getResource(fxmlPath));
            stage = (Stage) ruang.getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error loading FXML: " + e.getMessage());
        }
    }
    

    @FXML
    private void klik(MouseEvent event) {
        lives = 3;
        livesLabel.setText("Lives: " + lives);
        startTime = System.nanoTime();
        clk.play();

        FadeTransition fadeButton = new FadeTransition(Duration.seconds(1), tombolStart);
        fadeButton.setFromValue(1.0);
        fadeButton.setToValue(0.0);
        fadeButton.setOnFinished(e -> tombolStart.setVisible(false));

        FadeTransition fadeMenu = new FadeTransition(Duration.seconds(1), menu);
        fadeMenu.setFromValue(1.0);
        fadeMenu.setToValue(0.0);
        fadeMenu.setOnFinished(e -> menu.setVisible(false));

        fadeButton.play();
        fadeMenu.play();
        sndtrk.play();

        gameStarted = true;
        resetFrogPosition();
        ruang.requestFocus();
    }

    private void movementSetup() {
        ruang.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.W) {
                wPressed.set(true);
            } else if (e.getCode() == KeyCode.A) {
                aPressed.set(true);
            } else if (e.getCode() == KeyCode.S) {
                sPressed.set(true);
            } else if (e.getCode() == KeyCode.D) {
                dPressed.set(true);
            }
        });

        ruang.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.W) {
                wPressed.set(false);
            } else if (e.getCode() == KeyCode.A) {
                aPressed.set(false);
            } else if (e.getCode() == KeyCode.S) {
                sPressed.set(false);
            } else if (e.getCode() == KeyCode.D) {
                dPressed.set(false);
            }
        });
    }

    private void handleFrogMovement() {
        double newX = frog.getX();
        double newY = frog.getY();

        // Movement logic with boundary checks
        if (wPressed.get()) {
            newY -= MOVEMENT_VARIABLE;
        }
        if (aPressed.get()) {
            newX -= MOVEMENT_VARIABLE;
        }
        if (sPressed.get()) {
            newY += MOVEMENT_VARIABLE;
        }
        if (dPressed.get()) {
            newX += MOVEMENT_VARIABLE;
        }

        // Boundary constraints
        newX = Math.max(0, Math.min(newX, SCREEN_WIDTH - FROG_WIDTH));
        newY = Math.max(0, Math.min(newY, SCREEN_HEIGHT - FROG_HEIGHT));

        // Update frog position
        frog.setX(newX);
        frog.setY(newY);
        frog.getNode().setTranslateX(newX);
        frog.getNode().setTranslateY(newY);
    }
    
    private int calculateScore() {
        int timeBonus = (int) (1000 - elapsedTime * 10); // Faster times get more points
        int livesBonus = lives * 500; // Each remaining life adds points
        
        int totalScore = Math.max(0, timeBonus + livesBonus);
        return totalScore;
    }
    
}
