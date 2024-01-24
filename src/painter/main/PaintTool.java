package painter.main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Random;

import javax.imageio.ImageIO;

import painter.window.ClickEvent;
import painter.window.SelectPanel;

public class PaintTool {
	private BufferedImage image;	// 描画対象の画像
	private Graphics2D g;
	
	private BufferedImage exImg;	// 処理などに使用する追加の画像
	private Graphics2D exG;

	private BufferedImage[] stamps;	// スタンプの画像

	// 操作履歴の現在位置と上限
	private int now, limit;
	// 履歴の画像
	private BufferedImage[] recents;
	// 操作履歴の追加情報
	private int[] operations;	// 行った操作
	public static final int DRAW = 0;			// レイヤー操作以外の操作
	public static final int LAYER_SELECT = 1;
	public static final int LAYER_ADD = 2;
	public static final int LAYER_REMOVE = 3;
	public static final int LAYER_VISIBLE = 4;
	public static final int LAYER_NAME = 5;
	public static final int LAYER_UP = 6;
	public static final int LAYER_DOWN = 7;
	private int[] exInt1, exInt2;
	private String[] exStr1, exStr2;
	/*
	 * 操作履歴の記録方式
	 * 
	 * 通常の描画 : recentsに記録
	 * レイヤー選択 : レイヤー番号 exInt1 -> exInt2
	 * レイヤー追加 : レイヤー名 exStr1
	 */

	private SelectPanel panel;
	private ClickEvent ce;
	private Color selectedColor;	// 選択色
	
	private int mode;	// 描画モード
	public static final int PEN = 0;
	public static final int RAINBOW_PEN = 1;
	public static final int ERASER = 2;
	public static final int LINE = 3;
	public static final int POLYGON = 4;
	public static final int RECT = 5;
	public static final int OVAL = 6;
	public static final int FILL = 7;
	public static final int TEXT = 8;
	public static final int STAMP = 9;

	private int thickness;	// ペンの太さ
	private int shape;		// ペンの形
	public static final int ROUND = 0;
	public static final int SQUARE = 1;
	public static final int DASHED = 2;
	
	// 描画途中の多角形の点の座標
	private ArrayList<int[]> xys;

	public PaintTool(BufferedImage image, ClickEvent ce, SelectPanel panel) {
		super();
		
		// 変数の初期化
		this.image = image;
		g = (Graphics2D) image.getGraphics();
		this.panel = panel;
		this.ce = ce;
		mode = PEN;
		now = 0;
		limit = 0;
		xys = new ArrayList<int[]>();
		exImg = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		exG = (Graphics2D) exImg.getGraphics();
		stamps = new BufferedImage[14];
		try {
			for (int i = 0; i < stamps.length; i++) {
				// スタンプ画像の読み込み
				stamps[i] = ImageIO.read(new File("img/stamp/" + i + ".png"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		recents = new BufferedImage[15];
		for (int i = 0; i < recents.length; i++) {
			recents[i] = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		}
		operations = new int[15];
		exInt1 = new int[15];
		exInt2 = new int[15];
		exStr1 = new String[15];
		exStr2 = new String[15];

		setColor(Color.BLACK);
		setMode(0);
	}

	/**
	 * レイヤーを変える
	 * @param newImage 新しいレイヤー画像
	 */
	public void changeLayer(BufferedImage newImage) {
		image = newImage;
		
		// 色とペン設定は引き継ぐ
		Graphics2D newG = (Graphics2D)(newImage.getGraphics());
		newG.setColor(g.getColor());
		newG.setStroke(g.getStroke());
		g = newG;
	}

	/**
	 * 画像を閉じる
	 */
	public void close() {
		// 操作履歴をリセット
		now = 0;
		limit = 0;
	}

	/**
	 * 現在の色を取得
	 * @return
	 */
	public Color getColor() {
		return selectedColor;
	}

	/**
	 * 色を設定
	 * @param color
	 */
	public void setColor(Color color) {
		this.selectedColor = color;
	}

	/**
	 * 現在の太さを取得
	 * @return
	 */
	public int getThickness() {
		return (int) ((BasicStroke) g.getStroke()).getLineWidth();
	}

	/**
	 * 太さの設定された値を取得し設定
	 */
	public void setThickness() {
		if (mode == LINE || mode == POLYGON) {
			if (panel.getThickness() != thickness || panel.getLine() != shape) {
				thickness = panel.getThickness();
				shape = panel.getLine();
				setPenShape(shape, thickness);
			}
		} else {
			if (panel.getThickness() != thickness) {
				thickness = panel.getThickness();
				setPenShape(0, thickness);
			}
		}
	}

	/**
	 * 現在のペンの形を取得
	 * @return
	 */
	public int getPenShape() {
		if (((BasicStroke) g.getStroke()).getEndCap() == BasicStroke.CAP_ROUND) {
			return ROUND;
		} else if (((BasicStroke) g.getStroke()).getEndCap() == BasicStroke.CAP_SQUARE) {
			return SQUARE;
		} else {
			return DASHED;
		}
	}

	/**
	 * ペンの形を設定
	 * @param shape
	 * @param thickness
	 */
	public void setPenShape(int shape, int thickness) {
		switch (shape) {
		case ROUND:
			g.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			break;
		case SQUARE:
			g.setStroke(new BasicStroke(thickness, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND));
			break;
		case DASHED:
			g.setStroke(new BasicStroke(thickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1.0f,
					new float[] { thickness }, 0));
			break;
		}
	}

	/**
	 * 現在の描画モードを取得
	 * @return
	 */
	public int getMode() {
		return mode;
	}

	/**
	 * 描画モードを設定
	 * @param mode
	 */
	public void setMode(int mode) {
		if (this.mode != mode) {
			if (this.mode == LINE || this.mode == POLYGON || this.mode == RECT || this.mode == OVAL) {
				// 描画途中の多角形の点をクリア
				xys.clear();
				setPenShape(0, thickness);
			}
			if (mode == LINE || mode == POLYGON || mode == RECT || mode == OVAL) {
				setPenShape(panel.getLine(), thickness);
			}
			this.mode = mode;
		}
		Main.frame.spanel.changeMode(mode);
	}

	/**
	 * 元に戻す
	 */
	public void Undo() {
		// レイヤー操作の復元
		// 操作履歴の逆の操作を行う
		switch (operations[now]) {
		case DRAW:
			// 操作履歴の画像を復元
			clearImage(null, false);
			Main.pm.getImage().getGraphics().drawImage(recents[now + 1], 0, 0, null);
			break;
		case LAYER_SELECT:
			// レイヤーの選択
			Main.pm.changeLayer(exInt1[now], false);
			Main.frame.lpanel.selectLayer(exInt1[now]);
			break;
		case LAYER_ADD:
			// レイヤーの追加 -> 削除
			Main.pm.removeLayer(false);
			Main.frame.lpanel.removeLayer();
			break;
		case LAYER_REMOVE:
			// レイヤーの削除 -> 追加
			Main.pm.addLayer(recents[now], exInt1[now]);
			Main.frame.lpanel.addLayer(exStr1[now], exInt1[now]);
			break;
		case LAYER_VISIBLE:
			// レイヤーの可視切り替え
			Main.frame.lpanel.changeLayerVisible(exInt1[now]);
			break;
		case LAYER_NAME:
			// レイヤー名変更
			Main.frame.lpanel.setLayerName(exStr1[now], false);
			break;
		case LAYER_UP:
			// レイヤー上移動 -> 下移動
			Main.pm.moveLayer(false, false);
			Main.frame.lpanel.moveLayer(false);
			break;
		case LAYER_DOWN:
			// レイヤー下移動 -> レイヤー上移動
			Main.pm.moveLayer(true, false);
			Main.frame.lpanel.moveLayer(true);
			break;
		}

		now++;
		if (now == limit) {
			// 操作履歴の限界 (これ以上元に戻せない)
			Main.frame.mis2[0].setEnabled(false);
		}
		Main.frame.mis2[1].setEnabled(true);
	}

	/**
	 * やり直す
	 */
	public void Redo() {
		now--;
		
		// 操作履歴の復元
		// 操作履歴の操作を行う
		switch (operations[now]) {
		case DRAW:
			// 操作履歴の画像を復元
			clearImage(null, false);
			Main.pm.getImage().getGraphics().drawImage(recents[now], 0, 0, null);
			break;
		case LAYER_SELECT:
			// レイヤーの選択
			Main.pm.changeLayer(exInt2[now], false);
			Main.frame.lpanel.selectLayer(exInt2[now]);
			break;
		case LAYER_ADD:
			// レイヤーの追加
			Main.pm.addNewLayer();
			Main.frame.lpanel.addLayer(exStr1[now]);
			break;
		case LAYER_REMOVE:
			// レイヤーの削除
			Main.pm.removeLayer(false);
			Main.frame.lpanel.removeLayer();
			break;
		case LAYER_VISIBLE:
			// レイヤーの可視切り替え
			Main.frame.lpanel.changeLayerVisible(exInt1[now]);
			break;
		case LAYER_NAME:
			// レイヤー名変更
			Main.frame.lpanel.setLayerName(exStr2[now], false);
			break;
		case LAYER_UP:
			// レイヤー上移動
			Main.pm.moveLayer(true, false);
			Main.frame.lpanel.moveLayer(true);
			break;
		case LAYER_DOWN:
			// レイヤー下移動
			Main.pm.moveLayer(false, false);
			Main.frame.lpanel.moveLayer(false);
			break;
		}
		
		Main.frame.mis2[0].setEnabled(true);
		if (now == 0) {
			// 操作履歴の限界 (これ以上やり直せない)
			Main.frame.mis2[1].setEnabled(false);
		}
	}

	/**
	 * 操作履歴の現在位置を最新にする
	 */
	private void UpdateRecent() {
		if (now == 0) {
			// 操作履歴の現在位置が最新の位置
			BufferedImage last = recents[recents.length - 1];	// 最後の操作履歴 (破棄される)
			for (int i = 0; i < recents.length - 1; i++) {
				// 操作履歴をシフト
				recents[recents.length - i - 1] = recents[recents.length - i - 2];
				
				operations[exStr2.length - i - 1] = operations[exStr2.length - i - 2];
				exInt1[exInt1.length - i - 1] = exInt1[exInt1.length - i - 2];
				exInt2[exInt2.length - i - 1] = exInt2[exInt2.length - i - 2];
				exStr1[exStr1.length - i - 1] = exStr1[exStr1.length - i - 2];
				exStr2[exStr2.length - i - 1] = exStr2[exStr2.length - i - 2];
			}
			recents[0] = last;

			// 操作履歴の限界点を更新
			limit += (limit == recents.length - 1 ? 0 : 1);
		} else {
			// 操作履歴の現在位置が最新の位置でない
			for (int i = 0; i < recents.length - now; i++) {
				// 操作履歴を移動
				BufferedImage temp = recents[now + i];
				for (int j = 0; j < now - 1; j++) {
					recents[now + i - j] = recents[now + i - j - 1];
				}
				recents[i + 1] = temp;
				
				operations[i + 1] = operations[now + i];
				exInt1[i + 1] = exInt1[now + i];
				exInt2[i + 1] = exInt2[now + i];
				exStr1[i + 1] = exStr1[now + i];
				exStr2[i + 1] = exStr2[now + i];
			}

			// 操作履歴の限界点を更新
			limit -= (now - 1);
		}
	}

	/**
	 * レイヤー操作を操作履歴に記録
	 * @param operation
	 * @param num
	 * @param num2
	 * @param str
	 * @param str2
	 */
	public void SaveRecent(int operation, int num, int num2, String str, String str2) {
		UpdateRecent();

		operations[0] = operation;
		switch (operation) {
		case DRAW:
			// 描画
			clearImage(recents[0], false);										// 最後の操作履歴を破棄
			recents[0].getGraphics().drawImage(Main.pm.getImage(), 0, 0, null);	// 操作履歴画像を記録
			break;
		case LAYER_SELECT:
			// レイヤー選択
			exInt1[0] = num;
			exInt2[0] = num2;
			break;
		case LAYER_ADD:
			// レイヤー追加
			exStr1[0] = str;
			break;
		case LAYER_REMOVE:
			// レイヤー削除
			clearImage(recents[0], false);
			recents[0].getGraphics().drawImage(Main.pm.getImage(), 0, 0, null);
			exInt1[0] = num;
			exStr1[0] = Main.frame.lpanel.getLayerName();
			break;
		case LAYER_VISIBLE:
			// レイヤー可視変更
			exInt1[0] = num;
			break;
		case LAYER_NAME:
			// レイヤー名前変更
			exStr1[0] = str;
			exStr2[0] = str2;
			break;
		}
		
		now = 0;
		Main.frame.mis2[0].setEnabled(true);
		Main.frame.mis2[1].setEnabled(false);
	}

	/**
	 * 毎フレーム行われる処理
	 * @param click クリック情報 (ClickEvent参照)
	 */
	public void update(int[] click) {
		// ペンの色と太さを更新
		g.setColor(selectedColor);
		setThickness();

		// 描画処理
		switch (mode) {
		case PEN:
		case RAINBOW_PEN:
			draw(click);
			break;
		case ERASER:
			erase(click);
			break;
		case LINE:
			drawLine(click);
			break;
		case POLYGON:
			drawPolygon(click);
			break;
		case RECT:
			drawRect(click);
			break;
		case OVAL:
			drawOval(click);
			break;
		case FILL:
			fill(click);
			break;
		case STAMP:
			stamp(click);
			break;
		}
	}

	/**
	 * 描画対象に重ねて表示する内容を描画 (描画途中の直線など)
	 * @param x マウス位置x
	 * @param y マウス位置y
	 * @param g2 描画を行うGraphics2D
	 */
	public void drawOver(int x, int y, Graphics2D g2) {
		// 色を設定 (選択色を半透明にした色、透明度は時間ごとに変化)
		g2.setColor(new Color(selectedColor.getRed(), selectedColor.getGreen(), selectedColor.getBlue(),
				(int) (Math.abs(System.currentTimeMillis() % 1000 - 500.0) / 500 * 128)));
		g2.setStroke(g.getStroke());
		
		if (mode == LINE || mode == POLYGON) {
			// 線分モード、多角形モード
			if(xys.size() > 0) {
				for (int i = 0; i < xys.size() - 1; i++) {
					// 頂点同士を結んでいく
					g2.drawLine(xys.get(i)[0], xys.get(i)[1], xys.get(i + 1)[0], xys.get(i + 1)[1]);
				}
				// 最後の頂点とマウス位置を結ぶ
				g2.drawLine(xys.get(xys.size() - 1)[0], xys.get(xys.size() - 1)[1], x, y);
			}
		} else if (mode == RECT) {
			// 長方形モード
			if(xys.size() > 0) {
				// 長方形の左上の点
				int lux = Math.min(xys.get(0)[0], x), luy = Math.min(xys.get(0)[1], y);
				// 長方形のサイズ
				int sizex = Math.abs(x - xys.get(0)[0]), sizey = Math.abs(y - xys.get(0)[1]);
				if (panel.isSquare()) {
					// 正方形モード
					if (sizex > sizey) {
						sizey = sizex;
						if (xys.get(0)[1] > y) {
							luy = xys.get(0)[1] - sizey;
						}
					} else {
						sizex = sizey;
						if (xys.get(0)[0] > x) {
							lux = xys.get(0)[0] - sizex;
						}
					}
				}
				if (panel.isCornerCircle()) {
					// 角を丸めるモード
					
					// 長方形を描画
					g2.drawRect(lux, luy, sizex, sizey);
					
					// 塗りつぶすモード
					if (panel.isFill()) {
						// 塗りつぶす
						g2.fillRect(lux + thickness / 2, luy + thickness / 2, sizex - thickness, sizey - thickness);
					}
				} else {
					// 角をとがらせるモード
					
					// 塗りつぶすモード
					if (panel.isFill()) {
						// 長方形を塗り潰す
						g2.fillRect(lux - thickness / 2, luy - thickness / 2, sizex + thickness, sizey + thickness);
					} else {
						// 辺ごとに長方形を描画
						g2.fillRect(lux - thickness / 2, luy - thickness / 2, sizex, thickness);
						g2.fillRect(lux - thickness / 2, luy + thickness / 2, thickness, sizey);
						g2.fillRect(lux + thickness / 2, luy + sizey - thickness / 2, sizex, thickness);
						g2.fillRect(lux + sizex - thickness / 2, luy - thickness / 2, thickness, sizey);
					}
				}
			}
		} else if (mode == OVAL) {
			// 楕円の左上の基準点
			if(xys.size() > 0) {
				int lux = Math.min(xys.get(0)[0], x), luy = Math.min(xys.get(0)[1], y);
				// 楕円のサイズ
				int sizex = Math.abs(x - xys.get(0)[0]), sizey = Math.abs(y - xys.get(0)[1]);
				if (panel.isSquare()) {
					// 円モード
					if (sizex > sizey) {
						sizey = sizex;
						if (xys.get(0)[1] > y) {
							luy = xys.get(0)[1] - sizey;
						}
					} else {
						sizex = sizey;
						if (xys.get(0)[0] > x) {
							lux = xys.get(0)[0] - sizex;
						}
					}
				}
				
				// 塗りつぶすモード
				if (panel.isFill()) {
					g2.fillOval(lux - thickness / 2, luy - thickness / 2, sizex + thickness, sizey + thickness);
				} else {
					g2.drawOval(lux, luy, sizex, sizey);
				}
			}
		} else if (mode == TEXT) {
			// テキストモード
			
			// フォント設定
			g2.setFont(new Font(panel.getMyFont().getName(), Font.PLAIN, panel.getFontSize()));
			
			// 改行ごとに文字を分割
			String[] strs = panel.getText().split("\n");
			int fontHeight = g2.getFontMetrics().getHeight();	// 文字の縦幅
			for (int i = 0; i < strs.length; i++) {
				// 改行ごとに位置yを下げる
				g2.drawString(strs[i], panel.getMyX(), panel.getMyY() + fontHeight * (i + 1));
			}
		}
	}

	/**
	 * ペン描画モードでの処理
	 * @param click クリック情報 (ClickEvent参照)
	 */
	public void draw(int[] click) {
		// 書き終わり判定
		if (click[0] == -1) {
			if (click[2] != -1) {
				// 描画履歴を記録
				SaveRecent(DRAW, -1, -1, null, null);
			}
			return;
		}
		
		if (mode == RAINBOW_PEN) {
			// 虹色ペンなら色を時間とともに変化
			g.setColor(new Color(Color.HSBtoRGB(Math.abs(System.currentTimeMillis() % 3000 / 3000f), 1, 1)));
		}
		if (click[2] == -1) {
			// 書き始め
			g.drawLine(click[0], click[1], click[0], click[1]);
		} else {
			g.drawLine(click[2], click[3], click[0], click[1]);
		}
	}

	/**
	 * 消しゴムモードでの処理
	 * @param click クリック情報 (ClickEvent参照)
	 */
	public void erase(int[] click) {
		// 消し終わり判定
		if (click[0] == -1) {
			if (click[2] != -1) {
				// 描画履歴を記録
				SaveRecent(DRAW, -1, -1, null, null);
			}
			return;
		}
		
		// 一時的にexImgに黒色で描画する
		exG.setColor(Color.BLACK);
		exG.setStroke(g.getStroke());
		if (click[2] == -1) {
			// 書き始め
			exG.drawLine(click[0], click[1], click[0], click[1]);
		} else {
			exG.drawLine(click[2], click[3], click[0], click[1]);
		}
		
		// exImgの黒色部分でimgを透明にする
		int clear = new Color(0, 0, 0, 0).getRGB();	// 透明のRGB値
		for (int i = 0; i < image.getHeight(); i++) {
			for (int j = 0; j < image.getWidth(); j++) {
				if (exImg.getRGB(j, i) != clear) {
					// exImgの黒色部分
					image.setRGB(j, i, clear);	// imgを透明に
					exImg.setRGB(j, i, clear);	// exImgも透明に
				}
			}
		}
	}

	/**
	 * 線分モードでの処理
	 * @param click クリック情報 (ClickEvent参照)
	 */
	public void drawLine(int[] click) {
		// クリックされたら
		if (click[0] == -1 && click[2] != -1) {
			if (xys.size() == 0) {
				// 線分の始点情報を格納
				xys.add(new int[] { click[2], click[3] });
			} else {
				// 線分を描画
				g.drawLine(xys.get(0)[0], xys.get(0)[1], click[2], click[3]);
				xys.clear();
				
				// 描画履歴を記録
				SaveRecent(DRAW, -1, -1, null, null);
			}
			ce.clear();
		}
	}

	/**
	 * 多角形モードでの処理
	 * @param click クリック情報 (ClickEvent参照)
	 */
	public void drawPolygon(int[] click) {
		// クリックされたら
		if (click[0] == -1 && click[2] != -1) {
			if (click[1] == 0) {
				// 右クリック
				// 多角形を描画
				if (xys.size() > 0) {
					for (int i = 0; i < xys.size() - 1; i++) {
						// 頂点同士を結んでいく
						g.drawLine(xys.get(i)[0], xys.get(i)[1], xys.get(i + 1)[0], xys.get(i + 1)[1]);
					}
					// 開始頂点と終了頂点を結ぶ
					g.drawLine(xys.get(xys.size() - 1)[0], xys.get(xys.size() - 1)[1], xys.get(0)[0], xys.get(0)[1]);
					xys.clear();
					
					// 描画履歴を記録
					SaveRecent(DRAW, -1, -1, null, null);
				}
			} else {
				// 多角形の頂点情報を格納
				xys.add(new int[] { click[2], click[3] });
			}
			ce.clear();
		}
	}

	/**
	 * 長方形モードでの処理
	 * @param click クリック情報 (ClickEvent参照)
	 */
	public void drawRect(int[] click) {
		// クリックされたら
		if (click[0] == -1 && click[2] != -1) {
			if (xys.size() == 0) {
				// 長方形の一点を記録
				xys.add(new int[] { click[2], click[3] });
			} else {
				// 長方形の左上の点
				int lux = Math.min(xys.get(0)[0], click[2]), luy = Math.min(xys.get(0)[1], click[3]);
				// 長方形のサイズ
				int sizex = Math.abs(click[2] - xys.get(0)[0]), sizey = Math.abs(click[3] - xys.get(0)[1]);
				if (panel.isSquare()) {
					// 正方形モード
					if (sizex > sizey) {
						sizey = sizex;
						if (xys.get(0)[1] > click[3]) {
							luy = xys.get(0)[1] - sizey;
						}
					} else {
						sizex = sizey;
						if (xys.get(0)[0] > click[2]) {
							lux = xys.get(0)[0] - sizex;
						}
					}
				}
				
				if (panel.isCornerCircle()) {
					// 角を丸めるモード
					
					// 長方形を描画
					g.drawRect(lux, luy, sizex, sizey);
					
					// 塗りつぶすモード
					if (panel.isFill()) {
						// 塗りつぶす
						g.fillRect(lux + thickness / 2, luy + thickness / 2, sizex - thickness, sizey - thickness);
					}
				} else {
					// 角をとがらせるモード
					
					// 塗りつぶすモード
					if (panel.isFill()) {
						// 長方形を塗り潰す
						g.fillRect(lux - thickness / 2, luy - thickness / 2, sizex + thickness, sizey + thickness);
					} else {
						// 辺ごとに長方形を描画
						g.fillRect(lux - thickness / 2, luy - thickness / 2, sizex, thickness);
						g.fillRect(lux - thickness / 2, luy + thickness / 2, thickness, sizey);
						g.fillRect(lux + thickness / 2, luy + sizey - thickness / 2, sizex, thickness);
						g.fillRect(lux + sizex - thickness / 2, luy - thickness / 2, thickness, sizey);
					}
				}
				xys.clear();
				
				// 描画履歴を記録
				SaveRecent(DRAW, -1, -1, null, null);
			}
			ce.clear();
		}
	}

	/**
	 * 楕円モードでの処理
	 * @param click クリック情報 (ClickEvent参照)
	 */
	public void drawOval(int[] click) {
		// クリックされたら
		if (click[0] == -1 && click[2] != -1) {
			if (xys.size() == 0) {
				// 楕円の基準点 (一回目クリックされた位置) を記録
				xys.add(new int[] { click[2], click[3] });
			} else {
				// 楕円の左上の基準点
				int lux = Math.min(xys.get(0)[0], click[2]), luy = Math.min(xys.get(0)[1], click[3]);
				// 楕円のサイズ
				int sizex = Math.abs(click[2] - xys.get(0)[0]), sizey = Math.abs(click[3] - xys.get(0)[1]);
				if (panel.isSquare()) {
					// 円モード
					if (sizex > sizey) {
						sizey = sizex;
						if (xys.get(0)[1] > click[3]) {
							luy = xys.get(0)[1] - sizey;
						}
					} else {
						sizex = sizey;
						if (xys.get(0)[0] > click[2]) {
							lux = xys.get(0)[0] - sizex;
						}
					}
				}
				
				if (panel.isFill()) {
					// 塗りつぶすモード
					g.fillOval(lux - thickness / 2, luy - thickness / 2, sizex + thickness, sizey + thickness);
				} else {
					g.drawOval(lux, luy, sizex, sizey);

				}
				xys.clear();
				
				// 描画履歴を記録
				SaveRecent(DRAW, -1, -1, null, null);
			}
			ce.clear();
		}
	}

	/**
	 * 塗りつぶしモードでの処理
	 * @param click クリック情報 (ClickEvent参照)
	 */
	private void fill(int[] click) {
		// クリックされたら
		if (click[0] == -1 && click[2] != -1) {
			// スキャンラインアルゴリズムで塗りつぶす
			
			// 画像の幅、高さ
			int width = image.getWidth(), height = image.getHeight();
			// 塗りつぶし開始位置
			int x = click[2], y = click[3];
			// 塗りつぶし対象の色、塗りつぶす色
			int cc = image.getRGB(x, y), nc = selectedColor.getRGB();
			if (cc == nc) {
				return;
			}
			
			// 塗りつぶし保留座標スタック
			Deque<Integer> xs = new ArrayDeque<Integer>();
			Deque<Integer> ys = new ArrayDeque<Integer>();
			xs.push(x);
			ys.push(y);
			
			while (!xs.isEmpty()) {
				// 塗りつぶしを保留していた座標を取り出す
				int cx = xs.pop();
				int cy = ys.pop();
				if (image.getRGB(cx, cy) == nc) {
					// 既に塗りつぶされていたらスキップ
					continue;
				}

				// 塗りつぶし対象の色の左側境界
				int xleft = cx, xright = cx;
				while (xleft > -1 && image.getRGB(xleft, cy) == cc) {
					xleft--;
				}
				xleft++;
				
				// 塗りつぶし対象の色の右側境界
				while (xright < width && image.getRGB(xright, cy) == cc) {
					xright++;
				}
				xright--;
				
				// xleft ～ xrightまでを塗りつぶす
				for (int i = xleft; i < xright + 1; i++) {
					image.setRGB(i, cy, nc);
				}

				// 上端でなければ
				if (cy > 0) {
					// (xleft ～ xright, y - 1) に対して塗り潰すべき範囲の左端をスタックに入れる
					boolean b = true;	// 直前が塗りつぶし対象の色じゃない
					for (int i = xleft; i <= xright; i++) {
						if (b && image.getRGB(i, cy - 1) == cc) {
							// 塗り潰すべき範囲の左端をスタックに入れる
							xs.push(i);
							ys.push(cy - 1);
							b = false;
						} else {
							b = true;
						}
					}
					
					// xrightに対して
					if (b && xright < width - 1 && image.getRGB(xright + 1, cy - 1) == cc) {
						xs.push(xright);
						ys.push(cy - 1);
					}
				}
				// 下端でなければ
				if (cy < height - 1) {
					// (xleft ～ xright, y + 1) に対して塗り潰すべき範囲の左端をスタックに入れる
					boolean b = true;	// 直前が塗りつぶし対象の色じゃない
					for (int i = xleft; i <= xright; i++) {
						if (b && image.getRGB(i, cy + 1) == cc) {
							xs.push(i);
							ys.push(cy + 1);
							b = false;
						} else {
							b = true;
						}
					}
					
					// xrightに対して
					if (b && xright < width - 1 && image.getRGB(xright + 1, cy + 1) == cc) {
						xs.push(xright);
						ys.push(cy + 1);
					}
				}
			}
			
			// 描画履歴を記録
			SaveRecent(DRAW, -1, -1, null, null);
		}
	}
	
	/**
	 * スタンプモードでの処理
	 * @param click クリック情報 (ClickEvent参照)
	 */
	private void stamp(int[] click) {
		// クリックされたら
		if (click[0] == -1 && click[2] != -1) {
			// スタンプ描画位置 (スタンプ画像300に合わせて調整)
			int x = click[2] - 150, y = click[3] - 150;
			// スタンプサイズ
			int size = panel.getStampSize();

			// スタンプの種類 (スタンプ番号14はランダム)
			int s = panel.getStamp() == 14 ? new Random().nextInt(14) : panel.getStamp();
			
			// スタンプの画像 (スタンプサイズは最大300なので画像サイズも300で十分)
			BufferedImage newImage = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2 = (Graphics2D) newImage.getGraphics();
			
			// スタンプの元画像をサイズに合わせて縮小し、描画
			AffineTransform at = g2.getTransform();
			at.setToScale(size / 300.0, size / 300.0);
			g2.setTransform(at);
			g2.drawImage(stamps[s], 45000 / size - 150, 45000 / size - 150, null);

			// スタンプ画像の描画されている位置に選択色で描画
			// 描画開始位置
			int sx = x > 0 ? 0 : -x;
			int sy = y > 0 ? 0 : -y;
			// 描画終了位置
			int ex = (300 > image.getWidth() - x) ? (image.getWidth() - x) : 300;
			int ey = (300 > image.getHeight() - y) ? (image.getHeight() - y) : 300;
			int rgb = selectedColor.getRGB();			// 選択色
			int clear = new Color(0, 0, 0, 0).getRGB();	// 透明
			for (int i = sy; i < ey; i++) {
				for (int j = sx; j < ex; j++) {
					if (newImage.getRGB(j, i) != clear) {
						// 透明でなければ選択色で描画
						image.setRGB(x + j, y + i, rgb);
					}
				}
			}
			
			// 描画履歴を記録
			SaveRecent(DRAW, -1, -1, null, null);
		}
	}
	
	/**
	 * テキストの描画
	 * @param text 描画文字列
	 * @param x 描画座標x
	 * @param y 描画座標y
	 * @param size フォントサイズ
	 * @param font フォント
	 */
	public void writeText(String text, int x, int y, int size, String font) {
		// 色とフォントの設定
		g.setColor(selectedColor);
		g.setFont(new Font(font, Font.PLAIN, size));
		
		// 改行ごとに文字を分割
		String[] strs = text.split("\n");
		int fontHeight = g.getFontMetrics().getHeight();	// 文字の縦幅
		for (int i = 0; i < strs.length; i++) {
			// 改行ごとに位置yを下げる
			g.drawString(strs[i], x, y + fontHeight * (i + 1));
		}
		
		// 描画履歴を記録
		SaveRecent(DRAW, -1, -1, null, null);
	}

	/**
	 * 左右反転
	 */
	public void flipH() {
		// 画像の幅、高さ
		int width = image.getWidth(), height = image.getHeight();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width / 2; x++) {
				// 左右でRGBを入れ替え
				int tmp = image.getRGB(x, y);
				image.setRGB(x, y, image.getRGB(width - x - 1, y));
				image.setRGB(width - x - 1, y, tmp);
			}
		}
		
		// 描画履歴を記録
		SaveRecent(DRAW, -1, -1, null, null);
	}

	/**
	 * 上下反転
	 */
	public void flipI() {
		// 画像の幅、高さ
		int width = image.getWidth(), height = image.getHeight();
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height / 2; y++) {
				// 上下でRGBを入れ替え
				int tmp = image.getRGB(x, y);
				image.setRGB(x, y, image.getRGB(x, height - y - 1));
				image.setRGB(x, height - y - 1, tmp);
			}
		}
		
		// 描画履歴を記録
		SaveRecent(DRAW, -1, -1, null, null);
	}

	/**
	 * 回転
	 * @param r 回転角度
	 */
	public void rotate(int r) {
		// 回転後の画像newImage
		BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D newG = (Graphics2D) newImage.getGraphics();
		
		// 回転させてnewImageに描画
		AffineTransform at = newG.getTransform();
		at.setToRotation(Math.toRadians(r), image.getWidth() / 2.0, image.getHeight() / 2.0);
		newG.setTransform(at);
		newG.drawImage(image, 0, 0, null);
		
		clearImage(null, false);			// 現在の画像をクリア
		g.drawImage(newImage, 0, 0, null);	// 現在の画像にnewImageを描画
		
		// 描画履歴を記録
		SaveRecent(DRAW, -1, -1, null, null);
	}

	/***
	 * 画像を初期化する
	 * @param image 対象の画像 (null : 現在のレイヤーの画像)
	 * @param isSave 操作履歴に保存するか
	 */
	public void clearImage(BufferedImage image, boolean isSave) {
		if (image == null) {
			// nullなら現在のレイヤーの画像
			image = Main.pm.getImage();
		}
		int clear = new Color(0, 0, 0, 0).getRGB();	// 透明のRGB値
		for (int i = 0; i < image.getWidth(); i++) {
			for (int j = 0; j < image.getHeight(); j++) {
				// 全ピクセルを透明にする
				image.setRGB(i, j, clear);
			}
		}
		
		if (isSave) {
			// 描画履歴を記録
			SaveRecent(DRAW, -1, -1, null, null);
		}
	}

	/**
	 * 色反転
	 */
	public void inverse() {
		for (int i = 0; i < image.getHeight(); i++) {
			for (int j = 0; j < image.getWidth(); j++) {
				// ピクセルの色を取得
				Color c = new Color(image.getRGB(j, i), true);
				// 反転した色を描画
				image.setRGB(j, i,
						new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue(), c.getAlpha()).getRGB());
			}
		}
		
		// 描画履歴を記録
		SaveRecent(DRAW, -1, -1, null, null);
	}

	/**
	 * 白黒化
	 */
	public void monochro() {
		for (int i = 0; i < image.getHeight(); i++) {
			for (int j = 0; j < image.getWidth(); j++) {
				// ピクセルの色を取得
				Color c = new Color(image.getRGB(j, i), true);
				// 輝度を取得
				int kido = (int) (0.3 * c.getRed() + 0.6 * c.getGreen() + 0.1 * c.getBlue());
				// 輝度に基づき色を白黒化し描画
				image.setRGB(j, i, new Color(kido, kido, kido, c.getAlpha()).getRGB());
			}
		}
		
		// 描画履歴を記録
		SaveRecent(DRAW, -1, -1, null, null);
	}
}
