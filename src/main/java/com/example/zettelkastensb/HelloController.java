package com.example.zettelkastensb;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import zettelkasten.Buzzword;
import zettelkasten.Datenbank;
import zettelkasten.Zettel;
import zettelkasten.ZettelUpdater;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static zettelkasten.Datenbank.*;


public class HelloController {

    private byte[] currentZettelId; // Variable zur Speicherung der aktuellen ZettelId

    private byte[] currentBuzzwordId;

    private String selected = "";

    @FXML
    public TextField headerZettel;

    @FXML
    public TextArea textZettel;

    @FXML
    private ListView<Zettel> zettelList;
    private ObservableList<Zettel> zettelData = FXCollections.observableArrayList();


    @FXML
    private ListView<Buzzword> buzzwordList;

    @FXML
    private ListView<Zettel> zettelBuzzwordList;
    private ObservableList<Buzzword> bwData = FXCollections.observableArrayList();

    private ObservableList<Zettel> zettelBwData = FXCollections.observableArrayList();

    private ObservableList<Buzzword> bwDataFromZettel = FXCollections.observableArrayList();
    private ObservableList<Buzzword> connectedBuzzwords = FXCollections.observableArrayList();

    @FXML
    private MenuButton mbShowBw;
    @FXML
    private RadioMenuItem btnShowAllBw;
    @FXML
    private RadioMenuItem btnShowZettelBw;

    public String getHeaderZettelText() {
        return headerZettel.getText();
    }

    public String getTextZettelText() {
        return textZettel.getText();
    }

    @FXML
        // Methode um neuen Zettel zur DB zettel hinzuzufügen
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
//Ausgewählten Text aus der TextArea in String speichern
@FXML
    public void onContextMenuRequested(ContextMenuEvent event) {

        if (textZettel.getSelectedText() != null) {

            selected = textZettel.getSelectedText();

            System.out.println("selected text saved as string");
        }
    }




    @FXML
    public void initialize() {
        try (Connection connection = DriverManager.getConnection(connectionString)) {

            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("select * from zettel");

            while (rs.next()) {


                headerZettel.setText(rs.getString("Header"));
                textZettel.setText(rs.getString("Text"));
            }
            zettelData.setAll(Datenbank.getZettelData());
            zettelList.setItems(zettelData);
            initializeZettelList();
            bwData.setAll(Datenbank.getBwData());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

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


    @FXML
    void saveOnChangeZettel(KeyEvent event){
        if (currentZettelId != null) { // Überprüfe, ob eine aktuelle ZettelId vorhanden ist
            updateZettelInDatabase();
        }
    }


    public void initializeZettelList() {
        // Zetteldaten aus Datenbank holen und zur Liste hinzufügen
        zettelList.setItems(zettelData);

        //CellFactory einrichte, um die Zeilen der ListView zu manipulieren.
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

    }


    public void initializeBuzzwordList() {
                // Set the items of the ListView 'buzzwordList' using the fetched Buzzword objects
                buzzwordList.setItems(bwData);
        System.out.println("IBW called");

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
                // ChangeListener for 'buzzwordList' to handle loading connected Zettel objects
        buzzwordList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                byte[] selectedBuzzwordId = newValue.getBuzzwordId();
                List<Zettel> connectedZettels = Datenbank.loadConnectedZettels(selectedBuzzwordId);
                zettelBuzzwordList.setItems(FXCollections.observableArrayList(connectedZettels));
            } else {
                zettelBuzzwordList.setItems(null); // Clear the 'zettelBuzzwordList'
            }

            // Pass the selected Buzzword's ID to the method to fetch associated Zettel objects
            ObservableList<Zettel> zettelData = getZettelBwData(newValue.getBuzzwordId());
            // Do something with the zettelData if needed.
                });
        initializeZettelBuzzwordList();
            }

    public void initializeBuzzwordFromZettelList() {
        // Set the items of the ListView 'buzzwordList' using the fetched Buzzword objects
        buzzwordList.setItems(connectedBuzzwords);
        System.out.println("IBWFZL called");

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
        // ChangeListener for 'buzzwordList' to handle loading connected Zettel objects
        buzzwordList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                byte[] selectedBuzzwordId = newValue.getBuzzwordId();
                List<Zettel> connectedZettels = Datenbank.loadConnectedZettels(selectedBuzzwordId);
                zettelBuzzwordList.setItems(FXCollections.observableArrayList(connectedZettels));
            } else {
                zettelBuzzwordList.setItems(null); // Clear the 'zettelBuzzwordList'
            }

            // Pass the selected Buzzword's ID to the method to fetch associated Zettel objects
            ObservableList<Zettel> zettelData = getZettelBwData(newValue.getBuzzwordId());
            // Do something with the zettelData if needed.
        });
        initializeZettelBuzzwordList();
    }

    public void initializeZettelBuzzwordList() {
        // Set the items of the ListView 'buzzwordList' using the fetched Buzzword objects
        zettelBuzzwordList.setItems(zettelBwData);
        System.out.println("IZBW called");

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
        // ChangeListener für ListView um Zettel zu laden und handlen
        zettelBuzzwordList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Zettel>() {
            @Override
            public void changed(ObservableValue<? extends Zettel> observable, Zettel oldValue, Zettel newValue) {
                if (newValue != null) {
                    currentZettelId = newValue.getZettelId();
                    headerZettel.setText(newValue.getHeader());
                    textZettel.setText(newValue.getText());
                } else {
                    currentZettelId = null;
                    headerZettel.clear();
                    textZettel.clear();
                }
            }
        });
    }
    }





    /*public void initializeZettelBwList() {
        // Zetteldaten aus Datenbank holen und zur Liste hinzufügen
        lvZettelBw.setItems(zettelBwData);

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
        lvZettelBw.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Zettel>() {
            @Override
            public void changed(ObservableValue<? extends Zettel> observable, Zettel oldValue, Zettel newValue) {
                if (newValue != null) {
                    currentZettelId = newValue.getZettelId();
                    headerZettel.setText(newValue.getHeader());
                    textZettel.setText(newValue.getText());
                } else {
                    currentZettelId = null;
                    headerZettel.clear();
                    textZettel.clear();
                }
            }
        });

    }

*/



