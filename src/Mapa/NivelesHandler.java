package Mapa;

import GameGFX.*;
import Objetos.*;
import Objetos.Utilidad.*;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sistema mejorado de carga de niveles desde PNG
 * Cada pixel representa un tile del nivel
 * 
 * @author LENOVO
 */
public class NivelesHandler {
    private final String FOLDER = "/Imagenes/";
    
    // Configuración del mapa
    public static final int TILE_SIZE = 8;           // Tamaño base del tile (8x8 pixels)
    public static final int ESCALA_VISUAL = 1;        // Escalar 4x para mejor visualización
    public static final int TILE_RENDER_SIZE = TILE_SIZE * ESCALA_VISUAL; // 32 pixels en pantalla
    
    // Componentes del sistema
    private CargadorImagenes cargador;
    private BufferedImage mapaNivel;
    private Handler handler;
    
    // Estructuras de datos optimizadas
    private boolean[][] tilesSolidos;                 // Matriz de colisiones rápida
    private List<Point> barrilSpawns;                 // Puntos de spawn de barriles
    private List<Point> escalerasPos;                 // Posiciones de escaleras
    private Point posicionInicioDK;                   // Posición de Donkey Kong
    private Point posicionPrincesa;                   // Posición de la princesa
    
    // Dimensiones del mapa
    private int mapaAncho;
    private int mapaAlto;
    
    /**
     * Colores RGB para mapeo de tiles
     * Usa valores ARGB exactos: 0xFFRRGGBB
     */
    private static final int COLOR_PLATAFORMA_ROJA = 0xFFFF0000;      // Rojo puro
    private static final int COLOR_PLATAFORMA_AZUL = 0xFF0000FF;      // Azul puro
    private static final int COLOR_ESCALERA = 0xFF00FFFF;              // Cyan
    private static final int COLOR_ESCALERA_ROTA = 0xFFFFFF00;        // Amarillo
    private static final int COLOR_MARIO_SPAWN = 0xFF00FF00;          // Verde
    private static final int COLOR_DK_POSICION = 0xFFFF00FF;          // Magenta
    private static final int COLOR_PRINCESA = 0xFFFFC0CB;              // Rosa
    private static final int COLOR_BARRIL_SPAWN = 0xFFFFA500;         // Naranja
    private static final int COLOR_VACIO = 0xFF000000;                 // Negro (transparente)
    
    /**
     * Constructor del manejador de niveles
     */
    public NivelesHandler(Handler handler) {
        this.handler = handler;
        this.cargador = new CargadorImagenes();
        this.barrilSpawns = new ArrayList<>();
        this.escalerasPos = new ArrayList<>();
        
        cargarYProcesarMapa();
    }
    
    /**
     * Carga la imagen del mapa y la procesa
     */
    private void cargarYProcesarMapa() {
        try {
            mapaNivel = cargador.loadImage(FOLDER + "1_1.png");
            
            if (mapaNivel == null) {
                System.err.println("ERROR: No se pudo cargar la imagen del mapa");
                crearMapaPorDefecto();
                return;
            }
            
            mapaAncho = mapaNivel.getWidth();
            mapaAlto = mapaNivel.getHeight();
            
            // Inicializar matriz de colisiones
            tilesSolidos = new boolean[mapaAncho][mapaAlto];
            
            System.out.println("=== CARGANDO MAPA DONKEY KONG ===");
            System.out.println("Dimensiones: " + mapaAncho + "x" + mapaAlto + " píxeles");
            System.out.println("Tamaño pantalla: " + (mapaAncho * TILE_RENDER_SIZE) + "x" + (mapaAlto * TILE_RENDER_SIZE));
            
            procesarImagenMapa();
            
            System.out.println("=== MAPA CARGADO EXITOSAMENTE ===");
            System.out.println("Barriles spawn: " + barrilSpawns.size());
            System.out.println("Escaleras: " + escalerasPos.size());
            
        } catch (Exception e) {
            System.err.println("Error cargando mapa: " + e.getMessage());
            e.printStackTrace();
            crearMapaPorDefecto();
        }
    }
    
    /**
     * Procesa la imagen del mapa pixel por pixel
     * Cada pixel representa un tile del juego
     */
    private void procesarImagenMapa() {
        int platformCount = 0;
        int ladderCount = 0;
        
        for (int y = 0; y < mapaAlto; y++) {
            for (int x = 0; x < mapaAncho; x++) {
                int pixelARGB = mapaNivel.getRGB(x, y);
                
                // Convertir posición de pixel a coordenadas de mundo
                int worldX = x * TILE_RENDER_SIZE;
                int worldY = y * TILE_RENDER_SIZE;
                
                // Procesar según el color del pixel
                switch (pixelARGB) {
                    case COLOR_PLATAFORMA_ROJA:
                    case COLOR_PLATAFORMA_AZUL:
                        // Crear plataforma sólida
                        crearPlataforma(worldX, worldY);
                        tilesSolidos[x][y] = true;
                        platformCount++;
                        break;
                        
                    case COLOR_ESCALERA:
                        // Crear escalera normal
                        crearEscalera(worldX, worldY, false);
                        escalerasPos.add(new Point(x, y));
                        ladderCount++;
                        break;
                        
                    case COLOR_ESCALERA_ROTA:
                        // Crear escalera rota
                        crearEscalera(worldX, worldY, true);
                        escalerasPos.add(new Point(x, y));
                        ladderCount++;
                        break;
                        
                    case COLOR_MARIO_SPAWN:
                        // Posicionar a Mario
                        posicionarMario(worldX, worldY);
                        break;
                        
                    case COLOR_DK_POSICION:
                        // Guardar posición de DK para futuro
                        posicionInicioDK = new Point(worldX, worldY);
                        System.out.println("DK spawn en: (" + worldX + ", " + worldY + ")");
                        break;
                        
                    case COLOR_PRINCESA:
                        // Guardar posición de la princesa
                        posicionPrincesa = new Point(worldX, worldY);
                        System.out.println("Princesa en: (" + worldX + ", " + worldY + ")");
                        break;
                        
                    case COLOR_BARRIL_SPAWN:
                        // Agregar punto de spawn de barriles
                        barrilSpawns.add(new Point(worldX, worldY));
                        break;
                        
                    case COLOR_VACIO:
                        // Espacio vacío, no hacer nada
                        break;
                        
                    default:
                        // Color no reconocido, intentar aproximación
                        procesarColorDesconocido(pixelARGB, worldX, worldY, x, y);
                        break;
                }
            }
        }
        
        System.out.println("Plataformas creadas: " + platformCount);
        System.out.println("Escaleras creadas: " + ladderCount);
    }
    
    /**
     * Procesa colores que no coinciden exactamente
     * Usa tolerancia para aproximar al color más cercano
     */
    private void procesarColorDesconocido(int pixelARGB, int worldX, int worldY, int tileX, int tileY) {
        // Extraer componentes RGB (ignorar alpha)
        int r = (pixelARGB >> 16) & 0xFF;
        int g = (pixelARGB >> 8) & 0xFF;
        int b = pixelARGB & 0xFF;
        
        // Verificar si es un color oscuro (probablemente vacío)
        if (r < 30 && g < 30 && b < 30) {
            return; // Ignorar colores muy oscuros
        }
        
        // Buscar el color más cercano con tolerancia
        final int TOLERANCIA = 40;
        
        if (esColorSimilar(r, g, b, 255, 0, 0, TOLERANCIA)) {
            // Rojo - Plataforma
            crearPlataforma(worldX, worldY);
            tilesSolidos[tileX][tileY] = true;
        } 
        else if (esColorSimilar(r, g, b, 0, 0, 255, TOLERANCIA)) {
            // Azul - Plataforma
            crearPlataforma(worldX, worldY);
            tilesSolidos[tileX][tileY] = true;
        }
        else if (esColorSimilar(r, g, b, 0, 255, 255, TOLERANCIA)) {
            // Cyan - Escalera
            crearEscalera(worldX, worldY, false);
            escalerasPos.add(new Point(tileX, tileY));
        }
        else if (esColorSimilar(r, g, b, 255, 255, 0, TOLERANCIA)) {
            // Amarillo - Escalera rota
            crearEscalera(worldX, worldY, true);
            escalerasPos.add(new Point(tileX, tileY));
        }
        else if (esColorSimilar(r, g, b, 0, 255, 0, TOLERANCIA)) {
            // Verde - Mario
            posicionarMario(worldX, worldY);
        }
    }
    
    /**
     * Verifica si un color RGB es similar a otro dentro de una tolerancia
     */
    private boolean esColorSimilar(int r1, int g1, int b1, int r2, int g2, int b2, int tolerancia) {
        double distancia = Math.sqrt(
            Math.pow(r1 - r2, 2) + 
            Math.pow(g1 - g2, 2) + 
            Math.pow(b1 - b2, 2)
        );
        return distancia <= tolerancia;
    }
    
    /**
     * Crea una plataforma sólida en la posición especificada
     */
    private void crearPlataforma(int x, int y) {
        Bloque bloque = new Bloque(x, y, TILE_RENDER_SIZE, TILE_RENDER_SIZE, 1);
        handler.addObj(bloque);
    }
    
    /**
     * Crea una escalera en la posición especificada
     */
    private void crearEscalera(int x, int y, boolean esRota) {
        Escalera escalera = new Escalera(x, y, TILE_RENDER_SIZE, TILE_RENDER_SIZE, esRota);
        handler.addObj(escalera);
    }
    
    /**
     * Posiciona al jugador (Mario) en la posición inicial
     */
    private void posicionarMario(int x, int y) {
        Player player = handler.getPlayer();
        if (player != null) {
            // Ajustar para que esté sobre la plataforma
            player.setX(x);
            player.setY(y - TILE_RENDER_SIZE);
            System.out.println("Mario posicionado en: (" + x + ", " + (y - TILE_RENDER_SIZE) + ")");
        } else {
            System.out.println("ADVERTENCIA: Player no existe aún, guardando posición para después");
        }
    }
    
    /**
     * Crea un mapa básico por defecto si no se puede cargar la imagen
     */
    private void crearMapaPorDefecto() {
        System.out.println("Creando mapa por defecto...");
        
        mapaAncho = 30;
        mapaAlto = 22;
        tilesSolidos = new boolean[mapaAncho][mapaAlto];
        
        // Plataforma inferior
        for (int i = 0; i < mapaAncho; i++) {
            int x = i * TILE_RENDER_SIZE;
            int y = (mapaAlto - 2) * TILE_RENDER_SIZE;
            crearPlataforma(x, y);
            tilesSolidos[i][mapaAlto - 2] = true;
        }
        
        // Plataformas intermedias simulando Donkey Kong
        crearPlataformaHorizontal(0, 16, 25, 1);
        crearPlataformaHorizontal(5, 12, 25, -1);
        crearPlataformaHorizontal(0, 8, 25, 1);
        crearPlataformaHorizontal(5, 4, 20, -1);
        
        // Escaleras
        crearEscaleraVertical(10, 13, 3, false);
        crearEscaleraVertical(18, 9, 3, false);
        
        // Posicionar Mario por defecto
        if (handler.getPlayer() != null) {
            handler.getPlayer().setX(3 * TILE_RENDER_SIZE);
            handler.getPlayer().setY((mapaAlto - 4) * TILE_RENDER_SIZE);
        }
        
        System.out.println("Mapa por defecto creado");
    }
    
    /**
     * Crea una plataforma horizontal con inclinación simulada
     */
    private void crearPlataformaHorizontal(int startX, int startY, int longitud, int inclinacion) {
        for (int i = 0; i < longitud; i++) {
            int x = (startX + i) * TILE_RENDER_SIZE;
            int y = (startY + (i * inclinacion / 8)) * TILE_RENDER_SIZE;
            
            int tileX = startX + i;
            int tileY = startY + (i * inclinacion / 8);
            
            if (tileX >= 0 && tileX < mapaAncho && tileY >= 0 && tileY < mapaAlto) {
                crearPlataforma(x, y);
                tilesSolidos[tileX][tileY] = true;
            }
        }
    }
    
    /**
     * Crea una escalera vertical
     */
    private void crearEscaleraVertical(int x, int startY, int altura, boolean esRota) {
        for (int i = 0; i < altura; i++) {
            int worldX = x * TILE_RENDER_SIZE;
            int worldY = (startY + i) * TILE_RENDER_SIZE;
            crearEscalera(worldX, worldY, esRota);
            escalerasPos.add(new Point(x, startY + i));
        }
    }
    
    // ==================== SISTEMA DE COLISIONES OPTIMIZADO ====================
    
    /**
     * Verifica si un tile es sólido en coordenadas del mundo
     */
    public boolean esTileSolido(int worldX, int worldY) {
        int tileX = worldX / TILE_RENDER_SIZE;
        int tileY = worldY / TILE_RENDER_SIZE;
        
        if (tileX < 0 || tileX >= mapaAncho || tileY < 0 || tileY >= mapaAlto) {
            return false; // Fuera del mapa
        }
        
        return tilesSolidos[tileX][tileY];
    }
    
    /**
     * Obtiene todos los tiles sólidos que intersectan con un rectángulo
     * Útil para optimizar detección de colisiones
     */
    public List<Point> getTilesSolidosEnArea(int x, int y, int width, int height) {
        List<Point> tiles = new ArrayList<>();
        
        int startTileX = Math.max(0, x / TILE_RENDER_SIZE);
        int endTileX = Math.min(mapaAncho - 1, (x + width) / TILE_RENDER_SIZE);
        int startTileY = Math.max(0, y / TILE_RENDER_SIZE);
        int endTileY = Math.min(mapaAlto - 1, (y + height) / TILE_RENDER_SIZE);
        
        for (int ty = startTileY; ty <= endTileY; ty++) {
            for (int tx = startTileX; tx <= endTileX; tx++) {
                if (tilesSolidos[tx][ty]) {
                    tiles.add(new Point(tx * TILE_RENDER_SIZE, ty * TILE_RENDER_SIZE));
                }
            }
        }
        
        return tiles;
    }
    
    // ==================== GETTERS ====================
    
    public List<Point> getBarrilSpawns() {
        return new ArrayList<>(barrilSpawns);
    }
    
    public List<Point> getEscalerasPos() {
        return new ArrayList<>(escalerasPos);
    }
    
    public Point getPosicionInicioDK() {
        return posicionInicioDK;
    }
    
    public Point getPosicionPrincesa() {
        return posicionPrincesa;
    }
    
    public int getMapaAncho() {
        return mapaAncho;
    }
    
    public int getMapaAlto() {
        return mapaAlto;
    }
    
    public int getMapaAnchoPixels() {
        return mapaAncho * TILE_RENDER_SIZE;
    }
    
    public int getMapaAltoPixels() {
        return mapaAlto * TILE_RENDER_SIZE;
    }
    
    /**
     * Información del mapa para debug
     */
    public String getInfoMapa() {
        return String.format(
            "Mapa: %dx%d tiles (%dx%d pixels) | Escala: %dx | Tile: %dx%d -> %dx%d",
            mapaAncho, mapaAlto,
            mapaAncho * TILE_RENDER_SIZE, mapaAlto * TILE_RENDER_SIZE,
            ESCALA_VISUAL,
            TILE_SIZE, TILE_SIZE,
            TILE_RENDER_SIZE, TILE_RENDER_SIZE
        );
    }
    
    /**
     * Recarga el mapa desde la imagen
     */
    public void recargarMapa() {
        // Limpiar objetos existentes (excepto el jugador)
        handler.getGameObjs().removeIf(obj -> obj.getId() != ObjetosID.Jugador);
        
        // Limpiar listas
        barrilSpawns.clear();
        escalerasPos.clear();
        
        // Recargar
        cargarYProcesarMapa();
        
        System.out.println("Mapa recargado");
    }
}