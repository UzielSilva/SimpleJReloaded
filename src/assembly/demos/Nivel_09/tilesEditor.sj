/* Leer definiciones de tiles creadas con
   el tiles editor */
var tilesData = readTilesFile("tileseditorDemo.tmap");

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

pause(1);