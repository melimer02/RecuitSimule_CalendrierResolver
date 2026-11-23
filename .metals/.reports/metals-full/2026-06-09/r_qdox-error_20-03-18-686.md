error id: file://<WORKSPACE>/Grille.java
file://<WORKSPACE>/Grille.java
### com.thoughtworks.qdox.parser.ParseException: syntax error @[38,6]

error in qdox parser
file content:
```java
offset: 1399
uri: file://<WORKSPACE>/Grille.java
text:
```scala


import java.util.ArrayList;
import java.util.List;

/**
 * Représente la grille du calendrier puzzle.
 * Structure de la grille (8 colonnes x 8 lignes) 
 * Les cellules hors grille ne peuvent pas être couvertes.
 * La grille réelle contient 50 cellules valides.
 * 3 cellules sont laissées libres (la date courante : jour semaine + jour mois + mois).
 *   grid[row][col] = -1  → cellule hors grille 
 *   grid[row][col] =  0  → cellule libre 
 *   grid[row][col] =  k  → cellule couverte par la pièce numéro k */
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

    //Étiquettes des cellules pour affichage.
     *@@ "" = hors grille.
     */
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

    /** Nombre total de cellules valides dans la grille */
    public static final int TOTAL_VALID_CELLS = countValidCells();

    // -------------------------------------------------------------------------
    // État interne
    // -------------------------------------------------------------------------

    /**
     * État de chaque cellule :
     *  -1 = hors grille
     *   0 = libre
     *   k = couverte par la pièce k (index 1-basé dans la liste des placements)
     */
    private int[][] grid;

    /**
     * Liste des placements actifs.
     * Un placement = {pieceIndex, orientationIndex, row, col}
     * pieceIndex : index dans la liste des pièces (0-basé)
     */
    private List<int[]> placements;

    /** Cellules cibles à laisser libres (la date courante) */
    private int[][] targetCells; // [[r1,c1],[r2,c2],[r3,c3]]

    // -------------------------------------------------------------------------
    // Constructeur
    // -------------------------------------------------------------------------

    /**
     * Initialise une grille vide.
     * @param dayLabel   étiquette du jour de la semaine (ex: "sam.")
     * @param dayNumber  étiquette du jour du mois (ex: "27")
     * @param monthLabel étiquette du mois (ex: "déc.")
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
                        // Vérifie que ce n'est pas déjà trouvé
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

        // Marque les cellules cibles comme "réservées" (valeur -2 = cible libre)
        for (int[] cell : targetCells) {
            grid[cell[0]][cell[1]] = -2;
        }
    }

    /**
     * Constructeur de copie profonde.
     */
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

    // -------------------------------------------------------------------------
    // Accès / modification
    // -------------------------------------------------------------------------

    /** @return la valeur d'une cellule (-1=hors grille, -2=cible, 0=libre, k=pièce k) */
    public int getCell(int row, int col) { return grid[row][col]; }

    /** @return true si la cellule est dans les limites de la grille */
    public boolean inBounds(int row, int col) {
        return row >= 0 && row < ROWS && col >= 0 && col < COLS;
    }

    /** @return true si la cellule est une cellule valide de la grille */
    public boolean isValid(int row, int col) {
        return inBounds(row, col) && VALID_CELLS[row][col];
    }

    /** @return true si la cellule est libre (valeur 0) */
    public boolean isFree(int row, int col) {
        return inBounds(row, col) && grid[row][col] == 0;
    }

    /** @return true si la cellule est une cible (valeur -2) */
    public boolean isTarget(int row, int col) {
        return inBounds(row, col) && grid[row][col] == -2;
    }

    /** @return la liste des placements actuels */
    public List<int[]> getPlacements() { return placements; }

    /** @return les cellules cibles (la date) */
    public int[][] getTargetCells() { return targetCells; }

    // -------------------------------------------------------------------------
    // Placement / retrait de pièces
    // -------------------------------------------------------------------------

    /**
     * Tente de placer une orientation de pièce à la position (row, col).
     * Retourne true si le placement est valide et a été effectué.
     *
     * @param pieceIdx        index de la pièce dans la liste globale (0-basé)
     * @param orientationIdx  index de l'orientation dans la liste de la pièce
     * @param cells           cellules de l'orientation (relatives)
     * @param row             ligne de l'origine
     * @param col             colonne de l'origine
     * @return true si placement réussi
     */
    public boolean place(int pieceIdx, int orientationIdx, int[][] cells, int row, int col) {
        // Vérifie que toutes les cellules sont libres et valides
        for (int[] cell : cells) {
            int r = row + cell[0];
            int c = col + cell[1];
            if (!isFree(r, c)) return false;
        }
        // Effectue le placement
        int pieceId = placements.size() + 1; // ID 1-basé pour la grille
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
     * Recalcule les IDs de toutes les pièces.
     *
     * @param placementIdx index dans la liste des placements (0-basé)
     * @param cells        cellules de l'orientation de cette pièce
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
     * Reconstruis les IDs dans la grille à partir de la liste de placements.
     * Utilisé après suppression d'un placement intermédiaire.
     */
    private void rebuildGridIds() {
        // Remet à 0 toutes les cellules couvertes
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (grid[r][c] > 0) grid[r][c] = 0;
            }
        }
        // TODO: pour reconstruire correctement on aurait besoin des cellules de chaque pièce
        // Cette méthode est utilisée dans le recuit simulé qui reconstruit de toute façon
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

    // -------------------------------------------------------------------------
    // Métriques (fitness)
    // -------------------------------------------------------------------------

    /**
     * Calcule l'énergie (= nombre de cellules non couvertes parmi les cellules
     * normales — pas les cibles et pas hors-grille).
     *
     * Une solution parfaite a une énergie de 0 :
     *   - toutes les cellules valides non-cibles sont couvertes
     *   - les 3 cellules cibles sont libres
     *
     * @return nombre de cellules valides non-couvertes (hors cibles)
     */
    public int computeEnergy() {
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
     * Compte les cellules libres (valeur 0) dans la grille.
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
     * Retourne true si la grille est dans un état solution valide :
     * énergie == 0 (toutes cellules non-cibles couvertes).
     */
    public boolean isSolved() {
        return computeEnergy() == 0;
    }

    // -------------------------------------------------------------------------
    // Utilitaires
    // -------------------------------------------------------------------------

    /** Compte le nombre total de cellules valides dans la grille */
    private static int countValidCells() {
        int count = 0;
        for (boolean[] row : VALID_CELLS) {
            for (boolean v : row) if (v) count++;
        }
        return count;
    }

    /**
     * Affiche la grille dans la console pour le débogage.
     */
    public void printGrid() {
        System.out.println("Grille (énergie=" + computeEnergy() + ", placements=" + placements.size() + "):");
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
        return "CalendarGrid[cells=" + TOTAL_VALID_CELLS + ", energy=" + computeEnergy() + "]";
    }
}
```

```



#### Error stacktrace:

```
com.thoughtworks.qdox.parser.impl.Parser.yyerror(Parser.java:2025)
	com.thoughtworks.qdox.parser.impl.Parser.yyparse(Parser.java:2147)
	com.thoughtworks.qdox.parser.impl.Parser.parse(Parser.java:2006)
	com.thoughtworks.qdox.library.SourceLibrary.parse(SourceLibrary.java:232)
	com.thoughtworks.qdox.library.SourceLibrary.parse(SourceLibrary.java:190)
	com.thoughtworks.qdox.library.SourceLibrary.addSource(SourceLibrary.java:94)
	com.thoughtworks.qdox.library.SourceLibrary.addSource(SourceLibrary.java:89)
	com.thoughtworks.qdox.library.SortedClassLibraryBuilder.addSource(SortedClassLibraryBuilder.java:162)
	com.thoughtworks.qdox.JavaProjectBuilder.addSource(JavaProjectBuilder.java:174)
	scala.meta.internal.mtags.JavaMtags.indexRoot(JavaMtags.scala:49)
	scala.meta.internal.metals.SemanticdbDefinition$.foreachWithReturnMtags(SemanticdbDefinition.scala:99)
	scala.meta.internal.metals.Indexer.indexSourceFile(Indexer.scala:560)
	scala.meta.internal.metals.Indexer.$anonfun$reindexWorkspaceSources$3(Indexer.scala:691)
	scala.meta.internal.metals.Indexer.$anonfun$reindexWorkspaceSources$3$adapted(Indexer.scala:688)
	scala.collection.IterableOnceOps.foreach(IterableOnce.scala:630)
	scala.collection.IterableOnceOps.foreach$(IterableOnce.scala:628)
	scala.collection.AbstractIterator.foreach(Iterator.scala:1313)
	scala.meta.internal.metals.Indexer.reindexWorkspaceSources(Indexer.scala:688)
	scala.meta.internal.metals.MetalsLspService.$anonfun$onChange$2(MetalsLspService.scala:940)
	scala.runtime.java8.JFunction0$mcV$sp.apply(JFunction0$mcV$sp.scala:18)
	scala.concurrent.Future$.$anonfun$apply$1(Future.scala:691)
	scala.concurrent.impl.Promise$Transformation.run(Promise.scala:500)
	java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1144)
	java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:642)
	java.base/java.lang.Thread.run(Thread.java:1583)
```
#### Short summary: 

QDox parse error in file://<WORKSPACE>/Grille.java