package painter.window;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * クリックイベントの処理を行う
 * @author atsut
 *
 */
public class ClickEvent implements MouseListener, MouseMotionListener {
	// クリック情報
	// [0] [1] マウスクリック位置(x, y)
	// [2] [3] 前回マウスクリック位置(x, y)
	// クリックされていない場合は(-1, -1)
	// 最後にされたクリックが右クリックの場合は(-1, 0)
	private int[] click;
	
	// 現在フレームのマウスイベント認識状態
	// -1 : 認識なし
	// 0 : マウスクリック(または離す)イベント認識
	// 1 : マウスドラッグイベント認識
	private int state;
	
	// マウス位置
	private int x, y;
	
	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int[] getClick() {
		return click;
	}
	
	public ClickEvent() {
		click = new int[]{ -1, -1, -1, -1 };
		state = -1;
	}
	
	/**
	 * クリック情報を更新して返す
	 * @return クリック情報
	 */
	public int[] updateClick() {
		int[] currentClick = {click[0], click[1], click[2], click[3]};
		if(click[0] == -1) {
			click[2] = -1;
			click[3] = -1;
		}
		state = -1;
		return currentClick;
	}
	
	/**
	 * マウスが押された
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		if(state == 1) {
			return;
		}
		click[2] = -1;
		click[3] = -1;
		click[0] = e.getX();
		click[1] = e.getY();
		
		state = 0;
	}
	
	/**
	 * マウスが離された
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		click[2] = click[0];
		click[3] = click[1];
		click[0] = -1;
		click[1] = (e.getButton() == MouseEvent.BUTTON1 ? -1 : 0);		// 右クリックと左クリックで変える
		
		state = 1;
	}
	
	/**
	 * マウスがドラッグされた
	 */
	@Override
	public void mouseDragged(MouseEvent e) {
		if(state == 0) {
			return;
		}
		if(state == -1) {
			click[2] = click[0];
			click[3] = click[1];
		}
		click[0] = e.getX();
		click[1] = e.getY();
		state = 1;
		
		// マウス位置を更新
		x = e.getX();
		y = e.getY();
	}
	
	public void clear() {
		click[2] = -1;
		click[3] = -1;
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// マウス位置を更新
		x = e.getX();
		y = e.getY();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {		
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}
}
