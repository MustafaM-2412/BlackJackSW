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

}
