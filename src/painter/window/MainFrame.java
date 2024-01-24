package painter.window;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import painter.main.Main;

public class MainFrame extends JFrame {
	// プログラムで主に使用されるフォント
	public static Font font;
	
	public JMenuBar menuBar;
	public JMenu[] menus;
	public JMenuItem[] mis, mis2, mis3;
	public JButton[] toolButtons;
	public SelectPanel spanel;
	public PaintPanel panel;
	public LayerPanel lpanel;
	public JLabel tip;

	// イベントリスナー
	private MenuEvent mEvent;
	private ClickEvent cEvent;
	private ButtonEvent bEvent;

	// 発生したイベント
	public String event;
	// 保存先のパス
	private String savePath;

	// ヒント
	private String[] tips = {
			"「ファイル→新規作成」で画像を新規作成するか「ファイル→開く」で既存のファイルを開けます。",
			"ドラッグすると線が描けます。",
			"時間変化とともに色が変化するペンです。",
			"ドラッグすると消せます。",
			"クリックすると直線の始点が選択されます。再びクリックすると直線の終点が選択され、始点から終点までの直線が描けます。",
			"クリックして多角形の頂点を指定し、右クリックすると選択した頂点の多角形が描けます。",
			"クリックして長方形の一点が選択されます。再びクリックすると対角となる点が選択され、長方形が描けます。",
			"クリックして楕円の左上隅が選択されます。再びクリックすると楕円が描けます。",
			"同じ色の領域内を塗りつぶしします。",
			"テキストを指定のフォント、サイズで描けます。フォントはインストールされているフォントから選べます。",
			"クリックすると様々な形をスタンプできます。色も指定できます。"
	};

	/**
	 * クリック情報を更新
	 * @return
	 */
	public int[] updateClick() {
		return cEvent.updateClick();
	}

	public MainFrame(ClickEvent ce) {
		super("Super Hyper Paint");
		event = "";
		
		// プログラムで主に使用されるフォント
		font = new Font("ＭＳ ゴシック", Font.BOLD, 20);
		// ×ボタンが押されたときに終了するようにする
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		// ウィンドウサイズの設定
		setSize(1600, 900);
		// ウィンドウ位置を中心にする
		setLocationRelativeTo(null);
		try {
			// アイコン画像を読み込んで設定
			setIconImage(ImageIO.read(new File("img/icon.png")));
		} catch (IOException e) {
		}
		setLayout(new BorderLayout());

		// イベント
		mEvent = new MenuEvent(this);
		cEvent = ce;
		bEvent = new ButtonEvent(this, null);

		// 画面中央のパネル (PaintPanelを配置)
		JPanel centerPanel = new JPanel();
		// 背景色の設定
		centerPanel.setBackground(Color.GRAY);
		centerPanel.setLayout(null);
		// ウィンドウに追加
		add(centerPanel, BorderLayout.CENTER);
		
		// 画像描画領域のパネル
		panel = new PaintPanel();
		panel.setBounds(0, 0, 0, 0);
		// 背景色の設定
		panel.setBackground(Color.DARK_GRAY);
		// マウスイベントを受け取る
		panel.addMouseListener(cEvent);
		panel.addMouseMotionListener(cEvent);
		// 中央のパネルに追加
		centerPanel.add(panel, BorderLayout.CENTER);

		// 画面左側のパネル (SelectPanelとヒントを配置)
		JPanel westPanel = new JPanel();
		westPanel.setLayout(new BorderLayout());
		// ウィンドウに追加
		add(westPanel, BorderLayout.WEST);
		
		// ペイントツールの選択、設定するパネル
		spanel = new SelectPanel(this);
		// 左側のパネルに追加
		westPanel.add(spanel, BorderLayout.NORTH);

		// ヒントのパネル
		JPanel tipPanel = new JPanel();
		// 背景色の設定
		tipPanel.setBackground(Color.LIGHT_GRAY);
		// 左側のパネルに追加
		westPanel.add(tipPanel, BorderLayout.SOUTH);
		
		// ヒントのラベル
		tip = new JLabel();
		// フォントの設定
		tip.setFont(new Font("ＭＳ ゴシック", Font.PLAIN, 20));
		// サイズ設定
		tip.setPreferredSize(new Dimension(250, 250));
		// 文字を上揃え
		tip.setVerticalAlignment(JLabel.TOP);
		// デフォルトのヒント
		setTip(0);
		// ヒントのパネルに追加
		tipPanel.add(tip);
		
		
		// レイヤー表示のパネル
		lpanel = new LayerPanel(this);
		add(lpanel, BorderLayout.EAST);

		// 画面最上部のメニューバー
		menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		// メニューバーのメニュー
		menus = new JMenu[3];
		menus[0] = new JMenu("ファイル(F)");
		menus[0].setMnemonic(KeyEvent.VK_F);
		menus[1] = new JMenu("編集(E)");
		menus[1].setMnemonic(KeyEvent.VK_E);
		menus[2] = new JMenu("フィルタ(R)");
		menus[2].setMnemonic(KeyEvent.VK_R);
		// メニューバーに追加
		for (JMenu menu : menus) {
			menuBar.add(menu);
		}

		// メニューのアイテム (ファイル)
		mis = new JMenuItem[6];
		mis[0] = new JMenuItem("新規作成(N)", new ImageIcon("img/menu/new.png"));
		mis[0].setMnemonic(KeyEvent.VK_N);
		mis[0].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
		mis[1] = new JMenuItem("開く(O)", new ImageIcon("img/menu/open.png"));
		mis[1].setMnemonic(KeyEvent.VK_O);
		mis[1].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
		mis[2] = new JMenuItem("閉じる(C)", new ImageIcon("img/menu/close.png"));
		mis[2].setMnemonic(KeyEvent.VK_C);
		mis[3] = new JMenuItem("名前を付けて保存(A)", new ImageIcon("img/menu/newsave.png"));
		mis[3].setMnemonic(KeyEvent.VK_A);
		mis[3].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
		mis[4] = new JMenuItem("保存(S)", new ImageIcon("img/menu/save.png"));
		mis[4].setMnemonic(KeyEvent.VK_S);
		mis[4].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		mis[5] = new JMenuItem("終了(X)", new ImageIcon("img/menu/exit.png"));
		mis[5].setMnemonic(KeyEvent.VK_X);
		// メニューに追加
		for (int i = 0; i < mis.length; i++) {
			menus[0].add(mis[i]);
			mis[i].addActionListener(mEvent);
			if (i == 2 || i == 4) {
				// セパレータ
				menus[0].addSeparator();
			}
		}

		// メニューのアイテム (編集)
		mis2 = new JMenuItem[8];
		mis2[0] = new JMenuItem("元に戻す(Z)", new ImageIcon("img/tool/undo.png"));
		mis2[0].setMnemonic(KeyEvent.VK_Z);
		mis2[0].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
		mis2[1] = new JMenuItem("やり直す(Y)", new ImageIcon("img/tool/redo.png"));
		mis2[1].setMnemonic(KeyEvent.VK_Y);
		mis2[1].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK));
		mis2[2] = new JMenuItem("色選択(S)", new ImageIcon("img/tool/color.png"));
		mis2[2].setMnemonic(KeyEvent.VK_S);
		mis2[3] = new JMenuItem("クリア(C)", new ImageIcon("img/tool/clear.png"));
		mis2[3].setMnemonic(KeyEvent.VK_C);
		mis2[4] = new JMenuItem("左右反転(H)", new ImageIcon("img/tool/flipH.png"));
		mis2[4].setMnemonic(KeyEvent.VK_H);
		mis2[5] = new JMenuItem("上下反転(U)", new ImageIcon("img/tool/flipI.png"));
		mis2[5].setMnemonic(KeyEvent.VK_U);
		mis2[6] = new JMenuItem("左回転(L)", new ImageIcon("img/tool/rotateL.png"));
		mis2[6].setMnemonic(KeyEvent.VK_L);
		mis2[7] = new JMenuItem("右回転(R)", new ImageIcon("img/tool/rotateR.png"));
		mis2[7].setMnemonic(KeyEvent.VK_R);
		// メニューに追加
		for (int i = 0; i < mis2.length; i++) {
			menus[1].add(mis2[i]);
			mis2[i].addActionListener(mEvent);
			if (i == 1 || i == 3) {
				// セパレータ
				menus[1].addSeparator();
			}
		}

		// メニューのアイテム (フィルタ)
		mis3 = new JMenuItem[2];
		mis3[0] = new JMenuItem("色反転(I)", new ImageIcon("img/tool/inverse.png"));
		mis3[0].setMnemonic(KeyEvent.VK_I);
		mis3[1] = new JMenuItem("白黒にする(M)", new ImageIcon("img/tool/monochro.png"));
		mis3[1].setMnemonic(KeyEvent.VK_M);
		// メニューに追加
		for (int i = 0; i < mis3.length; i++) {
			menus[2].add(mis3[i]);
			mis3[i].addActionListener(mEvent);
		}

		// 画面上部のツールバー
		JToolBar toolBar = new JToolBar("ツールバー");
		add(toolBar, BorderLayout.NORTH);

		// ツールバーのツールボタン
		toolButtons = new JButton[10];
		toolButtons[0] = new JButton(new ImageIcon("img/tool/pen.png"));
		toolButtons[0].setActionCommand("Tペン");
		toolButtons[1] = new JButton(new ImageIcon("img/tool/rainbowPen.png"));
		toolButtons[1].setActionCommand("T虹色ペン");
		toolButtons[2] = new JButton(new ImageIcon("img/tool/eraser.png"));
		toolButtons[2].setActionCommand("T消しゴム");
		toolButtons[3] = new JButton(new ImageIcon("img/tool/line.png"));
		toolButtons[3].setActionCommand("T直線");
		toolButtons[4] = new JButton(new ImageIcon("img/tool/polygon.png"));
		toolButtons[4].setActionCommand("T多角形");
		toolButtons[5] = new JButton(new ImageIcon("img/tool/rect.png"));
		toolButtons[5].setActionCommand("T長方形");
		toolButtons[6] = new JButton(new ImageIcon("img/tool/circle.png"));
		toolButtons[6].setActionCommand("T円");
		toolButtons[7] = new JButton(new ImageIcon("img/tool/fill.png"));
		toolButtons[7].setActionCommand("T塗りつぶし");
		toolButtons[8] = new JButton(new ImageIcon("img/tool/text.png"));
		toolButtons[8].setActionCommand("Tテキスト");
		toolButtons[9] = new JButton(new ImageIcon("img/tool/stamp.png"));
		toolButtons[9].setActionCommand("Tスタンプ");
		// ツールバーに追加
		for (int i = 0; i < toolButtons.length; i++) {
			toolButtons[i].addActionListener(bEvent);
			toolBar.add(toolButtons[i]);
			if (i == 2 || i == 6) {
				// セパレータ
				toolBar.addSeparator();
			}
		}
		toolButtons[0].setBackground(Color.GRAY);
		
		// 最初は操作ボタンなどを無効化
		setAllEnabled(false);
		
		// 可視化
		setVisible(true);
	}

	/**
	 * (編集中の画像が失われる処理に対して)必要なら「編集中の画像は破棄されますがよろしいですか」ダイアログの表示を行う
	 * @param title ダイアログのタイトル
	 * @return よろしいか
	 */
	public boolean editCheck(String title) {
		// 編集中の画像があるときのみ確認
		if (Main.pm.getImage() != null) {
			// 確認ダイアログ
			int option = JOptionPane.showConfirmDialog(this, "編集中の画像は破棄されますがよろしいですか？", title,
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (option != 0) {
				// キャンセル
				return false;
			}
		}
		return true;
	}

	/**
	 * 画像の新規作成ボタンが押された
	 */
	public void newCreate() {
		// 編集中の画像が失われることの確認
		if (!editCheck("新規作成")) {
			return;
		}
		// 新規作成ダイアログの表示
		dialogEvent("新規作成");
	}
	
	/**
	 * 画像の新規作成
	 */
	public void newImage() {
		// 操作ボタンなどを有効にする
		setAllEnabled(true);
		mis2[0].setEnabled(false);
		mis2[1].setEnabled(false);
		
		// 保存先パスをリセット
		savePath = null;
		setTip(1);
	}

	/**
	 * ファイルを開く
	 */
	public void openFile() {
		if (!editCheck("開く")) {
			// 編集中の画像が失われることの確認
			return;
		}
		// ファイル選択ダイアログ
		JFileChooser fileChooser = new JFileChooser();
		// 開けるファイルの種類のフィルタ
		FileFilter png = new FileNameExtensionFilter("pngファイル", "png");
		FileFilter jpg = new FileNameExtensionFilter("jpgファイル", "jpg", "jpeg");
		FileFilter bmp = new FileNameExtensionFilter("bmpファイル", "bmp");
		fileChooser.addChoosableFileFilter(png);
		fileChooser.addChoosableFileFilter(jpg);
		fileChooser.addChoosableFileFilter(bmp);
		fileChooser.setFileFilter(png);
		
		// ファイル選択ダイアログを表示
		int ret = fileChooser.showOpenDialog(this);
		if (ret == JFileChooser.APPROVE_OPTION) {
			// ファイルが開かれた
			try {
				// 画像の読み込み
				Image image = ImageIO.read(fileChooser.getSelectedFile());
				if (image == null) {
					// 読み込みに失敗
					
					// 失敗ダイアログを表示
					JOptionPane.showMessageDialog(this, "ファイルが開けませんでした。", "失敗",
							JOptionPane.INFORMATION_MESSAGE);
				} else {
					// 読み込みに成功
					
					// 新しい画像を作成
					BufferedImage newImage = new BufferedImage(image.getWidth(null), image.getHeight(null),
							BufferedImage.TYPE_INT_ARGB);
					Graphics g = newImage.getGraphics();
					// 新しい画像に読み込んだ画像を描画
					g.drawImage(image, 0, 0, null);

					Main.newImage(newImage);
					setAllEnabled(true);		// 操作ボタンなどを有効にする
					lpanel.layerReset();
					panel.repaint();
					
					// 保存先パスを設定
					savePath = fileChooser.getSelectedFile().getPath();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 画像を閉じる
	 */
	public void close() {
		if (!editCheck("閉じる")) {
			// 編集中の画像が失われることの確認
			return;
		}
		Main.pm.close();
		panel.setBounds(0, 0, 0, 0);
		setAllEnabled(false);	// 操作ボタンなどを無効にする
	}

	/**
	 * 操作ボタンなどの有効を切り替える
	 * @param enable 有効か
	 */
	public void setAllEnabled(boolean enable) {
		// メニューアイテム
		mis[2].setEnabled(enable);
		mis[3].setEnabled(enable);
		mis[4].setEnabled(enable);
		for (int i = enable ? 2 : 0; i < mis2.length; i++) {
			mis2[i].setEnabled(enable);
		}
		for (int i = 0; i < mis3.length; i++) {
			mis3[i].setEnabled(enable);
		}
		
		spanel.setAllEnabled(enable);
		lpanel.setAllEnabled(enable);
		for (JButton button : toolButtons) {
			// ツールバーのボタン
			button.setEnabled(enable);
		}
	}

	/**
	 * 名前を付けて保存
	 */
	public void newSave() {
		// ファイル選択ダイアログ
		JFileChooser fileChooser = new JFileChooser();
		// 開けるファイルの種類のフィルタ
		FileFilter png = new FileNameExtensionFilter("pngファイル", "png");
		fileChooser.addChoosableFileFilter(png);
		fileChooser.setFileFilter(png);
		fileChooser.setAcceptAllFileFilterUsed(false);
		
		// ファイル選択ダイアログを表示
		int ret = fileChooser.showSaveDialog(this);
		if (ret == JFileChooser.APPROVE_OPTION) {
			// ファイルが開かれた
			try {
				// 開かれたファイル
				File file = fileChooser.getSelectedFile();
				savePath = file.getPath();
				
				// ファイルに保存
				ImageIO.write(Main.pm.getImage(), "png", file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 画像を保存
	 */
	public void save() {
		if (savePath == null) {
			// 保存先パスがわからないので名前を付けて保存
			newSave();
		} else {
			try {
				// 画像の保存
				ImageIO.write(Main.pm.getImage(), "png", new File(savePath));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * プログラムの終了
	 */
	public void end() {
		if (!editCheck("終了")) {
			// 編集中の画像が失われることの確認
			return;
		}
		// プログラムを終了
		System.exit(0);
	}

	/**
	 * ダイアログの表示
	 * @param str ダイアログのタイトル
	 */
	public void dialogEvent(String str) {
		new Dialog(this, bEvent, str);
	}

	/**
	 * イベントの発生を確認
	 */
	public void checkEvent() {
		if (!event.equals("")) {
			// イベントが発生した
			if (event.charAt(0) == 'D') {
				// ダイアログイベント
				dialogEvent(event.substring(1));
			}
			
			switch (event) {
			case "新規作成(N)":
				newCreate();
				break;
			case "開く(O)":
				openFile();
				break;
			case "閉じる(C)":
				close();
				break;
			case "保存(S)":
				save();
				break;
			case "名前を付けて保存(A)":
				newSave();
				break;
			case "終了(X)":
				end();
				break;
			default:
				edit(event);
				break;
			}
			
			// イベントをリセット
			event = "";
		}
	}

	/**
	 * イベントによる編集処理
	 * @param str
	 */
	public void edit(String str) {
		int button = -1;	// 切り替わった先のツールボタン
		switch (str) {
		case "ペン":
			button = 0;
			break;
		case "虹色ペン":
			button = 1;
			break;
		case "消しゴム":
			button = 2;
			break;
		case "直線":
			button = 3;
			break;
		case "多角形":
			button = 4;
			break;
		case "長方形":
			button = 5;
			break;
		case "円":
			button = 6;
			break;
		case "塗りつぶし":
			button = 7;
			break;
		case "テキスト":
			button = 8;
			break;
		case "スタンプ":
			button = 9;
			break;
		case "左右反転(H)":
			Main.pm.getPT().flipH();
			break;
		case "上下反転(U)":
			Main.pm.getPT().flipI();
			break;
		case "左回転(L)":
			Main.pm.getPT().rotate(-90);
			break;
		case "右回転(R)":
			Main.pm.getPT().rotate(90);
			break;
		case "クリア(C)":
			Main.pm.getPT().clearImage(null, true);
			break;
		case "色選択(S)":
			event = "D色選択";
			break;
		case "テキスト決定":
			Main.pm.getPT().writeText(spanel.getText(), spanel.getMyX(), spanel.getMyY(),
					spanel.getFontSize(), spanel.getMyFont().getFontName());
			break;
		case "色反転(I)":
			Main.pm.getPT().inverse();
			break;
		case "白黒にする(M)":
			Main.pm.getPT().monochro();
			break;
		case "元に戻す(Z)":
			Main.pm.getPT().Undo();
			break;
		case "やり直す(Y)":
			Main.pm.getPT().Redo();
			break;
		case "レイヤー追加":
			Main.pm.addNewLayer();
			lpanel.addLayer();
			break;
		case "レイヤー削除":
			Main.pm.removeLayer(true);
			lpanel.removeLayer();
			break;
		case "レイヤー上":
			Main.pm.moveLayer(true, true);
			lpanel.moveLayer(true);
			break;
		case "レイヤー下":
			Main.pm.moveLayer(false, true);
			lpanel.moveLayer(false);
			break;
		case "レイヤー名前":
			event = "Dレイヤー名変更";
			break;
		}
		if (button != -1) {
			// ツールボタンの切り替え
			toolButtons[Main.pm.getPT().getMode()].setBackground(Color.WHITE);
			toolButtons[button].setBackground(Color.GRAY);
			Main.pm.getPT().setMode(button);	// 描画モードの切り替え
			
			// ヒントの表示
			setTip(button + 1);
		}
	}

	/**
	 * ヒントの表示
	 * @param state ヒント番号
	 */
	public void setTip(int state) {
		tip.setText("<html><body>ヒント<br><br>" + tips[state] + "</body></html>");
	}
}
