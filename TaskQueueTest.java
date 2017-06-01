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

	// 所有 BlockingQueue 都可用于传输和保持提交的任务。可以使用此队列与池大小进行交互：
	// 如果运行的线程少于 corePoolSize，则 Executor 始终首选添加新的线程，而不进行排队。
	// 如果运行的线程等于或多于 corePoolSize，则 Executor 始终首选将请求加入队列，而不添加新的线程。
	// 如果无法将请求加入队列，则创建新的线程，除非创建此线程超出 maximumPoolSize，在这种情况下，任务将被拒绝。

	private static AtomicInteger threadCount = new AtomicInteger(0);
	private static final Integer MAX_COUNT = 0x03;
	private static final ThreadFactory sThreadFactory = new ThreadFactory() {
	     private final AtomicInteger mCount = new AtomicInteger(1);
	     public Thread newThread(Runnable r) {
			  // System.out.println("线程池中创建新的线程去处理>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>.");
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
						System.out.println("threadCount >> 个数:"+ threadCount.get());
						System.out.println(Thread.currentThread().getName()+ "线程执行完毕--------------------------");
						scheduleNext();
					}
				}
			});
			System.out.println("mTasks >>>>>>>>>>>>>> 个数:" + mTasks.size());
			if (threadCount.get() < MAX_COUNT) {
				scheduleNext();
			}
		}

		protected synchronized void scheduleNext() {
			if ((mActive = mTasks.poll()) != null) {
				threadCount.incrementAndGet();
				System.out.println("threadCount 个数小于线程池线程的个数进入线程池中执行:"
						+ threadCount.get());
				executorService.submit(mActive);
			}
		}
	}

	public static void main(String args[]) throws InterruptedException {
		PutTaskExecutor executor = new PutTaskExecutor();
		for (int i=0 ;i < 10; i++){
			// 初始化10个任务，每个任务需要花费5秒钟
			executor.execute(t1);
		}
	}

	static int threadNum = 0;

	private static Thread t1 = new Thread(new Runnable() {

		@Override
		public void run() {
			threadNum++;
			System.out.println("执行当前任务的线程池中的线程是: >> "+ Thread.currentThread().getName());
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	});

}
