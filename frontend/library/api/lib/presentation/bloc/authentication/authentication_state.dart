import 'package:api/domain/entity/token_entity.dart';

abstract class AuthenticationState {
  TokenEntity token;

  AuthenticationState({
    this.token,
  });

  bool get isAuthenticated => token != null && !token.isExpired;

  String get accessToken {
    return isAuthenticated ? token.accessToken : null;
  }
}

class InitialAuthenticationState extends AuthenticationState {}

class AuthenticatingState extends AuthenticationState {}

class AuthenticatedState extends AuthenticationState {
  AuthenticatedState(TokenEntity token) : super(token: token);
}

class LoggedOutState extends AuthenticationState {}

class FailedAuthenticationState extends AuthenticationState {
  final dynamic error;

  FailedAuthenticationState(this.error);
}
