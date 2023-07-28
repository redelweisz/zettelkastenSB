package zettelkasten;

import java.nio.ByteBuffer;
import java.util.UUID;

public class Collection {
    private byte[] collectionId;

    private String name;

    public Collection(byte[] collectionId, String name) {
        this.collectionId = generateCollectionId();
        this.name = name;
    }

    public byte[] getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(byte[] collectionId) {
        this.collectionId = collectionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void createCollection(){

    }

    public void deleteCollection(){

    }

    public void addZettel(){

    }



    public static byte[] generateCollectionId() {
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
