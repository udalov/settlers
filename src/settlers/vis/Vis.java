package settlers.vis;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import settlers.*;

public class Vis extends JFrame implements WindowListener, MouseListener, ComponentListener {
    
    public static final int BOARD_WIDTH = 1020;
    public static final int BOARD_HEIGHT = 740;

    private final Game game;
    private final Game.GameThread thread;

    private final BoardVis boardVis;
    private final JMenuBar menuBar = new JMenuBar();
    private final JButton nextActionButton = new JButton("Next action");

    public Vis(Game game, Game.GameThread thread) {
        setLayout(null);

        this.game = game;
        this.thread = thread;

        buildMenu();
        setJMenuBar(menuBar);

        buildButtons();
        getContentPane().add(nextActionButton);

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
            public void actionPerformed(ActionEvent e) {
                Object o = e.getSource();
                if (o == nextActionButton) {
                    thread.next();
                    repaint();
                }
            }
        };
        nextActionButton.addActionListener(listener);
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

        final int nextActionWidth = 100;
        final int nextActionHeight = 40;

        nextActionButton.setLocation(width / 2 - nextActionWidth / 2, height - 200);
        nextActionButton.setSize(nextActionWidth, nextActionHeight);

        boardVis.setSize(
            width - insets.left - insets.right,
            height - insets.top - insets.bottom - menuBar.getSize().height
        );
    }
    public void componentHidden(ComponentEvent e) { }
    public void componentShown(ComponentEvent e) { }
    public void componentMoved(ComponentEvent e) { }
}

