import 'package:api/domain/entity/entity.dart';

class ResourceEntity extends Entity {
  ResourceEntity({
    Map<String, dynamic> data,
  }) : super(data: data);

  String get id => getValue('id');

  set isRemoved(bool isRemoved) {
    setValue('isRemoved', true);
  }

  void set id(String id) => setValue('id', id);

  String get domain => getValue('domain');

  void set domain(String domain) => setValue('domain', domain);

  String get code => getValue('code');

  void set code(String code) => setValue('code', code);

  String get name => getValue('name');

  void set name(String name) => setValue('name', name);

  String get status => getValue('status');
}
