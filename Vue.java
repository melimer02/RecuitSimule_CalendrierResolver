/* Classe de l'interface du solveur avec processing  */
import processing.core.PApplet;
import java.util.List;
import java.util.ArrayList;

public class Vue {
    private final Main p;

    public Vue(Main p) {
        this.p = p;
    }

    public void draw() {
        p.background(245); 
        drawGrid();
        drawRightColumn(); // Regroupe Dates + Pièces + Terminal
        drawBottomPanel(); // Regroupe Boutons + Statistiques
    }

    private void drawGrid() {
        p.fill(40);
        p.textSize(17);
        p.textAlign(Main.LEFT, Main.CENTER);
        p.text("Calendrier — " + p.selDay() + " " + p.selNum() + " " + p.selMonth(),
             Main.GRID_X, Main.GRID_Y - 40);

        p.fill(255); 
        p.stroke(190); 
        p.strokeWeight(1);
        p.rect(Main.GRID_X - 4, Main.GRID_Y - 4,
             Main.COLS * Main.CELL_SIZE + 8, Main.ROWS * Main.CELL_SIZE + 8, 6);

        for (int r = 0; r < Main.ROWS; r++) {
            for (int c = 0; c < Main.COLS; c++) {
                drawCell(r, c);
            }
        }
    }

    private void drawCell(int r, int c) {
        if (!Grille.VALID_CELLS[r][c]) return;

        int x = Main.GRID_X + c * Main.CELL_SIZE;
        int y = Main.GRID_Y + r * Main.CELL_SIZE;
        String label = Grille.LABELS[r][c];

        boolean isTarget = label.equals(p.selDay())
                        || label.equals(p.selNum())
                        || label.equals(p.selMonth());

        int bg;
        if (isTarget) {
            bg = p.color(255, 255, 255);
        } else {
            int val = p.gridState[r][c];
            bg = (val > 0) ? p.PIECE_COLORS[(val - 1) % p.PIECE_COLORS.length] : p.color(238);
        }

        p.strokeWeight(1); 
        p.stroke(180);
        p.fill(bg);
        p.rect(x, y, Main.CELL_SIZE, Main.CELL_SIZE, 2);

        p.fill(isTarget ? p.color(160, 90, 0) : p.color(50));
        p.textSize(11);
        p.textAlign(Main.CENTER, Main.CENTER);
        p.text(label, x + Main.CELL_SIZE / 2f, y + Main.CELL_SIZE / 2f);

        int id = p.gridState[r][c];
        if (id > 0) {
            p.stroke(60); 
            p.strokeWeight(2.5f);
            if (r == 0 || p.gridState[r-1][c] != id)
                p.line(x, y, x + Main.CELL_SIZE, y);
            if (r == Main.ROWS - 1 || !Grille.VALID_CELLS[r+1][c] || p.gridState[r+1][c] != id)
                p.line(x, y + Main.CELL_SIZE, x + Main.CELL_SIZE, y + Main.CELL_SIZE);
            if (c == 0 || p.gridState[r][c-1] != id)
                p.line(x, y, x, y + Main.CELL_SIZE);
            if (c == Main.COLS - 1 || p.gridState[r][c+1] != id)
                p.line(x + Main.CELL_SIZE, y, x + Main.CELL_SIZE, y + Main.CELL_SIZE);
        }
    }

    // =========================================================
    // COLONNE DE DROITE (Menu + Terminal)
    // =========================================================
    private void drawRightColumn() {
        int px = Main.GRID_X + Main.COLS * Main.CELL_SIZE + 30; // 508
        int py = Main.GRID_Y;

        // 1. MENU DATE
        p.fill(40); p.textSize(13); p.textAlign(Main.LEFT, Main.TOP);
        p.text("Choisir la date", px, py);
        py += 22;
        drawSelector("Jour", p.DAY_LABELS, p.dayIdx, px, py);
        py += 52;
        drawSelector("Numéro", p.NUMBER_LABELS, p.numIdx, px, py);
        py += 52;
        drawSelector("Mois", p.MONTH_LABELS, p.monIdx, px, py);

        // 2. LISTE DES PIÈCES
        py += 70;
        p.fill(50); p.textSize(11); p.textAlign(Main.LEFT, Main.TOP);
        p.text("Pièces disponibles :", px, py);
        py += 16;
        List<Polyomino> pieces = p.getPieces();
        for (int i = 0; i < pieces.size(); i++) {
            p.fill(p.PIECE_COLORS[i % p.PIECE_COLORS.length]);
            p.noStroke(); p.rect(px, py + i * 14, 10, 10, 2);
            p.fill(50); p.textAlign(Main.LEFT, Main.TOP);
            p.text(pieces.get(i).getName(), px + 15, py + i * 14 - 2);
        }

        // 3. LE TERMINAL SCROLLABLE
        py += 160; 
        
        int terminalHeight = 310;
        int terminalWidth = 260;
        
        p.fill(20, 20, 25);
        p.stroke(100);
        p.strokeWeight(1);
        p.rect(px, py, terminalWidth, terminalHeight, 5);

        // Titre terminal
        p.fill(150); p.textSize(10);
        p.text("Terminal de l'IA (Molette pour Scroll)", px + 10, py + 5);
        p.stroke(50);
        p.line(px, py + 18, px + terminalWidth, py + 18);

        // Affichage des logs avec la variable d'Offset
        p.fill(74, 246, 38); p.textSize(11);
        
        int startY = py + 25;
        int maxLignesVisibles = 20; // Nb max de lignes dans la boite noire
        
        if (p.journalLogs != null && !p.journalLogs.isEmpty()) {
            List<String> logsSnapshot = new ArrayList<>(p.journalLogs);
            
            int offset = Math.min(p.logScrollOffset, Math.max(0, logsSnapshot.size() - maxLignesVisibles));
            int fin = Math.min(logsSnapshot.size(), offset + maxLignesVisibles);
            
            for (int i = offset; i < fin; i++) {
                p.text("> " + logsSnapshot.get(i), px + 10, startY + ((i - offset) * 14));
            }
            
            // Dessin de la Scrollbar (Ascenseur)
            if (logsSnapshot.size() > maxLignesVisibles) {
                p.fill(50); p.noStroke();
                p.rect(px + terminalWidth - 8, py + 19, 6, terminalHeight - 21, 3); // Fond ascenseur
                
                float proportionVisible = (float) maxLignesVisibles / logsSnapshot.size();
                float handleHeight = Math.max(20, proportionVisible * (terminalHeight - 21));
                
                float positionY = PApplet.map(offset, 0, logsSnapshot.size() - maxLignesVisibles, 
                                              py + 19, py + terminalHeight - 2 - handleHeight);
                
                p.fill(120);
                p.rect(px + terminalWidth - 8, positionY, 6, handleHeight, 3); // Poignée
            }
        }
    }

    // =========================================================
    // ZONE DU BAS (Boutons + Stats)
    // =========================================================
    private void drawBottomPanel() {
        int px = Main.GRID_X;
        int py = Main.GRID_Y + Main.ROWS * Main.CELL_SIZE + 20;

        // 1. LES BOUTONS
        int btnColor = p.isRunning ? p.color(190, 70, 70) : p.color(70, 155, 70);
        drawButton(p.isRunning ? "⏹  EN COURS..." : "▶  RÉSOUDRE  [ESPACE]", px, py, 210, 40, btnColor);
        drawButton("↺  RESTART  [R]", px + 230, py, 210, 40, p.color(100, 125, 200));

        // 2. LES STATISTIQUES
        py += 55;
        
        long t = 0;
        if (p.isRunning) t = System.currentTimeMillis() - p.startTime;
        else if (p.endTime > 0) t = p.endTime - p.startTime;
        long sec = t / 1000;
        long ms = t % 1000;

        p.fill(40); p.textSize(13); p.textAlign(Main.LEFT, Main.TOP);
        p.text("Performances :", px, py);
        
        py += 20;
        p.fill(60); p.textSize(12);
        
        // Affichage du statut global
        if (p.isRunning) {
            p.fill(200, 60, 60); p.text("● Moteur en marche...", px + 100, py - 20);
        } else if (p.isSolved) {
            p.fill(30, 150, 30); p.text("✓ Succès", px + 100, py - 20);
        } else {
            p.fill(130); p.text("En attente", px + 100, py - 20);
        }

        // Tableau de bord des stats
        p.fill(50);
        p.text(String.format("► Fitness : %d", Math.max(0, p.bestFitness)), px, py);
        p.text(String.format("► Température : %.5f", p.currentT), px + 150, py);
        
        py += 20;
        p.text(String.format("► Itérations : %,d", p.iterations), px, py);
        p.text(String.format("► Temps : %d.%03d s", sec, ms), px + 150, py);
    }

    private void drawSelector(String label, String[] values, int idx, int px, int py) {
        p.fill(80); p.textSize(10); p.textAlign(Main.LEFT, Main.TOP); p.text(label + " :", px, py);
        drawButton("<", px, py + 13, 26, 26, p.color(170));
        p.fill(30); p.textSize(13); p.textAlign(Main.CENTER, Main.CENTER); p.text(values[idx], px + 95, py + 26);
        drawButton(">", px + 158, py + 13, 26, 26, p.color(170));
    }

    private void drawButton(String label, float x, float y, float w, float h, int bg) {
        p.strokeWeight(1); p.stroke(p.lerpColor(bg, p.color(0), 0.25f));
        p.fill(bg); p.rect(x, y, w, h, 5);
        p.fill(255); p.textSize(11); p.textAlign(Main.CENTER, Main.CENTER);
        p.text(label, x + w / 2f, y + h / 2f);
    }
}