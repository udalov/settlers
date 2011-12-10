package settlers.vis;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.*;
import javax.swing.*;
import settlers.*;
import settlers.util.*;

public class BoardVis extends JPanel {

    private static final int HEX_SIZE = 54;
    private static final int TOWN_RADIUS = 10;
    private static final int PORT_DISTANCE = 8;

    private static final int[] DX = new int[] {2, 0, -2, -2, 0, 2};
    private static final int[] DY = new int[] {-1, -2, -1, 1, 2, 1};

    // TODO: generate
    private static final String[] PORTS = new String[] {
        "820,821,730,731,640",
        "641,440,441,240,241",
        "242,131,132,021,022",
        "023,112,113,202,203",
        "204,403,404,603,604",
        "605,600,715,710,825"
    };

    private final Game game;
    private final Board board;
    private final Map<Hex, Point> hex;
    private final Map<Path, Polygon> path;

    public BoardVis(Game game) {
        super(null);
        this.game = game;
        this.board = game.board();
        this.hex = new HashMap<Hex, Point>();
        this.path = new HashMap<Path, Polygon>();
    }

    private int width() { return getSize().width; }
    private int height() { return getSize().height; }

    void recalcHexes() {
        final int width = width();
        final int height = height();
        for (Hex c : Board.allHexes()) {
            hex.put(c, new Point((c.x() - 4) * HEX_SIZE + width / 2, height / 2 - (c.y() - 1) * HEX_SIZE * 3 / 2));
        }
    }

    int[][] calcHexagonVertices(int x, int y) {
        int[][] ans = new int[2][6];
        for (int d = 0; d < 6; d++) {
            ans[0][d] = x + HEX_SIZE * DX[d] / 2;
            ans[1][d] = y + HEX_SIZE * DY[d] / 2;
        }
        return ans;
    }

    Polygon createHexagon(Point z) {
        int[][] v = calcHexagonVertices(z.x, z.y);
        return new Polygon(v[0], v[1], 6);
    }

    Point xingCoords(Xing i) {
        Point z = hex.get(i.hex());
        int[][] v = calcHexagonVertices(z.x, z.y);
        int d = i.direction();
        return new Point(v[0][d], v[1][d]);
    }

    Point[] pathCoords(Path p) {
        Point z = hex.get(p.hex());
        int[][] v = calcHexagonVertices(z.x, z.y);
        int d = p.direction(), nd = (d + 1) % 6;
        return new Point[]{
            new Point(v[0][d], v[1][d]),
            new Point(v[0][nd], v[1][nd])
        };
    }

    void drawHex(Graphics2D g, Hex c) {
        Point z = hex.get(c);
        Polygon p = createHexagon(z);
        Color color = resourceToColor(board.resourceAt(c));
        g.setColor(color);
        g.fillPolygon(p);
        int number = board.numberAt(c);
        if (number != 0) {
            g.setColor(Color.BLACK);
            g.setFont(new Font("Tahoma",
                number == 6 || number == 8 ? Font.BOLD : Font.PLAIN, 36));
            String str = number + "";
            if (board.robber() == c)
                str = "[" + str + "]";
            g.drawString(str, z.x - g.getFontMetrics().stringWidth(str) / 2, z.y + 13);
        }
    }

    void drawPath(Graphics2D g, Path p) {
        Player pl = board.roadAt(p);
        if (pl == null) {
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(1));
        } else {
            g.setColor(playerColorToColor(pl.color()));
            g.setStroke(new BasicStroke(5));
        }
        Point[] z = pathCoords(p);
        g.drawLine(z[0].x, z[0].y, z[1].x, z[1].y);
    }

    void drawXing(Graphics2D g, Xing i) {
        Town t = board.townAt(i);
        if (t == null)
            return;
        g.setColor(playerColorToColor(t.player().color()));
        Point p = xingCoords(i);
        if (t.isCity()) {
            g.fillRect(p.x - TOWN_RADIUS, p.y - TOWN_RADIUS, 2 * TOWN_RADIUS, 2 * TOWN_RADIUS);
        } else {
            g.fillOval(p.x - TOWN_RADIUS, p.y - TOWN_RADIUS, 2 * TOWN_RADIUS, 2 * TOWN_RADIUS);
        }
    }

    void drawPort(Graphics2D g, Xing i) {
        Pair<Boolean, Resource> br = board.portAt(i);
        if (!br.first())
            return;
        Resource r = br.second();
        int d = -1;
        for (int j = 0; j < 6; j++) {
            if (PORTS[j].contains("" + i.x() + i.y() + i.direction())) {
                d = j;
                break;
            }
        }
        if (d < 0)
            throw new IllegalStateException("Internal: invalid port location");
        Point p = xingCoords(i);
        String str = resourceToPort(r);
        g.setFont(new Font("Tahoma", Font.BOLD, 10));
        Color c = r == null ? new Color(0x8888FF) : resourceToColor(r);
        if (r == Resource.LUMBER || r == Resource.BRICK)
            c = c.brighter().brighter();
        g.setColor(c);
        g.drawString(str,
            p.x + PORT_DISTANCE * DX[d] - g.getFontMetrics().stringWidth(str) / 2,
            p.y + PORT_DISTANCE * DY[d] + 5
        );
    }

    String resourceToPort(Resource r) {
        return r == null ? "3:1" : r.chr() + "";
    }

    Color resourceToColor(Resource r) {
        if (r == null)
            return new Color(0x778822);
        switch (r) {
            case BRICK: return new Color(0x550000);
            case WOOL: return new Color(0x00FF00);
            case ORE: return new Color(0x999999);
            case GRAIN: return new Color(0xAAAA00);
            case LUMBER: return new Color(0x004400);
            default: return Color.WHITE;
        }
    }

    Color playerColorToColor(Player.Color c) {
        switch (c) {
            case BLUE: return new Color(0x0000FF);
            case WHITE: return new Color(0xFFFFFF);
            case ORANGE: return new Color(0xFF9900);
            case RED: return new Color(0xFF0000);
            default: return Color.WHITE;
        }
    }

    void drawBoard(Graphics2D g) {
        for (Hex c : Board.allHexes()) {
            drawHex(g, c);
        }
        for (Path p : Board.allPaths()) {
            if (board.roadAt(p) == null)
                drawPath(g, p);
        }
        for (Path p : Board.allPaths()) {
            if (board.roadAt(p) != null)
                drawPath(g, p);
        }
        for (Xing i : Board.allXings()) {
            drawXing(g, i);
        }
        for (Xing i : Board.allXings()) {
            drawPort(g, i);
        }
    }

    public void paintComponent(Graphics gg) {
        super.paintComponent(gg);
        BufferedImage bi = new BufferedImage(width(), height(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D)bi.getGraphics();

        g.setColor(new Color(0xFFFF77));
        g.fillRect(0, 0, width(), height());

        recalcHexes();
        drawBoard(g);

        gg.drawImage(bi, 0, 0, width(), height(), null);
    }
}

