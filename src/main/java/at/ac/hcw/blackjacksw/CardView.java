package at.ac.hcw.blackjacksw;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

//Grafische Darstellung einer Spielkarte.


class CardView extends StackPane {

//  Erstellt eine neue Kartenansicht.
// Wenn card == null ist, wird die karte verdeckt dargestellt.

    public CardView(Card card) {
        // Setzt die feste Größe der Karte
        setPrefSize(60, 88);
        // Hintergrund der Karte
        Rectangle bg = new Rectangle(60, 88);
        bg.setArcWidth(8);
        bg.setArcHeight(8);
        bg.setEffect(new DropShadow(3, Color.BLACK));
        // Fall: verdeckte Karte
        if (card == null) {
            bg.setFill(Color.web("#3b6fa6"));// Kartenrücken-Farbe
            bg.setStroke(Color.WHITE);          // Weißer Rand
            getChildren().add(bg);

            // Fall: offene Karte
        } else {
            bg.setFill(Color.WHITE);

            // Text für den Kartenrang (A, K, Q, J, T, 2–9)
            Text rank = new Text(card.rank.shortName());
            rank.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            rank.setFill(card.suit.isRed() ? Color.RED : Color.BLACK);

            // Text für das Kartensymbol (♠ ♥ ♦ ♣)
            Text suit = new Text(card.suit.symbol());
            suit.setFont(Font.font("Arial", 24));
            suit.setFill(card.suit.isRed() ? Color.RED : Color.BLACK);

            // Zentrierter Container für das Kartensymbol
            VBox center = new VBox(suit);
            center.setAlignment(Pos.CENTER);

            // Elemente zur Karte hinzufügen
            getChildren().addAll(bg, center, rank);

            // Rang oben links platzieren
            StackPane.setAlignment(rank, Pos.TOP_LEFT);
            StackPane.setMargin(rank, new Insets(4));
        }

    }

}