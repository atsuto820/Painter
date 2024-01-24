package painter.window;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;

/**
 * ペイントツールの選択、設定するパネル
 * @author atsuto
 *
 */
public class SelectPanel extends JPanel {
	// パネルに配置されるコンポーネント
	private JPanel color; // 色を表示するパネル
	private JPanel etc; // 各種設定部分のパネル
	private JButton changeColor;
	private JSpinner thicknessSpinner, xSpinner, ySpinner, fontSizeSpinner, stampSizeSpinner;
	private JCheckBox fillCheck, cornerCheck, squareCheck;
	private JTextArea text;
	private JComboBox<String> fontCombo, lineCombo, stampConbo;
	// ボタンイベントリスナー
	private SelectButtonEvent bEvent;

	// 使用可能なフォント
	private Font[] fonts;
	// 描画モード
	private int mode;
	public static final int NONE = -1;
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

	// 保存される各種設定値
	private int thickness, x, y, font, fontSize, line, stamp, stampSize;
	private boolean isFill, isCornerCircle, isSquare;

	public Font getMyFont() {
		return fonts[fontCombo.getSelectedIndex()];
	}

	public int getMyX() {
		return (int) xSpinner.getValue();
	}

	public int getMyY() {
		return (int) ySpinner.getValue();
	}

	public int getFontSize() {
		return (int) fontSizeSpinner.getValue();
	}

	public int getThickness() {
		return (int) thicknessSpinner.getValue();
	}

	public String getText() {
		return text.getText();
	}

	public int getLine() {
		if (lineCombo == null) {
			return 0;
		}
		return lineCombo.getSelectedIndex();
	}

	public int getStamp() {
		return stampConbo.getSelectedIndex();
	}

	public int getStampSize() {
		return (int) stampSizeSpinner.getValue();
	}

	public boolean isFill() {
		return fillCheck.isSelected();
	}

	public boolean isCornerCircle() {
		return cornerCheck.isSelected();
	}

	public boolean isSquare() {
		return squareCheck.isSelected();
	}

	public SelectPanel(MainFrame frame) {
		super();
		// このパネルのサイズ設定
		setPreferredSize(new Dimension(250, 500));
		// 左揃えのFlowLayout
		setLayout(new FlowLayout(FlowLayout.LEFT));
		bEvent = new SelectButtonEvent(frame);

		// 色選択部分のパネル
		JPanel colorPanel = new JPanel();
		// パネルサイズ設定
		colorPanel.setPreferredSize(new Dimension(250, 50));
		// 左揃えのFlowLayout
		colorPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		// 背景色の設定
		colorPanel.setBackground(Color.LIGHT_GRAY);
		// このパネルに追加
		add(colorPanel);

		// 「選択色」のラベル
		JLabel selectcolor = new JLabel("選択色");
		// フォント設定
		selectcolor.setFont(MainFrame.font);
		// ラベルサイズ設定
		selectcolor.setPreferredSize(new Dimension(80, 30));
		// 文字を中央揃え
		selectcolor.setHorizontalAlignment(JLabel.CENTER);
		// 色選択パネルに追加
		colorPanel.add(selectcolor);

		// 色を表示するパネル
		color = new JPanel();
		// パネルサイズ設定
		color.setPreferredSize(new Dimension(40, 40));
		// 色設定 (デフォルトは黒)
		color.setBackground(Color.BLACK);
		// 色選択パネルに追加
		colorPanel.add(color);

		// 色変更ボタン
		changeColor = new JButton("変更");
		// フォントの設定
		changeColor.setFont(MainFrame.font);
		// ボタンイベントを受け取る
		changeColor.setActionCommand("色選択(S)");
		changeColor.addActionListener(bEvent);
		// 色選択パネルに追加
		colorPanel.add(changeColor);

		// 各種設定パネル
		etc = new JPanel();
		// パネルサイズ設定
		etc.setPreferredSize(new Dimension(250, 500));
		// 左揃えのFlowLayout
		etc.setLayout(new FlowLayout(FlowLayout.LEFT));
		// 背景色設定
		etc.setBackground(Color.LIGHT_GRAY);
		// このパネルに追加
		add(etc);

		mode = NONE;

		// 各種設定値の初期化
		thickness = 10;
		x = 0;
		y = 0;
		font = 0;
		fontSize = 30;
		line = 0;
		stamp = 0;
		stampSize = 100;
		isFill = false;
		isCornerCircle = true;
		isSquare = false;
	}

	/**
	 * 全体の有効無効を設定する
	 * @param enable
	 */
	public void setAllEnabled(boolean enable) {
		// 色変更ボタン
		changeColor.setEnabled(enable);
		if (enable) {
			changeMode(mode);
		} else {
			changeMode(NONE);
			// 各種設定を消す
			etc.removeAll();
			// 再描画
			revalidate();
			repaint();
		}
	}

	/**
	 * 描画モードを切り替える
	 * @param mode
	 */
	public void changeMode(int mode) {
		if (this.mode == mode) {
			return;
		}

		// モード変更前に行う処理 (設定値の保存)
		if (this.mode == PEN || this.mode == RAINBOW_PEN || this.mode == ERASER ||
				this.mode == LINE || this.mode == POLYGON || this.mode == RECT || this.mode == OVAL) {
			thickness = (int) thicknessSpinner.getValue();
		}
		if (this.mode == LINE || this.mode == POLYGON) {
			line = lineCombo.getSelectedIndex();
		}
		if (this.mode == RECT || this.mode == OVAL) {
			isFill = fillCheck.isSelected();
			isSquare = squareCheck.isSelected();
			if (this.mode == RECT) {
				isCornerCircle = cornerCheck.isSelected();
			}
		}
		if (this.mode == TEXT) {
			x = (int) xSpinner.getValue();
			y = (int) ySpinner.getValue();
			fontSize = (int) fontSizeSpinner.getValue();
			thickness = (int) thicknessSpinner.getValue();
		}
		if (this.mode == STAMP) {
			stamp = (int) stampConbo.getSelectedIndex();
			stampSize = (int) stampSizeSpinner.getValue();
		}

		this.mode = mode;
		// 各種設定パネルをリセット
		etc.removeAll();
		if (this.mode == PEN || this.mode == RAINBOW_PEN || this.mode == ERASER ||
				this.mode == LINE || this.mode == POLYGON || this.mode == RECT || this.mode == OVAL) {
			// 「太さ」ラベル
			JLabel thicknessLabel = new JLabel("太さ");
			// フォント設定
			thicknessLabel.setFont(MainFrame.font);
			// ラベルサイズ設定
			thicknessLabel.setPreferredSize(new Dimension(80, 30));
			// 各種設定パネルに追加
			etc.add(thicknessLabel);

			// 太さSpinner
			thicknessSpinner = new JSpinner(new SpinnerNumberModel(thickness, 1, 50, 1));
			// フォント設定
			thicknessSpinner.setFont(MainFrame.font);
			// Spinnerサイズ設定
			thicknessSpinner.setPreferredSize(new Dimension(100, 40));
			// 各種設定パネルに追加
			etc.add(thicknessSpinner);
		}
		if (mode == LINE || mode == POLYGON) {
			// 「線の形」ラベル
			JLabel thicknessLabel = new JLabel("線の形");
			// フォント設定
			thicknessLabel.setFont(MainFrame.font);
			// ラベルサイズ設定
			thicknessLabel.setPreferredSize(new Dimension(80, 30));
			// 各種設定パネルに追加
			etc.add(thicknessLabel);

			// 線の形ComboBox
			lineCombo = new JComboBox<String>(new String[] { "普通", "四角", "破線" });
			// フォント設定
			lineCombo.setFont(MainFrame.font);
			// ComboBoxサイズ設定
			lineCombo.setPreferredSize(new Dimension(240, 40));
			// デフォルト選択
			lineCombo.setSelectedIndex(line);
			// 各種設定パネルに追加
			etc.add(lineCombo);
		}
		if (mode == RECT || mode == OVAL) {
			// 「中を埋める」チェックボックス
			fillCheck = new JCheckBox("中を埋める");
			// フォント設定
			fillCheck.setFont(MainFrame.font);
			// 背景色設定
			fillCheck.setBackground(Color.LIGHT_GRAY);
			// デフォルト選択
			fillCheck.setSelected(isFill);
			// 各種設定パネルに追加
			etc.add(fillCheck);

			// 「正方形or円」チェックボックス
			squareCheck = new JCheckBox(mode == RECT ? "正方形" : "円");
			// フォント設定
			squareCheck.setFont(MainFrame.font);
			// 背景色設定
			squareCheck.setBackground(Color.LIGHT_GRAY);
			// デフォルト選択
			squareCheck.setSelected(isSquare);
			// 各種設定パネルに追加
			etc.add(squareCheck);

			if (mode == RECT) {
				// 「角を丸める」チェックボックス
				cornerCheck = new JCheckBox("角を丸める");
				// フォント設定
				cornerCheck.setFont(MainFrame.font);
				// 背景色設定
				cornerCheck.setBackground(Color.LIGHT_GRAY);
				// デフォルト選択
				cornerCheck.setSelected(isCornerCircle);
				// 各種設定パネルに追加
				etc.add(cornerCheck);
			}
		}
		if (mode == TEXT) {
			// 使用可能なフォントの一覧を取得
			fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();

			// 描画文字テキストエリア
			text = new JTextArea(5, 5);
			// フォント設定
			text.setFont(new Font("ＭＳ ゴシック", Font.BOLD, 18));
			// テキストエリアサイズ設定
			text.setPreferredSize(new Dimension(240, 150));
			// 各種設定パネルに追加
			etc.add(text);

			// ラベル
			JLabel xLabel = new JLabel("x座標");
			JLabel yLabel = new JLabel("y座標");
			JLabel sizeLabel = new JLabel("文字サイズ");
			JLabel fontLabel = new JLabel("フォント");
			// フォント設定
			xLabel.setFont(MainFrame.font);
			yLabel.setFont(MainFrame.font);
			sizeLabel.setFont(MainFrame.font);
			fontLabel.setFont(MainFrame.font);
			// ラベルサイズ設定
			xLabel.setPreferredSize(new Dimension(120, 40));
			yLabel.setPreferredSize(new Dimension(120, 40));
			sizeLabel.setPreferredSize(new Dimension(120, 40));
			fontLabel.setPreferredSize(new Dimension(120, 40));

			// x、y、フォントサイズSpinner
			xSpinner = new JSpinner(new SpinnerNumberModel(x, 0, null, 1));
			ySpinner = new JSpinner(new SpinnerNumberModel(y, 0, null, 1));
			fontSizeSpinner = new JSpinner(new SpinnerNumberModel(fontSize, 1, null, 1));
			// フォント設定
			xSpinner.setFont(MainFrame.font);
			ySpinner.setFont(MainFrame.font);
			fontSizeSpinner.setFont(MainFrame.font);
			// Spinnerサイズ設定
			xSpinner.setPreferredSize(new Dimension(80, 40));
			ySpinner.setPreferredSize(new Dimension(80, 40));
			fontSizeSpinner.setPreferredSize(new Dimension(80, 40));
			// 各種設定パネルに追加
			etc.add(xLabel);
			etc.add(yLabel);
			etc.add(sizeLabel);
			etc.add(fontLabel);

			// 使用可能なフォント名の配列
			String[] comboData = new String[fonts.length];
			for (int i = 0; i < comboData.length; i++) {
				comboData[i] = fonts[i].getName();
			}
			// 「フォント」ComboBox
			fontCombo = new JComboBox<String>(comboData);
			// フォント設定
			fontCombo.setFont(new Font("ＭＳ ゴシック", Font.BOLD, 12));
			// ComboBoxサイズ設定
			fontCombo.setPreferredSize(new Dimension(240, 40));
			// デフォルト選択
			fontCombo.setSelectedIndex(font);
			// 各種設定パネルに追加
			etc.add(xSpinner);
			etc.add(ySpinner);
			etc.add(fontSizeSpinner);
			etc.add(fontCombo);

			// テキスト決定ボタン
			JButton textOK = new JButton("決定");
			// フォント設定
			textOK.setFont(MainFrame.font);
			// ボタンサイズ設定
			textOK.setPreferredSize(new Dimension(120, 40));
			// ボタンイベントを受け取る
			textOK.setActionCommand("テキスト決定");
			textOK.addActionListener(bEvent);

			etc.add(textOK);
		} else if (mode == STAMP) {
			// 「形」ラベル
			JLabel shapeLabel = new JLabel("形");
			// フォント設定
			shapeLabel.setFont(MainFrame.font);
			// ラベルサイズ設定
			shapeLabel.setPreferredSize(new Dimension(80, 40));
			// 各種設定パネルに追加
			etc.add(shapeLabel);

			// 形ComboBox
			stampConbo = new JComboBox<String>(new String[] { "丸", "三角", "四角", "星型", "スペード", "ダイヤ", "ハート", "クローバー",
					"スマイル", "十字", "右矢印", "左矢印", "上矢印", "下矢印", "ランダム" });
			// フォント設定
			stampConbo.setFont(MainFrame.font);
			// ComboBoxサイズ設定
			stampConbo.setPreferredSize(new Dimension(130, 40));
			// デフォルト選択
			stampConbo.setSelectedIndex(stamp);
			// 各種設定パネルに追加
			etc.add(stampConbo);

			// 「サイズ」ラベル
			JLabel sizeLabel = new JLabel("サイズ");
			// フォント設定
			sizeLabel.setFont(MainFrame.font);
			// ラベルサイズ設定
			sizeLabel.setPreferredSize(new Dimension(80, 40));
			// 各種設定パネルに追加
			etc.add(sizeLabel);
			
			// スタンプサイズSpinner
			stampSizeSpinner = new JSpinner(new SpinnerNumberModel(stampSize, 1, 300, 1));
			// フォント設定
			stampSizeSpinner.setFont(MainFrame.font);
			// Spinnerサイズ設定
			stampSizeSpinner.setPreferredSize(new Dimension(130, 40));
			// 各種設定パネルに追加
			etc.add(stampSizeSpinner);
		}

		// 再描画
		revalidate();
		repaint();
	}

	/**
	 * 色設定
	 * @param c
	 */
	public void setColor(Color c) {
		// 表示色を更新
		color.setBackground(new Color(c.getRed(), c.getGreen(), c.getBlue()));
	}
}

/**
 * SelectPanel用ボタンイベントリスナー
 * @author atsut
 *
 */
class SelectButtonEvent implements ActionListener {
	public MainFrame frame;

	public SelectButtonEvent(MainFrame frame) {
		this.frame = frame;
	}

	public void actionPerformed(ActionEvent e) {
		frame.edit(e.getActionCommand());
	}
}
