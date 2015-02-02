final BUTTONS = 0x8c4;
final BTN_UP = 0x01;
final BTN_DOWN = 0x02;
final BTN_LEFT = 0x04;
final BTN_RIGHT = 0x08;
final BTN_SPACE = 0x40;
final BTN_PAUSE = 0x80;

getCharAtPixel(x, y) {
  return peek((y / 8) * 64 + x / 8);
}

displayNumber(n, col) {
  var s = "" + n;
  showAt(s, col - length(s), 0);
}

// Read artwork file (created with tiles editor)
var tilesData = readTilesFile("design.tmap");

// Copy to characters colormap
for (var i = 0; i < 16; i++)
  setTileColor(i, tilesData.colors[i].red,
                  tilesData.colors[i].green,
                  tilesData.colors[i].blue);

// Copy to sprites colormap
for (var i = 0; i < 15; i++)
  setSpriteColor(i, tilesData.colors[i].red,
                    tilesData.colors[i].green,
                    tilesData.colors[i].blue);

// Copy to tiles pixels
for (var i = 0; i < 256; i++)
  setTilePixels(i, tilesData.pixels[i]);

// Copy last 4 "rows" to sprite map
for (var i = 0; i < 64; i++)
  setSmallSpritePixels(i, tilesData.pixels[192 + i]);

// Read eyes dirs map
var tmp = readFile("eyes.tmap");

// Copy to eyes dirs map
var eyesDirs = new array[768];
for (var i = 0; i < 768; i++)
  eyesDirs[i] = tmp[8192 + 32 + i];

getEyeDir(x, y) {
  return eyesDirs[(y / 8) * 32 + (x / 8)];
}

final GAME_STATE_PLAY = 1;
final GAME_STATE_SHOW_DEATH = 2;
final GAME_STATE_DYING = 3;
final GAME_STATE_EAT_ENEMY_BONUS = 4;
final GAME_STATE_DISPLAY_BOARD = 5;
final GAME_STATE_READY = 6;
final GAME_STATE_GAME_OVER = 7;
final GAME_STATE_PAUSE = 8;

var prevState;
var gameState;
var lives;
var generalTimer;

final POWER_DOT = 0;
final DOT = 1;
final EMPTY = 2;
final DOOR = 3;
final WARP = 4;

final DOT_VALUE = 5;
final POWER_DOT_VALUE = 25;
final TOTAL_DOTS = 247;
final EXTRA_LIFE = 10000;

var score = 0;
var hiScore;

readHiScore() {
  var data = memCardLoad();
  hiScore = ((data[0] & 0xff) << 16) |
            ((data[1] & 0xff) << 8) |
            (data[2] & 0xff);
}

writeHiScore() {
  var data = new array[512];
  for (var i = 0; i < 512; i++)
    data[i] = 0;
  data[0] = ((hiScore >> 16) & 0xff);
  data[1] = ((hiScore >> 8) & 0xff);
  data[2] = (hiScore & 0xff);
  memCardSave(data);
}

displayScore() {
  if (score > hiScore)
    hiScore = score;
  displayNumber(score, 31);
  displayNumber(hiScore, 17);
}

abs(n) {
  return n >= 0 ? n : -n;
}

incrScore(n) {
  var t1 = score / EXTRA_LIFE;
  score += n;
  var t2 = score / EXTRA_LIFE;
  if (t2 > t1) {
    displayLives(lives);
    lives = min([lives + 1, 8]);
  }
  displayScore();
}

readHiScore();

final ENTER_RIGHT_X = 232;
final ENTER_LEFT_X = 8;

final PLAYER_SPRITE = 6;
final PLAYER_BASE = [16, 20, 24, 28];
final PLAYER_DYING_BASE = 1;
final PLAYER_LEFT = 0;
final PLAYER_UP = 1;
final PLAYER_RIGHT = 2;
final PLAYER_DOWN = 3;
final PLAYER_INIT_X = 120;
final PLAYER_INIT_Y = 144;

var playerSpeed = 24;
var playerSpeedCntr = 0;

var playerX, playerY, playerDir, playerCycle, playerCycleDir, eatenDotsCount,
    playerDyingCntr;

final PLAYER_LIVES_SPRITES = 10;

final ENEMY_UP = 0;
final ENEMY_DOWN = 1;
final ENEMY_LEFT = 2;
final ENEMY_RIGHT = 3;

final SCARED_ENEMY_IMAGE = 0;
final EYES_BASE = 32;

final NORMAL_ENEMY_STATE = 1;
final SCARED_ENEMY_STATE = 2;
final EYES_ENEMY_STATE = 3;

final EYES_TARGET_X = 120;
final EYES_TARGET_Y = 96;
final EYES_SPEED = 40;

final AI_WANDER_PHASE = 1;
final AI_ATTACK_PHASE = 2;

final EAT_ENEMY_BONUS_BASE = 36;
final EAT_ENEMY_BONUS_SPRITES = 0;

final SLOW_TUNNEL_Y = 12 * 8;
final SLOW_TUNNEL_LEFT = 5 * 8;
final SLOW_TUNNEL_RIGHT = 26 * 8;

var aiWavePhase, aiWaveCounter, aiWaveTime, scaredTimer;
var eatEnemyBonus, eatEnemyBonusTimer;

oppositeDir(dir) {
  if (dir == ENEMY_UP)
    return ENEMY_DOWN;
  if (dir == ENEMY_DOWN)
    return ENEMY_UP;
  if (dir == ENEMY_LEFT)
    return ENEMY_RIGHT;
  return ENEMY_LEFT;
}

Enemy(sprite, base, initX, initY, initDir, defaultSpeed, slowDown,
      preferredDirs, frontOffset, sideOffset) {
  var x, y, dir, state, speed, speedCntr;
  
  var oppositePreferredDirs = new array[4];
  for (var i = 0; i < 4; i++)
    oppositePreferredDirs[i] = oppositeDir(preferredDirs[i]);

  touchesPlayer() {
    if (playerX > x + 6)
      return false;
    if (playerX + 6 < x)
      return false;
    if (playerY > y + 6)
      return false;
    if (playerY + 6 < y)
      return false;
    return true;
  }

  reset() {
    x = initX;
    y = initY;
    dir = initDir;
    speed = defaultSpeed;
    state = NORMAL_ENEMY_STATE;
    speedCntr = 0;
  }

  scare() {
    if (state == NORMAL_ENEMY_STATE) {
      state = SCARED_ENEMY_STATE;
      speed -= slowDown;
      dir = oppositeDir(dir);
    }
  }

  unscare() {
    if (state == SCARED_ENEMY_STATE) {
      state = NORMAL_ENEMY_STATE;
      speed += slowDown;
    }
  }

  checkCollision() {
    if (touchesPlayer()) {
      if (state == NORMAL_ENEMY_STATE) {
        die();
        return true;
      } else if (state == SCARED_ENEMY_STATE) {
        state = EYES_ENEMY_STATE;
        speed = EYES_SPEED;
        putSpriteAt(sprite, -16, -16);
        setEatEnemyBonus(playerX, playerY);
        return true;
      }
    }
    return false;
  }

  hide() {
    putSpriteAt(sprite, -16, -16);
  }

  getDirections() {
    var result = Queue();
    if (getCharAtPixel(x, y - 1) <= DOOR)
      result.put(ENEMY_UP);
    var ch = getCharAtPixel(x, y + 8);
    if (ch <= EMPTY ||(state == EYES_ENEMY_STATE && ch == DOOR))
      result.put(ENEMY_DOWN);
    if (getCharAtPixel(x - 1, y) <= WARP)
      result.put(ENEMY_LEFT);
    if (getCharAtPixel(x + 8, y) <= WARP)
      result.put(ENEMY_RIGHT);
    return result;
  }

  doWanderAI() {
    if (x % 8 == 0 && y % 8 == 0) {
      var dirs = getDirections();
      if (dirs.size() == 1)
        dir = dirs.get();
      else
        for (var i = 0; i < 4; i++)
          if (dir != oppositePreferredDirs[i] &&
              dirs.contains(preferredDirs[i])) {
            dir = preferredDirs[i];
            break;
          }
    }
  }

  doAttackAI() {
    if (x % 8 == 0 && y % 8 == 0) {
      var targetX = playerX;
      var targetY = playerY;
      if (playerDir == PLAYER_RIGHT) {
        targetX += frontOffset;
        targetY += sideOffset;
      } else if (playerDir == PLAYER_LEFT) {
        targetX -= frontOffset;
        targetY -= sideOffset;
      } else if (playerDir == PLAYER_DOWN) {
        targetY += frontOffset;
        targetX -= sideOffset;
      } else {
        targetY -= frontOffset;
        targetX += sideOffset;
      }
      var dx = targetX - x;
      var dy = targetY - y;
      if (state == SCARED_ENEMY_STATE) {
        dx = -dx;
        dy = -dy;
      }
      var priorities = Queue();
      if (abs(dx) > abs(dy)) {
        if (dx < 0 && dir != ENEMY_RIGHT)
          priorities.put(ENEMY_LEFT);
        if (dx > 0 && dir != ENEMY_LEFT)
          priorities.put(ENEMY_RIGHT);
        if (dy < 0 && dir != ENEMY_DOWN)
          priorities.put(ENEMY_UP);
        if (dy > 0 && dir != ENEMY_UP)
          priorities.put(ENEMY_DOWN);
      } else {
        if (dy < 0 && dir != ENEMY_DOWN)
          priorities.put(ENEMY_UP);
        if (dy > 0 && dir != ENEMY_UP)
          priorities.put(ENEMY_DOWN);
        if (dx < 0 && dir != ENEMY_RIGHT)
          priorities.put(ENEMY_LEFT);
        if (dx > 0 && dir != ENEMY_LEFT)
          priorities.put(ENEMY_RIGHT);
      }
      var dirs = getDirections();
      priorities = toArray(priorities);
      for (var i = 0; i < length(priorities); i++) {
        var priority = priorities[i];
        if (dirs.contains(priority)) {
          dir = priority;
          return;
        }
      }
      doWanderAI();
    }
  }
  
  doEyesAI() {
    if (x % 8 == 0 && y % 8 == 0)
      dir = getEyeDir(x, y);
  }
  
  doAI() {
    if (state == EYES_ENEMY_STATE)
      doEyesAI();
    else if (aiWavePhase == AI_WANDER_PHASE)
      doWanderAI();
    else
      doAttackAI();
  }
    
  doIt() {
    var efectiveSpeed = speed;
    if (y == SLOW_TUNNEL_Y &&
        (x < SLOW_TUNNEL_LEFT || x > SLOW_TUNNEL_RIGHT))
      efectiveSpeed -= 6;
    var newSpeedCntr = speedCntr + efectiveSpeed;
    var moves = (newSpeedCntr >> 4) - (speedCntr >> 4);
    speedCntr = newSpeedCntr;
    for (var i = 0; i < moves; i++) {
      // Do AI
      doAI();
      
      // Update position
      switch (dir) {
        case ENEMY_LEFT:
          var ch = getCharAtPixel(x - 1, y);
          if (ch <= DOOR) {
            x -= 1;
          } else if (ch == WARP) {
            x = ENTER_RIGHT_X;
          }
          break;
            
        case ENEMY_RIGHT:
          var ch = getCharAtPixel(x + 8, y);
          if (ch <= DOOR) {
            x += 1;
          } else if (ch == WARP) {
            x = ENTER_LEFT_X;
          }
          break;
            
        case ENEMY_UP:
          if (getCharAtPixel(x, y - 1) <= DOOR) {
            y -= 1;
          }
          break;
            
        case ENEMY_DOWN:
          if (getCharAtPixel(x, y + 8) <= DOOR) {
            y += 1;
          }
          break;
      }
      putSpriteAt(sprite, x, y);
      
      if (state == EYES_ENEMY_STATE &&
        x == EYES_TARGET_X &&
        y == EYES_TARGET_Y) {
        state = NORMAL_ENEMY_STATE;
        speed = 18;
      }
      
      // Update sprite image
      if (state == EYES_ENEMY_STATE)
          setSmallSpriteImage(sprite,
                              EYES_BASE + dir);
      else if (state == SCARED_ENEMY_STATE &&
               (scaredTimer > 30 || (scaredTimer & 4) == 4))
        setSmallSpriteImage(sprite, SCARED_ENEMY_IMAGE);
      else
        setSmallSpriteImage(sprite, base + dir);
    }
  }

  return this;
}

var enemies = [
    Enemy(2, 48, 120, 80, ENEMY_RIGHT, 22, 10,
          [ENEMY_UP, ENEMY_RIGHT, ENEMY_DOWN, ENEMY_LEFT], 0, 0),
    Enemy(3, 52, 136, 96, ENEMY_RIGHT, 18, 6,
          [ENEMY_DOWN, ENEMY_LEFT, ENEMY_UP, ENEMY_RIGHT], -16, 0),
    Enemy(4, 56, 128, 96, ENEMY_RIGHT, 18, 6,
          [ENEMY_DOWN, ENEMY_RIGHT, ENEMY_UP, ENEMY_LEFT], 0, -16),
    Enemy(5, 60, 114, 96, ENEMY_LEFT, 18, 6,
          [ENEMY_UP, ENEMY_LEFT, ENEMY_DOWN, ENEMY_RIGHT], 40, 0)
];

setEatEnemyBonus(x, y) {
  setSmallSpriteImage(EAT_ENEMY_BONUS_SPRITES,
                      EAT_ENEMY_BONUS_BASE);
  putSpriteAt(EAT_ENEMY_BONUS_SPRITES, x, y);
  if (eatEnemyBonus == 200)
    setSmallSpriteImage(EAT_ENEMY_BONUS_SPRITES + 1,
                        EAT_ENEMY_BONUS_BASE + 1);
  else if (eatEnemyBonus == 400)
    setSmallSpriteImage(EAT_ENEMY_BONUS_SPRITES + 1,
                        EAT_ENEMY_BONUS_BASE + 2);
  else if (eatEnemyBonus == 800)
    setSmallSpriteImage(EAT_ENEMY_BONUS_SPRITES + 1,
                        EAT_ENEMY_BONUS_BASE + 3);
  else
    setSmallSpriteImage(EAT_ENEMY_BONUS_SPRITES + 1,
                        EAT_ENEMY_BONUS_BASE + 4);
  putSpriteAt(EAT_ENEMY_BONUS_SPRITES + 1, x - 8, y);
  incrScore(eatEnemyBonus);
  eatEnemyBonus *= 2;
  gameState = GAME_STATE_EAT_ENEMY_BONUS;
  eatEnemyBonusTimer = 25;
}

displayLives(n) {
  for (var i = 0; i < 10; i++) {
    if (i < n) {
      putSpriteAt(PLAYER_LIVES_SPRITES + i, 244, 16 + i * 10);
      setSmallSpriteImage(PLAYER_LIVES_SPRITES + i, PLAYER_BASE[0] + 1);
    } else
      putSpriteAt(PLAYER_LIVES_SPRITES + i, -16, -16);
  }
}

die() {
  gameState = GAME_STATE_SHOW_DEATH;
  playerDyingCntr = 0;
}

doPlayer() {
  // Do movement
  var moved = false;
  var newPlayerSpeedCntr = playerSpeedCntr + playerSpeed;
  var moves = (newPlayerSpeedCntr >> 4)  - (playerSpeedCntr >> 4);
  playerSpeedCntr = newPlayerSpeedCntr;
  for (var i = 0; i < moves; i++) {
      // Eat dot?
      var dotEaten = false;
      if (playerX % 8 == 0 && playerY % 8 == 0) {
          var ch = getCharAtPixel(playerX, playerY);
          if (ch == DOT) {
              putAt(EMPTY, playerX / 8, playerY / 8);
              eatenDotsCount++;
              dotEaten = true;
              incrScore(DOT_VALUE);
          } else if (ch == POWER_DOT) {
              putAt(EMPTY, playerX / 8, playerY / 8);
              eatenDotsCount++;
              dotEaten = true;
              eatEnemyBonus = 200;
              for (var i = 0; i < length(enemies); i++)
                  enemies[i].scare();
              scaredTimer = 175;
              incrScore(POWER_DOT_VALUE);
          }
          if (eatenDotsCount == TOTAL_DOTS)
              newBoard();
      }
      
      // Check for direction change
      var btns = peek(BUTTONS);
      if ((btns & BTN_UP) != 0 && playerX % 8 == 0 &&
          getCharAtPixel(playerX, playerY - 2) <= EMPTY)
        playerDir = PLAYER_UP;
      if ((btns & BTN_DOWN) != 0 && playerX % 8 == 0 &&
          getCharAtPixel(playerX, playerY + 10) <= EMPTY)
        playerDir = PLAYER_DOWN;
      if ((btns & BTN_LEFT) != 0 && playerY % 8 == 0 &&
          getCharAtPixel(playerX - 2, playerY) <= EMPTY)
        playerDir = PLAYER_LEFT;
      if ((btns & BTN_RIGHT) != 0 && playerY % 8 == 0 &&
          getCharAtPixel(playerX + 10, playerY) <= EMPTY)
        playerDir = PLAYER_RIGHT;
      
      // Update position
      if (!dotEaten)
        switch (playerDir) {
          case PLAYER_LEFT:
            var ch = getCharAtPixel(playerX - 1, playerY);
            if (ch <= EMPTY) {
              playerX -= 1;
              moved = true;
            } else if (ch == WARP) {
              playerX = ENTER_RIGHT_X;
              moved = true;
            }
            break;
              
          case PLAYER_RIGHT:
            var ch = getCharAtPixel(playerX + 8, playerY);
            if (ch <= EMPTY) {
              playerX += 1;
              moved = true;
            } else if (ch == WARP) {
              playerX = ENTER_LEFT_X;
              moved = true;
            }
            break;
              
          case PLAYER_UP:
            if (getCharAtPixel(playerX,playerY - 1) <= EMPTY) {
              playerY -= 1;
              moved = true;
            }
            break;
              
          case PLAYER_DOWN:
            if (getCharAtPixel(playerX,playerY + 8) <= EMPTY) {
              playerY += 1;
              moved = true;
            }
            break;
        }
  }

  // Check for collisions
  for (var i = 0; i < length(enemies); i++)
    if (enemies[i].checkCollision())
      return;

  // Update sprite
  putSpriteAt(PLAYER_SPRITE, playerX, playerY);
  if (moved) {
    playerCycle += playerCycleDir;
    if (playerCycle == 0 || playerCycle == 3)
        playerCycleDir = -playerCycleDir;
      setSmallSpriteImage(PLAYER_SPRITE,
                          PLAYER_BASE[playerDir] + playerCycle);
  } else
    setSmallSpriteImage(PLAYER_SPRITE, PLAYER_BASE[playerDir] + 1);
}

resetBoard() {
  for (var i = 0; i < 24; i++)
    arrayPoke(64 * i, tilesData.rows[i], 0, 32);
  eatenDotsCount = 0;
}

eraseMsg(msg, x, y) {
  for (var i = 0; i < length(msg); i++)
    poke(y * 64 + x + i, 2);
}

displayReady() {
  showAt("READY!", 13, 14);
}

eraseReady() {
  eraseMsg("READY!", 13, 14);
}

displayGameOver() {
  var pos = 14 * 64 + 11;
  for (var i = 0; i < 9; i++)
    poke(pos + i, 16 + i);
}

resetPositions() {
  playerX = PLAYER_INIT_X;
  playerY = PLAYER_INIT_Y;
  playerDir = PLAYER_LEFT;
  playerCycle = 0;
  playerCycleDir = 1;
  for (var i = 0; i < length(enemies); i++)
    enemies[i].reset();
  aiWavePhase = AI_WANDER_PHASE;
  aiWaveCounter = 125;
  aiWaveTime = 125;
  scaredTimer = 0;
  for (var i = 0; i < length(enemies); i++)
    enemies[i].doIt();
  putSpriteAt(PLAYER_SPRITE, -16, -16);
}

newBoard() {
  resetBoard();
  resetPositions();
  writeHiScore();
  gameState = GAME_STATE_DISPLAY_BOARD;
  generalTimer = 25;
}

newGame() {
  score = 0;
  displayScore();
  newBoard();
  lives = 3;
}

doPlay() {
  for (var i = 0; i < length(enemies); i++)
    enemies[i].doIt();
  doPlayer();
  if (scaredTimer > 0) {
    if (--scaredTimer == 0) {
      for (var i = 0; i < length(enemies); i++)
        enemies[i].unscare();
    }
  }
  if (--aiWaveCounter == 0) {
    if (aiWavePhase == AI_WANDER_PHASE) {
      aiWavePhase = AI_ATTACK_PHASE;
      aiWaveCounter = aiWaveTime * 3;
    } else {
      aiWavePhase = AI_WANDER_PHASE;
      aiWaveCounter = aiWaveTime;
    }
  }
}

doShowDeath() {
  if (++playerDyingCntr == 12) {
    playerDyingCntr = 0;
    gameState = GAME_STATE_DYING;
    for (var i = 0; i < length(enemies); i++)
      enemies[i].hide();
  }
}

doDying() {
  if (playerDyingCntr == 35) {
    if (--lives > 0) {
      resetPositions();
      gameState = GAME_STATE_DISPLAY_BOARD;
      generalTimer = 25;
    } else {
      displayGameOver();
      generalTimer = 0;
      gameState = GAME_STATE_GAME_OVER;
    }
  } else {
    if (playerDyingCntr == 18)
      putSpriteAt(PLAYER_SPRITE, -16, -16);
    setSmallSpriteImage(PLAYER_SPRITE,
                        PLAYER_DYING_BASE + playerDyingCntr++ / 2);
  }
}

doEatEnemyBonus() {
  if (--eatEnemyBonusTimer == 0) {
    putSpriteAt(EAT_ENEMY_BONUS_SPRITES, -16, -16);
    putSpriteAt(EAT_ENEMY_BONUS_SPRITES + 1, -16, -16);
    gameState = GAME_STATE_PLAY;
  } else
    putSpriteAt(PLAYER_SPRITE, -16, -16);
}

doDisplayBoard() {
  displayLives(lives);
  putSpriteAt(PLAYER_SPRITE, -16, -16);
  if (--generalTimer == 0) {
    gameState = GAME_STATE_READY;
    displayReady();
    displayLives(lives - 1);
    putSpriteAt(PLAYER_SPRITE, playerX, playerY);
    setSmallSpriteImage(PLAYER_SPRITE, PLAYER_BASE[0]);
    generalTimer = 40;
  }
}

doReady() {
  if (--generalTimer == 0) {
    eraseReady();
    gameState = GAME_STATE_PLAY;
  }
}

doGameOver() {
  if (++generalTimer == 50)
    showAt("Press 'SPACE' to start new game ", 0, 0);
  if ((peek(BUTTONS) & BTN_SPACE) != 0) 
    newGame();
}

var waitingForPauseRelease = false;

checkPause() {
  if (waitingForPauseRelease && (peek(BUTTONS) & BTN_PAUSE) != 0)
    return;
  waitingForPauseRelease = false;
  if ((peek(BUTTONS) & BTN_PAUSE) != 0) {
    if (gameState == GAME_STATE_PAUSE)
      gameState = prevState;
    else {
      prevState = gameState;
      gameState = GAME_STATE_PAUSE;
    }
    waitingForPauseRelease = true;
  }      
}       

newGame();

vbi() {
  checkPause();
  if (gameState == GAME_STATE_PLAY)
    doPlay();
  else if (gameState == GAME_STATE_SHOW_DEATH)
    doShowDeath();
  else if (gameState == GAME_STATE_DYING)
    doDying();
  else if (gameState == GAME_STATE_EAT_ENEMY_BONUS)
    doEatEnemyBonus();
  else if (gameState == GAME_STATE_DISPLAY_BOARD)
    doDisplayBoard();
  else if (gameState == GAME_STATE_READY)
    doReady();
  else if (gameState == GAME_STATE_GAME_OVER)
    doGameOver();
  else if (gameState == GAME_STATE_PAUSE)
    ;
}
