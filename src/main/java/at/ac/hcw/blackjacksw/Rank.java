package at.ac.hcw.blackjacksw;

public enum Rank {
    //Enum definiert alle möglichen Werte der Spielkarten ; verknüpft Karten mit ihrem Punktewert (Queen=10)
    TWO(2, "2"),
    THREE(3, "3"),
    FOUR(4, "4"),
    FIVE(5, "5"),
    SIX(6, "6"),
    SEVEN(7, "7"),
    EIGHT(8, "8"),
    NINE(9, "9"),
    TEN(10, "T"),
    JACK(10, "J"),
    QUEEN(10, "Q"),
    KING(10, "K"),
    ACE(11, "A");


    // speichert Punktewert der Karte
    final int value;
    final String symbol;


    //Weist Konstante der nummerischen Wert zu
    Rank(int v, String symbol) {
        value = v;

        this.symbol = symbol;
    }

    /* kürzt Anzeige ab:
    bei Zahlenwerten werden die Zahlen genommen
    bei Bildern und 10 wird nur der Anfangsbuchstabe genommen
     */
    public String shortName() {
        return symbol;
    }
}
