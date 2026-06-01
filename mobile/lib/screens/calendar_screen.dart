import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../services/api_service.dart';
import '../services/auth_service.dart';

class CalendarScreen extends StatefulWidget {
  const CalendarScreen({super.key});

  @override
  State<CalendarScreen> createState() => _CalendarScreenState();
}

class _CalendarScreenState extends State<CalendarScreen> {
  late DateTime _from;
  late DateTime _to;
  bool _loading = false;
  String? _error;
  List<dynamic> _shifts = [];

  @override
  void initState() {
    super.initState();
    final now = DateTime.now();
    _from = DateTime(now.year, now.month, 1);
    _to = DateTime(now.year, now.month + 1, 0);

    WidgetsBinding.instance.addPostFrameCallback((_) => _loadCalendar());
  }

  Future<void> _loadCalendar() async {
    final auth = context.read<AuthService>();
    final employeeId = auth.userId;

    if (employeeId == null) {
      setState(() => _error = 'Keine Mitarbeiter-ID im Login gefunden. Bitte neu einloggen.');
      return;
    }

    setState(() {
      _loading = true;
      _error = null;
    });

    try {
      final api = ApiService(auth);
      final data = await api.get(
        '/api/planning/calendar/$employeeId?from=${_formatIsoDate(_from)}&to=${_formatIsoDate(_to)}',
      );
      setState(() => _shifts = data as List<dynamic>);
    } catch (_) {
      setState(() => _error = 'Arbeitskalender konnte nicht geladen werden.');
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  void _changeMonth(int offset) {
    setState(() {
      final next = DateTime(_from.year, _from.month + offset, 1);
      _from = next;
      _to = DateTime(next.year, next.month + 1, 0);
    });
    _loadCalendar();
  }

  @override
  Widget build(BuildContext context) {
    final auth = context.watch<AuthService>();

    return RefreshIndicator(
      onRefresh: _loadCalendar,
      child: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text(
                'Arbeitskalender',
                style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold),
              ),
              IconButton(
                tooltip: 'Aktualisieren',
                onPressed: _loading ? null : _loadCalendar,
                icon: const Icon(Icons.refresh),
              ),
            ],
          ),
          const SizedBox(height: 4),
          Text(
            auth.username != null ? 'Angemeldet als ${auth.username}' : 'Meine veröffentlichten Schichten',
            style: Theme.of(context).textTheme.bodySmall,
          ),
          const SizedBox(height: 16),
          Row(
            children: [
              IconButton(
                onPressed: _loading ? null : () => _changeMonth(-1),
                icon: const Icon(Icons.chevron_left),
              ),
              Expanded(
                child: Center(
                  child: Text(
                    _formatMonth(_from),
                    style: const TextStyle(fontSize: 18, fontWeight: FontWeight.w600),
                  ),
                ),
              ),
              IconButton(
                onPressed: _loading ? null : () => _changeMonth(1),
                icon: const Icon(Icons.chevron_right),
              ),
            ],
          ),
          const SizedBox(height: 8),
          if (_loading)
            const Padding(
              padding: EdgeInsets.only(top: 32),
              child: Center(child: CircularProgressIndicator()),
            )
          else if (_error != null)
            Card(
              color: Colors.red.shade50,
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Text(_error!, style: TextStyle(color: Colors.red.shade800)),
              ),
            )
          else if (_shifts.isEmpty)
            const Card(
              child: Padding(
                padding: EdgeInsets.all(16),
                child: Text('Für diesen Monat sind keine veröffentlichten Schichten vorhanden.'),
              ),
            )
          else
            ..._shifts.map((shift) => _ShiftCard(shift: shift)),
        ],
      ),
    );
  }

  String _formatIsoDate(DateTime value) {
    final year = value.year.toString().padLeft(4, '0');
    final month = value.month.toString().padLeft(2, '0');
    final day = value.day.toString().padLeft(2, '0');
    return '$year-$month-$day';
  }

  String _formatMonth(DateTime value) {
    const months = [
      'Januar', 'Februar', 'März', 'April', 'Mai', 'Juni',
      'Juli', 'August', 'September', 'Oktober', 'November', 'Dezember'
    ];
    return '${months[value.month - 1]} ${value.year}';
  }
}

class _ShiftCard extends StatelessWidget {
  const _ShiftCard({required this.shift});

  final dynamic shift;

  @override
  Widget build(BuildContext context) {
    final date = shift['shiftDate'] ?? '—';
    final start = _shortTime(shift['startTime']);
    final end = _shortTime(shift['endTime']);
    final hours = shift['plannedHours']?.toString() ?? '0';
    final orderId = shift['orderId'];
    final notes = shift['notes'];

    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(_formatDisplayDate(date), style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
                Chip(label: Text('$hours h')),
              ],
            ),
            const SizedBox(height: 8),
            Row(
              children: [
                const Icon(Icons.schedule, size: 18),
                const SizedBox(width: 8),
                Text('$start – $end'),
              ],
            ),
            if (orderId != null) ...[
              const SizedBox(height: 8),
              Row(
                children: [
                  const Icon(Icons.work_outline, size: 18),
                  const SizedBox(width: 8),
                  Text('Auftrag #$orderId'),
                ],
              ),
            ],
            if (notes != null && notes.toString().isNotEmpty) ...[
              const SizedBox(height: 8),
              Text(notes.toString(), style: Theme.of(context).textTheme.bodySmall),
            ],
          ],
        ),
      ),
    );
  }

  static String _shortTime(dynamic value) {
    if (value == null) return '—';
    final text = value.toString();
    return text.length >= 5 ? text.substring(0, 5) : text;
  }

  static String _formatDisplayDate(dynamic value) {
    if (value == null) return '—';
    final parts = value.toString().split('-');
    if (parts.length != 3) return value.toString();
    return '${parts[2]}.${parts[1]}.${parts[0]}';
  }
}
