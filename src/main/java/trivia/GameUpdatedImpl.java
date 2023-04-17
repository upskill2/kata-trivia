package trivia;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Properties;

import static trivia.Categories.*;

// REFACTOR ME
public class GameUpdatedImpl implements IGame {
    private static final int PLACES = 6;
    private static final int PURSES = 6;
    private static final int PENALTY_BOX = 6;
    private final int TOTAL_QUESTIONS = 50;
    private final int[] boardPlace;
    private final int[] coins;
    private final boolean[] inPenaltyBox;
    private final ArrayList<String> players;
    private final LinkedList<String> popQuestions;
    private final LinkedList<String> scienceQuestions;
    private final LinkedList<String> sportsQuestions;
    private final LinkedList<String> rockQuestions;
    int currentPlayer;
    boolean isGettingOutOfPenaltyBox;

    public GameUpdatedImpl () {
        popQuestions = new LinkedList<> ();
        scienceQuestions = new LinkedList<> ();
        sportsQuestions = new LinkedList<> ();
        rockQuestions = new LinkedList<> ();
        players = new ArrayList<> ();
        coins = new int[PURSES];
        boardPlace = new int[PLACES];
        inPenaltyBox = new boolean[PENALTY_BOX];


        loadQuestionsFromProperties (POP.value ().toLowerCase (), popQuestions);
        loadQuestionsFromProperties (SCIENCE.value ().toLowerCase (), scienceQuestions);
        loadQuestionsFromProperties (SPORTS.value ().toLowerCase (), sportsQuestions);
        loadQuestionsFromProperties (ROCK.value ().toLowerCase (), rockQuestions);
        loadQuestionsFromProperties (GEOGRAPHY.value ().toLowerCase (), rockQuestions);
    }

    private void loadQuestionsFromProperties (String category, LinkedList<String> questions) {
        Properties props = new Properties ();
        try (InputStream stream = GameUpdatedImpl.class.getClassLoader ()
                .getResourceAsStream (category + ".properties")) {
            props.load (stream);
        } catch (IOException e) {
            throw new RuntimeException ("Unable to read properties file for category: " + category);
        }
        String question = props.getProperty ("app.question");
        for (int i = 0; i < TOTAL_QUESTIONS; i++) {
            questions.addLast (question + " " + i);
        }
    }

    public boolean isPlayable () {
        return (totalPlayers () >= 2);
    }

    public boolean add (String playerName) {
        checkPlayerNameIsUnique(playerName);
        players.add (playerName);
        boardPlace[totalPlayers ()] = 0;
        coins[totalPlayers ()] = 0;
        inPenaltyBox[totalPlayers ()] = false;

        System.out.println (playerName + " was added");
        System.out.println ("They are player number " + players.size ());
        return true;
    }

    public void checkPlayerNameIsUnique(String playerName){
       if ( players.contains (playerName) ) throw new IllegalArgumentException("Player name should be unique: " + playerName);

    }

    private int totalPlayers () {
        return players.size ();
    }

    public void roll (int roll) {
        String playerName = players.get (currentPlayer);
        System.out.println (playerName + " is the current player");
        System.out.println ("They have rolled a " + roll);

        if (inPenaltyBox[currentPlayer]) {
            if (roll % 2 != 0) {
                isGettingOutOfPenaltyBox = true;
                System.out.println (playerName + " is getting out of the penalty box");
            } else {
                System.out.println (playerName + " is not getting out of the penalty box");
                isGettingOutOfPenaltyBox = false;
                return;
            }

        }
        makePlayerMove (roll);
        makePlayerCircleMove ();
        displayPlayerMove (playerName);
        askQuestion ();
    }

    private void makePlayerCircleMove () {
        if (boardPlace[currentPlayer] > 11) boardPlace[currentPlayer] = boardPlace[currentPlayer] - 12;
    }

    private void makePlayerMove (final int roll) {
        boardPlace[currentPlayer] = boardPlace[currentPlayer] + roll;
    }

    private void displayPlayerMove (final String playerName) {
        System.out.println (playerName
                + "'s new location is "
                + boardPlace[currentPlayer]);
        System.out.println ("The category is " + currentCategory ().value ());
    }

    private void askQuestion () {
        Categories category = currentCategory ();
        switch (category) {
            case POP:
                System.out.println (popQuestions.removeFirst ());
                break;
            case SCIENCE:
                System.out.println (scienceQuestions.removeFirst ());
                break;
            case SPORTS:
                System.out.println (sportsQuestions.removeFirst ());
                break;
            default:
                System.out.println (rockQuestions.removeFirst ());
                break;
        }
    }


    private Categories currentCategory () {
        switch (boardPlace[currentPlayer]) {
            case 0:
            case 4:
            case 8:
                return POP;
            case 1:
            case 5:
            case 9:
                return SCIENCE;
            case 2:
            case 6:
            case 10:
                return SPORTS;
            case 3:
            case 7:
            case 11:
                return ROCK;
            default:
                return GEOGRAPHY;
        }
    }

    public boolean wasCorrectlyAnswered () {
        if (inPenaltyBox[currentPlayer] && !isGettingOutOfPenaltyBox) {
            currentPlayer++;
            if (currentPlayer == players.size ()) currentPlayer = 0;
            return true;
        }

        displayCorrectAnswerAndMakeMove ();
        boolean winner = didPlayerWin ();
        currentPlayer++;
        if (currentPlayer == players.size ()) currentPlayer = 0;
        return winner;
    }

    private void displayCorrectAnswerAndMakeMove () {
        System.out.println ("Answer was correct!!!!");
        coins[currentPlayer]++;
        System.out.println (players.get (currentPlayer)
                + " now has "
                + coins[currentPlayer]
                + " Gold Coins.");
    }

    public boolean wrongAnswer () {
        System.out.println ("Question was incorrectly answered");
        System.out.println ("Second chance to avoid the penalty, asking question from the same category");
        askQuestion ();
        System.out.println (players.get (currentPlayer) + " was sent to the penalty box");
        inPenaltyBox[currentPlayer] = true;

        currentPlayer++;
        if (currentPlayer == players.size ()) currentPlayer = 0;
        return true;
    }

    private boolean didPlayerWin () {
        return (coins[currentPlayer] != 6);
    }
}