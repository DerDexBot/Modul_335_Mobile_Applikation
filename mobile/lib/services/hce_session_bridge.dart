import 'package:flutter/services.dart';

import '../models/flipper_auth_models.dart';

class HceSessionBridge {
  static const _channel = MethodChannel('flipper_auth/hce_session');

  static Future<void> updateSession({
    required AuthStartResponse session,
    required String username,
  }) async {
    await _channel.invokeMethod<void>('updateSession', {
      'sessionId': session.sessionId,
      'username': username,
      'action': session.action,
      'challenge': session.challenge,
      'expiresAt': session.expiresAt?.toIso8601String(),
    });
  }

  static Future<void> clearSession() async {
    await _channel.invokeMethod<void>('clearSession');
  }
}
