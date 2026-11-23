import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public class RecuitSimule {

    // parametres de l'algorithme ----------------------------------------------

    // Nombre de mouvements tentés à une température fixe avant de la baisser.
    private static final int NB_ITERATIONS = 2000;
    
    // le taux de refroidissement  
    private static final double TAUX_REFROIDISSEMENT = 0.995;
    
    // Le seuil d'arrêt.
    private static final double RATIO_FIN = 1e-6;
    
    // Facteur pour punir sévèrement une pièce qu'on n'a pas posée sur le plateau.
    private static final int FACTEUR_PENALITE_NON_PLACE = 2;

    // Pénalité par cellule appartenant à un îlot irrécupérable.
    private static final int PENALITE_ILOT_IRRECUPERABLE = 3;
    //-----------------------------------------------------------------------------------


    // --- Affichage du journal ---
    private boolean modeJournal = true;
    private int frequenceJournalcycle = 50; 

    // Variables de configuration
    private final List<Polyomino> pieces;
    private final String etiquetteJour;
    private final String numeroJour;
    private final String etiquetteMois;
    private final Random generateurAleatoire;

    // Mémoire de l'algorithme  sauvegarde les meilleurs placements 
    private int[][] meilleursPlacements;
    private int meilleureFitness;
    private double temperatureCourante;
    private int fitnessCourante;
    private boolean estResolu;
    private EcouteurProgression ecouteur;
    private long totalIterations;

    public RecuitSimule(String etiquetteJour, String numeroJour, String etiquetteMois, long graine) {
        this.etiquetteJour = etiquetteJour;
        this.numeroJour    = numeroJour;
        this.etiquetteMois = etiquetteMois;
        this.generateurAleatoire = (graine >= 0) ? new Random(graine) : new Random();
        this.pieces = Polyomino.createAllPieces();
        this.estResolu = false;
        this.totalIterations = 0;

        this.meilleureFitness = Integer.MAX_VALUE;
        this.meilleursPlacements = new int[pieces.size()][3];
        for (int i = 0; i < pieces.size(); i++) {
            meilleursPlacements[i] = new int[]{-1, -1, -1};
        }
    }
    //---pour envoyer les informations a l'interface ----------------------
    public interface EcouteurProgression {
        void enProgression(int energie, double temperature, long iteration, int[][] placements);
        void nouveauLog(String message);
    }

    public void setEcouteur(EcouteurProgression ecouteur) {
        this.ecouteur = ecouteur;
    }

    private void envoyerLog(String msg) {
        if (modeJournal) System.out.println(msg);
        if (ecouteur != null) ecouteur.nouveauLog(msg);
    }


    //-------------------------------------------État initial -------------------------------------------


     /* Grille de depart ,  on prend les pièces aléatoirement et pose la grille.*/
    private int[][] genererEtatInitial() {
        int[][] etat = new int[pieces.size()][3];
        for (int[] p : etat) { p[0] = -1; p[1] = -1; p[2] = -1; }

        List<Integer> ordre = new ArrayList<>();
        for (int i = 0; i < pieces.size(); i++) ordre.add(i);
        Collections.shuffle(ordre, generateurAleatoire);

        Grille grilleTemporaire = new Grille(etiquetteJour, numeroJour, etiquetteMois);

        for (int pieceIdx : ordre) {
            Polyomino piece = pieces.get(pieceIdx);
            PlacementState ps = new PlacementState(pieces, generateurAleatoire);
            List<int[]> valides = ps.getValidPlacements(pieceIdx, grilleTemporaire);
            if (valides.isEmpty()) {
                etat[pieceIdx] = new int[]{-1, -1, -1};
            } else {
                int[] choisi = valides.get(generateurAleatoire.nextInt(valides.size()));
                etat[pieceIdx] = choisi.clone(); 
                int[][] cellules = piece.getOrientations().get(choisi[0]);
                grilleTemporaire.place(pieceIdx, choisi[0], cellules, choisi[1], choisi[2]);
            }
        }
        return etat;
    }




    /** Calcule la temerature de départ pour l'etat initial 
     * On fait 500  mouvements aléatoires et  on regarde de combien
     * la fitness varie en moyenne, et on fixe T0 pour accepter 80% de ces mouvements au début.
     */
    private double estimerTemperatureInitiale(int[][] etat) {
        List<Integer> deltas = new ArrayList<>();
        int energieDeBase = calculerFitness(etat);
        for (int k = 0; k < 500; k++) {
            int[][] voisin = genererVoisin(etat); 
            int fitnessVoisin = calculerFitness(voisin);
            deltas.add(Math.abs(fitnessVoisin - energieDeBase));
        }
        Collections.sort(deltas);
        int mMediane = deltas.get(deltas.size() / 2);
        if (mMediane == 0) mMediane = 1; 
        return Math.max(1.44 * mMediane, 10);
    }

    //------------------------------------Fitness----------------------------------------------------//
    /**
       Plus on est proche de 0 mieux c'est
      // Pénalité pour les cellules irrécupérables
     */

    public int calculerFitness(int[][] state) {
        Grille grille = new Grille(etiquetteJour, numeroJour, etiquetteMois);
        int penaliteChevauchement = 0; 
        int penaliteNonPlace = 0; 

        for (int i = 0; i < pieces.size(); i++) {
            if (state[i][0] < 0) { // Si la pièce n'est pas sur le plateau
                int taille = pieces.get(i).getOrientations().get(0).length;
                penaliteNonPlace += taille * FACTEUR_PENALITE_NON_PLACE;
                continue;
            }
            int orientIdx = state[i][0];
            int row       = state[i][1];
            int col       = state[i][2];
            int[][] cells = pieces.get(i).getOrientations().get(orientIdx);

            if (!grille.place(i, orientIdx, cells, row, col)) { // S'il y a collision
                int conflicts = compterConflits(cells, row, col, grille);
                penaliteChevauchement += conflicts;
            }
        }

        // Pénalité pour les cellules irrécupérables 
        int penaliteIlots = compterCellulesIlotsIrrecuperables(grille, state);

        return grille.computeFitness() + penaliteChevauchement + penaliteNonPlace
               + penaliteIlots * PENALITE_ILOT_IRRECUPERABLE;
    }

    private int compterConflits(int[][] cellules, int ligne, int colonne, Grille grille) {
        int conflits = 0;
        for (int[] cellule : cellules) {
            int r = ligne + cellule[0];
            int c = colonne + cellule[1];
            if (!grille.isFree(r, c)) conflits++;
        }
        return conflits;
    }

    /**
     * Une cellule libre est irrécupérable si le groupe connexe de cellules libres
     * auquel elle appartient est plus petit que la plus petite pièce restante.
     * Aucune pièce ne pourra jamais couvrir ce groupe → on le pénalise fortement.
     *
     * Algorithme : BFS depuis chaque cellule libre non visitée.
     * Si taille du groupe < taille de la plus petite pièce restante 
     * toutes les cellules de ce groupe sont comptées comme irrécupérables.
     */
    private int compterCellulesIlotsIrrecuperables(Grille grille, int[][] state) {
        int tailleMinPiece = taillePlusPetitePieceRestante(state);
        boolean[][] visite = new boolean[Grille.ROWS][Grille.COLS];
        int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
        int cellulesPenalisees = 0;

        for (int r = 0; r < Grille.ROWS; r++) {
            for (int c = 0; c < Grille.COLS; c++) {
                if (visite[r][c] || grille.getCell(r, c) != 0) continue;

                // BFS pour explorer tout le groupe connexe de cellules libres
                List<int[]> ilot = new ArrayList<>();
                Queue<int[]> file = new LinkedList<>();
                file.add(new int[]{r, c});
                visite[r][c] = true;
                while (!file.isEmpty()) {
                    int[] cell = file.poll();
                    ilot.add(cell);
                    for (int[] d : dirs) {
                        int nr = cell[0] + d[0];
                        int nc = cell[1] + d[1];
                        if (nr >= 0 && nr < Grille.ROWS && nc >= 0 && nc < Grille.COLS
                         && !visite[nr][nc] && grille.getCell(nr, nc) == 0) {
                            visite[nr][nc] = true;
                            file.add(new int[]{nr, nc});
                        }
                    }
                }

                // Groupe trop petit  on pénalise toutes ses cellules
                if (ilot.size() < tailleMinPiece) {
                    cellulesPenalisees += ilot.size();
                }
            }
        }
        return cellulesPenalisees;
    }

    // Retourne la taille de la plus petite pièce pas encore placée (minimum 4).
    private int taillePlusPetitePieceRestante(int[][] state) {
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < state.length; i++) {
            if (state[i][0] < 0) {
                int taille = pieces.get(i).getOrientations().get(0).length;
                min = Math.min(min, taille);
            }
        }
        return (min == Integer.MAX_VALUE) ? 4 : min;
    }

    //------------------Le voisinage --------------------------------------------------------//
    

    // Prend l'etat actuel et on fais une mutation----------------------------------------------------------------
    private int[][] genererVoisin(int[][] courant) {
        int[][] voisin = copieProfonde(courant);
        
        // 25% du temps  on bouge 2 pièces d'un coup qui sera utile pour s'échapper au minima local
        if (generateurAleatoire.nextDouble() < 0.25) {

            int pieceIdx1 = choisirPieceADeplacer(voisin);
            voisin[pieceIdx1] = new int[]{-1, -1, -1};
            int pieceIdx2 = choisirPieceADeplacer(voisin);
            voisin[pieceIdx2] = new int[]{-1, -1, -1};

            Grille grille1 = construireGrilleDepuisEtat(voisin);
            PlacementState ps1 = new PlacementState(pieces, generateurAleatoire);
            List<int[]> valides1 = ps1.getValidPlacements(pieceIdx1, grille1);
            if (!valides1.isEmpty()) {
                voisin[pieceIdx1] = valides1.get(generateurAleatoire.nextInt(valides1.size())).clone();
            }

            Grille grille2 = construireGrilleDepuisEtat(voisin);
            PlacementState ps2 = new PlacementState(pieces, generateurAleatoire);
            List<int[]> valides2 = ps2.getValidPlacements(pieceIdx2, grille2);
            if (!valides2.isEmpty()) {
                voisin[pieceIdx2] = valides2.get(generateurAleatoire.nextInt(valides2.size())).clone();
            }
            return voisin;
        }

        // 75% du temps   On enlève 1 pièce et on la remet ailleurs. 
        int pieceIdx = choisirPieceADeplacer(voisin);
        voisin[pieceIdx] = new int[]{-1, -1, -1};
        Grille grille = construireGrilleDepuisEtat(voisin);
        PlacementState ps = new PlacementState(pieces, generateurAleatoire);
        List<int[]> valides = ps.getValidPlacements(pieceIdx, grille);
        if (!valides.isEmpty()) {
            voisin[pieceIdx] = valides.get(generateurAleatoire.nextInt(valides.size())).clone();
        }
        return voisin;
    }
    //--------------------------------Chosir la piece a deplacer ----------------------------------------------
    private int choisirPieceADeplacer(int[][] etat) {
        List<Integer> nonPlacees = new ArrayList<>();
        for (int i = 0; i < etat.length; i++) {
            if (etat[i][0] < 0) nonPlacees.add(i);
        }
        //on force l'algorithme à privilégier à 80% les pièces 
        // qui ne sont pas encore posées, plutôt que de bouger celles déjà bien rangées.
        if (!nonPlacees.isEmpty() && generateurAleatoire.nextDouble() < 0.80) {
            return nonPlacees.get(generateurAleatoire.nextInt(nonPlacees.size()));
        }
        return generateurAleatoire.nextInt(etat.length);
    }
    
    //-----------------construire le caledrier --------------------------------------------------------------------
    private Grille construireGrilleDepuisEtat(int[][] etat) {
        Grille grille = new Grille(etiquetteJour, numeroJour, etiquetteMois);
        for (int i = 0; i < pieces.size(); i++) {
            if (etat[i][0] < 0) continue;
            int orientIdx = etat[i][0];
            int row = etat[i][1];
            int col = etat[i][2];
            int[][] cells = pieces.get(i).getOrientations().get(orientIdx);
            grille.place(i, orientIdx, cells, row, col);
        }
        return grille;
    }

    //----------------------------Les criteres pour accepter un nouveau placement ------------------------------------
    /** la logique du  Recuit Simulé !
     * retourne  true si on accepte le nouveau placement même s'il est moins bon que l'ancien, en fonction de la température courante.
     * deltaF est la différence de fitness entre le nouveau placement et l'ancien 
     */ 
    private boolean accepter(int deltaF, double tTemp) {


        // On accepte 
        if (deltaF < 0) return true; 
        
        
        // On calcule une probabilité d'accepter cette erreur.
        // Formule : e^(-DeltaF / Temperature)
         
        double prob = Math.exp(-deltaF / tTemp);


        // On accepte avec cette probabilité même si c'est moins bon pour s'échapper des minima locaux.
        
        return generateurAleatoire.nextDouble() < prob;
    }

    //-----------------------------------------------Boucle Principale la résolution ! ---------------------------------------------------//
    public Grille solve() {
        envoyerLog("Cible : " + etiquetteJour + " " + numeroJour + " " + etiquetteMois);
        totalIterations = 0;
        meilleureFitness = Integer.MAX_VALUE;

        // Préparation initiale
        int[][] etatCourant = genererEtatInitial();
        fitnessCourante = calculerFitness(etatCourant);
        meilleureFitness = fitnessCourante;
        meilleursPlacements = copieProfonde(etatCourant);
        
        envoyerLog("État initial , Fitness : " + fitnessCourante);

        // Chauffage
        temperatureCourante = estimerTemperatureInitiale(etatCourant);
        double tMin = temperatureCourante * RATIO_FIN;
        envoyerLog(String.format("T0 = %.2f. Début de l'exploration...", temperatureCourante));

        int cycle = 0;
        
        // variable pour rechauffer
        int cyclesSansAmelioration = 0;
        int ancienneMeilleureFitness = meilleureFitness;
        int nbRechauffeAutorise = 3;

        // tant que la tempeature n'atteind pas tMin  et qu'on n'a pas trouvé la solution parfaite
        while (temperatureCourante > tMin && !estResolu) {
            
            // On fait X tentatives avec cette même température
            for (int m = 0; m < NB_ITERATIONS; m++) {
                totalIterations++;

                // On invente une variante du plateau
                int[][] voisin = genererVoisin(etatCourant);
                int fitnessVoisin = calculerFitness(voisin);
                
                // On calcule la différence de fitness
                int diff_F = fitnessVoisin - fitnessCourante;

                // Est-ce qu'on l'accepte selon Metropolis ?
                if (accepter(diff_F, temperatureCourante)) {
                    etatCourant = voisin;
                    fitnessCourante = fitnessVoisin;

                    // On a battu le record historique !
                    if (fitnessCourante < meilleureFitness) {
                        envoyerLog(String.format("Amélioration ! Fitness : %d (T=%.2f)", fitnessCourante, temperatureCourante));
                        meilleureFitness = fitnessCourante;
                        meilleursPlacements = copieProfonde(etatCourant);

                        if (meilleureFitness == 0) { //on arrête
                            estResolu = true;
                            break;
                        }
                    }
                }

                // Animation visuelle en direct
                if (totalIterations % 100 == 0) {
                    if (ecouteur != null) {
                        ecouteur.enProgression(fitnessCourante, temperatureCourante, totalIterations, copieProfonde(etatCourant));
                    }
                    try { Thread.sleep(2); } catch(InterruptedException ignored) {}
                }
            }

            // ---on verifie si ca stagne 
            if (meilleureFitness == ancienneMeilleureFitness) {
                cyclesSansAmelioration++;
            } else {
                cyclesSansAmelioration = 0;
                ancienneMeilleureFitness = meilleureFitness;
            }

            // Si on n'a rien trouvé de mieux pendant 
            if (cyclesSansAmelioration >= 300 && nbRechauffeAutorise > 0) {
                envoyerLog("on rechauffe");
                temperatureCourante *= 5.0; // On remonte la température 
                cyclesSansAmelioration = 0; // On remet le compteur à zéro
                nbRechauffeAutorise--; 
            }

            if (cycle % frequenceJournalcycle == 0 && !estResolu && cycle > 0) {
                envoyerLog(String.format("cycle %d passé (T=%.3f)", cycle, temperatureCourante));
            }

            // À la fin du cycle, on fait baisser la température 
            temperatureCourante *= TAUX_REFROIDISSEMENT;
            cycle++;
        }

        // Fin de l'algorithme, on renvoie la meilleure grille construite
        if (ecouteur != null) {
            ecouteur.enProgression(meilleureFitness, temperatureCourante, totalIterations, copieProfonde(meilleursPlacements));
        }

        return construireGrilleDepuisEtat(meilleursPlacements);
    }
    
    // Getters --------------------------------------------------------------------------------------------
    public boolean isSolved() { return estResolu; }

    public int getBestFitness() { return meilleureFitness; }

    public long getTotalIterations() { return totalIterations; }

    public double getTemperature() { return temperatureCourante; }

    public int[][] getBestPlacements() { return meilleursPlacements; }
    
    public List<Polyomino> getPieces() { return pieces; }
    
    private int[][] copieProfonde(int[][] src) {
        int[][] copy = new int[src.length][];
        for (int i = 0; i < src.length; i++) {
            copy[i] = src[i].clone();
        }
        return copy;
    }
}