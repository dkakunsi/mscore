abstract class WorkflowEvent {}

class ApproveTaskWorkflowEvent extends WorkflowEvent {
  final String taskId;

  ApproveTaskWorkflowEvent(this.taskId);
}

class RejectTaskWorkflowEvent extends WorkflowEvent {
  final String taskId;

  RejectTaskWorkflowEvent(this.taskId);
}
