module com.example.zettelkastensb {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
            
    requires org.controlsfx.controls;
    requires java.sql;

    opens com.example.zettelkastensb to javafx.fxml;
    exports com.example.zettelkastensb;
}