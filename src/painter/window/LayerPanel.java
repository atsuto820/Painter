package painter.window;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import painter.main.Main;
import painter.main.PaintTool;

/**
 * レイヤー管理のパネル
 * @author atsuto
 *
 */
public class LayerPanel extends JPanel {
	// レイヤーを並べて表示するパネル
	private JPanel layerPanel;
	private JButton addButton, removeButton, upButton, downButton, renameBitton;
	private ArrayList<Layer> layers;
	private Layer selectedLayer;
	private SelectButtonEvent bEvent;
	
	// レイヤー数のナンバリング
	private int layerNum;
	
	public int getIndex() {
		return indexOf(selectedLayer);
	}
	
	public int indexOf(Layer layer) {
		return layers.indexOf(layer);
	}
	
	public boolean isVisibleLayer(int index) {
		return layers.get(index).isVisibleLayer();
	}
	
	public String getLayerName() {
		return selectedLayer.GetLayerName();
	}
	
	public void setLayerName(String name, boolean isSave) {
		selectedLayer.SetLayerName(name, isSave);
	}
	
	public LayerPanel(MainFrame frame) {
		super();
		// Layerのフォント設定
		Layer.font = new Font("ＭＳ ゴシック", Font.PLAIN, 16);
		Layer.lpanel = this;
		// このパネルのサイズ設定
		setPreferredSize(new Dimension(260, 600));
		// 左揃えのFlowLayout
		setLayout(new FlowLayout(FlowLayout.LEFT));
		bEvent = new SelectButtonEvent(frame);
		
		// 灰色部分のパネル
		JPanel basePanel = new JPanel();
		// パネルのサイズ設定
		basePanel.setPreferredSize(new Dimension(250, 700));
		// 中央揃えのFlowLayout
		basePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		// 背景色設定
		basePanel.setBackground(Color.LIGHT_GRAY);
		// このパネルに加える
		add(basePanel);
		
		// 「レイヤー」のラベル
		JLabel layerLabel = new JLabel("レイヤー");
		// フォント設定
		layerLabel.setFont(MainFrame.font);
		// ラベルサイズ設定
		layerLabel.setPreferredSize(new Dimension(250, 30));
		// 文字を中央揃え
		layerLabel.setHorizontalAlignment(JLabel.CENTER);
		// basePanelに加える
		basePanel.add(layerLabel);
		
		// レイヤーを並べて表示するパネル
		layerPanel = new JPanel();
		// パネルのサイズ設定
		layerPanel.setPreferredSize(new Dimension(230, 450));
		// 背景色設定
		layerPanel.setBackground(Color.GRAY);
		// basePanelに加える
		basePanel.add(layerPanel);
		
		// レイヤー操作類ボタン
		addButton = new JButton("追加");
		removeButton = new JButton("削除");
		upButton = new JButton("上へ移動");
		downButton = new JButton("下へ移動");
		renameBitton = new JButton("名前変更");
		// フォント設定
		addButton.setFont(MainFrame.font);
		removeButton.setFont(MainFrame.font);
		upButton.setFont(MainFrame.font);
		downButton.setFont(MainFrame.font);
		renameBitton.setFont(MainFrame.font);
		// 押されたときのイベントの設定
		addButton.setActionCommand("レイヤー追加");
		addButton.addActionListener(bEvent);
		removeButton.setActionCommand("レイヤー削除");
		removeButton.addActionListener(bEvent);
		upButton.setActionCommand("レイヤー上");
		upButton.addActionListener(bEvent);
		downButton.setActionCommand("レイヤー下");
		downButton.addActionListener(bEvent);
		renameBitton.setActionCommand("レイヤー名前");
		renameBitton.addActionListener(bEvent);
		// basePanelに加える
		basePanel.add(addButton);
		basePanel.add(removeButton);
		basePanel.add(upButton);
		basePanel.add(downButton);
		basePanel.add(renameBitton);
	}
	
	/**
	 * レイヤーを加える (レイヤー名自動)
	 */
	public void addLayer() {
		addLayer(new Layer(layerNum), layers.indexOf(selectedLayer), true);
		layerNum++;
	}
	
	/**
	 * レイヤーを加える
	 * @param name レイヤー名
	 */
	public void addLayer(String name) {
		addLayer(new Layer(name), layers.indexOf(selectedLayer), false);
	}
	
	/**
	 * 指定の位置にレイヤーを加える
	 * @param name
	 * @param index
	 */
	public void addLayer(String name, int index) {
		addLayer(new Layer(name), index, false);
	}
	
	/**
	 * レイヤーを加える
	 * @param newLayer
	 * @param index 加える場所
	 * @param isSave 操作履歴を保存するか
	 */
	private void addLayer(Layer newLayer, int index, boolean isSave) {
		// レイヤーを加える
		layers.add(index, newLayer);
		
		// レイヤーパネルをリセット
		layerPanel.removeAll();
		for (Layer layer : layers) {
			// レイヤー表示を再構築
			layerPanel.add(layer);
		}
		// 加えたレイヤーを選択
		selectLayer(newLayer);
		// 削除ボタンを有効化
		removeButton.setEnabled(true);
		
		// 再描画
		layerPanel.revalidate();
		layerPanel.repaint();
		
		if(isSave) {
			// 操作履歴を保存する
			Main.pm.getPT().SaveRecent(PaintTool.LAYER_ADD, -1, -1, newLayer.GetLayerName(), null);
		}
	}
	
	/**
	 * レイヤーを削除
	 */
	public void removeLayer() {
		// 削除対象のレイヤー番号
		int index = layers.indexOf(selectedLayer);
		// 削除
		layers.remove(selectedLayer);
		layerPanel.remove(selectedLayer);
		
		// 削除後に選択されるレイヤー
		if(index == layers.size()) {
			selectLayer(layers.get(layers.size() - 1));
		}
		else {
			selectLayer(layers.get(index));
		}
		// レイヤー数が1なら削除ボタンを無効化
		removeButton.setEnabled(layers.size() > 1);
		
		// 再描画
		layerPanel.revalidate();
		layerPanel.repaint();
	}
	
	/**
	 * レイヤーを選択する
	 * @param index
	 */
	public void selectLayer(int index) {
		selectLayer(layers.get(index));
	}
	
	/**
	 * レイヤーを選択する
	 * @param layer
	 */
	public void selectLayer(Layer layer) {
		if(selectedLayer != null) {
			// もと選択されていたレイヤーの選択を解除
			selectedLayer.SetSelect(false);
		}
		// レイヤーを選択
		layer.SetSelect(true);
		selectedLayer = layer;
		
		// 選択後のレイヤー番号
		int index = layers.indexOf(selectedLayer);
		// 一番上のレイヤーならレイヤー上移動ボタン無効化
		upButton.setEnabled(index > 0);
		// 一番下のレイヤーならレイヤー下移動ボタン無効化
		downButton.setEnabled(layers.size() != 1 && index != layers.size() - 1);
	}
	
	/**
	 * レイヤー移動
	 * @param isUp 上へか
	 */
	public void moveLayer(boolean isUp) {
		// 移動前のインデックス
		int index = layers.indexOf(selectedLayer);
		// 一時的にレイヤーを削除
		layerPanel.remove(selectedLayer);
		layers.remove(selectedLayer);
		// 移動先の位置にレイヤーを加える
		addLayer(selectedLayer, isUp ? index - 1 : index + 1, false);
	}
	
	/**
	 * ボタンなどの有効を切り替える
	 * @param enable
	 */
	public void setAllEnabled(boolean enable) {
		if(enable) {
			// レイヤーのリセット
			layerReset();
			
			layerNum = 2;
		} else {
			// レイヤーを全削除
			layerPanel.removeAll();
			if(layers != null) {
				layers.clear();
			}
			// 再描画
			layerPanel.revalidate();
			layerPanel.repaint();
			
			removeButton.setEnabled(false);
			upButton.setEnabled(false);
			downButton.setEnabled(false);
		}
		addButton.setEnabled(enable);
		renameBitton.setEnabled(enable);
	}
	
	/**
	 * レイヤーのリセット
	 */
	public void layerReset() {
		// レイヤーを全削除
		layerPanel.removeAll();
		layers = new ArrayList<Layer>();
		// デフォルトレイヤー
		Layer layer = new Layer(-1);
		layers.add(layer);
		layerPanel.add(layer);
		selectLayer(layer);
		// 再描画
		layerPanel.revalidate();
		layerPanel.repaint();
	}
	
	/**
	 * レイヤーの可視切り替え
	 * @param num
	 */
	public void changeLayerVisible(int num) {
		layers.get(num).changeVisible();
	}
}
