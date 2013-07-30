package cz.cuni.mff.d3s.deeco.api;

import cz.cuni.mff.d3s.deeco.ProcessInfo;

public interface Scheduler {
	public void schedule(ProcessInfo pi);
	public void start();
	public void stop();
}
