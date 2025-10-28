/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Objetos;

import Objetos.Utilidad.ObjetosID;
import java.awt.Graphics;
import java.awt.Rectangle;

/**
 *
 * @author LENOVO
 */
public class Tuberia extends GameObjetos{
    private boolean enterable;
    
    public Tuberia(int x , int y , int width , int height,int scale){
       super(x, y, ObjetosID.Pipe, width, height, scale);
       this.enterable = enterable;
    }

    @Override
    public void tick() {
    }

    @Override
    public void render(Graphics g) {
    }

    @Override
    public Rectangle getBounds() {
      return new Rectangle((int) getX(), (int) getY(), (int) getWidth(),(int) getHeight());
 
    }
}
