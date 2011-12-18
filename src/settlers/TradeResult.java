package settlers;

public class TradeResult {

    public static final int DECLINE = 0;
    public static final int COUNTEROFFER = 1;
    public static final int ACCEPT = 2;

    private final Player player;
    private final TradeOffer offer;
    private final int decision;
    private final TradeOffer counteroffer;

    private boolean completed = false;

    TradeResult(Player player, TradeOffer offer, int decision, TradeOffer counteroffer) {
        if (decision != COUNTEROFFER && counteroffer != null)
            throw new IllegalArgumentException("Internal: counteroffer may" +
                "be offered only when decision is COUNTEROFFER");
        this.player = player;
        this.offer = offer;
        this.decision = decision;
        this.counteroffer = counteroffer;
    }

    public Player player() { return player; }
    public TradeOffer offer() { return offer; }
    public int decision() { return decision; }
    public TradeOffer counteroffer() { return counteroffer; }

    public boolean accepted() { return decision == ACCEPT; }
    public boolean declined() { return decision == DECLINE; }

    public void complete() {
        if (decision == DECLINE)
            throw new GameException("Cannot complete declined offer");
        if (completed)
            throw new GameException("Cannot complete an offer twice");
        completed = true;
        if (decision == COUNTEROFFER)
            counteroffer.complete(player);
        else
            offer.complete(player);
    }

}


