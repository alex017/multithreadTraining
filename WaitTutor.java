package terminat;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import org.junit.Test;

/**
 * Как сделать так, чтобы потоки вызывались по очереди?
 * 
 * Часто необходимо упорядочить потоки, т.к. результат одного потока
 * понадобится другому, и нужно дождаться, когда первый поток сделает свою работу.
 * 
 * Задача: добавьте еще один поток, который будет выводить в лог сообщения о 
 * 	значениях счетчика, кратных 10, например 10, 20, 30...
 * При этом такие сообщения должны выводиться после того, как все потоки преодолели
 * кратность 10, но до того, как какой-либо поток двинулся дальше.
 */
public class WaitTutor {
	Thread t1, t2;
	Object monitor = new Object();
	int runningThreadNumber = 1;
	int t1Counter = 0, t2Counter = 0;

	class ThirdAction implements Runnable {
		int n = 0;		
		@Override
		public void run() {
			System.out.println(n);
			n = n + 10;
		}		
	}
	
	class TestThread implements Runnable {
		String threadName;
		int n;
		CyclicBarrier cb;
		int counter; //added counter inside stream
		
		public TestThread(String threadName, int n, CyclicBarrier cb) {
			this.threadName = threadName;
			this.n = n;
			this.cb = cb;
		}
		
		
		@Override
		public void run() {
			for (int i = 0; i < 100; i++) {
				counter = i;
				System.out.println(threadName+":"+i);
					if ((counter % 10) == 0) {
						try {
							cb.await();
						} catch (InterruptedException | BrokenBarrierException e) {
							e.printStackTrace();
						}
					}
				}
			}
			
		}
	/*	
	 * 
	 * was this
	 * 
	@Override
	public void run() {
		for (int i=0;i<100;i++) {
			System.out.println(threadName+":"+i);
			synchronized(monitor) {
				if (n==1) t1Counter = i;
				if (n==2) t2Counter = i;
				monitor.notify();
				Thread.yield();
				try {
					if (n==1) {
						if (i>t2Counter) {
							System.out.println("t1 is ahead with i="+i+", wait for t2Counter="+t2Counter);
							monitor.wait();
						}
					}
					if (n==2) {
						if (i>t1Counter) {
							System.out.println("t2 is ahead with i="+i+", wait for t1Counter="+t1Counter);
							monitor.wait();
						}
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			Thread.yield();
		}
	}
}
*/	
	@Test
	public void testThread() {
		CyclicBarrier cb = new CyclicBarrier(2, new ThirdAction());//added barrier
		t1 = new Thread(new TestThread("t1", 1, cb));
		t2 = new Thread(new TestThread("t2", 2, cb));
	    System.out.println("Starting threads");
		t1.start();
		t2.start();

		System.out.println("Waiting for threads");
	    try {
			t1.join();
			t2.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
