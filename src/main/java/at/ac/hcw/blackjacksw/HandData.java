package at.ac.hcw.blackjacksw;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;




//  Enthält alle relevanten Daten einer Blackjack-Hand.
//  Karten, gesetzter Einsatz und Statusinformationen (z.B. "Busted", "Blackjack").

class HandData {
    //    Liste der Karten auf der Hand.
    //    Der Einsatz (Bet) für diese Hand.
    //    Statusnachricht der Hand (z.B. "Blackjack!", "Busted").
    ObservableList<Card> cards = FXCollections.observableArrayList();
    IntegerProperty bet = new SimpleIntegerProperty(0);
    StringProperty statusMsg = new SimpleStringProperty("");

//    Berechnet den bestmöglichen Wert der Hand nach Blackjack-Regeln.
//    Asse werden zunächst als 11 gezählt und bei Bedarf als 1 gewertet.

    public int getBestValue() {
        int val = 0;
        int aces = 0;
        // Summiert Kartenwerte und zählt Asse
        for (Card c : cards) {
            val += c.rank.value;
            if (c.rank == Rank.ACE) {
                aces++;
            }
        }
// Falls über 21 reduziert den Wert von Assen (11 → 1)
        while (val > 21 && aces > 0) {
            val -= 10;
            aces--;
        }
        return val;
    }

    //    Prüft, ob die Hand überkauft (busted) ist
    public boolean isBusted() {
        return getBestValue() > 21;
    }

    //    Prüft, ob die Hand ein Blackjack ist.
    public boolean isBlackjack() {
        return cards.size() == 2 && getBestValue() == 21;
    }

    //Prüft, ob die Hand ein gesplitet werden kann
   // public boolean canSplit() {
     //   return cards.size() == 2 && cards.get(0).rank == cards.get(1).rank;}


    public boolean canSplit() {
        // Prüfen: Haben wir 2 Karten? UND: Ist der Wert (z.B. 10) gleich?
        // Das erlaubt jetzt auch König (10) + Dame (10) zu splitten.
        return cards.size() == 2 && cards.get(0).rank.value == cards.get(1).rank.value;
    }
}
