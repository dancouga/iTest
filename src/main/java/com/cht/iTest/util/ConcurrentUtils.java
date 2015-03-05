package com.cht.iTest.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 程式資訊摘要：<P>
 * 類別名稱　　：ConcurrentUtils.java<P>
 * 程式內容說明：<P>
 * 程式修改記錄：<P>
 * XXXX-XX-XX：<P>
 *@author wenyangkao
 *@version 1.0
 *@since 1.0
 */
public class ConcurrentUtils {
    
    private static ExecutorService EXECUTOR_SERIVCE = Executors.newFixedThreadPool(3);
    
    /**
     * 同時啟動多執行緒執行多個Callable<T>，並收集所有Callable<T>之回傳結果，待所有Callable執行結束後，進行結果集回傳。
     * 
     * @param callables Callable<T>...callables
     * @return List<T> 結果集
     * @throws InterruptedException
     * @throws ExecutionException
     */
	@SafeVarargs
	public static <T> List<T> forkCallablesJoin(Callable<T>... callables) throws InterruptedException, ExecutionException {
        ExecutorCompletionService<T> ecs = submitCallables(callables);
        List<T> results = new ArrayList<T>();
        
        for (int i = 0; i < callables.length; i++) {
            results.add(ecs.take().get());
        }
        
        return results;
    }
	
	public static <T> List<T> forkCallablesJoin(List<? extends Callable<T>> callables) throws InterruptedException, ExecutionException {
        ExecutorCompletionService<T> ecs = submitCallables(callables);
        List<T> results = new ArrayList<T>();
        
        for (int i = 0; i < callables.size(); i++) {
            results.add(ecs.take().get());
        }
        
        return results;
    }
	

    /**
     * 同時啟動多執行緒執行多個Callable<T>，只要取得一Callable<T>之執行結果，立刻回傳該結果。
     * 
     * @param callables Callable<T>...callables
     * @return T<T> 結果
     * @throws InterruptedException
     * @throws ExecutionException
     */
	@SafeVarargs
	public static <T> T forkCallablesAny(Callable<T>... callables) throws InterruptedException, ExecutionException {
        return EXECUTOR_SERIVCE.invokeAny(Arrays.asList(callables));
    }

    /**
     * 同時啟動多執行緒執行多個Runnable，並等待所有Runnable結束。
     * 
     * @param runnables Runnable...runnables
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public static void forkRunnablesJoin(Runnable... runnables) throws InterruptedException, ExecutionException {
        ExecutorCompletionService<Void> ecs = submitRunnables(runnables);

        for (int i = 0; i < runnables.length; i++) {
            ecs.take().get();
        }
    }
    
    public static void forkRunnablesJoin(List<Runnable> runnables) throws InterruptedException, ExecutionException {
        ExecutorCompletionService<Void> ecs = submitRunnables(runnables);

        for (int i = 0; i < runnables.size(); i++) {
            ecs.take().get();
        }
    }

    /**
     * 非同步啟動多執行緒執行多個Runnable
     * 
     * @param runnables Runnable...runnables
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public static void forkRunnableAsync(Runnable... runnables) throws InterruptedException, ExecutionException {
        submitRunnables(runnables);
    }

    private static <T> ExecutorCompletionService<T> submitRunnables(Runnable... runnables) {
        ExecutorCompletionService<T> ecs = new ExecutorCompletionService<T>(EXECUTOR_SERIVCE);

        for (Runnable runnable : runnables) {
            ecs.submit(runnable, null);
        }

        return ecs;
    }
    
    private static <T> ExecutorCompletionService<T> submitRunnables(List<? extends Runnable> runnables) {
        ExecutorCompletionService<T> ecs = new ExecutorCompletionService<T>(EXECUTOR_SERIVCE);

        for (Runnable runnable : runnables) {
            ecs.submit(runnable, null);
        }

        return ecs;
    }

	@SafeVarargs
	private static <T> ExecutorCompletionService<T> submitCallables(Callable<T>... callables) {
        ExecutorCompletionService<T> ecs = new ExecutorCompletionService<T>(EXECUTOR_SERIVCE);

        for (Callable<T> callable : callables) {
            ecs.submit(callable);
        }

        return ecs;
    }
    
	private static <T> ExecutorCompletionService<T> submitCallables(List<? extends Callable<T>> callables) {
        ExecutorCompletionService<T> ecs = new ExecutorCompletionService<T>(EXECUTOR_SERIVCE);

        for (Callable<T> callable : callables) {
            ecs.submit(callable);
        }

        return ecs;
    }
}
