package painter.window;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import painter.main.Main;
import painter.main.PaintTool;

/**
 * レイヤーの表示コンポーネント
 * @author atsuto
 *
 */
public class Layer extends JPanel implements MouseListener {
	// レイヤー表示で使用されるフォント
	public static Font font;
	public static LayerPanel lpanel;
	
	// レイヤー名
	private String layerName;
	// レイヤー名ラベル
	private JLabel name;
	// 可視切り替えボタン
	private VisibleButton button;
	
	/**
	 * 新規レイヤーを作成 (レイヤー名自動)
	 * @param num 何個目のレイヤーか
	 */
	public Layer(int num) {
		this("新しいレイヤー" + (num > 0 ? num : ""));
	}
	
	/**
	 * 新規レイヤーを作成
	 * @param layerName レイヤー名
	 */
	public Layer(String layerName) {
		super();
		// コンポーネントのサイズを設定
		setPreferredSize(new Dimension(220, 50));
		addMouseListener(this);
		// 左揃えのFlowLayout
		setLayout(new FlowLayout(FlowLayout.LEFT));
		
		this.layerName = layerName;
		// レイヤー名ラベル
		name = new JLabel(layerName);
		// フォント設定
		name.setFont(font);
		// ラベルサイズの設定
		name.setPreferredSize(new Dimension(160, 36));
		// レイヤー名ラベルをこのコンポーネントに加える
		add(name);
		
		// 可視切り替えボタン
		button = new VisibleButton(this);
		// 可視切り替えボタンをこのコンポーネントに加える
		add(button);
	}
	
	/**
	 * 選択状態の切り替え
	 * @param isSelected 選択状態にするか
	 */
	public void SetSelect(boolean isSelected) {
		// 選択状態ではボーダーを表示
		if(isSelected) {
			setBorder(new LineBorder(Color.RED, 2, true));
		} else {
			setBorder(null);
		}
	}
	
	public String GetLayerName() {
		return layerName;
	}
	
	/**
	 * レイヤー名設定
	 * @param layerName レイヤー名
	 * @param isSave 操作履歴を記録するか
	 */
	public void SetLayerName(String layerName, boolean isSave) {
		if(isSave) {
			// 操作履歴を保存
			Main.pm.getPT().SaveRecent(PaintTool.LAYER_NAME, -1, -1, this.layerName, layerName);
		}
		
		this.layerName = layerName;
		name.setText(layerName);	// ラベルに設定
	}
	
	public boolean isVisibleLayer() {
		return button.isVisibleLayer();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	/**
	 * このコンポーネントが押された
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		// 選択する
		lpanel.selectLayer(this);
		Main.pm.changeLayer(lpanel.indexOf(this), true);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}
	
	// 可視を切り替える
	public void changeVisible() {
		button.changeVisible();
	}
}

/**
 * 可視切り替えボタン
 * @author atsuto
 *
 */
class VisibleButton extends JPanel implements MouseListener {
	// 可視のときの画像と不可視のときの画像
	public static BufferedImage visible, invisible;
	// 親のLayer
	private Layer layer;
	// 見えるか
	private boolean isVisible;
	
	public boolean isVisibleLayer() {
		return isVisible;
	}
	
	public VisibleButton(Layer layer) {
		super();
		// コンポーネントのサイズを設定
		setPreferredSize(new Dimension(36, 36));
		addMouseListener(this);
		
		this.layer = layer;
		// デフォルトは可視
		isVisible = true;
		
		if(visible == null) {
			// 画像が読み込まれていないなら読み込む
			try {
				visible = ImageIO.read(new File("img/visible.png"));
				invisible = ImageIO.read(new File("img/invisible.png"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 描画
	 */
	public void paint(Graphics g) {
		super.paint(g);
		// 可視or不可視画像を描画
		g.drawImage(isVisible ? visible : invisible, 2, 2, this);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	/**
	 * ボタンが押された
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		// 操作履歴を保存
		Main.pm.getPT().SaveRecent(PaintTool.LAYER_VISIBLE, Layer.lpanel.indexOf(layer), -1, null, null);
		
		// 可視切り替え
		changeVisible();
	}
	
	/**
	 * 可視切り替え
	 */
	public void changeVisible() {
		isVisible = !isVisible;
		repaint();	// 再描画
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}
}
