import 'package:api/domain/entity/resource_entity.dart';
import 'package:flutter/cupertino.dart';
import 'package:widget/Configuration.dart';
import 'package:widget/component/domain.dart';

class DetailView extends StatefulWidget {
  final ResourceEntity entity;
  final ComponentDomain domain;
  final Configuration configuration;

  DetailView({
    @required this.entity,
    @required this.domain,
    @required this.configuration,
  });

  @override
  State<StatefulWidget> createState() => DetailViewState();
}

class DetailViewState extends State<DetailView> {
  @override
  Widget build(BuildContext context) {
    return Container(
      child: Align(
        alignment: Alignment.bottomCenter,
        child: Container(
          padding: EdgeInsets.only(top: 20),
          child: Column(
            children: widget.domain.inputFields(
              widget.entity,
              widget.configuration,
            ),
          ),
        ),
      ),
    );
  }
}
