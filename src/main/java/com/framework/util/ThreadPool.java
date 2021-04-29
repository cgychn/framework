package com.framework.util;

import com.framework.config.MyFrameworkCfgContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPool {

	//这是线程池，用于维护一些线程
	private static List<MyTask> threads = new ArrayList<MyTask>();
	//这是线程池的实例
	private static ThreadPool thPoolInstance = null;
	//这是任务队列
	private List<Runnable> taskQueue = new ArrayList<Runnable>();
	//线程池中可用线程，使用线程安全型int记录
	public static AtomicInteger available = new AtomicInteger(0);
	public static AtomicInteger taskExecuted = new AtomicInteger(0);
	private static int poolSize = 0;

	//私有化构造方法
	private ThreadPool() {

	}

	//得到线程池的单例
	public static synchronized ThreadPool getThreadPoolInstance(int poolSize) {
		if(ThreadPool.thPoolInstance == null) {
			System.out.println("线程池初始化中。。。。。。");
			ThreadPool.thPoolInstance = new ThreadPool();
			//初始化poolSize个的线程池
			for(int i = 0 ; i < poolSize ; i++) {
				ThreadPool.MyTask myTask = ThreadPool.thPoolInstance.new MyTask();
				myTask.setName("Thread-->" + i);
				threads.add(myTask);
			}
			ThreadPool.poolSize = poolSize;
			//设置线程池中可用的线程数量
			ThreadPool.available.getAndAdd(poolSize);
			System.out.println("线程池初始化完毕，开始启动线程池。。。。。。");
			for(ThreadPool.MyTask myTask : threads) {
				myTask.start();
			}
		}else {
			System.out.println("线程池已经被初始化了");
		}
		return ThreadPool.thPoolInstance;
	}

	public static synchronized ThreadPool getThreadPoolInstance() {
		Integer poolSize = MyFrameworkCfgContext.get("framework.threadpool.size", Integer.class);
		if (poolSize == null || poolSize.intValue() < 0) {
			poolSize = 20;
		}
		return getThreadPoolInstance(poolSize);
	}


	//执行任务
	public void exeTasks(ArrayList<Runnable> threadList) {
		//在任务队列中添加任务
		synchronized (taskQueue) {
			for(Runnable thread : threadList) {
				taskQueue.add(thread);
				taskExecuted.getAndAdd(1);
			}
			taskQueue.notifyAll();
		}

	}

	//执行单个任务
	public void exeTask(Runnable thread) {
		//在任务队列中添加任务
		synchronized (taskQueue) {
			taskQueue.add(thread);
			taskExecuted.getAndAdd(1);
			taskQueue.notifyAll();
		}

	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "可用线程数量为：" + ThreadPool.available;
	}


	//线程内部类（用于处理任务）
	class MyTask extends Thread{

		private volatile boolean canRun = true;

		private volatile boolean runningState = false;

		//运行任务
		@Override
		public void run() {
			// TODO Auto-generated method stub
			//从任务队列中取出一个任务执行
			try {
				while (this.canRun) {

					//如果任务队列中获取一个任务执行
					Runnable thread = null;
					synchronized (taskQueue) {
						while(this.canRun && taskQueue.isEmpty()) {
							//System.out.println("可用线程数量为：" + ThreadPool.available.get());
							//进入等待，等待任务到达
							taskQueue.wait(10);
							if (ThreadPool.available.get() == ThreadPool.poolSize) {
								//线程可以被执行，并且所有的任务都完成了
								//System.out.println("一共执行了： " + taskExcuted.get() + " 个任务");
//								for (HashMap<String, String> hashMap : DataCollectTask.resList) {
//									System.out.println(hashMap);
//								}
								//这里说明所有的任务都被执行完毕
							}
						}
						if(!taskQueue.isEmpty()) {
							//从队列中取得一个任务执行
							thread = taskQueue.remove(0);
							ThreadPool.available.getAndAdd(-1);
							//System.out.println("(拿取)空闲线程数量为：" + ThreadPool.available.get());
							//System.out.println("待执行任务数量为：" + taskQueue.size());
						}
						taskQueue.notifyAll();
					}
					if(thread != null) {
						//设置当前的线程的运行状态为运行
						this.runningState = true;
						thread.run();
						ThreadPool.available.getAndAdd(1);
						//System.out.println("(返回)空闲线程数量为：" + ThreadPool.available.get());
					}
					thread = null;
					//运行完毕后，将线程的状态置为空闲
					this.runningState = false;
				}
			}catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}finally{
				//出错，将线程的状态置为未运行
				this.runningState = false;
			}
		}

		//注销线程
		public void disableThisThread() {
			// TODO Auto-generated method stub
			//等待线程运行完毕
			while (!this.runningState) {
				this.canRun = false;
				break;
			}
		}

		//重新激活该线程
		public void reActiveThisThread() {
			this.canRun = true;
		}
	}

}
