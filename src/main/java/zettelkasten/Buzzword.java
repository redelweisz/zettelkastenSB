package zettelkasten;

import java.nio.ByteBuffer;
import java.util.UUID;

public class Buzzword {

    private byte[] buzzwordId;
    private String name;

    public Buzzword(String name) {
        this.buzzwordId = generateBuzzwordId();
        this.name = name;
    }

    public Buzzword(byte[] buzzwordId, String name) {
        this.buzzwordId = buzzwordId;
        this.name = name;
    }

    public byte[] getBuzzwordId() {

        return buzzwordId;
    }

    public void setBuzzwordId(byte[] buzzwordId) {

        this.buzzwordId = buzzwordId;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    @Override
    public String toString() {

        return name;
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
