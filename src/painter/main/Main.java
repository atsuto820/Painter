package painter.main;

import java.awt.image.BufferedImage;

import painter.window.MainFrame;

public class Main {
	public static MainFrame frame;
	public static PaintManager pm;
	public static final int FPS = 30;

	public static void main(String[] args) {
		pm = new PaintManager();
		frame = new MainFrame(pm.getCE());
		while (true) {
			// このフレームの開始時間
			long startTime = System.currentTimeMillis();

			// イベントチェック
			frame.checkEvent();
			// クリック情報更新、PaintManager更新
			if (pm.getImage() != null) {
				pm.update(frame.updateClick());
			}

			// 再描画
			frame.panel.repaint();
			
			try {
				// このフレームの処理経過時間
				long passTime = System.currentTimeMillis() - startTime;
				if (passTime < 1000 / FPS) {
					// FPSに合わせた時間待つ
					Thread.sleep(1000 / FPS - passTime);
				}
			} catch (InterruptedException e) {
			} catch (IllegalArgumentException e) {
			}
		}
	}

	/**
	 * 新しい画像を開く
	 * @param image 開く画像
	 */
	public static void newImage(BufferedImage image) {
		frame.panel.setBounds(0, 0, image.getWidth(), image.getHeight());
		pm.newImage(image, frame.spanel);
		frame.newImage();
	}
	
	public static BufferedImage getImage() {
		return pm.getImage();
	}
}
