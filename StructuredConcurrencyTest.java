class StructuredConcurrencyTest{

  public static void main(String[] args) {

		// Even though main thread is interrupted subtasks are stopped successfully.
		// inturruptMain();
//		allChildCompletes();
//		firstSubTaskFails();
		firstSubTaskSucceed();
	}

	private static void customeTaskScope() {
		try (var taskScope = new CustomeTaskScope()) {
			var taskOne = new LongRunningTask("Weather-1", 3, "100", true);
			var taskTwo = new LongRunningTask("Weather-2", 5, "200", false);
			var taskThree = new LongRunningTask("Weather-3", 7, "300", false);

			taskScope.fork(taskOne);
			taskScope.fork(taskTwo);
			taskScope.fork(taskThree);
			
			taskScope.join();
			
			System.out.println("Result => "+taskScope.response());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void firstSubTaskSucceed() {
		try (var taskScope = new StructuredTaskScope.ShutdownOnSuccess<TaskResponse>()) {
			var taskOne = new LongRunningTask("TaskOne", 3, "response1", false);
			var taskTwo = new LongRunningTask("TaskTwo", 5, "response2", false);

			taskScope.fork(taskOne);
			taskScope.fork(taskTwo);
			
			taskScope.join();
			
			System.out.println("Result => "+taskScope.result());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void firstSubTaskFails() {
		try (var taskScope = new StructuredTaskScope.ShutdownOnFailure()) {
			var taskOne = new LongRunningTask("TaskOne", 3, "response1", true);
			var taskTwo = new LongRunningTask("TaskTwo", 5, "response2", false);

			var subTask1 = taskScope.fork(taskOne);
			var subTask2 = taskScope.fork(taskTwo);
			
			taskScope.join();
			taskScope.throwIfFailed();
			
			System.out.println("Task1 => "+subTask1.get());
			System.out.println("Task2 => "+subTask2.get());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void allChildCompletes() {
		try (var taskScope = new StructuredTaskScope<TaskResponse>()) {
			var taskOne = new LongRunningTask("TaskOne", 3, "response1", false);
			var taskTwo = new LongRunningTask("TaskTwo", 10, "response2", false);

			Subtask<TaskResponse> subTask1 = taskScope.fork(taskOne);
			var subTask2 = taskScope.fork(taskTwo);

			/**
			 * Program would be waiting for all the tasks to be completed to get results
			 * even though some tasks are already completed! That is not always good! In
			 * this case TaskOne is completed after 3 seconds but we need to get for TaskTwo
			 * to be completed to get TaskOnes result.
			 */

			// No matter what happens sub tasks are interrupted.
//			if(true) {
//				Thread.sleep(2);
//				throw new RuntimeException("Some error");
//			}

			taskScope.join();
//			taskScope.joinUntil(Instant.now().plus(Duration.ofSeconds(2)));
			System.out.println("> All tasks are completed!");

			if (subTask1.state().equals(State.SUCCESS)) {
				System.out.println(subTask1.get());
			} else {
				System.out.println(subTask1.exception());
			}

			if (subTask2.state().equals(State.SUCCESS)) {
				System.out.println(subTask2.get());
			} else {
				System.out.println(subTask2.exception());
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static void inturruptMain() {
		var main = Thread.currentThread();
		Thread.startVirtualThread(() -> {

			try {
				Thread.sleep(3);
				main.interrupt();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
	}
  

  class LongRunningTask implements Callable<TaskResponse> {

	private final String name;
	private final int time;
	private final String response;
	private final boolean fail;

	public LongRunningTask(String name, int time, String response, boolean fail) {
		super();
		this.name = name;
		this.time = time;
		this.response = response;
		this.fail = fail;
	}

	@Override
	public TaskResponse call() throws Exception {
		System.out.println("> Long Running Task "+name+" Started");
		long start = System.currentTimeMillis();

		int count = 0;
		while (count++ < time) {

			if(Thread.interrupted()) {
				System.out.println("Task "+name+" is interrupted!");
				throwFailure(name);
			}
			
			System.out.println("Working on : "+name+" => "+count);
			
			try {
				TimeUnit.SECONDS.sleep(1);
			}catch (Exception ex) {
				throwFailure(name);
			}
		}
		
		if(fail) {
			throwFailure(name);
		}

		System.out.println("> Long Running Task "+name+" Ended");
		long end = System.currentTimeMillis();
		return new TaskResponse(name, response, (end - start));
	}

	private void throwFailure(String name) {
		System.out.println("Task "+name+" Failed");
		throw new RuntimeException("Failed");
	}
}

record TaskResponse(String name, String response, long timeTaken) {
}
	class CustomeTaskScope extends StructuredTaskScope<TaskResponse> {

	private final List<Subtask<? extends TaskResponse>> successfulTasks = new CopyOnWriteArrayList<>();

	@Override
	protected void handleComplete(Subtask<? extends TaskResponse> subtask) {
		if (subtask.state().equals(State.SUCCESS)) {
			int size = 0;
			synchronized (successfulTasks) {
				successfulTasks.add(subtask);
				size = successfulTasks.size();
			}

			if (size == 2) {
				this.shutdown();
			}
		}
	}

	public TaskResponse response() {
		super.ensureOwnerAndJoined();
		if (successfulTasks.size() != 2) {
			throw new RuntimeException("Atleast 2 subtasks must be successful.");
		}
		var a = successfulTasks.get(0).get();
		var b = successfulTasks.get(1).get();

		var res = (Integer.parseInt(a.response()) + Integer.parseInt(b.response())) / 2.0;
		var time = a.timeTaken() + b.timeTaken() / 2;
		return new TaskResponse("Weather", res + "", time);
	}
}

}
