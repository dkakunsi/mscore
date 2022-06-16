import 'dart:convert';

import 'package:api/data/datasource/remote/remote_datasource.dart';
import 'package:flutter/material.dart';

class ResourceRemoteDataSource extends BaseRemoteDataSource {
  ResourceRemoteDataSource({
    Map<String, dynamic> configuration,
  }) : super(
          configuration: configuration,
          path: 'api/resource',
        );

  Future<String> createResource({
    Map<String, dynamic> headers,
    @required String domain,
    @required Map<String, dynamic> data,
  }) async {
    var queryParameters = {
      'domain': domain,
    };
    var uri = Uri(
      host: getHost(),
      port: getPort(),
      path: this.path,
      queryParameters: queryParameters,
    );

    var response = await this.client.post(
          uri,
          body: jsonEncode(data),
          headers: headers,
        );

    return response.body;
  }

  Future<String> updateResource({
    Map<String, dynamic> headers,
    @required String domain,
    @required String id,
    @required Map<String, dynamic> data,
  }) async {
    var queryParameters = {
      'domain': domain,
    };
    var uri = Uri(
      host: getHost(),
      port: getPort(),
      path: this.path + '/' + id,
      queryParameters: queryParameters,
    );

    var response = await this.client.put(
          uri,
          body: jsonEncode(data),
          headers: headers,
        );

    return response.body;
  }

  Future<String> getResourceById({
    Map<String, dynamic> headers,
    @required String domain,
    @required String id,
  }) async {
    var queryParameters = {
      'domain': domain,
    };
    var uri = Uri(
      host: getHost(),
      port: getPort(),
      path: this.path + '/' + id,
      scheme: "http",
      queryParameters: queryParameters,
    );

    var response = await this.client.get(
          uri,
          headers: headers,
        );

    return response.body;
  }

  Future<String> getResourceByCode({
    Map<String, dynamic> headers,
    @required String domain,
    @required String code,
  }) async {
    var queryParameters = {
      'domain': domain,
    };
    var uri = Uri(
      host: getHost(),
      port: getPort(),
      path: this.path + '/code/' + code,
      queryParameters: queryParameters,
    );

    var response = await this.client.get(
          uri,
          headers: headers,
        );

    return response.body;
  }

  Future<String> searchResources({
    Map<String, dynamic> headers,
    @required String domain,
    @required Map<String, dynamic> criteria,
  }) async {
    var queryParameters = {
      'domain': domain,
    };
    var uri = Uri(
      host: getHost(),
      port: getPort(),
      path: this.path + '/search',
      queryParameters: queryParameters,
    );

    var response = await this.client.post(
          uri,
          body: jsonEncode(criteria),
          headers: headers,
        );

    return response.body;
  }
}
