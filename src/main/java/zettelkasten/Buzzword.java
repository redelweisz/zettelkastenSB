package zettelkasten;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.nio.ByteBuffer;
import java.util.UUID;

public class Buzzword {

    private byte[] buzzwordId;
    private StringProperty nameProperty = new SimpleStringProperty();

    public Buzzword(String name) {
        this.buzzwordId = generateBuzzwordId();
        this.nameProperty.set(name);
    }

    public Buzzword(byte[] buzzwordId, String name) {
        this.buzzwordId = buzzwordId;
        this.nameProperty.set(name);
    }

    public byte[] getBuzzwordId() {

        return buzzwordId;
    }

    public void setBuzzwordId(byte[] buzzwordId) {

        this.buzzwordId = buzzwordId;
    }

    public StringProperty nameProperty() {
        return nameProperty;
    }

    public String getName() {
        return nameProperty.get();
    }

    public void setName(String name) {
        nameProperty.set(name);
    }

    @Override
    public String toString() {
        return getName();
    }


    // Id-Generator
    public byte[] generateBuzzwordId() {
        UUID uuid = UUID.randomUUID();
        ByteBuffer byteBuffer = ByteBuffer.allocate(16);
        byteBuffer.putLong(uuid.getMostSignificantBits());
        byteBuffer.putLong(uuid.getLeastSignificantBits());
        return byteBuffer.array();
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
