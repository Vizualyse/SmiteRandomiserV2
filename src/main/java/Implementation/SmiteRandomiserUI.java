package Implementation;/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import Interfaces.ISmiteAPI;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import java.io.*;
import java.util.ArrayList;


/**
 *
 * @author mykha
 */
public class SmiteRandomiserUI extends Application {
    //Elements godList = new Elements();
    SmiteRandomiserRules _ruleSet = new SmiteRandomiserRules();
    ISmiteAPI _smiteAPI;
    ObservableList<String> gods = FXCollections.observableArrayList();
    ArrayList<String> godType = new ArrayList();

    BorderPane root = new BorderPane();
    StackPane topStack = new StackPane();
    VBox topBox = new VBox(5);
    ComboBox list;
    Stage _modifyRulesStage = new Stage();

    private static double xOffset = 0;
    private static double yOffset = 0;

    @Override
    public void start(Stage primaryStage) {
        _smiteAPI = new SmiteAPI();
        int connectionResult = ((SmiteAPI)_smiteAPI).Connect();
        if (connectionResult == 200 || connectionResult == 0)
        {
            _smiteAPI = new SmiteWebRipAPI();
        }

        try
        {
            Image img = new Image(new FileInputStream("Rexsi Logo.png"));
            ImageView imgView = new ImageView(img);
            imgView.setFitWidth(600);
            imgView.setFitHeight(140);
            topStack.getChildren().add(imgView);
            topBox.getChildren().add(topStack);
        }
        catch(Exception e)
        {
            System.out.println(e);
        }

        topBox.setAlignment(Pos.CENTER);
        root.setTop(topBox);

        Button exit = new Button("X");
        exit.setFont(Font.font("tahoma", 10));
        exit.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
        exit.setTextFill(Color.RED);
        exit.setTranslateY(-145);
        exit.setTranslateX(290);
        exit.setOnAction(event -> primaryStage.close());
        topBox.getChildren().add(exit);

        SetupGodsDropdown();
        SetupRandomiseButton();
        ModifyRuleStageSetup();
        UpdateImage();
        /*
        updateRules();
        */

        Button rules = new Button("Modify Rules");
        rules.setFont(Font.font("arial", 16));
        rules.setTextFill(Color.web("#f9e294"));
        rules.setBackground(new Background(new BackgroundFill(Color.BLACK, new CornerRadii(10), Insets.EMPTY)));
        rules.setOnAction(event -> _modifyRulesStage.show());
        topBox.getChildren().add(rules);

        Scene scene = new Scene(root, 600, 1000);

        root.setStyle("-fx-border-color: black; -fx-background-color: #f9e294;");
        primaryStage.setTitle("Rexsi's Randomiser");
        primaryStage.initStyle(StageStyle.UNDECORATED);
        root.setOnMousePressed(event ->
        {
            xOffset = primaryStage.getX() - event.getScreenX();
            yOffset = primaryStage.getY() - event.getScreenY();
        });

        root.setOnMouseDragged(event ->
        {
            primaryStage.setX(event.getScreenX() + xOffset);
            primaryStage.setY(event.getScreenY() + yOffset);

        });

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void ModifyRuleStageSetup()
    {
        ObservableList<String> selectedRules = FXCollections.observableArrayList(_ruleSet.GetRules(SmiteRandomiserRuleType.PHYSICAL));

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

        Button exit = new Button("X");
        exit.setFont(Font.font("tahoma", 10));
        exit.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
        exit.setTextFill(Color.RED);
        exit.setTranslateX(280);
        exit.setOnAction(event -> _modifyRulesStage.hide());
        rulePane.setTop(exit);

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
            SmiteRandomiserRuleType ruleType = SmiteRandomiserRules.GetRuleTypeByIndex(ruleBox.getSelectionModel().getSelectedIndex());

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
            SmiteRandomiserRuleType ruleType = SmiteRandomiserRules.GetRuleTypeByIndex(ruleBox.getSelectionModel().getSelectedIndex());

            _ruleSet.RemoveRule(ruleType, removeBox.getSelectionModel().getSelectedIndex());
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
            SmiteRandomiserRuleType ruleType = SmiteRandomiserRules.GetRuleTypeByIndex(ruleBox.getSelectionModel().getSelectedIndex());

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
        for(String s: _ruleSet.GetRules(SmiteRandomiserRuleType.PHYSICAL)){
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

    public void SetupRandomiseButton()
    {
        Button btn = new Button("Randomise");
        btn.setFont(Font.font("arial", 16));
        btn.setTextFill(Color.web("#f9e294"));
        btn.setBackground(new Background(new BackgroundFill(Color.BLACK, new CornerRadii(10), Insets.EMPTY)));
        /*btn.setOnAction((event)->{
            Random rand = new Random();
            list.getSelectionModel().select(rand.nextInt(godList.size()));
            updateImage();
            Label l = new Label();
            l.setStyle("-fx-text-fill: black;-fx-font-family: arial;-fx-font-size: 32px;");
            if(godType.get(list.getSelectionModel().getSelectedIndex()).equals("Physical")){
                if(physRules.size()>0){
                    l.setText(physRules.get(rand.nextInt(physRules.size())));
                }
            }else{
                if(magRules.size()>0){
                    l.setText(magRules.get(rand.nextInt(magRules.size())));
                }
            }
            TilePane tile = new TilePane();
            tile.setAlignment(Pos.CENTER);
            tile.getChildren().add(l);
            tile.setTranslateY(-20);
            root.setBottom(tile);
        });*/

        topBox.getChildren().add(btn);
    }

    public void SetupGodsDropdown()
    {
        gods.addAll(_smiteAPI.GetGodNames());
        list = new ComboBox(gods);
        list.getSelectionModel().selectFirst();

        list.setBackground(new Background(new BackgroundFill(Color.BLACK, new CornerRadii(10), Insets.EMPTY)));
        SetCellFactory(list);
        SetButtonCell(list);
        list.setOnAction(event -> UpdateImage());

        topBox.getChildren().add(list);
    }

    public void UpdateImage()
    {
        String url = _smiteAPI.GetGodImageLinks().get(list.getSelectionModel().getSelectedIndex());
        Image image = new Image(url);
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(600);
        imageView.setFitWidth(450);
        root.setCenter(imageView);
    }

    public void updateGodList(){
        /*String url = "https://smite.fandom.com/wiki/Category:Gods";
        try {
            Document doc = Jsoup.connect(url).get();
            for(Element e: doc.getElementsByClass("category-page__member")){
                godList.add(e);
                for(Element f: Jsoup.connect(e.getElementsByClass("category-page__member-link").get(0).absUrl("href")).get().getElementsByClass("pi-data-value pi-font").get(0).getElementsByAttribute("title")){
                    if(f.attr("title").equals("Magical") || f.attr("title").equals("Physical")){
                        godType.add(f.attr("title"));
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(SmiteGodList.class.getName()).log(Level.SEVERE, null, ex);
        }*/
    }

    private void SetCellFactory(ComboBox comboBox) {
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

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
