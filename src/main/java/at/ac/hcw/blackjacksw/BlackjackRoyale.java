package at.ac.hcw.blackjacksw;

import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.awt.*;

public class BlackjackRoyale extends Application {

    private Deck deck = new Deck();
    private IntegerProperty balance = new SimpleIntegerProperty(1000);
    private ObjectProperty<GameState> gameState = new SimpleObjectProperty<>(GameState.START_SCREEN);

    private SeatModel[] seats = new SeatModel[5];
    private HandData dealerHand = new HandData();

    private StackPane rootLayer;
    private AnchorPane tableLayer;
    private StackPane overlayLayer;

    private HBox dealerCardBox;
    private Label dealerScoreLbl;
    private Label messageLbl;
    private Button btnDeal;

    private Timeline timer;
    private IntegerProperty timeLeft = new SimpleIntegerProperty(60);

    private Pane[] seatVisualContainers = new Pane[5];
    private HBox[] seatCardHBoxes = new HBox[5];

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        for (int i = 0; i < 5; i++) {
            seats[i] = new SeatModel();
        }

        rootLayer = new StackPane();
        rootLayer.setStyle("-fx-background-color: " + Styles.BG_COLOR + ";");
        tableLayer = new AnchorPane();
        overlayLayer = new StackPane();
        overlayLayer.setPickOnBounds(false);

        buildTable();
        rootLayer.getChildren().addAll(tableLayer, overlayLayer);
        showStartScreen();

        Scene scene = new Scene(rootLayer, 1024, 640);
        stage.setTitle("BlackJack Royale 2025 - Final");
        stage.setScene(scene);
        stage.show();
    }

    private void buildTable() {
        // Tisch erstellen (Ellipse) und Stil setzen
        Ellipse table = new Ellipse(512, 320, 480, 270);
        table.setFill(Paint.valueOf(Styles.TABLE_FILL));
        table.setStroke(Paint.valueOf(Styles.TABLE_STROKE));
        table.setStrokeWidth(8);

        // Tisch in Container packen und zur TableLayer hinzufügen
        StackPane tableContainer = new StackPane(table);
        tableContainer.setPrefSize(1024, 640);
        tableLayer.getChildren().add(tableContainer);

        // Exit-Button erstellen
        Button exit = new Button("Exit");
        exit.setStyle("-fx-background-color: white; -fx-text-fill: #c0392b; " +
                "-fx-background-radius: 20; -fx-font-weight: bold; -fx-cursor: hand;");
        exit.setOnAction(e -> Platform.exit());

        // Rules-Button erstellen
        Button rules = new Button("Rules");
        rules.setStyle("-fx-background-color: white; -fx-text-fill: #2c3e50; " +
                "-fx-background-radius: 20; -fx-font-weight: bold; -fx-cursor: hand;");
        rules.setOnAction(e -> showRules());

        // Buttons positionieren
        AnchorPane.setTopAnchor(exit, 30.0);
        AnchorPane.setLeftAnchor(exit, 30.0);
        AnchorPane.setTopAnchor(rules, 30.0);
        AnchorPane.setRightAnchor(rules, 30.0);

        // Info-Box für Hinweise, Balance und Deal-Button
        VBox info = new VBox(8);
        info.setAlignment(Pos.CENTER);

        // Info: Auszahlungstext
        Label pay = new Label("Blackjack pays 3:2");
        pay.setTextFill(Color.web("#8daead"));
        pay.setFont(Font.font("Arial", 16));

        // Label für Nachrichten im Spiel
        messageLbl = new Label();
        messageLbl.setTextFill(Color.WHITE);
        messageLbl.setFont(javafx.scene.text.Font.font("Arial", 16));

        // Deal-Button erstellen (anfangs unsichtbar)
        btnDeal = new Button("DEAL NOW");
        btnDeal.setStyle("-fx-background-color: #f1c40f; -fx-text-fill: black; -fx-font-weight: bold; " +
                "-fx-background-radius: 20; -fx-padding: 5 15; -fx-cursor: hand; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 5, 0, 0, 1);");
        btnDeal.setVisible(false);
        btnDeal.setOnAction(e -> dealCardsSequence());

        info.getChildren().addAll(pay, messageLbl, btnDeal);
        info.setLayoutX(440);
        info.setLayoutY(70);

        // Label für Dealer
        Label dTag = new Label("Dealer");
        dTag.setStyle("-fx-background-color: #dcece8; -fx-text-fill: #2c3e50; " +
                "-fx-padding: 4 12; -fx-background-radius: 12; -fx-font-weight: bold;");
        dTag.setLayoutX(485);
        dTag.setLayoutY(160);

        // Label für Dealer-Punktzahl
        dealerScoreLbl = new Label();
        dealerScoreLbl.setTextFill(Color.WHITE);
        dealerScoreLbl.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        dealerScoreLbl.setLayoutX(500);
        dealerScoreLbl.setLayoutY(140);

        // Box für Dealer-Karten
        dealerCardBox = new HBox(-20);
        dealerCardBox.setAlignment(Pos.CENTER);
        dealerCardBox.setLayoutX(460);
        dealerCardBox.setLayoutY(190);

        // Sitze erstellen und positionieren
        double[][] pos = {{150, 300}, {280, 380}, {470, 420}, {660, 380}, {790, 300}};
        for (int i = 0; i < 5; i++) {
            createSeatUI(i, pos[i][0], pos[i][1]);
        }

        // Label für Spieler-Balance
        Label bal = new Label();
        bal.textProperty().bind(Bindings.concat("Your Balance: ", balance));
        bal.setTextFill(Color.WHITE);
        bal.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        AnchorPane.setBottomAnchor(bal, 20.0);
        AnchorPane.setLeftAnchor(bal, 30.0);

        // Alle Elemente zum TableLayer hinzufügen
        tableLayer.getChildren().addAll(exit, rules, info, dTag, dealerScoreLbl, dealerCardBox, bal);
    }

    //UI für jeden sitzplatz einzeln
    private void createSeatUI(int idx, double x, double y) {
        SeatModel seat = seats[idx];
        //groupiert
        Pane seatGroup = new Pane();
        seatGroup.setLayoutX(x);
        seatGroup.setLayoutY(y);
        seatVisualContainers[idx] = seatGroup;


        Circle bg = new Circle(40);
        bg.setFill(Styles.SEAT_EMPTY_COLOR);
        bg.setLayoutX(40);
        bg.setLayoutY(40);

        Label pb = new Label("Place Bet");
        pb.setStyle(Styles.PLACE_BET_BTN_STYLE);
        pb.setLayoutX(4);
        pb.setLayoutY(90);

        //klick
        StackPane interact = new StackPane();
        interact.setPrefSize(80, 80);
        interact.setCursor(javafx.scene.Cursor.HAND);
        interact.setOnMouseClicked(e -> {
            //wenn bettingphase dann wettfenster
            if (gameState.get() == GameState.BETTING) showBetModal(idx);
        });


        //updatemethode
        seat.hands.addListener((ListChangeListener.Change<? extends HandData> c) -> updateSeatVisuals(idx));
        seat.getMainHand().bet.addListener((o, old, v) -> updateSeatVisuals(idx));

        //Kartenanzeige
        HBox cardsArea = new HBox(20);
        cardsArea.setAlignment(Pos.CENTER);
        cardsArea.setLayoutX(-60);
        cardsArea.setLayoutY(-120);
        cardsArea.setPrefWidth(200);
        seatCardHBoxes[idx] = cardsArea;

        //Hit stand etc bzw abstand
        HBox actions = new HBox(8);
        actions.setLayoutX(-50);
        actions.setLayoutY(120);
        actions.setPrefWidth(180);
        actions.setAlignment(Pos.CENTER);

        Button hit = new Button("Hit");
        Button stand = new Button("Stand");
        Button dbl = new Button("Double");
        Button split = new Button("Split");

        String actionStyle = "-fx-base: #ecf0f1; -fx-text-fill: black; -fx-background-radius: 15; " +
                "-fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand; -fx-min-width: 50;";

        hit.setStyle(actionStyle);
        stand.setStyle(actionStyle);
        dbl.setStyle(actionStyle);
        split.setStyle(actionStyle);

        hit.setOnAction(e -> handleHit(idx));
        stand.setOnAction(e -> handleStand(idx));
        dbl.setOnAction(e -> handleDouble(idx));
        split.setOnAction(e -> handleSplit(idx));

        actions.getChildren().addAll(hit, stand, dbl, split);

        //listener für den jetzigen sitz
        seat.isActiveSeat.addListener((o, old, active) -> {
            if (!active) actions.setVisible(false);
            else updateActionButtons(idx, actions, hit, dbl, split);
        });
        actions.setVisible(false);

        // Alle Elemente zum Sitz-Group-Pane hinzufügen
        seatGroup.getChildren().addAll(bg, pb, interact, cardsArea, actions);
        tableLayer.getChildren().add(seatGroup);
    }

    /*
     * Steuert die Buttons (Hit, Stand, Double, Split).
     * Aktiviert oder deaktiviert sie basierend auf den Spielregeln und dem Guthaben.
     */
    private void updateActionButtons(int idx, HBox container, Button hitBtn, Button dblBtn, Button splitBtn) {
        SeatModel s = seats[idx];
        // Abbruch, wenn der Sitz gar nicht am Zug ist
        if (!s.isActiveSeat.get()) return;

        HandData currentHand = s.getCurrentHand();
        if (currentHand == null) return;

        container.setVisible(true);

        // Prüft, ob der Spieler schon 21 oder mehr Punkte hat
        boolean is21 = currentHand.getBestValue() >= 21;

        // Regel: Wer 21 hat, darf keine Karte mehr ziehen
        hitBtn.setDisable(is21);

        // Regel: Double Down geht nur bei den ersten 2 Karten, wenn man genug Geld hat und noch nicht bei 21 ist.
        dblBtn.setDisable(is21 || balance.get() < currentHand.bet.get() || currentHand.cards.size() > 2);

        // Regel: Split ist erlaubt, wenn:
        // 1. Die Karten den gleichen Rang haben (z.B. 8 und 8)
        // 2. Genug Geld für den zweiten Einsatz da ist
        // 3. Man nicht bereits gesplittet hat (wir erlauben nur max. 2 Hände)
        boolean canSplit = currentHand.canSplit() &&
                balance.get() >= currentHand.bet.get() &&
                s.hands.size() == 1;

        // Zeige den Split-Button nur an, wenn er auch benutzt werden dar
        splitBtn.setVisible(canSplit);
        splitBtn.setManaged(canSplit);
    }

    /*
     * Methode für Grafik-Updates  Sitzplatzes.
     * Synchronisiert das Datenmodell (SeatModel) mit der Anzeige (Pane).
     * Wird immer aufgerufen, wenn sich Daten ändern.
     */
    private void updateSeatVisuals(int idx) {
        SeatModel s = seats[idx];
        Pane group = seatVisualContainers[idx];

        // Zugriff auf die UI-Elemente im Container (Chips, Labels, Kartenbereich)
        StackPane interact = (StackPane) group.getChildren().get(2);
        Label pb = (Label) group.getChildren().get(1);
        HBox cardsArea = (HBox) group.getChildren().get(3);

        //  Chips und Einsatzanzeige
        interact.getChildren().clear();
        int totalBet = s.hands.stream().mapToInt(h -> h.bet.get()).sum();

        if (totalBet > 0) {
            pb.setVisible(false); // Verstecke "Place Bet" Text
            ChipView cv = new ChipView(totalBet > 0 ? s.getMainHand().bet.get() : 0, 45, false);
            Label l = new Label("Bet: " + totalBet);
            l.setStyle("-fx-text-fill: white; -fx-effect: dropshadow(one-pass-box, black, 2,0,0,1);");
            l.setTranslateY(150);
            interact.getChildren().addAll(cv, l);

            // Wenn wir in der Wettphase sind und gewettet wurde -> Deal Button anzeigen
            if (gameState.get() == GameState.BETTING) btnDeal.setVisible(true);
        } else {
            pb.setVisible(true); // Zeige "Place Bet", wenn kein Einsatz da ist
        }

        cardsArea.getChildren().clear();

        // 2. Karten zeichnen (Schleife, weil es durch Split mehrere Hände geben kann)
        cardsArea.getChildren().clear();
        for (int hIdx = 0; hIdx < s.hands.size(); hIdx++) {
            HandData h = s.hands.get(hIdx);
            VBox handCol = new VBox(5);
            handCol.setAlignment(Pos.CENTER);

            // Punktestand anzeigen
            Label score = new Label(h.cards.isEmpty() ? "" : String.valueOf(h.getBestValue()));
            score.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-effect: dropshadow(one-pass-box, black, 2,0,0,1);");

            // Kartenstapel (negativer Abstand sorgt für Überlappung)
            HBox pile = new HBox(-35);
            pile.setAlignment(Pos.CENTER);
            for (Card c : h.cards) {
                pile.getChildren().add(new CardView(c));
            }
            // Status-Text (z.B. "WIN" oder "BUST")
            Label status = new Label();
            status.textProperty().bind(h.statusMsg);
            status.setStyle("-fx-text-fill: #f1c40f; -fx-font-weight: bold; -fx-font-size: 10px; -fx-effect: dropshadow(one-pass-box, black, 2,0,0,1);");
            // Wenn diese Hand gerade aktiv gespielt wird -> Goldener Schein
            if (s.isActiveSeat.get() && s.activeHandIndex.get() == hIdx) {
                pile.setStyle("-fx-effect: dropshadow(three-pass-box, gold, 15, 0, 0, 0);");
            }

            handCol.getChildren().addAll(score, pile, status);
            cardsArea.getChildren().add(handCol);
        }
    }

    /*
     * Erstellt das Startmenü (Overlay).
     * Legt sich über das Spielfeld und macht den Hintergrund unscharf.
     */
    private void showStartScreen() {
        overlayLayer.getChildren().clear();
        overlayLayer.setPickOnBounds(true); // Aktiviert Mausklicks auf dem Overlay (blockiert das Spiel dahinter)
        tableLayer.setEffect(new GaussianBlur(15)); // Optischer Effekt: Der Spieltisch im Hintergrund wird unscharf

        // Halb-transparenter schwarzer Hintergrund
        StackPane bg = new StackPane();
        bg.setStyle("-fx-background-color: rgba(0,0,0,0.7);");

        // Weiße Box mit Titel und Buttons
        VBox box = Styles.createWhiteModalBox();
        Text logo = new Text("BlackJack Royale");
        logo.setFont(Font.font("Arial", FontWeight.BOLD, 30));
        // Start Button
        Button start = Styles.createModalButton("Start Game", "CONFIRM");
        start.setOnAction(e -> {
            closeOverlay();
            startBetting();
        });
        // Regel Button
        Button rules = Styles.createModalButton("Rules", "GREY");
        rules.setOnAction(e -> showRules());
        // Exit Button
        Button exit = Styles.createModalButton("Exit", "CANCEL");
        exit.setOnAction(e -> Platform.exit());

        box.getChildren().addAll(logo, start, rules, exit);
        bg.getChildren().add(box);
        overlayLayer.getChildren().add(bg);
    }

//    Startet den Spielzug für einen bestimmten Sitz.
//  Überspringt Sitze ohne Einsatz und wechselt anschließend zum Dealer


    private void showBetModal(int seatIdx) {
        // Hintergrund unscharf machen
        tableLayer.setEffect(new GaussianBlur(15));

        // Overlay erstellen (halbtransparent)
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.5);");

        // Box für Inhalt
        VBox box = Styles.createWhiteModalBox();

        // Überschrift
        Label title = new Label("Place Bet");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        // Anzeige des aktuellen Guthabens
        Label bal = new javafx.scene.control.Label("Your balance: " + balance.get());
        bal.setTextFill(Color.GRAY);

        // Temporärer Einsatz, damit man noch ändern kann
        IntegerProperty tempBet = new SimpleIntegerProperty(seats[seatIdx].getMainHand().bet.get());

        // Chips zum Wetten erstellen
        HBox chips = new HBox(15);
        chips.setAlignment(Pos.CENTER);
        int[] values = {5, 10, 25, 50, 100};
        for (int val : values) {
            ChipView cv = new ChipView(val, 50, true);
            // Beim Klick auf Chip: Einsatz erhöhen, wenn genug Geld vorhanden
            cv.setOnMouseClicked(e -> {
                if (balance.get() >= (tempBet.get() + val - seats[seatIdx].getMainHand().bet.get())) {
                    tempBet.set(tempBet.get() + val);
                }
            });
            chips.getChildren().add(cv);
        }

        // Anzeige des aktuellen Einsatzes
        Label curBet = new Label();
        curBet.textProperty().bind(Bindings.concat("Bet size: ", tempBet));

        // Buttons für "Cancel" und "Bet"
        HBox buttons = new HBox(20);
        buttons.setAlignment(Pos.CENTER);

        // Cancel-Button schließt Overlay
        Button btnCancel = Styles.createModalButton("Cancel", "CANCEL");
        btnCancel.setOnAction(e -> closeOverlay());

        // Bet-Button: Einsatz bestätigen und Geld abziehen
        Button btnBet = Styles.createModalButton("Bet", "CONFIRM");
        btnBet.setOnAction(e -> {
            int diff = tempBet.get() - seats[seatIdx].getMainHand().bet.get();
            if (balance.get() >= diff) {
                balance.set(balance.get() - diff);
                seats[seatIdx].getMainHand().bet.set(tempBet.get());
                updateSeatVisuals(seatIdx);
            }
            closeOverlay();
        });

        // Buttons und alles andere zur Box hinzufügen
        buttons.getChildren().addAll(btnCancel, btnBet);
        box.getChildren().addAll(title, bal, chips, curBet, buttons);
        overlay.getChildren().add(box);
        overlayLayer.getChildren().add(overlay);
        overlayLayer.setPickOnBounds(true);  // Overlay klickbar machen
    }

    private void showGameOver() {
        // Hintergrund unscharf machen
        tableLayer.setEffect(new GaussianBlur(15));

        // Overlay erstellen (halbtransparent)
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.5);");

        // Box für Inhalt
        VBox box = Styles.createWhiteModalBox();

        // Überschrift
        Label title = new Label("Game over!");
        title.setFont(javafx.scene.text.Font.font("Arial", FontWeight.BOLD, 22));

        // Untertitel
        Label sub = new Label("You ran out of money!");
        sub.setTextFill(Color.GRAY);

        // Button zum Spiel zurücksetzen
        Button reset = Styles.createModalButton("Reset Game", "CONFIRM");
        reset.setOnAction(e -> {
            balance.set(100);   // Geld zurücksetzen
            closeOverlay();     // Overlay schließen
            startBetting();     // Neues Spiel starten
        });

        // Button zum Spiel beenden
        Button exit = Styles.createModalButton("Exit", "CANCEL");
        exit.setOnAction(e -> Platform.exit());

        // Alles zusammenfügen
        box.getChildren().addAll(title, sub, reset, exit);
        overlay.getChildren().add(box);
        overlayLayer.getChildren().add(overlay);
        overlayLayer.setPickOnBounds(true);  // Overlay klickbar machen
    }

    private void showRules() {
        // Overlay erstellen (halbtransparent, alles darüber legen)
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.6);");
        overlay.setPickOnBounds(true);

        // Box für Inhalt erstellen
        VBox box = Styles.createWhiteModalBox();

        // Überschrift
        Label t = new Label("House Rules");
        t.setFont(Font.font("Arial", FontWeight.BOLD, 22));

        // Scrollbarer Text mit Regeln
        ScrollPane sp = new ScrollPane();
        Label content = new Label(
                "OBJECTIVE:\nBeat the dealer's hand total without exceeding 21.\n\n" +
                        "CARD VALUES:\n- 2-10: Face value.\n- J, Q, K: 10.\n- Ace: 1 or 11.\n\n" +
                        "THE DEAL:\nEveryone gets 2 cards. Dealer has one face down.\n\n" +
                        "ACTIONS:\n- HIT: Take another card.\n- STAND: End turn.\n- DOUBLE: Double bet, take exactly 1 card, then stand.\n" +
                        "- SPLIT: If 2 cards are same rank, split into 2 hands (requires equal bet). You play each hand separately.\n\n" +
                        "DEALER:\nDealer stands on all 17s. Dealer hits on 16.\n\n" +
                        "PAYOUTS:\nWin 1:1. Blackjack 3:2. Push means tie (bet returned).\n\n" +
                        "BUST:\nOver 21 is an immediate loss."
        );
        content.setWrapText(true);
        content.setFont(Font.font("Arial", 14));
        content.setPadding(new Insets(15));
        sp.setContent(content);
        sp.setFitToWidth(true);
        sp.setPrefHeight(350);
        sp.setStyle("-fx-background: white; -fx-background-color: transparent;");

        // Schließen-Button
        Button cl = Styles.createModalButton("Close", "GREY");
        cl.setOnAction(e -> {
            overlayLayer.getChildren().remove(overlay);
            if (gameState.get() != GameState.START_SCREEN) overlayLayer.setPickOnBounds(false);
            if (gameState.get() == GameState.BETTING) tableLayer.setEffect(null);
        });

        // Alles zusammenfügen
        box.getChildren().addAll(t, sp, cl);
        overlay.getChildren().add(box);
        overlayLayer.getChildren().add(overlay);
    }

    //schließt alles
    private void closeOverlay() {
        overlayLayer.getChildren().clear();
        overlayLayer.setPickOnBounds(false);
        tableLayer.setEffect(null);
    }

    // Startet die Setzphase der Einsätze
    private void startBetting() {
        gameState.set(GameState.BETTING);
        // ZEit verbleibend fürs Setzen der Einsätze
        timeLeft.set(60);
        // Deal-Button ist während der Setzphase nicht sichtbar
        btnDeal.setVisible(false);
        // Zeigt eine Statusmeldung
        messageLbl.textProperty().bind(Bindings.concat("Awaiting bets... ", timeLeft.asString("%02d")));
        // Setzt die Dealer-Hand zurück
        dealerHand.cards.clear();
        dealerCardBox.getChildren().clear();
        dealerScoreLbl.setText("");

        // Setzt alle Spieler-Sitzplätze zurück - entfernt alte Hände
        for (int i = 0; i < 5; i++) {
            seats[i].reset();
            updateSeatVisuals(i);
        }

        // falls ein Timer noch läuft wird er gestoppt
        if (timer != null) timer.stop();

        // neuer COuntdown-Toimer (1 Sek pro Tick
        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeLeft.set(timeLeft.get() - 1);
            //Wemm Zeit abgelaufen wird Kertenverteilung gestartet
            if (timeLeft.get() <= 0) dealCardsSequence();
        }));
        // Timer läuft unbegrenzt, bis er manuell gestoppt wird
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }


    // Karten werden in festgelegter Reihenfolge ausgeteilt + nur Plätze mit Einsatz bekommen Karte
    private void dealCardsSequence() {

        // checkt ob min. 1 Spieler etwas gesetzt hat
        boolean anyBets = false;
        for (SeatModel s : seats) {
            if (!s.hands.isEmpty() && s.getMainHand().bet.get() > 0) anyBets = true;
        }


        // Ohne einsatz -> Runde wird nicht gestartet
        if (!anyBets) {
            timeLeft.set(60);
            return;
        }
        // Stoppt den Setz-Timer und blendet den Deal-Button aus
        timer.stop();
        btnDeal.setVisible(false);

        // Wechselt den Spielzustand in die Austeilphase
        gameState.set(GameState.DEALING);
        messageLbl.textProperty().unbind();
        //Statusmeldung
        messageLbl.setText("Cards have been dealt...");

        // Zeitversatz für Kartenanimation
        Timeline tl = new Timeline();
        double d = 0;

        // bei jeder runde können Karten and alle Spieler mit Einsatu ausgeteilt werden
        for (int i = 0; i < 2; i++) {
            for (int s = 0; s < 5; s++) {
                if (seats[s].getMainHand().bet.get() > 0) {
                    final int sIdx = s;
                    Card c = deck.draw();
                    // Plane die Animation für diesen Sitzplatz
                    tl.getKeyFrames().add(new KeyFrame(Duration.seconds(d), e -> animateDealToSeat(sIdx, c)));
                    // erhöht Zeitverrsatz für nächste Karte
                    d += 0.2;
                }
            }
            // Dealer bekommt auch Karte pro Runde
            Card dc = deck.draw();
            // Zweite Dealer-Karte wird verdeckt ausgeteilt
            final boolean hidden = (i == 1);
            tl.getKeyFrames().add(new KeyFrame(Duration.seconds(d), e -> animateDealToDealer(dc, hidden)));
            d += 0.2;
        }

        // nachdem alle eine Karte haben, beginnt Zug vom ersten Spieler
        tl.setOnFinished(e -> playSeat(0));
        tl.play();
    }

    // Animation vom Austeilen der Karte zu Spieler
    private void animateDealToSeat(int seatIdx, Card c) {

        CardView cv = new CardView(c);

        // Startposition außerhalb vom sichtbaren bereit
        cv.setTranslateX(400);
        cv.setTranslateY(-300);
        seatCardHBoxes[seatIdx].getChildren().add(cv);

        // Animation für Verschieben der Karte mit 500ms Dauer
        TranslateTransition tt = new TranslateTransition(Duration.millis(500), cv);
        // Ziel Spot der Karte
        tt.setToX(0);
        tt.setToY(0);
        // Aktionen nach Abschluss der Anomaton
        tt.setOnFinished(e -> {
            //entfernt animierte Karte (nur visuelle Übergangslösung)
            seatCardHBoxes[seatIdx].getChildren().remove(cv);
            //Fügt karte logisch zur Spieler Hand hinzu
            seats[seatIdx].getMainHand().cards.add(c);
            // Aktualisierung der der Anzeige des jeweiligen Spielers
            updateSeatVisuals(seatIdx);
        });
        // Start der Aninmation
        tt.play();
    }


    // auch das austeilen der Karten an den Dealer muss animiert werden
    private void animateDealToDealer(Card c, boolean hidden) {
        //fügt karte logisch zur Dealer Hand hinzu
        dealerHand.cards.add(c);
        // graphische Darstellung der Karte (wenn hidden == true -> Rückseite der Karte)
        CardView cv = new CardView(hidden ? null : c);
        // Startposition außerhalb vom sichtbaren bereit
        cv.setTranslateX(400);
        cv.setTranslateY(-300);
        dealerCardBox.getChildren().add(cv);

        // Animation für Verschieben der Karte mit 300ms Dauer
        TranslateTransition tt = new TranslateTransition(Duration.millis(300), cv);
        // Ziel Spot der Karte
        tt.setToX(0);
        tt.setToY(0);

        // Aktualisier Dealer Score wenn Karte offen ausgeteilt wurde
        tt.setOnFinished(e -> {
            if (!hidden) dealerScoreLbl.setText(String.valueOf(dealerHand.getBestValue()));
        });

        // Starten der Animation
        tt.play();
    }


    private void playSeat(int seatIdx) {

        // Überspringt Sitze ohne Einsatz
        while (seatIdx < 5 && seats[seatIdx].getMainHand().bet.get() == 0) {
            seatIdx++;
        }
        // Wenn kein gültiger Sitz mehr vorhanden ist, spielt der Dealer
        if (seatIdx >= 5) {
            playDealer();
            return;
        }

        // Aktiviert den aktuellen Sitz
        SeatModel s = seats[seatIdx];
        s.isActiveSeat.set(true);
        // Startet immer mit der ersten Hand
        s.activeHandIndex.set(0);

        // UI aktualisieren und automatische Prüfungen durchführen
        updateSeatVisuals(seatIdx);
        checkHandAutoOps(seatIdx);
    }

    //    Führt automatische Aktionen für eine Hand aus.
//   z.B.  bei 21 oder Bust automatisch zu stehen.
    private void checkHandAutoOps(int seatIdx) {
        SeatModel s = seats[seatIdx];
        HandData h = s.getCurrentHand();
        // UI aktualisieren
        updateSeatVisuals(seatIdx);

        // Bei 21 oder Bust automatisch Stand ausführen
        if (h.getBestValue() >= 21) {
            Platform.runLater(() -> handleStand(seatIdx));
        }
    }

    //    Behandelt die Hit-Aktion für eine Hand.
    private void handleHit(int seatIdx) {
        SeatModel s = seats[seatIdx];
        HandData h = s.getCurrentHand();
        // Eine Karte ziehen
        h.cards.add(deck.draw());
        // ui aktualisieren
        updateSeatVisuals(seatIdx);
        // Bei 21 oder Bust automatisch stehen
        if (h.getBestValue() >= 21) {
            handleStand(seatIdx);
        }
    }

    //* Behandelt die Stand-Aktion.
// Wechselt zur nächsten Hand//Sitz
    private void handleStand(int seatIdx) {
        SeatModel s = seats[seatIdx];
        // Falls weitere Hände vorhanden sind (Split),
        // zur nächsten Hand wechseln
        if (s.activeHandIndex.get() < s.hands.size() - 1) {
            s.activeHandIndex.set(s.activeHandIndex.get() + 1);
            updateSeatVisuals(seatIdx);
            checkHandAutoOps(seatIdx);
            // Andernfalls Sitz beenden und zum nächsten wechseln
        } else {
            s.isActiveSeat.set(false);
            updateSeatVisuals(seatIdx);
            playSeat(seatIdx + 1);
        }
    }

    //    Behandelt die Double-Down-Aktion
    //    Der Einsatz wird verdoppelt, der Spieler erhält genau eine weitere Karte und muss danach automatisch stehen.
    private void handleDouble(int seatIdx) {
        SeatModel s = seats[seatIdx];
        HandData h = s.getCurrentHand();

        // Prüft, ob genügend Guthaben zum Verdoppeln vorhanden ist
        if (balance.get() >= h.bet.get()) {
            // Einsatz abziehen und verdoppeln
            balance.set(balance.get() - h.bet.get());
            h.bet.set(h.bet.get() * 2);

            // Genau eine Karte ziehen
            h.cards.add(deck.draw());
            updateSeatVisuals(seatIdx);

            // Nach Doublen automatisch stehen
            handleStand(seatIdx);
        }
    }

    //    Behandelt die Split-Aktion für einen Sitz.
    // Eine Hand mit zwei gleichen Karten wird in zwei Hände aufgeteilt, jeweils mit einer neuen Karte ergänzt.
    private void handleSplit(int seatIdx) {
        SeatModel s = seats[seatIdx];
        HandData h = s.getCurrentHand();
        // Prüft, ob genügend Guthaben für den zweiten Einsatz vorhanden ist
        if (balance.get() >= h.bet.get()) {
            // Einsatz für die zweite Hand abziehen
            // Hand in zwei separate Hände aufteilen
            balance.set(balance.get() - h.bet.get());
            s.split();

            // Jede neue Hand erhält eine zusätzliche Karte
            s.hands.get(0).cards.add(deck.draw());
            s.hands.get(1).cards.add(deck.draw());

            updateSeatVisuals(seatIdx);

            // Prüft automatische Aktionen (z.B. Blackjack, Bust)
            checkHandAutoOps(seatIdx);
        }
    }

    private void playDealer() {
        //Spielstatus ändern -  Dealer dran
        gameState.set(GameState.DEALER_TURN);
        // Verdeckte Karte aufdecken und durch echte Karte ersetzen
        if (dealerCardBox.getChildren().size() > 1) {
            dealerCardBox.getChildren().remove(1);
            dealerCardBox.getChildren().add(new CardView(dealerHand.cards.get(1)));
        }
        // Zeigt aktuellen Punktestand des Dealers an
        dealerScoreLbl.setText(String.valueOf(dealerHand.getBestValue()));
        //Timeline damit Karten nacheinander kommen
        Timeline dt = new Timeline();
        dt.setCycleCount(Timeline.INDEFINITE); //läuft endlos bis Timer stopp

        dt.getKeyFrames().add(new KeyFrame(Duration.seconds(1), e -> {
            // REGEL: Hat der Dealer weniger als 17 Punkte?
            if (dealerHand.getBestValue() < 17) {
                // JA -> Der Dealer MUSS eine Karte ziehen (Hausregel)
                Card c = deck.draw();
                dealerHand.cards.add(c); // Karte hinzufügen
                dealerCardBox.getChildren().add(new CardView(c)); // Karte anzeigen
                dealerScoreLbl.setText(String.valueOf(dealerHand.getBestValue())); // Punkte updaten
            } else {
                dt.stop(); // Dealer hat 17 od. mehr -> muss aufhören ; Timer wird gestoppt
                resolve(); // Endabrechnung - Gewinner wird ermittelt
            }
        }));
        dt.play(); // Startet Animation
    }

    /*
     * Auswertung der Runde (Gewinn/Verlust).
     * Vergleicht Punkte und verteilt Geld.
     */

    private void resolve() {
        int dVal = dealerHand.getBestValue();
        boolean dBust = dealerHand.isBusted();
        boolean dBJ = dealerHand.isBlackjack();

        for (int i = 0; i < 5; i++) {
            SeatModel s = seats[i];
            for (HandData h : s.hands) {
                if (h.bet.get() == 0) continue; // Nur wetten auswerten

                int pVal = h.getBestValue();
                boolean pBJ = h.isBlackjack();
                String msg;
                int win = 0;

                if (h.isBusted()) {
                    msg = "LOST"; // Spieler hat überzogen
                } else if (pBJ) {
                    if (dBJ) {
                        msg = "PUSH"; // Unentschieden (beide Blackjack)
                        win = h.bet.get();
                    } else {
                        msg = "WIN (BJ)"; // Spieler Blackjack (zahlt 3:2)
                        win = (int) (h.bet.get() * 2.5);
                    }
                } else if (dBust) {
                    msg = "WON"; // Dealer hat überzogen, Spieler gewinnt
                    win = h.bet.get() * 2;
                } else if (pVal > dVal) {
                    msg = "WON"; // Spieler hat mehr Punkte als Dealer
                    win = h.bet.get() * 2;
                } else if (pVal == dVal) {
                    msg = "PUSH"; // Gleichstand
                    win = h.bet.get();
                } else {
                    msg = "LOST";
                }

                h.statusMsg.set(msg);
                if (win > 0) balance.set(balance.get() + win);
            }
            updateSeatVisuals(i);
        }
        // Warte 4 Sekunden, dann Neustart oder Game Over
        PauseTransition pt = new PauseTransition(Duration.seconds(4));
        pt.setOnFinished(e -> {
            if (balance.get() <= 0) showGameOver();
            else startBetting();
        });
        pt.play();
    }
}



