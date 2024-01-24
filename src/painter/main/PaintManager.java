package painter.main;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import painter.window.ClickEvent;
import painter.window.SelectPanel;

public class PaintManager {
	private PaintTool pt;
	private ClickEvent ce;

	// 選択されている画像
	private BufferedImage selectedImage;
	// レイヤーの画像
	private ArrayList<BufferedImage> images;
	
	private int width, height;

	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public PaintTool getPT() {
		return pt;
	}
	
	public ClickEvent getCE() {
		return ce;
	}

	public ArrayList<BufferedImage> getImages() {
		return images;
	}
	
	public BufferedImage getImage() {
		return selectedImage;
	}

	public PaintManager() {
		super();
		ce = new ClickEvent();
	}
	
	/**
	 * 新しい画像にする
	 * @param image 新しい画像
	 * @param panel
	 */
	public void newImage(BufferedImage image, SelectPanel panel) {
		// レイヤーを新規作成し、画像をレイヤーに加える
		images = new ArrayList<BufferedImage>();
		images.add(image);
		selectedImage = image;
		
		// 画像の幅、高さを取得
		width = image.getWidth();
		height = image.getHeight();
		
		// 前の画像のPaintTool
		PaintTool bpt = (pt == null ? null : pt);
		pt = new PaintTool(image, ce, panel);
		if(bpt != null) {
			// 前の画像のPaintToolから色やモードは引き継ぐ
			pt.setColor(bpt.getColor());
			pt.setMode(bpt.getMode());
		}
	}

	/**
	 * 毎フレーム呼び出される処理
	 * @param click クリック情報 (ClickEvent参照)
	 */
	public void update(int[] click) {
		pt.update(click);
	}
	
	/**
	 * 描画対象に重ねて表示する内容を描画 (描画途中の直線など)
	 * @param g2 描画を行うGraphics2D
	 */
	public void drawOver(Graphics2D g2) {
		pt.drawOver(ce.getX(), ce.getY(), g2);
	}
	
	/**
	 * 新しい画像のレイヤーを加える
	 */
	public void addNewLayer() {
		// 加える場所のインデックス
		int index = images.indexOf(selectedImage);
		// レイヤー画像を新規作成して加える
		addLayer(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB), index);
	}
	
	/**
	 * 既存の画像のレイヤーを加える
	 */
	public void addLayer(BufferedImage image, int index) {
		// レイヤー画像を加える
		images.add(index, image);
		// 新しいレイヤーに変える
		changeLayer(index, false);
	}
	
	/**
	 * レイヤーを削除
	 * @param isSave 操作履歴を保存するか
	 */
	public void removeLayer(boolean isSave) {
		// 削除するレイヤーのインデックス
		int index = images.indexOf(selectedImage);
		// レイヤー画像を削除
		images.remove(selectedImage);
		
		// 削除後に選択されるレイヤー
		if(index == images.size()) {
			changeLayer(images.size() - 1, false);
		} else {
			changeLayer(index, false);
		}
		
		if(isSave) {
			// 操作履歴を記録
			pt.SaveRecent(PaintTool.LAYER_REMOVE, index, -1, null, null);
		}
	}
	
	/**
	 * レイヤーを移動
	 * @param isUp 上に移動か
	 * @param isSave 操作履歴を保存するか
	 */
	public void moveLayer(boolean isUp, boolean isSave) {
		// 移動先のインデックス
		int index = isUp ? images.indexOf(selectedImage) - 1 : images.indexOf(selectedImage) + 1;
		
		// 移動対象のレイヤー画像を削除し、移動後の位置に加える
		images.remove(selectedImage);
		images.add(index, selectedImage);
		// 移動後のレイヤーに変える
		changeLayer(index, false);
		
		if(isSave) {
			// 操作履歴を記録
			pt.SaveRecent(isUp ? PaintTool.LAYER_UP : PaintTool.LAYER_DOWN, -1, -1, null, null);
		}
	}
	
	/**
	 * 選択レイヤーを変える
	 * @param layer
	 * @param isSave 操作履歴を保存するか
	 */
	public void changeLayer(int layer, boolean isSave) {
		// 選択レイヤーを変える
		selectedImage = images.get(layer);
		pt.changeLayer(selectedImage);
		
		if(isSave) {
			// 操作履歴を記録
			pt.SaveRecent(PaintTool.LAYER_SELECT, images.indexOf(selectedImage), layer, null, null);
		}
	}
	
	/**
	 * 画像を閉じる
	 */
	public void close() {
		images = null;
		selectedImage = null;
		pt.close();
	}
}
