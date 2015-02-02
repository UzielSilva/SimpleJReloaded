/*
 * PlaceAndRoute.java
 * Copyright (C) 2006 Gerardo Horvilleur Martinez 
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package com.simplej.vc.devkit.viewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.*;

import javax.swing.*;

public class PlaceAndRoute {

    private final static int LINE_PENALTY = 1000;

    private final static int TURN_PENALTY = 900;

    private final static int PORT_RESERVED = 3;

    private final static int UP = 1;

    private final static int DOWN = 2;

    private final static int LEFT = 3;

    private final static int RIGHT = 4;

    private final static int UP_LEFT = 5;

    private final static int UP_RIGHT = 6;

    private final static int DOWN_LEFT = 7;

    private final static int DOWN_RIGHT = 8;

    private final static int LEFT_UP = 9;

    private final static int LEFT_DOWN = 10;

    private final static int RIGHT_UP = 11;

    private final static int RIGHT_DOWN = 12;

    private Dimensions dimensions;

    private List mainBoxes;

    private List links;

    private Set placedBoxes;

    private int width;

    private int height;

    private Node[] heap = new Node[100000];

    private int count;

    private int maxCount;

    private boolean[] used;

    private boolean[] lineUsed;

    private Node[] best;

    private static class Node {
        int x;

        int y;

        int cost;

        int estimatedCost;

        Node path;

        int orientation;

        int index;

        static int count;

        void set(int x, int y, int cost, int estimatedCost,
                 int orientation, Node path) {
            this.x = x;
            this.y = y;
            this.cost = cost;
            this.estimatedCost = estimatedCost;
            this.orientation = orientation;
            this.path = path;
            count++;
        }
    }

    public PlaceAndRoute(Dimensions dimensions) {
        this.dimensions = dimensions;
    }

    public int getUnit() {
        return dimensions.getUnit();
    }

    public int getWidth() {
        return width * getUnit();
    }

    public int getHeight() {
        return height * getUnit();
    }

    public Box getBoxAt(Point p) {
        Iterator iter = placedBoxes.iterator();
        while (iter.hasNext()) {
            Box box = (Box) iter.next();
            if (box.contains(p))
                return box;
        }
        return null;
    }

    public void paint(Graphics g, Rectangle clip) {
        if (placedBoxes == null)
            return;
        g.setColor(Color.BLACK);
        Iterator iter = placedBoxes.iterator();
        while (iter.hasNext()) {
            Box box = (Box) iter.next();
            box.paint(g, clip);
        }
        g.setColor(Color.BLACK);
        iter = links.iterator();
        while (iter.hasNext()) {
            Link link = (Link) iter.next();
            byte[] path = link.getPath();
            if (path == null)
                continue;
            Port src = link.getSrc();
            int x = src.getX();
            int y = src.getY();
            int cx = (x - 2) * getUnit();
            g.drawLine(cx, y * getUnit() + getUnit() / 2,
                       x * getUnit(), y * getUnit() + getUnit() / 2);
            g.fillOval(cx - getUnit() / 4, y * getUnit() + getUnit() / 4,
                       getUnit() / 2, getUnit() / 2);
            for (int i = 0; i < path.length; i++) {
                switch (path[i]) {
                    case UP:
                        g.drawLine(x * getUnit() + getUnit() / 2,
                                   y * getUnit(),
                                   x * getUnit() + getUnit() / 2,
                                   y * getUnit() + getUnit());
                        y--;
                        break;
                        
                    case DOWN:
                        g.drawLine(x * getUnit() + getUnit() / 2,
                                   y * getUnit(),
                                   x * getUnit() + getUnit() / 2,
                                   y * getUnit() + getUnit());
                        y++;
                        break;

                    case LEFT:
                        g.drawLine(x * getUnit(),
                                   y * getUnit() + getUnit() / 2,
                                   x * getUnit() + getUnit(),
                                   y * getUnit() + getUnit() / 2);
                        x--;
                        break;
                        
                    case RIGHT:
                        g.drawLine(x * getUnit(),
                                   y * getUnit() + getUnit() / 2,
                                   x * getUnit() + getUnit(),
                                   y * getUnit() + getUnit() / 2);
                        x++;
                        break;

                    case UP_LEFT:
                        g.drawArc(x * getUnit() - getUnit() / 2,
                                  y * getUnit() + getUnit() / 2,
                                  getUnit(), getUnit(),
                                  0, 90);
                        x--;
                        break;

                    case UP_RIGHT:
                        g.drawArc(x * getUnit() + getUnit() / 2,
                                  y * getUnit() + getUnit() / 2,
                                  getUnit(), getUnit(),
                                  90, 90);
                        x++;
                        break;

                    case DOWN_LEFT:
                        g.drawArc(x * getUnit() - getUnit() / 2,
                                  y * getUnit() - getUnit() / 2,
                                  getUnit(), getUnit(),
                                  270, 90);
                        x--;
                        break;

                    case DOWN_RIGHT:
                        g.drawArc(x * getUnit() + getUnit() / 2,
                                  y * getUnit() - getUnit() / 2,
                                  getUnit(), getUnit(),
                                  180, 90);
                        x++;
                        break;

                    case LEFT_UP:
                        g.drawArc(x * getUnit() + getUnit() / 2,
                                  y * getUnit() - getUnit() / 2,
                                  getUnit(), getUnit(),
                                  180, 90);
                        y--;
                        break;

                    case LEFT_DOWN:
                        g.drawArc(x * getUnit() + getUnit() / 2,
                                  y * getUnit() + getUnit() / 2,
                                  getUnit(), getUnit(),
                                  90, 90);
                        y++;
                        break;

                    case RIGHT_UP:
                        g.drawArc(x * getUnit() - getUnit() / 2,
                                  y * getUnit() - getUnit() / 2,
                                  getUnit(), getUnit(),
                                  270, 90);
                        y--;
                        break;
                        
                    case RIGHT_DOWN:
                        g.drawArc(x * getUnit() - getUnit() / 2,
                                  y * getUnit() + getUnit() / 2,
                                  getUnit(), getUnit(),
                                  0, 90);
                        y++;
                        break;
                }
            }
            Polygon poly = new Polygon();
            switch (path[path.length - 1]) {
                case UP:
                case LEFT_UP:
                case RIGHT_UP:
                    y++;
                    poly.addPoint(x * getUnit() + getUnit() / 2, y * getUnit());
                    poly.addPoint(x * getUnit(), y * getUnit() + getUnit());
                    poly.addPoint(x * getUnit() + getUnit(),
                                  y * getUnit() + getUnit());
                    break;

                case DOWN:
                case LEFT_DOWN:
                case RIGHT_DOWN:
                    poly.addPoint(x * getUnit() + getUnit() / 2, y * getUnit());
                    poly.addPoint(x * getUnit(), y * getUnit() - getUnit());
                    poly.addPoint(x * getUnit() + getUnit(),
                                  y * getUnit() - getUnit());
                    break;

                case RIGHT:
                case UP_RIGHT:
                case DOWN_RIGHT:
                    poly.addPoint(x * getUnit(), y * getUnit() + getUnit() / 2);
                    poly.addPoint(x * getUnit() - getUnit(), y * getUnit());
                    poly.addPoint(x * getUnit() - getUnit(),
                                  y * getUnit() + getUnit());
                    break;
            }
            g.fillPolygon(poly);
        }
    }
    
    public void addMainBox(Box box) {
        mainBoxes.add(box);
    }

    public void addLink(Port src, Box dst) {
        links.add(new Link(src, dst));
    }

    private void queueReset() {
        count = 0;
        for (int i = 0; i < best.length; i++)
            best[i] = null;
    }

    private boolean queueIsEmpty() {
        return count == 0;
    }

    private void removeNode(int i) {
        Node last = heap[count];
        heap[count] = null;
        --count;
        while (2 * i < count + 1) {
            int child = 2 * i;
            if (child + 1 < count + 1 &&
                heap[child + 1].estimatedCost < heap[child].estimatedCost)
                child++;
            if (last.estimatedCost <= heap[child].estimatedCost)
                break;
            heap[i] = heap[child];
            heap[i].index = i;
            i = child;
        }
        heap[i] = last;
        heap[i].index = i;
    }

    private Node queueGet() {
        if (count == 0)
            return null;
        Node result = heap[1];
        removeNode(1);
        return result;
    }

    private void queueInsert(int x, int y, int cost,
                             int estimatedCost, int orientation, Node path) {
        int offset = y * width + x;
        Node node = best[offset];
        if (node != null && node.cost <= cost)
            return;
        if (node != null)
            removeNode(best[offset].index);
        else {
            node = new Node();
            best[offset] = node;
        }
        node.set(x, y, cost, estimatedCost, orientation, path);
        count++;
        if (count == heap.length) {
            Node[] tmp = new Node[heap.length * 2];
            System.arraycopy(heap, 0, tmp, 0, heap.length);
            heap = tmp;
        }
        maxCount = Math.max(count, maxCount);
        int i = count;
        while (i > 1 && heap[i / 2].estimatedCost > node.estimatedCost) {
            heap[i] = heap[i / 2];
            heap[i].index = i;
            i /= 2;
        }
        heap[i] = node;
        heap[i].index = i;
    }

    private void setUsed(int x, int y) {
        used[y * width + x] = true;
    }

    private void resetUsed(int x, int y) {
        used[y * width + x] = false;
    }

    private boolean isUsed(int x, int y) {
        return used[y * width + x];
    }

    private void setLineUsed(int x, int y) {
        lineUsed[y * width + x] = true;
    }

    private boolean isLineUsed(int x, int y) {
        return lineUsed[y * width + x];
    }

    private int dist(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) / 2 + Math.abs(y1 - y2);
    }

    private void foundPath(Link link, Node node) {
        int n = 0;
        Node p = node;
        while (p != null) {
            setLineUsed(p.x, p.y);
            p = p.path;
            n++;
        }
        byte[] path = new byte[n];
        int previous = LEFT;
        p = node;
        int i = 0;
        while (p != null) {
            int current = p.orientation;
            switch (current) {
                case UP:
                    switch (previous) {
                        case UP:
                            path[i] = DOWN;
                            break;

                        case DOWN:
                            throw new RuntimeException("path backtrack!");

                        case LEFT:
                            path[i] = RIGHT_DOWN;
                            break;

                        case RIGHT:
                            path[i] = LEFT_DOWN;
                            break;
                    }
                    break;

                case DOWN:
                    switch (previous) {
                        case UP:
                            throw new RuntimeException("path backtrack!");

                        case DOWN:
                            path[i] = UP;
                            break;

                        case LEFT:
                            path[i] = RIGHT_UP;
                            break;

                        case RIGHT:
                            path[i] = LEFT_UP;
                            break;
                    }
                    break;

                case LEFT:
                    switch (previous) {
                        case UP:
                            path[i] = DOWN_RIGHT;
                            break;

                        case DOWN:
                            path[i] = UP_RIGHT;
                            break;

                        case LEFT:
                            path[i] = RIGHT;
                            break;

                        case RIGHT:
                            throw new RuntimeException("path backtrack!");
                    }
                    break;

                case RIGHT:
                    switch (previous) {
                        case UP:
                            path[i] = DOWN_LEFT;
                            break;

                        case DOWN:
                            path[i] = UP_LEFT;
                            break;

                        case LEFT:
                            throw new RuntimeException("path backtrack!");

                        case RIGHT:
                            path[i] = LEFT;
                            break;
                    }
                    break;
            }
            previous = current;
            i++;
            p = p.path;
        }
        link.setPath(path);
    }

    private void route() {
        Iterator iter = placedBoxes.iterator();
        while (iter.hasNext()) {
            Box box = (Box) iter.next();
            box.scale(dimensions.getScaling());
            width = Math.max(width, box.getX() + box.getWidth());
            height = Math.max(height, box.getY() + box.getHeight());
        }
        width += 30;
        height += 10;
        used = new boolean[width * height];
        lineUsed = new boolean[width * height];
        best = new Node[width * height];
        iter = placedBoxes.iterator();
        while (iter.hasNext()) {
            Box box = (Box) iter.next();
            int xMin = box.getX();
            int yMin = box.getY();
            int w = box.getWidth();
            int h = box.getHeight();
            for (int y = 0; y < h; y++)
                for (int x = 0; x < w; x++)
                    setUsed(xMin + x, yMin + y);
            if (box.isCollapsed())
                continue;
            for (int i = 0; i < box.getPortCount(); i++) {
                Port port = box.getPort(i);
                for (int j = 0; j < PORT_RESERVED; j++)
                    setUsed(port.getX() + j, port.getY());
            }
        }
        iter = links.iterator();
        while (iter.hasNext()) {
            Link link = (Link) iter.next();
            Port src = link.getSrc();
            Box dst = link.getDst();
            int targetX = src.getX();
            int targetY = src.getY();
            for (int j = 0; j < PORT_RESERVED; j++)
                resetUsed(targetX + j, targetY);
            int xMin = dst.getX();
            int yMin = dst.getY();
            int w = dst.getWidth();
            int h = dst.getHeight();
            queueReset();
            for (int y = 1; y < h - 1; y++) {
                queueInsert(xMin - 1, yMin + y, -5,
                            dist(targetX, targetY, xMin - 1, yMin + y),
                            LEFT,
                            null);
            }
            for (int x = 1; x < w - 1; x++) {
                queueInsert(xMin + x, yMin - 1, 0,
                            dist(targetX, targetY, xMin + x, yMin - 1),
                            UP,
                            null);
                queueInsert(xMin + x, yMin + h, 0,
                            dist(targetX, targetY, xMin + x, yMin + h),
                            DOWN,
                            null);
            }
            boolean success = false;
            Node.count = 0;
            Node node;
            while ((node = queueGet()) != null) {
                if (node.x == targetX && node.y == targetY) {
                    foundPath(link, node);
                    success = true;
                    break;
                }
                if (node.x > 0 && !isUsed(node.x - 1, node.y)) {
                    int cost = node.cost +
                        (isLineUsed(node.x - 1, node.y) ? LINE_PENALTY : 1) +
                        (node.orientation != LEFT ? TURN_PENALTY : 0);
                    queueInsert(node.x - 1, node.y, cost,
                                cost + dist(targetX, targetY,
                                            node.x - 1, node.y),
                                LEFT,
                                node);
                }
                if (node.y > 0 && !isUsed(node.x, node.y - 1)) {
                    int cost = node.cost +
                        (isLineUsed(node.x, node.y - 1) ? LINE_PENALTY : 1) +
                        (node.orientation != UP ? TURN_PENALTY : 0);
                    queueInsert(node.x, node.y - 1, cost,
                                cost + dist(targetX, targetY,
                                            node.x, node.y - 1),
                                UP,
                                node);
                }
                if (node.y < height - 1 && !isUsed(node.x, node.y + 1)) {
                    int cost = node.cost +
                        (isLineUsed(node.x, node.y + 1) ? LINE_PENALTY : 1) +
                        (node.orientation != DOWN ? TURN_PENALTY : 0);
                    queueInsert(node.x, node.y + 1, cost,
                                cost + dist(targetX, targetY,
                                            node.x, node.y + 1),
                                DOWN,
                                node);
                }
                if (node.x < width - 1 && !isUsed(node.x + 1, node.y)) {
                    int cost = node.cost +
                        (isLineUsed(node.x + 1, node.y) ? LINE_PENALTY : 1) +
                        (node.orientation != RIGHT ? TURN_PENALTY : 0);
                    queueInsert(node.x + 1, node.y, cost,
                                cost + dist(targetX, targetY,
                                            node.x + 1, node.y),
                                RIGHT,
                                node);
                }
            }
            if (!success) {
                System.out.println("Failed! " + node.count);
            }
        }
    }

    private Cell buildCell(Box box) {
        if (placedBoxes.contains(box))
            return null;
        placedBoxes.add(box);
        int n = box.getPortCount();
        Cell cell = new BoxCell(box);
        if (n == 0 || box.isCollapsed())
            return cell;
        List boxes = new ArrayList();
        for (int i = 0; i < n; i++) {
            Port port = box.getPort(i);
            boxes.add(port.getLink().getDst());
        }
        CompositeCell children = place(boxes, false);
        children.addCenteredLeft(cell, 3);
        return children;
    }

    private CompositeCell place(List boxes, boolean isMainBoxes) {
        List cells = new ArrayList();
        Cell previousCell = null;
        Iterator iter = boxes.iterator();
        while (iter.hasNext()) {
            Box box = (Box) iter.next();
            if (!isMainBoxes && mainBoxes.contains(box))
                continue;
            Cell cell = buildCell(box);
            if (cell != null) {
                if (previousCell != null)
                    cell.placeBelow(previousCell);
                cells.add(cell);
                previousCell = cell;
            }
        }
        return new CompositeCell(cells);
    }

    private void place() {
        Cell cell = place(mainBoxes, true);
        cell.translate(1, 1);
    }

    public void init() {
        mainBoxes = new ArrayList();
        links = new ArrayList();
        placedBoxes = new HashSet();
        width = 0;
        height = 0;
    }

    public void placeAndRoute() {
        place();
        route();
    }

}