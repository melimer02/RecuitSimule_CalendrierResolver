error id: file://<WORKSPACE>/Polyomino.java
file://<WORKSPACE>/Polyomino.java
### com.thoughtworks.qdox.parser.ParseException: syntax error @[44,5]

error in qdox parser
file content:
```java
offset: 1303
uri: file://<WORKSPACE>/Polyomino.java
text:
```scala


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Représente un polyomino 
 * Chaque pièce est définie par un ensemble de cellules relatives à une origine (0,0).
 * Les cellules sont des paires (ligne, colonne).
 */

public class Polyomino {

    //nom de la piece
    private final String name;

    /**
     * Toutes les orientations possibles de cette pièce (rotations + miroirs)
     * sans doublons. Chaque orientation est un tableau de cellules.
     */
    private final List<int[][]> orientations;

//---------------------------------------------------------------------------------------------------------------------------//
    /**
     * Construit un polyomino à partir d'une forme de base.
     * Toutes les rotations et leurs miroirs sont générés
     * automatiquement, les doublons sont éliminés.
     *
     * @param name  nom de la pièce
     * @param shape forme de base 
     */

    public Polyomino(String name, int[][] shape) {
        this.name = name;
        this.orientations = generateOrientations(shape);
    }
   M
//-----------------------------------------------------------------------------------------------------------------------------//

    /** @return le nom de la pièce */
    p@@ublic String getName() { return name; }


    /** @return toutes les orientations uniques de la pièce */
    public List<int[][]> getOrientations() { return orientations; }


    // Génère toutes les orientations d'une forme.
    private List<int[][]> generateOrientations(int[][] base) {
        Set<String> seen = new HashSet<>();
        List<int[][]> result = new ArrayList<>();

        int[][] current = normalize(base);
        for (int r = 0; r < 4; r++) {
            // Orientation normale
            tryAdd(current, seen, result);
            // Miroir horizontal
            tryAdd(mirror(current), seen, result);
            // Rotation 90° pour la prochaine itération
            current = rotate90(current);
        }
        return result;
    }

    //Ajoute une forme à la liste si elle n'a pas encore été vue. ( cle = representation textuelle)

    private void tryAdd(int[][] shape, Set<String> seen, List<int[][]> result) {
        int[][] norm = normalize(shape);
        String key = toKey(norm);
        if (seen.add(key)) {
            result.add(norm);
        }
    }

    /**
     * Rotation de 90° dans le sens horaire : (r, c) → (c, -r).
     * On normalise ensuite pour ramener à (0,0).
     */
    private int[][] rotate90(int[][] shape) {
        int[][] rotated = new int[shape.length][2];
        for (int i = 0; i < shape.length; i++) {
            rotated[i][0] = shape[i][1];
            rotated[i][1] = -shape[i][0];
        }
        return normalize(rotated);
    }

    /**
     * Miroir horizontal : (r, c) → (r, -c).
     * On normalise ensuite.
     */
    private int[][] mirror(int[][] shape) {
        int[][] mirrored = new int[shape.length][2];
        for (int i = 0; i < shape.length; i++) {
            mirrored[i][0] = shape[i][0];
            mirrored[i][1] = -shape[i][1];
        }
        return normalize(mirrored);
    }

    /**
     * Normalise une forme : décale toutes les cellules pour que
     * la ligne minimale et la colonne minimale soient toutes les deux 0.
     */
    private int[][] normalize(int[][] shape) {
        int minR = Integer.MAX_VALUE, minC = Integer.MAX_VALUE;
        for (int[] cell : shape) {
            minR = Math.min(minR, cell[0]);
            minC = Math.min(minC, cell[1]);
        }
        int[][] norm = new int[shape.length][2];
        for (int i = 0; i < shape.length; i++) {
            norm[i][0] = shape[i][0] - minR;
            norm[i][1] = shape[i][1] - minC;
        }
        // Tri pour la comparaison
        Arrays.sort(norm, (a, b) -> a[0] != b[0] ? a[0] - b[0] : a[1] - b[1]);
        return norm;
    }

    /**
     * Génère une clé textuelle unique pour une forme normalisée et triée.
     * Ex: "0,0;0,1;1,0;1,1"
     */
    private String toKey(int[][] shape) {
        StringBuilder sb = new StringBuilder();
        for (int[] cell : shape) {
            if (sb.length() > 0) sb.append(';');
            sb.append(cell[0]).append(',').append(cell[1]);
        }
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Factory : création de toutes les pièces du jeu
    // -------------------------------------------------------------------------

    /**
     * Crée et retourne la liste complète des pièces utilisées dans le jeu :
     * - Tetrominos : Z, L, I
     * - Pentominos : T, P, L, V, N, Z, U
     *
     * @return liste de tous les polyominos
     */
    public static List<Polyomino> createAllPieces() {
        List<Polyomino> pieces = new ArrayList<>();

        // --- TETROMINOS (4 cellules) ---

        // Tetromino Z :  XX
        //               XX  (décalé)
        pieces.add(new Polyomino("Z_tetro", new int[][]{
            {0,0},{0,1},{1,1},{1,2}
        }));

        // Tetromino L :  X
        //                X
        //                XX
        pieces.add(new Polyomino("L_tetro", new int[][]{
            {0,0},{1,0},{2,0},{2,1}
        }));

        // Tetromino I : XXXX
        pieces.add(new Polyomino("I_tetro", new int[][]{
            {0,0},{0,1},{0,2},{0,3}
        }));

        // --- PENTOMINOS (5 cellules) ---

        // Pentomino T :  XXX
        //                 X
        //                 X
        pieces.add(new Polyomino("T_pento", new int[][]{
            {0,0},{0,1},{0,2},{1,1},{2,1}
        }));

        // Pentomino P :  XX
        //                XX
        //                X
        pieces.add(new Polyomino("P_pento", new int[][]{
            {0,0},{0,1},{1,0},{1,1},{2,0}
        }));

        // Pentomino L :  X
        //                X
        //                X
        //                XX
        pieces.add(new Polyomino("L_pento", new int[][]{
            {0,0},{1,0},{2,0},{3,0},{3,1}
        }));

        // Pentomino V :  X
        //                X
        //                XXX
        pieces.add(new Polyomino("V_pento", new int[][]{
            {0,0},{1,0},{2,0},{2,1},{2,2}
        }));

        // Pentomino N :   X
        //                XX
        //                X
        //                X
        pieces.add(new Polyomino("N_pento", new int[][]{
            {0,1},{1,0},{1,1},{2,0},{3,0}
        }));

        // Pentomino Z :  XX
        //                 X
        //                 XX
        pieces.add(new Polyomino("Z_pento", new int[][]{
            {0,0},{0,1},{1,1},{2,1},{2,2}
        }));

        // Pentomino U :  X X
        //                XXX
        pieces.add(new Polyomino("U_pento", new int[][]{
            {0,0},{0,2},{1,0},{1,1},{1,2}
        }));

        return pieces;
    }

    @Override
    public String toString() {
        return name + " (" + orientations.size() + " orientations)";
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

QDox parse error in file://<WORKSPACE>/Polyomino.java