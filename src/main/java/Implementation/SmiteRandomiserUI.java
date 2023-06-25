package Implementation;/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import Enums.GodType;
import Helpers.ResourceHelper;
import Interfaces.IConstants;
import Interfaces.ISmiteAPI;
import com.goxr3plus.fxborderlessscene.borderless.BorderlessScene;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import org.reactfx.EventSource;
import org.reactfx.EventStream;
import org.reactfx.EventStreams;

import java.util.ArrayList;
import java.util.Random;


/**
 *
 * @author mykha
 */
public class SmiteRandomiserUI extends Application {

    ISmiteAPI _smiteAPI;
    IConstants _constants;
    SmiteRandomiserRules _ruleSet = new SmiteRandomiserRules();
    ObservableList<String> gods = FXCollections.observableArrayList();

    EventStream _onDragEvent;
    EventSource _onRandomiseEvent;
    BorderPane root = new BorderPane();
    ComboBox list;
    Stage _modifyRulesStage = new Stage();

    private static double xOffset = 0;
    private static double yOffset = 0;

    @Override
    public void start(Stage primaryStage)
    {
        _smiteAPI = new SmiteAPI();
        int connectionResult = ((SmiteAPI)_smiteAPI).Connect();
        if (connectionResult == 200 || connectionResult == 0)
        {
            _smiteAPI = new SmiteWebRipAPI();
        }

        _constants = new Constants();

        _onRandomiseEvent = new EventSource();

        VBox topBox = new VBox(5);
        topBox.setAlignment(Pos.CENTER);

        root.setTop(topBox);
        root.setStyle("-fx-border-color: black; -fx-background-color: #f9e294;");
        SetDraggable(root, primaryStage);

        BorderlessScene scene = new BorderlessScene(primaryStage, StageStyle.UNDECORATED, root, _constants.MinScreenWidth(), _constants.MinScreenHeight());
        scene.removeDefaultCSS();
        _onDragEvent = EventStreams.eventsOf(primaryStage, MouseEvent.MOUSE_DRAGGED);

        primaryStage.setTitle("Rexsi's Randomiser");
        primaryStage.setScene(scene);
        primaryStage.setWidth(_constants.DefaultScreenWidth());
        primaryStage.setHeight(_constants.DefaultScreenHeight());
        primaryStage.show();

        ModifyRuleStageSetup();

        StackPane stackPane = new StackPane();
        try
        {
            Image img = new Image(ResourceHelper.GetResourceFromFile(_constants.DataFolder() + "Rexsi Logo.png"));
            ImageView imgView = new ImageView(img);
            imgView.setFitHeight(_constants.DefaultScreenHeight() * 0.15);
            imgView.setPreserveRatio(true);
            UpdateImageOnResize(primaryStage, imgView, img, 0.15);
            stackPane.getChildren().add(imgView);
        }
        catch(Exception e)
        {
            System.out.println(e);
        }

        Button exit = CreateExitButton(primaryStage, false);
        stackPane.getChildren().add(exit);
        stackPane.setAlignment(exit, Pos.TOP_RIGHT);

        topBox.getChildren().addAll(
                stackPane,
                SetupGodsDropdown(),
                CreateButton("Modify Rules", event -> _modifyRulesStage.show()),
                CreateButton("Randomise", event -> Randomise()));

        String url = _smiteAPI.GetGodImageLinks().get(list.getSelectionModel().getSelectedIndex());
        Image image = new Image(url);
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(_constants.DefaultScreenHeight()*0.6);
        imageView.setPreserveRatio(true);
        UpdateImageOnResize(primaryStage, imageView, image, 0.6);
        _onRandomiseEvent.subscribe(x -> imageView.setImage(new Image(_smiteAPI.GetGodImageLinks().get(list.getSelectionModel().getSelectedIndex()))));
        root.setCenter(imageView);

        Randomise();
    }

    private void ModifyRuleStageSetup()
    {
        ObservableList<String> selectedRules = FXCollections.observableArrayList(_ruleSet.GetRules(GodType.PHYSICAL));

        BorderPane rulePane = new BorderPane();
        Scene ruleScene = new Scene(rulePane, 300, 400);

        rulePane.setStyle("-fx-border-color: black; -fx-background-color: #f9e294;");
        _modifyRulesStage.initStyle(StageStyle.UNDECORATED);
        rulePane.setOnMousePressed(event->
        {
            xOffset = _modifyRulesStage.getX() - event.getScreenX();
            yOffset = _modifyRulesStage.getY() - event.getScreenY();
        });

        rulePane.setOnMouseDragged(event->
        {
            _modifyRulesStage.setX(event.getScreenX() + xOffset);
            _modifyRulesStage.setY(event.getScreenY() + yOffset);
        });

        Button exit = CreateExitButton(_modifyRulesStage, true);
        rulePane.setTop(exit);
        rulePane.setAlignment(exit, Pos.TOP_RIGHT);

        VBox rules = new VBox(5);
        rules.setAlignment(Pos.TOP_CENTER);
        ComboBox ruleBox = new ComboBox();
        ruleBox.getItems().addAll("Physical", "Magical");
        ruleBox.getSelectionModel().selectFirst();

        ruleBox.setBackground(new Background(new BackgroundFill(Color.BLACK, new CornerRadii(10), Insets.EMPTY)));
        SetCellFactory(ruleBox);
        SetButtonCell(ruleBox);

        Label currentRules = new Label();
        currentRules.setStyle("-fx-text-fill: black;-fx-font-family: arial;-fx-font-size: 16px;");

        ruleBox.setOnAction(event ->
        {
            GodType ruleType = SmiteRandomiserRules.GetRuleTypeByIndex(ruleBox.getSelectionModel().getSelectedIndex());

            String currentRulesStr = "";
            for(String s: _ruleSet.GetRules(ruleType))
            {
                currentRulesStr += s + "\n";
            }
            currentRules.setText(currentRulesStr);
            selectedRules.clear();
            selectedRules.addAll(_ruleSet.GetRules(ruleType));
        });

        ComboBox removeBox = new ComboBox(selectedRules);
        removeBox.setBackground(new Background(new BackgroundFill(Color.BLACK, new CornerRadii(10), Insets.EMPTY)));
        SetCellFactory(removeBox);
        SetButtonCell(removeBox);

        Button removeButton = new Button("Remove");
        removeButton.setFont(Font.font("arial", 16));
        removeButton.setTextFill(Color.web("#f9e294"));
        removeButton.setBackground(new Background(new BackgroundFill(Color.BLACK, new CornerRadii(10), Insets.EMPTY)));
        removeButton.setOnAction(event ->
        {
            GodType ruleType = SmiteRandomiserRules.GetRuleTypeByIndex(ruleBox.getSelectionModel().getSelectedIndex());

            _ruleSet.RemoveRule(ruleType, removeBox.getSelectionModel().getSelectedIndex());
            _ruleSet.UpdateRuleFile();

            String currentRulesStr = "";
            for(String s: _ruleSet.GetRules(ruleType))
            {
                currentRulesStr += s + "\n";
            }
            currentRules.setText(currentRulesStr);
            selectedRules.clear();
            selectedRules.addAll(_ruleSet.GetRules(ruleType));
        });

        TextField ruleEntry = new TextField();
        ruleEntry.setBackground(new Background(new BackgroundFill(Color.BLACK, new CornerRadii(10), Insets.EMPTY)));
        ruleEntry.setStyle("-fx-text-fill: #f9e294;-fx-font-family: arial;-fx-font-size: 16px;");
        ruleEntry.setOnKeyPressed(event ->
        {
            GodType ruleType = SmiteRandomiserRules.GetRuleTypeByIndex(ruleBox.getSelectionModel().getSelectedIndex());

            if(event.getCode().equals(KeyCode.ENTER))
            {
                _ruleSet.AddRule(ruleType, ruleEntry.getText());
            }

            _ruleSet.UpdateRuleFile();

            String currentRulesStr = "";
            for(String s: _ruleSet.GetRules(ruleType))
            {
                currentRulesStr += s + "\n";
            }
            currentRules.setText(currentRulesStr);
            selectedRules.clear();
            selectedRules.addAll(_ruleSet.GetRules(ruleType));
        });

        String currentRulesStr = "";
        for(String s: _ruleSet.GetRules(GodType.PHYSICAL)){
            currentRulesStr += s + "\n";
        }

        currentRules.setText(currentRulesStr);

        HBox remove = new HBox(5);
        remove.setAlignment(Pos.CENTER);
        remove.getChildren().addAll(removeBox, removeButton);

        rules.getChildren().addAll(ruleBox, ruleEntry, currentRules, remove);
        rulePane.setCenter(rules);
        _modifyRulesStage.setScene(ruleScene);
    }

    public ComboBox SetupGodsDropdown()
    {
        gods.addAll(_smiteAPI.GetGodNames());
        list = new ComboBox(gods);
        list.getSelectionModel().selectFirst();

        list.setBackground(new Background(new BackgroundFill(Color.BLACK, new CornerRadii(10), Insets.EMPTY)));
        SetCellFactory(list);
        SetButtonCell(list);
        list.setOnAction(event -> _onRandomiseEvent.push(null));

        return list;
    }

    private void Randomise()
    {
        Random rand = new Random();
        list.getSelectionModel().select(rand.nextInt(gods.size()));
        _onRandomiseEvent.push(null);
        Label l = new Label();
        l.setStyle("-fx-text-fill: black;-fx-font-family: arial;-fx-font-size: 32px;");

        ArrayList<String> rules = _ruleSet.GetRules(_smiteAPI.GetGodType((String)list.getSelectionModel().getSelectedItem()));
        if (!rules.isEmpty())
        {
            l.setText(rules.get(rand.nextInt(rules.size())));
        }

        TilePane tile = new TilePane();
        tile.setAlignment(Pos.CENTER);
        tile.getChildren().add(l);
        tile.setTranslateY(-20);
        root.setBottom(tile);
    }

    private Button CreateExitButton(Stage stage, Boolean hide)
    {
        Button exit = new Button("X");
        exit.setStyle("""
                -fx-min-width: 25px;
                -fx-max-width: 25px;
                -fx-min-height: 25px;
                -fx-max-height: 25px;
                -fx-text-fill: #f00;
                -fx-font-family: tahoma;
                -fx-font-size: 12px;""");
        exit.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
        exit.setTextFill(Color.RED);
        exit.setViewOrder(0);
        exit.setOnAction(event ->
        {
            if (hide) stage.hide();
            else stage.close();
        });

        return exit;
    }

    private Button CreateButton(String buttonText, EventHandler<ActionEvent> event)
    {
        Button b = new Button(buttonText);
        b.setFont(Font.font("arial", 16));
        b.setTextFill(Color.web("#f9e294"));
        b.setBackground(new Background(new BackgroundFill(Color.BLACK, new CornerRadii(10), Insets.EMPTY)));
        b.setOnAction(event);

        return b;
    }

    private void SetCellFactory(ComboBox comboBox)
    {
        comboBox.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override public ListCell<String> call(ListView<String> param) {
                final ListCell<String> cell = new ListCell<String>() {
                    {
                        super.setPrefWidth(100);
                    }
                    @Override public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if(item!=null){
                            setText(item.toString());
                            setStyle("-fx-text-fill: #f9e294;-fx-font-family: arial;-fx-font-size: 16px;-fx-background-color: black;");
                        } else {
                            setStyle("-fx-text-fill: #f9e294;-fx-font-family: arial;-fx-font-size: 16px;-fx-background-color: black;");
                        }
                    }
                };
                return cell;
            }
        });
    }

    private void SetButtonCell(ComboBox comboBox)
    {
        comboBox.setButtonCell(new ListCell(){
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if(item!=null){
                    setText(item.toString());
                    setStyle("-fx-text-fill: #f9e294;-fx-font-family: arial;-fx-font-size: 16px;");
                } else {
                    setStyle("-fx-text-fill: #f9e294;-fx-font-family: arial;-fx-font-size: 16px;");
                }
            }

        });
    }

    private void SetDraggable(Node node, Stage stage)
    {
        node.setOnMousePressed(event ->
        {
            xOffset = stage.getX() - event.getScreenX();
            yOffset = stage.getY() - event.getScreenY();
        });

        node.setOnMouseDragged(event ->
        {
            stage.setX(event.getScreenX() + xOffset);
            stage.setY(event.getScreenY() + yOffset);

        });
    }

    private void UpdateImageOnResize(Stage stage, ImageView imageView, Image image, double percentage)
    {
        _onDragEvent.subscribe(click -> {
            double height = stage.getHeight() * percentage;    //height equal to 15% of total
            double updatedWidth = height/image.getHeight() * image.getWidth();      //width adjusted to match

            if (updatedWidth < stage.getWidth())
            {
                imageView.setFitHeight(height);
            }
            else
            {
                double updatedHeight = stage.getWidth()/image.getWidth() * image.getHeight();        //height based on maximum width (only used for shrinking)
                imageView.setFitHeight(updatedHeight);
            }
        });
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        launch(args);
    }
}
