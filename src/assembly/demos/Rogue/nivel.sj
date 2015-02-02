showAt("nivel", 0, _linea++);

Cuarto() {
  var xmin, ymin;
  var xmax, ymax;
  var conectado = false;
  var existe = true;
  var quienes = Set();
  var quienesArray = null;
  var vecinos = [this];
  var numObjetos = 0;
  return this;
}

Pasaje() {
  var quienes = Set();
  var quienesArray = null;
  var vecinos = [this];
  var numObjetos = 0;
  return this;
}

Puerta() {
  var posx, posy;
  var quienes = Set();
  var quienesArray = null;
  var vecinos = [this];
  var numObjetos = 0;
  return this;
}

modificaMapa(p, t) {
  mapa[p.y][p.x] = t;
  mapaCambio = true;
  if (t != PISO)
    lugares[p.y / 3][p.x / 3].numObjetos++;
  else
    lugares[p.y / 3][p.x / 3].numObjetos--;
}

agregaVecino(lugar, vecino) {
  var n = length(lugar.vecinos);
  var tmp = new array[n + 1];
  arrayCopy(lugar.vecinos, 0, tmp, 0, n);
  tmp[n] = vecino;
  lugar.vecinos = tmp;
}
  
hazVecinos(a, b) {
  agregaVecino(a, b);
  agregaVecino(b, a);
}

colocaObjetos(cuarto) {
  var xmin = cuarto.xmin * 3;
  var ymin = cuarto.ymin * 3;
  var xmax = cuarto.xmax * 3;
  var ymax = cuarto.ymax * 3;
  var libre = (xmax - xmin + 1) * (ymax - ymin + 1) - 9;
 
  colocaObjeto(cuarto, objeto) {
    var p = Coords();
    do {
      p.x = random(xmax - xmin + 1) + xmin;
      p.y = random(ymax - ymin + 1) + ymin;
    } while (mapa[p.y][p.x] != PISO);
    modificaMapa(p, objeto);
  }

  if (random(6) == 0) {
    colocaObjeto(cuarto, FLECHAS);
    libre--;
  }
  if (libre > 0 && random(6) == 0) {
    colocaObjeto(cuarto, TUBO_FLECHAS);
    libre--;
  }
  if (libre > 0 && random(4) == 0) {
    colocaObjeto(cuarto, ORO);
    libre--;
  }
  if (libre > 0 && random(6) == 0) {
    colocaObjeto(cuarto, FUEGO);
    libre--;
  }
  if (libre > 0 /* && random(4) == 0 */) {
    colocaObjeto(cuarto, POCION_ROJA + random(POCIONES));
    libre--;
  }
  if (libre > 0 /* && random(5) == 0 */) {
    colocaObjeto(cuarto, HECHIZO_ROJO + random(HECHIZOS));
    libre--;
  }
}

colocaMonstruos(c) {
  var xmin = c.xmin * 24;
  var ymin = c.ymin * 24;
  var ancho = (c.xmax - c.xmin + 1) * 24;
  var alto = (c.ymax - c.ymin + 1) * 24;
  var libre = ancho * alto - 2048;
  
  colocaMonstruo(monstruo) {
    if (!haySpriteEnemigo())
      return;
    var m = monstruo(obtenSpriteEnemigo());
    do {
      m.posx = random(ancho - m.ancho) + xmin;
      m.posy = random(alto - m.alto) + ymin;
      coloca(m);
    } while (choca(m, m.posx, m.posy) != null);
    agregaMonstruo(m);
    libre -= m.ancho * m.alto;
  }
  
  if (libre > 0 && random(2) == 0)
    colocaMonstruo(Escupidor);
  if (libre > 0 && random(2) == 0)
    colocaMonstruo(Gusano);
}

creaCuartos() {
  var anchoZona = ANCHO / CUARTOS_H;
  var altoZona = ALTO  / CUARTOS_V;
  cuartos = new array[CUARTOS];
  for (var i = 0; i < CUARTOS; i++)
    cuartos[i] = Cuarto();
  var noExisten = random(4);
  for (var i = 0; i < noExisten; i++) {
    var cuarto = chooseOne(cuartos);
    cuarto.existe = false;
  }
  var nLlave = 0;
  var nSalida = 0;
  for (var i = 0; i < CUARTOS_V; i++) {
    if (!cuartos[i * CUARTOS_H].existe)
      nLlave++;
    if (!cuartos[(i + 1) * CUARTOS_H - 1].existe)
      nSalida++;
  }
  if (nLlave == CUARTOS_V)
    cuartos[0].existe = true;
  if (nSalida == CUARTOS_V)
    cuartos[CUARTOS - 1].existe = true;
  for (var i = 0; i < CUARTOS; i++) {
    var cuarto = cuartos[i];
    var x = (i % CUARTOS_H) * anchoZona;
    var y = (i / CUARTOS_H) * altoZona;
    var maxAncho = anchoZona - 4;
    var maxAlto = altoZona - 4;
    if (cuarto.existe) {
      var ancho = random(maxAncho - 3) + 4;
      var alto = random(maxAlto - 3) + 4;
      cuarto.xmin =
        x + clamp(random(anchoZona), 2, anchoZona - ancho - 2);
      cuarto.ymin =
        y + clamp(random(altoZona), 2, altoZona - alto - 2);
      cuarto.xmax = cuarto.xmin + ancho - 1;
      cuarto.ymax = cuarto.ymin + alto - 1;
      for (var y = cuarto.ymin; y <= cuarto.ymax; y++) {
        var yd = y * 3;
        for (var x = cuarto.xmin; x <= cuarto.xmax; x++) {
          lugares[y][x] = cuarto;
          var xd = x * 3;
          mapa[yd][xd] = PISO;
          mapa[yd + 1][xd] = PISO;
          mapa[yd + 2][xd] = PISO;
          mapa[yd][xd + 1] = PISO;
          mapa[yd + 1][xd + 1] = PISO;
          mapa[yd + 2][xd + 1] = PISO;
          mapa[yd][xd + 2] = PISO;
          mapa[yd + 1][xd + 2] = PISO;
          mapa[yd + 2][xd + 2] = PISO;
        }
      }
      colocaObjetos(cuarto);
      colocaMonstruos(cuarto);
    } else {
      cuarto.xmin = x + anchoZona / 2;
      cuarto.ymin = y + altoZona / 2;
    }
  }
}

marcaPasaje(x, y, pasaje) {
  lugares[y][x] = pasaje;
  var xd = x * 3;
  var yd = y * 3;
  mapa[yd][xd] = PISO;
  mapa[yd + 1][xd] = PISO;
  mapa[yd + 2][xd] = PISO;
  mapa[yd][xd + 1] = PISO;
  mapa[yd + 1][xd + 1] = PISO;
  mapa[yd + 2][xd + 1] = PISO;
  mapa[yd][xd + 2] = PISO;
  mapa[yd + 1][xd + 2] = PISO;
  mapa[yd + 2][xd + 2] = PISO;
}

conecta(c1, c2) {
  cuartos[c2].conectado = true;
  conectados[c1][c2] = true;
  conectados[c2][c1] = true;
  if (c2 < c1) {
    var tmp = c1;
    c1 = c2;
    c2 = tmp;
  }
  var dir = (c1 + 1 == c2) ? DERECHA : ABAJO;
  c1 = cuartos[c1];
  c2 = cuartos[c2];
  var ix, iy, fx, fy, dist, vDist, dx, dy, vdx, vdy;
  if (dir == ABAJO) {
    dx = 0;
    dy = 1;
    if (c1.existe) {
      ix = random(c1.xmax - c1.xmin - 1) + c1.xmin + 1;
      iy = c1.ymax + 1;
    } else {
      ix = c1.xmin;
      iy = c1.ymin;
    }
    if (c2.existe) {
      fx = random(c2.xmax - c2.xmin - 1) + c2.xmin + 1;
      fy = c2.ymin - 1;
    } else {
      fx = c2.xmin;
      fy = c2.ymin;
    }
    dist = fy - iy;
    vDist = abs(ix - fx);
    vdx = (fx < ix) ? -1 : 1;
    vdy = 0;
  } else {
    dx = 1;
    dy = 0;
    if (c1.existe) {
      ix = c1.xmax + 1;
      iy = random(c1.ymax - c1.ymin - 1) + c1.ymin + 1;
    } else {
      ix = c1.xmin;
      iy = c1.ymin;
    }
    if (c2.existe) {
      fx = c2.xmin - 1;
      fy = random(c2.ymax - c2.ymin - 1) + c2.ymin + 1;
    } else {
      fx = c2.xmin;
      fy = c2.ymin;
    }
    dist = fx - ix;
    vDist = abs(iy - fy);
    vdx = 0;
    vdy = (fy < iy) ? -1 : 1;
  }
  var v = random(dist - 2) + 1;
  var x = ix;
  var y = iy;
  var pasaje = Pasaje();
  marcaPasaje(x, y, pasaje);
  while (dist-- > 0) {
    x += dx;
    y += dy;
    if (dist == v && vDist > 0)
      while (vDist-- > 0) {
        marcaPasaje(x, y, pasaje);
        x += vdx;
        y += vdy;
      }
    marcaPasaje(x, y, pasaje);
  }
  if (c1.existe)
    hazVecinos(c1, pasaje);
  else
    agregaVecino(c1, pasaje);
  if (c2.existe)
    hazVecinos(c2, pasaje);
  else
    agregaVecino(c2, pasaje);
}

creaPasajes() {
  for (var i = 0; i < CUARTOS; i++)
    for (var j = 0; j < CUARTOS; j++)
      conectados[i][j] = false;
  var cuantos = 1;
  var c1 = random(CUARTOS);
  cuartos[c1].conectado = true;
  do {
    var c2 = null;
    for (var i = 0; i < CUARTOS; i++)
      if (cercanos[c1][i] && !cuartos[i].conectado) {
        c2 = i;
        break;
      }
    if (c2 == null)
      do
        c1 = random(CUARTOS);
      while (!cuartos[c1].conectado);
    else {
      conecta(c1, c2);
      cuantos++;
    }
  } while (cuantos < CUARTOS);
  for (cuantos = random(5); cuantos > 0; cuantos--) {
    c1 = random(CUARTOS);
    var c2 = null;
    for (var i = 0; i < CUARTOS; i++)
      if (cercanos[c1][i] && !conectados[c1][i]) {
        c2 = i;
        break;
      }
    if (c2 != null)
      conecta(c1, c2);
  }
  for (var i = 0; i < CUARTOS; i++) {
    var c = cuartos[i];
    if (!c.existe) {
      var n = length(c.vecinos);
      for (var i = 0; i < n - 1; i++)
        for (var j = i + 1; j < n; j++)
          hazVecinos(c.vecinos[i], c.vecinos[j]);
    }
  }
}

creaPuerta() {
  puerta = Puerta();
  var c;
  do {
    c = cuartos[(random(CUARTOS_V) + 1) * CUARTOS_H - 1];
  } while (!c.existe);
  puerta.posx = c.xmax + 1;
  puerta.posy = random(c.ymax - c.ymin + 1) + c.ymin;
  lugares[puerta.posy][puerta.posx] = puerta;
  var x = puerta.posx * 3;
  var y = puerta.posy * 3;
  for (var i = 0; i < 9; i++)
    mapa[y + i / 3][x + i % 3] = PUERTA_CERRADA + i;
  hazVecinos(c, puerta);
}

colocaLlave() {
  var c;
  do {
    c = cuartos[random(CUARTOS_V) * CUARTOS_H];
  } while (!c.existe);
  var xmin = c.xmin * 3;
  var ymin = c.ymin * 3;
  var xmax = c.xmax * 3;
  var ymax = c.ymax * 3;
  var p = Coords();
  do {
    p.x = random(xmax - xmin + 1) + xmin;
    p.y = random(ymax - ymin + 1) + ymin;
  } while (mapa[p.y][p.x] != PISO);
  modificaMapa(p, LLAVE);
}

colocaAmuleto() {
  var c;
  do {
    c = chooseOne(cuartos);
  } while (!c.existe);
  var xmin = c.xmin * 3;
  var ymin = c.ymin * 3;
  var xmax = c.xmax * 3;
  var ymax = c.ymax * 3;
  var p = Coords();
  do {
    p.x = random(xmax - xmin + 1) + xmin;
    p.y = random(ymax - ymin + 1) + ymin;
  } while (mapa[p.y][p.x] != PISO);
  modificaMapa(p, AMULETO);
}

abrePuerta() {
  var x = puerta.posx * 3;
  var y = puerta.posy * 3;
  for (var i = 0; i < 9; i++)
    mapa[y + i / 3][x + i % 3] = PUERTA_ABIERTA + i;
  mapaCambio = true;
}

colocaJugador() {
  var c;
  do
    c = chooseOne(cuartos);
  while (!c.existe);
  var xmin = c.xmin * 24;
  var ymin = c.ymin * 24;
  var ancho = (c.xmax - c.xmin + 1) * 24;
  var alto = (c.ymax - c.ymin + 1) * 24;
  do {
    jugador.posx = random(ancho - 10) + xmin;
    jugador.posy = random(alto - 16) + ymin;
    coloca(jugador);
  } while (hayAlgo(jugador) != null ||
           choca(jugador, jugador.posx, jugador.posy) != null);
}

creaNivel() {
  for (var i = 0; i < ALTO * 3; i++)
    for (var j = 0; j < ANCHO * 3; j++)
      mapa[i][j] = ROCA;
  for (var i = 0; i < ALTO; i++)
    for (var j = 0; j < ANCHO; j++)
      lugares[i][j] = null;
  resetSpritesEnemigosYProyectiles();
  resetMonstruos();
  creaCuartos();
  creaPasajes();
  creaPuerta();
  colocaLlave();
  if (nivel >= NIVEL_AMULETO && !jugador.tieneAmuleto)
    colocaAmuleto();
  colocaJugador();
  mapaCambio = true;
}

noEsRoca(x, y) { return mapa[y / 8][x / 8] != ROCA; }

esRoca(x, y) { return mapa[y / 8][x / 8] == ROCA; }

toca(x, y) {
  if (esRoca(x, y))
    return personajeNivel;
  var vecinos = lugares[y / 24][x / 24].vecinos;
  var n = length(vecinos);
  for (var i = 0; i < n; i++) {
    var quienes = quienesArray(vecinos[i]);
    var nn = length(quienes);
    for (var ii = 0; ii < nn; ii++) {
      var quien = quienes[ii];
      if (quien.posx <= x && quien.posx + quien.ancho > x &&
          quien.posy <= y && quien.posy + quien.alto > y)
        return quien;
    }
  }
  return null;
}

choca(yo, x, y) {
  var ancho = yo.ancho - 1;
  var alto = yo.alto - 1;
  if (esRoca(x, y) || esRoca(x + ancho, y) ||
      esRoca(x, y + alto) || esRoca(x + ancho, y + alto))
    return personajeNivel;
  var vecinos = yo.lugar.vecinos;
  var n = length(vecinos);
  for (var i = 0; i < n; i++) {
    var quienes = quienesArray(vecinos[i]);
    var nn = length(quienes);
    for (var ii = 0; ii < nn; ii++) {
      var quien = quienes[ii];
      if (quien == yo)
        continue;
      if (x + ancho < quien.posx)
        continue;
      if (x >= quien.posx + quien.ancho)
        continue;
      if (y + alto < quien.posy)
        continue;
      if (y >= quien.posy + quien.alto)
        continue;
      return quien;
    }
  }
  return null;
}

dejaAlgo(quien, prob, que) {
  if (random(prob) != 0)
    return;
  var p = Coords();
  p.x = (quien.posx + quien.ancho / 2) / 8;
  p.y = (quien.posy + quien.alto / 2) / 8;
  if (mapa[p.y][p.x] != PISO)
    return;
  modificaMapa(p, que);
  audioEngine.play(sonidos.DEJA_ALGO);
}

hayAlgo(quien) {
  if (quien.lugar.numObjetos == 0)
    return null;
  var p = Coords();
  var x = quien.posx;
  var y = quien.posy;
  var ancho = quien.ancho;
  var alto = quien.alto;
  p.x = x / 8;
  p.y = y / 8;
  if (mapa[p.y][p.x] != PISO)
    return p;
  p.x = (x + ancho) / 8;
  if (mapa[p.y][p.x] != PISO)
    return p;
  p.y = (y + alto) / 8;
  if (mapa[p.y][p.x] != PISO)
    return p;
  p.x = x / 8;
  if (mapa[p.y][p.x] != PISO)
    return p;
  p.x = (x + ancho / 2) / 8;
  p.y = (y + alto / 2) / 8;
  if (mapa[p.y][p.x] != PISO)
    return p;
  return null;
}

hizoRuido(quien) {
  var x = (quien.posx + quien.ancho / 2) / 24;
  var y = (quien.posy + quien.alto / 2) / 24;
  var lugar = lugares[y][x];
  var a = quienesArray(quien.lugar);
  var n = length(a);
  var msj = MensajeRuido(quien);
  for (var i = 0; i < n; i++)
    a[i].recibeMensaje(msj);
}

veo(yo, quien) {
  var x = (yo.posx + yo.ancho / 2) / 24;
  var y = (yo.posy + yo.alto / 2) / 24;
  var lugar = lugares[y][x];
  if (!lugar.quienes.contains(quien))
    return false;
  switch (yo.dir) {
    case ARRIBA:
      if (yo.posy <= quien.posy + quien.alto)
        return false;
      break;
    
    case ABAJO:
      if (yo.posy + yo.alto >= quien.posy)
        return false;
      break;
    
    case IZQUIERDA:
      if (yo.posx <= quien.posx + quien.ancho)
        return false;
      break;
    
    case DERECHA:
      if (yo.posx + yo.ancho >= quien.posx)
        return false;
      break;
  }
  return true;
}

quienesPut(lugar, quien) {
  lugar.quienes.put(quien);
  lugar.quienesArray = null;
}

quienesRemove(lugar, quien) {
  lugar.quienes.remove(quien);
  lugar.quienesArray = null;
}

quienesArray(lugar) {
  if (lugar.quienesArray == null)
    lugar.quienesArray = lugar.quienes.toArray();
  return lugar.quienesArray;
}

coloca(quien) {
  var x = (quien.posx + quien.ancho / 2) / 24;
  var y = (quien.posy + quien.alto / 2) / 24;
  quien.lugar = lugares[y][x];
  quienesPut(quien.lugar, quien);
}

quita(quien) {
  var x = (quien.posx + quien.ancho / 2) / 24;
  var y = (quien.posy + quien.alto / 2) / 24;
  quien.lugar = lugares[y][x];
  quienesRemove(quien.lugar, quien);
}

mueve(quien, x, y) {
  var vx = (quien.posx + quien.ancho / 2) / 24;
  var vy = (quien.posy + quien.alto / 2) / 24;
  var nx = (x + quien.ancho / 2) / 24;
  var ny = (y + quien.alto / 2) / 24;
  quien.lugar = lugares[vy][vx];
  var lugar = lugares[ny][nx];
  if (lugar != quien.lugar) {
    quienesRemove(quien.lugar, quien);
    if (quien == jugador) {
      var msj = Mensaje(MSJ_SALIO);
      var a = quienesArray(quien.lugar);
      var n = length(a);
      for (var i = 0; i < n; i++)
        a[i].recibeMensaje(msj);
      msj = Mensaje(MSJ_ENTRO);
      a = quienesArray(lugar);
      n = length(a);
      for (var i = 0; i < n; i++)
        a[i].recibeMensaje(msj);
    }
    quien.lugar = lugar;
    quienesPut(lugar, quien);
    quien.recibeMensaje(Mensaje(MSJ_CAMBIO_LUGAR));
  }
  quien.posx = x;
  quien.posy = y;
}

mueveMismoLugar(p) {
  var x = p.posx + p.ancho / 2;
  var y = p.posy + p.alto / 2;
  switch (p.dir) {
    case ARRIBA:
      y -= p.velocidad;
      break;
    
    case ABAJO:
      y += p.velocidad;
      break;
    
    case IZQUIERDA:
      x -= p.velocidad;
      break;
    
    case DERECHA:
      x += p.velocidad;
      break;
  }
  return p.lugar == lugares[y / 24][x / 24];
}

intentaMuevePersonaje(p) {
  var velocidad = p.velocidad;
  var x = p.posx;
  var y = p.posy;
  var quien;
  switch (p.dir) {
    case ARRIBA:
      quien = choca(p, x, y - velocidad);
      if (quien == null)
        mueve(p, x, y - velocidad);
      else {
        var v = velocidad - 1;
        while (v > 0 && choca(p, x, y - v) != null)
          v--;
        if (v != 0)
          mueve(p, x, y - v);
      }
      break;
    
    case ABAJO:
      quien = choca(p, x, y + velocidad);
      if (quien == null)
        mueve(p, x, y + velocidad);
      else {
        var v = velocidad - 1;
        while (v > 0 && choca(p, x, y + v) != null)
          v--;
        if (v != 0)
          mueve(p, x, y + v);
      }
      break;
      
    case IZQUIERDA:
      quien = choca(p, x - velocidad, y);
      if (quien == null)
        mueve(p, x - velocidad, y);
      else {
        var v = velocidad - 1;
        while (v > 0 && choca(p, x - v, y) != null)
          v--;
        if (v != 0)
          mueve(p, x - v, y);
      }
      break;
    
    case DERECHA:
      quien = choca(p, x + velocidad, y);
      if (quien == null)
        mueve(p, x + velocidad, y);
      else {
        var v = velocidad + 1;
        while (v > 0 && choca(p, x + v, y) != null)
          v--;
        if (v != 0)
          mueve(p, x + v, y);
      }
      break;
  }
  return quien;
}

muestraMundo(x, y) {
  x = clamp(x, MIN_X, MAX_X);
  y = clamp(y, MIN_Y, MAX_Y);
  var xt = x / 8;
  var xp = x % 8;
  var yt = y / 8;
  var yp = y % 8;
  if (mapaCambio || xt != xtViejo || yt != ytViejo) {
    for (var r = 0; r < 25; r++)
      arrayPoke(r * 64, mapa[yt + r], xt, 33);
    mapaCambio = false;
    xtViejo = xt;
    ytViejo = yt;
  }
  setSmoothScroll(xp, yp);
  jugador.coloca(x, y);
  muestraMonstruos(x, y);
}

PersonajeNivel() {
  recibeMensaje(msj) {
    return true;
  }
  
  return this;
}

initProcs.put(lambda() {
  for (var i = 0; i < CUARTOS; i++)
    for (var j = 0; j < CUARTOS; j++)
      cercanos[i][j] = false;
  for (var x = 0; x < CUARTOS_H - 1; x++)
    for (var y = 0; y < CUARTOS_V - 1; y++) {
      var a = y * CUARTOS_H + x;
      cercanos[a][a + 1] = true;
      cercanos[a + 1][a] = true;
      cercanos[a][a + CUARTOS_H] = true;
      cercanos[a + CUARTOS_H][a] = true;
    }
  for (var y = 0; y < CUARTOS_V - 1; y++) {
    var a = y * CUARTOS_H + CUARTOS_H - 1;
    cercanos[a][a + CUARTOS_H] = true;
    cercanos[a + CUARTOS_H][a] = true;
  }
  for (var x = 0; x < CUARTOS_H - 1; x++) {
    var a = (CUARTOS_V - 1) * CUARTOS_H + x;
    cercanos[a][a + 1] = true;
    cercanos[a + 1][a] = true;
  }
  personajeNivel = PersonajeNivel();
});
