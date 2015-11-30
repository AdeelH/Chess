/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package chess;

import chess.Board.*;
import chess.Side.*;
import java.awt.Color;

/**
 *
 * @author Adeel
 */
public class Square
{
    private final Board board;
    public Piece piece;
    public int file;
    public int rank;
    ChessLabel icon;

    public Square(Board b, int rank, int file)
    {
        board = b;
        piece = null;
        setCoordinates(rank, file);
        icon = new ChessLabel("", board, this);
    }

    public void setPiece(Piece targetPiece)
    {
        this.piece = targetPiece;
        if (targetPiece == null)
        {
            icon.setText("");
        }
        else
        {
            icon.setText(targetPiece.icon);
            icon.setForeground((targetPiece.isWhite()) ? new Color(255,237,210) : new Color(50,50,50));
        }
    }

    private void setCoordinates(int rank, int file)
    {
        this.rank = rank;
        this.file = file;
    }

    public boolean isUnderAttackFrom(Side attackingSide, boolean pinMatters)
    {
        return board.isUnderAttack(this, attackingSide, pinMatters);
    }
    
    public boolean isUnderAttackFromNonKingPieces(Side attackingSide, boolean pinMatters)
    {
        return board.isUnderAttackFromNonKingPieces(this, attackingSide, pinMatters);
    }
}
