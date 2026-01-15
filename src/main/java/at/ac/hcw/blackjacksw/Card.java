package at.ac.hcw.blackjacksw;
// Repräsentiert eine einzelne Spielkarte im Blackjack.
//  Welche aus einer Frabe (suit) und einem Rang(rank) (Herz Ass, Karo Köning usw.)


class Card {
    final Suit suit;
    final Rank rank;

    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }
}