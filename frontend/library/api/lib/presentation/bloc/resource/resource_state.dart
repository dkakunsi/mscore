import 'package:api/domain/entity/resource_entity.dart';
import 'package:api/presentation/bloc/resource/resource_util.dart';

abstract class ResourceState {
  List<ResourceEntity> resources;

  ResourceState(this.resources);
}

class InitialResourceState extends ResourceState {
  InitialResourceState() : super(<ResourceEntity>[]);
}

class SavedResourceState extends ResourceState {
  SavedResourceState(ResourceEntity entity) : super(<ResourceEntity>[entity]);
}

class RemovedResourceState extends ResourceState {
  RemovedResourceState(List<ResourceEntity> resources) : super(resources);
}

class FetchedResourceState extends ResourceState {
  FetchedResourceState(ResourceEntity entity) : super(<ResourceEntity>[entity]);
}

class FoundResourceState extends ResourceState {
  FoundResourceState(List<ResourceEntity> resources) : super(resources);
}

class ErrorResourceState extends ResourceState {
  final ErrorTypeEnum errorType;
  final String message;
  ErrorResourceState({
    this.errorType = ErrorTypeEnum.GENERIC_ERROR,
    this.message,
  }) : super(<ResourceEntity>[]);
}
