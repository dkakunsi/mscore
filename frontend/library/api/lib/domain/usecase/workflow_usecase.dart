import 'package:api/data/repository/workflow_repository.dart';
import 'package:api/domain/context.dart';
import 'package:flutter/material.dart';

class WorkflowUseCase {
  final WorkflowRepository workflowRepository;

  WorkflowUseCase({
    @required this.workflowRepository,
  });

  Future<void> approveTask({
    @required Context context,
    @required String taskId,
  }) async {
    await workflowRepository.approveTask(
      context: context,
      taskId: taskId,
    );
  }

  Future<void> rejectTask({
    @required Context context,
    @required String taskId,
    String reason,
  }) async {
    await workflowRepository.rejectTask(
      context: context,
      taskId: taskId,
      reason: reason,
    );
  }
}
