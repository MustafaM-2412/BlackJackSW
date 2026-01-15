package blackjack;

import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;


class ChipView extends StackPane {
    public ChipView(int val, double size, boolean selectable) {
        // Größe setzten vom Chip
        setPrefSize(size, size);

        // Äußerer Rand Design - Farbe nach value, Weißer Rand, Schatten
        Circle c = new Circle(size / 2);
        c.setFill(Styles.getChipColor(val));
        c.setStroke(Color.WHITE);
        c.setStrokeWidth(3);
        c.setEffect(new DropShadow(3, Color.rgb(0, 0, 0, 0.4)));

        // Innen Design - durchsichtig, weißer Rand, gestrichelte Linie
        Circle inner = new Circle(size / 2 - 5);
        inner.setFill(Color.TRANSPARENT);
        inner.setStroke(Color.WHITE);
        inner.setStrokeWidth(1);
        inner.getStrokeDashArray().addAll(4d, 4d);

        // Text einstellen der den WERT vom Chip anzeigt
        Text t = new Text(String.valueOf(val));
        t.setFill(Color.WHITE);
        t.setFont(Font.font("Arial", FontWeight.BOLD, size * 0.4));


        // zusammenfügen von allen oberen Elementen
        getChildren().addAll(c, inner, t);


        // wenn Chip is selectable - HOWER EFFECT
        if (selectable) {

            // wenn Mauszeiger über Chip ist -> wird Zeiger zur HAND
            setCursor(javafx.scene.Cursor.HAND);

            // Maus über Chip = Chip wird größer
            setOnMouseEntered(e -> {
                setScaleX(1.1);
                setScaleY(1.1);
            });

            // Maus geht weg von Chip -> Chips werden wieder zur Normalgröße
            setOnMouseExited(e -> {
                setScaleX(1.0);
                setScaleY(1.0);
            });
        }
    }
}