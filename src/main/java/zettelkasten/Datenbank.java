package zettelkasten;

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
            //zettelBuzzwordId generieren
            byte[] zettelBuzzwordId = generateZettelBuzzwordId();

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

            // Query um zu überprüfen, ob das Mapping bereits in zettelBuzzwords existiert
            String checkMappingQuery = "SELECT COUNT(*) AS count FROM zettelBuzzwords WHERE ZettelId = ? AND BuzzwordId = ?";
            PreparedStatement checkMappingStatement = connection.prepareStatement(checkMappingQuery);
            checkMappingStatement.setBytes(2, b.getBuzzwordId());

            // Einfügen, wenn nicht
            String zettelBuzzwordInsertQuery = "INSERT INTO zettelBuzzwords (ZettelBuzzwordId, ZettelId, BuzzwordId) VALUES (?, ?, ?)";
            PreparedStatement zettelBuzzwordStatement = connection.prepareStatement(zettelBuzzwordInsertQuery);
            zettelBuzzwordStatement.setBytes(1, zettelBuzzwordId);
            zettelBuzzwordStatement.setBytes(3, b.getBuzzwordId());

            while (resultSet.next()) {
                byte[] zettelId = resultSet.getBytes("ZettelId");
                checkMappingStatement.setBytes(1, zettelId);
                ResultSet checkResult = checkMappingStatement.executeQuery();
                checkResult.next();
                int count = checkResult.getInt("count");

                if (count == 0) {
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
