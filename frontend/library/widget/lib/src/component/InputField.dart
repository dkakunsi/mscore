library widget;

import 'package:flutter/material.dart';

import '../Util.dart';
import '../../Configuration.dart';

var config = Configuration();

class InputField extends StatefulWidget {
  final TextEditingController controller = TextEditingController();

  final String elementId;

  final String elementName;

  final IconData icon;

  InputField(this.elementId, this.elementName, [this.icon]);

  @override
  State<StatefulWidget> createState() => InputFieldState();
}

class InputFieldState extends State<InputField> {
  @override
  Widget build(BuildContext context) {
    return Container(
      width: isWebScreen(context) ? 360 : getMobileScreenWidth(context),
      child: Padding(
        padding: EdgeInsets.all(4.0),
        child: TextField(
          decoration: InputDecoration(
            prefixIcon: Padding(
              padding: const EdgeInsets.all(8.0),
              child: Icon(widget.icon ?? Icons.pending,
                  color: Colors.grey, size: 30),
            ),
            enabledBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(25),
              borderSide: BorderSide(color: Colors.grey),
            ),
            filled: true,
            fillColor: config.getConfig()['secondaryColor'],
            focusColor: config.getConfig()['focusColor'],
            focusedBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(25),
              borderSide: BorderSide(color: config.getConfig()['focusColor']),
            ),
            labelText: widget.elementName,
          ),
          controller: widget.controller,
        ),
      ),
    );
  }
}
