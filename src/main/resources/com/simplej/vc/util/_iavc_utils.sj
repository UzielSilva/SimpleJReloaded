final pokew(addr, w) {
  poke(addr, w >> 8);
  poke(addr + 1, w);
}

final setScreenOffset(x, y) {
  pokew(0x8c0, (y & 0x7) * 64 + (x & 0x1f));
}

final setSmoothScroll(x, y) {
  poke(0x8c2, ((y & 0xf) << 4) | (x & 0xf));
}

final setTileColor(index, red, green, blue) {
  pokew(0x800 + ((index & 0xf) << 1),
        ((red & 0x1f) << 10) | ((green & 0x1f) << 5) | (blue & 0x1f));
}

final setTilePixels(index, pixels) {
  var addr = 0x2000 + ((index & 0xff) << 5);
  for (var i = 0; i < 64; i += 2) {
    poke(addr, ((pixels[i] & 0xf) << 4) | (pixels[i + 1] & 0xf));
    addr++;
  }
}

final setSpriteColor(index, red, green, blue) {
  pokew(0x820 + ((index & 0xf) << 1),
        ((red & 0x1f) << 10) | ((green & 0x1f) << 5) | (blue & 0x1f));
}

final setSmallSpriteImage(spriteIndex, imageIndex) {
  poke(0x840 + ((spriteIndex & 0x1f) << 2),
       0x80 | (imageIndex & 0x7f));
}

final setLargeSpriteImage(spriteIndex, imageIndex) {
  poke(0x840 + ((spriteIndex & 0x1f) << 2),
       imageIndex & 0x7f);
}

final putSpriteAt(spriteIndex, x , y) {
  x += 16;
  y += 16;
  var base = 0x840 + ((spriteIndex & 0x1f) << 2);
  poke(base + 1, x);
  poke(base + 3, x >> 8);
  poke(base + 2, y);
}

final setSmallSpritePixels(index, pixels) {
  var addr = 0x1000 + ((index & 0x7f) << 5);
  for (var i = 0; i < 64; i += 2) {
    poke(addr, ((pixels[i] & 0xf) << 4) | (pixels[i + 1] & 0xf));
    addr++;
  }
}

final setLargeSpritePixels(index, pixels) {
  var addr = 0x4000 + ((index & 0x7f) << 7);
  for (var i = 0; i < 256; i += 2) {
    poke(addr, ((pixels[i] & 0xf) << 4) | (pixels[i + 1] & 0xf));
    addr++;
  }
}

final isButtonDown(buttons, mask) {
  return (buttons & mask) != 0;
}

final setSoundFrequency(channel, frequency) {
  pokew(0x8c6 + ((channel & 0x3) << 4),
        frequency);
}

final setSoundAttack(channel, attack) {
  pokew(0x8c8 + ((channel & 0x3) << 4),
        attack);
}

final setSoundDecay(channel, decay) {
  pokew(0x8ca + ((channel & 0x3) << 4),
        decay);
}

final setSoundRelease(channel, release) {
  pokew(0x8cc + ((channel & 0x3) << 4),
        release);
}

final setSoundVolume(channel, volume) {
  var addr = 0x8ce + ((channel & 0x3) << 4);
  poke(addr, (peek(addr) & 0xf0) | (volume & 0xf));
}

final setSoundSustain(channel, sustain) {
  var addr = 0x8ce + ((channel & 0x3) << 4);
  poke(addr, (peek(addr) & 0xcf) | ((sustain & 0x3) << 4));
}

final soundOn(channel) {
  var addr = 0x8ce + ((channel & 0x3) << 4);
  poke(addr, (peek(addr) & 0x7f) | 0x80);
}

final soundOff(channel) {
  var addr = 0x8ce + ((channel & 0x3) << 4);
  poke(addr, (peek(addr) & 0xbf) | 0x40);
}

final setSoundWave(channel, waveform) {
  var addr = 0xc00 + ((channel & 0x3) << 8);
  arrayPoke(addr, waveform, 0, 256);
}

final memCardLoad() {
  var data = new array[512];
  poke(0x906, 0x2);
  while (peek(0x906) != 0)
    ;
  for (var i = 0; i < 512; i++)
    data[i] = peek(0xa00 + i);
  return data;
}

final memCardSave(data) {
  for (var i = 0; i < 512; i++)
    poke(0xa00 + i, data[i]);
  poke(0x906, 0x1);
  while (peek(0x906) != 0)
    ;
}
