package zettelkasten;

import com.example.zettelkastensb.HelloController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.nio.ByteBuffer;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
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
    public static ObservableList<Zettel> getZettelBwData(byte[] zettelId) {
        ObservableList<Zettel> zettelBwData = FXCollections.observableArrayList();

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

                zettelBwData.add(zettel);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return zettelBwData;
    }

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
        System.out.println("BWData returned");
        return bwData;

    }

// Implement the method to fetch connected Zettel objects from the 'zettelBuzzwords' table

    public static ObservableList<Zettel> loadConnectedZettels(byte[] buzzwordId) {
        ObservableList<Zettel> connectedZettels =  FXCollections.observableArrayList();

        // Connect to the SQLite database

        try (Connection conn = DriverManager.getConnection(connectionString)) {

            String fetchConnectedZettelsQuery = "SELECT * FROM zettelBuzzwords WHERE BuzzwordId = ?";
            try (PreparedStatement fetchConnectedZettelsStmt = conn.prepareStatement(fetchConnectedZettelsQuery)) {
                fetchConnectedZettelsStmt.setBytes(1, buzzwordId);
                ResultSet resultSet = fetchConnectedZettelsStmt.executeQuery();

                while (resultSet.next()) {
                    byte[] zettelId = resultSet.getBytes("ZettelId");
                    // Fetch the Zettel object based on the zettelId and add it to the connectedZettels list
                    Zettel zettel = fetchZettelById(zettelId);
                    if (zettel != null) {
                        connectedZettels.add(zettel);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return connectedZettels;
    }

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

    public static ObservableList<Buzzword> getBwFromZettel(byte[] zettelId) {
        ObservableList<Buzzword> bwDataFromZettel = FXCollections.observableArrayList();

        try (Connection conn = DriverManager.getConnection(connectionString)) {
            String fetchConnectedZettelsQuery = "SELECT * FROM zettelBuzzwords WHERE ZettelId = ?";
            try (PreparedStatement fetchConnectedZettelsStmt = conn.prepareStatement(fetchConnectedZettelsQuery)) {
                fetchConnectedZettelsStmt.setBytes(1, zettelId);
                ResultSet resultSet = fetchConnectedZettelsStmt.executeQuery();

                while (resultSet.next()) {
                    byte[] buzzwordId = resultSet.getBytes("BuzzwordId");
                    // Fetch the Buzzword object based on the buzzwordId and add it to the bwDataFromZettel list
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
    public void updateZettel(String header, String text, byte[] zettelId) {
        try (Connection conn = DriverManager.getConnection(connectionString);
             PreparedStatement stmt = conn.prepareStatement("UPDATE zettel SET Header = ?, Text = ? WHERE ZettelId = ?")) {
            stmt.setString(1, header);
            stmt.setString(2, text);
            stmt.setBytes(3, zettelId);
            stmt.executeUpdate();
            System.out.println("Zettel updated successfully.");
        } catch (SQLException e) {
            System.out.println("Error updating zettel: " + e.getMessage());
        }
    }

    public static byte[] generateZettelBuzzwordId() {
        UUID uuid = UUID.randomUUID();
        ByteBuffer byteBuffer = ByteBuffer.allocate(16);
        byteBuffer.putLong(uuid.getMostSignificantBits());
        byteBuffer.putLong(uuid.getLeastSignificantBits());
        return byteBuffer.array();
    }


    /*public static Zettel readZettel(byte[] zettelId){
        try (Connection connection = DriverManager.getConnection(connectionString)) {
            String query = "SELECT * FROM zettel WHERE ZettelId = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setBytes(1, zettelId);
            ResultSet rs = statement.executeQuery();

            if(rs.next()){
                Zettel z =  new Zettel(rs.getBytes("ZettelId"), rs.getString("Header"), rs.getString("Text"),
                        LocalDate.parse(rs.getString("Date"), DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)));
            }


            // schließen
            statement.close();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

*/
}
