library widget;

import 'package:flutter/material.dart';
import 'package:widget/src/component/DataPage.dart';

import 'domain/Advertorial.dart';
import 'domain/Login.dart';
import 'domain/Task.dart';

abstract class Domain {
  final String name;

  final String body;

  final Widget defaultBody = Container(
    width: 0,
    height: 0,
  );

  Domain(this.name, this.body);

  ListTile getListTile(Function onTap) {
    return ListTile(
        title: Text(this.name),
        onTap: () {
          onTap(this.name);
        });
  }

  SearchableWidget getDataView(Function onTap);

  FloatingActionButton getGridActionButton(Function onPressed) {
    return null;
  }

  Widget getInputView(String entityId);

  FloatingActionButton getInputActionButton(Function onPressed) {
    return null;
  }
}

Map<String, Domain> getDomains(List roles) {
  if (roles == null || roles.isEmpty) {
    return {'Login': Login()};
  }

  Map<String, Domain> domains = {
    'Advertorial': Advertorial(),
    'Task': Task(),
  };
  return domains;
}
