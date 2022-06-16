import 'package:api/data/datasource/remote/resource_remote_datasource.dart';
import 'package:api/data/model/resource_model.dart';
import 'package:api/domain/context.dart';
import 'package:flutter/material.dart';

class ResourceRepository {
  final ResourceRemoteDataSource resourceDataSource;

  ResourceRepository({
    @required this.resourceDataSource,
  });

  Future<ResourceResponseModel> createResource({
    @required Context context,
    @required ResourceRequestModel requestModel,
  }) async {
    final domain = requestModel.domain;

    final responseMessage = await resourceDataSource.createResource(
      domain: domain,
      data: requestModel.data,
      headers: context.toHeader(),
    );

    return ResourceResponseModel(
      responseMessage: responseMessage,
    );
  }

  Future<ResourceResponseModel> updateResource({
    @required Context context,
    @required ResourceRequestModel requestModel,
  }) async {
    final domain = requestModel.domain;
    final resourceId = requestModel.id;

    final responseMessage = await resourceDataSource.updateResource(
      domain: domain,
      id: resourceId,
      data: requestModel.data,
      headers: context.toHeader(),
    );

    return ResourceResponseModel(
      responseMessage: responseMessage,
    );
  }

  Future<ResourceResponseModel> getResourceById({
    @required Context context,
    @required String domain,
    @required String id,
  }) async {
    var responseMessage = await resourceDataSource.getResourceById(
      domain: domain,
      id: id,
      headers: context.toHeader(),
    );

    return ResourceResponseModel(
      responseMessage: responseMessage,
    );
  }

  Future<ResourceResponseModel> getResourceByCode({
    @required Context context,
    @required String domain,
    @required String code,
  }) async {
    final responseMessage = await resourceDataSource.getResourceByCode(
      domain: domain,
      code: code,
      headers: context.toHeader(),
    );

    return ResourceResponseModel(
      responseMessage: responseMessage,
    );
  }

  Future<ResourceResponseModel> searchResources({
    @required Context context,
    @required String domain,
    @required Map<String, dynamic> criteria,
  }) async {
    final responseMessage = await resourceDataSource.searchResources(
      domain: domain,
      criteria: criteria,
      headers: context.toHeader(),
    );

    return ResourceResponseModel(
      responseMessage: responseMessage,
    );
  }
}
