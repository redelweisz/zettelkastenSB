package zettelkasten;


import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;

public class Zettel {



    private byte[] zettelId;

    private String header;
    private String text;
    private LocalDate date;
    public ArrayList<Buzzword> buzzword;

    public Zettel() {
    }

    public Zettel(LocalDate date, ArrayList<Buzzword> buzzword) {
        this.zettelId = generateZettelId();
        this.header = "Type something";
        this.text = "Type something";
        this.date = date;
        this.buzzword = buzzword;
    }

    public Zettel(byte[] zettelId, String header, String text, LocalDate date) {
        this.zettelId = zettelId;
        this.header = header;
        this.text = text;
        this.date = date;
        buzzword = new ArrayList<>();
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public ArrayList<Buzzword> getBuzzword() {
        return buzzword;
    }

    public void setBuzzword(ArrayList<Buzzword> buzzword) {
        this.buzzword = buzzword;
    }

    public byte[] getZettelId() {
        return zettelId;
    }

    public void setZettelId(byte[] zettelId) {
        this.zettelId = zettelId;
    }

    // Id-Generator
    public byte[] generateZettelId() {
        UUID uuid = UUID.randomUUID();
        ByteBuffer byteBuffer = ByteBuffer.allocate(16);
        byteBuffer.putLong(uuid.getMostSignificantBits());
        byteBuffer.putLong(uuid.getLeastSignificantBits());
        return byteBuffer.array();
    }

    @Override
    public String toString() {
        return "Zettel{" +
                "header='" + header + '\'' +
                ", text='" + text + '\'' +
                '}';
    }

    // toString methode ID
    private static String bytesToHexString(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : bytes) {
            stringBuilder.append(String.format("%02X", b));
        }
        return stringBuilder.toString();
    }
}
