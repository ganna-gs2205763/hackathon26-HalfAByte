/// Base class for API exceptions.
sealed class ApiException implements Exception {
  final String message;
  
  ApiException(this.message);

  @override
  String toString() => message;
}

/// Network connectivity error.
class NetworkException extends ApiException {
  NetworkException() : super('No internet connection. Please check your network.');
}

/// Request timeout.
class TimeoutException extends ApiException {
  TimeoutException() : super('Request timed out. Please try again.');
}

/// Request was cancelled.
class RequestCancelledException extends ApiException {
  RequestCancelledException() : super('Request was cancelled.');
}

/// Bad request (400).
class BadRequestException extends ApiException {
  BadRequestException(String message) : super(message);
}

/// Unauthorized (401).
class UnauthorizedException extends ApiException {
  UnauthorizedException() : super('Authentication required. Please log in again.');
}

/// Forbidden (403).
class ForbiddenException extends ApiException {
  ForbiddenException() : super('You do not have permission to perform this action.');
}

/// Not found (404).
class NotFoundException extends ApiException {
  NotFoundException(String message) : super(message);
}

/// Server error (5xx).
class ServerException extends ApiException {
  ServerException() : super('Server error. Please try again later.');
}

/// Unknown error.
class UnknownException extends ApiException {
  UnknownException(String message) : super('An error occurred: $message');
}
