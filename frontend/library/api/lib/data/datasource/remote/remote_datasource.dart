import 'package:http/http.dart' as http;

abstract class BaseRemoteDataSource {
  http.Client client;

  final Map<String, dynamic> configuration;

  final String path;

  BaseRemoteDataSource({
    this.configuration,
    this.path,
  }) {
    this.client = http.Client();
  }

  String getHost() {
    return this.configuration['host'];
  }

  int getPort() {
    var portStr = this.configuration['port'];
    return int.parse(portStr);
  }
}
