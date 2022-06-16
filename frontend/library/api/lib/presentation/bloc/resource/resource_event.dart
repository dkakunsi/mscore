import 'package:api/domain/entity/resource_entity.dart';
import 'package:api/presentation/bloc/resource/resource_util.dart';
import 'package:flutter/material.dart';

abstract class ResourceEvent {}

class SaveResourceEvent extends ResourceEvent {
  final bool isCreate;
  final ResourceEntity entity;

  SaveResourceEvent({
    @required this.entity,
    this.isCreate = false,
  });
}

class RemoveResourceEvent extends ResourceEvent {
  final ResourceEntity entity;

  RemoveResourceEvent({
    @required this.entity,
  });
}

class FetchResourceEvent extends ResourceEvent {
  final FetchByEnum fetchBy;
  final String criteria;
  final String domain;

  FetchResourceEvent(
    this.domain,
    this.fetchBy,
    this.criteria,
  );
}

class SearchResourceEvent extends ResourceEvent {
  final int size;
  final int page;
  final String domain;
  final Map<String, dynamic> criteria;

  SearchResourceEvent({
    this.size = 10,
    this.page = 0,
    @required this.domain,
    @required this.criteria,
  });
}
