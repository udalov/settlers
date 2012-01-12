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
    private static final int TOWN_RADIUS = 9;
    private static final int PORT_DISTANCE = 9;

    private static final int[] DX = new int[] {2, 0, -2, -2, 0, 2};
    private static final int[] DY = new int[] {-1, -2, -1, 1, 2, 1};

    private static final int PLAYER_INFO_WIDTH = 160;
    private static final int PLAYER_INFO_HEIGHT = 70;

    private static final Color BACKGROUND_COLOR = new Color(0xFFFFAA);

    // TODO: generate
    private static final String[] PORTS = new String[] {
        "820,821,730,731,640",
        "641,440,441,240,241",
        "242,131,132,021,022",
        "023,112,113,202,203",
        "204,403,404,603,604",
        "605,600,715,710,825"
    };

    private static final String[] playerColorName = new String[]
    { "RED", "BLUE", "ORANGE", "WHITE" };
    private static final Color[] playerColor = new Color[] {
        new Color(0xFF0000),
        new Color(0x0000FF),
        new Color(0xFF9900),
        new Color(0xFFFFFF)
    };

    private final Game game;
    private final Game.VisAPI api;
    private final Animator animator;
    private final Board board;
    private final Map<Hex, Point> hex;
    private final Map<Edge, Polygon> edge;

    public BoardVis(Game game, Game.VisAPI api, Animator animator) {
        super(null);
        this.game = game;
        this.api = api;
        this.animator = animator;
        this.board = api.board();
        this.hex = new HashMap<Hex, Point>();
        this.edge = new HashMap<Edge, Polygon>();
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

    Point nodeCoords(Node i) {
        Point z = hex.get(i.hex());
        int[][] v = calcHexagonVertices(z.x, z.y);
        int d = i.direction();
        return new Point(v[0][d], v[1][d]);
    }

    Point[] edgeCoords(Edge p) {
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
            if (api.robber() == c)
                str = "[" + str + "]";
            g.drawString(str, z.x - g.getFontMetrics().stringWidth(str) / 2, z.y + 13);
        }
    }

    void drawEdge(Graphics2D g, Edge p) {
        Player pl = api.roadAt(p);
        if (pl == null) {
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(1));
        } else {
            g.setColor(playerColor[pl.color()]);
            g.setStroke(new BasicStroke(5));
        }
        Point[] z = edgeCoords(p);
        g.drawLine(z[0].x, z[0].y, z[1].x, z[1].y);
        g.setStroke(new BasicStroke(1));
    }

    void drawNode(Graphics2D g, Node i) {
        Town t = api.townAt(i);
        if (t == null)
            return;
        Point p = nodeCoords(i);
        int x = p.x;
        int y = p.y;
        int r = TOWN_RADIUS;
        g.setColor(Color.BLACK);
        if (t.isCity()) {
            g.fillPolygon(
                new int[] {x + r/2 + 1, x - r/2, x - 3*r/2 - 1, x - 3*r/2 - 1, x + 2*r + 1, x + 2*r + 1},
                new int[] {y - r/2 - 1, y - 3*r/2 - 1, y - r/2, y + r + 1, y + r + 1, y - r/2 - 1},
                6
            );
        } else {
            g.fillPolygon(
                new int[] {x + r + 1, x, x - r - 1, x - r - 1, x + r + 1},
                new int[] {y - r/2, y - 3*r/2 - 1, y - r/2, y + r + 1, y + r + 1},
                5
            );
        }
        g.setColor(playerColor[t.player().color()]);
        if (t.isCity()) {
            g.fillPolygon(
                new int[] {x + r/2, x - r/2, x - 3*r/2, x - 3*r/2, x + 2*r, x + 2*r},
                new int[] {y - r/2, y - 3*r/2, y - r/2, y + r, y + r, y - r/2},
                6
            );
        } else {
            g.fillPolygon(
                new int[] {x + r, x, x - r, x - r, x + r},
                new int[] {y - r/2, y - 3*r/2, y - r/2, y + r, y + r},
                5
            );
        }
    }

    void drawPort(Graphics2D g, Node i) {
        Pair<Boolean, Resource> br = board.harborAt(i);
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
        Point p = nodeCoords(i);
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

    void drawBoard(Graphics2D g) {
        for (Hex c : Board.allHexes()) {
            drawHex(g, c);
        }
        for (Edge p : Board.allEdges()) {
            if (api.roadAt(p) == null)
                drawEdge(g, p);
        }
        for (Edge p : Board.allEdges()) {
            if (api.roadAt(p) != null)
                drawEdge(g, p);
        }
        for (Node i : Board.allNodes()) {
            drawNode(g, i);
        }
        for (Node i : Board.allNodes()) {
            drawPort(g, i);
        }
    }

    void drawPlayerInfo(Graphics2D g, Player player, int x, int y, boolean invertTurnArrow) {
        final int arc = 16;
        final int captionFont = 16;
        final int fontSize = 16;
        final int caption = captionFont + 6;
        final int arrowRadius = 20;

        final Color normalText = new Color(0x444444);
        final Color manyResources = new Color(0xAA4444);
        final Color longestRoad = new Color(0xFF4444);
        final Color largestArmy = new Color(0xFF4444);
        final Color dashedLineColor = new Color(0xAAAAAA);

        final Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{4}, 0);

        g.setColor(new Color(0xAAAAFF));
        g.fillRoundRect(x - 2, y - 2, PLAYER_INFO_WIDTH + 4, PLAYER_INFO_HEIGHT + 4, arc, arc);
        g.setColor(playerColor[player.color()]);
        g.fillRoundRect(x, y, PLAYER_INFO_WIDTH, caption + 24, arc, arc);
        g.setColor(new Color(0xFFFFEE));
        g.fillRect(x, y + caption, PLAYER_INFO_WIDTH, 40);
        g.fillRoundRect(x, y + caption, PLAYER_INFO_WIDTH, PLAYER_INFO_HEIGHT - caption, arc, arc);
        g.setColor(new Color(0xAAAAFF));
        g.drawLine(x, y + caption, x + PLAYER_INFO_WIDTH, y + caption);

        g.setFont(new Font("Tahoma", Font.BOLD, captionFont));
        g.setColor(normalText);
        g.drawString(player + "", x + 4, y + captionFont + 2);
        y += caption;

        String[] strings = new String[]
            { "res", "dev", "road", "army", "vp" };
        int[] values = new int[] {
            player.cardsNumber(),
            player.developmentsNumber(),
            api.roadLength(player),
            api.armyStrength(player),
            api.points(player)
        };
        Font stringFont = new Font("Courier", Font.PLAIN, fontSize - 5);
        Font valueFont = new Font("Courier", Font.PLAIN, fontSize);
        Color[] colors = new Color[] {
            player.cardsNumber() > 7 ? manyResources : normalText,
            normalText,
            api.longestRoad() == player && api.roadLength(player) >= 5 ? longestRoad : normalText,
            api.largestArmy() == player && api.armyStrength(player) >= 3 ? largestArmy : normalText,
            normalText
        };
        for (int i = 0; i < 5; i++) {
            int tx = x + PLAYER_INFO_WIDTH * (2*i + 1) / 10;
            if (i < 4) {
                g.setColor(dashedLineColor);
                g.setStroke(dashed);
                int xx = tx + PLAYER_INFO_WIDTH/10;
                g.drawLine(xx, y + 3, xx, y + 3*fontSize);
                g.setStroke(new BasicStroke());
            }
            g.setColor(colors[i]);
            g.setFont(stringFont);
            g.drawString(strings[i], tx - g.getFontMetrics().stringWidth(strings[i]) / 2, y + fontSize);
            g.setFont(valueFont);
            g.drawString("" + values[i], tx - g.getFontMetrics().stringWidth("" + values[i]) / 2, y + 2*fontSize + 2);
        }

        if (api.turn() == player) {
            g.setColor(new Color(0x00AA00));
            int r = arrowRadius;
            int cx = x + PLAYER_INFO_WIDTH + 2*r;
            int cy = y + r;
            int[] xs = new int[]
                { cx, cx, cx - r, cx, cx, cx + r, cx + r };
            int[] ys = new int[]
                { cy - r/3, cy - 2*r/3, cy, cy + 2*r/3, cy + r/3, cy + r/3, cy - r/3 };
            if (invertTurnArrow)
                for (int i = 0; i < xs.length; i++)
                    xs[i] = 2 * x + PLAYER_INFO_WIDTH - xs[i];
            g.fillPolygon(xs, ys, xs.length);
        }
    }

    void drawPlayersInfo(Graphics2D g) {
        final int indent = 20;
        final int[] playerInfoX = new int[]
            {indent, indent, width() - PLAYER_INFO_WIDTH - indent, width() - PLAYER_INFO_WIDTH - indent};
        final int[] playerInfoY = new int[]
            {height() - PLAYER_INFO_HEIGHT - indent, indent, indent, height() - PLAYER_INFO_HEIGHT - indent};
        for (int i = 0; i < api.players().size(); i++) {
            drawPlayerInfo(g, api.players().get(i), playerInfoX[i], playerInfoY[i], i >= 2);
        }
    }

    static String eventDescription(Player player, settlers.Event event) {
        String color = player == null ? null : playerColorName[player.color()];
        switch (event.type()) {
            case INITIAL_ROAD:
                return playerColorName[event.player().color()] + " builds initial road";
            case INITIAL_SETTLEMENT:
                return playerColorName[event.player().color()] + " builds initial settlement";
            case ROLL_DICE:
                return color + " rolls " + event.number();
            case ROBBER:
                Player pl = event.player();
                return color + " moves the robber and robs " +
                    (pl == null ? "nobody" : playerColorName[pl.color()]);
            case RESOURCES:
                // TODO?
                return "";
            case DISCARD:
                return playerColorName[event.player().color()] + " discards " +
                    Util.toResourceString(event.resources());
            case ROAD:
                return color + " builds a road";
            case SETTLEMENT:
                return color + " builds a settlement";
            case CITY:
                return color + " builds a city";
            case DEVELOPMENT:
                return color + " draws a development card";
            case KNIGHT:
                return color + " plays knight";
            case INVENTION:
                return color + " plays " + event.resource() + ", " + event.resource2() + " invention";
            case MONOPOLY:
                return color + " plays monopoly and receives " + event.number() + " of " + event.resource();
            case ROAD_BUILDING:
                return color + " plays road building";
            case LONGEST_ROAD:
                return color + " receives the longest road (" + event.number() + ")";
            case LARGEST_ARMY:
                return color + " receives the largest army (" + event.number() + ")";
            case CHANGE:
                return color + " changes " + event.sell() + " to " + event.buy();
            case TRADE:
                return color + " sells " + event.sell() + " to " +
                    playerColorName[event.player().color()] + " and buys " + event.buy();
            case VICTORY:
                return color + " wins!" + (event.number() == 0 ? "" : " (" + event.number() + " VP)");
            case EXCEPTION:
                return color + " throws " + event.exception().getClass().getName();
            default:
                return "";
        }
    }

    void drawLastHistoryEvent(final Graphics g) {
        g.setColor(Color.BLACK);
        g.setFont(new Font("Tahoma", Font.BOLD, 24));
        Pair<Player, settlers.Event> pair = api.history().getLastEvent();
        String str = pair == null ? "" : eventDescription(pair.first(), pair.second());
        if (str.isEmpty())
            return;
        g.drawString(str,
            width() / 2 - g.getFontMetrics().stringWidth(str) / 2,
            32
        );
    }

    public void paintComponent(Graphics gg) {
        super.paintComponent(gg);
        BufferedImage bi = new BufferedImage(width(), height(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D)bi.getGraphics();

        g.setColor(BACKGROUND_COLOR);
        g.fillRect(0, 0, width(), height());

        recalcHexes();
        drawBoard(g);
        drawPlayersInfo(g);
        drawLastHistoryEvent(g);

        gg.drawImage(bi, 0, 0, width(), height(), null);
    }
}

