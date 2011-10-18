package settlers;

public class TradeResult {

    public static final int DECLINE = 0;
    public static final int COUNTEROFFER = 1;
    public static final int ACCEPT = 2;

    private final int decision;
    private final TradeOffer counteroffer;

    TradeResult(int decision, TradeOffer counteroffer) {
        if (decision != COUNTEROFFER && counteroffer != null)
            throw new IllegalArgumentException("Internal: counteroffer may" +
                "be offered only when decision is COUNTEROFFER");
        this.decision = decision;
        this.counteroffer = counteroffer;
    }

    public int decision() {
        return decision;
    }

    public TradeOffer counteroffer() {
        return counteroffer;
    }
}


