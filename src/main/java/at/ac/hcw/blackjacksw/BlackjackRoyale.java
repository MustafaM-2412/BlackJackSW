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



}
