package com.example.zettelkastensb;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import zettelkasten.Buzzword;
import zettelkasten.Datenbank;
import zettelkasten.Zettel;
import zettelkasten.ZettelUpdater;



import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

import static zettelkasten.Datenbank.connectionString;



public class HelloController {

    private byte[] currentZettelId; // Variable zur Speicherung der aktuellen ZettelId

    private byte[] currentBuzzwordId;

    private String selected = "";

    @FXML
    public TextField headerZettel;

    @FXML
    public TextArea textZettel;

    public String getHeaderZettelText() {
        return headerZettel.getText();
    }

    public String getTextZettelText() {
        return textZettel.getText();
    }

    @FXML
        // Methode um neuen Zettel zur DB zettel hinzuzufügen
    void newZettel(ActionEvent event) {
        Zettel z = new Zettel(LocalDate.now(), new ArrayList<>());
        try {
            Datenbank.insertZettel(z);
            System.out.println("New Zettel generated");
            currentZettelId = z.getZettelId(); // Speichere die aktuelle ZettelId in der Variable
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
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateZettelInDatabase() {
        String header = getHeaderZettelText();
        String text = getTextZettelText();
        if (currentZettelId != null) {
            ZettelUpdater zettelUpdater = new ZettelUpdater();
            zettelUpdater.updateZettel(header, text, currentZettelId);
        } else {
            System.out.println("Keine aktuelle ZettelId vorhanden.");
        }
    }


    @FXML
    void saveOnChangeZettel(KeyEvent event){
        if (currentZettelId != null) { // Überprüfe, ob eine aktuelle ZettelId vorhanden ist
            updateZettelInDatabase();
        }
    }
}









