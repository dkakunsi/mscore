library widget;

import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

double getScreenWidth(BuildContext context) =>
    MediaQuery.of(context).size.width;

bool isWebScreen(BuildContext context) => getScreenWidth(context) > 600;

double getMobileScreenWidth(BuildContext context) =>
    MediaQuery.of(context).size.width * 0.8;

int gridCount(BuildContext context) {
  var crossAxisCount = 1;
  if (isWebScreen(context)) {
    crossAxisCount = 2;
  }
  return crossAxisCount;
}

double getGridAspectRation(BuildContext context) {
  var width = getMobileScreenWidth(context);
  var height = 160;
  if (isWebScreen(context)) {
    width = getScreenWidth(context);
    width /= 2;
    height = 220;
  }
  return width / height;
}

dynamic getJSONValue(Map json, String attribute) {
  if (attribute.contains('_')) {
    var elements = attribute.split('_');
    dynamic value = json;
    elements.forEach((element) {
      value = value[element];
    });
    return value.toString();
  }
  return json[attribute] ?? '';
}

void setJSONValue(Map json, String attribute, value) {
  if (attribute.contains('_')) {
    var elements = attribute.split('_');
    var length = elements.length;

    var currentJson = json;
    var i = 1;
    elements.forEach((element) {
      if (i < length) {
        if (currentJson[element] == null) {
          var nextJson = {};
          currentJson[element] = nextJson;
          currentJson = nextJson;
        } else {
          currentJson = currentJson[element];
        }
      } else {
        currentJson[element] = value;
      }

      i++;
    });
  } else {
    json[attribute] = value;
  }
}

var formatter = DateFormat('yyyy-MM-dd hh:mm:ss');
String toDateTime(String dateTime) {
  if (dateTime == null) {
    return '';
  }
  var dt = DateTime.parse(dateTime);
  return formatter.format(dt);
}

String getViewData(Map data) {
  var viewData = data['name'];
  if (viewData == null) {
    viewData = data['id'];
  }
  return viewData;
}
