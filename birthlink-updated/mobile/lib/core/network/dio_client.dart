import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:logger/logger.dart';

import '../constants/api_endpoints.dart';
import 'api_exceptions.dart';

/// Provider for the Dio HTTP client.
final dioClientProvider = Provider<DioClient>((ref) {
  return DioClient();
});

/// Configured Dio HTTP client with interceptors.
/// 
/// For Android emulator: uses 10.0.2.2 to access host machine's localhost.
/// For iOS simulator: uses localhost directly.
class DioClient {
  late final Dio _dio;
  final Logger _logger = Logger();

  DioClient() {
    _dio = Dio(BaseOptions(
      // Using Android emulator base URL (10.0.2.2 -> host localhost)
      // For iOS simulator, this would need to be localhost:8080/api
      baseUrl: ApiEndpoints.devBaseUrl,
      connectTimeout: const Duration(seconds: 10),
      receiveTimeout: const Duration(seconds: 10),
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
      },
    ));

    _dio.interceptors.add(_LoggingInterceptor(_logger));
    _dio.interceptors.add(_ErrorInterceptor());
  }

  /// Set the volunteer phone number for API authentication.
  void setVolunteerPhone(String phoneNumber) {
    _dio.options.headers['X-Phone-Number'] = phoneNumber;
  }

  /// GET request.
  Future<Response<T>> get<T>(
    String path, {
    Map<String, dynamic>? queryParameters,
    Options? options,
  }) async {
    return _dio.get<T>(
      path,
      queryParameters: queryParameters,
      options: options,
    );
  }

  /// POST request.
  Future<Response<T>> post<T>(
    String path, {
    dynamic data,
    Map<String, dynamic>? queryParameters,
    Options? options,
  }) async {
    return _dio.post<T>(
      path,
      data: data,
      queryParameters: queryParameters,
      options: options,
    );
  }

  /// PUT request.
  Future<Response<T>> put<T>(
    String path, {
    dynamic data,
    Map<String, dynamic>? queryParameters,
    Options? options,
  }) async {
    return _dio.put<T>(
      path,
      data: data,
      queryParameters: queryParameters,
      options: options,
    );
  }

  /// DELETE request.
  Future<Response<T>> delete<T>(
    String path, {
    dynamic data,
    Map<String, dynamic>? queryParameters,
    Options? options,
  }) async {
    return _dio.delete<T>(
      path,
      data: data,
      queryParameters: queryParameters,
      options: options,
    );
  }
}

/// Interceptor for logging HTTP requests and responses.
class _LoggingInterceptor extends Interceptor {
  final Logger _logger;

  _LoggingInterceptor(this._logger);

  @override
  void onRequest(RequestOptions options, RequestInterceptorHandler handler) {
    _logger.d('🌐 ${options.method} ${options.uri}');
    if (options.data != null) {
      _logger.d('📤 Request: ${options.data}');
    }
    handler.next(options);
  }

  @override
  void onResponse(Response response, ResponseInterceptorHandler handler) {
    _logger.d('✅ ${response.statusCode} ${response.requestOptions.uri}');
    handler.next(response);
  }

  @override
  void onError(DioException err, ErrorInterceptorHandler handler) {
    _logger.e('❌ ${err.response?.statusCode ?? 'ERROR'} ${err.requestOptions.uri}');
    _logger.e('Error: ${err.message}');
    handler.next(err);
  }
}

/// Interceptor for converting Dio errors to application exceptions.
class _ErrorInterceptor extends Interceptor {
  @override
  void onError(DioException err, ErrorInterceptorHandler handler) {
    final exception = _mapDioException(err);
    handler.reject(
      DioException(
        requestOptions: err.requestOptions,
        error: exception,
        response: err.response,
        type: err.type,
      ),
    );
  }

  ApiException _mapDioException(DioException err) {
    switch (err.type) {
      case DioExceptionType.connectionTimeout:
      case DioExceptionType.sendTimeout:
      case DioExceptionType.receiveTimeout:
        return TimeoutException();
      case DioExceptionType.connectionError:
        return NetworkException();
      case DioExceptionType.badResponse:
        return _handleBadResponse(err.response);
      case DioExceptionType.cancel:
        return RequestCancelledException();
      default:
        return UnknownException(err.message ?? 'Unknown error');
    }
  }

  ApiException _handleBadResponse(Response? response) {
    if (response == null) {
      return UnknownException('No response');
    }

    switch (response.statusCode) {
      case 400:
        return BadRequestException(response.data?['message'] ?? 'Bad request');
      case 401:
        return UnauthorizedException();
      case 403:
        return ForbiddenException();
      case 404:
        return NotFoundException(response.data?['message'] ?? 'Not found');
      case 500:
      case 502:
      case 503:
        return ServerException();
      default:
        return UnknownException('Status: ${response.statusCode}');
    }
  }
}
