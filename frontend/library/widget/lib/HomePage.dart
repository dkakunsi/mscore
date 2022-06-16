library widget;

import 'dart:developer';

import 'package:flutter/material.dart';
import 'package:flutter_search_bar/flutter_search_bar.dart';

import 'src/Domain.dart';
import 'Configuration.dart';

enum Action {
  GRID_VIEW,
  INPUT_VIEW,
}

var config = Configuration();

class MyHomePage extends StatefulWidget {
  MyHomePage({Key key, this.title}) : super(key: key);

  final String title;

  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  Action _action = Action.GRID_VIEW;

  List<Widget> _drawerWidgets;

  Map<String, Domain> _domains;

  Domain _selectedDomain;

  String _entityId;

  Function _viewAction;

  SearchBar searchBar;

  _MyHomePageState() {
    this.searchBar = SearchBar(
      inBar: false,
      buildDefaultAppBar: _buildAppBar,
      setState: setState,
      onSubmitted: onSubmitted,
      onCleared: () {
        print("cleared");
      },
      onClosed: () {
        print("closed");
      },
    );
  }

  @override
  void initState() {
    super.initState();
    this._entityId = null;
    this._domains = getDomains(config.getToken()['role']);
    this._selectedDomain = this._domains.values.elementAt(0);

    this._viewAction = this._selectedDomain.name == 'Login'
        ? this._setLogin
        : this._openInputView;

    this._drawerWidgets = getDrawerWidgets(this._domains);
  }

  void onSubmitted(String value) {
    this._selectedDomain.getDataView(this._viewAction).search(value);
  }

  AppBar _buildAppBar(BuildContext context) {
    return AppBar(
      title: Text(
        this._selectedDomain.name,
        maxLines: 1,
        style: TextStyle(
          color: Colors.white,
          fontWeight: FontWeight.bold,
          fontSize: 18,
        ),
      ),
      centerTitle: true,
      elevation: 0,
      actions: [searchBar.getSearchAction(context)],
    );
  }

  List<Widget> getDrawerWidgets(Map<String, Domain> domains) {
    List<Widget> widgets = [
      DrawerHeader(
          decoration: BoxDecoration(color: config.getConfig()['primaryColor']),
          child: Image(image: AssetImage('logo.png'))),
    ];

    domains.forEach((key, domain) {
      widgets.add(domain.getListTile(_selectDomain));
    });

    return widgets;
  }

  void _selectDomain(String domain) {
    setState(() {
      this._action = Action.GRID_VIEW;
      this._selectedDomain = this._domains[domain];
    });
    Navigator.pop(context);
  }

  void _openInputView(String entityId) {
    setState(() {
      this._action = Action.INPUT_VIEW;
      this._entityId = entityId;
    });
  }

  void _openGridView() {
    setState(() {
      this._action = Action.GRID_VIEW;
      this._entityId = null;
    });
  }

  void _setLogin() {
    setState(() {
      this._domains = getDomains(config.getToken()['role']);
      this._selectedDomain = this._domains.values.elementAt(0);

      this._viewAction = this._selectedDomain.name == 'Login'
          ? this._setLogin
          : this._openInputView;

      this._drawerWidgets = getDrawerWidgets(this._domains);
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: this.searchBar.build(context),
      body: Container(
        child: this._action == Action.GRID_VIEW
            ? this._selectedDomain.getDataView(this._viewAction)
            : this._selectedDomain.getInputView(this._entityId),
      ),
      drawer: Drawer(
        child: ListView(
          padding: EdgeInsets.zero,
          children: this._drawerWidgets,
        ),
      ),
      floatingActionButton: this._action == Action.GRID_VIEW
          ? this._selectedDomain.getGridActionButton(_openInputView)
          : this._selectedDomain.getInputActionButton(_openGridView),
    );
  }
}
