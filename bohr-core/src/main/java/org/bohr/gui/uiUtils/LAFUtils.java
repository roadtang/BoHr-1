package org.bohr.gui.uiUtils;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.bohr.gui.laf.DefaultButtonUI;
import org.bohr.gui.laf.RoundRectPanel;
import org.bohr.gui.layout.TableLayout;

public class LAFUtils {

	public static void setNotFillDefaultButtonUI(AbstractButton b) {
		b.setUI(new DefaultButtonUI() {
			@Override
			public boolean isFillBackGroup() {
				return false;
			}
		});
	}


	public static BufferedImage createEmptyImage(int width, int height) {
		BufferedImage temp = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		return temp;
	}

	public static ImageIcon createEmptyIconImage(int width, int height) {
		BufferedImage temp = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		return new ImageIcon(temp);
	}

	public static ImageIcon createEmptyIconImage(int width, int height, int color) {
		BufferedImage temp = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = temp.createGraphics();
		g.setColor(new Color(color));
		g.fillRect(0, 0, width, height);
		g.dispose();
		return new ImageIcon(temp);
	}
	
	public static ImageIcon createLineIconImage(int width, int height, int color,int linecolor,int lineHeight,int leftright) {
		BufferedImage temp = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = temp.createGraphics();
		g.setColor(new Color(color));
		g.fillRect(0, 0, width, height);
		
		
		g.setColor(new Color(linecolor));
		g.fillRect(leftright, height-lineHeight-1, width-leftright-leftright, lineHeight);
		
		g.dispose();
		return new ImageIcon(temp);
	}


	public static double[] getDoubleArray(double... args) {
		return args;
	}

	/**
	 *
	 * 
	 * @param img
	 * @return
	 */
	public static BufferedImage image2BufferedImage(Image img) {
		BufferedImage temp = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = temp.createGraphics();
		g.drawImage(img, 0, 0, null);
		g.dispose();
		return temp;
	}

	/**
	 *
	 * 
	 * @param img
	 * @return
	 */
	public static Color[][] getImageColorValue(Image img) {
		BufferedImage temp = image2BufferedImage(img);
		int w = temp.getWidth();
		int h = temp.getHeight();
		Color[][] data = new Color[h][w];
		for (int row = 0; row < h; row++) {
			for (int col = 0; col < w; col++) {
				int argb = temp.getRGB(col, row);
				data[row][col] = new Color(argb, true);
			}
		}
		return data;
	}

	private static boolean tolerantCal(int a, int b, int t) {
		int min = a - t;
		int max = a + t;
		if (b >= min && b <= max) {
			return true;
		}
		return false;
	}

	/**
	 *
	 */
	public static BufferedImage changeImageColor(Image img, Color srcColor, Color destColor, int tolerant) {
		Color[][] srcData = getImageColorValue(img);
		int w = img.getWidth(null);
		int h = img.getHeight(null);

		Color[][] data = new Color[h][w];
		for (int row = 0; row < h; row++) {
			for (int col = 0; col < w; col++) {
				Color curr = srcData[row][col];
				int a = curr.getAlpha();
				int r = curr.getRed();
				int g = curr.getGreen();
				int b = curr.getBlue();

				boolean br = tolerantCal(srcColor.getRed(), r, tolerant);
				boolean bg = tolerantCal(srcColor.getGreen(), g, tolerant);
				boolean bb = tolerantCal(srcColor.getBlue(), b, tolerant);

				if (br && bg && bb) {
					r = destColor.getRed();
					g = destColor.getGreen();
					b = destColor.getBlue();
				}

				data[row][col] = new Color(r, g, b, a);
			}
		}

		BufferedImage temp = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_4BYTE_ABGR);
		for (int row = 0; row < h; row++) {
			for (int col = 0; col < w; col++) {
				temp.setRGB(col, row, data[row][col].getRGB());
			}
		}
		return temp;
	}

	public static BufferedImage getButtonImage1(ImageIcon background, ImageIcon icon, String iconColor,
			String buttonName, String buttonNameColor, Font font) {
		int width = background.getIconWidth();
		int height = background.getIconHeight();
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();

		g.drawImage(background.getImage(), 0, 0, null);

		if (iconColor != null) {
			icon = new ImageIcon(changeImageColor(icon.getImage(), ColorUtils.createColor("8E939D"),
					ColorUtils.createColor(iconColor), 30));
		}

		if (icon != null) {
			g.drawImage(icon.getImage(), 0, 0, null);
		}

		if (buttonNameColor != null) {
			g.setColor(ColorUtils.createColor(buttonNameColor));
		}

		if (icon == null) {
			GraphicsUtils.drawString(g, font, buttonName, width / 2, height / 2, 4);
		} else {
			GraphicsUtils.drawString(g, font, buttonName, icon.getIconWidth() + 2, 2, 0);
		}

		g.dispose();

		return image;
	}
	
	public static ImageIcon getButtonImageTop(ImageIcon background, ImageIcon icon, String iconColor,
			String buttonName, String buttonNameColor, Font font) {
		int width = background.getIconWidth();
		int height = background.getIconHeight();
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();

		g.drawImage(background.getImage(), 0, 0, null);

		if (iconColor != null) {
			icon = new ImageIcon(changeImageColor(icon.getImage(), ColorUtils.createColor("8E939D"),
					ColorUtils.createColor(iconColor), 30));
		}

		if (icon != null) {
			g.drawImage(icon.getImage(), 0, 0, null);
		}

		if (buttonNameColor != null) {
			g.setColor(ColorUtils.createColor(buttonNameColor));
		}

		if (icon == null) {
			GraphicsUtils.drawString(g, font, buttonName, width / 2,1, 1);
		} else {
			GraphicsUtils.drawString(g, font, buttonName, icon.getIconWidth() + 2, 2, 0);
		}

		g.dispose();

		return new ImageIcon(image) ;
	}
	


	public static BufferedImage getButtonImage(ImageIcon background, ImageIcon icon, String iconColor,
			String buttonName, String buttonNameColor, Font font) {
		int width = background.getIconWidth();
		int height = background.getIconHeight();
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();

		g.drawImage(background.getImage(), 0, 0, null);

		if (iconColor != null) {
			icon = new ImageIcon(changeImageColor(icon.getImage(), ColorUtils.createColor("8E939D"),
					ColorUtils.createColor(iconColor), 30));
		}

		g.drawImage(icon.getImage(), 11, 25, null);

		if (buttonNameColor != null) {
			g.setColor(ColorUtils.createColor(buttonNameColor));
		}

		GraphicsUtils.drawString(g, font, buttonName, 55, 30 - 7, 0);

		g.dispose();

		return image;
	}


	public static JLabel createLabel(String title, int style, float size, int color) {
		JLabel label = new JLabel(title);
		label.setFont(label.getFont().deriveFont(style, size));
		label.setForeground(new Color(color));
		return label;
	}


	public static JPanel createPanel(double[] colSize, double[] rowSize) {
		JPanel panel = new JPanel();
		panel.setOpaque(false);
		double[][] tableSize = { colSize, rowSize };
		TableLayout layout = new TableLayout(tableSize);
		panel.setLayout(layout);
		return panel;
	}

	public static JPanel createRoundRectanglePanel(double[] colSize, double[] rowSize) {
		JPanel panel = new RoundRectPanel();
		panel.setOpaque(true);
		double[][] tableSize = { colSize, rowSize };
		TableLayout layout = new TableLayout(tableSize);
		panel.setLayout(layout);
		panel.setBackground(Color.WHITE);
		return panel;
	}

	public static ImageIcon getButtonImageIcon1(ImageIcon background, ImageIcon icon, String iconColor,
			String buttonName, String buttonNameColor, Font font) {
		BufferedImage img = getButtonImage1(background, icon, iconColor, buttonName, buttonNameColor, font);
		return new ImageIcon(img);
	}

	public static ImageIcon getButtonImageIcon(ImageIcon background, ImageIcon icon, String iconColor,
			String buttonName, String buttonNameColor, Font font) {
		BufferedImage img = getButtonImage(background, icon, iconColor, buttonName, buttonNameColor, font);
		return new ImageIcon(img);
	}


}
