package com.xybug.music;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.*;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    public  static int[] pixelsBuffer = new int[30000];

    public static int byteArrayToInt(byte[] bytes,int st) {
        int value = 0;
        // 由高位到低位
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (bytes[st+i] & 0x000000FF) << shift;// 往高位游
        }
        return value;
    }

    public static byte[] intToByteArray(int i) {
        byte[] result = new byte[4];
        // 由高位到低位
        result[0] = (byte) ((i >> 24) & 0xFF);
        result[1] = (byte) ((i >> 16) & 0xFF);
        result[2] = (byte) ((i >> 8) & 0xFF);
        result[3] = (byte) (i & 0xFF);
        return result;
    }

    public static  byte[] unzip(byte[] compressedData)  {
        System.out.println("before zlib " + compressedData.length);
        Inflater inflater = new Inflater();
        inflater.setInput(compressedData);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        while (!inflater.finished()) {
            int count = 0;
            try {
                count = inflater.inflate(buffer);
            } catch (DataFormatException e) {
                throw new RuntimeException(e);
            }
            outputStream.write(buffer, 0, count);
        }
        byte[] decompressedData = outputStream.toByteArray();
        System.out.println("after zlib " + decompressedData.length);
        inflater.end();
        try {
            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return decompressedData;
    }

    public static void main(String[] args) throws IOException {
        BufferedImage read = ImageIO.read(new File("C:\\OSGeo4W\\202409290324_dbz.png"));
//        BufferedImage read = ImageIO.read(new File("C:\\OSGeo4W\\202409291024_VIL.png"));
        FileOutputStream fos = new FileOutputStream(new File("C:\\OSGeo4W\\1.txt"));
        OutputStreamWriter osw = new OutputStreamWriter(fos);

        FileInputStream fis = new FileInputStream("C:\\OSGeo4W\\202409290324_dbz.png");
        byte[] bytes = fis.readAllBytes();
//        for (int i = 0; i < 8; i++) {
//            System.out.println("文件署名 " +( bytes[i]& 0xFF));
//        }

        int offset = 33 ;// idat.length
        int len =  byteArrayToInt(bytes,offset);
        System.out.println("长度 " +len);
        offset = 41;
        byte[] subArray = Arrays.copyOfRange(bytes, offset, len+offset);
        byte[] after = unzip(subArray);


        int w = read.getWidth();
        int h = read.getHeight();
        BufferedImage image = new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);


        int x = 0;
        int y = 0;
        System.out.println("w = " +w + ", h = " +h);
        for (int i = 1; i < after.length-3; i=i+4) {
            int r = (after[i]& 0xFF);
            int g = (after[i+1]& 0xFF);
            int b = (after[i+2]& 0xFF);
            int a = (after[i+3]& 0xFF);

            Color c =new Color(r, b,b,a);
            image.setRGB(x,y,c.getRGB());
//            image.setRGB(x,y,Color.YELLOW.getRGB());
            x++;
            if((x+4)>w){
                i++;
                y++;
                x=0;
            }
            if(y+1>h){
                break;
            }
//            System.out.println("w = " +w + ", h = " +h + "| i = "+ i + ",x = " + x + ", y = " + y);

            osw.write(after[i]& 0xFF);
            osw.write(after[i+1]& 0xFF);
            osw.write(after[i+2]& 0xFF);
            osw.write(after[i+3]& 0xFF);
        }


        image.flush();
        ImageIO.write(image, "PNG", new File("C:\\OSGeo4W\\dbz.png"));
        osw.flush();
        osw.close();

    }



}