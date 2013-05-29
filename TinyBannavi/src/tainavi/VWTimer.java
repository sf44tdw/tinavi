package tainavi;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.Timer;

/**
 * 毎分00秒に処理をキックするタイマー
 */
public class VWTimer {

	public void setInterval(int min) { timer_interval = min; }
	private int timer_interval = 1;				// 何分ごとにキックするか
	
	public void setDelay(int n) { timer_delay_add = n; }
	private int timer_delay_add = 100;			// 実際には毎分00秒ではなく00.1秒にキックするよ
	
	private Timer timer_now = null;				// タイマーのオブジェクト
	
	private ArrayList<VWTimerRiseListener> listener_list = new ArrayList<VWTimerRiseListener>(); 
	
	/**
	 * 次の00秒までの時間(ミリ秒)を計算する
	 */
	private int getNextDelay() {
		return timer_interval*60000 - (int) (new Date().getTime() % (long) (timer_interval*60000));
	}
	
	/**
	 * タイマー起動
	 */
	public int start() {
		
		if ( timer_now == null ) {
			// 新規のタイマー
			timer_now = new Timer(0, al_nowtimer);	// TIMER_INTERVALの値はなんでもいい
			timer_now.setRepeats(false);	// 一回しか実行しないよ
		}
		
		int delay = getNextDelay()+timer_delay_add;
		timer_now.setInitialDelay(delay);
		
		timer_now.start();
		
		return delay;
	}
	
	/**
	 * タイマー停止
	 */
	public boolean stop() {
		if ( timer_now != null ) {
			timer_now.stop();
			timer_now = null;
			
			return true;
		}
		return false;
	}
	
	/**
	 * タイマー一時停止
	 */
	public boolean pause() {
		if ( timer_now != null && timer_now.isRunning() ) {
			timer_now.stop();
			
			return true;
		}
		return false;
	}
	
	/**
	 * タイマーで実行する内容を追加
	 */
	public void addVWTimerRiseListener(VWTimerRiseListener l) {
		if ( ! listener_list.contains(l) ) {
			listener_list.add(l);
		}
	}
	
	/**
	 * タイマーで実行する内容を削除
	 */
	public void removeVWTimerRiseListener(VWTimerRiseListener l) {
		listener_list.remove(l);
	}
	
	/**
	 * タイマーで実行する内容
	 */
	private final ActionListener al_nowtimer = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			
			VWTimerRiseEvent ev = new VWTimerRiseEvent(this);
			
			for ( VWTimerRiseListener l : listener_list ) {
				l.timerRised(ev);
			}
			
			int delay = start();
			//System.out.println("Timer Rised: now="+CommonUtils.getDateTimeYMD(ev.getCalendar())+" delay="+delay);
		}
	};

}
