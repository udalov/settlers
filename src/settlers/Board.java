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

    public static class Cell {
        private final int x;
        private final int y;

        private Cell(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int x() { return x; }
        public int y() { return y; }
        public String toString() { return "("+x+","+y+")"; }
    }

    public static class Path {
        private final Cell cell;
        private final int direction;

        private Path(Cell cell, int direction) {
            this.cell = cell;
            this.direction = direction;
        }

        public Cell cell() { return cell; }
        public int direction() { return direction; }
        public int x() { return cell.x; }
        public int y() { return cell.y; }
        public String toString() { return "["+cell.x+","+cell.y+","+direction+"]"; }
    }

    public static class Intersection {
        private final Cell cell;
        private final int direction;

        private Intersection(Cell cell, int direction) {
            this.cell = cell;
            this.direction = direction;
        }

        public Cell cell() { return cell; }
        public int direction() { return direction; }
        public int x() { return cell.x; }
        public int y() { return cell.y; }
        public String toString() { return "{"+cell.x+","+cell.y+","+direction+"}"; }
    }


    public static final int[] DX = {1, -1, -2, -1, 1, 2};
    public static final int[] DY = {1, 1, 0, -1, -1, 0};


    
    private final Map<Cell, Resource> resources;
    private final Map<Cell, Integer> numbers;
    private final Map<Path, Player> roads;
    private final Map<Intersection, Town> towns;
    private final Map<Path, Resource> ports2to1;
    private final List<Path> ports3to1;
    private Cell robber;


    private Board(
        Map<Cell, Resource> resources,
        Map<Cell, Integer> numbers,
        Map<Path, Resource> ports2to1,
        List<Path> ports3to1
    ) {
        this.resources = resources;
        this.numbers = numbers;
        this.roads = new HashMap<Path, Player>();
        this.towns = new HashMap<Intersection, Town>();
        this.ports2to1 = ports2to1;
        this.ports3to1 = ports3to1;
        robber = null;
        for (Cell cell : resources.keySet()) {
            if (resources.get(cell) == null) {
                robber = cell;
            }
        }
    }

    static Board create(Random rnd) {
        Map<Cell, Resource> resources = new HashMap<Cell, Resource>();
        Map<Cell, Integer> numbers = new HashMap<Cell, Integer>();
        Map<Path, Resource> ports2to1 = new HashMap<Path, Resource>();
        List<Path> ports3to1 = new ArrayList<Path>();

        List<Cell> allCells = new ArrayList<Cell>(allCells());
        Collections.shuffle(allCells, rnd);

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

        for (int i = 0; i < allCells.size(); i++) {
            resources.put(allCells.get(i), allResources.get(i));
        }

        List<Integer> allNumbers = new ArrayList<Integer>();
        allNumbers.add(6);
        allNumbers.add(6);
        allNumbers.add(8);
        allNumbers.add(8);
        for (int i = 2; i <= 12; i++) {
            if (6 <= i && i <= 8) continue;
            allNumbers.add(i);
            if (i == 2 || i == 12) continue;
            allNumbers.add(i);
        }

        allCells.remove(allResources.indexOf(null));

        for (Integer number : allNumbers) {
            while (true) {
                int i = rnd.nextInt(allCells.size());
                Cell cell = allCells.get(i);
                if (numbers.containsKey(cell))
                    continue;
                if (number != 6 && number != 8) {
                    numbers.put(cell, number);
                    break;
                }
                boolean ok = true;
                for (int d = 0; d < 6; d++) {
                    int x = cell.x + DX[d];
                    int y = cell.y + DY[d];
                    Cell c = cell(x, y);
                    if (c == null || !numbers.containsKey(c))
                        continue;
                    if (numbers.get(c) == 6 || numbers.get(c) == 8) {
                        ok = false;
                        break;
                    }
                }
                if (!ok) continue;
                numbers.put(cell, number);
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



    public Resource resourceAt(Cell cell) {
        return resources.get(cell);
    }

    public int numberAt(Cell cell) {
        Integer i = numbers.get(cell);
        return i == null ? 0 : i;
    }

    public Player roadAt(Path p) {
        return roads.get(p);
    }

    public Town townAt(Intersection i) {
        return towns.get(i);
    }

    public Cell robber() {
        return robber;
    }

    public Pair<Boolean, Resource> portAt(Intersection i) {
        for (Path p : ports2to1.keySet()) {
            Intersection[] u = endpoints(p);
            if (u[0] == i || u[1] == i)
                return Pair.make(true, ports2to1.get(p));
        }
        for (Path p : ports3to1) {
            Intersection[] u = endpoints(p);
            if (u[0] == i || u[1] == i)
                return Pair.make(true, null);
        }
        return Pair.make(false, null);
    }

    public List<Town> adjacentTowns(Cell c) {
        List<Town> ans = new ArrayList<Town>();
        for (Intersection i : adjacentIntersections(c)) {
            Town t = towns.get(i);
            if (t != null)
                ans.add(t);
        }
        return ans;
    }


    // TODO: rename? Checks only existence of other towns at i and in neighbors
    public boolean canBuildTownAt(Intersection i) {
        if (towns.get(i) != null)
            return false;
        for (Intersection j : adjacentIntersections(i))
            if (towns.get(j) != null)
                return false;
        return true;
    }





    void buildTown(Intersection i, Town t) {
        towns.put(i, t);
    }

    void buildRoad(Path p, Player pl) {
        roads.put(p, pl);
    }

    void moveRobber(Cell c) {
        robber = c;
    }

    Set<Pair<Intersection, Town>> allTowns() {
        Set<Pair<Intersection, Town>> ans =
            new HashSet<Pair<Intersection, Town>>();
        for (Intersection i : towns.keySet()) {
            ans.add(Pair.make(i, towns.get(i)));
        }
        return ans;
    }




    private static Cell[] cells = new Cell[128];
    private static Path[] paths = new Path[1024];
    private static Intersection[] intss = new Intersection[1024];

    static {
        for (int y = 0; y <= 4; y++) {
            for (int x = Math.abs(y - 2); x <= 8 - Math.abs(y - 2); x += 2) {
                cells[(x << 3) + y] = new Cell(x, y);
            }
        }

        for (int y = 0; y <= 4; y++) {
            for (int x = Math.abs(y - 2); x <= 8 - Math.abs(y - 2); x += 2) {
                int e = (x << 3) + y;
                Cell cell = cells[e];
                for (int d = 0; d < 3; d++) {
                    paths[(d << 7) + e] = new Path(cell, d);
                }
            }
        }

        for (int y = 0; y <= 4; y++) {
            for (int x = Math.abs(y - 2); x <= 8 - Math.abs(y - 2); x += 2) {
                int e = (x << 3) + y;
                Cell cell = cells[e];
                if (x + y == 2 || y == 0) paths[(3 << 7) + e] = new Path(cell, 3);
                    else paths[(3 << 7) + e] = paths[(0 << 7) + ((x - 1) << 3) + y - 1];
                if (y == 0 || x - y == 6) paths[(4 << 7) + e] = new Path(cell, 4);
                    else paths[(4 << 7) + e] = paths[(1 << 7) + ((x + 1) << 3) + y - 1];
                if (x - y == 6 || x + y == 10) paths[(5 << 7) + e] = new Path(cell, 5);
                    else paths[(5 << 7) + e] = paths[(2 << 7) + ((x + 2) << 3) + y];
            }
        }
        
        for (int y = -1; y <= 5; y++) {
            for (int x = -2; x <= 9; x++) {
                if ((x + y) % 2 == 1)
                    continue;
                for (int it = 0; it < 2; it++) {
                    Cell c1, c2, c3;
                    if (it == 0) {
                        c1 = cell(x + 1, y + 1);
                        c2 = cell(x + 2, y);
                        c3 = cell(x, y);
                    } else {
                        c1 = cell(x, y);
                        c2 = cell(x + 2, y);
                        c3 = cell(x + 1, y - 1);
                    }
                    Cell c = null;
                    int d = -1;
                    if (c1 != null) { c = c1; d = 4 + it; }
                    if (c2 != null) { c = c2; d = 2 + it; }
                    if (c3 != null) { c = c3; d = 0 + it; }
                    if (c == null) continue;
                    Intersection i = new Intersection(c, d);
                    if (c1 != null) intss[((4 + it) << 7) + (c1.x << 3) + c1.y] = i;
                    if (c2 != null) intss[((2 + it) << 7) + (c2.x << 3) + c2.y] = i;
                    if (c3 != null) intss[((0 + it) << 7) + (c3.x << 3) + c3.y] = i;
                }
            }
        }
    }

    public static Cell cell(int x, int y) {
        int ind = (x << 3) + y;
        if (ind < 0 || ind >= cells.length || cells[ind] == null)
            return null;
        return cells[ind];
    }

    public static Path path(Cell cell, int direction) {
        int ind = (direction << 7) + (cell.x << 3) + cell.y;
        if (ind < 0 || ind >= paths.length || paths[ind] == null)
            return null;
        return paths[ind];
    }

    public static Path path(int x, int y, int direction) {
        return path(cell(x, y), direction);
    }

    public static Intersection ints(Cell cell, int direction) {
        int ind = (direction << 7) + (cell.x << 3) + cell.y;
        if (ind < 0 || ind >= intss.length || intss[ind] == null)
            return null;
        return intss[ind];
    }

    public static Intersection ints(int x, int y, int direction) {
        return ints(cell(x, y), direction);
    }



    public static List<Cell> adjacentCells(Intersection a) {
        List<Cell> ans = new ArrayList<Cell>(3);
        for (Cell cell : allCells())
            for (int d = 0; d < 6; d++)
                if (ints(cell, d) == a)
                    ans.add(cell);
        return ans;
    }

    public static List<Path> adjacentPaths(Intersection a) {
        List<Path> ans = new ArrayList<Path>(3);
        for (Path path : allPaths())
            if (areAdjacent(a, path))
                ans.add(path);
        return ans;
    }

    public static List<Intersection> adjacentIntersections(Intersection a) {
        List<Intersection> ans = new ArrayList<Intersection>(3);
        for (Intersection b : allIntersections())
            if (areAdjacent(a, b))
                ans.add(b);
        return ans;
    }

    public static List<Intersection> adjacentIntersections(Cell c) {
        List<Intersection> ans = new ArrayList<Intersection>(6);
        for (int d = 0; d < 6; d++)
            ans.add(ints(c, d));
        return ans;
    }

    public static Intersection[] endpoints(Path p) {
        return new Intersection[] {
            ints(p.cell, p.direction),
            ints(p.cell, (p.direction + 1) % 6),
        };
    }

    public static boolean areAdjacent(Intersection a, Intersection b) {
        for (Cell c : allCells()) {
            for (int d = 0; d < 6; d++) {
                if (ints(c, d) == a && ints(c, (d + 1) % 6) == b)
                    return true;
                if (ints(c, d) == b && ints(c, (d + 1) % 6) == a)
                    return true;
            }
        }
        return false;
    }

    public static boolean areAdjacent(Intersection a, Path p) {
        Intersection[] ends = endpoints(p);
        return ends[0] == a || ends[1] == a;
    }

    public static boolean areAdjacent(Path p, Intersection a) {
        return areAdjacent(a, p);
    }



    private static List<Cell> ALL_CELLS = null;
    public static List<Cell> allCells() {
        if (ALL_CELLS != null)
            return ALL_CELLS;
        List<Cell> ans = new ArrayList<Cell>();
        for (int y = 0; y <= 4; y++) {
            for (int x = Math.abs(y - 2); x <= 8 - Math.abs(y - 2); x += 2) {
                ans.add(cell(x, y));
            }
        }
        ALL_CELLS = Collections.unmodifiableList(ans);
        return ALL_CELLS;
    }

    private static List<Path> ALL_PATHS = null;
    public static List<Path> allPaths() {
        if (ALL_PATHS != null)
            return ALL_PATHS;
        Set<Path> ans = new HashSet<Path>();
        for (Cell c : allCells()) {
            for (int d = 0; d < 6; d++) {
                Path p = path(c, d);
                if (p != null)
                    ans.add(p);
            }
        }
        ALL_PATHS = Collections.unmodifiableList(new ArrayList<Path>(ans));
        return ALL_PATHS;
    }

    private static List<Intersection> ALL_INTSS = null;
    public static List<Intersection> allIntersections() {
        if (ALL_INTSS != null)
            return ALL_INTSS;
        Set<Intersection> ans = new HashSet<Intersection>();
        for (Cell c : allCells()) {
            for (int d = 0; d < 6; d++) {
                Intersection i = ints(c, d);
                if (i != null)
                    ans.add(i);
            }
        }
        ALL_INTSS = Collections.unmodifiableList(new ArrayList<Intersection>(ans));
        return ALL_INTSS;
    }

}

