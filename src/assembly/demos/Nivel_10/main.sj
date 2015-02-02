final VELERO_DERECHA = 0;
final VELERO_IZQUIERDA = 1;
final AMBULANCIA_DERECHA = 2;
final AMBULANCIA_IZQUIERDA = 3;
final CAMION_DERECHA = 4;
final CAMION_IZQUIERDA = 5;
final MILITAR_DERECHA = 6;
final MILITAR_IZQUIERDA = 7;
final COCHE_DERECHA = 8;
final COCHE_IZQUIERDA = 9;
final AVION_DERECHA = 10;
final AVION_IZQUIERDA = 11;

/* Leer definiciones de tiles creadas con
   el tiles editor */
var tilesData = readTilesFile("tilesEditorDemo.tmap");

/* Poner colores en el mapa de colores */
for (var i = 0; i < 16; i++)
  setTileColor(i, tilesData.colors[i].red,
                  tilesData.colors[i].green,
                  tilesData.colors[i].blue);

/* Grabar nuevas definiciones de tiles */
for (var i = 0; i < 256; i++)
  setTilePixels(i, tilesData.pixels[i]);

/* Dibujar la pantalla */
for (var r = 0; r < 24; r++)  
  for (var c = 0; c < 32; c++)
    putAt(tilesData.rows[r][c], c, r);
    
/* Leer definiciones de sprites creadas con
   el sprites editor */
var spritesData = readSpritesFile("sprites.smap");

/* Poner colores en el mapa de colores */
for (var i = 0; i < 15; i++)
  setSpriteColor(i, spritesData.colors[i].red,
                    spritesData.colors[i].green,
                    spritesData.colors[i].blue);

/* Grabar nuevas definiciones de sprites */
for (var i = 0; i < 12; i++)
  setLargeSpritePixels(i, spritesData.largePixels[i]);

/* Datos para los sprites de los vehiculos */
var vehiculos = [
  {pixeles: VELERO_DERECHA, x: 20, vx: 1, y: 175},
  {pixeles: VELERO_DERECHA, x: 100, vx: 1, y: 172},
  {pixeles: VELERO_IZQUIERDA, x: 250, vx: -1, y: 165},
  {pixeles: AMBULANCIA_DERECHA, x: 40, vx: 2, y: 150},
  {pixeles: CAMION_DERECHA, x: 100, vx: 2, y: 150},
  {pixeles: CAMION_DERECHA, x: 170, vx: 2, y: 150},
  {pixeles: COCHE_DERECHA, x: 10, vx: 3, y: 145},
  {pixeles: COCHE_DERECHA, x: 100, vx: 3, y: 145},
  {pixeles: COCHE_DERECHA, x: 200, vx: 3, y: 145},
  {pixeles: COCHE_IZQUIERDA, x: 200, vx: -3, y: 137},
  {pixeles: COCHE_IZQUIERDA, x: 150, vx: -3, y: 137},
  {pixeles: COCHE_IZQUIERDA, x: 100, vx: -3, y: 137},
  {pixeles: COCHE_IZQUIERDA, x: 50, vx: -3, y: 137},
  {pixeles: CAMION_IZQUIERDA, x: 10, vx: -2, y:132},
  {pixeles: MILITAR_IZQUIERDA, x: 100, vx: -2, y:132},
  {pixeles: MILITAR_IZQUIERDA, x: 120, vx: -2, y:132},
  {pixeles: MILITAR_IZQUIERDA, x: 140, vx: -2, y:132},
  {pixeles: MILITAR_IZQUIERDA, x: 160, vx: -2, y:132},
  {pixeles: MILITAR_IZQUIERDA, x: 180, vx: -2, y:132},
  {pixeles: AVION_IZQUIERDA, x: 100, vx: -4, y: 70},
  {pixeles: AVION_DERECHA, x: 0, vx: 4, y: 30}
];

/* Seleccionar imagenes para los sprites */
for (var i = 0; i < length(vehiculos); i++)
  setLargeSpriteImage(i, vehiculos[i].pixeles);

vbi() {
  for (var i = 0; i < length(vehiculos); i++) {
    putSpriteAt(i, vehiculos[i].x, vehiculos[i].y);
    vehiculos[i].x = vehiculos[i].x + vehiculos[i].vx;
    if (vehiculos[i].vx > 0 && vehiculos[i].x >= 256)
      vehiculos[i].x = -16;
    if (vehiculos[i].vx < 0 && vehiculos[i].x <= -16)
      vehiculos[i].x = 256;
  }
}