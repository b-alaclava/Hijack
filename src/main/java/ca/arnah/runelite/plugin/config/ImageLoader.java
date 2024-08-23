package ca.arnah.runelite.plugin.config;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class ImageLoader {
    public static BufferedImage loadIcon(String githubPath) throws IOException {
            try {
                // Define the URL from which to load the image
                URL imageUrl = new URL(githubPath);

                // Read the image from the URL
                BufferedImage image = ImageIO.read(imageUrl);

                if (image != null) {
                    return image;
                } else {
                    return new BufferedImage(10,10,BufferedImage.TYPE_BYTE_GRAY);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
    }
}