/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package chess;

import chess.Board.*;
import chess.Side.*;
import chess.Square.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 *  A game of chess consists of a board, two sides and a list of moves.
 *
 *  The Game class represents a game of chess.
 *  It contains (references to) a board, two sides (white and black), a move-list
 *  and some info about the status of the game.
 *  @author Adeel
 */
public class Game
{
    public static Scanner in = new Scanner(System.in);
    Board board;
    Side White;
    Side Black;
    MoveList moveList;
    GameStatus gameStatus;

    public Game()
    {
        initializeGame();
        board.display();
    //    play();
    }
    private void initializeGame()
    {
        board = new Board(this);
        White = new Side(true, this);
        Black = new Side(false, this);
        White.opponent = Black;
        Black.opponent = White;
        moveList = new MoveList();
        gameStatus = new GameStatus();
        gameStatus.readyForMove = true;
    }
    
    public void processMove(Move m)
    {
        if (gameActive())
        {
            if(gameStatus.readyForMove)
            {
                if (gameStatus.sideToMove.isLegal(m))
                {
                    gameStatus.readyForMove = false;
                    makeMove(m);
                    updateGameStatus();
                }
            }
        }
        board.discardMove();
    }
    
//    private void play()
//    {
//        do
//        {
//            makeMove(getNextMove());
//            updateGameStatus();
//        }
//        while(gameActive());
//    }
    
//    private Move getNextMove()
//    {
//        String s;
//        Move move;
//        do
//        {
//            System.out.println("next move:");
//            s = in.next();
//            move = new Move(board.get(s.charAt(1) - '1', s.charAt(0) - 'a'), board.get(s.charAt(3) - '1', s.charAt(2)- 'a'));
////            board.sq1 = null;
////            board.sq2 = null;
////            move = board.getMove();
//        }
//        while(!gameStatus.sideToMove.isLegal(move));
//        
//        return move;
//    }

    private void makeMove(Move move)
    {
        System.out.println("moving...");
        gameStatus.sideToMove.makeMove(move);
        moveList.addMove(move);
    }

    private boolean gameActive()
    {
        return gameStatus.gameActive;
    }

    private void updateGameStatus()
    {
        gameStatus.update();
    }

    private class GameStatus
    {
        boolean gameActive;
        boolean readyForMove;
        Side sideToMove;
        boolean enPassantActive;
        int enPassantFile;
        List<Piece> capturedPieces;

        public GameStatus()
        {
            sideToMove = White;
            gameActive = true;
            enPassantActive = false;
        }
        
        private void update()
        {
            sideToMove = sideToMove.opponent;
            removeCapturedPieces(capturedPieces);
            updateCheckStatus();
            
            if (!gameActive)
            {
                System.out.println("Checkmate! " + ((sideToMove.opponent.isWhite) ? "White wins!":"Black wins!"));
                return;
            }
            if (sideToMove.inCheck)
                System.out.println("Check!");
            
            updateEnPassantStatus();
            capturedPieces = new ArrayList();
            readyForMove = true;
        }

        private void updateCheckStatus()
        {
            if (sideToMove.king.inCheck())
                gameActive = !gameStatus.sideToMove.king.isMated();
        }

        private boolean getEnPassantStatus()
        {
            Move lastMove = moveList.lastMove();
            return lastMove.targetSquare.piece.getClass().equals(Pawn.class)
               && (Math.abs(lastMove.targetSquare.rank - lastMove.sourceSquare.rank) == 2);
        }

        private void updateEnPassantStatus()
        {
            enPassantActive = getEnPassantStatus();
            enPassantFile = ( (enPassantActive) ? moveList.lastMove().targetSquare.file : -1 );
        }

        private void removeCapturedPieces(List<Piece> pieceList)
        {
            if (pieceList != null)
            {
                for (Piece p : pieceList)
                {                
                    p.square = null;
                    getPieceList(p).remove(p);
                }
                capturedPieces.clear();
            }
        }

        private List<? extends Piece> getPieceList(Piece p)
        {
            if (p.getClass().equals(Pawn.class))
                return p.getSide().pawns;
            if (p.getClass().equals(Bishop.class))
                return p.getSide().bishops;
            if (p.getClass().equals(Knight.class))
                return p.getSide().knights;
            if (p.getClass().equals(Rook.class))
                return p.getSide().rooks;
            else
                return p.getSide().queens;
        }

    }

    /**
     *  A class representing the list of moves in a game.
     */
    private class MoveList
    {
        // list of moves
        private List<Move> moveList;

        // constructor
        public MoveList()
        {
            moveList = new ArrayList();
        }

        /**
         * Appends the move to the move-list
         * @param move
         */
        public void addMove(Move move)
        {
            moveList.add(move);
        }

        /**
         * Returns the last move in the move-list
         * @return
         */
        public Move lastMove()
        {
            return moveList.get(moveList.size() - 1);
        }
    }

    /**
     * Tells whether the move is a valid En Passant or not.
     * See: https://en.wikipedia.org/wiki/En_passant#The_rule
     *
     * @param move
     * @return true if the move is a valid En Passant. False otherwise.
     */
    public boolean isLegalEnPassant(Move move)
    {
        return (gameStatus.enPassantActive
            &&  move.sourceSquare.rank == ((gameStatus.sideToMove == White) ? 4:3)
            &&  gameStatus.enPassantFile == move.targetSquare.file);
    }

    public void markAsCaptured(Piece p)
    {
        gameStatus.capturedPieces.add(p);
    }
}
