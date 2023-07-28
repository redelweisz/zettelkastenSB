package com.example.zettelkastensb;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import zettelkasten.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static zettelkasten.Datenbank.*;


public class HelloController {
    private byte[] currentZettelId; // Variable zur Speicherung der aktuellen ZettelId
    private byte[] currentBuzzwordId;
    private byte[] currentCollectionId;
    private String selected = "";
   public String collectionName = "";
    private ObservableList<Buzzword> bwData = FXCollections.observableArrayList();
    private ObservableList<Zettel> zettelBwData = FXCollections.observableArrayList();
    private ObservableList<Buzzword> bwDataFromZettel = FXCollections.observableArrayList();
    private ObservableList<Buzzword> connectedBuzzwords = FXCollections.observableArrayList();
    private ObservableList<Zettel> zettelData = FXCollections.observableArrayList();
    private ObservableList<Zettel> selectedZettel = FXCollections.observableArrayList();
    @FXML
    public TextField headerZettel;
    @FXML
    public TextArea textZettel;
    @FXML
    private TextField txtFieldCollectionName;
    @FXML
    private ListView<Zettel> zettelList;
    @FXML
    private ListView<Buzzword> buzzwordList;
    @FXML
    private ListView<Zettel> zettelBuzzwordList;
    @FXML
    private ListView<Collection> collectionList;
    @FXML
    private MenuButton mbShowBw;
    @FXML
    private RadioMenuItem btnShowAllBw;
    @FXML
    private RadioMenuItem btnShowZettelBw;
    @FXML
    private Button btnCreateCollectionFromSelected;

    public String getHeaderZettelText() {

        return headerZettel.getText();
    }

    public String getTextZettelText() {

        return textZettel.getText();
    }
    // Methode um neuen Zettel zur DB zettel hinzuzufügen
    @FXML
    void newZettel(ActionEvent event) {
        LocalDate date = LocalDate.now();
        Zettel z = new Zettel(date, new ArrayList<>());
        try {
            Datenbank.insertZettel(z);
            System.out.println("New Zettel generated");
            currentZettelId = z.getZettelId(); // Speichere die aktuelle ZettelId in der Variable
            zettelData.add(z); // Add the new Zettel to the zettelData list
            zettelList.getSelectionModel().select(z); // Select the newly added Zettel in the ListView
            initialize();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
//Methode um neues Buzzword anzulegen
    @FXML
   void newBuzzword(ActionEvent event) {
        Buzzword b = new Buzzword(selected);
        Datenbank.insertBuzzword(b);
        System.out.println("New Buzzword generated");
        currentBuzzwordId = b.getBuzzwordId(); // Speichere die aktuelle BuzzwordId in der Variable
    }

    @FXML
    void newCollection(ActionEvent event) {
        Collection c = new Collection(null, collectionName);
        Datenbank.insertCollection(c);
        System.out.println("New Collection generated");
        currentCollectionId = c.getCollectionId(); // Speichere die aktuelle CollectionId in der Variable
    }

    @FXML
    private void handleCreateCollection() {
        //Text aus TextField auslesen
        String name = txtFieldCollectionName.getText();
        if (!name.isEmpty()) {
            collectionName = name;

            selectedZettel = zettelBuzzwordList.getSelectionModel().getSelectedItems();

            if (selectedZettel.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "No Zettel Selected", "Please select at least one Zettel.");
            } else {
                byte[] collectionId = Collection.generateCollectionId();

                Collection c = new Collection(null, collectionName);
                Datenbank.insertCollection(c);
                Datenbank.insertIntoCollection(collectionId, selectedZettel);
                showAlert(Alert.AlertType.INFORMATION, "Collection Created", "Collection '" + name + "' has been created.");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Empty Collection Name", "Please enter a collection name.");
        }
    }



    //Methode um Alarm anzuzeigen
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    //Ausgewählten Text aus der TextArea in String speichern
    @FXML
    public void onContextMenuRequested(ContextMenuEvent event) {

        if (textZettel.getSelectedText() != null) {

            selected = textZettel.getSelectedText();

            System.out.println("selected text saved as string");
        }
    }
    //ZettelListe und Data-Objekte initialisieren
    @FXML
    public void initialize() {
            zettelData.setAll(Datenbank.getZettelData());
            zettelList.setItems(zettelData);
            initializeZettelList();
            bwData.setAll(Datenbank.getBwData());
    }
    //ListView mit Zetteln befüllen
    public void initializeZettelList() {
        // Zetteldaten aus Datenbank holen und zur Liste hinzufügen
        zettelList.setItems(zettelData);

        //CellFactory einrichten, um die Zeilen der ListView zu manipulieren.
        zettelList.setCellFactory(param -> new ListCell<>() {
            private final VBox vbRoot = new VBox();
            private final Label lblHeader = new Label();
            private final Label lblText = new Label();
            private Zettel zettel;

            {
                lblHeader.setStyle("-fx-font-weight: bold;");
                vbRoot.setSpacing(10);
                vbRoot.getChildren().addAll(lblHeader, lblText);
            }

            @Override
            protected void updateItem(Zettel z, boolean empty) {
                super.updateItem(z, empty);

                if (z != null && !empty) {
                    lblHeader.setText(z.getHeader());
                    lblText.setText(z.getText().split("\n")[0]);
                    setGraphic(vbRoot);
                    zettel = z; // Save the reference to the associated Zettel object
                } else {
                    setGraphic(null);
                    zettel = null; // Clear the reference when the cell is empty
                }
            }
        });
        // ChangeListener für ListView um Zettel zu laden und handlen
        zettelList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Zettel>() {
            @Override
            public void changed(ObservableValue<? extends Zettel> observable, Zettel oldValue, Zettel newValue) {
                if (newValue != null) {
                    currentZettelId = newValue.getZettelId();
                    headerZettel.setText(newValue.getHeader());
                    textZettel.setText(newValue.getText());
                    connectedBuzzwords = getBwFromZettel(currentZettelId);
                    initializeBuzzwordFromZettelList();
                } else {
                    currentZettelId = null;
                    headerZettel.clear();
                    textZettel.clear();
                }
            }
        });
        if(zettelData.size() > 0)
            zettelList.getSelectionModel().select(0);
    }
    //Zetteldaten in Datenbank updaten
    public void updateZettelInDatabase() {
        String header = getHeaderZettelText();
        String text = getTextZettelText();
        if (currentZettelId != null) {
            Zettel zettelToUpdate = zettelData.stream()
                    .filter(z -> Arrays.equals(z.getZettelId(), currentZettelId))
                    .findFirst()
                    .orElse(null);

            if (zettelToUpdate != null) {
                zettelToUpdate.setHeader(header);
                zettelToUpdate.setText(text);

                // Zettel in der Datenbank Updaten
                ZettelUpdater zettelUpdater = new ZettelUpdater();
                zettelUpdater.updateZettel(header, text, currentZettelId);
            } else {
                System.out.println("Keine aktuelle ZettelId vorhanden.");
            }

        }
    }
    //Zetteländerungen beim Tippen speichern
    @FXML
    void saveOnChangeZettel(KeyEvent event){
        if (currentZettelId != null) { // Überprüfe, ob eine aktuelle ZettelId vorhanden ist
            updateZettelInDatabase();
        }
    }

    //Inhalt von ListView buzzwordList für alle Buzzwords setzen
    public void initializeBuzzwordList() {
        // Items von ListView buzzwordList setzen
        buzzwordList.setItems(bwData);
        System.out.println("initializeBuzzwordList() called");
        buzzwordList.setCellFactory(param -> new ListCell<>() {
            private final VBox vbRoot = new VBox();
            private final Label lblName = new Label();
            private Buzzword buzzword;
            {
                vbRoot.setSpacing(10);
                vbRoot.getChildren().addAll(lblName);
            }

            @Override
            protected void updateItem(Buzzword b, boolean empty) {
                super.updateItem(b, empty);

                if (b != null && !empty) {

                    lblName.setText(b.getName());
                    setGraphic(vbRoot);
                    buzzword = b; // Referenz zum zugeordneten Zettelobjekt speichern
                } else {
                    setGraphic(null);
                    buzzword = null; // Referenz löschen, wenn kein Buzzword gewählt
                }
            }
        });
        // ChangeListener für buzzwordList um verbundene Zettelobjekte zu laden
        buzzwordList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                byte[] selectedBuzzwordId = newValue.getBuzzwordId();
                List<Zettel> connectedZettel = Datenbank.loadConnectedZettel(selectedBuzzwordId);
                zettelBuzzwordList.setItems(FXCollections.observableArrayList(connectedZettel));
            } else {
                zettelBuzzwordList.setItems(null); // Clear the 'zettelBuzzwordList'
            }

            // BuzzwordID weiterreichen und die verbundenen Zettel zu holen
            ObservableList<Zettel> zettelData = getZettelBwData(newValue.getBuzzwordId());
                });
        initializeZettelBuzzwordList();
            }

            //ListView zettelBuzzwordList mit Zetteln zu einem Buzzword befüllen
    public void initializeZettelBuzzwordList() {
        // Items für ListView zettelBuzzwordList setzen
        zettelBuzzwordList.setItems(zettelBwData);
        System.out.println("initializeZettelBuzzwordList() called");
        zettelBuzzwordList.setCellFactory(param -> new ListCell<>() {
            private final VBox vbRoot = new VBox();
            private final Label lblHeader = new Label();
            private final Label lblText = new Label();
            private Zettel zettel;

            {
                lblHeader.setStyle("-fx-font-weight: bold;");
                vbRoot.setSpacing(10);
                vbRoot.getChildren().addAll(lblHeader, lblText);
            }

            @Override
            protected void updateItem(Zettel z, boolean empty) {
                super.updateItem(z, empty);

                if (z != null && !empty) {

                    lblHeader.setText(z.getHeader());
                    lblText.setText(z.getText().split("\n")[0]);
                    setGraphic(vbRoot);
                    zettel = z; // Save the reference to the associated Zettel object
                } else {
                    setGraphic(null);
                    zettel = null; // Clear the reference when the cell is empty
                }
            }
        });

        //Multiple selection erlauben
        zettelBuzzwordList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // ChangeListener für ListView um die ausgewählten Zettel zu speichern
        zettelBuzzwordList.getSelectionModel().getSelectedItems().addListener((ListChangeListener<Zettel>) change -> {

            MultipleSelectionModel<Zettel> selectionModel = zettelBuzzwordList.getSelectionModel();


            ObservableList<Zettel> selectedZettel = selectionModel.getSelectedItems();
            for (Zettel zettel : selectedZettel) {
                byte[] zettelId = zettel.getZettelId(); // Replace getZettelId() with the actual method
            }
        });

        // ChangeListener für ListView um Zetteldaten zu laden und handlen
        zettelBuzzwordList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                currentZettelId = newValue.getZettelId(); // Replace getZettelId() with the actual method
                headerZettel.setText(newValue.getHeader());
                textZettel.setText(newValue.getText());
            } else {
                currentZettelId = null;
                headerZettel.clear();
                textZettel.clear();
            }
        });
    }

//ListView buzzwordList für Buzzwords von ausgewähltem Zettel setzen
    public void initializeBuzzwordFromZettelList() {
        //Items für ListView buzzwordList setzen
        buzzwordList.setItems(connectedBuzzwords);
        System.out.println("initializeBuzzwordFromZettelList() called");

        buzzwordList.setCellFactory(param -> new ListCell<>() {
            private final VBox vbRoot = new VBox();
            private final Label lblName = new Label();

            private Buzzword buzzword;
            {
                vbRoot.setSpacing(10);
                vbRoot.getChildren().addAll(lblName);
            }

            @Override
            protected void updateItem(Buzzword b, boolean empty) {
                super.updateItem(b, empty);

                if (b != null && !empty) {

                    lblName.setText(b.getName());
                    setGraphic(vbRoot);
                    buzzword = b; // Save the reference to the associated Zettel object
                } else {
                    setGraphic(null);
                    buzzword = null; // Clear the reference when the cell is empty
                }
            }
        });
        // ChangeListener für ListView um verbundene Zettel zu laden
        buzzwordList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                byte[] selectedBuzzwordId = newValue.getBuzzwordId();
                List<Zettel> connectedZettel = Datenbank.loadConnectedZettel(selectedBuzzwordId);
                zettelBuzzwordList.setItems(FXCollections.observableArrayList(connectedZettel));
            } else {
                zettelBuzzwordList.setItems(null); //zettelBuzzwordList löschen
            }

            //BuzzwordID weitergeben, um verbundene Zettel zu holen
            ObservableList<Zettel> zettelData = getZettelBwData(newValue.getBuzzwordId());

        });
        initializeZettelBuzzwordList();
    }

    }