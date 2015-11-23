/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javafx.fotogallerie;

import com.sun.javafx.property.adapter.PropertyDescriptor;
import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.input.ZoomEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.TilePaneBuilder;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author Florian
 */
public class JavaFXFotogallerie extends Application {

    private final TabPane myTabPane = new TabPane();
    private final Pane secondMyPane = new Pane();
    private final ScrollPane scrollView = new ScrollPane();
    private final GridPane inputGridPane = new GridPane();
    private final Pane rootGroup = new VBox(12);
    private final List<File> imageList = new ArrayList();
    private final Tab imageTab = new Tab("Image");
    private ImageView largerImageView = new ImageView();
    private TilePane tilePane = TilePaneBuilder.create()
            .padding(new Insets(20, 10, 10, 10))
            .hgap(4)
            .vgap(4)
            .build();

    private Double imageWidht;
    private Double imageHeight;
    private Boolean isFullscreen = false;
    private final VBox zoomBox = new VBox();

    @Override
    public void start(final Stage stage) {

        StackPane root = new StackPane();
        stage.setTitle("Image Chooser");

        FileChooser fileChooser = new FileChooser();
        DirectoryChooser dicChooser = new DirectoryChooser();
        Button openButton = new Button("Open a Folder...");
        Button openMultipleButton = new Button("Open Pictures...");
        Button clearButton = new Button("New");

        Button zoomInButton = new Button("+");
        Button zoomOutButton = new Button("-");

        zoomInButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                for (Node child : secondMyPane.getChildren()) {
                    if (child instanceof ImageView) {
                        child.setScaleX(child.getScaleX() + 10);
                        child.setScaleY(child.getScaleY() + 10);

                    }
                }
            }
        });
        VBox zoomBox = new VBox();
        zoomBox.getChildren().addAll(zoomInButton, zoomOutButton);
        secondMyPane.getChildren().addAll(zoomBox);

        Tab openTab = new Tab("Öffen");
        scrollView.setVisible(false);

        openButton.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                        File file = dicChooser.showDialog(stage);
                        if (file != null) {
                            addFilesFromDir(file);

                        }

                        tilePane = addImagesToGrid(stage);
                        scrollView.setVisible(true);

                    }
                });

        openMultipleButton.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                        List<File> list
                        = fileChooser.showOpenMultipleDialog(stage);
                        if (list != null) {
                            for (File file : list) {
                                String fileName = file.getName().toLowerCase();

                                if (fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                                    imageList.add(file);
                                }

                            }
                        }

                        tilePane = addImagesToGrid(stage);
                        scrollView.setVisible(true);
                    }

                });

        clearButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                scrollView.setVisible(false);
                imageList.clear();
                tilePane = new TilePane();
            }
        });

        scrollView.setFitToHeight(true);
        scrollView.setFitToWidth(true);
        scrollView.setContent(tilePane);

        GridPane.setConstraints(openButton, 0, 0);
        GridPane.setConstraints(openMultipleButton, 1, 0);
        GridPane.setConstraints(clearButton, 2, 0);
        inputGridPane.setHgap(6);
        inputGridPane.setVgap(6);
        inputGridPane.getChildren().addAll(openButton, openMultipleButton, clearButton);

        rootGroup.getChildren().addAll(inputGridPane, scrollView);
        rootGroup.setPadding(new Insets(12, 12, 12, 12));
        
        openTab.setContent(rootGroup);
        myTabPane.getTabs().add(openTab);
        myTabPane.getTabs().add(imageTab);
        myTabPane.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>(){

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if(newValue  == (Number) 0) {
                secondMyPane.getChildren().clear();
                }
            }
        });

        Scene myScene = new Scene(myTabPane, 600, 600);
        myScene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            public void handle(KeyEvent ke) {
                if (ke.getCode() == KeyCode.ESCAPE) {
                    largerImageView.setX(40);
                    largerImageView.setY(40);
                    largerImageView.setFitHeight(imageHeight);
                    largerImageView.setFitWidth(imageWidht);
                    isFullscreen = false;
                }
            }
        });
        stage.setScene(myScene);

        stage.show();

        stage.widthProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                for (Node child : secondMyPane.getChildren()) {
                    if (child instanceof ImageView) {
                        ImageView test = (ImageView) child;
                        if (isFullscreen) {
                            test.setFitWidth((double) newValue);
                        }

                    }
                }
            }

        });

        stage.heightProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                for (Node child : secondMyPane.getChildren()) {
                    if (child instanceof ImageView) {
                        ImageView test = (ImageView) child;
                        if (isFullscreen) {
                            test.setFitHeight((double) newValue);
                        }
                    }
                }
            }

        });

    }

    private void addFilesFromDir(File dir) {
        if (dir.isDirectory()) {
            File[] fileList = dir.listFiles();
            for (File file : fileList) {
                imageList.add(file);
            }
        }
    }

    private TilePane addImagesToGrid(Stage stage) {
       
        Button zoomInButton = new Button("+");
        zoomInButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                largerImageView.setFitHeight(imageHeight + 10);
                largerImageView.setFitWidth(imageWidht + 10);
                imageHeight += 10;
                imageWidht += 10;
            }
        });

        Button zoomOutButton = new Button("-");
        zoomOutButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                largerImageView.setFitHeight(imageHeight - 10);
                largerImageView.setFitWidth(imageWidht - 10);
                imageHeight -= 10;
                imageWidht -= 10;
            }
        });

        Button fullScreenButtonOn = new Button("++");
        fullScreenButtonOn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                isFullscreen = true;
                stage.setFullScreen(true);
                largerImageView.setX(0);
                largerImageView.setY(0);

            }
        });

        largerImageView = new ImageView();
        zoomBox.getChildren().addAll(zoomInButton, zoomOutButton, fullScreenButtonOn);
        secondMyPane.getChildren().addAll(zoomBox);

        for (File file : imageList) {
            ImageView smallImageView = new ImageView();
            Image image = new Image(file.toURI().toString());
            smallImageView.setImage(image);
            smallImageView.setFitHeight(100);
            smallImageView.setFitWidth(100);
            // open Image
            smallImageView.setOnMouseClicked(new EventHandler<MouseEvent>() {

                public void handle(MouseEvent event) {
                    
                    Image largeImage = smallImageView.getImage();
                    largerImageView = new ImageView(largeImage);
                    largerImageView.setX(40);
                    largerImageView.setY(40);

                    imageHeight = largeImage.getHeight();
                    imageWidht = largeImage.getWidth();

                    secondMyPane.getChildren().addAll(largerImageView);

                    imageTab.setContent(secondMyPane);
                    myTabPane.getSelectionModel().select(imageTab);
                }
            });

            tilePane.getChildren().add(smallImageView);

        }
        return tilePane;

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
