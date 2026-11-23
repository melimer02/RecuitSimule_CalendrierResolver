




import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * Représente un état complet du problème pour le RS.
 * Un état est défini par la liste de tous les placements des pièces.
 * Chaque placement indique : quelle pièce, quelle orientation, et où .
 *--------------------------------------------------------------------------------------------------------------------------------------------*/

public class PlacementState {
    

    //Liste de toutes les pièces disponibles 
    private final List<Polyomino> pieces;

    //Placement courant de chaque pièce : placements[i] = {pieceIdx, orientIdx, row, col}
    private final int[][] placements; 

    //random
    private final Random random;



    /**
     * Crée un état avec des placements initiaux aléatoires.
     * @param pieces liste de tous les polyominos
     * @param rng    générateur aléatoire 
     */

    public PlacementState(List<Polyomino> pieces, Random rng) {
        this.pieces = pieces;
        this.random = rng;
        this.placements = new int[pieces.size()][4];

        // Initialise tous les placements à -1 ( non placé)
        for (int i = 0; i < pieces.size(); i++) {
            placements[i][0] = i;
            placements[i][1] = -1; // orientation non définie
            placements[i][2] = -1; // row non définie
            placements[i][3] = -1; // col non définie
        }
    }

    //  Constructeur de copie

    public PlacementState(PlacementState other) {
        this.pieces = other.pieces;
        this.random = other.random;
        this.placements = new int[other.placements.length][4];
        for (int i = 0; i < other.placements.length; i++) {
            this.placements[i] = other.placements[i].clone();
        }
    }


    //-----Accesseurs------------

    /** @return le placement d'une pièce */
    public int[] getPlacement(int pieceIdx) { return placements[pieceIdx]; }

    /** @return true si la pièce est actuellement placée */
    public boolean isPlaced(int pieceIdx) { return placements[pieceIdx][1] >= 0; }

    /** @return le nombre de pièces */
    public int getPieceCount() { return pieces.size(); }

    /** @return la liste des pièces */
    public List<Polyomino> getPieces() { return pieces; }






    //--------------------------construction de la grille depuis l'etat des placements
     /** 
     * @param dayLabel   étiquette du jour de la semaine
     * @param dayNumber  étiquette du jour du mois
     * @param monthLabel étiquette du mois
     * @return la grille reconstruite
     */
    public Grille buildGrid(String dayLabel, String dayNumber, String monthLabel) {
        Grille g = new Grille(dayLabel, dayNumber, monthLabel);
        for (int i = 0; i < pieces.size(); i++) {
            if (!isPlaced(i)) continue;
            int orientIdx = placements[i][1];
            int row = placements[i][2];
            int col = placements[i][3];
            int[][] cells = pieces.get(i).getOrientations().get(orientIdx);
            // Tente le placement ( si chevauchement j'ignore)
            g.place(i, orientIdx, cells, row, col);
        }
        return g;
    }


    // -----------------------Génération de placements valides
    
    /**
     * Retourne tous les placements valides pour une pièce  sur une grille donnée.
     * Un placement est valide si toutes les cellules de la pièce tombent sur des
     * cellules libres  de la grille.
     *
     * @param pieceIdx index de la pièce
     * @param grid     grille courante
     * @return liste de placements valides 
     */
    public List<int[]> getValidPlacements(int pieceIdx, Grille grid) {
        List<int[]> valid = new ArrayList<>();
        Polyomino piece = pieces.get(pieceIdx);
        List<int[][]> orientations = piece.getOrientations();

        for (int oi = 0; oi < orientations.size(); oi++) {
            int[][] cells = orientations.get(oi);
            // Essaie toutes les positions de la grille
            for (int r = 0; r < Grille.ROWS; r++) {
                for (int c = 0; c < Grille.COLS; c++) {
                    if (canPlace(cells, r, c, grid)) {
                        valid.add(new int[]{oi, r, c});
                    }
                }
            }
        }
        return valid;
    }

    //-------------------------Vérifie si une orientation peut être placée à (row, col) sur la grille.

    private boolean canPlace(int[][] cells, int row, int col, Grille grid) {
        for (int[] cell : cells) {
            int r = row + cell[0];
            int c = col + cell[1];
            if (!grid.isFree(r, c)) return false;
        }
        return true;
    }

    /**
     * Retourne un placement aléatoire valide pour une pièce.
     * Retourne null si aucun placement n'est possible.
     * @param pieceIdx index de la pièce
     * @param grid     grille courante (sans cette pièce)
     * @return {orientIdx, row, col} ou null
     */
    public int[] randomValidPlacement(int pieceIdx, Grille grid) {
        List<int[]> valid = getValidPlacements(pieceIdx, grid);
        if (valid.isEmpty()) return null;
        return valid.get(random.nextInt(valid.size()));
    }
}