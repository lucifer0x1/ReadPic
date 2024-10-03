package com.xybug.music;

import netscape.javascript.JSObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class RadarDBZCheck {



    public static HashMap<String, Color> dbzColor(){
        HashMap<String,Color> map = new HashMap<>();
        int[] r = new int[]{122,24,165 ,0 ,16 ,255 ,207 ,141 ,255 ,255 ,239 ,215 ,173};
        int[] g = new int[]{112 ,36 ,255 ,235 ,147 ,247 ,203 ,143 ,175 ,100 ,0 ,143 ,36};
        int[] b = new int[]{239 ,215 ,173 ,0 ,24 ,98 ,0 ,0 ,173 ,82 ,48 ,255 ,255};
        int[] v = new int[]{5 ,10 ,15 ,20 ,25 ,30 ,35 ,40 ,45 ,50 ,55 ,60 ,65};
        for (int i = 0; i < v.length; i++) {
            map.put(String.valueOf(v[i]),new Color(r[i],g[i],b[i]));
        }
        HashMap<String,Color> dbzMap = new HashMap<>();
        dbzMap.put(String.valueOf(5),new Color(122,112,239));
        dbzMap.put(String.valueOf(10),new Color(24,36,215));
        dbzMap.put(String.valueOf(15),new Color(165,255,173));
        dbzMap.put(String.valueOf(20),new Color(0,235,0));
        dbzMap.put(String.valueOf(25),new Color(16,147,24));
        dbzMap.put(String.valueOf(30),new Color(255,247,98));
        dbzMap.put(String.valueOf(35),new Color(207,203,0));
        dbzMap.put(String.valueOf(40),new Color(141,143,0));
        dbzMap.put(String.valueOf(45),new Color(255,175,173));
        dbzMap.put(String.valueOf(50),new Color(255,100,82));
        dbzMap.put(String.valueOf(55),new Color(239,0,48));
        dbzMap.put(String.valueOf(60),new Color(215,143,255));
        dbzMap.put(String.valueOf(55),new Color(173,36,255));
        return map;

    }

    public static void main(String[] args) throws IOException {
        int[] v = new int[]{5 ,10 ,15 ,20 ,25 ,30 ,35 ,40 ,45 ,50 ,55 ,60 ,65};
        HashMap<String ,Color> dbzMap =dbzColor();
        System.out.println(dbzMap);
        ClassLoader classLoader = PNGUtils.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("202410030300_dbz.png");
        assert inputStream != null;
        byte[] bytes = inputStream.readAllBytes();
        int offset  = 0;
        byte[] head = Arrays.copyOfRange(bytes ,offset,offset+8);
        offset += 8;
        byte[] len_IHDR = Arrays.copyOfRange(bytes,offset,offset+4);
        offset += 4;
        byte[] type_IHDR = Arrays.copyOfRange(bytes,offset,offset+4);
        offset += 4;
        byte[] chunkData_IHDR = Arrays.copyOfRange(bytes,offset,offset+PNGUtils.byteArrayToInt(len_IHDR,0));
        int w =PNGUtils.byteArrayToInt(chunkData_IHDR,0);
        int h = PNGUtils.byteArrayToInt(chunkData_IHDR,4);

        FileInputStream fis_a = new FileInputStream(System.getProperty("user.dir")+ File.separator + "a.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(fis_a));
        String line = br.readLine();

        BufferedImage image = new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
        int x = 0;
        int y = 0;
        while (line != null && !line.equals("")) {
            if(Integer.valueOf(line) <5){
                image.setRGB(x,y,Color.white.getRGB());
            }else {
                for (int i = 1; i < v.length; i++) {
                    if(Integer.valueOf(line) <v[i]){
                        image.setRGB(x,y,dbzMap.get(String.valueOf(v[i-1])).getRGB());
                        break;
                    }
                }
            }
            x++;
            if(x>=w){
                x=0;
                y++;
            }
            if(y>=h){
                System.out.println("break =>>>>>> x = " + x + ", y = " + y );
                break;
            }
            line = br.readLine();
        }
        image.flush();
        ImageIO.write(image, "PNG", new File(System.getProperty("user.dir")+ File.separator + "new.png"));
        br.close();
        fis_a.close();

    }
}
