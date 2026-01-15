package at.ac.hcw.blackjacksw;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

class SeatModel {

    ObservableList<HandData> hands = FXCollections.observableArrayList();
    //5 seats activeHandIndex zeigt auf den seat und welche hand ihn spielt
    IntegerProperty activeHandIndex = new SimpleIntegerProperty(0);
    //wird auf dieser hand gespielt ja nein / true false
    BooleanProperty isActiveSeat = new SimpleBooleanProperty(false);

    public SeatModel() {
        reset();
    }

    //ein reset fordert, alle hände werden gecleard der zeiger der handfokus wird auf 0 gesetzt und active auf false
    public void reset() {
        hands.clear();
        hands.add(new HandData());
        activeHandIndex.set(0);
        isActiveSeat.set(false);
    }

    //gibt die allerste hand
    public HandData getMainHand() {
        return hands.isEmpty() ? null : hands.get(0);
    }

    public HandData getCurrentHand() {
        if (activeHandIndex.get() < hands.size()) {
            return hands.get(activeHandIndex.get());
        }
        return null;
    }

    public void split() {
        //ist eine hand da?
        if (hands.isEmpty()) return;

        //nimm die erste hand
        HandData h1 = hands.get(0);
        //man braucht 2 karten um zu splitten
        if (h1.cards.size() < 2) return;

        //nimm, die zweite karte aus der hand
        Card splitCard = h1.cards.remove(1);

        //erstelle eine neue hand
        HandData h2 = new HandData();
        //einsatz muss der gleiche sein
        h2.bet.set(h1.bet.get());
        //lege die zweite weggnommene karte in hand 2
        h2.cards.add(splitCard);
        //füg hand2 in die liste der hände
        hands.add(h2);
    }
}