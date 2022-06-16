import 'package:api/data/model/resource_model.dart';
import 'package:api/data/repository/resource_repository.dart';
import 'package:api/domain/entity/resource_entity.dart';
import 'package:api/domain/context.dart';
import 'package:flutter/material.dart';

class ResourceUseCase {
  final ResourceRepository resourceRepository;

  ResourceUseCase({
    @required this.resourceRepository,
  });

  Future<String> createResource({
    @required Context context,
    @required ResourceEntity entity,
  }) async {
    final requestModel = ResourceRequestModel.of(entity);
    final response = await resourceRepository.createResource(
        context: context, requestModel: requestModel);

    return response.toEntity().id;
  }

  Future<String> updateResource({
    @required Context context,
    @required ResourceEntity entity,
  }) async {
    final requestModel = ResourceRequestModel.of(entity);
    final response = await resourceRepository.updateResource(
        context: context, requestModel: requestModel);

    return response.toEntity().id;
  }

  Future<ResourceEntity> getResourceById({
    @required Context context,
    @required String domain,
    @required String id,
  }) async {
    final responseModel = await resourceRepository.getResourceById(
      context: context,
      domain: domain,
      id: id,
    );

    return responseModel.toEntity();
  }

  Future<ResourceEntity> getResourceByCode({
    @required Context context,
    @required String domain,
    @required String code,
  }) async {
    final responseModel = await resourceRepository.getResourceByCode(
      context: context,
      domain: domain,
      code: code,
    );

    return responseModel.toEntity();
  }

  Future<List<ResourceEntity>> searchResources({
    @required Context context,
    @required String domain,
    @required Map<String, dynamic> criteria,
  }) async {
    final responseModel = await resourceRepository.searchResources(
      context: context,
      domain: domain,
      criteria: criteria,
    );

    return responseModel.toEntities();
  }
}
