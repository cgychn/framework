package com.framework.util;

import com.framework.config.MyFrameworkCfgContext;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

//framework.threadPool.size

public class ThreadPool {

	private static List<MyTask> threads = new ArrayList();
	private static List<Runnable> taskQueue = new LinkedList<>();
	public static AtomicInteger available = new AtomicInteger(0);
	private static int poolSize = MyFrameworkCfgContext.get("framework.threadPool.size", Integer.class) == null ?
			36 :
			MyFrameworkCfgContext.get("framework.threadPool.size", Integer.class);
	private static ThreadPool instance = null;

	private ThreadPool () {}

	/**
	 * 拿线程池的单例
	 * @return
	 */
	public synchronized static ThreadPool getInstance() {
		if (instance == null) {
			instance = new ThreadPool();
		}
		return instance;
	}

	/**
	 * 向线程池中插入新的空闲线程
	 */
	private void addFreeThreadToPool () {
		synchronized (threads) {
			if (threads.size() < poolSize) {
				MyTask myTask = this.new MyTask();
				threads.add(myTask);
				myTask.start();
				available.getAndAdd(1);
			}
//			System.out.println("线程池中线程数量：" + threads.size() + "；队列数量：" + taskQueue.size() + "；可用线程数量：" + available.get());
			threads.notifyAll();
		}
	}

	/**
	 * 从线程池中移除空闲任务
	 * @param task
	 */
	protected void removeFreeThreadFromPool (MyTask task) {
		synchronized (threads) {
			try {
				threads.remove(task);
				available.getAndAdd(-1);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
//				System.out.println("销毁了一个线程：" + threads.size() + "；队列数量：" + taskQueue.size() + "；可用线程数量：" + available.get());
				threads.notifyAll();
			}
		}
	}

	/**
	 * 执行多任务
	 * @param threadList
	 */
	public void exeTasks(List<Runnable> threadList) {
		synchronized (taskQueue) {
			for(Runnable thread : threadList) {
				taskQueue.add(thread);
				if (taskQueue.size() > threads.size()) {
					addFreeThreadToPool();
				}
			}
			taskQueue.notifyAll();
		}

	}

	/**
	 * 执行单任务
	 * @param thread
	 */
	public void exeTask(Runnable thread) {
		synchronized (taskQueue) {
			taskQueue.add(thread);
			if (taskQueue.size() > available.get()) {
				addFreeThreadToPool();
			}
			taskQueue.notifyAll();
		}
	}

	@Override
	public String toString() {
		return "可用线程数量为：" + ThreadPool.available.get() +
				"; 线程池中线程数量为：" + ThreadPool.threads.size() +
				"; 任务队列数量为：" + taskQueue.size() +
				"; 线程池最大并行处理任务数量：" + ThreadPool.poolSize;
	}


	/**
	 * 任务类
	 */
	class MyTask extends Thread{
		private volatile boolean canRun = true;
		private volatile boolean runningState = false;
		//运行任务
		@Override
		public void run() {
			while (this.canRun) {
				// 从任务队列中抢占一个任务
				Runnable task = null;
				synchronized (taskQueue) {
					// 线程空转次数
					int waitTime = 0;
					// 扫描任务队列
					while (this.canRun && taskQueue.isEmpty()) {
						try {
							taskQueue.wait(10);
							// 等待超时判断，如果线程空闲时间太长，就销毁线程，并从线程池中移除 10 * 3000 ms （30秒没拿到任务就销毁自己）
							waitTime++;
							if (waitTime == 3000) {
								removeFreeThreadFromPool(this);
								this.canRun = false;
								return;
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
							// 出错将当前线程移除
							removeFreeThreadFromPool(this);
							this.canRun = false;
							return;
						}
					}
					if (!taskQueue.isEmpty()) {
						//从队列中取得一个任务执行
						task = taskQueue.remove(0);
					}
					taskQueue.notifyAll();
				}
				// 如果抢占任务失败，task为空
				try {
					if (task != null) {
						// 取到任务后，线程进入工作状态，available -1
						ThreadPool.available.getAndAdd(-1);
						//设置当前的线程的运行状态为运行
						this.runningState = true;
						task.run();
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					//运行完毕后，将线程的状态置为空闲
					this.runningState = false;
					// available +1
					ThreadPool.available.getAndAdd(1);
				}
			}

		}

		/**
		 * 注销线程
		 */
		public void disableThisThread() {
			//等待线程运行完毕
			while (!this.runningState) {
				this.canRun = false;
				break;
			}
		}

		/**
		 * 重新激活
		 */
		public void reActiveThisThread() {
			this.canRun = true;
		}
	}

}
