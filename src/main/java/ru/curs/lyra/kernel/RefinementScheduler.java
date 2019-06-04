package ru.curs.lyra.kernel;

import java.util.concurrent.Callable;
import java.util.concurrent.DelayQueue;

/**
 * Schedules and executes grid refinement tasks in separate thread.
 */
public abstract class RefinementScheduler implements Callable<Void> {
    private final DelayQueue<RefinementTask> queue = new DelayQueue<>();

    private RefinementTask freshest(RefinementTask task) {
        RefinementTask result = task;
        RefinementTask fresherTask = task;
        while (fresherTask != null) {
            if (fresherTask.isImmediate()) {
                return fresherTask;
            }
            result = fresherTask;
            fresherTask = queue.poll();
        }
        return result;
    }

    /**
     * Calls interpolator refiner.
     * @return false, if no further interpolator refinement needed.
     */
    protected abstract boolean refineInterpolator();

    /**
     * Performs single point refinement.
     * @param task Refinement task
     */
    protected abstract void refineAndNotify(RefinementTask task);

    /**
     * Adds task to scheduler.
     * @param task Delayed refinement task
     */
    public void setTask(RefinementTask task) {
        queue.put(task);
    }

    @Override
    public Void call() throws InterruptedException {
        //if the next queue poll should be blocking or non-blocking
        boolean block = false;

        while (true) {
            RefinementTask task = freshest(
                    block ? queue.take() : queue.poll());
            if (task == null) {
                //nothing to do yet, refine interpolator
                block = !refineInterpolator();
                //if no refinement done - block until new tasks arrive
            } else if (task.isImmediate()) {
                //refine & notify
                refineAndNotify(task);
                block = false;
            } else {
                if (queue.size() > 0) {
                    //we will wait, in the meantime, refine interpolator
                    block = !refineInterpolator();
                } else {
                    refineAndNotify(task);
                    block = false;
                }
            }
        }
    }
}
