import 'package:flutter/material.dart';

class CheckInScreen extends StatefulWidget {
  const CheckInScreen({super.key});

  @override
  State<CheckInScreen> createState() => _CheckInScreenState();
}

class _CheckInScreenState extends State<CheckInScreen> {
  bool _checkedIn = false;

  void _toggleCheckIn() {
    setState(() => _checkedIn = !_checkedIn);
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text(_checkedIn ? 'Eingecheckt!' : 'Ausgecheckt!')),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(
            _checkedIn ? Icons.check_circle : Icons.radio_button_unchecked,
            size: 80,
            color: _checkedIn ? Colors.green : Colors.grey,
          ),
          const SizedBox(height: 24),
          ElevatedButton.icon(
            onPressed: _toggleCheckIn,
            icon: Icon(_checkedIn ? Icons.logout : Icons.login),
            label: Text(_checkedIn ? 'Check-out' : 'Check-in'),
            style: ElevatedButton.styleFrom(
              padding: const EdgeInsets.symmetric(horizontal: 40, vertical: 16),
            ),
          ),
        ],
      ),
    );
  }
}
