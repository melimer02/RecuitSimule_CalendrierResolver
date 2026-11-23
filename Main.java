import processing.core.PApplet;
import java.util.*;
import processing.event.MouseEvent;
/**
 * classe principale -----------------------------------------------------------------------------------//
*/


public class Main extends PApplet {

    // constante graphique
    static final int CELL_SIZE = 64; 
    static final int GRID_X    = 30; 
    static final int GRID_Y    = 110;
    static final int ROWS      = Grille.ROWS;
    static final int COLS      = Grille.COLS;
    
    // variable de journaling 
    public volatile long startTime = 0;
    public volatile long endTime = 0;
    public volatile List<String> journalLogs = new ArrayList<>();
    public volatile int logScrollOffset = 0; 
    // label pour la navigation
    public final String[] DAY_LABELS    = {"lun.","mar.","mer.","jeu.","ven.","sam.","dim."};
    public final String[] NUMBER_LABELS = {"1","2","3","4","5","6","7","8","9","10",
        "11","12","13","14","15","16","17","18","19","20",
        "21","22","23","24","25","26","27","28","29","30","31"};
    public final String[] MONTH_LABELS  = {"janv.","févr.","mars","avr.","mai","juin",
        "juil.","août","sept.","oct.","nov.","déc."};


    // Palette de couleurs pour les 10 pièces
    public final int[] PIECE_COLORS = new int[]{
        color(255, 100, 100), color(100, 180, 255), color(100, 220, 100), 
        color(255, 200, 80),  color(200, 130, 255), color(255, 160, 50), 
        color(80, 220, 200),  color(255, 120, 180), color(160, 200, 80), color(120, 160, 230)
    };

    //varibale de selection de date
    public int dayIdx = 5; 
    public int numIdx = 26;
    public int monIdx = 11;


    public String selDay()   { return DAY_LABELS[dayIdx]; }
    public String selNum()   { return NUMBER_LABELS[numIdx]; }
    public String selMonth() { return MONTH_LABELS[monIdx]; }

    // varibale de l'etat de l'algo
    public volatile int[][]  gridState  = new int[ROWS][COLS]; 
    public volatile int      bestFitness = -1;
    public volatile double   currentT   = 0;
    public volatile long     iterations = 0;
    public volatile boolean  isSolved   = false;
    public volatile boolean  isRunning  = false; 

    private List<Polyomino> pieces;
    private final ArrayList<Integer> energyHistory = new ArrayList<>();
    
    // thread qui  à l'algorithme de tourner sans bloquer  l'interface 
    private Thread saThread; 
    private Vue renderer;
    
    //--------------------Dessin du calendrier  ---------------------------------------//
    @Override
    public void settings() {
        size(790, 800); 
        smooth(); 
    }

    @Override
    public void setup() {
        surface.setTitle("Calendrier & Polyominos — Recuit Simulé");
        textFont(createFont("SansSerif", 12, true));
        pieces = Polyomino.createAllPieces();

        // Récupération automatique de la date d'aujourd'hui
        Calendar aujourdHui = Calendar.getInstance();
        monIdx = aujourdHui.get(Calendar.MONTH);
        numIdx = aujourdHui.get(Calendar.DAY_OF_MONTH) - 1;
        int jourSemaineJava = aujourdHui.get(Calendar.DAY_OF_WEEK);
        dayIdx = (jourSemaineJava == Calendar.SUNDAY) ? 6 : jourSemaineJava - Calendar.MONDAY; 

        renderer = new Vue(this);
        clearGrid();
    }

    @Override
    public void draw() {
        renderer.draw();
    }

    //-------------------------Interaction User  ---------------------------------------------------//
    @Override
    public void keyPressed() {
        if (key == ' ') Resoudre();
        if (key == 'r' || key == 'R') resetAll();
    }

    @Override
   public void mousePressed() {
        int rightX = GRID_X + COLS * CELL_SIZE + 30;
        int py = GRID_Y + 22; 
        
        checkSel(rightX, py, 0);       // Clic sur Jour
        py += 52;
        checkSel(rightX, py, 1);       // Clic sur Numéro
        py += 52;
        checkSel(rightX, py, 2);       // Clic sur Mois

        // --- 2. COORDONNÉES DES BOUTONS (En bas du calendrier) ---
        int bottomY = GRID_Y + ROWS * CELL_SIZE + 20;
        
        if (over(GRID_X, bottomY, 210, 40)) Resoudre();              // Clic sur Résoudre
        if (over(GRID_X + 230, bottomY, 210, 40)) resetAll();        // Clic sur Restart
    }

    @Override
    public void mouseWheel(MouseEvent event) {
        float e = event.getCount();
        logScrollOffset += (int) (e * 3); 
        
        
        int maxVisible = 20; 
        int maxScroll = Math.max(0, journalLogs.size() - maxVisible);
        logScrollOffset = Math.max(0, Math.min(logScrollOffset, maxScroll));
    }

    private void checkSel(int px, int py, int type) {
          int hitSize = 40;
        int offsetX = -7; 
        int offsetY = 13 - 7;
        
        if (over(px + offsetX, py + offsetY, hitSize, hitSize)) changeSelector(type, -1);
        if (over(px + 158 + offsetX, py + offsetY, hitSize, hitSize)) changeSelector(type, +1);
    }

    //--Permet de naviguer dans les listes de la date 
    private void changeSelector(int type, int delta) {
        if (type == 0) dayIdx = (dayIdx + delta + DAY_LABELS.length) % DAY_LABELS.length;
        if (type == 1) numIdx = (numIdx + delta + NUMBER_LABELS.length) % NUMBER_LABELS.length;
        if (type == 2) monIdx = (monIdx + delta + MONTH_LABELS.length) % MONTH_LABELS.length;
        clearGrid();
        resetMetrics();
    }
    

    //Vérifie si la souris survole une zone rectangulaire précise 
    private boolean over(int x, int y, int w, int h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }





  //-------------------------------------------Lancement de l'algorithme RS ------------------------------------/
    private void Resoudre() {
        if (isRunning) return; 
        
        isRunning = true;
        isSolved  = false;
        bestFitness = -1;
        energyHistory.clear();
        clearGrid();

        final String day   = selDay();
        final String num   = selNum();
        final String month = selMonth();
        
        // Démarrage du chronomètre et nettoyage du terminal
        startTime = System.currentTimeMillis();
        endTime = 0;
        journalLogs.clear();
        addLog("Démarrage du recuit simulé...");

        // On lance l'algorithme dans un Thread séparé pour laisser Processing dessiner
        saThread = new Thread(() -> {
            try {
                RecuitSimule sa = new RecuitSimule(day, num, month, -1L);
                
                // Connexion entre l'algorithme qui calcule et Main qui affiche
                sa.setEcouteur(new RecuitSimule.EcouteurProgression() {
                    @Override
                    public void enProgression(int energy, double temp, long iter, int[][] placements) {
                        bestFitness = energy;
                        currentT   = temp;
                        iterations = iter;
                        energyHistory.add(energy);
                        updateGridFromPlacements(placements, day, num, month); // Met à jour l'image visuelle
                    }

                    @Override
                    public void nouveauLog(String message) {
                        addLog(message); 
                    }
                });

                // --- Lancement effectif de la boucle mathématique ---
                Grille result = sa.solve();
                
                // Récupération des résultats finaux
                bestFitness = sa.getBestFitness();
                isSolved   = sa.isSolved();
                updateGridFromPlacements(sa.getBestPlacements(), day, num, month);
                
                endTime = System.currentTimeMillis(); // Arrêt du chrono
                
                if (isSolved) {
                    addLog("Solution parfaite trouvée. ! ");
                } else {
                    addLog("Terminé. Solution partielle trouvée.");
                }

            } catch (Exception e) {
                System.err.println("[Main] Erreur dans le Thread  : " + e.getMessage());
                e.printStackTrace();
            } finally {
                isRunning = false; 
            }
        });
        
        saThread.setDaemon(true); 
        saThread.start();
    }

//-------------------Utilitaire ----------------------------------------------------//
    /**
     * Transforme les placements mathématiques du Recuit Simulé en une carte 2D  
     * */

    public synchronized void updateGridFromPlacements(int[][] placements, String day, String num, String month) {
        Grille g = new Grille(day, num, month);
        for (int i = 0; i < pieces.size(); i++) {
            if (placements[i][0] < 0) continue; // Pièce non placée
            int oi  = placements[i][0];
            int row = placements[i][1];
            int col = placements[i][2];
            int[][] cells = pieces.get(i).getOrientations().get(oi);
            g.place(i, oi, cells, row, col);
        }
        
        // Copie dans la variable globale lue par l'interface
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                gridState[r][c] = g.getCell(r, c);
    }

    private void clearGrid() {
        for (int[] row : gridState) Arrays.fill(row, 0);
    }

    private void resetAll() {
        if (saThread != null) saThread.interrupt(); 
        isRunning = false;
        isSolved  = false;
        clearGrid();
        resetMetrics();
        startTime = 0;
        endTime = 0;
        journalLogs.clear();
    }

    private void resetMetrics() {
        bestFitness = -1;
        currentT   = 0;
        iterations = 0;
        energyHistory.clear();
    }

    /** Ajoute un message au terminal.
      */
    public synchronized void addLog(String msg) {
        journalLogs.add(msg);
        if (journalLogs.size() > 100) { 
            journalLogs.remove(0); 
        }
        if (isRunning) {
            int maxVisible = 20;
            logScrollOffset = Math.max(0, journalLogs.size() - maxVisible);
        }
    
    } 

    public List<Polyomino> getPieces() { return pieces; }

    public static void main(String[] args) {
        PApplet.main("Main");
    }
}