import 'dart:convert';

import 'package:api/domain/entity/resource_entity.dart';
import 'package:flutter/material.dart';

class ResourceRequestModel extends ResourceEntity {
  Map<String, dynamic> data;

  ResourceRequestModel({
    @required this.data,
  }) : super();

  static ResourceRequestModel of(ResourceEntity entity) {
    return ResourceRequestModel(data: entity.data);
  }
}

class ResourceResponseModel {
  List list;
  Map<String, dynamic> object;

  ResourceResponseModel({
    @required String responseMessage,
  }) {
    if (_containsArraySymbols(responseMessage)) {
      list = jsonDecode(responseMessage);
    } else {
      object = jsonDecode(responseMessage);
      list = [object];
    }
  }

  static bool _containsArraySymbols(String message) {
    return message.contains('[') && message.contains(']');
  }

  bool get isObject => object != null;

  bool get isArray => !isObject;

  ResourceEntity toEntity() {
    return ResourceEntity(data: object);
  }

  List<ResourceEntity> toEntities() {
    var entities = <ResourceEntity>[];
    list.forEach((obj) {
      final entity = ResourceEntity(data: obj);
      entities.add(entity);
    });
    return entities;
  }
}
