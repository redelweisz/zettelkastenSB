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
import javafx.util.Callback;
import zettelkasten.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static zettelkasten.Datenbank.*;


public class HelloController {
    private byte[] currentZettelId; // Variable zur Speicherung der aktuellen ZettelId
    private byte[] currentBuzzwordId;
    private byte[] currentCollectionId;
    private String selected = "";
   public String collectionName = "";
    private ObservableList<Buzzword> bwData = FXCollections.observableArrayList();
    private ObservableList<Zettel> currentZettelData = FXCollections.observableArrayList();
    public ObservableList<Buzzword> bwDataFromZettel = FXCollections.observableArrayList();
    private ObservableList<Buzzword> connectedBuzzwords = FXCollections.observableArrayList();
    private ObservableList<Zettel> zettelData = FXCollections.observableArrayList();
    private ObservableList<Zettel> selectedZettel = FXCollections.observableArrayList();
    private ObservableList<Zettel> searchResults = FXCollections.observableArrayList();
    @FXML
    public TextField headerZettel;
    @FXML
    public TextArea textZettel;
    @FXML
    private TextField txtSearchBar;
    @FXML
    private TextField txtFieldCollectionName;
    @FXML
    private ListView<Zettel> zettelList;
    private ListView<Zettel> zettelListView;
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
    @FXML
    private Button btnDeleteZettel;
    @FXML
    private Button btnRemoveFromCollection;



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
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    // Methode um ausgewählten Zettel aus der DB zettel zu löschen
    @FXML
    void deleteZettel(ActionEvent event) {
        Zettel selectedZettel = zettelList.getSelectionModel().getSelectedItem();
        //Zeige eine Warnung, falls kein zu löschender Zettel in zettelList ausgewählt wurde
        if (selectedZettel == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "First select a Zettel in the list above please.");
            return;
        }

        // Zeige einen Bestätigungsdialog vor dem Löschen
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Zettel");
        alert.setContentText("Are you sure you want to delete the selected Zettel?");

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Datenbank.deleteZettel(selectedZettel.getZettelId());

                initialize();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    //Methode um die ListView im Dialogfenster für die Collections anzuzeigen
    public void initializeZettelListView() {
        zettelListView = new ListView<>(Datenbank.getZettelData());
        zettelListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        zettelListView.setCellFactory(param -> new ListCell<>() {
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
                    zettel = z; //  Referenz zum verbundenen Zettelobjekt löschen
                } else {
                    setGraphic(null);
                    zettel = null; // Referenz verlieren, wenn Zelle leer ist
                }
            }
        });
    }
//Methode um neues Buzzword anzulegen
    @FXML
   void newBuzzword(ActionEvent event) {
        // Prüfen ob BW bereits existiert
        String selectedBuzzword = selected;

        // Check if the buzzword already exists in bwData
        boolean buzzwordExists = bwData.stream()
                .anyMatch(buzzword -> buzzword.getName().equalsIgnoreCase(selectedBuzzword));


        if (buzzwordExists) {
            // Alert anzeigen, falls BW bereits existiert
            showAlert(Alert.AlertType.WARNING, "Duplicate Buzzword", "A buzzword with the name '" + selectedBuzzword + "' already exists.");
        } else {
            Buzzword b = new Buzzword(selected);
            Datenbank.insertBuzzword(b);
            System.out.println("New Buzzword generated");
            currentBuzzwordId = b.getBuzzwordId(); // Speichere die aktuelle BuzzwordId in der Variable
        }
    }
//Methode um per Button New Collection und Eingabedialog eine neue Collection zu erstellen
    @FXML
    void newCollection(ActionEvent event) {
        initializeZettelListView();
        Dialog<Collection> dialog = new Dialog<>();
        dialog.setTitle("Create New Collection");

        ButtonType createButtonType = new ButtonType("Create Collection", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        TextField collectionNameField = new TextField();
        collectionNameField.setPromptText("Enter Collection Name");

        VBox content = new VBox(10);
        content.getChildren().addAll(zettelListView, collectionNameField);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(new Callback<ButtonType, Collection>() {
            @Override
            public Collection call(ButtonType buttonType) {
                if (buttonType == createButtonType) {
                    String name = collectionNameField.getText();

                    byte[] collectionId = Collection.generateCollectionId();

                    Collection c = new Collection(collectionId, name);
                    Datenbank.insertCollection(c);
                    Datenbank.insertIntoCollection(collectionId, zettelListView.getSelectionModel().getSelectedItems());
                    showAlert(Alert.AlertType.INFORMATION, "Collection Created", "Collection '" + name + "' has been created.");

                    ObservableList<Zettel> selectedZettels = zettelListView.getSelectionModel().getSelectedItems();
                    c.getZettelCollection().addAll(selectedZettels);

                    return c;
                }
                return null;
            }
        });

        dialog.showAndWait();
        initializeCollectionList();
    }
    @FXML
    void deleteCollection(ActionEvent event) {
        Collection selectedCollection = collectionList.getSelectionModel().getSelectedItem();

        if (selectedCollection == null) {
            // Zeige eine Warnung, falls keine zu löschende Collection in collectionList ausgewählt ist
            showAlert(Alert.AlertType.WARNING, "No Selection", "First select a Collection in the list above, please.");
            return;
        }

        // Zeige einen Bestätigungsdialog vor dem Löschen
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Collection");
        alert.setContentText("Are you sure you want to delete the selected Collection?" + selectedCollection.getName());
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Datenbank.deleteCollection(selectedCollection.getCollectionId());
                initialize();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
    // Methode um ausgewählten Zettel aus ausgewählter Collection zu entfernen
    @FXML
    void removeFromCollection() throws SQLException {
        Collection selectedCollection = collectionList.getSelectionModel().getSelectedItem();
        Zettel selectedZettel = zettelList.getSelectionModel().getSelectedItem();

        if (selectedCollection == null || selectedZettel == null) {
            // Alert anzeigen, wenn nicht Zettel UND Collection ausgewählt wurden
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a Collection and a Zettel to remove.");
            return;
        }

        byte[] zettelIdToRemove = selectedZettel.getZettelId();
        removeZettelFromCollection(zettelIdToRemove);

        // Liste refreshen
        updateZettelListForCollection(selectedCollection.getCollectionId());
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

                Collection c = new Collection(collectionId, collectionName);
                Datenbank.insertCollection(c);
                Datenbank.insertIntoCollection(collectionId, selectedZettel);
                showAlert(Alert.AlertType.INFORMATION, "Collection Created", "Collection '" + name + "' has been created.");
            }
            initializeCollectionList();
        } else {
            showAlert(Alert.AlertType.WARNING, "Empty Collection Name", "Please enter a collection name.");
        }
    }



    //Methode um Alert anzuzeigen
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
    public void search(String searchText){
        try {
            // Call the searchZettelByText method with the new search text
            searchResults = Datenbank.searchZettelByText(searchText);
            System.out.println("Search for: " + searchText);
            zettelList.setItems(searchResults);
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
                        zettel = z; // Referenz zum verbundenen Zettelobjekt löschen
                    } else {
                        setGraphic(null);
                        zettel = null; // Referenz verlieren, wenn Zelle leer ist
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
            if(searchResults.size() > 0)
                zettelList.getSelectionModel().select(0);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //ZettelListe und Data-Objekte initialisieren
    @FXML
    public void initialize() {
            zettelData.setAll(Datenbank.getZettelData());
            zettelList.setItems(zettelData);
            initializeZettelList();
            bwData.setAll(Datenbank.getBwData());
            initializeCollectionList();

// Listener für die search bar
        txtSearchBar.textProperty().addListener((observable, oldValue, searchText) -> {
            search(searchText);
                });
        // Listener für Auswahl in collectionsList
        collectionList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                byte[] collectionId = newValue.getCollectionId();
                updateZettelListForCollection(collectionId);
            }
        });

        // RemoveFromCollectionButton disablen
        btnRemoveFromCollection.setDisable(true);

// Listener um Button zu enablen, wenn beides ausgewählt wurde
        collectionList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            btnRemoveFromCollection.setDisable(newValue == null || zettelList.getSelectionModel().isEmpty());
        });

        zettelList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            btnRemoveFromCollection.setDisable(newValue == null || collectionList.getSelectionModel().isEmpty());
        });

    }
    //Methode um die ListView ZettelList mit den Zetteln der ausgewählten Collection zu befüllen
    @FXML
    private void updateZettelListForCollection(byte[] collectionId) {
        ObservableList<Zettel> collectionZettel = loadZettelFromCollection(collectionId);
        zettelList.setItems(collectionZettel);

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
                    zettel = z; // Referenz zum verbundenen Zettelobjekt löschen
                } else {
                    setGraphic(null);
                    zettel = null; // Referenz verlieren, wenn Zelle leer ist
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
        if(collectionZettel.size() > 0)
            zettelList.getSelectionModel().select(0);
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
                    zettel = z; // Referenz zum verbundenen Zettelobjekt löschen
                } else {
                    setGraphic(null);
                    zettel = null; // Referenz verlieren, wenn Zelle leer ist
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
            checkForBuzzwordsInCurrentZettel(currentZettelId);
            initializeBuzzwordList();
            updateBuzzwordsForZettel(currentZettelId, bwDataFromZettel);
        }
    }

    public void checkForBuzzwordsInCurrentZettel(byte[] currentZettelId) {
        for (Buzzword buzzword : bwData) {
            checkForBuzzwords(buzzword, currentZettelId);
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
                });
        initializeZettelBuzzwordList();
            }

            //ListView zettelBuzzwordList mit Zetteln zu einem Buzzword befüllen
    public void initializeZettelBuzzwordList() {
        // Items für ListView zettelBuzzwordList setzen
        zettelBuzzwordList.setItems(currentZettelData);
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
                    zettel = z; // Referenz zum Zettelobjekt speichern
                } else {
                    setGraphic(null);
                    zettel = null; // Referenz verlieren, wenn Zelle leer ist
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
                    buzzword = b; // Referenz zum verbundenen Zettelobjekt löschen
                } else {
                    setGraphic(null);
                    buzzword = null; // Referenz löschen, wenn Zelle leer ist
                }
            }
        });
        // ChangeListener für ListView um verbundene Zettel zu laden
        buzzwordList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                byte[] selectedBuzzwordId = newValue.getBuzzwordId();
                List<Zettel> connectedZettel = Datenbank.loadConnectedZettel(selectedBuzzwordId);
                zettelBuzzwordList.setItems(FXCollections.observableArrayList(connectedZettel));
                //BuzzwordID weitergeben, um verbundene Zettel zu holen
                ObservableList<Zettel> zettelData = getCurrentZettelData(newValue.getBuzzwordId());
            } else {
                zettelBuzzwordList.setItems(null); //zettelBuzzwordList löschen
            }
        });
        initializeZettelBuzzwordList();
    }

    // Methode um ListView collectionList zu befüllen
    void initializeCollectionList() {
        try {
            ObservableList<Collection> collectionData = Datenbank.getCollections();
            collectionList.setItems(collectionData);

            collectionList.setCellFactory(param -> new ListCell<Collection>() {
                @Override
                protected void updateItem(Collection item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null || item.getName() == null) {
                        setText(null);
                    } else {
                        setText(item.getName());
                    }
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    }