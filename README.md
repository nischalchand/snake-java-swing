@'
# Tiny Snake

A small Snake game built with Java Swing. It starts slow and speeds up with every food you eat, while the snake grows longer.

## Features

- Arrow keys or WASD controls
- Speed increases with each food, up to a limit
- Live score, best score, and speed level
- Blocks instant 180-degree turns so you can't die by double-tapping

## Requirements

JDK 14 or newer.

## Run it

    cd src
    javac SnakeGame.java
    java SnakeGame

## Tweaking

The constants at the top of `SnakeGame.java` control the game:

- `START_DELAY`: starting speed (higher is slower)
- `MIN_DELAY`: fastest speed allowed
- `SPEEDUP`: how much faster each food makes it
- `GROWTH`: segments gained per food
'@ | Out-File -Encoding utf8 README.md
