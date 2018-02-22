package sample;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Translate;

import javax.xml.crypto.dsig.Transform;
import java.util.ArrayList;

public class Controller {
    private final static int  SELMOV = 0, ELLIPSE = 1, RECT = 2, LINE = 3;
    private boolean optionSelected[] = {true, false, false, false};
    private boolean cloneOptionSelected = false;
    private double orgSceneX, orgSceneY;
    private double orgTranslateX, orgTranslateY;
    private RadioButton[] radButtons;
    private ToggleGroup groupRadBtn;
    private Color selectedColor;
    private ArrayList<Shape> shapeList;
    private Shape selectedShape;

    @FXML public Canvas canvas;
    @FXML public Pane canvasPane;

    @FXML public RadioButton selMovRadBtn;
    @FXML public RadioButton ellipseRadBtn;
    @FXML public RadioButton rectRadBtn;
    @FXML public RadioButton lineRadBtn;

    @FXML public ColorPicker colorPicker;

    @FXML public Button deleteButton;
    @FXML public Button cloneButton;

    public Controller() {
        shapeList = new ArrayList<>();
        groupRadBtn = new ToggleGroup();
        selectedColor = Color.RED;
    }

    @FXML
    public void initialize() {
        initializeCanvasPane();
        initializeRadioButtons();
        initializeColorPicker();
        initializeButtons();

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.strokeLine(0, 1, canvas.getWidth(), 1);
        gc.strokeLine(canvas.getWidth(), 1, canvas.getWidth(), canvas.getHeight());
        gc.strokeLine(canvas.getWidth(), canvas.getHeight(), 0, canvas.getHeight());
        gc.strokeLine(0, 1, 0, canvas.getHeight());
    }

    //initialise le canvasPane : appel des fonctions
    private void initializeCanvasPane() {
        //lorsque l'on clique
        canvasPane.setOnMouseClicked(event -> {
            //si on a sélectionné l'option select / move, on vérifie si la souris est sur une shape
            if(optionSelected[SELMOV]) {
                checkMouseInShape(event.getX(), event.getY());
            //sinon on dessine
            } else {
                drawShapes(event.getX(), event.getY());
            }
        });

        canvasPane.setOnMousePressed(event -> {
            //si on est en mode clone, on clone la shape (le clone devient la selected shape) pour le mouse dragged
            checkMouseInShape(event.getX(), event.getY());

            if(cloneOptionSelected && selectedShape != null) {
                orgSceneX = event.getX();
                orgSceneY = event.getY();
                orgTranslateX = selectedShape.getTranslateX();
                orgTranslateY = selectedShape.getTranslateY();

                Shape newShape = clone(selectedShape);

                canvasPane.getChildren().add(newShape);
                shapeList.add(newShape);
                selectedShape = newShape;
            }
        });

        //déplacement du clone
        canvasPane.setOnMouseDragged(event -> {
            if(cloneOptionSelected && selectedShape != null) {
                double offsetX = event.getX() - orgSceneX;
                double offsetY = event.getY() - orgSceneY;
                double translateX = orgTranslateX + offsetX;
                double translateY = orgTranslateY + offsetY;

                selectedShape.setTranslateX(translateX);
                selectedShape.setTranslateY(translateY);
            }
        });
    }

    //vérifie si la souris est une sur une shape et met à jour la variable de classe selectedShape si oui (null si non)
    // PROBLEME AU NIVEAU DES CLONES : ils sont déplacés avec des Translate, ce qui fait qu'à l'écran ils ne sont pas affichés à leurs coordonées réelles, la fonction devrait le prendre en compte pour régler les problèmes mais manque de temps
    private void checkMouseInShape(double x, double y) {
        System.out.println(shapeList.size());

        boolean isContained = false;
        for(Shape shape : shapeList) {
            System.out.println(shape);
            if(shape.contains(x, y)) {
                selectedShape = shape;
                isContained = true;
            }
        }
        selectedShape = isContained ? selectedShape : null;
    }

    //initialise les boutons radios : select / move selectionné + ajout des listeners qui mette à jour lun tableau de booléens en fonction de l'option sélectionnée
    private void initializeRadioButtons() {
        disableDelCloBtn(false);

        selMovRadBtn.setSelected(true);
        selMovRadBtn.setToggleGroup(groupRadBtn);
        ellipseRadBtn.setToggleGroup(groupRadBtn);
        rectRadBtn.setToggleGroup(groupRadBtn);
        lineRadBtn.setToggleGroup(groupRadBtn);

        groupRadBtn.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            optionSelected[SELMOV] = (newValue == selMovRadBtn);
            optionSelected[ELLIPSE] = (newValue == ellipseRadBtn);
            optionSelected[RECT] = (newValue == rectRadBtn);
            optionSelected[LINE] = (newValue == lineRadBtn);

            disableDelCloBtn(!optionSelected[SELMOV]);
        });
    }

    //initialise le color picker : listener qui change la variable de classe selectedColor
    private void initializeColorPicker() {
        colorPicker.setValue(selectedColor);
        colorPicker.setOnAction(event -> selectedColor = colorPicker.getValue());
    }

    //initialise les boutons delete et clone
    private void initializeButtons() {
        //supprimer la shape sélectionnée par le click sur le canvasPane
        deleteButton.setOnMouseClicked(event -> {
            cloneOptionSelected = false;
            if(selectedShape != null) {
                canvasPane.getChildren().remove(selectedShape);
                shapeList.remove(selectedShape);
                selectedShape = null;
            }
        });

        //cloner la shape sélectionnée
        cloneButton.setOnMouseClicked(event -> {
            cloneOptionSelected = true;

            /*if(selectedShape != null) {
                Shape newShape = clone(selectedShape);
            }*/
        });
    }

    //renvoie une copie d'une shape
    private Shape clone(Shape shape) {
        Shape newShape = null;

        if(shape instanceof Circle) {
            Circle circle = (Circle) shape;

            newShape = new Circle(circle.getCenterX(), circle.getCenterY(), circle.getRadius(), circle.getFill());
        } else if(shape instanceof Rectangle) {
            Rectangle rect = (Rectangle) shape;

            newShape = new Rectangle(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
            newShape.setFill(rect.getFill());
        } else if(shape instanceof Line) {
            Line line = (Line) shape;

            newShape = new Line(line.getStartX(), line.getStartY(), line.getEndX(), line.getEndY());
            newShape.setStroke(line.getStroke());
        }

        return newShape;
    }

    private void drawShapes(double x, double y) {
        Shape newShape = null;

        //en fonction de l'option sélectionnée on créé la shape correspondante
        if(optionSelected[ELLIPSE]) {
            newShape = new Circle(x, y, 20, selectedColor);
        } else if(optionSelected[RECT]) {
            newShape = new Rectangle(x, y, 100, 25);
            newShape.setFill(selectedColor);
        } else if(optionSelected[LINE]) {
            newShape = new Line(x, y, x + 100, y);
            newShape.setStroke(selectedColor);
        }

        //puis on la dessine et l'ajoute ) la liste des shapes
        canvasPane.getChildren().add(newShape);
        shapeList.add(newShape);
    }

    //active/désactive les boutons delete et clone
    private void disableDelCloBtn(boolean bool) {
        deleteButton.setDisable(bool);
        cloneButton.setDisable(bool);
    }
}
