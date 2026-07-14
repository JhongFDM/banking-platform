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
 *
 * Handles backend responses like:
 *
 * errors: [
 *   {
 *      defaultMessage: "Amount is required"
 *   }
 * ]
 */
function firstValidationError(
  errors: unknown
): string | null {


  if (
    !Array.isArray(errors) ||
    errors.length === 0
  ) {

    return null;

  }


  const firstError = errors[0];


  if (typeof firstError === 'string') {

    return firstError;

  }


  if (
    typeof firstError === 'object' &&
    firstError !== null
  ) {

    const error =
      firstError as any;


    return (
      error.defaultMessage ||
      error.message ||
      null
    );

  }


  return null;

}


/**
 * Converts any backend HTTP error into
 * the application's standard error format.
 *
 * Replaces:
 *
 * mapAxiosError(error)
 */
export function mapApiError(
  error: HttpErrorResponse
): ApiError {


  const data = error.error;


  const validationMessage =
    firstValidationError(
      data?.errors
    );


  /*
   * Special account restriction message.
   *
   * Keeps existing React behavior.
   */
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



  /*
   * Backend returned:
   *
   * {
   *   code,
   *   message,
   *   field
   * }
   */
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



  /*
   * Validation errors without
   * code/message at the root.
   */
  if (validationMessage) {

    return {

      code:
        `HTTP_${error.status || 422}`,

      message:
        validationMessage,

      field: null

    };

  }



  /*
   * Backend returned plain text.
   */
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