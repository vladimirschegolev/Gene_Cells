package main;

import java.awt.*;

abstract class Cell {

    int x, y;
    Color color;

    abstract boolean  isAlive();

    abstract boolean act();


}
