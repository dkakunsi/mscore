import 'dart:convert';
import 'package:api/data/datasource/local/token_local_datasource.dart';
import 'package:api/data/datasource/remote/token_remote_datasource.dart';
import 'package:api/data/model/token_model.dart';
import 'package:api/domain/context.dart';
import 'package:flutter/material.dart';

class TokenRepository {
  final TokenRemoteDataSource tokenRemoteDataSource;
  final TokenLocalDataSource tokenLocalDataStore;

  TokenRepository({
    @required this.tokenRemoteDataSource,
    @required this.tokenLocalDataStore,
  });

  Future<TokenResponseModel> getToken({
    Context context,
    @required String username,
    @required String password,
  }) async {
    var responseMessage = await tokenRemoteDataSource.getToken(
      username: username,
      password: password,
      headers: context.toHeader(),
    );

    return TokenResponseModel(
      data: jsonDecode(responseMessage),
    );
  }

  void saveToken({
    @required Context context,
    @required TokenLocalModel tokenModel,
  }) {
    tokenLocalDataStore.setToken(tokenModel);
  }

  Future<TokenLocalModel> getActiveToken({
    @required Context context,
  }) {
    return tokenLocalDataStore.getToken();
  }

  void removeToken({
    @required Context context,
  }) {
    tokenLocalDataStore.removeToken();
  }
}
