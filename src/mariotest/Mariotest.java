package mariotest;

import GameGFX.*;
import Objetos.Utilidad.BarrilSpawner;
import Mapa.NivelesHandler;
import Mapa.TiledTMXParser;
import Objetos.*;
import Objetos.Utilidad.*;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferStrategy;

/**
 * 
 * 
 * @author LENOVO
 */
public class Mariotest extends Canvas implements Runnable {
    
    // CONSTANTES DEL JUEGO 
    private static final int MILLIS_PER_SEC = 1000;
    private static final int NANOS_PER_SEC = 1000000000;
    private static final double NUM_TICKS = 60.0;  // 60 FPS
    
    // Configuración de ventana
    private static final String NOMBRE_JUEGO = "Diego Kong";
    private static final int VENTANA_WIDTH = 920;
    private static final int VENTANA_HEIGHT = 760;
    
    // COMPONENTES DEL JUEGO 
    private Thread thread;
    private Handler handler;
    private NivelesHandler nivelHandler;
    private BarrilSpawner barrelSpawner;
    private DiegoKong kong;
    private static Texturas textura;
    
    // Estado del juego
    public boolean running;
    private boolean debug = false;
    
    // Estadísticas
    private int fps = 0;
    private int tps = 0;
    
    /**
     * Constructor del juego
     */
    public Mariotest() {
        initialize();
        PlataformaMovil plataformaVertical = new PlataformaMovil(
    60,                                      // X inicial
    80,                                      // Y inicial
    8,                                       // Ancho
    8,                                        // Alto
    3,                                        // Scale
    1,                                        // TileID
    PlataformaMovil.TipoMovimiento.VERTICAL,  // ⬅️ TIPO VERTICAL
    1.5f,                                     // Velocidad (positiva = empieza bajando)
    190,                                      // Límite SUPERIOR (Y mínimo)
    670,                                      // Límite INFERIOR (Y máximo)
    900,                                      // Visible por 180 ticks (3 segundos)
    120                                       // Invisible por 120 ticks (2 segundos)
);
handler.addObj(plataformaVertical);
        PlataformaMovil plataformaVertical2 = new PlataformaMovil(
    124,                                      // X inicial
    80,                                      // Y inicial
    8,                                       // Ancho
    8,                                        // Alto
    3,                                        // Scale
    1,                                        // TileID
    PlataformaMovil.TipoMovimiento.VERTICAL,  // ⬅️ TIPO VERTICAL
    -1.5f,                                     // Velocidad (positiva = empieza bajando)
    190,                                      // Límite SUPERIOR (Y mínimo)
    670,                                      // Límite INFERIOR (Y máximo)
    980,                                      // Visible por 180 ticks (3 segundos)
    120                                       // Invisible por 120 ticks (2 segundos)
);
handler.addObj(plataformaVertical2);
    }
    
    /**
     * Punto de entrada del programa
     */
    public static void main(String[] args) {
        new Mariotest();
    }
    
    /**
     * Inicializa todos los componentes del juego
     */
    private void initialize() {
        System.out.println("\n[INIT] Cargando texturas...");
        textura = new Texturas();
        
        System.out.println("[INIT] Inicializando handler...");
        handler = new Handler();
        
        System.out.println("[INIT] Configurando controles...");
        this.addKeyListener(new Teclas(handler));
        
        System.out.println("[INIT] Creando jugador...");
        // Crear jugador en posición temporal (el mapa lo reposicionará)
        handler.setPlayer(new Player(100, 100, 2, handler));
        
        System.out.println("[INIT] Cargando mapa desde PNG...");
        //nivelHandler = new NivelesHandler(handler);
        //System.out.println("[INFO] " + nivelHandler.getInfoMapa());
        
        TiledTMXParser tmxParser = new TiledTMXParser(handler);
        tmxParser.cargarMapa("/Imagenes/test1.tmx");
        
        System.out.println("[INIT] Configurando sistema de barriles...");
        barrelSpawner = new BarrilSpawner(handler, tmxParser.getBarrilSpawns());
        barrelSpawner.agregarSpawnPoint(new java.awt.Point(90, 90));
        
        System.out.println("[INIT] Creando Diego Kong manualmente...");
        DiegoKong dk = new DiegoKong(90, 40, 2, handler);
        handler.addObj(dk);
        
        Princesa pric = new Princesa(200, 6, 2, handler);
        handler.addObj(pric);

        // Activar spawner después de 3 segundos (180 ticks)
         barrelSpawner.activar(); // Descomenta cuando tengas la clase Barril
        
        System.out.println("[INIT] Creando ventana...");
        new Ventana(VENTANA_WIDTH, VENTANA_HEIGHT, NOMBRE_JUEGO, this);
        
        System.out.println("[INIT] Iniciando game loop...");
        start();
        
        System.out.println("\n========================================");
        System.out.println("    JUEGO INICIADO CORRECTAMENTE");
        System.out.println("========================================");
        System.out.println("Controles:");
        System.out.println("  ESPACIO - Saltar");
        System.out.println("  W - Subir escalera");
        System.out.println("  S - Bajar escalera");
        System.out.println("  A - Mover izquierda");
        System.out.println("  D - Mover derecha");
        System.out.println("  ESC - Salir");
        System.out.println("  F3 - Toggle debug");
        System.out.println("========================================\n");
    }
    
    /**
     * Inicia el thread del juego
     */
    private synchronized void start() {
        thread = new Thread(this);
        thread.start();
        running = true;
    }
    
    /**
     * Detiene el thread del juego
     */
    private synchronized void stop() {
        try {
            thread.join();
            running = false;
            System.out.println("\n[STOP] Juego detenido correctamente");
        } catch (InterruptedException e) {
            System.err.println("[ERROR] Error deteniendo el juego:");
            e.printStackTrace();
        }
    }
    
    /**
     * Game loop principal
     * Usa un sistema de fixed timestep para mantener 60 TPS constantes
     */
    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double amountOfTicks = NUM_TICKS;
        double ns = NANOS_PER_SEC / amountOfTicks;
        double delta = 0;
        long timer = System.currentTimeMillis();
        int frames = 0;
        int updates = 0;
        
        // Dar foco a la ventana para capturar teclas
        this.requestFocus();
        
        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;
            
            // Actualizar lógica del juego a 60 TPS
            while (delta >= 1) {
                tick();
                updates++;
                delta--;
            }
            
            // Renderizar (ilimitado, V-Sync controlará)
            if (running) {
                render();
                frames++;
            }
            
            // Actualizar contador de FPS cada segundo
            if (System.currentTimeMillis() - timer > MILLIS_PER_SEC) {
                timer += MILLIS_PER_SEC;
                fps = frames;
                tps = updates;
                
                // Mostrar stats cada 5 segundos
                if (debug) {
                    System.out.println(String.format(
                        "[STATS] FPS: %d | TPS: %d | Objetos: %d | Player: (%.0f, %.0f)",
                        fps, tps, 
                        handler.getGameObjs().size(),
                        handler.getPlayer().getX(),
                        handler.getPlayer().getY()
                    ));
                }
                
                updates = 0;
                frames = 0;
            }
        }
        
        stop();
    }
    
    /**
     * Actualiza la lógica del juego
     * Llamado 60 veces por segundo
     */
    private void tick() {
        // Actualizar todos los objetos del juego
        handler.tick();
        
        // Actualizar spawner de barriles
        if (barrelSpawner != null) {
            barrelSpawner.tick();
        }
        
        // Verificar límites del mapa
        //verificarLimitesJugador();
    }
    
    /**
     * Verifica que el jugador no salga de los límites del mapa
     */
    /*
private void verificarLimitesJugador() {
        Player player = handler.getPlayer();
        if (player == null) return;
        
        // Usar csvParser en lugar de nivelHandler
        int mapaAncho = csvParser.getMapaAnchoPixels();
        int mapaAlto = csvParser.getMapaAltoPixels();
        
        // Límite izquierdo
        if (player.getX() < 0) {
            player.setX(0);
        }
        
        // Límite derecho
        if (player.getX() + player.getWidth() > mapaAncho) {
            player.setX(mapaAncho - player.getWidth());
        }
        
        // Límite superior
        if (player.getY() < 0) {
            player.setY(0);
        }
        
        // Límite inferior (muerte por caída)
        if (player.getY() > mapaAlto + 100) {
            respawnJugador();
        }
    }
    */
    /**
     * Respawnea al jugador en la posición inicial
     */
    private void respawnJugador() {
        Player player = handler.getPlayer();
        if (player != null) {
            // Volver a cargar posición del mapa
            player.setX(100);
            player.setY(100);
            player.setVelX(0);
            player.setVely(0);
            player.setSalto(false);
            
            System.out.println("[RESPAWN] Jugador reposicionado");
        }
    }
    
    /**
     * Renderiza los gráficos del juego
     */
    private void render() {
        BufferStrategy buffer = this.getBufferStrategy();
        
        // Crear buffer strategy si no existe
        if (buffer == null) {
            this.createBufferStrategy(3);  // Triple buffering
            return;
        }
        
        Graphics g = buffer.getDrawGraphics();
        Graphics2D g2d = (Graphics2D) g;
        
        // Activar antialiasing para mejor calidad (opcional)
        // g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Limpiar pantalla con color de fondo
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, VENTANA_WIDTH, VENTANA_HEIGHT);
        
        // Renderizar todos los objetos del juego
        handler.render(g);
        
        // Renderizar HUD y debug info
        if (debug) {
            renderDebugInfo(g);
        }
        
        // Liberar recursos y mostrar
        g.dispose();
        buffer.show();
    }
    
    /**
     * Renderiza información de debug en pantalla
     */
    private void renderDebugInfo(Graphics g) {
         g.setColor(Color.GREEN);
        g.drawString("FPS: " + fps + " | TPS: " + tps, 10, 20);
        g.drawString("Objetos: " + handler.getGameObjs().size(), 10, 35);
        
        Player player = handler.getPlayer();
        if (player != null) {
            g.drawString(String.format("Pos: (%.0f, %.0f)", player.getX(), player.getY()), 10, 50);
            g.drawString(String.format("Vel: (%.1f, %.1f)", player.getVelX(), player.getVely()), 10, 65);
            g.drawString("Salto: " + player.hasSalto(), 10, 80);
            g.drawString("En Escalera: " + player.isEnEscalera(), 10, 95);
            g.drawString("Puede Subir: " + player.isPuedeMoverseEnEscalera(), 10, 110);
        }
        
        if (barrelSpawner != null) {
            g.drawString(barrelSpawner.getInfo(), 10, 125);
        }
        
        // Usar csvParser en lugar de nivelHandler
        /*
        if (csvParser != null) {
            g.drawString(csvParser.getInfoMapa(), 10, VENTANA_HEIGHT - 20);
        }
        */
    }
    
    /**
     * Toggle modo debug
     */
    public void toggleDebug() {
        debug = !debug;
        System.out.println("[DEBUG] Modo debug: " + (debug ? "ACTIVADO" : "DESACTIVADO"));
    }
    
    // ==================== GETTERS ESTÁTICOS ====================
    
    public static int getVentanaWidth() {
        return VENTANA_WIDTH;
    }
    
    public static int getVentanaHeight() {
        return VENTANA_HEIGHT;
    }
    
    public static Texturas getTextura() {
        return textura;
    }
    
    // ==================== MÉTODOS PÚBLICOS ====================
    
    /**
     * Obtiene el handler del juego
     */
    public Handler getHandler() {
        return handler;
    }
    
    /**
     * Obtiene el nivel handler
     */
    public NivelesHandler getNivelHandler() {
        return nivelHandler;
    }
    
    /**
     * Obtiene el barrel spawner
     */
    public BarrilSpawner getBarrelSpawner() {
        return barrelSpawner;
    }
    /*
    public TiledCSVParser getCSVParser() {
        return csvParser;
    }
    */
    
    
    /**
     * Reinicia el nivel actual
     */
    public void reiniciarNivel() {
        System.out.println("\n[RESET] Reiniciando nivel...");
        
        // Limpiar objetos (excepto jugador)
        handler.getGameObjs().removeIf(obj -> obj.getId() != ObjetosID.Jugador);
        
        // Recargar mapa
        nivelHandler.recargarMapa();
        
        // Reconfigurar barrel spawner
        barrelSpawner = new BarrilSpawner(handler, nivelHandler.getBarrilSpawns());
        
        // Respawnear jugador
        respawnJugador();
        
        System.out.println("[RESET] Nivel reiniciado correctamente");
    }
    
    /**
     * Pausa el juego
     */
    public void pausar() {
        // TODO: Implementar sistema de pausa
        System.out.println("[PAUSE] Juego pausado");
    }
    
    /**
     * Reanuda el juego
     */
    public void reanudar() {
        // TODO: Implementar sistema de pausa
        System.out.println("[RESUME] Juego reanudado");
    }
}