package settlers.vis;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import settlers.*;

public class Vis extends JFrame implements WindowListener, MouseListener, ComponentListener {
    
    public static final int BOARD_WIDTH = 1020;
    public static final int BOARD_HEIGHT = 740;

    private final Game game;
    private final Game.Runner thread;

    private final BoardVis boardVis;
    private final JMenuBar menuBar = new JMenuBar();
    private final JButton nextActionButton = new JButton("Next action");
    private final JButton nextTurnButton = new JButton("Next turn");

    public Vis(Game game, Game.Runner thread) {
        setLayout(null);

        this.game = game;
        this.thread = thread;

        buildMenu();
        setJMenuBar(menuBar);

        buildButtons();
        getContentPane().add(nextActionButton);
        getContentPane().add(nextTurnButton);

        boardVis = new BoardVis(game);
        getContentPane().add(boardVis);

        addWindowListener(this);
        addMouseListener(this);
        addComponentListener(this);

        pack();

        final Insets insets = getInsets();
        final int width = BOARD_WIDTH + insets.left + insets.right;
        final int height = BOARD_HEIGHT + insets.top + insets.bottom + menuBar.getSize().height;
        setSize(width, height);
        setMinimumSize(new Dimension(width, height));

        new Thread(thread).start();
        nextTurnButton.setText("Skip building phase");
        setVisible(true);
    }

    void buildMenu() {
        final JMenu game = new JMenu("Game");

        final JMenuItem gameNew = new JMenuItem("New...");
        final JMenuItem gameQuit = new JMenuItem("Quit");

        final ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Object o = e.getSource();
                if (o == gameNew) {
                } else if (o == gameQuit) {
                    System.exit(0);
                }
            }
        };

        gameNew.addActionListener(listener);
        gameQuit.addActionListener(listener);
        // game.add(gameNew);
        // game.addSeparator();
        game.add(gameQuit);
        menuBar.add(game);
    }

    void buildButtons() {
        final ActionListener listener = new ActionListener() {
            private void nextAction() {
                thread.next();
                // TODO: invent something different
                try { Thread.sleep(90); } catch (InterruptedException ie) { }
                synchronized(game) { }
            }

            public void actionPerformed(ActionEvent e) {
                Object o = e.getSource();
                if (o == nextActionButton) {
                    nextAction();
                    repaint();
                } else if (o == nextTurnButton) {
                    int turn = game.history().size();
                    do {
                        nextAction();
                    } while (game.history().size() == turn);
                    if (turn == 1)
                        nextTurnButton.setText("Next turn");
                    repaint();
                }
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

        boardVis.setSize(
            width - insets.left - insets.right,
            height - insets.top - insets.bottom - menuBar.getSize().height
        );
    }
    public void componentHidden(ComponentEvent e) { }
    public void componentShown(ComponentEvent e) { }
    public void componentMoved(ComponentEvent e) { }
}

