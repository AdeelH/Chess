/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package chess;

import chess.Board.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JButton;
import javax.swing.SwingConstants;

public class ChessLabel extends JButton //implements MouseListener
{
    Board board;
    Square square;
    Font font = new Font("Ariel", Font.PLAIN, 35);
    Color bgLight = new Color(185, 140, 100);
    Color bgDark = new Color(139, 90, 59);

    ChessLabel(String s, Board b, Square sq)
    {
        super(s);
        board = b;
        square = sq;
        addMouseListener(new MouseListener()
        {

            @Override
            public void mouseClicked(MouseEvent e)
            {
                generateMove();
            }

            @Override
            public void mousePressed(MouseEvent e)
            {
                e.consume();
            }

            @Override
            public void mouseReleased(MouseEvent e)
            {
                e.consume();
            }

            @Override
            public void mouseEntered(MouseEvent e)
            {                
                e.consume();
            }

            @Override
            public void mouseExited(MouseEvent e)
            {                
                e.consume();
            }
        });
    }

    public void set(int rank, int file)
    {
        setBorderPainted(false);
        setFont(font);
        setOpaque(true);
        setBackground((rank + file) % 2 == 0 ? bgDark : bgLight);
        setHorizontalAlignment(SwingConstants.CENTER);
    }

    public void generateMove()
    {
        if (board.sq1 == null)
        {
            board.sq1 = square;
            square.icon.setBorderPainted(true);
//            square.icon.setBackground(Color.red);
        }
        else if (board.sq2 == null)
        {
            board.sq2 = square;                    
            square.icon.setBorderPainted(true);
            board.processMove(new Move(board.sq1, board.sq2));
//            square.icon.setBackground(Color.red);
        }
        else
        {
            board.discardMove();
            System.out.println("hey");
        }
    }
    
}

