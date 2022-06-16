import 'package:flutter/material.dart';

class TokenEntity {
  final String accessToken;
  final DateTime expiredAt;
  final String refreshToken;

  final String name;
  final String email;
  final String roles;

  TokenEntity({
    @required this.accessToken,
    @required this.expiredAt,
    @required this.name,
    this.refreshToken,
    this.email,
    this.roles,
  });

  bool get isExpired => DateTime.now().isAfter(expiredAt);
}
