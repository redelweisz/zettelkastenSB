package com.example.zettelkastensb;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import zettelkasten.Buzzword;
import zettelkasten.Datenbank;
import zettelkasten.Zettel;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

import static zettelkasten.Datenbank.connectionString;


public class HelloController {


    @FXML
    private TextField headerZettel;

    @FXML
    private TextArea textZettel;

    @FXML
        // Methode um neuen Zettel zur DB zettel hinzuzufügen
    void newZettel(ActionEvent event) {
        Zettel z = new Zettel(LocalDate.now(), new ArrayList<>());
        try {
            Datenbank.insertZettel(z);
        } catch (SQLException e) {
            throw new RuntimeException(e);
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
}








