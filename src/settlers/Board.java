package settlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import settlers.util.Pair;

public class Board {

    public static final int[] DX = {1, -1, -2, -1, 1, 2};
    public static final int[] DY = {1, 1, 0, -1, -1, 0};
    
    private final Map<Hex, Resource> resources;
    private final Map<Hex, Integer> numbers;
    private final Map<Path, Player> roads;
    private final Map<Xing, Town> towns;
    private final Map<Path, Resource> ports2to1;
    private final List<Path> ports3to1;
    private Hex robber;


    private Board(
        Map<Hex, Resource> resources,
        Map<Hex, Integer> numbers,
        Map<Path, Resource> ports2to1,
        List<Path> ports3to1
    ) {
        this.resources = resources;
        this.numbers = numbers;
        this.roads = new HashMap<Path, Player>();
        this.towns = new HashMap<Xing, Town>();
        this.ports2to1 = ports2to1;
        this.ports3to1 = ports3to1;
        robber = null;
        for (Hex hex : resources.keySet())
            if (resources.get(hex) == null)
                robber = hex;
    }

    static Board create(Random rnd) {
        Map<Hex, Resource> resources = new HashMap<Hex, Resource>();
        Map<Hex, Integer> numbers = new HashMap<Hex, Integer>();
        Map<Path, Resource> ports2to1 = new HashMap<Path, Resource>();
        List<Path> ports3to1 = new ArrayList<Path>();

        List<Hex> allHexes = new ArrayList<Hex>(allHexes());
        Collections.shuffle(allHexes, rnd);

        List<Resource> allResources = new ArrayList<Resource>();
        for (int i = 0; i < 4; i++) {
            allResources.add(Resource.WOOL);
            allResources.add(Resource.GRAIN);
            allResources.add(Resource.LUMBER);
            if (i < 3) {
                allResources.add(Resource.BRICK);
                allResources.add(Resource.ORE);
            }
        }
        allResources.add(null); // desert
        Collections.shuffle(allResources, rnd);

        for (int i = 0; i < allHexes.size(); i++)
            resources.put(allHexes.get(i), allResources.get(i));

        List<Integer> allNumbers = new ArrayList<Integer>();
        for (int i = 2; i <= 12; i++) {
            if (i == 7)
                continue;
            allNumbers.add(i);
            if (i == 2 || i == 12)
                continue;
            allNumbers.add(i);
        }

        allHexes.remove(allResources.indexOf(null));

        for (Integer number : allNumbers) {
            while (true) {
                int i = rnd.nextInt(allHexes.size());
                Hex hex = allHexes.get(i);
                if (numbers.containsKey(hex))
                    continue;
                if (number != 6 && number != 8) {
                    numbers.put(hex, number);
                    break;
                }
                boolean ok = true;
                for (int d = 0; d < 6; d++) {
                    int x = hex.x() + DX[d];
                    int y = hex.y() + DY[d];
                    Hex c = hex(x, y);
                    if (c == null || !numbers.containsKey(c))
                        continue;
                    if (numbers.get(c) == 6 || numbers.get(c) == 8) {
                        ok = false;
                        break;
                    }
                }
                if (!ok) continue;
                numbers.put(hex, number);
                break;
            }
        }

        generatePorts(rnd, ports2to1, ports3to1);

        return new Board(resources, numbers, ports2to1, ports3to1);
    }

    static void generatePorts(Random rnd, Map<Path, Resource> ports2to1, List<Path> ports3to1) {
        List<Path> coast = new ArrayList<Path>();
        int d = 0, x = 8, y = 2;
        for (int i = 0; i < 6; i++) {
            int nextd = (d + 1) % 6;
            int prevd = (d + 5) % 6;
            for (int j = 0; j < 2; j++) {
                coast.add(path(x, y, d));
                x += DX[nextd]; y += DY[nextd];
                coast.add(path(x, y, prevd));
            }
            coast.add(path(x, y, d));
            d = nextd;
        }

        List<Integer> permutation = new ArrayList<Integer>();
        for (int i = 0; i < 6; i++)
            permutation.add(i);
        Collections.shuffle(permutation, rnd);

        // ports:
        // 0 --L--
        // 1 -B--3
        // 2 --3--
        // 3 -W--3
        // 4 --O--
        // 5 -G--3

        int ptr = 0;
        for (int ind = 0; ind < 6; ind++) {
            int i = permutation.get(ind);
            if (i == 0) {
                ports2to1.put(coast.get(ptr + 2), Resource.LUMBER);
            } else if (i == 1) {
                ports2to1.put(coast.get(ptr + 1), Resource.BRICK);
                ports3to1.add(coast.get(ptr + 4));
            } else if (i == 2) {
                ports3to1.add(coast.get(ptr + 2));
            } else if (i == 3) {
                ports2to1.put(coast.get(ptr + 1), Resource.WOOL);
                ports3to1.add(coast.get(ptr + 4));
            } else if (i == 4) {
                ports2to1.put(coast.get(ptr + 2), Resource.ORE);
            } else if (i == 5) {
                ports2to1.put(coast.get(ptr + 1), Resource.GRAIN);
                ports3to1.add(coast.get(ptr + 4));
            }
            ptr += 5;
        }

    }



    public Resource resourceAt(Hex hex) {
        return resources.get(hex);
    }

    public int numberAt(Hex hex) {
        Integer i = numbers.get(hex);
        return i == null ? 0 : i;
    }

    public Player roadAt(Path p) {
        return roads.get(p);
    }

    public Town townAt(Xing i) {
        return towns.get(i);
    }

    public Hex robber() {
        return robber;
    }

    public Pair<Boolean, Resource> portAt(Xing i) {
        for (Path p : ports2to1.keySet()) {
            Xing[] u = endpoints(p);
            if (u[0] == i || u[1] == i)
                return Pair.make(true, ports2to1.get(p));
        }
        for (Path p : ports3to1) {
            Xing[] u = endpoints(p);
            if (u[0] == i || u[1] == i)
                return Pair.make(true, null);
        }
        return Pair.make(false, null);
    }

    public List<Town> adjacentTowns(Hex c) {
        List<Town> ans = new ArrayList<Town>();
        for (Xing i : adjacentXings(c)) {
            Town t = towns.get(i);
            if (t != null)
                ans.add(t);
        }
        return ans;
    }


    boolean canBuildTownAt(Xing i, boolean mustBeRoad, Player player) {
        if (towns.get(i) != null)
            return false;
        for (Xing j : adjacentXings(i))
            if (towns.get(j) != null)
                return false;
        if (!mustBeRoad)
            return true;
        for (Path p : adjacentPaths(i))
            if (roads.get(p) == player)
                return true;
        return false;
    }

    public boolean canBuildRoadAt(Path p, Player player) {
        if (roads.get(p) != null)
            return false;
        for (Xing i : endpoints(p)) {
            Town t = towns.get(i);
            if (t != null && t.player() != player)
                continue;
            for (Path q : adjacentPaths(i)) {
                if (q == p)
                    continue;
                if (roads.get(q) == player)
                    return true;
            }
        }
        return false;
    }





    void buildTown(Xing i, Town t) {
        towns.put(i, t);
    }

    void buildRoad(Path p, Player pl) {
        roads.put(p, pl);
    }

    void moveRobber(Hex c) {
        robber = c;
    }

    Set<Pair<Xing, Town>> allTowns() {
        Set<Pair<Xing, Town>> ans =
            new HashSet<Pair<Xing, Town>>();
        for (Xing i : towns.keySet())
            ans.add(Pair.make(i, towns.get(i)));
        return ans;
    }

    Set<Pair<Xing, Resource>> allPorts() {
        Set<Pair<Xing, Resource>> ans =
            new HashSet<Pair<Xing, Resource>>();
        for (Path p : ports2to1.keySet())
            for (Xing i : endpoints(p))
                ans.add(Pair.make(i, ports2to1.get(i)));
        for (Path p : ports3to1)
            for (Xing i : endpoints(p))
                ans.add(Pair.make(i, (Resource)null));
        return ans;
    }




    private static Hex[] hexes = new Hex[128];
    private static Path[] paths = new Path[1024];
    private static Xing[] xings = new Xing[1024];

    static {
        for (int y = 0; y <= 4; y++) {
            for (int x = Math.abs(y - 2); x <= 8 - Math.abs(y - 2); x += 2) {
                hexes[(x << 3) + y] = new Hex(x, y);
            }
        }

        for (int y = 0; y <= 4; y++) {
            for (int x = Math.abs(y - 2); x <= 8 - Math.abs(y - 2); x += 2) {
                int e = (x << 3) + y;
                Hex hex = hexes[e];
                for (int d = 0; d < 3; d++) {
                    paths[(d << 7) + e] = new Path(hex, d);
                }
            }
        }

        for (int y = 0; y <= 4; y++) {
            for (int x = Math.abs(y - 2); x <= 8 - Math.abs(y - 2); x += 2) {
                int e = (x << 3) + y;
                Hex hex = hexes[e];
                if (x + y == 2 || y == 0) paths[(3 << 7) + e] = new Path(hex, 3);
                    else paths[(3 << 7) + e] = paths[(0 << 7) + ((x - 1) << 3) + y - 1];
                if (y == 0 || x - y == 6) paths[(4 << 7) + e] = new Path(hex, 4);
                    else paths[(4 << 7) + e] = paths[(1 << 7) + ((x + 1) << 3) + y - 1];
                if (x - y == 6 || x + y == 10) paths[(5 << 7) + e] = new Path(hex, 5);
                    else paths[(5 << 7) + e] = paths[(2 << 7) + ((x + 2) << 3) + y];
            }
        }
        
        for (int y = -1; y <= 5; y++) {
            for (int x = -2; x <= 9; x++) {
                if ((x + y) % 2 == 1)
                    continue;
                for (int it = 0; it < 2; it++) {
                    Hex c1, c2, c3;
                    if (it == 0) {
                        c1 = hex(x + 1, y + 1);
                        c2 = hex(x + 2, y);
                        c3 = hex(x, y);
                    } else {
                        c1 = hex(x, y);
                        c2 = hex(x + 2, y);
                        c3 = hex(x + 1, y - 1);
                    }
                    Hex c = null;
                    int d = -1;
                    if (c1 != null) { c = c1; d = 4 + it; }
                    if (c2 != null) { c = c2; d = 2 + it; }
                    if (c3 != null) { c = c3; d = 0 + it; }
                    if (c == null) continue;
                    Xing i = new Xing(c, d);
                    if (c1 != null) xings[((4 + it) << 7) + (c1.x() << 3) + c1.y()] = i;
                    if (c2 != null) xings[((2 + it) << 7) + (c2.x() << 3) + c2.y()] = i;
                    if (c3 != null) xings[((0 + it) << 7) + (c3.x() << 3) + c3.y()] = i;
                }
            }
        }
    }

    public static Hex hex(int x, int y) {
        int ind = (x << 3) + y;
        if (ind < 0 || ind >= hexes.length || hexes[ind] == null)
            return null;
        return hexes[ind];
    }

    public static Path path(Hex hex, int direction) {
        int ind = (direction << 7) + (hex.x() << 3) + hex.y();
        if (ind < 0 || ind >= paths.length || paths[ind] == null)
            return null;
        return paths[ind];
    }

    public static Path path(int x, int y, int direction) {
        return path(hex(x, y), direction);
    }

    public static Xing xing(Hex hex, int direction) {
        int ind = (direction << 7) + (hex.x() << 3) + hex.y();
        if (ind < 0 || ind >= xings.length || xings[ind] == null)
            return null;
        return xings[ind];
    }

    public static Xing xing(int x, int y, int direction) {
        return xing(hex(x, y), direction);
    }



    public static List<Hex> adjacentHexes(Xing a) {
        List<Hex> ans = new ArrayList<Hex>(3);
        for (Hex hex : allHexes())
            for (int d = 0; d < 6; d++)
                if (xing(hex, d) == a)
                    ans.add(hex);
        return ans;
    }

    public static List<Path> adjacentPaths(Xing a) {
        List<Path> ans = new ArrayList<Path>(3);
        for (Path path : allPaths())
            if (areAdjacent(a, path))
                ans.add(path);
        return ans;
    }

    public static List<Xing> adjacentXings(Xing a) {
        List<Xing> ans = new ArrayList<Xing>(3);
        for (Xing b : allXings())
            if (areAdjacent(a, b))
                ans.add(b);
        return ans;
    }

    public static List<Xing> adjacentXings(Hex c) {
        List<Xing> ans = new ArrayList<Xing>(6);
        for (int d = 0; d < 6; d++)
            ans.add(xing(c, d));
        return ans;
    }

    public static Xing[] endpoints(Path p) {
        return new Xing[] {
            xing(p.hex(), p.direction()),
            xing(p.hex(), (p.direction() + 1) % 6),
        };
    }

    public static boolean areAdjacent(Xing a, Xing b) {
        for (Hex c : allHexes()) {
            for (int d = 0; d < 6; d++) {
                if (xing(c, d) == a && xing(c, (d + 1) % 6) == b)
                    return true;
                if (xing(c, d) == b && xing(c, (d + 1) % 6) == a)
                    return true;
            }
        }
        return false;
    }

    public static boolean areAdjacent(Xing a, Path p) {
        Xing[] ends = endpoints(p);
        return ends[0] == a || ends[1] == a;
    }

    public static boolean areAdjacent(Path p, Xing a) {
        return areAdjacent(a, p);
    }



    private static List<Hex> ALL_HEXES = null;
    public static List<Hex> allHexes() {
        if (ALL_HEXES != null)
            return ALL_HEXES;
        List<Hex> ans = new ArrayList<Hex>();
        for (int y = 0; y <= 4; y++)
            for (int x = Math.abs(y - 2); x <= 8 - Math.abs(y - 2); x += 2)
                ans.add(hex(x, y));
        ALL_HEXES = Collections.unmodifiableList(ans);
        return ALL_HEXES;
    }

    private static List<Path> ALL_PATHS = null;
    public static List<Path> allPaths() {
        if (ALL_PATHS != null)
            return ALL_PATHS;
        Set<Path> ans = new HashSet<Path>();
        for (Hex c : allHexes())
            for (int d = 0; d < 6; d++)
                ans.add(path(c, d));
        ALL_PATHS = Collections.unmodifiableList(new ArrayList<Path>(ans));
        return ALL_PATHS;
    }

    private static List<Xing> ALL_XINGS = null;
    public static List<Xing> allXings() {
        if (ALL_XINGS != null)
            return ALL_XINGS;
        Set<Xing> ans = new HashSet<Xing>();
        for (Hex c : allHexes())
            for (int d = 0; d < 6; d++)
                ans.add(xing(c, d));
        ALL_XINGS = Collections.unmodifiableList(new ArrayList<Xing>(ans));
        return ALL_XINGS;
    }

}

