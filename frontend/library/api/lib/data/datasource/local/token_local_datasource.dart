import 'package:api/data/model/token_model.dart';
import 'package:localstorage/localstorage.dart';

class TokenLocalDataSource {
  LocalStorage storage;

  TokenLocalDataSource() {
    storage = LocalStorage('token_store');
  }

  void setToken(TokenLocalModel tokenModel) {
    storage.setItem('token', tokenModel);
  }

  Future<TokenLocalModel> getToken() {
    return storage.getItem('token');
  }

  void removeToken() {
    storage.deleteItem('token');
  }
}
