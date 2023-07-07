package com.example.zettelkastensb;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import zettelkasten.Datenbank;
import zettelkasten.Zettel;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;


public class HelloController {

    @FXML
    private TextField headerZettel;

    @FXML
    private TextArea textZettel;
    @FXML
    // Methode um neuen Zettel zur DB zettel hinzuzufügen
    void newZettel(ActionEvent event) {
        Zettel z = new Zettel(headerZettel.getText(), textZettel.getText(),
        LocalDate.now(), new ArrayList<>());
        try {
            Datenbank.insertZettel(z);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }




}