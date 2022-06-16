import 'package:flutter/material.dart';
import 'package:widget/Configuration.dart';
import 'package:widget/src/Util.dart';

class InputField extends StatefulWidget {
  final TextEditingController controller = TextEditingController();
  final String id;
  final String name;
  final IconData icon;
  final Configuration configuration;

  InputField({
    @required this.id,
    @required this.name,
    @required this.configuration,
    this.icon,
    String value,
  }) {
    controller.text = value;
  }

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
            fillColor: widget.configuration.common['secondaryColor'],
            focusColor: widget.configuration.common['focusColor'],
            focusedBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(25),
              borderSide:
                  BorderSide(color: widget.configuration.common['focusColor']),
            ),
            labelText: widget.name,
          ),
          controller: widget.controller,
        ),
      ),
    );
  }
}
