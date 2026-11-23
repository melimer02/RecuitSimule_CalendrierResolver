

import java.util.ArrayList;
import java.util.List;

/**
 * Représente la grille du calendrier .
 * Structure de la grille (8 colonnes x 8 lignes) 
 * Les cellules hors grille ne peuvent pas être couvertes.
 * La grille réelle contient 50 cellules valides.
 * 3 cellules sont laissées libres (la date courante : jour semaine + jour mois + mois).*/
//---------------------------------------------------------------------------------------------------------------



public class Grille {

   
    public static final int ROWS = 8;   //lignes
    public static final int COLS = 7;  // colonne

    // celulle du calendrier de la grille
    public static final boolean[][] VALID_CELLS = {
        {true,  true,  true,  false, false, false, false},  
        {true,  true,  true,  true,  true,  true,  true }, 
        {true,  true,  true,  true,  true,  true,  true },  
        {true,  true,  true,  true,  true,  true,  true }, 
        {true,  true,  true,  true,  true,  true,  true },  
        {true,  true,  true,  true,  true,  true,  true },  
        {false, true,  true,  true,  true,  true,  true },  
        {false, true,  true,  true,  true,  true,  true }   
    };

    // label des cellules pour affichage. " " signifie hors grille.
 
    public static final String[][] LABELS = {
        {"lun.", "mar.", "mer.", "",      "",     "",    ""},
        {"jeu.", "ven.", "sam.", "dim.",  "1",    "2",   "3"},
        {"4",    "5",    "6",    "7",     "8",    "9",   "10"},
        {"11",   "12",   "13",   "14",   "15",   "16",  "17"},
        {"18",   "19",   "20",   "21",   "22",   "23",  "24"},
        {"25",   "26",   "27",   "28",   "29",   "30",  "31"},
        {"",     "janv.","févr.","mars", "avr.", "mai", "juin"},
        {"",     "juil.","août", "sept.","oct.", "nov.","déc."}
    };


    // Nombre total de cellules valides dans la grille 
    public static final int TOTAL_VALID_CELLS = countValidCells();
 
    //État de chaque cellule : -1 = hors grille ,   0 = libre ,   k = couverte par la pièce k 
    private int[][] grid;

    //Liste des placements actifs.
    private List<int[]> placements;

    // cellule que on doit laisser libre ( date )
    private int[][] targetCells; 

//---------------------------------Constructeurs------------------------------
    /**
     * Initialise une grille vide.
     * @param dayLabel   étiquette du jour de la semaine 
     * @param dayNumber  étiquette du jour du mois 
     * @param monthLabel étiquette du mois 
     */
    public Grille(String dayLabel, String dayNumber, String monthLabel) {
        grid = new int[ROWS][COLS];
        placements = new ArrayList<>();

        // Initialise les cellules hors grille à -1
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                grid[r][c] = VALID_CELLS[r][c] ? 0 : -1;
            }
        }

        // Trouve les coordonnées des 3 cellules cibles
        targetCells = new int[3][2];
        int found = 0;
        String[] targets = {dayLabel, dayNumber, monthLabel};
        for (int r = 0; r < ROWS && found < 3; r++) {
            for (int c = 0; c < COLS && found < 3; c++) {
                for (String t : targets) {
                    if (LABELS[r][c].equals(t)) {
                        boolean already = false;
                        for (int i = 0; i < found; i++) {
                            if (targetCells[i][0] == r && targetCells[i][1] == c) {
                                already = true; break;
                            }
                        }
                        if (!already) {
                            targetCells[found][0] = r;
                            targetCells[found][1] = c;
                            found++;
                            break;
                        }
                    }
                }
            }
        }
        if (found < 3) {
            throw new IllegalArgumentException(
                "Date invalide : '" + dayLabel + "', '" + dayNumber + "', '" + monthLabel + "'");
        }

        // Marque les cellules cibles reserve
        for (int[] cell : targetCells) {
            grid[cell[0]][cell[1]] = -2;
        }
    }

    //  Constructeur de copie profonde.

    public Grille(Grille other) {
        this.grid = new int[ROWS][COLS];
        for (int r = 0; r < ROWS; r++) {
            this.grid[r] = other.grid[r].clone();
        }
        this.placements = new ArrayList<>();
        for (int[] p : other.placements) {
            this.placements.add(p.clone());
        }
        this.targetCells = new int[3][2];
        for (int i = 0; i < 3; i++) {
            this.targetCells[i] = other.targetCells[i].clone();
        }
    }

    //------------------------Getters et setters ---------------------------------------//

    // retourne la valeur d'une cellule (-1=hors grille, -2=cible, 0=libre, k=pièce k)
    public int getCell(int row, int col) { return grid[row][col]; }

    // retourne true si la cellule est dans les limites de la grille 
    public boolean inBounds(int row, int col) {
        return row >= 0 && row < ROWS && col >= 0 && col < COLS;
    }

    // retourne true si la cellule est une cellule valide de la grille 
    public boolean isValid(int row, int col) {
        return inBounds(row, col) && VALID_CELLS[row][col];
    }

    // retourne true si la cellule est libre 
    public boolean isFree(int row, int col) {
        return inBounds(row, col) && grid[row][col] == 0;
    }

    // retourne true si la cellule est une cible
    public boolean isTarget(int row, int col) {
        return inBounds(row, col) && grid[row][col] == -2;
    }

    // retourne la liste des placements actuels
    public List<int[]> getPlacements() { return placements; }

    // retourne les cellules cibles 
    public int[][] getTargetCells() { return targetCells; }



    //----------------placement des pieces et retrait -------------------------------
    /**
     * Tente de placer une orientation de pièce à la position (row, col).
     * Retourne true si le placement est valide et a été effectué.
     * @param pieceIdx        index de la pièce dans la liste globale 
     * @param orientationIdx  index de l'orientation dans la liste de la pièce
     * @param cells           cellules de l'orientation 
     * @param row             ligne de l'origine
     * @param col             colonne de l'origine
     * @return true si placement réussi
     */
    public boolean place(int pieceIdx, int orientationIdx, int[][] cells, int row, int col) {
        // on verifie que les cells sont valide 
        for (int[] cell : cells) {
            int r = row + cell[0];
            int c = col + cell[1];
            if (!isFree(r, c)) return false;
        }
        // Effectue le placement
        int pieceId = placements.size() + 1; 
        for (int[] cell : cells) {
            grid[row + cell[0]][col + cell[1]] = pieceId;
        }
        placements.add(new int[]{pieceIdx, orientationIdx, row, col});
        return true;
    }

    /**
     * Retire le dernier placement effectué.
     * @param cells cellules de l'orientation placée
     */
    public void removeLast(int[][] cells) {
        if (placements.isEmpty()) return;
        int[] last = placements.remove(placements.size() - 1);
        int row = last[2], col = last[3];
        for (int[] cell : cells) {
            grid[row + cell[0]][col + cell[1]] = 0;
        }
    }

    /**
     * Retire un placement spécifique par son index dans la liste.
     et Recalcule les id de toutes les pièces.
     */
    public void removePlacement(int placementIdx, int[][] cells) {
        if (placementIdx < 0 || placementIdx >= placements.size()) return;
        int[] p = placements.get(placementIdx);
        int row = p[2], col = p[3];

        // Efface les cellules de cette pièce
        for (int[] cell : cells) {
            grid[row + cell[0]][col + cell[1]] = 0;
        }
        placements.remove(placementIdx);

        // Recalcule tous les IDs dans la grille
        rebuildGridIds();
    }

    /**
     * Reconstruis la grille à partir de la liste de placements.
     */
    private void rebuildGridIds() {
        // Remet à 0 toutes les cellules couvertes
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (grid[r][c] > 0) grid[r][c] = 0;
            }
        }
    }


    /**
     * Vide complètement la grille de tous ses placements.
     */
    public void clearPlacements() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (grid[r][c] > 0) grid[r][c] = 0;
            }
        }
        placements.clear();
    }

   //-----------------------fitness----------------------------------------------------------------
    /**
     * calcule le  nombre de cellules non couvertes parmi les cellules normales valide
     * Une solution parfaite a une fitness de 0 
     * @return nombre de cellules valides non-couvertes */


    public int computeFitness() {
        int uncovered = 0;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (grid[r][c] == 0) { // libre mais pas cible
                    uncovered++;
                }
            }
        }
        return uncovered;
    }

    /**
     * Compte les cellules libres  dans la grille.
     * @return nombre de cellules libres
     */
    public int countFreeCells() {
        int count = 0;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (grid[r][c] == 0) count++;
            }
        }
        return count;
    }

    /**
     * Retourne true si la grille est dans un état solution valide : fitness== 0 */
    public boolean isSolved() {
        return computeFitness() == 0;
    }

    //-------------------fonctions utilitaires -----------------------------------------

    //Compte le nombre total de cellules valides dans la grille
    private static int countValidCells() {
        int count = 0;
        for (boolean[] row : VALID_CELLS) {
            for (boolean v : row) if (v) count++;
        }
        return count;
    }

    //Affiche la grille dans la console

    public void printGrid() {
        System.out.println("Grille (fitness=" + computeFitness() + ", placements=" + placements.size() + "):");
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                int v = grid[r][c];
                if (v == -1) System.out.printf("  . ");
                else if (v == -2) System.out.printf(" [*]");
                else if (v == 0) System.out.printf("  _ ");
                else System.out.printf("%3d ", v);
            }
            System.out.println();
        }
        System.out.println();
    }

    @Override
    public String toString() {
        return "Calendrier[cells=" + TOTAL_VALID_CELLS + ", fitness=" + computeFitness() + "]";
    }
}