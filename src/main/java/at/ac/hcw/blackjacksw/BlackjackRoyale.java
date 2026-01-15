package at.ac.hcw.blackjacksw;

public class BlackjackRoyale extends Application{

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

//    Startet den Spielzug für einen bestimmten Sitz.
//  Überspringt Sitze ohne Einsatz und wechselt anschließend zum Dealer

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
}
