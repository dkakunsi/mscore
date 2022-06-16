import 'package:api/domain/context.dart';
import 'package:api/domain/usecase/token_usecase.dart';
import 'package:api/presentation/bloc/authentication/authentication_event.dart';
import 'package:api/presentation/bloc/authentication/authentication_state.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class AuthenticationBloc
    extends Bloc<AuthenticationEvent, AuthenticationState> {
  final TokenUseCase tokenUseCase;

  AuthenticationBloc(this.tokenUseCase) : super(InitialAuthenticationState()) {
    on<LoginEvent>((event, emit) => _loginEventToState(event));
    on<LogoutEvent>((event, emit) => _logoutEventToState(event));
  }

  Stream<AuthenticationState> _loginEventToState(LoginEvent event) async* {
    if (_isValid(event.username) || _isValid(event.password)) {
      yield AuthenticatingState();

      try {
        final tokenEntity = await tokenUseCase.authenticate(
          username: event.username,
          password: event.password,
          context: Context(),
        );

        yield AuthenticatedState(tokenEntity);
      } catch (error) {
        yield FailedAuthenticationState(error);
      }
    } else {
      yield FailedAuthenticationState('Invalid username or password');
    }
  }

  bool _isValid(String credential) {
    return credential != null && credential.isNotEmpty;
  }

  Stream<AuthenticationState> _logoutEventToState(LogoutEvent event) async* {
    yield LoggedOutState();
  }
}
