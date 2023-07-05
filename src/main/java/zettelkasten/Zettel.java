package zettelkasten;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

public class Zettel implements Serializable {

    @Serial
    private static final long serialVersionUID = -6322516082598336634L;
    private String header;
    private String text;
    private LocalDate date;
    private Buzzword buzzword;

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

    public Buzzword getBuzzword() {
        return buzzword;
    }

    public void setBuzzword(Buzzword buzzword) {
        this.buzzword = buzzword;
    }
}
