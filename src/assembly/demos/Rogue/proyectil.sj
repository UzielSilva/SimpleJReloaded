showAt("proyectil", 0, _linea++);

Proyectil(duenio, x, y, dir, base, sprite, velocidad, sonido, danio) {
  var activo = true;
  
  accion() {
    var quien;
    switch (dir) {
      case IZQUIERDA:
        x -= velocidad;
        quien = toca(x, y + 4);
        break;
      
      case ARRIBA:
        y -= velocidad;
        quien = toca(x + 4, y);
        break;
      
      case DERECHA:
        x += velocidad;
        quien = toca(x + 7, y + 4);
        break;
      
      case ABAJO:
        y += velocidad;
        quien = toca(x + 4, y + 7);
        break;
    }
    if (quien != null) {
      activo = false;
      audioEngine.play(sonido);
      quien.recibeMensaje(MensajeGolpe(duenio, danio));
    }
    if (!activo)
      oculta();
  }
  
  coloca(px, py) {
    setSmallSpriteImage(sprite, base + dir);
    var sx = x - px;
    var sy = y - py;
    if (sx > -16 && sx <= 270 && sy > -16 && sy <= 200)
      putSpriteAt(sprite, sx, sy);
  }
  
  oculta() {
    putSpriteAt(sprite, -16, -16);
  }
  
  return this;
}
