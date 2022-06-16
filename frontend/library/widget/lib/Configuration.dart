library widget;

import 'dart:convert';
import 'dart:html' show window;
import 'package:jwt_decode/jwt_decode.dart';

class Configuration {
  static final Configuration _singleton = Configuration._internal();

  Map<String, dynamic> _config = {};

  factory Configuration() {
    return _singleton;
  }

  Configuration._internal();

  Map<String, dynamic> getConfig() => _config;

  Map<String, dynamic> get common => _config;

  void setConfig(Map<String, dynamic> config) {
    _config = config;
  }

  Map getToken() {
    var token = window.localStorage['token'];
    if (token == null) {
      return {};
    }
    var parsed = jsonDecode(token);
    var expire = parsed['expire'] * 1000;
    var now = new DateTime.now().millisecondsSinceEpoch;
    if (now > expire) {
      removeToken();
      return {};
    }
    return parsed;
  }

  void setToken(Map token) {
    var accessToken = token['access_token'];

    // build parsed object
    var payload = Jwt.parseJwt(accessToken);
    var roles = payload['realm_access']['roles'];
    var name = payload['name'];
    var email = payload['email'];
    var exp = payload['exp'];
    var parsedToken = {
      'id': accessToken,
      'role': roles,
      'name': name,
      'email': email,
      'expire': exp
    };

    window.localStorage['token'] = jsonEncode(parsedToken);
  }

  void removeToken() {
    window.localStorage.remove('token');
  }

  bool hasRole(String role) {
    List roles = getToken()['role'];
    return roles.contains(role);
  }
}
