package com.feoblue.videopoker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PokerEngine {
    // States of game playing
    public static final int STATE_START = 0;
    public static final int STATE_CHANGING = 1;
    public static final int STATE_END = 2;

    private static int balance = 50;
    private static int state = STATE_START;
    private static int multiplier = 0; // current bet decreased by 1
    private static int secretBalanceClicks = 0; // clicks to reset balance

    private static final List<Card> DECK = new ArrayList<>(); // deck with all cards
    private static final List<Card> HAND = new ArrayList<>(); // first 5 cards ("hand")
    private static final boolean[] CARDS_TO_HOLD = new boolean[5]; // non-changeable cards

    static {
        // Initializing of all cards in deck
        for (int i = 1; i < 14; i++) {
            DECK.add(new Card(i, 's'));
            DECK.add(new Card(i, 'c'));
            DECK.add(new Card(i, 'd'));
            DECK.add(new Card(i, 'h'));
        }
        // Initializing of hand
        for (int i = 0; i < 5; i++) {
            HAND.add(DECK.get(i));
            setCardToHold(i, true);
        }
    }

    public static void setState(int newState) {
        state = newState;
    }

    public static int getState() {
        return state;
    }

    public static int getMultiplier() {
        return multiplier;
    }

    public static void incMultiplier() {
        if (multiplier < 4)
            multiplier++;
    }

    public static void decMultiplier() {
        if (multiplier > 0)
            multiplier--;
    }

    public static void setBalance(int newBalance) {
        balance = newBalance;
    }

    public static void incBalance(int increment) {
        balance += increment;
    }

    public static int getBalance() {
        return balance;
    }

    public static void setSecretBalanceClicks(int newSecretBalanceClicks) {
        secretBalanceClicks = newSecretBalanceClicks;
    }

    public static int getSecretBalanceClicks() {
        return secretBalanceClicks;
    }

    public static void incSecretBalanceClicks() {
        secretBalanceClicks++;
    }

    public static boolean getCardToHold(int number) {
        return CARDS_TO_HOLD[number];
    }

    public static void setCardToHold(int number, boolean hold) {
        CARDS_TO_HOLD[number] = hold;
    }

    public static void setAllCardsToHold(boolean hold) {
        for (int i = 0; i < 5; i++)
            PokerEngine.setCardToHold(i, hold);
    }

    public static String getCardFromHand(int number) {
        return HAND.get(number).getCard();
    }

    public static void dealCards() {
        // Shuffling cards and taking first five of them in hand
        Collections.shuffle(DECK);
        for (int i = 0; i < 5; i++)
            HAND.set(i, DECK.get(i));
    }

    public static void changeCards() {
        // Changing chosen cards in hand by next cards in deck
        int nextCardNumber = 5;
        for (int i = 0; i < 5; i++) {
            if (!getCardToHold(i)) {
                HAND.set(i, DECK.get(nextCardNumber));
                nextCardNumber++;
            }
        }
    }

    // Determination of poker combination in hand

    private static boolean isRoyal() {
        return HAND.get(3).getValue() == 13 & HAND.get(0).getValue() == 1;
    }

    private static boolean isStraight() {
        boolean status = true;
        for (int i = 1; i < 4; i++) {
            if (HAND.get(i + 1).getValue() - HAND.get(i).getValue() != 1) {
                status = false;
                break;
            }
        }
        if (status) {
            if (HAND.get(4).getValue() - HAND.get(0).getValue() != 12
                    && HAND.get(1).getValue() - HAND.get(0).getValue() != 1) {
                status = false;
            }
        }
        return status;
    }

    private static boolean isFlush() {
        boolean status = true;
        char kind = HAND.get(0).getKind();
        for (int i = 1; i < 5; i++) {
            if (HAND.get(i).getKind() != kind) {
                status = false;
                break;
            }
        }
        return status;
    }

    private static boolean isFourOfAKind() {
        return HAND.get(1).getValue() == HAND.get(2).getValue()
                && HAND.get(3).getValue() == HAND.get(2).getValue()
                && (HAND.get(0).getValue() == HAND.get(2).getValue()
                | HAND.get(4).getValue() == HAND.get(2).getValue());
    }

    private static boolean isFullHouse() {
        return (HAND.get(0).getValue() == HAND.get(2).getValue()
                & HAND.get(1).getValue() == HAND.get(2).getValue()
                & HAND.get(3).getValue() == HAND.get(4).getValue())
                || (HAND.get(3).getValue() == HAND.get(2).getValue()
                & HAND.get(4).getValue() == HAND.get(2).getValue()
                & HAND.get(0).getValue() == HAND.get(1).getValue());
    }

    private static boolean isThreeOfAKind() {
        return (HAND.get(0).getValue() == HAND.get(2).getValue()
                & HAND.get(1).getValue() == HAND.get(2).getValue())
                || (HAND.get(1).getValue() == HAND.get(2).getValue()
                & HAND.get(3).getValue() == HAND.get(2).getValue())
                || (HAND.get(3).getValue() == HAND.get(2).getValue()
                & HAND.get(4).getValue() == HAND.get(2).getValue());
    }

    private static boolean isTwoPairs() {
        return (HAND.get(0).getValue() ==  HAND.get(1).getValue()
                | HAND.get(2).getValue() ==  HAND.get(1).getValue())
                && (HAND.get(2).getValue() == HAND.get(3).getValue()
                | HAND.get(4).getValue() == HAND.get(3).getValue());
    }

    private static boolean isPairJacks() {
        boolean status = false;
        for (int i = 1; i < 5; i++)
            if (((HAND.get(i).getValue() == 1) || (HAND.get(i).getValue() > 10))
                    && HAND.get(i).getValue() == HAND.get(i-1).getValue()) {
                status = true;
                break;
            }
        return status;
    }

    public static int getHandComboCode() {
        // Returning number of poker combination in hand in accordance with winning points
        Collections.sort(HAND,Card.COMPARE_BY_VALUE);
        if (isFlush() && isStraight()) {
            return (isRoyal() ? 250 : 50);
        }
        else if (isFourOfAKind()) return 25;
        else if (isFullHouse()) return 9;
        else if (isFlush()) return 6;
        else if (isStraight()) return 4;
        else if (isThreeOfAKind()) return 3;
        else if (isTwoPairs()) return 2;
        else if (isPairJacks()) return 1;
        else return 0;
    }
}
