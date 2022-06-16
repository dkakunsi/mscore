import 'dart:convert';

import 'package:api/data/datasource/remote/remote_datasource.dart';
import 'package:flutter/material.dart';

class WorkflowRemoteDataSource extends BaseRemoteDataSource {
  WorkflowRemoteDataSource(
    Map<String, dynamic> configuration,
  ) : super(
          configuration: configuration,
          path: 'api/workflow',
        ) {}

  Future<String> approveTask({
    @required String taskId,
    Map<String, String> headers,
  }) async {
    var uri = Uri(
      host: getHost(),
      port: getPort(),
      path: this.path + "/" + taskId,
    );

    var jsonData = {
      'domain': 'workflowtask',
      'approved': 'true',
    };

    var response = await this.client.put(
          uri,
          body: jsonEncode(jsonData),
          headers: headers,
        );

    return response.body;
  }

  Future<String> rejectTask({
    @required String taskId,
    @required String reason,
    Map<String, String> headers,
  }) async {
    var uri = Uri(
      host: getHost(),
      port: getPort(),
      path: this.path + "/" + taskId,
    );

    var jsonData = {
      'domain': 'workflowtask',
      'approved': 'false',
      'closeReason': reason,
    };

    var response = await this.client.put(
          uri,
          body: jsonEncode(jsonData),
          headers: headers,
        );

    return response.body;
  }
}
