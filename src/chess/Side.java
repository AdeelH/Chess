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
 *
 * @author Adeel
 */
public class Side
{
    public static Scanner in = new Scanner(System.in);
    private final Game game;
    private final Side self;
    private final Board board;

    public boolean isWhite;
    boolean inCheck;
    public Side opponent;
    public King king;
    public List<Queen> queens;
    public List<Rook> rooks;
    public List<Bishop> bishops;
    public List<Knight> knights;
    public List<Pawn> pawns;

    public Side(boolean isWhite, Game g)
    {
        game = g;
        this.isWhite = isWhite;
        this.board = g.board;
        inCheck = false;
        
        queens = new ArrayList(1);
        rooks = new ArrayList(2);
        bishops = new ArrayList(2);
        knights = new ArrayList(2);
        pawns = new ArrayList(8);
        
        self = this;
        setupPieces();        
    }

    private void setupPieces()
    {
        king = new King(((isWhite) ? 0 : 7), 4);
        queens.add(new Queen(((isWhite) ? 0 : 7), 3));

        rooks.add(new Rook(((isWhite) ? 0 : 7), 0));
        rooks.add(new Rook(((isWhite) ? 0 : 7), 7));

        knights.add(new Knight(((isWhite) ? 0 : 7), 1));
        knights.add(new Knight(((isWhite) ? 0 : 7), 6));

        bishops.add(new Bishop(((isWhite) ? 0 : 7), 2));
        bishops.add(new Bishop(((isWhite) ? 0 : 7), 5));

        setupPawns();
    }

    private void setupPawns()
    {
        for (int file = 0, pawnRow = (isWhite) ? 1 : 6; file < 8; file++)
        {
            pawns.add(new Pawn(pawnRow, file));
        }
    }

    public void makeMove(Move m)
    {
        m.sourceSquare.piece.makeMove(m);
    }

    public boolean isLegal(Move move)
    {
        return move.sourceSquare.piece != null
            && !move.sourceSquare.piece.equals(move.targetSquare.piece)
            && move.sourceSquare.piece.getSide().equals(this)
            && move.sourceSquare.piece.isLegal(move)
            && (move.sourceSquare.piece.equals(king) || board.doesNotExposeKingToCheck(move));
    }

    public abstract class Piece
    {
        public Square square;
        public String icon;

        public Side getSide()
        {
            return self;
        }
        
        public boolean isWhite()
        {
            return self.isWhite;
        }

        abstract boolean isLegal(Board.Move move);

        abstract public void makeMove(Move m);

        public void moveTo(Square targetSquare)
        {
            if (this.square != null)
            {
                this.square.setPiece(null);
            }

            if (targetSquare.piece != null)
            {
                game.markAsCaptured(targetSquare.piece);
            }

            this.square = targetSquare;
            targetSquare.setPiece(this);
        }                
    }

    public class King extends Piece
    {
        private List<Piece> checkingPiecesList;
        private boolean castlingAllowed;
        private boolean castlingInProgress;

        public King(int rank, int file)
        {
            icon = "\u265A";
            moveTo(board.get(rank, file));
            castlingAllowed = true;
            castlingInProgress = false;
        }

        public boolean isMated()
        {
            return inCheck && noEscape() && noIntercepts() && checkingPieceNotCapturable();
        }

        public boolean inCheck()
        {
            checkingPiecesList = board.getCheckingPiecesList(square, opponent);
            inCheck = checkingPiecesList.size() > 0;

            return inCheck;
        }

        @Override
        boolean isLegal(Move move)
        {
            int rankDiff = Math.abs(move.targetSquare.rank - move.sourceSquare.rank);
            int fileDiff = Math.abs(move.targetSquare.file - move.sourceSquare.file);

            return ((rankDiff >= 0 && rankDiff <= 1 && fileDiff >= 0 && fileDiff <= 1)
                    && (move.targetSquare.piece == null || isCapturablePiece(move.targetSquare.piece))
                    && !move.targetSquare.isUnderAttackFrom(opponent, false))
                    || isLegalCasling(move);
        }

        private boolean isLegalCasling(Move move)
        {
            int rankDiff = move.targetSquare.rank - move.sourceSquare.rank;
            int fileDiff = move.targetSquare.file - move.sourceSquare.file;

            if (castlingAllowed && !inCheck)
            {
                if (rankDiff == 0)
                {
                    if (fileDiff == 2)
                    {
                        if (rooks.get(1).notMoved)
                        {
                            castlingInProgress = !(board.get(square.rank, 5).isUnderAttackFrom(opponent, false)
                                                 && board.get(square.rank, 6).isUnderAttackFrom(opponent, false));
                            return castlingInProgress;
                        }
                    }
                    if (fileDiff == -2)
                    {
                        if (rooks.get(0).notMoved)
                        {
                            castlingInProgress = !(board.get(square.rank, 2).isUnderAttackFrom(opponent, false)
                                                 && board.get(square.rank, 3).isUnderAttackFrom(opponent, false));
                            return castlingInProgress;
                        }
                    }
                }
            }
            return false;
        }

        @Override
        public void makeMove(Move m)
        {
            castlingAllowed = false;
            inCheck = false;
            if (castlingInProgress)
            {
                if (m.targetSquare.file == 2)
                {
                    rooks.get(0).makeMove(new Move(rooks.get(0).square, board.get(m.sourceSquare.rank, 3)));
                }
                if (m.targetSquare.file == 6)
                {
                    rooks.get(1).makeMove(new Move(rooks.get(0).square, board.get(m.sourceSquare.rank, 5)));
                }
                castlingInProgress = false;
            }
            moveTo(m.targetSquare);
        }

        private boolean noEscape()
        {
            Square sq;
            for (int rankInc = -1, rank, file; rankInc <= 1; rankInc++)
            {
                rank = square.rank + rankInc;
                for (int fileInc = -1;(rank <= 7 && rank >= 0) && fileInc <= 1; fileInc++)
                {
                    file = square.file + fileInc;

                    if ((file <= 7 && file >= 0))
                    {
                        sq = board.get(rank, file);
                        if (sq.piece == null || isCapturablePiece(sq.piece))
                        {
                            if (!sq.isUnderAttackFrom(opponent, false))
                            {
                                return false;
                            }
                        }
                    }
                }
            }
            return true;
        }

        private boolean noIntercepts()
        {
            if (checkingPiecesList.size() == 1)
            {
                if (!(checkingPiecesList.get(0).getClass().equals(Knight.class) || checkingPiecesList.get(0).getClass().equals(Pawn.class)))
                {
                    int kRank = square.rank,
                        kFile = square.file;

                    int rankInc = (int) Math.signum(checkingPiecesList.get(0).square.rank - kRank),
                        fileInc = (int) Math.signum(checkingPiecesList.get(0).square.file - kFile);

                    int r = kRank + rankInc, f = kFile + fileInc;
                    while (Math.abs(checkingPiecesList.get(0).square.rank - r) >= 1
                            || Math.abs(checkingPiecesList.get(0).square.file - f) >= 1)
                    {
                        if (board.get(r, f).isUnderAttackFromNonKingPieces(self, true))
                        {
                            return false;
                        }
                        r += rankInc;
                        f += fileInc;
                    }
                }
            }
            return true;
        }

        private boolean checkingPieceNotCapturable()
        {
            return (checkingPiecesList.size() == 1)
                 && checkingPiecesList.get(0).square.isUnderAttackFrom(opponent, true)
                 && checkingPiecesList.get(0).square.isUnderAttackFrom(self, true);
        }

    }

    public class Queen extends Piece
    {
        public Queen(int rank, int file)
        {
            icon = "\u265B";
            moveTo(board.get(rank, file));
        }

        @Override
        public boolean isLegal(Board.Move move)
        {
            if (move.targetSquare.piece == null || isCapturablePiece(move.targetSquare.piece))
            {
                if (move.sourceSquare.file == move.targetSquare.file
                    || move.sourceSquare.rank == move.targetSquare.rank
                    || (Math.abs(move.sourceSquare.rank - move.targetSquare.rank)
                        == Math.abs(move.sourceSquare.file - move.targetSquare.file)))
                {
                    int rankInc = (int) Math.signum(move.targetSquare.rank - move.sourceSquare.rank),
                        fileInc = (int) Math.signum(move.targetSquare.file - move.sourceSquare.file);
                    for (int rank = move.sourceSquare.rank + rankInc, file = move.sourceSquare.file + fileInc;
                            Math.abs(rank - move.targetSquare.rank) > 0 || Math.abs(file - move.targetSquare.file) > 0;
                            rank += rankInc, file += fileInc)
                    {
                        if (board.get(rank, file).piece != null)
                        {
                            return false;
                        }
                    }
                    return true;
                }
            }
            return false;
        }

        @Override
        public void makeMove(Move m)
        {
            moveTo(m.targetSquare);
        }
    }

    public class Rook extends Piece
    {
        boolean notMoved;

        public Rook(int rank, int file)
        {
            icon = "\u265C";
            moveTo(board.get(rank, file));
            notMoved = true;
        }

        @Override
        public boolean isLegal(Board.Move move)
        {
            if (move.targetSquare.piece == null || isCapturablePiece(move.targetSquare.piece))
            {
                if ((move.sourceSquare.file == move.targetSquare.file && move.sourceSquare.rank != move.targetSquare.rank)
                        || (move.sourceSquare.rank == move.targetSquare.rank && move.sourceSquare.file != move.targetSquare.file))
                {
                    int rankInc = (int) Math.signum(move.targetSquare.rank - move.sourceSquare.rank),
                        fileInc = (int) Math.signum(move.targetSquare.file - move.sourceSquare.file);
                    for (int rank = move.sourceSquare.rank + rankInc, file = move.sourceSquare.file + fileInc;
                            Math.abs(rank - move.targetSquare.rank) > 0 || Math.abs(file - move.targetSquare.file) > 0;
                            rank += rankInc, file += fileInc)
                    {
                        if (board.get(rank, file).piece != null)
                        {
                            return false;
                        }
                    }
                    return true;
                }
            }
            return false;
        }

        @Override
        public void makeMove(Move m)
        {
            notMoved = false;
            moveTo(m.targetSquare);
        }        
    }

    public class Bishop extends Piece
    {
        public Bishop(int rank, int file)
        {
            icon = "\u265D";
            moveTo(board.get(rank, file));
        }

        @Override
        public boolean isLegal(Board.Move move)
        {
            if (move.targetSquare.piece == null || isCapturablePiece(move.targetSquare.piece))
            {
                if ((Math.abs(move.sourceSquare.rank - move.targetSquare.rank)
                        == Math.abs(move.sourceSquare.file - move.targetSquare.file)))
                {
                    int rankInc = (int) Math.signum(move.targetSquare.rank - move.sourceSquare.rank),
                        fileInc = (int) Math.signum(move.targetSquare.file - move.sourceSquare.file);
                    for (int rank = move.sourceSquare.rank + rankInc, file = move.sourceSquare.file + fileInc;
                            Math.abs(rank - move.targetSquare.rank) > 0 || Math.abs(file - move.targetSquare.file) > 0;
                            rank += rankInc, file += fileInc)
                    {
                        if (board.get(rank, file).piece != null)
                        {
                            return false;
                        }
                    }
                    return true;
                }
            }
            return false;
        }

        @Override
        public void makeMove(Move m)
        {
            moveTo(m.targetSquare);
        }
    }

    public class Knight extends Piece
    {
        public Knight(int rank, int file)
        {
            icon = "\u265E";
            moveTo(board.get(rank, file));
        }

        @Override
        boolean isLegal(Board.Move move)
        {
            if (move.targetSquare.piece == null || isCapturablePiece(move.targetSquare.piece))
            {
                int rankDiff = Math.abs(move.targetSquare.rank - move.sourceSquare.rank);
                int fileDiff = Math.abs(move.targetSquare.file - move.sourceSquare.file);

                if ((rankDiff == 2 && fileDiff == 1) || (fileDiff == 2 && rankDiff == 1))
                {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void makeMove(Move m)
        {
            moveTo(m.targetSquare);
        }
    }

    public class Pawn extends Piece
    {
        boolean enPassantInProgress;

        public Pawn(int rank, int file)
        {
            icon = "\u265F";
            moveTo(board.get(rank, file));
            enPassantInProgress = false;
        }

        @Override
        boolean isLegal(Board.Move move)
        {
            int rankDiff = (move.targetSquare.rank - move.sourceSquare.rank) * ((isWhite) ? 1 : -1);
            int fileDiff = Math.abs(move.targetSquare.file - move.sourceSquare.file);
            boolean result = false;
            if ((rankDiff == 1) && (fileDiff == 1)) // capture
            {
                if (isCapturablePiece(move.targetSquare.piece))
                {
                    return true;
                }
                else
                {
                    enPassantInProgress = game.isLegalEnPassant(move);
                    return enPassantInProgress;
                }
            }
            if ((rankDiff == 1) && (fileDiff == 0)) // 1-square-forward move
            {
                return (move.targetSquare.piece == null);
            }
            if ((rankDiff == 2) && (fileDiff == 0)) // 2-squares-forward move
            {
                return ((move.sourceSquare.rank == ((isWhite) ? 1 : 6)) && move.targetSquare.piece == null);
            }

            return false;
        }

        @Override
        public void makeMove(Move m)
        {
            // remove captured pawn
            if (enPassantInProgress)
            {
                game.markAsCaptured(board.get(m.sourceSquare.rank, m.targetSquare.file).piece);
                board.get(m.sourceSquare.rank, m.targetSquare.file).setPiece(null);
            }

            moveTo(m.targetSquare);

            if (m.targetSquare.rank == ((isWhite) ? 7 : 0))
            {
                promote(this);
            }

            enPassantInProgress = false;
        }

        private void promote(Pawn p)
        {
            System.out.print("Promote to (q, r, b, n): ");
            switch(in.next().charAt(0))
            {
                case 'q': queens.add(new Queen(p.square.rank, p.square.file)); break;
                case 'r': rooks.add(new Rook(p.square.rank, p.square.file)); break;
                case 'b': bishops.add(new Bishop(p.square.rank, p.square.file)); break;
                case 'n': knights.add(new Knight(p.square.rank, p.square.file)); break;
            }            
            game.markAsCaptured(p);
        }
    }

    private boolean isCapturablePiece(Piece piece)
    {
        return (piece!= null && !piece.getClass().equals(King.class) && piece.getSide().equals(opponent));
    }
}
