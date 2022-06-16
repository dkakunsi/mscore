import 'package:api/presentation/bloc/resource/resource_util.dart';

abstract class WorkflowState {}

class InitialWorkflowState extends WorkflowState {}

class TaskApprovedWorkflowState extends WorkflowState {}

class TaskRejectedWorkflowState extends WorkflowState {}

class ErrorWorkflowState extends WorkflowState {
  final ErrorTypeEnum errorType;
  final String message;
  ErrorWorkflowState({
    this.errorType = ErrorTypeEnum.GENERIC_ERROR,
    this.message,
  });
}
