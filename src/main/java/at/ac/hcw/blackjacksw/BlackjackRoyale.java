package at.ac.hcw.blackjacksw;

import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

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
        Ellipse table = new Ellipse(512, 320, 480, 270);
        table.setFill(javafx.scene.paint.Color.web(Styles.TABLE_FILL));
        table.setStroke(javafx.scene.paint.Color.web(Styles.TABLE_STROKE));
        table.setStrokeWidth(8);

        StackPane tableContainer = new StackPane(table);
        tableContainer.setPrefSize(1024, 640);
        tableLayer.getChildren().add(tableContainer);

        javafx.scene.control.Button exit = new javafx.scene.control.Button("Exit");
        exit.setStyle("-fx-background-color: white; -fx-text-fill: #c0392b; " +
                "-fx-background-radius: 20; -fx-font-weight: bold; -fx-cursor: hand;");
        exit.setOnAction(e -> Platform.exit());

        javafx.scene.control.Button rules = new javafx.scene.control.Button("Rules");
        rules.setStyle("-fx-background-color: white; -fx-text-fill: #2c3e50; " +
                "-fx-background-radius: 20; -fx-font-weight: bold; -fx-cursor: hand;");
        rules.setOnAction(e -> showRules());

        AnchorPane.setTopAnchor(exit, 30.0);
        AnchorPane.setLeftAnchor(exit, 30.0);
        AnchorPane.setTopAnchor(rules, 30.0);
        AnchorPane.setRightAnchor(rules, 30.0);

        VBox info = new VBox(8);
        info.setAlignment(Pos.CENTER);

        javafx.scene.control.Label pay = new javafx.scene.control.Label("Blackjack pays 3:2");
        pay.setTextFill(javafx.scene.paint.Color.web("#8daead"));
        pay.setFont(javafx.scene.text.Font.font("Arial", 16));

        messageLbl = new javafx.scene.control.Label();
        messageLbl.setTextFill(javafx.scene.paint.Color.WHITE);
        messageLbl.setFont(javafx.scene.text.Font.font("Arial", 16));

        btnDeal = new javafx.scene.control.Button("DEAL NOW");
        btnDeal.setStyle("-fx-background-color: #f1c40f; -fx-text-fill: black; -fx-font-weight: bold; " +
                "-fx-background-radius: 20; -fx-padding: 5 15; -fx-cursor: hand; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 5, 0, 0, 1);");
        btnDeal.setVisible(false);
        btnDeal.setOnAction(e -> dealCardsSequence());

        info.getChildren().addAll(pay, messageLbl, btnDeal);
        info.setLayoutX(440);
        info.setLayoutY(70);

        javafx.scene.control.Label dTag = new javafx.scene.control.Label("Dealer");
        dTag.setStyle("-fx-background-color: #dcece8; -fx-text-fill: #2c3e50; " +
                "-fx-padding: 4 12; -fx-background-radius: 12; -fx-font-weight: bold;");
        dTag.setLayoutX(485);
        dTag.setLayoutY(160);

        dealerScoreLbl = new javafx.scene.control.Label();
        dealerScoreLbl.setTextFill(javafx.scene.paint.Color.WHITE);
        dealerScoreLbl.setFont(javafx.scene.text.Font.font("Arial", FontWeight.BOLD, 12));
        dealerScoreLbl.setLayoutX(500);
        dealerScoreLbl.setLayoutY(140);

        dealerCardBox = new HBox(-20);
        dealerCardBox.setAlignment(Pos.CENTER);
        dealerCardBox.setLayoutX(460);
        dealerCardBox.setLayoutY(190);

        double[][] pos = {{150, 300}, {280, 380}, {470, 420}, {660, 380}, {790, 300}};
        for (int i = 0; i < 5; i++) {
            createSeatUI(i, pos[i][0], pos[i][1]);
        }

        javafx.scene.control.Label bal = new javafx.scene.control.Label();
        bal.textProperty().bind(Bindings.concat("Your Balance: ", balance));
        bal.setTextFill(Color.WHITE);
        bal.setFont(javafx.scene.text.Font.font("Arial", FontWeight.BOLD, 18));
        AnchorPane.setBottomAnchor(bal, 20.0);
        AnchorPane.setLeftAnchor(bal, 30.0);

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

        javafx.scene.control.Label pb = new javafx.scene.control.Label("Place Bet");
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
        seat.hands.addListener((javafx.collections.ListChangeListener.Change<? extends HandData> c) -> updateSeatVisuals(idx));
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

        javafx.scene.control.Button hit = new javafx.scene.control.Button("Hit");
        javafx.scene.control.Button stand = new javafx.scene.control.Button("Stand");
        javafx.scene.control.Button dbl = new javafx.scene.control.Button("Double");
        javafx.scene.control.Button split = new javafx.scene.control.Button("Split");

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
    private void updateActionButtons(int idx, HBox container, Button hitBtn, Button dblBtn, Button splitBtn) {
        SeatModel s = seats[idx];
        if (!s.isActiveSeat.get()) return;

        HandData currentHand = s.getCurrentHand();
        if (currentHand == null) return;

        container.setVisible(true);

        boolean is21 = currentHand.getBestValue() >= 21;

        hitBtn.setDisable(is21);
        dblBtn.setDisable(is21 || balance.get() < currentHand.bet.get() || currentHand.cards.size() > 2);

        boolean canSplit = currentHand.canSplit() &&
                balance.get() >= currentHand.bet.get() &&
                s.hands.size() == 1;

        splitBtn.setVisible(canSplit);
        splitBtn.setManaged(canSplit);
    }

    private void updateSeatVisuals(int idx) {
        SeatModel s = seats[idx];
        Pane group = seatVisualContainers[idx];
        StackPane interact = (StackPane) group.getChildren().get(2);
        Label pb = (Label) group.getChildren().get(1);
        HBox cardsArea = (HBox) group.getChildren().get(3);

        interact.getChildren().clear();
        int totalBet = s.hands.stream().mapToInt(h -> h.bet.get()).sum();

        if (totalBet > 0) {
            pb.setVisible(false);
            ChipView cv = new ChipView(totalBet > 0 ? s.getMainHand().bet.get() : 0, 45, false);
            Label l = new Label("Bet: " + totalBet);
            l.setStyle("-fx-text-fill: white; -fx-effect: dropshadow(one-pass-box, black, 2,0,0,1);");
            l.setTranslateY(150);
            interact.getChildren().addAll(cv, l);
            if (gameState.get() == GameState.BETTING) btnDeal.setVisible(true);
        } else {
            pb.setVisible(true);
        }

        cardsArea.getChildren().clear();
        for (int hIdx = 0; hIdx < s.hands.size(); hIdx++) {
            HandData h = s.hands.get(hIdx);
            VBox handCol = new VBox(5);
            handCol.setAlignment(Pos.CENTER);

            Label score = new Label(h.cards.isEmpty() ? "" : String.valueOf(h.getBestValue()));
            score.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-effect: dropshadow(one-pass-box, black, 2,0,0,1);");

            HBox pile = new HBox(-35);
            pile.setAlignment(Pos.CENTER);
            for (Card c : h.cards) {
                pile.getChildren().add(new CardView(c));
            }

            Label status = new Label();
            status.textProperty().bind(h.statusMsg);
            status.setStyle("-fx-text-fill: #f1c40f; -fx-font-weight: bold; -fx-font-size: 10px; -fx-effect: dropshadow(one-pass-box, black, 2,0,0,1);");

            if (s.isActiveSeat.get() && s.activeHandIndex.get() == hIdx) {
                pile.setStyle("-fx-effect: dropshadow(three-pass-box, gold, 15, 0, 0, 0);");
            }

            handCol.getChildren().addAll(score, pile, status);
            cardsArea.getChildren().add(handCol);
        }
    }

    private void showStartScreen() {
        overlayLayer.getChildren().clear();
        overlayLayer.setPickOnBounds(true);
        tableLayer.setEffect(new GaussianBlur(15));

        StackPane bg = new StackPane();
        bg.setStyle("-fx-background-color: rgba(0,0,0,0.7);");

        VBox box = Styles.createWhiteModalBox();
        Text logo = new Text("BlackJack Royale");
        logo.setFont(Font.font("Arial", FontWeight.BOLD, 30));

        Button start = Styles.createModalButton("Start Game", "CONFIRM");
        start.setOnAction(e -> {
            closeOverlay();
            startBetting();
        });

        Button rules = Styles.createModalButton("Rules", "GREY");
        rules.setOnAction(e -> showRules());

        Button exit = Styles.createModalButton("Exit", "CANCEL");
        exit.setOnAction(e -> Platform.exit());

        box.getChildren().addAll(logo, start, rules, exit);
        bg.getChildren().add(box);
        overlayLayer.getChildren().add(bg);
    }

    private void showBetModal(int seatIdx) {
        tableLayer.setEffect(new GaussianBlur(15));
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.5);");

        VBox box = Styles.createWhiteModalBox();
        javafx.scene.control.Label title = new javafx.scene.control.Label("Place Bet");
        title.setFont(javafx.scene.text.Font.font("Arial", FontWeight.BOLD, 18));

        javafx.scene.control.Label bal = new javafx.scene.control.Label("Your balance: " + balance.get());
        bal.setTextFill(Color.GRAY);

        IntegerProperty tempBet = new SimpleIntegerProperty(seats[seatIdx].getMainHand().bet.get());
        HBox chips = new HBox(15);
        chips.setAlignment(Pos.CENTER);
        int[] values = {5, 10, 25, 50, 100};

        for (int val : values) {
            ChipView cv = new ChipView(val, 50, true);
            cv.setOnMouseClicked(e -> {
                if (balance.get() >= (tempBet.get() + val - seats[seatIdx].getMainHand().bet.get())) {
                    tempBet.set(tempBet.get() + val);
                }
            });
            chips.getChildren().add(cv);
        }

        javafx.scene.control.Label curBet = new javafx.scene.control.Label();
        curBet.textProperty().bind(Bindings.concat("Bet size: ", tempBet));

        HBox buttons = new HBox(20);
        buttons.setAlignment(Pos.CENTER);

        javafx.scene.control.Button btnCancel = Styles.createModalButton("Cancel", "CANCEL");
        btnCancel.setOnAction(e -> closeOverlay());

        javafx.scene.control.Button btnBet = Styles.createModalButton("Bet", "CONFIRM");
        btnBet.setOnAction(e -> {
            int diff = tempBet.get() - seats[seatIdx].getMainHand().bet.get();
            if (balance.get() >= diff) {
                balance.set(balance.get() - diff);
                seats[seatIdx].getMainHand().bet.set(tempBet.get());
                updateSeatVisuals(seatIdx);
            }
            closeOverlay();
        });

        buttons.getChildren().addAll(btnCancel, btnBet);
        box.getChildren().addAll(title, bal, chips, curBet, buttons);
        overlay.getChildren().add(box);
        overlayLayer.getChildren().add(overlay);
        overlayLayer.setPickOnBounds(true);
    }

    private void showGameOver() {
        tableLayer.setEffect(new GaussianBlur(15));
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.5);");

        VBox box = Styles.createWhiteModalBox();
        javafx.scene.control.Label title = new javafx.scene.control.Label("Game over!");
        title.setFont(javafx.scene.text.Font.font("Arial", FontWeight.BOLD, 22));

        javafx.scene.control.Label sub = new javafx.scene.control.Label("You ran out of money!");
        sub.setTextFill(Color.GRAY);

        javafx.scene.control.Button reset = Styles.createModalButton("Reset Game", "CONFIRM");
        reset.setOnAction(e -> {
            balance.set(100);
            closeOverlay();
            startBetting();
        });

        javafx.scene.control.Button exit = Styles.createModalButton("Exit", "CANCEL");
        exit.setOnAction(e -> Platform.exit());

        box.getChildren().addAll(title, sub, reset, exit);
        overlay.getChildren().add(box);
        overlayLayer.getChildren().add(overlay);
        overlayLayer.setPickOnBounds(true);
    }

    private void showRules() {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.6);");
        overlay.setPickOnBounds(true);

        VBox box = Styles.createWhiteModalBox();
        javafx.scene.control.Label t = new javafx.scene.control.Label("House Rules");
        t.setFont(javafx.scene.text.Font.font("Arial", FontWeight.BOLD, 22));

        javafx.scene.control.ScrollPane sp = new javafx.scene.control.ScrollPane();
        javafx.scene.control.Label content = new javafx.scene.control.Label(
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
        content.setFont(javafx.scene.text.Font.font("Arial", 14));
        content.setPadding(new Insets(15));

        sp.setContent(content);
        sp.setFitToWidth(true);
        sp.setPrefHeight(350);
        sp.setStyle("-fx-background: white; -fx-background-color: transparent;");

        javafx.scene.control.Button cl = Styles.createModalButton("Close", "GREY");
        cl.setOnAction(e -> {
            overlayLayer.getChildren().remove(overlay);
            if (gameState.get() != GameState.START_SCREEN) overlayLayer.setPickOnBounds(false);
            if (gameState.get() == GameState.BETTING) tableLayer.setEffect(null);
        });

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
    private void playSeat(int seatIdx) {
        while (seatIdx < 5 && seats[seatIdx].getMainHand().bet.get() == 0) {
            seatIdx++;
        }

        if (seatIdx >= 5) {
            playDealer();
            return;
        }

        SeatModel s = seats[seatIdx];
        s.isActiveSeat.set(true);
        s.activeHandIndex.set(0);
        updateSeatVisuals(seatIdx);
        checkHandAutoOps(seatIdx);
    }

    private void checkHandAutoOps(int seatIdx) {
        SeatModel s = seats[seatIdx];
        HandData h = s.getCurrentHand();
        updateSeatVisuals(seatIdx);
        if (h.getBestValue() >= 21) {
            Platform.runLater(() -> handleStand(seatIdx));
        }
    }

    private void handleHit(int seatIdx) {
        SeatModel s = seats[seatIdx];
        HandData h = s.getCurrentHand();
        h.cards.add(deck.draw());
        updateSeatVisuals(seatIdx);
        if (h.getBestValue() >= 21) {
            handleStand(seatIdx);
        }
    }

    private void handleStand(int seatIdx) {
        SeatModel s = seats[seatIdx];
        if (s.activeHandIndex.get() < s.hands.size() - 1) {
            s.activeHandIndex.set(s.activeHandIndex.get() + 1);
            updateSeatVisuals(seatIdx);
            checkHandAutoOps(seatIdx);
        } else {
            s.isActiveSeat.set(false);
            updateSeatVisuals(seatIdx);
            playSeat(seatIdx + 1);
        }
    }

    //    Behandelt die Double-Down-Aktion
//  Der Einsatz wird verdoppelt, der Spieler erhält genau eine weitere Karte und muss danach automatisch stehen.
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

    @Override
    public void start(Stage stage) throws Exception {

    }
}
