/*
 Audio.java
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

import javax.sound.sampled.*;

public class Audio implements Runnable {

    private final static int AUDIO = 0x8c6;

    private final static int AUDIO_SAMPLES = 0xc00;

    private final static float SAMPLE_RATE = 44100;

    private final static float SOUND_FRAMES_PER_SECOND = 25;

    private final static long WAIT_TIME =
        (long) (1000 / SOUND_FRAMES_PER_SECOND);

    private final static int TO_MILLIS = (int) (SAMPLE_RATE / 1000);

    private final byte[] ram;

    private SFI sfi;

    private ExceptionHandler exceptionHandler;

    private SourceDataLine sdl;

    private byte[] samples;

    private int size;

    private int[] scntr = new int[4];
    
    private long[] amp = new long[4];

    private long[] dAmp = new long[4];

    private int[] incr = new int[4];

    private long[] vol = new long[4];

    private int[] attack = new int[4];

    private int[] decay = new int[4];

    private int[] sustain = new int[4];

    private int[] release = new int[4];

    private int[] timeCntr = new int[4];

    private SoundState[] state = new SoundState[4];

    private boolean[] on = new boolean[4];

    private boolean[] off = new boolean[4];

    private boolean audioDelayWorkaround;

    private Thread thread;

    private boolean frozen;

    private boolean doReset;

    public Audio(byte[] ram) {
        this.ram = ram;
        thread = new Thread(this);
        thread.setPriority(Thread.MAX_PRIORITY);
    }

    public void start() {
        thread.start();
    }

    public synchronized void freeze() {
        frozen = true;
    }

    public synchronized void unfreeze() {
        frozen = false;
        notify();
    }

    public synchronized void reset() {
        doReset = true;
    }

    public void setSFI(SFI sfi) {
        this.sfi = sfi;
    }

    public void setExceptionHandler(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    public void setAudioDelayWorkaround(boolean v) {
        audioDelayWorkaround = v;
    }

    public boolean isAudioDelayWorkaroundEnabled() {
        return audioDelayWorkaround;
    }

    private void init() throws LineUnavailableException {
        AudioFormat af = new AudioFormat(SAMPLE_RATE, 16, 1, true, true);
        DataLine.Info dli = new DataLine.Info(SourceDataLine.class, af);
        sdl = (SourceDataLine) AudioSystem.getLine(dli);
        size = (int) (SAMPLE_RATE / SOUND_FRAMES_PER_SECOND) * 2;
        samples = new byte[size];
        sdl.open(af, size * 2);
        FloatControl volume =
            (FloatControl) sdl.getControl(FloatControl.Type.MASTER_GAIN);
        volume.setValue(volume.getMaximum());
        sdl.start();
        for (int i = 0; i < 4; i++)
            state[i] = SoundState.OFF;
    }

    private void readRAM() {
        for (int i = 0; i < 4; i++) {
            int base = i << 4;
            incr[i] = ((ram[AUDIO + base] & 0xff) << 8) |
                (ram[AUDIO + base + 1] & 0xff);
            vol[i] = ((long) (ram[AUDIO + base + 8] & 0x0f)) << 33;
            attack[i] = (((ram[AUDIO + base + 2] & 0xff) << 8) |
                         (ram[AUDIO + base + 3] & 0xff)) * TO_MILLIS;
            decay[i] = (((ram[AUDIO + base + 4] & 0xff) << 8) |
                        (ram[AUDIO + base + 5] & 0xff)) * TO_MILLIS;
            sustain[i] = (ram[AUDIO + base + 8] & 0x30) >> 4;
            release[i] = (((ram[AUDIO + base + 6] & 0xff) << 8) |
                          (ram[AUDIO + base + 7] & 0xff)) * TO_MILLIS;
            on[i] = (ram[AUDIO + base + 8] & 0x80) != 0;
            off[i] = (ram[AUDIO + base + 8] & 0x40) != 0;
        }
    }

    private void doOn(int ch) {
        dAmp[ch] = (vol[ch] - amp[ch]) / (attack[ch] + 1);
        state[ch] = SoundState.ATTACK;
        timeCntr[ch] = attack[ch] + 1;
        ram[AUDIO + (ch << 4) + 8] &= 0x7f;
        on[ch] = false;
    }

    private void doOff(int ch) {
        dAmp[ch] = -amp[ch] / (release[ch] + 1);
        timeCntr[ch] = release[ch] + 1;
        state[ch] = SoundState.RELEASE;
        ram[AUDIO + (ch << 4) + 8] &= 0xbf;
        off[ch] = false;
    }

    private boolean checkOn(int ch) {
        if (on[ch]) {
            doOn(ch);
            return true;
        } else
            return false;
    }

    private boolean checkOff(int ch) {
        if (off[ch]) {
            doOff(ch);
            return true;
        } else
            return false;
    }

    private boolean checkOnOff(int ch) {
        if (checkOn(ch))
            return true;
        if (checkOff(ch))
            return true;
        return false;
    }

    public void run() {
        try {
            init();
            boolean soundWasOn = false;
            while (true) {
                synchronized (this) {
                    if (doReset)
                        for (int i = 0; i < 4; i++) {
                            state[i] = SoundState.OFF;
                            amp[i] = 0;
                            timeCntr[i] = 0;
                            ram[AUDIO + (i << 4) + 8] &= 0x3f;
                            doReset = false;
                        }
                    while (frozen)
                        wait();
                }
                synchronized (ram) {
                    readRAM();
                }
                boolean soundIsOn = false;
                long start = System.currentTimeMillis();
                for (int i = 0; i < size; i += 2) {
                    long v = 0;
                    for (int ch = 0; ch < 4; ch++) {
                        if (state[ch] == SoundState.OFF)
                            checkOn(ch);
                        else if (state[ch] == SoundState.ATTACK) {
                            if (checkOnOff(ch))
                                continue;
                            else if (timeCntr[ch] == 0) {
                                dAmp[ch] =
                                    ((vol[ch] * sustain[ch] / 3) - (vol[ch])) /
                                    (decay[ch] + 1);
                                timeCntr[ch] = decay[ch] + 1;
                                state[ch] = SoundState.DECAY;
                            } else {
                                timeCntr[ch]--;
                                amp[ch] += dAmp[ch];
                            }
                        } else if (state[ch] == SoundState.DECAY) {
                            if (checkOnOff(ch))
                                continue;
                            else if (timeCntr[ch] == 0) {
                                state[ch] = SoundState.SUSTAIN;
                                amp[ch] = vol[ch] * sustain[ch] / 7;
                            } else {
                                timeCntr[ch]--;
                                amp[ch] += dAmp[ch];
                            }
                        } else if (state[ch] == SoundState.SUSTAIN) {
                            amp[ch] = vol[ch] * sustain[ch] / 3;
                            checkOnOff(ch);
                        } else if (state[ch] == SoundState.RELEASE) {
                            if (checkOn(ch))
                                continue;
                            else if (timeCntr[ch] == 0) {
                                amp[ch] = 0;
                                state[ch] = SoundState.OFF;
                            } else {
                                timeCntr[ch]--;
                                amp[ch] += dAmp[ch];
                            }
                        }
                        v += ram[AUDIO_SAMPLES + (ch << 8) +
                                 ((scntr[ch] >> 12) & 0xff)] * amp[ch];
                        scntr[ch] += incr[ch];
                    }
                    v >>= 32;
                    samples[i] = (byte) (v >> 8);
                    samples[i + 1] = (byte) v;
                    if (v != 0L)
                        soundIsOn = true;
                }
                if (!audioDelayWorkaround || soundIsOn || soundWasOn) {
                    sdl.write(samples, 0, size);
                    soundWasOn = soundIsOn;
                } else
                    try {
                        sdl.drain();
                        Thread.sleep(Math.max(WAIT_TIME + start -
                                              System.currentTimeMillis() - 10L,
                                              0L));
                    } catch (InterruptedException e) {
                        // Should not happen
                    }
                if (sfi != null) {
                    Throwable error = null;
                    try {
                        sfi.sfi();
                    } catch (Throwable t) {
                        error = t;
                    }
                    if (error != null && exceptionHandler != null)
                        exceptionHandler.sfiException(error);
                }
            }
        } catch (LineUnavailableException e) {
            // TODO: what?
            System.out.println(e);
        } catch (InterruptedException e) {
            // Shouldn't happen
            e.printStackTrace();
        }
    }

}
