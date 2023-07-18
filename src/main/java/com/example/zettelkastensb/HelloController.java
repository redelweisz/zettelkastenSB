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

import static zettelkasten.Datenbank.connectionString;



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

                // Update the Zettel in the database
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

        //CellFactory einrichten um die Zeilen der ListView zu manipulieren.
        zettelList.setCellFactory(param -> new ListCell<>() {
            private final VBox vbRoot = new VBox(); // Use VBox to hold the text components (you can add other components as well)
            private final Label lblHeader = new Label();
            private final Label lblText = new Label();
            private Zettel zettel;

            {
                lblHeader.setStyle("-fx-font-weight: bold;");
                vbRoot.setSpacing(10);
                vbRoot.getChildren().addAll(lblHeader, lblText); // Add the labels to the VBox
            }

            @Override
            protected void updateItem(Zettel z, boolean empty) {
                super.updateItem(z, empty);
                if (z != null && !empty) {
                    lblHeader.setText(z.getHeader());
                    lblText.setText(z.getText());
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
                } else {
                    currentZettelId = null;
                    headerZettel.clear();
                    textZettel.clear();
                }
            }
        });

    }

}



