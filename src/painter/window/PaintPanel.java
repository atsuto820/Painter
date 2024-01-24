package painter.window;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import painter.main.Main;

/**
 * ペイント画像を表示するパネル
 * @author atsuto
 *
 */
public class PaintPanel extends JPanel {
	// 表示する画像
	public BufferedImage background;

	public PaintPanel() {
		super();
		try {
			// 背景画像の読み込み
			background = ImageIO.read(new File("img/background.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void paint(Graphics g) {
		super.paint(g);
		// レイヤーの画像
		ArrayList<BufferedImage> images = Main.pm.getImages();
		if (images != null) {
			// 背景画像の描画
			for (int i = 0; i < Main.pm.getWidth() / 64 + 1; i++) {
				for (int j = 0; j < Main.pm.getHeight() / 64 + 1; j++) {
					g.drawImage(background, 64 * i, 64 * j, this);
				}
			}
			
			// 下のレイヤーから
			for (int i = images.size() - 1; i > -1; i--) {
				if(Main.frame.lpanel.isVisibleLayer(i)) {
					// レイヤーの画像を描画
					g.drawImage(images.get(i), 0, 0, this);
					
					if(Main.frame.lpanel.getIndex() == i) {
						// 重ねて表示する内容を描画
						Main.pm.drawOver((Graphics2D)g);
					}
				}
			}
		}
	}
}
