import 'package:uuid/uuid.dart';

const BREADCRUMB_ID = 'breadcrumbId';

const AUTHORIZATION = 'Authorization';

class Context {
  String breadcrumbId;
  String accessToken;

  Context({
    this.accessToken,
  }) {
    breadcrumbId = _getUuid();
  }

  static String _getUuid() => Uuid().v1().toString();

  Map<String, dynamic> toHeader() => {
        BREADCRUMB_ID: breadcrumbId,
        AUTHORIZATION: 'Bearer ' + accessToken,
      };
}
