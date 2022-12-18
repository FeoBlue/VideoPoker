package com.feoblue.videopoker;

import java.util.Comparator;

public class Card {
    private int Value;
    private char Kind;

    public Card (int Value, char Kind) {
        this.Value = Value;
        this.Kind = Kind;
    }

    public int getValue() {
        return Value;
    }

    public char getValueChar() {
        switch (this.getValue()) {
            case (1):
                return 'a';
            case (10):
                return 't';
            case (11):
                return 'j';
            case (12):
                return 'q';
            case (13):
                return 'k';
            default:
                return Character.forDigit(this.getValue(), 10);
        }
    }

    public char getKind() {
        return Kind;
    }

    public String getCard() {
        return "" + this.getValueChar() + this.getKind();
    }

    public static final Comparator<Card> COMPARE_BY_VALUE = new Comparator<Card>() {
        @Override
        public int compare(Card c1, Card c2) {
            return c1.getValue() - c2.getValue();
        }
    };
}


