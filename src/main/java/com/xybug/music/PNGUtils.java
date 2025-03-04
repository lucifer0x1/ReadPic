package com.xybug.music;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.*;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class PNGUtils {
    public  static int[] pixelsBuffer = new int[30000];


    /**
     *
     * @return offset
     */
    public static int chunkRead(byte[] bytes, int offset){
        byte[] len = Arrays.copyOfRange(bytes,offset,offset+4);
        offset += 4;
        byte[] type = Arrays.copyOfRange(bytes,offset,offset+4);
        offset += 4;
        byte[] chunkData = Arrays.copyOfRange(bytes,offset,offset+byteArrayToInt(len,0));
        offset+=byteArrayToInt(len,0);
        byte[] crc = Arrays.copyOfRange(bytes,offset,offset+4);
        offset+=4;
        return  offset;
    }

    public static void main(String[] args) throws IOException {
        ClassLoader classLoader = PNGUtils.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("202410030300_dbz.png");
        assert inputStream != null;
        byte[] bytes = inputStream.readAllBytes();
//        System.out.println( "==>" + System.getProperty("java.class.path"));
//        System.out.println( "==>" + System.getProperty("user.dir"));

        FileOutputStream fos_r = new FileOutputStream(System.getProperty("user.dir")+ File.separator + "r.txt");
        FileOutputStream fos_g = new FileOutputStream(System.getProperty("user.dir")+ File.separator + "g.txt");
        FileOutputStream fos_b = new FileOutputStream(System.getProperty("user.dir")+ File.separator + "b.txt");
        FileOutputStream fos_a = new FileOutputStream(System.getProperty("user.dir")+ File.separator + "a.txt");

        OutputStreamWriter osw_r = new OutputStreamWriter(fos_r);
        OutputStreamWriter osw_g = new OutputStreamWriter(fos_g);
        OutputStreamWriter osw_b = new OutputStreamWriter(fos_b);
        OutputStreamWriter osw_a = new OutputStreamWriter(fos_a);

        int offset  = 0;

        byte[] head = Arrays.copyOfRange(bytes, offset,offset+8);
        offset += 8;
        // TODO IHDR
        byte[] len_IHDR = Arrays.copyOfRange(bytes,offset,offset+4);
        offset += 4;
        byte[] type_IHDR = Arrays.copyOfRange(bytes,offset,offset+4);
        offset += 4;
        byte[] chunkData_IHDR = Arrays.copyOfRange(bytes,offset,offset+byteArrayToInt(len_IHDR,0));
        offset+=byteArrayToInt(len_IHDR,0);
        byte[] crc_IHDR = Arrays.copyOfRange(bytes,offset,offset+4);
        offset+=4;
        System.out.println("offset IHDR = " + offset);
        // TODO IDAT
        byte[] len_IDAT = Arrays.copyOfRange(bytes,offset,offset+4);
        offset += 4;
        byte[] type_IDAT = Arrays.copyOfRange(bytes,offset,offset+4);
        offset += 4;
        byte[] chunkData_IDAT = Arrays.copyOfRange(bytes,offset,offset+byteArrayToInt(len_IDAT,0));
        offset+=byteArrayToInt(len_IDAT,0);
        byte[] crc_IDAT = Arrays.copyOfRange(bytes,offset,offset+4);
        offset+=4;
        System.out.println("offset IDAT = " + offset);
        System.out.println("len IDAT = " + byteArrayToInt(len_IDAT,0));

        byte[] subArray = chunkData_IDAT;
        byte[] data = unzip(subArray);

        int w =byteArrayToInt(chunkData_IHDR,0);
        int h = byteArrayToInt(chunkData_IHDR,4);
        int d = chunkData_IHDR[8];
        int c_type = chunkData_IHDR[9];
        int c = 4;
        System.out.println("压缩 "+ chunkData_IHDR[10]);//压缩
        System.out.println("过滤 "+ chunkData_IHDR[11]);//过滤
        System.out.println("扫描 "+ chunkData_IHDR[12]);//扫描
        System.out.println("w " +w);
        System.out.println("h " +h);
        System.out.println("d =" + d);
        System.out.println("c =" + c);


        int bytesPerPixel = Math.max(1, c * d / 8);
        pixelsBuffer = new int[bytesPerPixel * w * h]; // 存储过滤后的像素数据

        // TODO scan
        scan(data, w, h, c, d);

        BufferedImage image = new BufferedImage(w,h,BufferedImage.TYPE_3BYTE_BGR);

        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                int index = bytesPerPixel * ( (h-j-1) * w + i);
                //0.299 * R + 0.587 * G + 0.114 * B
                int r = pixelsBuffer[index];
                int g = pixelsBuffer[index+1];
                int b = pixelsBuffer[index+2];
                int a =pixelsBuffer[index+3];
                int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);

                try {
                    Color color = new Color(r,r,b,a);
                } catch (Exception e){
                    System.out.println(r);
                    System.out.println(g);
                    System.out.println(b);
                    e.printStackTrace();
                }

//                r = r>>1;
//                g = g>>1;
//                b = b>>1;
                Color color = new Color(r,g,b,a);
                int rgba =  color.getRGB()  ;

//                Color color = new Color(
//                        Byte.toUnsignedInt(pixelsBuffer[index])
//                        ,Byte.toUnsignedInt(pixelsBuffer[index+1])
//                        ,Byte.toUnsignedInt(pixelsBuffer[index+2])
//                        ,Byte.toUnsignedInt(pixelsBuffer[index+3])

                        // TODO  反向 BufferedImage.TYPE_BYTE_GRAY
//                        255-Byte.toUnsignedInt(pixelsBuffer[index])
//                        ,255-Byte.toUnsignedInt(pixelsBuffer[index+1])
//                        ,255-Byte.toUnsignedInt(pixelsBuffer[index+2])
//                        , 255-Byte.toUnsignedInt(pixelsBuffer[index+3])
//                );

                image.setRGB(i,j,rgba);

//               if(r!=0) osw_r.write(r/10 + ",");
//               if(g!=0) osw_g.write(g/10 + ",");
//               if(b!=0) osw_b.write(b/10 + ",");
               osw_a.write((r+g+b+a) /10+ "\n");
            }
        }

        image.flush();
        ImageIO.write(image, "PNG", new File(System.getProperty("user.dir")+ File.separator + "dbz.png"));

        osw_r.flush();
        osw_g.flush();
        osw_b.flush();
        osw_a.flush();

        osw_r.close();
        osw_g.close();
        osw_b.close();
        osw_a.close();

    }


    public static void  scan(byte[] data,int width,int height , int colors,int bitDepth) throws IOException {
//        int  width = w; // 解析IHDR数据块时得到的图像宽度
//        int height = h; // 解析IHDR数据块时得到的图像高度
//        int colors = c; // 解析IHDR数据块时得到的通道数
//        int bitDepth = d; // 解析IHDR数据块时得到的图像深度

        int bytesPerPixel = Math.max(1, colors * bitDepth / 8); // 每像素字节数
        System.out.println("bytesPerPixel = " + bytesPerPixel);
        int bytesPerRow = bytesPerPixel * width; // 每行字节数


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
                    System.out.println("未知过滤类型！" +readInt8(data, i));
            }
            offset += bytesPerRow;
        }
    }

    public static int readInt8(byte[] buffer, int offset) {
        return Byte.toUnsignedInt(buffer[offset]);
    }

    public static void  filterNone(byte[] scanline, int bytesPerPixel,int  bytesPerRow,int  offset) {
        for(int i=0; i<bytesPerRow; i++) {
            pixelsBuffer[offset + i] = Byte.toUnsignedInt(scanline[i]);
        }
    }

    public static  void filterSub(byte[] scanline, int bytesPerPixel,int  bytesPerRow,int  offset) {
        for(int  i=0; i<bytesPerRow; i++) {
            if(i < bytesPerPixel) {
                // 第一个像素，不作解析
                pixelsBuffer[offset + i] = Byte.toUnsignedInt(scanline[i]);
            } else {
                // 其他像素
                int a = pixelsBuffer[offset + i - bytesPerPixel];
                int value = Byte.toUnsignedInt(scanline[i]) + a;
                pixelsBuffer[offset + i] = value & 0xFF;
            }
        }
    }

    public static void filterUp(byte[] scanline, int bytesPerPixel,int  bytesPerRow,int  offset) {
        if(offset < bytesPerRow) {
            // 第一行，不作解析
            for(int i=0; i<bytesPerRow; i++) {
                pixelsBuffer[offset + i] = Byte.toUnsignedInt(scanline[i]);
            }
        } else {
            for(int i=0; i<bytesPerRow; i++) {
                int b = pixelsBuffer[offset + i - bytesPerRow];
                int value = Byte.toUnsignedInt(scanline[i]) + b;
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
                    pixelsBuffer[offset + i] =  Byte.toUnsignedInt(scanline[i]);
                } else {
                    // 其他像素
                    int a = pixelsBuffer[offset + i - bytesPerPixel];

                    int  value = Byte.toUnsignedInt(scanline[i]) + a>> 1; // 需要除以2
                    pixelsBuffer[offset + i] = (value & 0xFF);
                }
            }
        } else {
            for(int i=0; i<bytesPerRow; i++) {
                if(i < bytesPerPixel) {
                    // 第一个像素，只做Up
                    int b = pixelsBuffer[offset + i - bytesPerRow];

                    int value = Byte.toUnsignedInt(scanline[i])+ b >> 1; // 需要除以2
                    pixelsBuffer[offset + i] = (value & 0xFF);
                } else {
                    // 其他像素
                    int a = pixelsBuffer[offset + i - bytesPerPixel];
                    int b = pixelsBuffer[offset + i - bytesPerRow];
                    int value = Byte.toUnsignedInt(scanline[i])+ (a + b >> 1);
                    pixelsBuffer[offset + i] = (value & 0xFF);
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
                    pixelsBuffer[offset + i] = Byte.toUnsignedInt(scanline[i]);
                } else {
                    // 其他像素
                    int a = pixelsBuffer[offset + i - bytesPerPixel];
                    int value = Byte.toUnsignedInt(scanline[i]) + a;
                    pixelsBuffer[offset + i] = value & 0xFF;
                }
            }
        } else {
            for(int i=0; i<bytesPerRow; i++) {
                if(i < bytesPerPixel) {
                    // 第一个像素，只做Up
                    int b = pixelsBuffer[offset + i - bytesPerRow];
                    int value = Byte.toUnsignedInt(scanline[i])  + b;
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
                    int value = Byte.toUnsignedInt(scanline[i]) + pr;
                    pixelsBuffer[offset + i] = value & 0xFF;
                }
            }
        }
    }

    public static int byteArrayToInt(byte[] bytes,int st) {
        int value = 0;
        // 由高位到低位
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (bytes[st+i] & 0x000000FF) << shift;// 往高位游
        }
        return value;
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
}



