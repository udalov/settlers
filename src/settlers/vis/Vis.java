package settlers.vis;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import settlers.*;

public class Vis extends JFrame implements WindowListener, MouseListener, ComponentListener {
    
    private static final int BOARD_WIDTH = 1020;
    private static final int BOARD_HEIGHT = 740;

    private final Game game;
    private final Game.VisAPI api;

    private final GameVis gameVis;
    private final JMenuBar menuBar = new JMenuBar();
    private final JButton nextActionButton = new JButton("Next action");
    private final JButton nextTurnButton = new JButton("Next turn");

    public Vis(Game game, Game.VisAPI api) {
        setLayout(null);

        this.game = game;
        this.api = api;

        buildMenu();
        setJMenuBar(menuBar);

        buildButtons();
        getContentPane().add(nextActionButton);
        getContentPane().add(nextTurnButton);

        gameVis = new GameVis(api);
        getContentPane().add(gameVis);

        addWindowListener(this);
        addMouseListener(this);
        addComponentListener(this);

        pack();

        final Insets insets = getInsets();
        final int width = BOARD_WIDTH + insets.left + insets.right;
        final int height = BOARD_HEIGHT + insets.top + insets.bottom + menuBar.getSize().height;
        setSize(width, height);
        setMinimumSize(new Dimension(width, height));

        new Thread(new Runnable() {
            public void run() {
                Vis.this.api.play();
            }
        }).start();
        setVisible(true);

        repaint();
    }

    void buildMenu() {
        final JMenu game = new JMenu("Game");

        final JMenuItem gameRestart = new JMenuItem("Restart");
        final JMenuItem gameQuit = new JMenuItem("Quit");

        final ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Object o = e.getSource();
                if (o == gameRestart) {
                    // TODO
                } else if (o == gameQuit) {
                    System.exit(0);
                }
            }
        };

        gameRestart.addActionListener(listener);
        gameQuit.addActionListener(listener);
        // game.add(gameRestart);
        // game.addSeparator();
        game.add(gameQuit);
        menuBar.add(game);
    }

    void buildButtons() {
        final ActionListener listener = new ActionListener() {
            private void nextAction() {
                api.next();
            }

            private void repaintVis() {
                // TODO: invent something different
                try { Thread.sleep(50); } catch (InterruptedException ignored) { }
                synchronized(game) { }
                repaint();
            }

            public void actionPerformed(ActionEvent e) {
                if (api.isFinished())
                    return;
                Object o = e.getSource();
                if (o == nextActionButton) {
                    nextAction();
                } else if (o == nextTurnButton) {
                    int turn = api.history().size();
                    do {
                        nextAction();
                    } while (api.history().size() == turn && !api.isFinished());
                }
                repaintVis();
            }
        };
        nextActionButton.addActionListener(listener);
        nextTurnButton.addActionListener(listener);
    }

    public void windowClosing(WindowEvent e) { 
        System.exit(0); 
    }
    public void windowActivated(WindowEvent e) { }
    public void windowDeactivated(WindowEvent e) { }
    public void windowOpened(WindowEvent e) { }
    public void windowClosed(WindowEvent e) { }
    public void windowIconified(WindowEvent e) { }
    public void windowDeiconified(WindowEvent e) { }

    public void mouseExited(MouseEvent e) { }
    public void mouseEntered(MouseEvent e) { }
    public void mouseReleased(MouseEvent e) { }
    public void mousePressed(MouseEvent e) { }
    public void mouseClicked(MouseEvent e) { }

    public void componentResized(ComponentEvent e) {
        final int width = getSize().width;
        final int height = getSize().height;
        final Insets insets = getInsets();

        final int nextActionWidth = 200;
        final int nextActionHeight = 40;
        final int nextTurnWidth = 200;
        final int nextTurnHeight = 40;

        nextActionButton.setLocation(width / 2 - nextActionWidth / 2, height - 200);
        nextActionButton.setSize(nextActionWidth, nextActionHeight);
        nextTurnButton.setLocation(width / 2 - nextTurnWidth / 2, height - 180 + nextActionHeight);
        nextTurnButton.setSize(nextTurnWidth, nextTurnHeight);

        gameVis.setSize(
            width - insets.left - insets.right,
            height - insets.top - insets.bottom - menuBar.getSize().height
        );
    }
    public void componentHidden(ComponentEvent e) { }
    public void componentShown(ComponentEvent e) { }
    public void componentMoved(ComponentEvent e) { }
}

