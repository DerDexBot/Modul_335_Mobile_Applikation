import 'dart:async';

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../models/flipper_auth_models.dart';
import '../services/auth_service.dart';
import '../services/api_config.dart';
import '../services/flipper_auth_api.dart';
import '../services/hce_session_bridge.dart';

class FlipperAuthScreen extends StatefulWidget {
  const FlipperAuthScreen({super.key});

  @override
  State<FlipperAuthScreen> createState() => _FlipperAuthScreenState();
}

class _FlipperAuthScreenState extends State<FlipperAuthScreen> {
  final _backendUrlController = TextEditingController(
    text: ApiConfig.baseUrl,
  );
  final _usernameController = TextEditingController(text: 'emp.meier');

  Timer? _pollTimer;
  AuthStartResponse? _session;
  AuthStatus? _status;
  String _stateText = 'Abgemeldet';
  String? _error;
  String? _message;
  bool _busy = false;

  @override
  void initState() {
    super.initState();
    final username = context.read<AuthService>().username;
    if (username != null && username.isNotEmpty) {
      _usernameController.text = username;
    }
    unawaited(HceSessionBridge.clearSession().catchError((_) {}));
  }

  @override
  void dispose() {
    _pollTimer?.cancel();
    _backendUrlController.dispose();
    _usernameController.dispose();
    super.dispose();
  }

  AuthApi get _api => AuthApi(_backendUrlController.text);

  Future<void> _start(String action) async {
    _pollTimer?.cancel();
    setState(() {
      _busy = true;
      _error = null;
      _message = null;
      _status = null;
      _stateText = 'Warte auf Flipper';
    });

    try {
      final session = await _api.start(
        username: _usernameController.text.trim(),
        action: action,
      );
      if (!mounted) {
        return;
      }

      setState(() {
        _session = session;
        _message = '${action == 'LOGIN' ? 'Login' : 'Logout'} gestartet';
      });
      await HceSessionBridge.updateSession(
        session: session,
        username: _usernameController.text.trim(),
      );
      await _loadStatus(session.sessionId);
      _startPolling(session.sessionId);
    } catch (error) {
      _showError(error);
    } finally {
      if (mounted) {
        setState(() {
          _busy = false;
        });
      }
    }
  }

  void _startPolling(int sessionId) {
    _pollTimer?.cancel();
    _pollTimer = Timer.periodic(const Duration(seconds: 2), (_) {
      _loadStatus(sessionId);
    });
  }

  Future<void> _loadStatus(int sessionId) async {
    try {
      final status = await _api.status(sessionId);
      if (!mounted) {
        return;
      }

      setState(() {
        _status = status;
        _stateText = _labelForStatus(status);
      });

      if (status.used) {
        _pollTimer?.cancel();
        await HceSessionBridge.clearSession();
      }
    } catch (error) {
      _showError(error);
    }
  }

  Future<void> _simulate() async {
    final session = _session;
    if (session == null) {
      return;
    }

    setState(() {
      _busy = true;
      _error = null;
      _message = null;
    });

    try {
      final response = await _api.simulateDevice(session.sessionId);
      if (!mounted) {
        return;
      }
      setState(() {
        _message = response.message;
      });
      await _loadStatus(session.sessionId);
    } catch (error) {
      _showError(error);
    } finally {
      if (mounted) {
        setState(() {
          _busy = false;
        });
      }
    }
  }

  void _showError(Object error) {
    if (!mounted) {
      return;
    }
    setState(() {
      _error = error.toString();
      _stateText = 'Fehler';
    });
  }

  String _labelForStatus(AuthStatus status) {
    if (!status.used) {
      return 'Warte auf Flipper';
    }
    return status.loggedIn ? 'Angemeldet' : 'Abgemeldet';
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Flipper Auth')),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(20),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              _InputCard(
                backendUrlController: _backendUrlController,
                usernameController: _usernameController,
              ),
              const SizedBox(height: 16),
              _ActionButtons(
                busy: _busy,
                hasSession: _session != null,
                onLogin: () => _start('LOGIN'),
                onLogout: () => _start('LOGOUT'),
                onSimulate: _simulate,
              ),
              const SizedBox(height: 16),
              _StatusCard(
                stateText: _stateText,
                session: _session,
                status: _status,
                message: _message,
                error: _error,
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _InputCard extends StatelessWidget {
  const _InputCard({
    required this.backendUrlController,
    required this.usernameController,
  });

  final TextEditingController backendUrlController;
  final TextEditingController usernameController;

  @override
  Widget build(BuildContext context) {
    return Card(
      elevation: 0,
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Text('Verbindung', style: Theme.of(context).textTheme.titleLarge),
            const SizedBox(height: 12),
            TextField(
              controller: backendUrlController,
              decoration: const InputDecoration(
                labelText: 'Backend URL',
                border: OutlineInputBorder(),
              ),
              keyboardType: TextInputType.url,
            ),
            const SizedBox(height: 12),
            TextField(
              controller: usernameController,
              decoration: const InputDecoration(
                labelText: 'Username',
                border: OutlineInputBorder(),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _ActionButtons extends StatelessWidget {
  const _ActionButtons({
    required this.busy,
    required this.hasSession,
    required this.onLogin,
    required this.onLogout,
    required this.onSimulate,
  });

  final bool busy;
  final bool hasSession;
  final VoidCallback onLogin;
  final VoidCallback onLogout;
  final VoidCallback onSimulate;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        FilledButton(
          onPressed: busy ? null : onLogin,
          child: const Text('Login starten'),
        ),
        const SizedBox(height: 10),
        FilledButton.tonal(
          onPressed: busy ? null : onLogout,
          child: const Text('Logout starten'),
        ),
        const SizedBox(height: 10),
        OutlinedButton(
          onPressed: busy || !hasSession ? null : onSimulate,
          child: const Text('Flipper simulieren'),
        ),
      ],
    );
  }
}

class _StatusCard extends StatelessWidget {
  const _StatusCard({
    required this.stateText,
    required this.session,
    required this.status,
    required this.message,
    required this.error,
  });

  final String stateText;
  final AuthStartResponse? session;
  final AuthStatus? status;
  final String? message;
  final String? error;

  @override
  Widget build(BuildContext context) {
    return Card(
      elevation: 0,
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('Status', style: Theme.of(context).textTheme.titleLarge),
            const SizedBox(height: 10),
            _StateChip(label: stateText),
            if (message != null) ...[
              const SizedBox(height: 10),
              Text(message!, style: const TextStyle(color: Color(0xFF166534))),
            ],
            if (error != null) ...[
              const SizedBox(height: 10),
              Text(error!, style: const TextStyle(color: Color(0xFFB91C1C))),
            ],
            const SizedBox(height: 16),
            _InfoRow(label: 'Session ID', value: session?.sessionId.toString()),
            _InfoRow(label: 'Challenge', value: session?.challenge),
            _InfoRow(label: 'Action', value: session?.action),
            _InfoRow(label: 'Läuft ab', value: _formatDate(session?.expiresAt)),
            _InfoRow(
              label: 'Backend Status',
              value: _formatBackendStatus(status),
            ),
          ],
        ),
      ),
    );
  }
}

class _StateChip extends StatelessWidget {
  const _StateChip({required this.label});

  final String label;

  @override
  Widget build(BuildContext context) {
    return DecoratedBox(
      decoration: BoxDecoration(
        color: const Color(0xFFEFF6FF),
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: const Color(0xFFBFDBFE)),
      ),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
        child: Text(
          label,
          style: const TextStyle(
            color: Color(0xFF1D4ED8),
            fontWeight: FontWeight.w700,
          ),
        ),
      ),
    );
  }
}

class _InfoRow extends StatelessWidget {
  const _InfoRow({required this.label, required this.value});

  final String label;
  final String? value;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 6),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(label, style: Theme.of(context).textTheme.labelMedium),
          const SizedBox(height: 2),
          SelectableText(value == null || value!.isEmpty ? '-' : value!),
        ],
      ),
    );
  }
}

String _formatDate(DateTime? value) {
  if (value == null) {
    return '-';
  }
  return value.toIso8601String();
}

String _formatBackendStatus(AuthStatus? status) {
  if (status == null) {
    return '-';
  }
  return '${status.used ? 'benutzt' : 'offen'}, '
      '${status.success ? 'erfolgreich' : 'ausstehend'}, '
      '${status.loggedIn ? 'angemeldet' : 'abgemeldet'}';
}
