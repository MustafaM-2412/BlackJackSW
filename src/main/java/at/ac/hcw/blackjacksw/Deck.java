package at.ac.hcw.blackjacksw;

import javax.smartcardio.Card;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


//kümmert sich um Karten
class Deck {
    private final List<Card> cards = new ArrayList<>();


    // wenn Deck aufgerufen wird, dann wir automatisch refill() gestartet -> Deck sofort Spielbereit

    public Deck() {
        refill();
    }

    // Karten werden wieder zusammengehaut und Deck wird neu gemischt
    public void refill() {
        // Entfernt Karten
        cards.clear();
        // Es werden 6 Kartendecks erzeugt
        for (int i = 0; i < 6; i++) {
            for (Suit s : Suit.values()) {
                for (Rank r : Rank.values()) {
                    cards.add(new Card(s, r));
                }
            }
        }
        // Karten mischen
        Collections.shuffle(cards);
    }


    // man zieht oberste Karte - wenn keine Karten mer dann wir ein neuer Stapel gemischt
    public at.ac.hcw.blackjacksw.Card draw() {
        if (cards.isEmpty()) {
            refill();
        }
        return cards.remove(0);
    }
}