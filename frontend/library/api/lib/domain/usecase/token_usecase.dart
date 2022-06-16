import 'package:api/data/model/token_model.dart';
import 'package:api/data/repository/token_repository.dart';
import 'package:api/domain/entity/token_entity.dart';
import 'package:api/domain/context.dart';
import 'package:flutter/material.dart';
import 'package:jwt_decode/jwt_decode.dart';

class TokenUseCase {
  final TokenRepository tokenRepository;

  TokenUseCase({
    @required this.tokenRepository,
  });

  Future<TokenEntity> authenticate({
    @required String username,
    @required String password,
    Context context,
  }) async {
    final activeToken = await tokenRepository.getActiveToken(context: context);
    if (activeToken != null && !activeToken.isExpired) {
      return activeToken;
    }

    final tokenResponse = await tokenRepository.getToken(
        username: username, password: password, context: context);

    final tokenModel = _toEntity(tokenResponse);
    saveToLocal(context: context, tokenModel: tokenModel);

    return tokenModel;
  }

  void saveToLocal({
    Context context,
    @required TokenLocalModel tokenModel,
  }) {
    tokenRepository.saveToken(context: context, tokenModel: tokenModel);
  }

  TokenLocalModel _toEntity(TokenResponseModel tokenResponse) {
    var payload = Jwt.parseJwt(tokenResponse.accessToken);

    return TokenLocalModel(
      accessToken: tokenResponse.accessToken,
      refreshToken: tokenResponse.refreshToken,
      expiredAt: DateTime.fromMillisecondsSinceEpoch(payload['exp']),
      name: payload['name'],
      email: payload['email'],
      roles: payload['realm_access']['roles'],
    );
  }
}
