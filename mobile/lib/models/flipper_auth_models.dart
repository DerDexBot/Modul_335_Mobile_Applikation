class AuthStartResponse {
  AuthStartResponse({
    required this.sessionId,
    required this.challenge,
    required this.action,
    required this.expiresAt,
  });

  final int sessionId;
  final String challenge;
  final String action;
  final DateTime? expiresAt;

  factory AuthStartResponse.fromJson(Map<String, dynamic> json) {
    return AuthStartResponse(
      sessionId: _readInt(json['sessionId']),
      challenge: _readString(json['challenge']),
      action: _readString(json['action']),
      expiresAt: _readDateTime(json['expiresAt']),
    );
  }
}

class AuthStatus {
  AuthStatus({
    required this.sessionId,
    required this.username,
    required this.action,
    required this.used,
    required this.success,
    required this.loggedIn,
  });

  final int sessionId;
  final String username;
  final String action;
  final bool used;
  final bool success;
  final bool loggedIn;

  factory AuthStatus.fromJson(Map<String, dynamic> json) {
    return AuthStatus(
      sessionId: _readInt(json['sessionId']),
      username: _readString(json['username']),
      action: _readString(json['action']),
      used: _readBool(json['used']),
      success: _readBool(json['success']),
      loggedIn: _readBool(json['loggedIn']),
    );
  }
}

class ApiMessage {
  ApiMessage({required this.success, required this.message});

  final bool success;
  final String message;

  factory ApiMessage.fromJson(Map<String, dynamic> json) {
    return ApiMessage(
      success: _readBool(json['success']),
      message: _readString(json['message']),
    );
  }
}

int _readInt(Object? value) {
  if (value is int) {
    return value;
  }
  return int.parse(value.toString());
}

String _readString(Object? value) {
  return value?.toString() ?? '';
}

bool _readBool(Object? value) {
  if (value is bool) {
    return value;
  }
  return value?.toString().toLowerCase() == 'true';
}

DateTime? _readDateTime(Object? value) {
  final text = value?.toString();
  if (text == null || text.isEmpty) {
    return null;
  }
  return DateTime.tryParse(text)?.toLocal();
}
