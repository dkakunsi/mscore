import 'package:api/domain/context.dart';
import 'package:api/domain/entity/resource_entity.dart';
import 'package:api/domain/usecase/resource_usecase.dart';
import 'package:api/presentation/bloc/authentication/authentication_bloc.dart';
import 'package:api/presentation/bloc/resource/resource_event.dart';
import 'package:api/presentation/bloc/resource/resource_state.dart';
import 'package:api/presentation/bloc/resource/resource_util.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class ResourceBloc extends Bloc<ResourceEvent, ResourceState> {
  final ResourceUseCase resourceUseCase;
  final AuthenticationBloc authenticationBloc;

  ResourceBloc({
    this.resourceUseCase,
    this.authenticationBloc,
  }) : super(InitialResourceState()) {
    on<SaveResourceEvent>((event, emit) => _saveResource(event));
    on<RemoveResourceEvent>((event, emit) => _removeResource(event));
    on<FetchResourceEvent>((event, emit) => _fetchResource(event));
    on<SearchResourceEvent>((event, emit) => _searchResource(event));
  }

  Stream<ResourceState> _saveResource(SaveResourceEvent event) async* {
    if (_isAuthenticated) {
      final context = Context(accessToken: _accessToken);

      try {
        var resourceId;
        if (event.isCreate) {
          resourceId = await resourceUseCase.createResource(
            context: context,
            entity: event.entity,
          );
        } else {
          resourceId = await resourceUseCase.updateResource(
            context: context,
            entity: event.entity,
          );
        }

        var entity = event.entity;
        entity.id = resourceId;

        yield SavedResourceState(entity);
      } catch (error) {
        yield ErrorResourceState(message: error);
      }
    } else {
      yield ErrorResourceState(errorType: ErrorTypeEnum.AUTHENTICATION_ERROR);
    }
  }

  Stream<ResourceState> _removeResource(
    RemoveResourceEvent event,
  ) async* {
    if (_isAuthenticated) {
      final context = Context(accessToken: _accessToken);

      try {
        var entity = event.entity;
        entity.isRemoved = true;

        var resourceId = await resourceUseCase.updateResource(
          context: context,
          entity: entity,
        );

        var resources = state.resources;
        resources.removeWhere((element) => element.id == resourceId);

        yield RemovedResourceState(resources);
      } catch (error) {
        yield ErrorResourceState(message: error);
      }
    } else {
      yield ErrorResourceState(errorType: ErrorTypeEnum.AUTHENTICATION_ERROR);
    }
  }

  Stream<ResourceState> _fetchResource(FetchResourceEvent event) async* {
    if (_isAuthenticated) {
      final context = Context(accessToken: _accessToken);

      try {
        ResourceEntity entity;
        if (event.fetchBy == FetchByEnum.FETCH_BY_ID) {
          entity = await resourceUseCase.getResourceById(
            context: context,
            domain: event.domain,
            id: event.criteria,
          );
        } else if (event.fetchBy == FetchByEnum.FETCH_BY_CODE) {
          entity = await resourceUseCase.getResourceByCode(
            context: context,
            domain: event.domain,
            code: event.criteria,
          );
        }

        yield FetchedResourceState(entity);
      } catch (error) {
        yield ErrorResourceState(message: error);
      }
    } else {
      yield ErrorResourceState(errorType: ErrorTypeEnum.AUTHENTICATION_ERROR);
    }
  }

  Stream<ResourceState> _searchResource(SearchResourceEvent event) async* {
    if (_isAuthenticated) {
      final context = Context(accessToken: _accessToken);

      try {
        var resources = await resourceUseCase.searchResources(
          context: context,
          domain: event.domain,
          criteria: event.criteria,
        );

        yield FoundResourceState(resources);
      } catch (error) {
        yield ErrorResourceState(message: error);
      }
    } else {
      yield ErrorResourceState(errorType: ErrorTypeEnum.AUTHENTICATION_ERROR);
    }
  }

  bool get _isAuthenticated => authenticationBloc.state.isAuthenticated;

  String get _accessToken => authenticationBloc.state.accessToken;
}
