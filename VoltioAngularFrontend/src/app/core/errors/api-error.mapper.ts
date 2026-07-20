import { HttpErrorResponse } from '@angular/common/http';


/**
 * Standard application error format.
 *
 * All services/components receive errors
 * in this consistent shape.
 */
export interface ApiError {

  code: string;

  message: string;

  field: string | null;

}


/**
 * Extract the first validation error message.
 */
function firstValidationError(
  errors: unknown
): string | null {


  if (!errors) {
    return null;
  }


  // Spring validation format:
  // {
  //   "amount": "amount must be positive"
  // }
  if (
    typeof errors === 'object' &&
    !Array.isArray(errors)
  ) {

    const values =
      Object.values(errors as Record<string, unknown>);


    return typeof values[0] === 'string'
      ? values[0]
      : null;

  }


  // Array format:
  // [
  //   {
  //     defaultMessage:"..."
  //   }
  // ]
  if (
    Array.isArray(errors) &&
    errors.length > 0
  ) {

    const firstError = errors[0];


    if (typeof firstError === 'string') {
      return firstError;
    }


    if (
      typeof firstError === 'object' &&
      firstError !== null
    ) {

      const error = firstError as any;

      return (
        error.defaultMessage ||
        error.message ||
        null
      );

    }

  }

  return null;

}


/**
 * Converts any backend HTTP error into
 * the application's standard error format.
 */
export function mapApiError(
  error: HttpErrorResponse
): ApiError {


  const data = error.error;


  const validationMessage =
    firstValidationError(
      data?.errors
    );

  if (
    data?.code ===
    'ACCOUNT_TEMPORARILY_RESTRICTED'
  ) {

    return {

      code: data.code,

      message:
        'This account is temporarily restricted. Please contact support.',

      field:
        data.field ?? null

    };

  }

  if (
    data?.code ||
    data?.message
  ) {

    return {

      code:
        data.code ||
        `HTTP_${error.status || 'UNKNOWN'}`,

      message:
        data.message ||
        validationMessage ||
        `Request failed with status ${error.status}`,

      field:
        data.field ?? null

    };

  }


  if (validationMessage) {

    return {

      code:
        `HTTP_${error.status || 422}`,

      message:
        validationMessage,

      field: null

    };

  }


  if (
    typeof data === 'string' &&
    data.trim().length > 0
  ) {

    return {

      code:
        `HTTP_${error.status || 'UNKNOWN'}`,

      message:
        data,

      field: null

    };

  }



  /*
   * Unknown fallback.
   */
  return {

    code:
      'UNKNOWN_ERROR',

    message:
      error.status
        ? `Request failed with status ${error.status}`
        : 'Request failed',

    field: null

  };

}