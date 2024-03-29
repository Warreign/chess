package cz.cvut.fel.pjv.shubevik.game.pieces;

import cz.cvut.fel.pjv.shubevik.game.PColor;
import cz.cvut.fel.pjv.shubevik.game.Game;
import cz.cvut.fel.pjv.shubevik.game.moves.Move;

public class King extends Piece {

    public King(PColor color) { super(color); }

    public boolean isValid(Game game, Move move) {
        return (!move.getEnd().isOccupied() || move.getEnd().getPieceColor() != getColor()) &&
                ((xDiff(move) <= 1 && yDiff(move) <= 1) ||
                        (xDiff(move) == 0 && yDiff(move) == 2 &&
                         canCastleRook(game, move) &&
                         canCastleKing(game, move)));
    }
    public boolean inCheck(Game game) {
        return game.tileUnderAttack(game.findKings().get(getColor()));
    }

    public boolean canCastleRook(Game game, Move move) {
        boolean r;
        if (getColor() == PColor.WHITE) {
            if (isRight(move)) {
                Piece p = game.getBoard().getPiece(0, 7);
                return p instanceof Rook && !p.wasMoved();
            }
            else { // left
                Piece p = game.getBoard().getPiece(0, 0);
                return p instanceof Rook && !p.wasMoved() && !game.getBoard().getTile(0, 1).isOccupied();
            }

        }
        else { // black
            if (isRight(move)) {
                Piece p = game.getBoard().getPiece(7,7);
                return p instanceof Rook && !p.wasMoved();
            }
            else { // left
                Piece p = game.getBoard().getPiece(7, 0);
                return p instanceof Rook && !p.wasMoved() && !game.getBoard().getTile(7,1).isOccupied();
            }
        }
    }

    public boolean canCastleKing(Game game, Move move) {
        boolean r;
        if (getColor() == PColor.WHITE) {
            if (isRight(move)) {
                return !wasMoved() && !inCheck(game) &&
                        !game.checkAfterMove(new Move(game.getTile(0,4), game.getTile(0,5))) &&
                        !game.checkAfterMove(new Move(game.getTile(0,4), game.getTile(0,6)));
            }
            else { // left
                return !wasMoved() && !inCheck(game) &&
                        !game.checkAfterMove(new Move(game.getTile(0,4), game.getTile(0,3))) &&
                        !game.checkAfterMove(new Move(game.getTile(0,4), game.getTile(0,2)));
            }
        }
        else { // black
            if (isRight(move)) {
                return !wasMoved() && !inCheck(game) &&
                        !game.checkAfterMove(new Move(game.getTile(7,4), game.getTile(7,5))) &&
                        !game.checkAfterMove(new Move(game.getTile(7,4), game.getTile(7,6)));
            }
            else { // left
                return !wasMoved() && !inCheck(game) &&
                        !game.checkAfterMove(new Move(game.getTile(7,4), game.getTile(7,2))) &&
                        !game.checkAfterMove(new Move(game.getTile(7,4), game.getTile(7,3)));
            }
        }
    }

    @Override
    public String toString() {
        return "K";
    }
}
