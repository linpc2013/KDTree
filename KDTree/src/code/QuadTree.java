package code;

import java.util.HashSet;
import java.util.Set;

/*
 * QuadTree implementation
 * Features :
 * Search : Range Search
 * Insert : SingleNode insert
 * Refer to https://en.wikipedia.org/wiki/Quadtree
 * Author : linpc2013
 * */
class Point2D {
    double x, y;

    public Point2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}

class Rect {
    Point2D min, max;
    double length;

    public Rect(Point2D min, double length) {
        this.min = min;
        this.length = length;
        max = new Point2D(min.x + length, min.y + length);
    }

    public boolean containsPoint(Point2D p) {
        return min.x <= p.x && p.x <= max.x && min.y <= p.y && p.y <= max.y;
    }

    public boolean intersects(Rect r) {
        return !(r.min.x > max.x || r.min.y > max.y || r.max.x < min.x || r.max.y < min.y);
    }

    public String toString() {
        return "min : " + min + " len: " + length;
    }
}

public class QuadTree {

    class Node {
        Rect boundary;
        Node northWest, northEast, southWest, southEast;
        Point2D[] points;
        int count;

        public Node(Rect boundary) {
            this.boundary = boundary;
            points = new Point2D[nodeCapacity];
            count = 0;
        }

        public int size() {
            return count;
        }

        public void addPoint(Point2D p) {
            points[count++] = p;
        }

    }

    Node root;
    int nodeCapacity = 4;

    public QuadTree(Rect boundary, int nodeCapacity) {
        this.nodeCapacity = nodeCapacity;
        root = new Node(boundary);
    }

    public void insert(Point2D p) {
        insert(root, p);
    }

    private boolean insert(Node r, Point2D p) {
        if (r == null || !r.boundary.containsPoint(p))
            return false;
        if (r.size() < nodeCapacity) {
            r.addPoint(p);
            return true;
        }
        if (r.northWest == null)
            subdivide(r);
        if (insert(r.northWest, p) || insert(r.northEast, p) || insert(r.southWest, p) || insert(r.southEast, p))
            return true;
        return false;
    }

    private void subdivide(Node p) {
        Rect r = p.boundary;
        double nlength = r.length / 2;
        Point2D min = r.min;
        Point2D nwMin = new Point2D(min.x, min.y + nlength), neMin = new Point2D(min.x + nlength, min.y + nlength);
        Point2D swMin = min, seMin = new Point2D(min.x + nlength, min.y);
        p.northWest = new Node(new Rect(nwMin, nlength));
        p.northEast = new Node(new Rect(neMin, nlength));
        p.southWest = new Node(new Rect(swMin, nlength));
        p.southEast = new Node(new Rect(seMin, nlength));
    }

    public Set<Point2D> queryRange(Rect range) {
        Set<Point2D> res = new HashSet<Point2D>();
        queryRange(root, range, res);
        return res;
    }

    private void queryRange(Node r, Rect range, Set<Point2D> res) {
        if (r == null || !r.boundary.intersects(range))
            return;
        Point2D[] points = r.points;
        for (Point2D p : points)
            if (range.containsPoint(p))
                res.add(p);
        queryRange(r.northWest, range, res);
        queryRange(r.northEast, range, res);
        queryRange(r.southWest, range, res);
        queryRange(r.southEast, range, res);
    }

    public static void main(String[] args) {
        double[] x = { 0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9 };
        double[] y = { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1 };
        Point2D mp0 = new Point2D(0, 0);
        double len = 1;
        Rect boundary = new Rect(mp0, len);
        QuadTree qt = new QuadTree(boundary, 1);
        double num = x.length;
        for (int i = 0; i < num; i++)
            qt.insert(new Point2D(x[i], y[i]));
    }

}
