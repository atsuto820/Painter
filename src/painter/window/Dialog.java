package painter.window;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import painter.main.Main;

/**
 * ダイアログ
 * @author atsuto
 *
 */
public class Dialog extends JDialog {
	JSpinner[] spinners;
	//SpinnerNumberModel[] models;
	JColorChooser colorChooser;
	JComboBox<String> combobox;
	JTextField text;

	/**
	 * ダイアログの初期化
	 * @param frame
	 * @param bEvent
	 * @param type
	 */
	public Dialog(MainFrame frame, ButtonEvent bEvent, String type) {
		super(frame, type);
		setModal(true);
		// リサイズ不可
		setResizable(false);
		bEvent.setDialog(this);

		// ダイアログで使用するフォント
		Font font = new Font("ＭＳ ゴシック", Font.BOLD, 16);
		// ダイアログに配置される要素
		JPanel[] panels;
		JLabel[] labels;
		JButton[] buttons;
		
		switch (type) {
		case "新規作成":
			// ダイアログサイズ設定
			setSize(400, 200);
			// 中央揃えFlowLayout
			setLayout(new FlowLayout(FlowLayout.CENTER));
			
			panels = new JPanel[2];
			labels = new JLabel[2];
			buttons = new JButton[2];
			spinners = new JSpinner[2];
			
			// サイズ設定部分のパネル
			panels[0] = new JPanel();
			// OK、キャンセルボタンのパネル
			panels[1] = new JPanel();
			// パネルサイズ設定
			panels[0].setPreferredSize(new Dimension(300, 80));	
			panels[1].setPreferredSize(new Dimension(300, 80));
			// レイアウトグループ設定
			panels[0].setLayout(new FlowLayout(FlowLayout.CENTER));
			panels[1].setLayout(new FlowLayout(FlowLayout.CENTER));
			// パネルをダイアログに加える
			add(panels[0]);
			add(panels[1]);
			
			// ラベル作成
			labels[0] = new JLabel("サイズ");
			labels[1] = new JLabel("×");
			// フォント設定
			labels[0].setFont(font);
			labels[1].setFont(font);
			// ラベルサイズ設定
			labels[0].setPreferredSize(new Dimension(100, 50));
			labels[1].setPreferredSize(new Dimension(20, 50));
			// Spinner作成 (数値デフォルト500)
			spinners[0] = new JSpinner(new SpinnerNumberModel(500, 1, null, 1));
			spinners[1] = new JSpinner(new SpinnerNumberModel(500, 1, null, 1));
			// フォント設定
			spinners[0].setFont(font);
			spinners[1].setFont(font);
			// Spinnerサイズ設定
			spinners[0].setPreferredSize(new Dimension(80, 30));
			spinners[1].setPreferredSize(new Dimension(80, 30));

			// サイズ設定部分の要素をパネルに加える
			panels[0].add(labels[0]);	// サイズ
			panels[0].add(spinners[0]);	// [縦]
			panels[0].add(labels[1]);	// ×
			panels[0].add(spinners[1]);	// [横]

			// ボタン作成
			buttons[0] = new JButton("OK");
			buttons[1] = new JButton("キャンセル");
			// フォント設定
			buttons[0].setFont(font);
			buttons[1].setFont(font);
			// 押されたときのイベントの設定
			buttons[0].setActionCommand("新規作成決定");
			buttons[1].setActionCommand("ダイアログ消去");
			buttons[0].addActionListener(bEvent);
			buttons[1].addActionListener(bEvent);
			// ボタンサイズ設定
			buttons[0].setPreferredSize(new Dimension(120, 40));
			buttons[1].setPreferredSize(new Dimension(120, 40));
			
			// OK、キャンセルボタンの要素をパネルに加える
			panels[1].add(buttons[0]);
			panels[1].add(buttons[1]);
			break;

		case "色選択":
			// ダイアログサイズ設定
			setSize(700, 400);
			// BorderLayout
			setLayout(new BorderLayout());

			panels = new JPanel[1];
			buttons = new JButton[2];
			
			// 色選択コンポーネント
			colorChooser = new JColorChooser();
			// OK、キャンセルボタンのパネル
			panels[0] = new JPanel();
			
			// 色選択コンポーネントとパネルをダイアログに加える
			add(colorChooser, BorderLayout.CENTER);
			add(panels[0], BorderLayout.SOUTH);

			// ボタン作成
			buttons[0] = new JButton("OK");
			buttons[1] = new JButton("キャンセル");
			// フォント設定
			buttons[0].setFont(font);
			buttons[1].setFont(font);
			// 押されたときのイベントの設定
			buttons[0].setActionCommand("色選択");
			buttons[1].setActionCommand("ダイアログ消去");
			buttons[0].addActionListener(bEvent);
			buttons[1].addActionListener(bEvent);
			
			// OK、キャンセルボタンの要素をパネルに加える
			panels[0].add(buttons[0]);
			panels[0].add(buttons[1]);
			break;
		case "レイヤー名変更":
			// ダイアログサイズ設定
			setSize(400, 150);
			// 中央揃えFlowLayout
			setLayout(new FlowLayout(FlowLayout.CENTER));
			
			panels = new JPanel[2];
			buttons = new JButton[1];
			
			// レイヤー名入力欄パネル
			panels[0] = new JPanel();
			// OKボタンのパネル
			panels[1] = new JPanel();
			// パネルサイズ設定
			panels[0].setPreferredSize(new Dimension(380, 40));
			panels[1].setPreferredSize(new Dimension(380, 50));
			// レイアウトグループ設定
			panels[0].setLayout(new FlowLayout(FlowLayout.CENTER));
			panels[1].setLayout(new FlowLayout(FlowLayout.CENTER));
			// パネルをダイアログに加える
			add(panels[0]);
			add(panels[1]);
			
			// レイヤー名入力欄
			text = new JTextField();
			// フォント設定
			text.setFont(new Font("ＭＳ ゴシック", Font.BOLD, 18));
			// TextFieldサイズ設定
			text.setPreferredSize(new Dimension(360, 30));
			// 入力欄にレイヤー名を設定しておく
			text.setText(Main.frame.lpanel.getLayerName());
			// レイヤー名入力欄をパネルに加える
			panels[0].add(text);
			
			// OKボタン
			buttons[0] = new JButton("OK");
			// フォント設定
			buttons[0].setFont(font);
			// 押されたときのイベントの設定
			buttons[0].setActionCommand("レイヤー名変更決定");
			buttons[0].addActionListener(bEvent);
			// ボタンサイズ設定
			buttons[0].setPreferredSize(new Dimension(120, 40));
			
			// OKボタンをパネルに加える
			panels[1].add(buttons[0]);
			break;
		}

		// ウィンドウ位置を中心にする
		setLocationRelativeTo(null);
		// 可視化
		setVisible(true);
	}
}
