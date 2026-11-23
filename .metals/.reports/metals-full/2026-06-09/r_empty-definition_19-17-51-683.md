error id: file://<WORKSPACE>/Vue.java:_empty_/Main#CELL_SIZE.
file://<WORKSPACE>/Vue.java
empty definition using pc, found symbol in pc: _empty_/Main#CELL_SIZE.
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 1686
uri: file://<WORKSPACE>/Vue.java
text:
```scala
import java.util.List;

public class Vue {
    private final Main p;

    public Vue(Main p) {
        this.p = p;
    }

    public void draw() {
        p.background(245); // <-- Ici on utilise bien p.background() et non background() tout court
        drawGrid();
        drawControlPanel();
        drawMetricsAndJournal(); 
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
 label.equals(p.selNum())
 label.equals(p.selMonth());

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
        p.rect(x, y, Main.@@CELL_SIZE, Main.CELL_SIZE, 2);

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

    private void drawControlPanel() {
        int px = Main.GRID_X + Main.COLS * Main.CELL_SIZE + 22;
        int py = Main.GRID_Y;

        p.fill(40); 
        p.textSize(13); 
        p.textAlign(Main.LEFT, Main.TOP);
        p.text("Choisir la date", px, py);

        py += 22;
        drawSelector("Jour", p.DAY_LABELS, p.dayIdx, px, py);
        py += 52;
        drawSelector("Numéro", p.NUMBER_LABELS, p.numIdx, px, py);
        py += 52;
        drawSelector("Mois", p.MONTH_LABELS, p.monIdx, px, py);

        py += 72;
        int btnColor = p.isRunning ? p.color(190, 70, 70) : p.color(70, 155, 70);
        drawButton(p.isRunning ? "⏹  EN COURS..." : "▶  LANCER  [ESPACE]", px, py, 225, 42, btnColor);

        py += 52;
        drawButton("↺  RÉINITIALISER  [R]", px, py, 225, 36, p.color(100, 125, 200));

        py += 55;
        p.fill(50); p.textSize(11); p.textAlign(Main.LEFT, Main.TOP);
        p.text("Statut :", px, py);
        py += 16;
        if (p.isRunning) {
            p.fill(200, 60, 60); p.text("● Recuit en cours...", px, py);
        } else if (p.isSolved) {
            p.fill(30, 150, 30); p.text("✓ Solution trouvée !", px, py);
        } else if (p.bestEnergy >= 0) {
            p.fill(170, 100, 0); p.text("⚠ Partielle  (E = " + p.bestEnergy + ")", px, py);
        } else {
            p.fill(130); p.text("En attente", px, py);
        }

        py += 40;
        p.fill(50); p.textSize(11); p.textAlign(Main.LEFT, Main.TOP);
        p.text("Pièces :", px, py);
        py += 16;
        
        List<Polyomino> pieces = p.getPieces();
        for (int i = 0; i < pieces.size(); i++) {
            p.fill(p.PIECE_COLORS[i % p.PIECE_COLORS.length]);
            p.noStroke(); 
            p.rect(px, py + i * 18, 14, 14, 2);
            p.fill(50); 
            p.textAlign(Main.LEFT, Main.TOP);
            p.text(pieces.get(i).getName(), px + 20, py + i * 18);
        }
    }

    private void drawSelector(String label, String[] values, int idx, int px, int py) {
        p.fill(80); 
        p.textSize(10); 
        p.textAlign(Main.LEFT, Main.TOP);
        p.text(label + " :", px, py);
        drawButton("<", px, py + 13, 26, 26, p.color(170));
        p.fill(30); 
        p.textSize(13); 
        p.textAlign(Main.CENTER, Main.CENTER);
        p.text(values[idx], px + 95, py + 26);
        drawButton(">", px + 158, py + 13, 26, 26, p.color(170));
    }

    private void drawButton(String label, int x, int y, int w, int h, int bg) {
        p.strokeWeight(1);
        p.stroke(p.lerpColor(bg, p.color(0), 0.25f));
        p.fill(bg);
        p.rect(x, y, w, h, 5);
        p.fill(255); 
        p.textSize(11); 
        p.textAlign(Main.CENTER, Main.CENTER);
        p.text(label, x + w / 2f, y + h / 2f);
    }

    private void drawMetricsAndJournal() {
        int mx = Main.GRID_X;
        int my = Main.GRID_Y + Main.ROWS * Main.CELL_SIZE + 15;

        // 1. Calcul du temps écoulé
        long t = 0;
        if (p.isRunning) {
            t = System.currentTimeMillis() - p.startTime;
        } else if (p.endTime > 0) {
            t = p.endTime - p.startTime;
        }
        long sec = t / 1000;
        long ms = t % 1000;

        // 2. Affichage des métriques
        p.fill(50); 
        p.textSize(12); 
        p.textAlign(Main.LEFT, Main.TOP);
        p.text(String.format("Énergie: %d   |   Température: %.5f   |   Itérations: %d   |   Temps: %d.%03d s",
             Math.max(0, p.bestEnergy), p.currentT, p.iterations, sec, ms), mx, my);

        // 3. Dessin de la "Console / Journal"
        int journalY = my + 25;
        int journalHeight = 110;
        
        p.fill(20, 20, 25);
        p.stroke(100);
        p.strokeWeight(1);
        p.rect(mx, journalY, Main.COLS * Main.CELL_SIZE + 4, journalHeight, 5);

        p.fill(150);
        p.textSize(10);
        p.text("Terminal de Recuit Simulé", mx + 10, journalY + 5);
        p.stroke(50);
        p.line(mx, journalY + 18, mx + Main.COLS * Main.CELL_SIZE + 4, journalY + 18);

        p.fill(74, 246, 38); 
        p.textSize(11);
        
        if (p.journalLogs != null) {
            int startY = journalY + 25;
            for (int i = 0; i < p.journalLogs.size(); i++) {
                p.text("> " + p.journalLogs.get(i), mx + 10, startY + (i * 14));
            }
        }
    }
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/Main#CELL_SIZE.