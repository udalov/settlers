package settlers;

public class Town {
    
    private final Player player;
    private final boolean isCity;

    Town(Player player, boolean isCity) {
        this.player = player;
        this.isCity = isCity;
    }

    public Player player() {
        return player;
    }

    public boolean isCity() {
        return isCity;
    }

}

