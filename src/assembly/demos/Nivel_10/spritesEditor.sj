/* Leer definiciones de sprites creadas con
   el sprites editor */
var spritesData = readSpritesFile("sprites.smap");

/* Poner colores en el mapa de colores */
for (var i = 0; i < 15; i++)
  setSpriteColor(i, spritesData.colors[i].red,
                    spritesData.colors[i].green,
                    spritesData.colors[i].blue);

/* Grabar nuevas definiciones de sprites de 16 por 16 */
for (var i = 0; i < 128; i++)
  setLargeSpritePixels(i, spritesData.largePixels[i]);

/* Grabar nuevas definiciones de sprites de 8 por 8 */
for (var i = 0; i < 128; i++)
  setSmallSpritePixels(i, spritesData.smallPixels[i]);

/* Muestra las imagenes */
for (var i = 0; i < 12; i++) {
  setLargeSpriteImage(i, i);
  putSpriteAt(i, i * 16, i * 8);
}