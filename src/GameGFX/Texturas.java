package GameGFX;
import java.awt.image.BufferedImage;
import java.util.HashMap;
/**
 *
 * @author LENOVO
 */
public class Texturas {
    private final String folder = "/Imagenes";
    
    private final int mario_L_count = 21;
    private final int mario_S_count = 14; //mario grande
    
    private final int Tile_1_count = 28; // mario pequeño
    private final int Tile_2_count = 33; // 
    
    private final int barril_count = 8;
    private final int diegokong_count = 8;
    private final int bloque_count = 8;
    private final int princesa_count = 8;
    
    
    private CargadorImagenes cargar;
    // HOJA DE PERSONAJES O OBJETOS DE LAS IMAGENES
    private BufferedImage player_sheet , enemy_sheet_, bloque_sheet , barril_sheet , diegokong_sheet ,princesaSheet;
    private HashMap<Integer, BufferedImage> tileSprites;

    private BufferedImage[] mario_l , mario_s , tile1,tile2,tile3,tile4 , barril_sprites , diegokong_sprites , bloque_sprites,princesaSprites;
    
    public Texturas(){
        mario_l = new BufferedImage[mario_L_count];
        mario_s = new BufferedImage[mario_S_count];
        tile1 = new BufferedImage[Tile_1_count + Tile_2_count];
        tile2 = new BufferedImage[Tile_1_count + Tile_2_count];
        tile3 = new BufferedImage[Tile_1_count + Tile_2_count];
        tile4 = new BufferedImage[Tile_1_count + Tile_2_count];  
        barril_sprites = new BufferedImage [barril_count];
        diegokong_sprites = new BufferedImage[diegokong_count];
        princesaSprites = new BufferedImage[princesa_count];
        
        tileSprites = new HashMap<>();
        cargar = new CargadorImagenes();
        
        try{
           player_sheet = cargar.loadImage(folder + "/testt.png");
           barril_sheet = cargar.loadImage(folder + "/testt.png");
           diegokong_sheet = cargar.loadImage(folder + "/testt.png");
           bloque_sheet = cargar.loadImage(folder + "/bloques2.png");
           princesaSheet = cargar.loadImage(folder + "/testt.png");
            
        }catch(Exception e){
            e.printStackTrace();
        }
        
        getPlayerTexturas();
        getBarrilTexturas();
        getDiegoKongTexturas();
        getPrincesaTexturas();
        getBloquesTexturas();
    }
    
    private void getPlayerTexturas(){
        int x_off = 1;
        int y_off = 1;
        int width = 16;
        int height = 16;
        // ver esto cuando anima
        for (int i = 0 ; i < mario_S_count; i++){
            mario_s[i] = player_sheet.getSubimage(x_off + i*(width+2), y_off, width, height);
        }
    }
    
    private void getBarrilTexturas(){
        int x_off = 1;
        int y_off = 229;  // Ajustar según tu hoja de sprites
        int width = 16;
        int height = 16;
        
        // Extraer frames de rotación del barril
        for (int i = 0; i < barril_count; i++) {
            barril_sprites[i] = barril_sheet.getSubimage(
                x_off + i * (width + 2), 
                y_off, 
                width, 
                height
            );
        }
    }
    private void getDiegoKongTexturas() {
        int width = 48;
        int height = 32;
        int spritesPrimeraFila = 4; // Number of sprites on the first row

        try {
            // Fila 1 (Y=258)
            int x_off_fila1 = 1;
            int y_off_fila1 = 258;
            for (int i = 0; i < spritesPrimeraFila && i < diegokong_count; i++) {
                diegokong_sprites[i] = diegokong_sheet.getSubimage(
                    x_off_fila1 + i * (width + 2), 
                    y_off_fila1, 
                    width, 
                    height
                );
            }
            
            // Fila 2 (Y=292)
            int x_off_fila2 = 1;
            int y_off_fila2 = 292;
            for (int i = spritesPrimeraFila; i < diegokong_count; i++) {
                int spriteIndexEnFila = i - spritesPrimeraFila;
                diegokong_sprites[i] = diegokong_sheet.getSubimage(
                    x_off_fila2 + spriteIndexEnFila * (width + 2), 
                    y_off_fila2, 
                    width, 
                    height
                );
            }
            
            System.out.println("[TEXTURAS] Sprites de Diego Kong cargados correctamente: " + diegokong_count);
            
        } catch (Exception e) {
            System.err.println("[ERROR] Fallo al cargar los sprites de Diego Kong: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
        private void getPrincesaTexturas(){
        int x_off = 1;
        int y_off = 141;
        int width = 16;
        int height = 32;
        
        for (int i = 0 ; i < princesa_count; i++){
            princesaSprites[i] = princesaSheet.getSubimage(x_off + i*(width+2), y_off, width, height);
        }
            
        }
    
     private void getBloquesTexturas() {
    if (bloque_sheet == null) {
        System.err.println("[ERROR] La hoja de sprites 'bloques_set.png' no se pudo cargar.");
        return;
    }
    

    // =================== PARÁMETROS EDITABLES ===================
    //
    // Edita estos valores para que coincidan con tu imagen 'bloques_set.png'

    // Coordenada X inicial para empezar a cortar (píxeles desde la izquierda)
    final int x_off = 0; 
    
    // Coordenada Y inicial para empezar a cortar (píxeles desde arriba)
    final int y_off = 0; // Primera fila que quieres cortar
    
    // Ancho de cada tile individual en píxeles
    final int tileWidth = 8;
    
    // Alto de cada tile individual en píxeles
    final int tileHeight = 8;
    
    // Espacio horizontal entre un tile y el siguiente (en píxeles)
    final int x_spacing = 0;
    
    // Espacio vertical entre una fila de tiles y la siguiente (en píxeles)
    final int y_spacing = 0;
    
    // ID del primer tile en Tiled (el de la esquina superior izquierda del set)
    final int firstgid = 1;
    
    // ============================================================
    
    // NUEVO: Definimos cuántas filas queremos cortar
    final int NUM_FILAS_A_CORTAR = 5;

    int currentTileID = firstgid;
    
    System.out.println("[TEXTURAS] Cargando " + NUM_FILAS_A_CORTAR + " filas de tiles del mapa...");
    
    // El bucle Y se limita para cortar exactamente las 5 filas
    for (int fila = 0; fila < NUM_FILAS_A_CORTAR; fila++) {
        // Calcula la coordenada Y para la fila actual
        int y = y_off + fila * (tileHeight + y_spacing);

        // Asegúrate de que no te salgas de los límites de la imagen
        if (y + tileHeight > bloque_sheet.getHeight()) {
            System.err.println("[ADVERTENCIA] La fila " + (fila + 1) + " excede el alto de la hoja de sprites.");
            break; // Detener si se excede el límite
        }

        // Bucle para cortar los tiles horizontalmente en la fila
        for (int x = x_off; x + tileWidth <= bloque_sheet.getWidth(); x += tileWidth + x_spacing) {
        // Corta el sprite en (x, y)
        BufferedImage sprite = bloque_sheet.getSubimage(x, y, tileWidth, tileHeight);
        
        // Guarda el sprite en el mapa con el ID
        tileSprites.put(currentTileID, sprite);
        
        currentTileID++; // <-- ESTO ASEGURA QUE CADA TILE TENGA UN ID ÚNICO Y CONSECUTIVO
    }
    }
    
}
    
    
    public BufferedImage[] getMarioL(){
        return mario_l;
    }
    
    public BufferedImage[] getMarioS(){
        return mario_s;
    }
      
    public BufferedImage[] getTile1(){
        return tile1;
    }
    
    public BufferedImage[] getTile2(){
        return tile2;
    }
    
    public BufferedImage[] getTile3(){
        return tile3;
    }
    
    public BufferedImage[] getTile4(){
        return tile4;
    }
    
     public BufferedImage[] getBarrilSprites() {
        return barril_sprites;
    }
public BufferedImage[] getDiegoKongSprites() {
    return diegokong_sprites;
}
 public BufferedImage getSpritePorID(int tileID) {
        return tileSprites.get(tileID);
    }

    public BufferedImage[] getPrincesaSprites() {
        return princesaSprites;
    }
 
}
