package zettelkasten;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;

public class Collection {
    private byte[] collectionId;
    private String name;
    private ObservableList<Zettel> zettelCollection;

    public Collection(byte[] collectionId, String name) {
        this.collectionId = collectionId;
        this.name = name;
        this.zettelCollection = FXCollections.observableArrayList();
    }

    public ObservableList<Zettel> getZettelCollection() {
        System.out.println(zettelCollection.toString());
        return zettelCollection;

    }

    public void setZettelCollection(ObservableList<Zettel> zettelCollection) {
        this.zettelCollection = zettelCollection;
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

    public void addZettel(Zettel zettel) {
        zettelCollection.add(zettel);
    }

    public void addAll(List<Zettel> zettelsToAdd) {
        zettelCollection.addAll(zettelsToAdd);
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
