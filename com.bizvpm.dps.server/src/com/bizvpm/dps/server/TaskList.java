package com.bizvpm.dps.server;

import java.util.List;

public class TaskList {
	
	List<PersistableTask> tasks;

	public List<PersistableTask> getTasks() {
		return tasks;
	}
	
	public void setTasks(List<PersistableTask> tasks) {
		this.tasks = tasks;
	}
}
