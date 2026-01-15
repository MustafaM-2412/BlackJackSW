module at.ac.hcw.blackjacksw {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.smartcardio;
    requires java.desktop;
    requires javafx.graphics;
    requires javafx.base;


    opens at.ac.hcw.blackjacksw to javafx.fxml;
    exports at.ac.hcw.blackjacksw;
}