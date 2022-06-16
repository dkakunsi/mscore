library widget;

import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:uuid/uuid.dart';
import 'package:api/api.dart';
import 'package:widget/src/component/DataPage.dart';

import '../Domain.dart';
import '../component/InputField.dart';
import '../../Configuration.dart';

var config = Configuration();
var api = Api(config.getConfig());

class Login extends Domain {
  Login() : super('Login', 'This is login');

  @override
  SearchableWidget getDataView(Function onLogin) {
    return LoginDataView(onLogin);
  }

  @override
  Widget getInputView(String entityId) {
    return Column(children: [
      Text('Login input view'),
    ]);
  }
}

class LoginDataView extends SearchableWidget {
  final Map<String, Map<String, dynamic>> _attributes = {
    'username': {'title': 'Username', 'icon': Icons.account_circle},
    'password': {'title': 'Password', 'icon': Icons.vpn_key_rounded}
  };

  final List<InputField> _inputFields = [];

  final Function _onLogin;

  LoginDataView(this._onLogin) {
    this._attributes.forEach((key, detail) {
      var name = detail['title'].toString();
      var icon = detail['icon'];
      this._inputFields.add(InputField(key, name, icon));
    });
  }

  void _sendAuthentication() {
    var jsonData = {};
    this._inputFields.forEach((inputField) {
      jsonData[inputField.elementId] = inputField.controller.text;
    });

    api.login(Uuid().v4(), jsonData['username'], jsonData['password']).then(
      (value) {
        var token = jsonDecode(value['message']);
        config.setToken(token);
        this._onLogin();
      },
    );
  }

  @override
  void search(String value) {}

  @override
  State<StatefulWidget> createState() => _LoginDataViewState();
}

class _LoginDataViewState extends State<LoginDataView> {
  @override
  Widget build(BuildContext context) {
    bool isWebScreen = MediaQuery.of(context).size.width > 500;
    var mobileScreenWidth = MediaQuery.of(context).size.width * 0.8;

    return Stack(
      children: <Widget>[
        Align(
          alignment: Alignment.topCenter,
          child: Container(
            width: MediaQuery.of(context).size.width,
            color: config.getConfig()['primaryColor'],
            child: Column(
              mainAxisSize: MainAxisSize.max,
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              crossAxisAlignment: CrossAxisAlignment.center,
              children: <Widget>[
                Column(
                  children: [
                    Padding(
                      padding: const EdgeInsets.symmetric(
                        horizontal: 50,
                        vertical: 3,
                      ),
                      child: Hero(
                        tag: 'hero-login',
                        child: Image(
                          image: AssetImage('logo.png'),
                          width: isWebScreen ? 200 : 100,
                          height: isWebScreen ? 200 : 100,
                          fit: BoxFit.fill,
                        ),
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ),
        Align(
          alignment: Alignment.bottomCenter,
          child: Container(
            height: isWebScreen
                ? MediaQuery.of(context).size.height * 0.5
                : MediaQuery.of(context).size.height * 0.7,
            width: MediaQuery.of(context).size.width,
            decoration: new BoxDecoration(
              color: config.getConfig()['secondaryColor'],
              borderRadius: new BorderRadius.only(
                topLeft: const Radius.circular(50.0),
                topRight: const Radius.circular(50.0),
              ),
            ),
            child: Column(
              children: <Widget>[
                SizedBox(height: 10),
                widget._inputFields[0],
                widget._inputFields[1],
                Container(
                  width: isWebScreen ? 360 : mobileScreenWidth,
                  child: Padding(
                    padding: EdgeInsets.only(top: 10, right: 40, left: 40),
                    child: ElevatedButton(
                      onPressed: widget._sendAuthentication,
                      child: Text('Login',
                          style: TextStyle(
                            fontSize: 14,
                            color: config.getConfig()['secondaryColor'],
                          )),
                      style: ElevatedButton.styleFrom(
                        primary: config.getConfig()['primaryColor'],
                        onPrimary: config.getConfig()['secondaryColor'],
                        minimumSize: Size(200, 50),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.all(Radius.circular(25)),
                        ),
                      ),
                    ),
                  ),
                ),
              ],
            ),
          ),
        ),
      ],
    );
  }
}
