package zettelkasten;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

public class Datenbank {
    private static final String connectionString = "jdbc:sqlite:/home/sissi/zettel.db";
    // Methode um neuen Zettel zur DB zettel hinzuzufügen

    public static void insertZettel(Zettel z) throws SQLException {
        try (Connection connection = DriverManager.getConnection(connectionString)) {
            String query = "INSERT INTO zettel (ZettelId, Header, Text, Date) VALUES (?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);


            // SQL statement vorbereiten
            statement.setBytes(1, z.getZettelId());
            statement.setString(2, z.getHeader());
            statement.setString(3, z.getText());
            LocalDateTime dt = LocalDateTime.of(z.getDate(), LocalTime.of(0, 0, 0, 0));
            java.sql.Date date = new java.sql.Date(dt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            statement.setDate(4, date);

            // ausführen
            statement.executeUpdate();

            // schließen
            statement.close();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
}
