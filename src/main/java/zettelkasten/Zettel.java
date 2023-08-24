package zettelkasten;


import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;

public class Zettel {



    private byte[] zettelId;

    private StringProperty headerProperty = new SimpleStringProperty();
    private StringProperty textProperty = new SimpleStringProperty();

    private LocalDate date;
    public ArrayList<Buzzword> buzzword;

    public Zettel() {
    }


    public Zettel(LocalDate date, ArrayList<Buzzword> buzzword) {
        this.zettelId = generateZettelId();
        this.headerProperty.set("Type something");
        this.textProperty.set("Type something");
        this.date = date;
        this.buzzword = buzzword;
    }

    public Zettel(byte[] zettelId, String header, String text, LocalDate date) {
        this.zettelId = zettelId;
        this.headerProperty.set(header);
        this.textProperty.set(text);
        this.date = date;
        buzzword = new ArrayList<>();
    }

    public StringProperty headerProperty() {
        return headerProperty;
    }

    public String getHeader() {
        return headerProperty.get();
    }

    public void setHeader(String header) {
        headerProperty.set(header);
    }

    public StringProperty textProperty() {
        return textProperty;
    }

    public String getText() {
        return textProperty.get();
    }

    public void setText(String text) {
        textProperty.set(text);
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
                "header='" + headerProperty.get() + '\'' +
                ", text='" + textProperty.get() + '\'' +
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
