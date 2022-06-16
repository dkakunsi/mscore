import 'package:api/domain/entity/token_entity.dart';
import 'package:flutter/material.dart';

class TokenResponseModel {
  Map<String, dynamic> data;

  TokenResponseModel({
    @required this.data,
  });

  String get accessToken => data['access_token'];

  String get refreshToken => data['refresh_token'];
}

class TokenLocalModel extends TokenEntity {
  TokenLocalModel({
    @required String accessToken,
    @required DateTime expiredAt,
    @required String name,
    String email,
    String refreshToken,
    String roles,
  }) : super(
          accessToken: accessToken,
          expiredAt: expiredAt,
          name: name,
          email: email,
          refreshToken: refreshToken,
          roles: roles,
        );

  Map<String, dynamic> toJson() {
    return {
      'accessToken': accessToken,
      'expiredAt': expiredAt,
      'name': name,
      'email': email,
      'refreshToken': refreshToken,
      'roles': roles,
    };
  }
}
