import 'package:api/domain/entity/resource_entity.dart';
import 'package:flutter/material.dart';
import 'package:widget/Configuration.dart';
import 'package:widget/component/input_field.dart';
import 'package:widget/src/Util.dart';

abstract class ComponentDomain {
  final String key;

  ComponentDomain(this.key);

  GridTile gridTile({
    @required ResourceEntity entity,
    @required Configuration configuration,
    @required Function onTap,
  });

  List<InputField> inputFields(
    ResourceEntity entity,
    Configuration configuration,
  );

  Map<String, dynamic> get loadCriteria;
}

// example
class AdvertorialComponentDomain extends ComponentDomain {
  AdvertorialComponentDomain() : super('advertorial');

  final Map<String, Map<String, dynamic>> _attributes = {
    'code': {'title': 'Code', 'icon': Icons.code},
    'name': {'title': 'Title', 'icon': Icons.assignment},
    'url': {'title': 'Url', 'icon': Icons.public},
    'provider_name': {'title': 'Provider name', 'icon': Icons.account_circle},
    'provider_email': {'title': 'Provider email', 'icon': Icons.alternate_email}
  };

  @override
  GridTile gridTile({
    ResourceEntity entity,
    Configuration configuration,
    Function onTap,
  }) {
    return GridTile(
      header: GridTileBar(
        title: Text(entity.code),
        backgroundColor: configuration.common['tileColor'],
      ),
      child: InkResponse(
        child: Container(
          padding: EdgeInsets.only(top: 50),
          child: Column(
            children: <Widget>[
              Padding(
                padding: EdgeInsets.only(left: 18, top: 10),
                child: Align(
                  alignment: Alignment.topLeft,
                  child: Text(
                    "Name: " + entity.name,
                  ),
                ),
              ),
              Padding(
                padding: EdgeInsets.only(left: 18, top: 10),
                child: Align(
                  alignment: Alignment.topLeft,
                  child: Text(
                    "Status: " + entity.status,
                  ),
                ),
              ),
              Padding(
                padding: EdgeInsets.only(left: 18, top: 10),
                child: Align(
                  alignment: Alignment.topLeft,
                  child: Text(
                    "Provider: " + entity.getObject('provider')['name'],
                  ),
                ),
              ),
              Padding(
                padding: EdgeInsets.only(left: 18, top: 10),
                child: Align(
                  alignment: Alignment.topLeft,
                  child: Text(
                    "Created At: " + toDateTime(entity.getValue('createdDate')),
                  ),
                ),
              ),
            ],
          ),
        ),
        onTap: onTap,
      ),
    );
  }

  @override
  List<InputField> inputFields(
    ResourceEntity entity,
    Configuration configuration,
  ) {
    List<InputField> inputFields;
    _attributes.forEach((key, object) {
      var name = object['title'];
      var icon = object['icon'];

      inputFields.add(InputField(
        id: key,
        name: name,
        configuration: configuration,
        icon: icon,
        value: entity.getValue(key),
      ));
    });

    return inputFields;
  }

  @override
  Map<String, dynamic> get loadCriteria => {
        "domain": "index",
        "page": 0,
        "size": 50,
        "criteria": [],
      };
}
