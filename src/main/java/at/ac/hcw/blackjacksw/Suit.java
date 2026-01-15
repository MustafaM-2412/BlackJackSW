package at.ac.hcw.blackjacksw;

//enum für "fantasie" werte
enum Suit {
    HEARTS, DIAMONDS, CLUBS, SPADES;

    //herzen und diamond sind automatisch rot rest wird schwarz
    public boolean isRed() {
        return this == HEARTS || this == DIAMONDS;
    }

    //symbol bekommt ein String value je nach symbol
    public String symbol() {
        return switch (this) {
            case HEARTS -> "♥";
            case DIAMONDS -> "♦";
            case CLUBS -> "♣";
            default -> "♠";
        };
    }
}