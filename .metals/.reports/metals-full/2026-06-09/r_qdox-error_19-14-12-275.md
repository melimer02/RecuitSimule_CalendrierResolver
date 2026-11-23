error id: file://<WORKSPACE>/PlacementState.java
file://<WORKSPACE>/PlacementState.java
### com.thoughtworks.qdox.parser.ParseException: syntax error @[27,19]

error in qdox parser
file content:
```java
offset: 900
uri: file://<WORKSPACE>/PlacementState.java
text:
```scala
public class PlacementState {
    
}

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Représente un état complet du problème pour le recuit simulé.
 *
 * Un état est défini par la liste de tous les placements des pièces.
 * Chaque placement indique : quelle pièce, quelle orientation, et où (row, col).
 *
 * Structure d'un placement : int[] {pieceIndex, orientationIndex, row, col}
 *   - pieceIndex      : index dans Polyomino.createAllPieces() (0-basé)
 *   - orientationIndex: index dans piece.getOrientations() (0-basé)
 *   - row, col        : position de l'origine dans la grille
 *
 * Pour le recuit simulé, un voisin est généré en :
 *   1. Retirant aléatoirement une pièce de son emplacement actuel
 *   2. La replaçant aléatoirement dans un emplacement valide (ou pas si aucun)
 */


    /** Liste de toutes les pièces disponibles */
    private final L@@ist<Polyomino> pieces;

    /** Placement courant de chaque pièce : placements[i] = {pieceIdx, orientIdx, row, col} */
    private final int[][] placements; // placements[pieceIdx]

    /** Générateur de nombres aléatoires */
    private final Random random;

    // -------------------------------------------------------------------------
    // Constructeur
    // -------------------------------------------------------------------------

    /**
     * Crée un état avec des placements initiaux aléatoires.
     * Certaines pièces peuvent ne pas avoir de placement valide au départ.
     *
     * @param pieces liste de tous les polyominos
     * @param rng    générateur aléatoire partagé
     */
    public PlacementState(List<Polyomino> pieces, Random rng) {
        this.pieces = pieces;
        this.random = rng;
        this.placements = new int[pieces.size()][4];

        // Initialise tous les placements à "non placé" (-1)
        for (int i = 0; i < pieces.size(); i++) {
            placements[i][0] = i;
            placements[i][1] = -1; // orientation non définie
            placements[i][2] = -1; // row non définie
            placements[i][3] = -1; // col non définie
        }
    }

    /**
     * Constructeur de copie.
     */
    public PlacementState(PlacementState other) {
        this.pieces = other.pieces;
        this.random = other.random;
        this.placements = new int[other.placements.length][4];
        for (int i = 0; i < other.placements.length; i++) {
            this.placements[i] = other.placements[i].clone();
        }
    }

    // -------------------------------------------------------------------------
    // Accès
    // -------------------------------------------------------------------------

    /** @return le placement d'une pièce : {pieceIdx, orientIdx, row, col} ou [-1,-1,-1,-1] */
    public int[] getPlacement(int pieceIdx) { return placements[pieceIdx]; }

    /** @return true si la pièce est actuellement placée */
    public boolean isPlaced(int pieceIdx) { return placements[pieceIdx][1] >= 0; }

    /** @return le nombre de pièces */
    public int getPieceCount() { return pieces.size(); }

    /** @return la liste des pièces */
    public List<Polyomino> getPieces() { return pieces; }

    // -------------------------------------------------------------------------
    // Construction de la grille depuis l'état
    // -------------------------------------------------------------------------

    /**
     * Reconstruit la grille à partir de la liste de placements.
     * Utilisé pour évaluer l'énergie et pour l'affichage.
     *
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
            // Tente le placement (peut échouer si chevauchement → ignoré)
            g.place(i, orientIdx, cells, row, col);
        }
        return g;
    }

    // -------------------------------------------------------------------------
    // Génération de placements valides
    // -------------------------------------------------------------------------

    /**
     * Retourne tous les placements valides pour une pièce donnée sur une grille donnée.
     * Un placement est valide si toutes les cellules de la pièce tombent sur des
     * cellules libres (valeur 0) de la grille.
     *
     * @param pieceIdx index de la pièce
     * @param grid     grille courante
     * @return liste de placements valides : chaque élément = {orientIdx, row, col}
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

    /**
     * Vérifie si une orientation peut être placée à (row, col) sur la grille.
     */
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
     *
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

QDox parse error in file://<WORKSPACE>/PlacementState.java