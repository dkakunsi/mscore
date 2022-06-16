import 'package:api/domain/context.dart';
import 'package:api/domain/usecase/workflow_usecase.dart';
import 'package:api/presentation/bloc/authentication/authentication_bloc.dart';
import 'package:api/presentation/bloc/resource/resource_util.dart';
import 'package:api/presentation/bloc/workflow/workflow_event.dart';
import 'package:api/presentation/bloc/workflow/workflow_state.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class WorkflowBloc extends Bloc<WorkflowEvent, WorkflowState> {
  WorkflowUseCase workflowUseCase;
  AuthenticationBloc authenticationBloc;

  WorkflowBloc(
    this.workflowUseCase,
    this.authenticationBloc,
  ) : super(InitialWorkflowState()) {
    on<ApproveTaskWorkflowEvent>((event, emit) => _approveTask(event));
    on<RejectTaskWorkflowEvent>((event, emit) => _rejectTask(event));
  }

  Stream<WorkflowState> _approveTask(ApproveTaskWorkflowEvent event) async* {
    if (_isAuthenticated) {
      var context = Context(accessToken: _accessToken);
      try {
        workflowUseCase.approveTask(
          context: context,
          taskId: event.taskId,
        );

        yield TaskApprovedWorkflowState();
      } catch (error) {
        yield ErrorWorkflowState(message: error);
      }
    } else {
      yield ErrorWorkflowState(errorType: ErrorTypeEnum.AUTHENTICATION_ERROR);
    }
  }

  Stream<WorkflowState> _rejectTask(RejectTaskWorkflowEvent event) async* {
    if (_isAuthenticated) {
      var context = Context(accessToken: _accessToken);

      try {
        workflowUseCase.rejectTask(
          context: context,
          taskId: event.taskId,
        );

        yield TaskRejectedWorkflowState();
      } catch (error) {
        yield ErrorWorkflowState(message: error);
      }
    } else {
      yield ErrorWorkflowState(errorType: ErrorTypeEnum.AUTHENTICATION_ERROR);
    }
  }

  bool get _isAuthenticated => authenticationBloc.state.isAuthenticated;

  String get _accessToken => authenticationBloc.state.accessToken;
}
