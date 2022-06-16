import 'package:api/presentation/bloc/resource/resource_bloc.dart';
import 'package:api/presentation/bloc/resource/resource_event.dart';
import 'package:flutter/material.dart';
import 'package:widget/Configuration.dart';
import 'package:widget/component/domain.dart';
import 'package:widget/src/Util.dart';

class DataGrid extends StatefulWidget {
  final ResourceBloc resourceBloc;
  final Configuration configuration;
  final ComponentDomain domain;

  DataGrid({
    @required this.resourceBloc,
    @required this.configuration,
    @required this.domain,
  }) {
    resourceBloc.add(SearchResourceEvent(
      domain: domain.key,
      criteria: domain.loadCriteria,
    ));
  }

  @override
  State<StatefulWidget> createState() => DataGridState();
}

class DataGridState extends State<DataGrid> {
  @override
  Widget build(BuildContext context) {
    return Container(
      child: FutureBuilder(
          future: _createGridView(),
          builder: (BuildContext context, AsyncSnapshot<Widget> snapshot) {
            return snapshot.hasData
                ? snapshot.data
                : Container(
                    width: 0,
                    height: 0,
                  );
          }),
    );
  }

  Future<Widget> _createGridView() async {
    var data = widget.resourceBloc.state.resources;
    var crossAxisCount = gridCount(context);

    return GridView.builder(
      padding: EdgeInsets.all(10.0),
      itemCount: data.length,
      gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: crossAxisCount,
        crossAxisSpacing: 8.0,
        mainAxisSpacing: 8.0,
        childAspectRatio: getGridAspectRation(context),
      ),
      itemBuilder: (BuildContext context, int index) {
        return Container(
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(2.0),
            color: Colors.white,
            boxShadow: [
              BoxShadow(
                color: Colors.black,
                blurRadius: 2.0,
                spreadRadius: 0.0,
                offset: Offset(0.0, 2.0),
              )
            ],
          ),
          padding: EdgeInsets.only(top: 1),
          child: widget.domain.gridTile(
              entity: data[index],
              configuration: widget.configuration,
              onTap: () {
                // navigate to detail
              }),
        );
      },
    );
  }
}
