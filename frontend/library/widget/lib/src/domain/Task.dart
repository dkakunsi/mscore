library widget;

import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:uuid/uuid.dart';
import 'package:api/api.dart';
import 'package:widget/src/component/DataPage.dart';

import '../Domain.dart';
import '../Util.dart';
import '../component/InputField.dart';
import '../../Configuration.dart';

var config = Configuration();
var api = Api(config.getConfig());

class Task extends Domain {
  _TaskInputView _taskInputView;

  Task() : super('Task', 'This is task');

  @override
  SearchableWidget getDataView(Function onAction) {
    return _TaskGridView(onAction);
  }

  @override
  Widget getInputView(String entityId) =>
      this._taskInputView = _TaskInputView(entityId);

  @override
  FloatingActionButton getInputActionButton(Function onPressed) {
    if (!config.hasRole('workflow_management_user')) {
      return null;
    }

    return FloatingActionButton(
      backgroundColor: config.getConfig()['primaryColor'],
      hoverColor: config.getConfig()['focusColor'],
      elevation: 10,
      onPressed: () {
        this._taskInputView.save().then((value) {
          print(jsonEncode(value));
        }).onError((error, stackTrace) {
          print(error);
        }).whenComplete(() {
          onPressed();
        });
      },
      tooltip: 'Save',
      child: Icon(Icons.save),
    );
  }
}

class _TaskGridView extends SearchableWidget {
  final _TaskGridViewState _state = _TaskGridViewState();

  final Function _onAction;

  _TaskGridView(this._onAction);

  @override
  State<StatefulWidget> createState() => this._state;

  Future<Map> _getGridData() async {
    var criteria = {
      "domain": "index",
      "page": 0,
      "size": 10,
      "criteria": [
        {
          "attribute": "processInstance.organisation",
          "value": "PDE",
          "operator": "equals",
        },
        {
          "attribute": "status",
          "value": "Active",
          "operator": "equals",
        }
      ]
    };
    return await _load(criteria);
  }

  @override
  void search(String value) {
    var criteria = {
      "domain": "index",
      "page": 0,
      "size": 10,
      "criteria": [
        {
          "attribute": "processInstance.organisation",
          "value": "PDE",
          "operator": "equals",
        },
        {
          "attribute": "name",
          "value": value,
          "operator": "contains",
        }
      ]
    };
    var result = _load(criteria);
    this._state.setTasks(result);
  }

  Future<Map> _load(Map criteria) async {
    Map<String, String> context = {
      "Authorization": config.getToken()['id'],
      "breadcrumbId": Uuid().v4()
    };
    return await api.search(context, 'workflowtask', criteria);
  }
}

class _TaskGridViewState extends State<_TaskGridView> {
  Future<Map> _tasks;

  @override
  void initState() {
    super.initState();
    this._tasks = widget._getGridData();
  }

  void setTasks(Future<Map> tasks) {
    setState(() {
      this._tasks = tasks;
    });
  }

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
    var result = await _tasks;
    var data = jsonDecode(result['message']);
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
          child: GridTile(
            header: GridTileBar(
              title: Text(getViewData(data[index]['processInstance'])),
              backgroundColor: config.getConfig()['tileColor'],
            ),
            child: InkResponse(
              child: Container(
                padding: EdgeInsets.only(top: 50),
                child: Column(
                  children: <Widget>[
                    Padding(
                      padding: EdgeInsets.only(left: 18, top: 10, right: 4),
                      child: Align(
                        alignment: Alignment.topLeft,
                        child: Text(
                          "Task: " + data[index]['name'],
                        ),
                      ),
                    ),
                    Padding(
                      padding: EdgeInsets.only(left: 18, top: 10),
                      child: Align(
                        alignment: Alignment.topLeft,
                        child: Text(
                          "Status: " + data[index]['status'],
                        ),
                      ),
                    ),
                  ],
                ),
              ),
              onTap: () {
                widget._onAction(data[index]['id']);
              },
            ),
          ),
        );
      },
    );
  }
}

class _TaskInputView extends StatefulWidget {
  final String _id;

  final Map<String, Map<String, dynamic>> _attributes = {
    'name': {'title': 'Task name', 'icon': Icons.assignment},
    'approved': {'title': 'Approval', 'icon': Icons.check},
    'closedReason': {'title': 'Reason'},
  };

  final List<InputField> _inputFields = [];

  final Map _data = {'task': {}};

  _TaskInputView(this._id) {
    this._attributes.forEach((key, detail) {
      var name = detail['title'];
      var icon = detail['icon'];
      this._inputFields.add(InputField(key, name, icon));
    });
  }

  @override
  State<StatefulWidget> createState() => _TaskInputViewState();

  Future<Map> _getDetailData() async {
    if (this._id == null) {
      return {};
    }

    Map<String, String> context = {
      "Authorization": config.getToken()['id'],
      "breadcrumbId": Uuid().v4()
    };

    var criteria = {
      "domain": "index",
      "page": 0,
      "size": 1,
      "criteria": [
        {
          "attribute": "id",
          "value": this._id,
          "operator": "equals",
        }
      ]
    };
    return await api.search(context, 'workflowtask', criteria);
  }

  Future<Map> save() async {
    Map payload = this._data['task'];
    this._inputFields.forEach((inputField) {
      var value = inputField.controller.text;
      setJSONValue(payload, inputField.elementId, value);
    });

    Map<String, String> context = {
      "Authorization": config.getToken()['id'],
      "breadcrumbId": Uuid().v4()
    };

    if (payload['approved'] == 'true') {
      return Future.delayed(
        Duration(seconds: 2),
        () => api.approveTask(context, this._id),
      );
    } else if (payload['approved'] == 'false') {
      return Future.delayed(
        Duration(seconds: 2),
        () => api.rejectTask(context, this._id, payload['closedReason']),
      );
    } else {
      throw ('Approval response is not recognized: ' + payload['approved']);
    }
  }
}

class _TaskInputViewState extends State<_TaskInputView> {
  Future<Map> _task;

  @override
  void initState() {
    super.initState();
    this._task = widget._getDetailData();
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      child: FutureBuilder(
          future: _createInputView(),
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

  Future<Widget> _createInputView() async {
    var result = await this._task;
    if (result.isNotEmpty) {
      widget._data['task'] = jsonDecode(result['message'])[0];

      widget._inputFields.forEach((inputField) {
        var value = getJSONValue(widget._data['task'], inputField.elementId);
        inputField.controller.text = value;
      });
    }

    return Align(
      alignment: Alignment.bottomCenter,
      child: Container(
        padding: EdgeInsets.only(top: 20),
        child: Column(
          children: widget._inputFields,
        ),
      ),
    );
  }
}
