package settlers;

import javax.swing.JFrame;
import settlers.*;
import settlers.bot.Bot;
import settlers.bot.ExampleBot;
import settlers.vis.Vis;

public class Main {
    
    public static void main(String[] args) {
        Game game = new Game();
        game.addPlayer(new Player(new ExampleBot(game), Player.Color.BLUE));
        game.addPlayer(new Player(new ExampleBot(game), Player.Color.WHITE));
        game.addPlayer(new Player(new ExampleBot(game), Player.Color.YELLOW));
        game.addPlayer(new Player(new ExampleBot(game), Player.Color.RED));
        game.start();

        JFrame jf = new JFrame();
        Vis v = new Vis(game.board());
        jf.getContentPane().add(v);
        jf.addWindowListener(v);
        jf.setSize(1002, 824);
        jf.setVisible(true);
    }

}

