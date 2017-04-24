package wftp.utils;

import java.awt.image.BufferedImage;
import java.awt.image.DirectColorModel;

import javax.swing.ImageIcon;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.PlatformUI;

public class ImageConvertor {

	public static Image getSWTImageFromSwing(ImageIcon imageIcon) {
		if (imageIcon.getImage() instanceof BufferedImage) {
			BufferedImage bufferedImage = (BufferedImage) imageIcon.getImage();
			return getSwtImageFromBufferedImage(bufferedImage);
		} else {
			return null;
		}
	}

	public static Image getSwtImageFromBufferedImage(BufferedImage bufferedImage) {
		if (bufferedImage == null)
			return null;
		DirectColorModel colorModel = (DirectColorModel) bufferedImage
				.getColorModel();
		PaletteData palette = new PaletteData(colorModel.getRedMask(),
				colorModel.getGreenMask(), colorModel.getBlueMask());
		ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage
				.getHeight(), colorModel.getPixelSize(), palette);

		// ����ÿ�����ص����ɫ��Alphaֵ
		for (int y = 0; y < data.height; y++) {
			for (int x = 0; x < data.width; x++) {
				int rgb = bufferedImage.getRGB(x, y);
				int pixel = palette.getPixel(new RGB((rgb >> 16) & 0xFF,
						(rgb >> 8) & 0xFF, rgb & 0xFF));
				data.setPixel(x, y, pixel);
				if (colorModel.hasAlpha()) {
					data.setAlpha(x, y, (rgb >> 24) & 0xFF);
				}
			}
		}

		// ����Image����
		Image swtImage = new Image(PlatformUI.getWorkbench().getDisplay(), data);
		return swtImage;
	}
}