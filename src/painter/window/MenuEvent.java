package painter.window;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * メニューイベント
 * @author atsuto
 *
 */
public class MenuEvent implements ActionListener {
	private MainFrame frame;
	
	public MenuEvent(MainFrame frame) {
		this.frame = frame;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// メインウィンドウにイベントを追加
		frame.event = e.getActionCommand();
	}
}
