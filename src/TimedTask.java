public interface TimedTask {
	public void runTask();
	public long getNextRun();
	public void schedule();
	public void unschedule();
}
