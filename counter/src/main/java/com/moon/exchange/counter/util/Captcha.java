package com.moon.exchange.counter.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;

/**
 * @author Chanmoey
 * @date 2023年01月16日
 */
public class Captcha {

    private String dict = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public void setDict(String dict) {
        this.dict = dict;
    }

    private double noiseRate = 0.01;

    public void setNoiseRate(double rate) {
        this.noiseRate = rate;
    }

    private final String code;

    private final BufferedImage bufferedImage;

    private final Random random = new Random();

    public Captcha(int width, int height, int codeCount, int lineCount) {

        // 生成图像
        bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // 设置背景色
        Graphics graphics = bufferedImage.getGraphics();
        graphics.setColor(getRandColor(200, 250));
        graphics.fillRect(0, 0, width, height);

        // 生成干扰线、噪点
        for (int i = 0; i < lineCount; i++) {
            int xs = random.nextInt(width);
            int ys = random.nextInt(height);
            int xe = xs + random.nextInt(width);
            int ye = ys + random.nextInt(height);
            graphics.setColor(getRandColor(1, 255));
            graphics.drawLine(xs, ys, xe, ye);
        }


        int area = (int) (noiseRate * width * height);
        for (int i = 0; i < area; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            bufferedImage.setRGB(x, y, random.nextInt(255));
        }

        // 添加字符
        this.code = randomStr(codeCount);
        Font font = new Font("Fixedsys", Font.BOLD, height - 5);
        graphics.setFont(font);
        int fontWidth = width / codeCount;
        int fontHeight = height - 5;
        for (int i = 0; i < codeCount; i++) {
            String str = this.code.substring(i, i + 1);
            graphics.setColor(getRandColor(1, 255));
            graphics.drawString(str, i * fontWidth + 3, fontHeight - 3);
        }
    }

    /**
     * 生成随机字符
     *
     * @param codeCount 字符数
     * @return 随机字符
     */
    private String randomStr(int codeCount) {
        StringBuilder sb = new StringBuilder();
        int len = dict.length() - 1;

        double r;
        for (int i = 0; i < codeCount; i++) {
            r = (Math.random()) * len;
            sb.append(dict.charAt((int) r));
        }

        return sb.toString();
    }

    private Color getRandColor(int fc, int bc) {
        fc = Math.min(255, fc);
        fc = Math.max(0, fc);
        bc = Math.min(255, bc);
        bc = Math.max(0, bc);

        int r = fc + random.nextInt(bc - fc);
        int g = fc + random.nextInt(bc - fc);
        int b = fc + random.nextInt(bc - fc);

        return new Color(r, g, b);
    }

    public String getCode() {
        return code.toLowerCase();
    }

    public String getBase64ByteStr() throws IOException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", outputStream);

        String s = Base64.getEncoder().encodeToString(outputStream.toByteArray());
        s = s.replace("\n", "")
                .replace("\r", "");

        return "data:image/jpg;base64," + s;
    }
}
