package zettelkasten;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class ZettelManager {
    private Connection connection;


    private static final String ZETTEL_ID = "ZettelId";
    private static final String ZETTEL_HEADER = "Header";
    private static final String ZETTEL_TEXT = "Text";
    private static final String ZETTEL_DATE = "Date";


    // Constructor für DB Connection
    public ZettelManager(String databasePath) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:/home/sissi/zettel.db");
    }

    // Methode um neuen Zettel zur DB zettel hinzuzufügen
    public void insertZettel(byte[] uuidBytes) throws SQLException {

        connection = DriverManager.getConnection("jdbc:sqlite:/home/sissi/zettel.db");
        String query = "INSERT INTO zettel (ZettelId, Header, Text, Date) VALUES (?, ?, ?, ?)";
        PreparedStatement statement = connection.prepareStatement(query);

        // Datum holen
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = dateFormat.format(new Date());

        // SQL statement vorbereiten
        statement.setBytes(1, uuidBytes);
        statement.setString(2, "Header value");
        statement.setString(3, "Text value");
        statement.setString(4, date);

        // ausführen
        statement.executeUpdate();

        // schließen
        statement.close();
    }

    // Id-Generator
    private byte[] generateZettelId() {
        UUID uuid = UUID.randomUUID();
        ByteBuffer byteBuffer = ByteBuffer.allocate(16);
        byteBuffer.putLong(uuid.getMostSignificantBits());
        byteBuffer.putLong(uuid.getLeastSignificantBits());
        return byteBuffer.array();
    }

        // DB-Verbindung schließen
        public void closeConnection () throws SQLException {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        }



    // toString methode
    private static String bytesToHexString(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : bytes) {
            stringBuilder.append(String.format("%02X", b));
        }
        return stringBuilder.toString();
    }
}
