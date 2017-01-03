package code;

import java.util.HashSet;
import java.util.Set;
/*
 * OctTree implementation
 * Features :
 * Search : Range Search
 * Insert : SingleNode insert
 * Refer to https://en.wikipedia.org/wiki/Octree
 * Author : linpc2013
 * */
class Point3D {
    double x, y, z;

    public Point3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }
}

class Cubiod {
    Point3D min, max;
    double length;

    public Cubiod(Point3D min, double length) {
        this.min = min;
        this.length = length;
        max = new Point3D(min.x + length, min.y + length, min.z + length);
    }

    public boolean containsPoint(Point3D p) {
        return min.x <= p.x && p.x <= max.x && min.y <= p.y && p.y <= max.y && min.z <= p.z && p.z <= max.z;
    }

    public boolean intersects(Cubiod r) {
        return !(r.min.x > max.x || r.min.y > max.y || r.max.x < min.x || r.max.y < min.y || r.max.z < min.z || r.min.z > max.z);
    }

    public String toString() {
        return "min : " + min + " len: " + length;
    }
}

public class OctTree {
    final int SIZE = 8;

    class Node {
        Cubiod boundary;
        Node[] dirs;
        Point3D[] points;
        int count;

        public Node(Cubiod boundary) {
            this.boundary = boundary;
            points = new Point3D[nodeCapacity];
            dirs = new Node[SIZE];
            count = 0;
        }

        public int size() {
            return count;
        }

        public void addPoint(Point3D p) {
            points[count++] = p;
        }
    }

    Node root;
    int nodeCapacity = 4;

    public OctTree(Cubiod boundary, int nodeCapacity) {
        this.nodeCapacity = nodeCapacity;
        root = new Node(boundary);
    }

    public void insert(Point3D p) {
        insert(root, p);
    }

    private boolean insert(Node r, Point3D p) {
        if (r == null || !r.boundary.containsPoint(p))
            return false;
        if (r.size() < nodeCapacity) {
            r.addPoint(p);
            return true;
        }
        if (r.dirs[0] == null)
            subdivide(r);
        for (int i = 0; i < SIZE; i++)
            if (insert(r.dirs[i], p))
                return true;
        return false;
    }

    private void subdivide(Node r) {
        Point3D min = r.boundary.min;
        double len = r.boundary.length / 2.0;
        for (int i = 0; i < SIZE; i++) {
            Point3D m = new Point3D(min.x + ((i & 1) > 0 ? len : 0), min.y + ((i & 2) > 0 ? len : 0), min.z + ((i & 4) > 0 ? len : 0));
            r.dirs[i] = new Node(new Cubiod(m, len));
        }
    }

    public Set<Point3D> queryRange(Cubiod range) {
        Set<Point3D> res = new HashSet<Point3D>();
        queryRange(root, range, res);
        return res;
    }

    private void queryRange(Node r, Cubiod range, Set<Point3D> res) {
        if (r == null || !r.boundary.intersects(range))
            return;
        for (int i = 0; i < r.size(); i++) {
            Point3D p = r.points[i];
            if (range.containsPoint(p))
                res.add(p);
        }
        for (int i = 0; i < SIZE; i++)
            queryRange(r.dirs[i], range, res);
    }

    public static void main(String[] args) {
        double[] x = { 0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8 };
        double[] y = { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9 };
        double[] z = { 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1 };
        Point3D mp0 = new Point3D(0, 0, 0);
        double len = 1;
        Cubiod boundary = new Cubiod(mp0, len);
        OctTree oct = new OctTree(boundary, 1);
        double num = x.length;
        for (int i = 0; i < num; i++)
            oct.insert(new Point3D(x[i], y[i], z[i]));
    }

}
