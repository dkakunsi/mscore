abstract class AuthenticationEvent {}

class LoginEvent extends AuthenticationEvent {
  final String username;
  final String password;

  LoginEvent(this.username, this.password);
}

class LogoutEvent extends AuthenticationEvent {}
