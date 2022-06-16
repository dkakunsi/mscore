abstract class Entity {
  Map<String, dynamic> data;

  Entity({
    this.data,
  });

  setValue(String field, dynamic value) {
    data[field] = value;
  }

  dynamic getValue(String field) {
    return data[field];
  }

  Map getObject(String field) {
    return data[field];
  }
}
