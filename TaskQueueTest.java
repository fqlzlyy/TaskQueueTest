import java.util.ArrayDeque;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskQueueTest {

	// ���� BlockingQueue �������ڴ���ͱ����ύ�����񡣿���ʹ�ô˶�����ش�С���н�����
	// ������е��߳����� corePoolSize���� Executor ʼ����ѡ����µ��̣߳����������Ŷӡ�
	// ������е��̵߳��ڻ���� corePoolSize���� Executor ʼ����ѡ�����������У���������µ��̡߳�
	// ����޷������������У��򴴽��µ��̣߳����Ǵ������̳߳��� maximumPoolSize������������£����񽫱��ܾ���

	private static AtomicInteger threadCount = new AtomicInteger(0);
	private static final Integer MAX_COUNT = 0x03;
	private static final ThreadFactory sThreadFactory = new ThreadFactory() {
	     private final AtomicInteger mCount = new AtomicInteger(1);
	     public Thread newThread(Runnable r) {
			  // System.out.println("�̳߳��д����µ��߳�ȥ����>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>.");
			  return new Thread(r, "Thread Name #" + mCount.getAndIncrement());
	    }
	};
	private static ExecutorService executorService = Executors
			.newFixedThreadPool(2, sThreadFactory);
	public static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(
			2, 5, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(1),
			sThreadFactory);

	private static class PutTaskExecutor implements Executor {
		final ArrayDeque<Runnable> mTasks = new ArrayDeque<Runnable>();
		Runnable mActive;

		public synchronized void execute(final Runnable r) {
			mTasks.offer(new Runnable() {
				public void run() {
					try {
						r.run();
					} finally {
						threadCount.decrementAndGet();
						System.out.println("threadCount >> ����:"+ threadCount.get());
						System.out.println(Thread.currentThread().getName()+ "�߳�ִ�����--------------------------");
						scheduleNext();
					}
				}
			});
			System.out.println("mTasks >>>>>>>>>>>>>> ����:" + mTasks.size());
			if (threadCount.get() < MAX_COUNT) {
				scheduleNext();
			}
		}

		protected synchronized void scheduleNext() {
			if ((mActive = mTasks.poll()) != null) {
				threadCount.incrementAndGet();
				System.out.println("threadCount ����С���̳߳��̵߳ĸ��������̳߳���ִ��:"
						+ threadCount.get());
				executorService.submit(mActive);
			}
		}
	}

	public static void main(String args[]) throws InterruptedException {
		PutTaskExecutor executor = new PutTaskExecutor();
		for (int i=0 ;i < 10; i++){
			// ��ʼ��10������ÿ��������Ҫ����5����
			executor.execute(t1);
		}
	}

	static int threadNum = 0;

	private static Thread t1 = new Thread(new Runnable() {

		@Override
		public void run() {
			threadNum++;
			System.out.println("ִ�е�ǰ������̳߳��е��߳���: >> "+ Thread.currentThread().getName());
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	});

}
