package at.ac.hcw.blackjacksw;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

class Styles {
    // Definiert feste Farben für Hintergrund und Spieltisch
    public static final String BG_COLOR = "#262626";
    public static final String TABLE_FILL = "#205e4e";
    public static final String TABLE_STROKE = "#5daea0";
    public static final Color SEAT_EMPTY_COLOR = Color.WHITE;

    // CSS für Design der Place Bet Knöpfe
    public static final String PLACE_BET_BTN_STYLE =
            "-fx-background-color: #95a5a6; " +
                    "-fx-text-fill: white; " +
                    "-fx-background-radius: 15; " +
                    "-fx-font-family: 'Arial'; " +
                    "-fx-font-size: 11px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-padding: 4 12;";

    // Farben für die Chips basierend auf dem Wert
    public static Color getChipColor(int val) {
        switch (val) {
            case 5:
                return Color.web("#e74c3c");
            case 10:
                return Color.web("#e91e63");
            case 25:
                return Color.web("#3498db");
            case 50:
                return Color.web("#9b59b6");
            case 100:
                return Color.web("#f1c40f");
            default:
                return Color.GRAY;
        }
    }

    // Erstellt styled Knopf für Popup - ändert Farbe nach Typ (zb. Confirm - Grün)
    public static Button createModalButton(String text, String type) {
        Button b = new Button(text);
        String bg, txt;

        if (type.equals("CONFIRM")) {
            bg = "#d1f2eb";
            txt = "#117a65";
        } else if (type.equals("CANCEL")) {
            bg = "#fadbd8";
            txt = "#c0392b";
        } else {
            bg = "#ecf0f1";
            txt = "#2c3e50";
        }
        // setzt CSS für Button zusammen
        b.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + txt +
                "; -fx-background-radius: 20; -fx-font-weight: bold; -fx-font-size: 14; " +
                "-fx-padding: 10 30; -fx-cursor: hand;");
        return b;
    }

    // Erstellt "weiße Box" für Popups mit Shadow Effect
    public static VBox createWhiteModalBox() {
        VBox box = new VBox(20);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: white; -fx-background-radius: 20; " +
                "-fx-padding: 30; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 20, 0, 0, 5);");
        box.setMaxSize(600, 500);
        return box;
    }
}