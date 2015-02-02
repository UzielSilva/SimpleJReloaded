/*
 SoundState.java
 Copyright (C) 2004 Gerardo Horvilleur Martinez

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/

package com.simplej.vc.hardware;

public class SoundState {

    public static final SoundState OFF = new SoundState("OFF");

    public static final SoundState ATTACK = new SoundState("ATTACK");

    public static final SoundState DECAY = new SoundState("DECAY");

    public static final SoundState SUSTAIN = new SoundState("SUSTAIN");

    public static final SoundState RELEASE = new SoundState("RELEASE");

    private final String name;

    private SoundState(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }

}
