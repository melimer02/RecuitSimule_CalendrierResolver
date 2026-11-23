

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Représente un polyomino 
 * Chaque pièce est définie par un ensemble de cellules relatives à une origine (0,0).
 * Les cellules sont des paires (ligne, colonne).
 -------------------------------------------------------------------------------------------------------------------------*/

public class Polyomino {

    //nom de la piece
    private final String name;

    /**
     * Toutes les orientations possibles de cette pièce (rotations + miroirs)
     * sans doublons. Chaque orientation est un tableau de cellules.
     */
    private final List<int[][]> orientations;


// Constructeurs-------------------------

    /**
     * Construit un polyomino à partir d'une forme de base.
     * Toutes les rotations et leurs miroirs sont générés
     * automatiquement sans doublons
     *
     * @param name  nom de la pièce
     * @param shape forme de base 
     */

    public Polyomino(String name, int[][] shape) {
        this.name = name;
        this.orientations = generateOrientations(shape);
    }


//Methodes--------------------------------

    /** @return le nom de la pièce */
    public String getName() { return name; }


    /** @return toutes les orientations uniques de la pièce */
    public List<int[][]> getOrientations() { return orientations; }


    // Génère toutes les orientations d'une forme.
    private List<int[][]> generateOrientations(int[][] base) {
        Set<String> seen = new HashSet<>();
        List<int[][]> result = new ArrayList<>();

        int[][] current = normalize(base);
        for (int r = 0; r < 4; r++) {
            tryAdd(current, seen, result);         // Orientation normale
            tryAdd(mirror(current), seen, result); // Miroir horizontal
            current = rotate90(current);           // Rotation 90° pour la prochaine itération
        }
        return result;
    }


    //Ajoute une forme à la liste  ( cle = representation textuelle)
    private void tryAdd(int[][] shape, Set<String> seen, List<int[][]> result) {
        int[][] norm = normalize(shape);
        String key = toKey(norm);
        if (seen.add(key)) {
            result.add(norm);
        }
    }

    /**
     * Rotation de 90° dans le sens horaire 
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
     * Miroir horizontal  , On normalise ensuite.
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
     */

    private String toKey(int[][] shape) {
        StringBuilder sb = new StringBuilder();
        for (int[] cell : shape) {
            if (sb.length() > 0) sb.append(';');
            sb.append(cell[0]).append(',').append(cell[1]);
        }
        return sb.toString();
    }

//----------------------------------------Creation de toutes les pieces ------------------------------------------------//
    /**
     * Crée et retourne la liste complète des pièces utilisées dans le jeu :
     * - Tetrominos : Z, L, I
     * - Pentominos : T, P, L, V, N, Z, U
     * @return liste de tous les polyominos
     */


    public static List<Polyomino> createAllPieces() {
        List<Polyomino> pieces = new ArrayList<>();

        // --- tetrominos (4 cellules) ---

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