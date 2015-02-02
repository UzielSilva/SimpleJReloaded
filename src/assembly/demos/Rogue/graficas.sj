showAt("graficas", 0, _linea++);

instalaTilesJuego() {
  /* Poner colores en el mapa de colores */
  for (var i = 0; i < 16; i++)
    setTileColor(i, tilesData.colors[i].red,
                    tilesData.colors[i].green,
                    tilesData.colors[i].blue);
  
  /* Grabar nuevas definiciones de tiles */
  for (var i = 0; i < 256; i++)
    setTilePixels(i, tilesData.pixels[i]);

  var tmp = {
    fuegos: [new array[32], new array[32], new array[32]],
    amuletos: [new array[32], new array[32]],
    flechaIzquierda: new array[32],
    flechaDerecha: new array[32],
    vacio: new array[32]
  };
  for (var i = 0; i < 32; i++) {
    tmp.fuegos[0][i] = peek(TILE_IMGS + FUEGO * 32 + i);
    tmp.fuegos[1][i] = peek(TILE_IMGS + FUEGO * 32 + 32 + i);
    tmp.fuegos[2][i] = peek(TILE_IMGS + FUEGO * 32 + 64 + i);
    tmp.amuletos[0][i] = peek(TILE_IMGS + AMULETO * 32 + i);
    tmp.amuletos[1][i] = peek(TILE_IMGS + AMULETO * 32 + 32 + i);
    tmp.flechaIzquierda[i] =
      peek(TILE_IMGS + FLECHA_IZQUIERDA * 32 + i);
    tmp.flechaDerecha[i] = peek(TILE_IMGS + FLECHA_DERECHA * 32 + i);
    tmp.vacio[i] = peek(TILE_IMGS + PISO * 32 + i);
  }
  tilesAnimacion = tmp;
}


setScreen(data) {
  arrayPoke(TILE_CMAP, data, 0, 32);
  arrayPoke(TILE_IMGS, data, TILE_DATA_OFFSET, 8192);
  for (var i = 0; i < 24; i++)
    arrayPoke(i * 64, data, SCREEN_DATA_OFFSET + i * 32, 32);
}

initProcs.put(lambda() {
  simpleJScreenData = readFile("simpleJ.tmap");
  ienjiniaScreenData = readFile("ienjinia.tmap");
  rogueScreenData = readFile("rogue.tmap");
  /* Leer definiciones de tiles creadas con
    el tiles editor */
  tilesData = readTilesFile("tiles.tmap");

  /* Leer definiciones de sprites creadas con
    el sprites editor */
  spritesData = readSpritesFile("sprites.smap");
  
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
});
