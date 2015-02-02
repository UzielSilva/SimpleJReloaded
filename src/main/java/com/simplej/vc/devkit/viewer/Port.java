/*
 * Port.java
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

public class Port {

    private final Box owner;

    private int position;

    private Link link;

    public Port(Box owner, int position) {
        this.owner = owner;
        this.position = position;
    }

    public void scale(int f) {
        position *= f;
        position += f / 2;
    }

    public Box getOwner() {
        return owner;
    }

    public Link getLink() {
        return link;
    }

    public void setLink(Link link) {
        this.link = link;
    }

    public int getPosition() {
        return position;
    }

    public int getX() {
        return owner.getX() + owner.getWidth();
    }

    public int getY() {
        return owner.getY() + position;
    }

}