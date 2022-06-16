import 'package:api/data/datasource/remote/remote_datasource.dart';
import 'package:flutter/material.dart';

class TokenRemoteDataSource extends BaseRemoteDataSource {
  TokenRemoteDataSource(Map<String, dynamic> configuration)
      : super(
          configuration: configuration,
          path: _getRealmsUri(configuration),
        );

  Future<String> getToken({
    Map<String, dynamic> headers,
    @required String username,
    @required String password,
  }) async {
    var kcSetting = this.configuration['kc'];
    var uri = Uri(
      host: kcSetting['host'],
      port: kcSetting['port'],
      path: this.path,
    );

    if (headers == null) {
      headers = {
        'Content-Type': 'application/x-www-form-urlencoded',
      };
    } else {
      headers.putIfAbsent(
        'Content-Type',
        () => 'application/x-www-form-urlencoded',
      );
    }

    var value = 'grant_type=' +
        Uri.encodeComponent(kcSetting['grant_type']) +
        '&client_id=' +
        Uri.encodeComponent(kcSetting['client_id']) +
        '&client_secret=' +
        Uri.encodeComponent(kcSetting['client_secret']) +
        '&username=' +
        Uri.encodeComponent(username) +
        '&password=' +
        Uri.encodeComponent(password);

    var response = await this.client.post(
          uri,
          body: value,
          headers: headers,
        );

    return response.body;
  }

  static String _getRealmsUri(Map<String, dynamic> configuration) {
    var realm = configuration["kc"]["realm"];
    return 'auth/realms/${realm}/protocol/openid-connect/token';
  }
}
