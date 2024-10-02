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
    public  static byte[] pixelsBuffer = new byte[30000];


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

        FileOutputStream fos = new FileOutputStream(new File("C:\\OSGeo4W\\1.txt"));
        OutputStreamWriter osw = new OutputStreamWriter(fos);

//        BufferedImage read = ImageIO.read(new File("C:\\OSGeo4W\\202409291024_VIL.png"));
        FileInputStream fis = new FileInputStream("C:\\OSGeo4W\\202409290324_dbz.png");
//        FileInputStream fis = new FileInputStream("C:\\OSGeo4W\\202409291024_VIL.png");
        int offset  = 0;
        byte[] bytes = fis.readAllBytes();
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
        pixelsBuffer = new byte[bytesPerPixel * w * h]; // 存储过滤后的像素数据

        // TODO scan
        scan(data, w, h, c, d);

//        ColorModel colorModel = ColorModel.getRGBdefault();
//        BufferedImage image = new BufferedImage(colorModel, colorModel.createCompatibleWritableRaster(w, h), false, null);
        BufferedImage image = new BufferedImage(w,h,BufferedImage.TYPE_BYTE_GRAY);

        Graphics2D g2d = image.createGraphics();

        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                int index = bytesPerPixel * ( j * w + i);
                //0.299 * R + 0.587 * G + 0.114 * B
                int r = Byte.toUnsignedInt(pixelsBuffer[index]);
                int g = Byte.toUnsignedInt(pixelsBuffer[index+1]);
                int b = Byte.toUnsignedInt(pixelsBuffer[index+2]);
                int alpha =Byte.toUnsignedInt(pixelsBuffer[index+3]);
                int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                if(gray >210){
                    System.out.println(gray);
                }
                Color color = new Color(
                        Byte.toUnsignedInt(pixelsBuffer[index])
                        ,Byte.toUnsignedInt(pixelsBuffer[index+1])
                        ,Byte.toUnsignedInt(pixelsBuffer[index+2])
                        ,Byte.toUnsignedInt(pixelsBuffer[index+3])

                        // TODO  反向 BufferedImage.TYPE_BYTE_GRAY
//                        255-Byte.toUnsignedInt(pixelsBuffer[index])
//                        ,255-Byte.toUnsignedInt(pixelsBuffer[index+1])
//                        ,255-Byte.toUnsignedInt(pixelsBuffer[index+2])
//                        , 255-Byte.toUnsignedInt(pixelsBuffer[index+3])
                );

                image.setRGB(i,j,color.getRGB());
                osw.write(color.getAlpha() + ",");
            }
        }

        image.flush();
        ImageIO.write(image, "PNG", new File("C:\\OSGeo4W\\dbz.png"));

        osw.flush();
        osw.close();

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
                byte a = pixelsBuffer[offset + i - bytesPerPixel];
                byte value = (byte) (scanline[i] + a);
                pixelsBuffer[offset + i] = (byte) (value & 0xFF);
            }
        }
    }

    public static void filterUp(byte[] scanline, int bytesPerPixel,int  bytesPerRow,int  offset) {
        if(offset < bytesPerRow) {
            // 第一行，不作解析
            for(int i=0; i<bytesPerRow; i++) {
                pixelsBuffer[offset + i] = scanline[i] ;
            }
        } else {
            for(int i=0; i<bytesPerRow; i++) {
                byte b = pixelsBuffer[offset + i - bytesPerRow];
                byte value = (byte) (scanline[i] + b);
                pixelsBuffer[offset + i] = (byte) (value & 0xFF);
            }
        }
    }

    public static void filterAverage(byte[] scanline, int bytesPerPixel,int  bytesPerRow,int  offset) {
        if(offset < bytesPerRow) {
            // 第一行，只做Sub
            for(int i=0; i<bytesPerRow; i++) {
                if(i < bytesPerPixel) {
                    // 第一个像素，不作解析
                    pixelsBuffer[offset + i] =  scanline[i] ;
                } else {
                    // 其他像素
                    byte a = pixelsBuffer[offset + i - bytesPerPixel];

                    byte value = (byte) (scanline[i]  + (a >> 1)); // 需要除以2
                    pixelsBuffer[offset + i] = (byte) (value & 0xFF);
                }
            }
        } else {
            for(int i=0; i<bytesPerRow; i++) {
                if(i < bytesPerPixel) {
                    // 第一个像素，只做Up
                    byte b = pixelsBuffer[offset + i - bytesPerRow];

                    byte value = (byte) (scanline[i] + (b >> 1)); // 需要除以2
                    pixelsBuffer[offset + i] = (byte) (value & 0xFF);
                } else {
                    // 其他像素
                    byte a = pixelsBuffer[offset + i - bytesPerPixel];
                    byte b = pixelsBuffer[offset + i - bytesPerRow];

                    byte value = (byte) (scanline[i]  + ((a + b) >> 1));
                    pixelsBuffer[offset + i] = (byte) (value & 0xFF);
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
                    byte a = pixelsBuffer[offset + i - bytesPerPixel];
                    byte value = (byte) (scanline[i] + a);
                    pixelsBuffer[offset + i] = (byte) (value & 0xFF);
                }
            }
        } else {
            for(int i=0; i<bytesPerRow; i++) {
                if(i < bytesPerPixel) {
                    // 第一个像素，只做Up
                    byte b = pixelsBuffer[offset + i - bytesPerRow];
                    byte value = (byte) (scanline[i]  + b);
                    pixelsBuffer[offset + i] = (byte) (value & 0xFF);
                } else {
                    // 其他像素
                    byte a = pixelsBuffer[offset + i - bytesPerPixel];
                    byte b = pixelsBuffer[offset + i - bytesPerRow];
                    byte c = pixelsBuffer[offset + i - bytesPerRow - bytesPerPixel];
                    byte p = (byte) (a + b - c);
                    byte pa = (byte) Math.abs(p - a);
                    byte pb = (byte) Math.abs(p - b);
                    byte pc = (byte) Math.abs(p - c);
                    byte pr;
                    if (pa <= pb && pa <= pc) pr = a;
                    else if (pb <= pc) pr = b;
                    else pr = c;
                    byte value = (byte) (scanline[i] + pr);
                    pixelsBuffer[offset + i] = (byte) (value & 0xFF);
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



