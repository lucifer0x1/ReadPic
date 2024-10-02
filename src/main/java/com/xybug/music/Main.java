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

        System.out.println((read.getData().getWidth()*read.getData().getHeight()*read.getData().getNumBands()));
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
        BufferedImage image = new BufferedImage(w,h,BufferedImage.TYPE_4BYTE_ABGR);


        int x = 0;
        int y = 0;
        System.out.println("w = " +w + ", h = " +h);
        for (int i = 0; i < after.length-3; i=i+4) {
            int r = (after[i]& 0xFF);
            int g = (after[i+1]& 0xFF);
            int b = (after[i+2]& 0xFF);
            int a = (after[i+3]& 0xFF);

            if(r!=0 || b !=0 || b!=0 || a!=0) {
                System.out.println(r + " " + b + " " + b + " " + a);
            }
            Color c =new Color(r, b,b,a);
            image.setRGB(x,y,c.getRGB());
//            image.setRGB(x,y,Color.YELLOW.getRGB());
            x++;
            if((x+4)>w){
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

        // 8 byte 文件署名
        osw.flush();
        osw.close();








    }

    public void scan(byte[] data,int width,int height , int colors,int bitDepth) throws IOException {
//        int  width = w; // 解析IHDR数据块时得到的图像宽度
//        int height = h; // 解析IHDR数据块时得到的图像高度
//        int colors = c; // 解析IHDR数据块时得到的通道数
//        int bitDepth = d; // 解析IHDR数据块时得到的图像深度

        int bytesPerPixel = Math.max(1, colors * bitDepth / 8); // 每像素字节数
        int bytesPerRow = bytesPerPixel * width; // 每行字节数

        byte[] pixelsBuffer = new byte[bytesPerPixel * width * height]; // 存储过滤后的像素数据
        int offset = 0; // 当前行的偏移位置

        // 逐行扫描解析
        for(int i=0, len=data.length; i<len; i+=bytesPerRow+1) {
            byte[] scanline = Arrays.copyOfRange(data, i+1,  i+1+bytesPerRow);

            // 第一个字节代表过滤类型
            switch(readInt8(data, i)) {
                case 0:
                    filterNone(scanline, bytesPerPixel, bytesPerRow, offset);
                    break;
                case 1:
                    filterSub(scanline, bytesPerPixel, bytesPerRow, offset);
                    break;
                case 2:
                    filterUp(scanline, bytesPerPixel, bytesPerRow, offset);
                    break;
                case 3:
                    filterAverage(scanline, bytesPerPixel, bytesPerRow, offset);
                    break;
                case 4:
                    filterPaeth(scanline, bytesPerPixel, bytesPerRow, offset);
                    break;
                default:
                    System.out.println("未知过滤类型！");
            }
            offset += bytesPerRow;
        }
    }

    public int readInt8(byte[] buffer, int offset) {
        return Byte.toUnsignedInt(buffer[offset]);
    }

    public static void  filterNone(byte[] scanline, int bytesPerPixel,int  bytesPerRow,int  offset) {
        for(int i=0; i<bytesPerRow; i++) {
            pixelsBuffer[offset + i] = scanline[i];
        }
    }

    public static  void filterSub(byte[] scanline, int bytesPerPixel,int  bytesPerRow,int  offset) {

        for(int  i=0; i<bytesPerRow; i++) {
            if(i < bytesPerPixel) {
                // 第一个像素，不作解析
                pixelsBuffer[offset + i] = scanline[i];
            } else {
                // 其他像素
                int a = pixelsBuffer[offset + i - bytesPerPixel];

                int value = scanline[i] + a;
                pixelsBuffer[offset + i] = value & 0xFF;
            }
        }
    }

    public static void filterUp(byte[] scanline, int bytesPerPixel,int  bytesPerRow,int  offset) {
        if(offset < bytesPerRow) {
            // 第一行，不作解析
            for(int i=0; i<bytesPerRow; i++) {
                pixelsBuffer[offset + i] = scanline[i];
            }
        } else {
            for(int i=0; i<bytesPerRow; i++) {
                int b = pixelsBuffer[offset + i - bytesPerRow];
                int value = scanline[i] + b;
                pixelsBuffer[offset + i] = value & 0xFF;
            }
        }
    }

    public static void filterAverage(byte[] scanline, int bytesPerPixel,int  bytesPerRow,int  offset) {
        if(offset < bytesPerRow) {
            // 第一行，只做Sub
            for(int i=0; i<bytesPerRow; i++) {
                if(i < bytesPerPixel) {
                    // 第一个像素，不作解析
                    pixelsBuffer[offset + i] = scanline[i];
                } else {
                    // 其他像素
                    int a = pixelsBuffer[offset + i - bytesPerPixel];

                    int value = scanline[i] + (a >> 1); // 需要除以2
                    pixelsBuffer[offset + i] = value & 0xFF;
                }
            }
        } else {
            for(int i=0; i<bytesPerRow; i++) {
                if(i < bytesPerPixel) {
                    // 第一个像素，只做Up
                    int b = pixelsBuffer[offset + i - bytesPerRow];

                    int value = scanline[i] + (b >> 1); // 需要除以2
                    pixelsBuffer[offset + i] = value & 0xFF;
                } else {
                    // 其他像素
                    int a = pixelsBuffer[offset + i - bytesPerPixel];
                    int b = pixelsBuffer[offset + i - bytesPerRow];

                    int value = scanline[i] + ((a + b) >> 1);
                    pixelsBuffer[offset + i] = value & 0xFF;
                }
            }
        }
    }

    public static void filterPaeth(byte[] scanline, int bytesPerPixel,int  bytesPerRow,int  offset) {
        if(offset < bytesPerRow) {
            // 第一行，只做Sub
            for(int i=0; i<bytesPerRow; i++) {
                if(i < bytesPerPixel) {
                    // 第一个像素，不作解析
                    pixelsBuffer[offset + i] = scanline[i];
                } else {
                    // 其他像素
                    int a = pixelsBuffer[offset + i - bytesPerPixel];
                    int value = scanline[i] + a;
                    pixelsBuffer[offset + i] = value & 0xFF;
                }
            }
        } else {
            for(int i=0; i<bytesPerRow; i++) {
                if(i < bytesPerPixel) {
                    // 第一个像素，只做Up
                    int b = pixelsBuffer[offset + i - bytesPerRow];
                    int value = scanline[i] + b;
                    pixelsBuffer[offset + i] = value & 0xFF;
                } else {
                    // 其他像素
                    int a = pixelsBuffer[offset + i - bytesPerPixel];
                    int b = pixelsBuffer[offset + i - bytesPerRow];
                    int c = pixelsBuffer[offset + i - bytesPerRow - bytesPerPixel];
                    int p = a + b - c;
                    int pa = Math.abs(p - a);
                    int pb = Math.abs(p - b);
                    int pc = Math.abs(p - c);
                    int pr;
                    if (pa <= pb && pa <= pc) pr = a;
                    else if (pb <= pc) pr = b;
                    else pr = c;
                    int value = scanline[i] + pr;
                    pixelsBuffer[offset + i] = value & 0xFF;
                }
            }
        }
    }

}