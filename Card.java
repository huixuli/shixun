package com.example.lihuixu.hello;


import org.opencv.core.Point;

public class Card {
    public int id;
    public int count;
    public int priority;
    public org.opencv.core.Point loc;
    public Card(int id, org.opencv.core.Point loc) {
        this.id = id;
        this.count = 0;
        double po[] = {0.0, 0.0};
        this.loc = loc;
        this.priority = 0;
    }
    public Card(Card card) {
        this.id = card.id;
        this.count = card.count;
        this.loc = card.loc;
        this.priority = card.priority;
    }
}
