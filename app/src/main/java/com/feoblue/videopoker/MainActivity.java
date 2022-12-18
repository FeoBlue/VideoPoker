package com.feoblue.videopoker;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    final int LOCALE_ENG = 0;
    final int LOCALE_UKR = 1;

    String  strBalanceCount, // text for BalanceView
            strChange, strHold, strStartGame, strGetCards, strDealAgain, // text values for Buttons
            strMessageComb0, strMessageComb1, strMessageComb2, strMessageComb3,
            strMessageComb4, strMessageComb6, strMessageComb9, strMessageComb25,
            strMessageComb50, strMessageComb250, strChooseCards,
            strPressStart; // text values (messages) for TextView

    TextView[] combText = new TextView[9]; // names for table of poker combinations
    View[][] tabPts = new LinearLayout[5][9]; // color-able cells of table of poker combinations

    TextView textView, balanceView; // information for player
    Button buttonDeal, betPlus, betMinus, buttonChangeAll;
    Switch languageSelector;

    List<ImageView> cards = new ArrayList<>(); // 5 card images on display
    List<Button> buttonHolds = new ArrayList<>(); // 5 buttons "HOLD/CHANGE" under cards

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initActivityObjects();
        loadLocale(LOCALE_ENG);
        drawPointsColumn();
        hideCards();
        setEnabledHoldButtons(false);
        readBalanceFromFile();
        refreshBalanceView();

        betPlus.setOnClickListener(new View.OnClickListener() {
            // increasing bet before dealing cards
            @Override
            public void onClick(View view) {
                PokerEngine.incMultiplier();
                drawPointsColumn();
            }
        });

        betMinus.setOnClickListener(new View.OnClickListener() {
            // decreasing bet before dealing cards
            @Override
            public void onClick(View view) {
                PokerEngine.decMultiplier();
                drawPointsColumn();
            }
        });

        languageSelector.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            // changing language of interface and increasing clicks for reset balance
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                loadLocale(b ? LOCALE_UKR : LOCALE_ENG);
                incSecretClicks();
            }
        });

        buttonDeal.setOnClickListener(new View.OnClickListener() {
            // dealing and changing cards
            @Override
            public void onClick(View view) {
                if (PokerEngine.getState() == PokerEngine.STATE_CHANGING)
                    changeCards();
                else
                    dealCards();
                setTextForDealButton();
                setTextForTextView();
                refreshBalanceView();
                writeBalanceToFile();
            }
        });

        buttonChangeAll.setOnClickListener(new View.OnClickListener() {
            // setting all cards to change
            @Override
            public void onClick(View view) {
                PokerEngine.setAllCardsToHold(false);
                hideCards();
                setTextForAllHoldButtons();
            }
        });

        for (int i = 0; i < 5; i++) {
            // making buttons able to hold/change cards after dealing
            int cardNumber = i;
            buttonHolds.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (PokerEngine.getState() == PokerEngine.STATE_CHANGING) {
                        PokerEngine.setCardToHold(cardNumber,
                                !PokerEngine.getCardToHold(cardNumber));
                        setTextForHoldButton(cardNumber);
                        loadCardImage(cardNumber);
                    }
                }
            });
        }
    }

    protected void initActivityObjects() {
        // connecting activity objects with Java variables (initializing)
        initTextView();
        initBalanceView();
        initLanguageSelector();
        initPlayButtons();
        initBetButtons();
        initHoldButtons();
        initCardPics();
        initTableCombos();
        initTableNumFields();
    }

    // Initializing of Buttons

    protected void initBetButtons() {
        betPlus = findViewById(R.id.betPlus);
        betMinus = findViewById(R.id.betMinus);
    }

    protected void initHoldButtons() {
        String resName;
        int resID;
        for (int i = 0; i < 5; i++) {
            resName = "hold" + i;
            resID = getResources().getIdentifier(resName, "id", getPackageName());
            buttonHolds.add(findViewById(resID));
        }
    }

    protected void initPlayButtons() {
        buttonDeal = findViewById(R.id.buttonDeal);
        buttonChangeAll = findViewById(R.id.buttonChangeAll);
    }

    protected void setEnabledBetButtons(boolean enabled) {
        betMinus.setEnabled(enabled);
        betPlus.setEnabled(enabled);
    }

    protected void setEnabledHoldButtons(boolean enabled) {
        for (int i = 0; i < 5; i++)
            buttonHolds.get(i).setEnabled(enabled);
        buttonChangeAll.setEnabled(enabled);
    }

    // Initializing of Card ImageViews

    protected void initCardPics() {
        String resName;
        int resID;
        for (int i = 0; i < 5; i++) {
            resName = "cardPic" + i;
            resID = getResources().getIdentifier(resName, "id", getPackageName());
            cards.add(findViewById(resID));
        }
    }

    // Initializing of Combinations' Info Table

    protected void initTableCombos() {
        String resName;
        int resID;
        for (int i=0; i<9; i++) {
            resName = "comb" + i;
            resID = getResources().getIdentifier(resName, "id", getPackageName());
            combText[i] = findViewById(resID);
        }
    }

    protected void initTableNumFields() {
        String resName;
        int resID;
        for (int i = 0; i < 5; i++)
            for (int j = 0; j < 9; j++) {
                resName = "tabWin" + j + "Mult" + i;
                resID = getResources().getIdentifier(resName, "id", getPackageName());
                tabPts[i][j] = findViewById(resID);
            }
    }

    // Initializing of TextViews

    protected void initTextView() {
        textView = findViewById(R.id.textview1);
    }

    protected void initBalanceView() {
        balanceView = findViewById(R.id.balanceString);
    }

    // Initializing of Language Selector

    protected void initLanguageSelector() {
        languageSelector = findViewById(R.id.switch1);
    }

    // Loading resources (text and images) for activity objects

    protected void loadApplicationTitle() {
        setTitle(R.string.app_name);
    }

    protected void loadTextForTableOfCombos() {
        String resName;
        int resID;
        for (int i = 0; i < 9; i++) {
            resName = "comb" + i;
            resID = getResources().getIdentifier(resName, "string", getPackageName());
            combText[i].setText(resID);
        }
    }

    protected void loadTextForBetButtons() {
        betPlus.setText(R.string.betPlus);
        betMinus.setText(R.string.betMinus);
    }

    protected void loadTextForChangeAllButton() {
        buttonChangeAll.setText(R.string.changeAll);
    }

    protected void loadTextForHoldButtons() {
        strChange = getResources().getString(R.string.change);
        strHold = getResources().getString(R.string.hold);
        setTextForAllHoldButtons();
    }

    protected void loadTextForDealButton() {
        strStartGame = getResources().getString(R.string.startGame);
        strGetCards = getResources().getString(R.string.getCards);
        strDealAgain = getResources().getString(R.string.dealAgain);
        setTextForDealButton();
    }

    @SuppressLint("SetTextI18n")
    protected void loadTextForBalanceView() {
        strBalanceCount = getResources().getString(R.string.balanceCount);
        balanceView.setText(strBalanceCount + PokerEngine.getBalance());
    }

    protected void loadTextViewMessages() {
        strPressStart = getResources().getString(R.string.pressStart);
        strChooseCards = getResources().getString(R.string.chooseCards);
        strMessageComb0 = getResources().getString(R.string.messageComb0);
        strMessageComb1 = getResources().getString(R.string.messageComb1);
        strMessageComb2 = getResources().getString(R.string.messageComb2);
        strMessageComb3 = getResources().getString(R.string.messageComb3);
        strMessageComb4 = getResources().getString(R.string.messageComb4);
        strMessageComb6 = getResources().getString(R.string.messageComb6);
        strMessageComb9 = getResources().getString(R.string.messageComb9);
        strMessageComb25 = getResources().getString(R.string.messageComb25);
        strMessageComb50 = getResources().getString(R.string.messageComb50);
        strMessageComb250 = getResources().getString(R.string.messageComb250);
        setTextForTextView();
    }

    protected void loadCardImage(int number) {
        String resName = (PokerEngine.getCardToHold(number)
                ? "card_" + PokerEngine.getCardFromHand(number) : "bk");
        int resID;
        resID = getResources().getIdentifier(resName, "drawable", getPackageName());
        cards.get(number).setImageResource(resID);
    }

    protected void setTextForHoldButton(int number) {
        buttonHolds.get(number).setText(PokerEngine.getCardToHold(number) ? strChange : strHold);
    }

    protected void setTextForDealButton() {
        switch (PokerEngine.getState()) {
            case (PokerEngine.STATE_START):
                buttonDeal.setText(strStartGame);
                break;
            case (PokerEngine.STATE_CHANGING):
                buttonDeal.setText(strGetCards);
                break;
            case (PokerEngine.STATE_END):
                buttonDeal.setText(strDealAgain);
                break;
            default:
                break;
        }
    }

    protected void setTextForTextView() {
        switch (PokerEngine.getState()) {
            case (PokerEngine.STATE_START):
                textView.setText(strPressStart);
                break;
            case (PokerEngine.STATE_CHANGING):
                textView.setText(strChooseCards);
                break;
            case (PokerEngine.STATE_END):
                textView.setText(comboCodeToString(PokerEngine.getHandComboCode()));
                break;
            default:
                break;
        }
    }

    protected void setTextForAllHoldButtons() {
        for (int i = 0; i < 5; i++) {
            setTextForHoldButton(i);
        }
    }

    // Actions with gaming balance (virtual money)

    @SuppressLint("SetTextI18n")
    protected void refreshBalanceView() {
        balanceView.setText(strBalanceCount + PokerEngine.getBalance());
    }

    protected void readBalanceFromFile() {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = openFileInput("balance.dat");
            byte[] bytes = new byte[fileInputStream.available()];
            fileInputStream.read(bytes);
            String text = new String (bytes);
            PokerEngine.setBalance(Integer.parseInt(text));
        }
        catch (IOException ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
        finally {
            try {
                if (fileInputStream != null)
                    fileInputStream.close();
            }
            catch (IOException ex) {
                Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    protected void writeBalanceToFile() {
        FileOutputStream fileOutputStream = null;
        try {
            String text = Integer.toString(PokerEngine.getBalance());
            fileOutputStream = openFileOutput("balance.dat", MODE_PRIVATE);
            fileOutputStream.write(text.getBytes());
        }
        catch (IOException ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
        finally {
            try {
                if (fileOutputStream != null)
                    fileOutputStream.close();
            }
            catch (IOException ex) {
                Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    protected void incSecretClicks() {
        // Increasing of clicks for balance changing
        PokerEngine.incSecretBalanceClicks();
        if (PokerEngine.getSecretBalanceClicks() >= 17
                && PokerEngine.getSecretBalanceClicks() <= 19) {
            Toast.makeText(getApplicationContext(),
                    "Reset the balance after: "
                            + (20 - PokerEngine.getSecretBalanceClicks()),
                    Toast.LENGTH_SHORT).show();
        }
        else if (PokerEngine.getSecretBalanceClicks() == 20) {
            PokerEngine.setSecretBalanceClicks(0);
            PokerEngine.setBalance(50);
            refreshBalanceView();
            writeBalanceToFile();
            Toast.makeText(getApplicationContext(), "Balance reset!", Toast.LENGTH_LONG).show();
        }
    }

    // Showing and hiding cards on display

    protected void showCards() {
        String resName;
        int resID;
        for (int i = 0; i < 5; i++) {
            resName = "card_" + PokerEngine.getCardFromHand(i);
            resID = getResources().getIdentifier(resName, "drawable", getPackageName());
            cards.get(i).setImageResource(resID);
        }
    }

    protected void hideCards() {
        int resID;
        for (int i = 0; i < 5; i++) {
            resID = getResources().getIdentifier("bk", "drawable", getPackageName());
            cards.get(i).setImageResource(resID);
        }
    }

    // Operations with cards

    protected void dealCards() {
        PokerEngine.setState(PokerEngine.STATE_CHANGING);
        PokerEngine.dealCards();
        showCards();
        PokerEngine.incBalance(-1 * (PokerEngine.getMultiplier() + 1));
        setEnabledBetButtons(false);
        setEnabledHoldButtons(true);
    }

    protected void changeCards() {
        PokerEngine.setState(PokerEngine.STATE_END);
        PokerEngine.changeCards();
        showCards();
        PokerEngine.incBalance(PokerEngine.getHandComboCode() * (PokerEngine.getMultiplier() + 1));
        PokerEngine.setAllCardsToHold(true);
        setTextForAllHoldButtons();
        setEnabledBetButtons(true);
        setEnabledHoldButtons(false);
    }

    // Other operations

    protected void drawPointsColumn() {
        // Coloring of column of table of poker combinations in accordance with current bet
        for (int i = 0; i < 5; i++)
            for (int j = 0; j < 9; j++)
                tabPts[i][j].setBackgroundColor(i == PokerEngine.getMultiplier()
                        ? 0x195700F9 : 0x00000000);
    }

    public String comboCodeToString(int i) {
        // Conversion code of card combination to string message for TextView
        switch (i) {
            case (1):
                return strMessageComb1;
            case (2):
                return strMessageComb2;
            case (3):
                return strMessageComb3;
            case (4):
                return strMessageComb4;
            case (6):
                return strMessageComb6;
            case (9):
                return strMessageComb9;
            case (25):
                return strMessageComb25;
            case (50):
                return strMessageComb50;
            case (250):
                return strMessageComb250;
            default:
                return strMessageComb0;
        }
    }

    protected void loadLocale(int localeCode) {
        // Setting chosen locale
        Locale locale;
        if (localeCode == LOCALE_UKR) {
            locale = new Locale("uk");
            languageSelector.setText("\uD83C\uDDFA\uD83C\uDDE6");
        }
        else {
            locale = new Locale("en");
            languageSelector.setText("\uD83C\uDDEC\uD83C\uDDE7");
        }
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getBaseContext().getResources().updateConfiguration(configuration, null);
        // Load locale strings
        loadApplicationTitle();
        loadTextForTableOfCombos();
        loadTextForBetButtons();
        loadTextForChangeAllButton();
        loadTextForHoldButtons();
        loadTextForDealButton();
        loadTextViewMessages();
        loadTextForBalanceView();
    }
}