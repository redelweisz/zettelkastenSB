package zettelkasten;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static zettelkasten.Datenbank.connectionString;

public class ZettelUpdater {
    ;

    public void updateZettel(String header, String text, byte[] zettelId) {
        try (Connection conn = DriverManager.getConnection(connectionString);
             PreparedStatement stmt = conn.prepareStatement("UPDATE Zettel SET Header = ?, Text = ? WHERE ZettelId = ?")) {
            stmt.setString(1, header);
            stmt.setString(2, text);
            stmt.setBytes(3, zettelId);
            stmt.executeUpdate();
            System.out.println("Zettel updated successfully.");
        } catch (SQLException e) {
            System.out.println("Error updating zettel: " + e.getMessage());
        }
    }


}