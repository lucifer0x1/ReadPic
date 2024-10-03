package com.xybug.music;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;


//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    public static void main(String[] args) throws IOException {
        ClassLoader classLoader = PNGUtils.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("202410030300_dbz.png");
        BufferedImage bi = ImageIO.read(inputStream);
        HashMap<String ,Color> dbzMap =RadarDBZCheck.dbzColor();
        int[] v = new int[]{5 ,10 ,15 ,20 ,25 ,30 ,35 ,40 ,45 ,50 ,55 ,60 ,65};

        int width = bi.getWidth();
        int height = bi.getHeight();

//        ImageIO.write(image, "PNG", new File(System.getProperty("user.dir")+ File.separator + "new.png"));

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y< height; y++) {
            for (int x = 0; x < width; x++) {
                Color c = new Color( bi.getRGB(x, y));
                int r = c.getRed();
                int g = c.getGreen();
                int b = c.getBlue();
                int dbz = (r+g+b)/10;
                if(Integer.valueOf(dbz) <5) {
                    image.setRGB(x,height - y -1, Color.white.getRGB());
                }else {
                    for (int i = 1; i < v.length; i++) {
                        if(dbz <v[i]){
                            image.setRGB(x,height -y -1,dbzMap.get(String.valueOf(v[i-1])).getRGB());
                            break;
                        }
                    }
                }
            }
        }
        ImageIO.write(image, "PNG", new File(System.getProperty("user.dir")+ File.separator + "copy.png"));

    }



}