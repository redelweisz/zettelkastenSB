module com.example.zettelkastensb {
    requires javafx.controls;
    requires javafx.fxml;
        requires javafx.web;
            
        requires org.controlsfx.controls;
                            requires eu.hansolo.tilesfx;
        
    opens com.example.zettelkastensb to javafx.fxml;
    exports com.example.zettelkastensb;
}