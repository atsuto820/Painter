package painter.window;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import painter.main.Main;

/**
 * ボタンイベントの処理を行う
 * @author atsuto
 */
public class ButtonEvent implements ActionListener {
	private MainFrame frame;
	private Dialog dialog;

	public ButtonEvent(MainFrame frame, Dialog dialog) {
		this.frame = frame;
		this.dialog = dialog;
	}

	public void setDialog(Dialog dialog) {
		this.dialog = dialog;
	}

	/**
	 * ActionEventを受け取った
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().charAt(0) == 'T') {
			// ダイアログ以外のボタン
			Main.frame.edit(e.getActionCommand().substring(1));
		} else {
			// ダイアログのボタン
			switch (e.getActionCommand()) {
			case "新規作成決定":
				// 画像を新規作成
				BufferedImage image = new BufferedImage((Integer) dialog.spinners[0].getValue(),
						(Integer) dialog.spinners[1].getValue(), BufferedImage.TYPE_INT_ARGB);
				Main.newImage(image);
				frame.panel.repaint();
				
				// ボタンなどを有効にする
				frame.setEnabled(true);
				break;
			case "色選択" :
				// 選択された色
				Color color = dialog.colorChooser.getColor();
				Main.pm.getPT().setColor(color);	// ペンの色を変える
				frame.spanel.setColor(color);		// ウィンドウの選択色の表示色を変える
				break;
			case "レイヤー名変更決定" :
				frame.lpanel.setLayerName(dialog.text.getText(), true);
				break;
			}
			
			if(dialog != null) {
				// ダイアログを閉じる
				dialog.dispose();
			}
		}
	}
}
