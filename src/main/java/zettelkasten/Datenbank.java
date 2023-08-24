package zettelkasten;
import com.example.zettelkastensb.HelloController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.nio.ByteBuffer;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


public class Datenbank {

    public static final String connectionString = "jdbc:sqlite:/home/sissi/zettel.db";
    static byte[] selectedBuzzwordId;
    // Methode um neuen Zettel zur DB zettel hinzuzufügen
    public static void insertZettel(Zettel z) throws SQLException {
        try (Connection connection = DriverManager.getConnection(connectionString)) {
            String query = "INSERT INTO zettel (ZettelId, Header, Text, Date) VALUES (?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);


            // SQL statement vorbereiten
            statement.setBytes(1, z.getZettelId());
            statement.setString(2, z.getHeader());
            statement.setString(3, z.getText());
            statement.setString(4, z.getDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)));

            // ausführen
            statement.executeUpdate();

            // schließen
            statement.close();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
// Methode um ausgewählten Zettel zu aus der DB zettel zu löschen
    public static void deleteZettel(byte[] zettelId) throws SQLException {

        try (Connection connection = DriverManager.getConnection(connectionString)) {

            String deleteCollectionsSql = "DELETE FROM zettelCollections WHERE ZettelId = ?";
            String deleteBuzzwordsSql = "DELETE FROM zettelBuzzwords WHERE ZettelId = ?";
            String sql = "DELETE FROM zettel WHERE ZettelId = ?";
            try (PreparedStatement statement = connection.prepareStatement(deleteCollectionsSql)) {
                statement.setBytes(1, zettelId);
                statement.executeUpdate();
                System.out.println("Zettel with the ID " + zettelId.toString() + "deleted from Database zettelCollections");
            }
            try (PreparedStatement deleteBuzzwordsStatement = connection.prepareStatement(deleteBuzzwordsSql)) {
                deleteBuzzwordsStatement.setBytes(1, zettelId);
                deleteBuzzwordsStatement.executeUpdate();
                System.out.println("Connected buzzwords for Zettel with ID " + zettelId.toString() + " deleted from Database zettelBuzzwords");
            }
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setBytes(1, zettelId);
                statement.executeUpdate();
                System.out.println("Zettel with the ID " + zettelId.toString() + "deleted from Database");
            }
        }
    }

//Methode um Buzzword zur Tabelle hinzuzufügen
    public static void insertBuzzword(Buzzword b) {
        try (Connection connection = DriverManager.getConnection(connectionString)) {

            //Query vorbereiten
            String query = "INSERT INTO buzzwords (BuzzwordId, Name) VALUES (?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);

            // SQL statement vorbereiten
            statement.setBytes(1, b.getBuzzwordId());
            statement.setString(2, b.getName());

            // ausführen
            statement.executeUpdate();

            // In headers und text nach buzzword suchen
            String searchText = "%" + b.getName() + "%";
            String searchQuery = "SELECT DISTINCT ZettelId FROM zettel WHERE Header LIKE ? OR Text LIKE ?";
            PreparedStatement searchStatement = connection.prepareStatement(searchQuery);
            searchStatement.setString(1, searchText);
            searchStatement.setString(2, searchText);
            ResultSet resultSet = searchStatement.executeQuery();

            // Query, um zu überprüfen, ob das Mapping bereits in zettelBuzzwords existiert
            String checkMappingQuery = "SELECT COUNT(*) AS count FROM zettelBuzzwords WHERE ZettelId = ? AND BuzzwordId = ?";
            PreparedStatement checkMappingStatement = connection.prepareStatement(checkMappingQuery);
            checkMappingStatement.setBytes(2, b.getBuzzwordId());

            // Einfügen, wenn nicht
            String zettelBuzzwordInsertQuery = "INSERT INTO zettelBuzzwords (ZettelBuzzwordId, ZettelId, BuzzwordId) VALUES (?, ?, ?)";
            PreparedStatement zettelBuzzwordStatement = connection.prepareStatement(zettelBuzzwordInsertQuery);
            zettelBuzzwordStatement.setBytes(3, b.getBuzzwordId());

            while (resultSet.next()) {
                byte[] zettelId = resultSet.getBytes("ZettelId");
                checkMappingStatement.setBytes(1, zettelId);
                ResultSet checkResult = checkMappingStatement.executeQuery();
                checkResult.next();
                int count = checkResult.getInt("count");

                if (count == 0) {
                    zettelBuzzwordStatement.setBytes(1, generateZettelBuzzwordId());
                    zettelBuzzwordStatement.setBytes(2, zettelId);
                    zettelBuzzwordStatement.executeUpdate();
                }
            }
            System.out.println("made entry in zettelBuzzwords");

            zettelBuzzwordStatement.close();
            checkMappingStatement.close();
            searchStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    // Methode um DB nach Zetteln mit dem gesuchten Text zu durchsuchen, und eine OL an die ListView zu liefern.
    public static ObservableList<Zettel> searchZettelByText(String searchText) throws SQLException {
        ObservableList<Zettel> searchResults = FXCollections.observableArrayList();
        System.out.println("Arraylist created");

        try (Connection connection = DriverManager.getConnection(connectionString)) {
            String searchQuery = "SELECT DISTINCT ZettelId, Header, Text, Date FROM zettel WHERE (Header LIKE ? OR Text LIKE ?)";
            PreparedStatement searchStatement = connection.prepareStatement(searchQuery);
            searchStatement.setString(1, "%" + searchText + "%");
            searchStatement.setString(2, "%" + searchText + "%");
            ResultSet resultSet = searchStatement.executeQuery();
            System.out.println("Query executed");
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            while (resultSet.next()) {
                byte[] zettelId = resultSet.getBytes("ZettelId");
                String header = resultSet.getString("Header");
                String text = resultSet.getString("Text");
                String dateString = resultSet.getString("Date");

                LocalDate date = null; // Initialize with a default value or null
                try {
                    // Parse the date if the dateString is not null or empty
                    if (dateString != null && !dateString.isEmpty()) {
                        date = LocalDate.parse(dateString, dateFormatter);
                        System.out.println("Date parsed");
                    }
                } catch (DateTimeParseException ex) {
                    // Handle the exception, log an error, or provide a default value
                    System.out.println("?");
                }
                Zettel zettel = new Zettel();
                zettel.setZettelId(zettelId);
                zettel.setHeader(header);
                zettel.setText(text);
                zettel.setDate(date);
                System.out.println("Zettel Found");
                searchResults.add(zettel);
            }
        }
        return searchResults;
    }
    public static void checkForBuzzwords(Buzzword b, byte[] currentZettelId) {
        try (Connection connection = DriverManager.getConnection(connectionString)) {
            // In headers und text nach buzzword suchen
            String searchText = "%" + b.getName() + "%";
            String searchQuery = "SELECT DISTINCT ZettelId FROM zettel WHERE (Header LIKE ? OR Text LIKE ?) AND ZettelId = ?";
            PreparedStatement searchStatement = connection.prepareStatement(searchQuery);
            searchStatement.setString(1, searchText);
            searchStatement.setString(2, searchText);
            searchStatement.setBytes(3, currentZettelId);
            ResultSet resultSet = searchStatement.executeQuery();

            // Query, um zu überprüfen, ob das Mapping bereits in zettelBuzzwords existiert
            String checkMappingQuery = "SELECT COUNT(*) AS count FROM zettelBuzzwords WHERE ZettelId = ? AND BuzzwordId = ?";
            PreparedStatement checkMappingStatement = connection.prepareStatement(checkMappingQuery);
            checkMappingStatement.setBytes(1, currentZettelId);
            checkMappingStatement.setBytes(2, b.getBuzzwordId());

            // Einfügen, wenn nicht
            String zettelBuzzwordInsertQuery = "INSERT INTO zettelBuzzwords (ZettelBuzzwordId, ZettelId, BuzzwordId) VALUES (?, ?, ?)";
            PreparedStatement zettelBuzzwordStatement = connection.prepareStatement(zettelBuzzwordInsertQuery);
            zettelBuzzwordStatement.setBytes(2, currentZettelId);
            zettelBuzzwordStatement.setBytes(3, b.getBuzzwordId());

            while (resultSet.next()) {
                byte[] zettelId = resultSet.getBytes("ZettelId");
                checkMappingStatement.setBytes(1, zettelId);
                ResultSet checkResult = checkMappingStatement.executeQuery();
                checkResult.next();
                int count = checkResult.getInt("count");

                if (count == 0) {
                    zettelBuzzwordStatement.setBytes(1, generateZettelBuzzwordId());
                    zettelBuzzwordStatement.executeUpdate();
                }
            }
            System.out.println("made entry in zettelBuzzwords");

            zettelBuzzwordStatement.close();
            checkMappingStatement.close();
            searchStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void insertCollection(Collection c) {
        try (Connection connection = DriverManager.getConnection(connectionString)) {

            //Query vorbereiten
            String query = "INSERT INTO collections (CollectionId, Name) VALUES (?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);

            // SQL statement vorbereiten
            statement.setBytes(1, c.getCollectionId());
            statement.setString(2, c.getName());


            // ausführen
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void deleteCollection(byte[] collectionId) throws SQLException {
        try (Connection connection = DriverManager.getConnection(connectionString)) {
            String deleteZettelCollectionSQL = "DELETE FROM zettelCollections WHERE CollectionId = ?";
            String deleteCollectionSQL = "DELETE FROM collections WHERE CollectionId = ?";
            try (PreparedStatement deleteZettelCollectionStatement = connection.prepareStatement(deleteZettelCollectionSQL)) {

                // Zuerst verbundene Einträge in zettelCollections löschen
                deleteZettelCollectionStatement.setBytes(1, collectionId);
                deleteZettelCollectionStatement.executeUpdate();

               // Die collection löschen
                try (PreparedStatement deleteCollectionStatement = connection.prepareStatement(deleteCollectionSQL)) {
                    deleteCollectionStatement.setBytes(1, collectionId);
                    deleteCollectionStatement.executeUpdate();
                    System.out.println("Collection and related zettelCollections entries deleted");
                }
            }
        }
    }

    //Methode um ausgewählten Zettel der Collection hinzuzufügen
    public static void insertIntoCollection(byte[] collectionId, ObservableList<Zettel> selectedZettel) {
        String insertSQL = "INSERT INTO zettelCollections (ZettelCollectionId, CollectionId, ZettelId) VALUES (?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(connectionString);
             PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {

            for (Zettel zettel : selectedZettel) {
                //ZettelId für alle gewählten Zettel holen
                byte[] zettelId = zettel.getZettelId();

                //zettelId and collectionId in die zettelCollections Tabelle einfügen
                pstmt.setBytes(1, generateZettelCollectionId());
                pstmt.setBytes(2, collectionId);
                pstmt.setBytes(3, zettelId);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();

        }
    }

    // Methode um Collections aus DB collections zu lesen und an ListView zu übergeben
    public static ObservableList<Collection> getCollections() throws SQLException {
        ObservableList<Collection> collections = FXCollections.observableArrayList();

        try (Connection connection = DriverManager.getConnection(connectionString)) {
            String sql = "SELECT collectionId, Name FROM collections";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    byte[] collectionId = resultSet.getBytes("collectionId");
                    String name = resultSet.getString("Name");
                    collections.add(new Collection(collectionId, name));
                }
            }
        }

        return collections;
    }


//Methode um Inhalt der Zettel zu holen und an ListViews zu übergeben
    public static ObservableList<Zettel> getZettelData(){
        ObservableList<Zettel> zettelData = FXCollections.observableArrayList();

        try (Connection conn = DriverManager.getConnection(connectionString);
             PreparedStatement stmt = conn.prepareStatement("SELECT ZettelId, Header, Text, Date FROM zettel");
             ResultSet rs = stmt.executeQuery()) {

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

            while (rs.next()) {
                byte[] zettelId = rs.getBytes("ZettelId");
                String header = rs.getString("Header");
                String text = rs.getString("Text");
                String dateString = rs.getString("Date");


                LocalDate date = LocalDate.parse(dateString, dateFormatter);

                Zettel zettel = new Zettel();
                zettel.setZettelId(zettelId);
                zettel.setHeader(header);
                zettel.setText(text);
                zettel.setDate(date);

                zettelData.add(zettel);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return zettelData;
    }

    //Methode um Buzzwords eines Zettels zu holen und an ListViews weiterzugeben
    public static ObservableList<Zettel> getCurrentZettelData(byte[] zettelId) {
        ObservableList<Zettel> currentZettelData = FXCollections.observableArrayList();

        try (Connection conn = DriverManager.getConnection(connectionString);
             PreparedStatement stmt = conn.prepareStatement("SELECT ZettelId, Header, Text, Date FROM zettel WHERE ZettelId = ?")) {

            // Set the zettelId parameter in the query
            stmt.setBytes(1, zettelId);

            ResultSet rs = stmt.executeQuery();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

            while (rs.next()) {
                byte[] fetchedZettelId = rs.getBytes("ZettelId");
                String header = rs.getString("Header");
                String text = rs.getString("Text");
                String dateString = rs.getString("Date");
                LocalDate date = LocalDate.parse(dateString, dateFormatter);

                Zettel zettel = new Zettel();
                zettel.setZettelId(fetchedZettelId);
                zettel.setHeader(header);
                zettel.setText(text);
                zettel.setDate(date);

                currentZettelData.add(zettel);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return currentZettelData;
    }
//Methode um Buzzwords in der Tabelle auszulesen und an ListViews weiterzugeben
    public static ObservableList<Buzzword> getBwData() {
        ObservableList<Buzzword> bwData = FXCollections.observableArrayList();

        try (Connection conn = DriverManager.getConnection(connectionString);
             PreparedStatement stmt = conn.prepareStatement("SELECT BuzzwordId, Name FROM buzzwords");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                byte[] buzzwordIdBytes = rs.getBytes("BuzzwordId");
                String name = rs.getString("Name");

                Buzzword buzzword = new Buzzword(buzzwordIdBytes, name);
                bwData.add(buzzword);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bwData;
    }

    //Methode um mit Zettel verbundene Buzzwords aus der Verbundtabelle zettelBuzzwords zu holen und an ListView zu übergeben
    public static ObservableList<Buzzword> getBwFromZettel(byte[] zettelId) {
        ObservableList<Buzzword> bwDataFromZettel = FXCollections.observableArrayList();

        try (Connection conn = DriverManager.getConnection(connectionString)) {
            String fetchConnectedZettelQuery = "SELECT * FROM zettelBuzzwords WHERE ZettelId = ?";
            try (PreparedStatement fetchConnectedZettelStmt = conn.prepareStatement(fetchConnectedZettelQuery)) {
                fetchConnectedZettelStmt.setBytes(1, zettelId);
                ResultSet resultSet = fetchConnectedZettelStmt.executeQuery();

                while (resultSet.next()) {
                    byte[] buzzwordId = resultSet.getBytes("BuzzwordId");
                    // Buzzword mit ID holen und zur Liste hinzufügen
                    Buzzword buzzword = fetchBuzzwordById(buzzwordId);
                    if (buzzword != null) {
                        bwDataFromZettel.add(buzzword);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bwDataFromZettel;
    }

    public static void updateBuzzwordsForZettel(byte[] currentZettelId, ObservableList<Buzzword> bwDataFromZettel) {
        try (Connection connection = DriverManager.getConnection(connectionString)) {
            // Liste mit buzzwordIds dieses Zettels holen
            List<byte[]> currentBuzzwordIds = new ArrayList<>();
            String getBuzzwordIdsQuery = "SELECT BuzzwordId FROM zettelBuzzwords WHERE ZettelId = ?";
            try (PreparedStatement getBuzzwordIdsStatement = connection.prepareStatement(getBuzzwordIdsQuery)) {
                getBuzzwordIdsStatement.setBytes(1, currentZettelId);
                ResultSet resultSet = getBuzzwordIdsStatement.executeQuery();

                while (resultSet.next()) {
                    byte[] buzzwordId = resultSet.getBytes("BuzzwordId");
                    currentBuzzwordIds.add(buzzwordId);
                }
            }

            // Fehlen buzzwords im Text?
            List<byte[]> buzzwordsToRemove = new ArrayList<>();
            for (byte[] buzzwordId : currentBuzzwordIds) {
                boolean found = false;
                for (Buzzword buzzword : bwDataFromZettel) {
                    if (Arrays.equals(buzzword.getBuzzwordId(), buzzwordId)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    buzzwordsToRemove.add(buzzwordId);
                }
            }

            // Mappings von buzzwords die nicht mehr im Text existieren löschen
            String deleteMappingQuery = "DELETE FROM zettelBuzzwords WHERE ZettelId = ? AND BuzzwordId = ?";
            try (PreparedStatement deleteMappingStatement = connection.prepareStatement(deleteMappingQuery)) {
                deleteMappingStatement.setBytes(1, currentZettelId);
                for (byte[] buzzwordId : buzzwordsToRemove) {
                    deleteMappingStatement.setBytes(2, buzzwordId);
                    deleteMappingStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

//Methode um verbundene Zettel mithilfe der BuzzwordId aus der Komposittabelle zettelBuzzwords zu holen und an ListViews weiterzugeben
    public static ObservableList<Zettel> loadConnectedZettel(byte[] buzzwordId) {
        ObservableList<Zettel> connectedZettel =  FXCollections.observableArrayList();

        try (Connection conn = DriverManager.getConnection(connectionString)) {

            String fetchConnectedZettelQuery = "SELECT * FROM zettelBuzzwords WHERE BuzzwordId = ?";
            try (PreparedStatement fetchConnectedZettelStmt = conn.prepareStatement(fetchConnectedZettelQuery)) {
                fetchConnectedZettelStmt.setBytes(1, buzzwordId);
                ResultSet resultSet = fetchConnectedZettelStmt.executeQuery();

                while (resultSet.next()) {
                    byte[] zettelId = resultSet.getBytes("ZettelId");
                    // Zettel mithilfe von zettelId holen und zu connectedZettel hinzuzufügen
                    Zettel zettel = fetchZettelById(zettelId);
                    if (zettel != null) {
                        connectedZettel.add(zettel);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connectedZettel;
    }
//Methode um Zettel aus der Tabelle auszulesen
    public static Zettel fetchZettelById(byte[] zettelId) {
        Zettel zettel = null;

        try (Connection conn = DriverManager.getConnection(connectionString);
             PreparedStatement stmt = conn.prepareStatement("SELECT Header, Text, Date FROM zettel WHERE ZettelId = ?");
        ) {
            stmt.setBytes(1, zettelId);
            ResultSet rs = stmt.executeQuery();

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

            if (rs.next()) {
                String header = rs.getString("Header");
                String text = rs.getString("Text");
                String dateString = rs.getString("Date");

                LocalDate date = LocalDate.parse(dateString, dateFormatter);

                zettel = new Zettel();
                zettel.setZettelId(zettelId);
                zettel.setHeader(header);
                zettel.setText(text);
                zettel.setDate(date);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return zettel;
    }


    //Methode um Buzzword aus der Tabelle auszulesen
    public static Buzzword fetchBuzzwordById(byte[] buzzwordId) {
        try (Connection conn = DriverManager.getConnection(connectionString)) {
            String fetchBuzzwordQuery = "SELECT * FROM buzzwords WHERE BuzzwordId = ?";
            try (PreparedStatement fetchBuzzwordStmt = conn.prepareStatement(fetchBuzzwordQuery)) {
                fetchBuzzwordStmt.setBytes(1, buzzwordId);
                ResultSet resultSet = fetchBuzzwordStmt.executeQuery();

                if (resultSet.next()) {
                    String name = resultSet.getString("Name");
                    // Create and return the Buzzword object
                    return new Buzzword(buzzwordId, name);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Return null if the Buzzword with the given ID is not found
    }

    //Methode um Zettel aus zettelCollections-DB zu holen und an ListViews weiterzugeben
    public static ObservableList<Zettel> loadZettelFromCollection(byte[] collectionId) {
        ObservableList<Zettel> collectionZettel =  FXCollections.observableArrayList();

        try (Connection conn = DriverManager.getConnection(connectionString)) {

            String fetchCollectionZettelQuery = "SELECT * FROM zettelCollections WHERE CollectionId = ?";
            try (PreparedStatement fetchCollectionZettelStmt = conn.prepareStatement(fetchCollectionZettelQuery)) {
                fetchCollectionZettelStmt.setBytes(1, collectionId);
                ResultSet resultSet = fetchCollectionZettelStmt.executeQuery();

                while (resultSet.next()) {
                    byte[] zettelId = resultSet.getBytes("ZettelId");
                    // Zettel mithilfe von zettelId holen und zu collectionZettel hinzufügen
                    Zettel zettel = fetchZettelById(zettelId);
                    if (zettel != null) {
                        collectionZettel.add(zettel);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return collectionZettel;
    }
    // Methode um Zettel aus zettelCollections zu entfernen
    public static void removeZettelFromCollection(byte[] zettelId) throws SQLException {
        String sql = "DELETE FROM ZettelCollections WHERE ZettelId = ?";
        try (Connection connection = DriverManager.getConnection(connectionString);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setBytes(1, zettelId);
            preparedStatement.executeUpdate();
        }
    }
    //Methode um zettelBuzzwordId zu generieren
    public static byte[] generateZettelBuzzwordId() {
        UUID uuid = UUID.randomUUID();
        ByteBuffer byteBuffer = ByteBuffer.allocate(16);
        byteBuffer.putLong(uuid.getMostSignificantBits());
        byteBuffer.putLong(uuid.getLeastSignificantBits());
        return byteBuffer.array();
    }

    public static byte[] generateZettelCollectionId() {
        UUID uuid = UUID.randomUUID();
        ByteBuffer byteBuffer = ByteBuffer.allocate(16);
        byteBuffer.putLong(uuid.getMostSignificantBits());
        byteBuffer.putLong(uuid.getLeastSignificantBits());
        return byteBuffer.array();
    }


}

