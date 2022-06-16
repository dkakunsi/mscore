import 'package:api/data/datasource/remote/workflow_remote_datasource.dart';
import 'package:api/domain/context.dart';
import 'package:flutter/material.dart';

class WorkflowRepository {
  final WorkflowRemoteDataSource workflowDataSource;

  WorkflowRepository({
    @required this.workflowDataSource,
  });

  Future<void> approveTask({
    @required Context context,
    @required String taskId,
  }) async {
    workflowDataSource.approveTask(
      taskId: taskId,
      headers: context.toHeader(),
    );
  }

  Future<void> rejectTask({
    @required Context context,
    @required String taskId,
    String reason,
  }) async {
    workflowDataSource.rejectTask(
      taskId: taskId,
      reason: reason,
      headers: context.toHeader(),
    );
  }
}
