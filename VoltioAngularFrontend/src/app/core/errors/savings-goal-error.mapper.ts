import { HttpErrorResponse } from '@angular/common/http';
import { ApiError } from './api-error.mapper';
import { mapApiError } from './api-error.mapper';


/**
 * Backend error codes returned by the Savings Goal APIs.
 */
const SAVINGS_GOAL_ERROR_MESSAGES: Record<string, string> = {

  // 400 - Validation errors

  INVALID_TARGET_AMOUNT:
    'Target amount must be greater than $0',

  INVALID_TARGET_DATE:
    'Target date must be today or in the future',

  INVALID_GOAL_NAME:
    'Please enter a goal name',

  MISSING_REQUIRED_FIELD:
    'All fields are required',


  // 403 - Authorization errors

  UNAUTHORIZED_ACCOUNT_ACCESS:
    'You do not have permission to access this account',


  // 404 - Not found errors

  ACCOUNT_NOT_FOUND:
    'Account not found or is inactive',

  GOAL_NOT_FOUND:
    'Savings goal not found',


  // 409 - Conflict errors

  GOAL_ALREADY_EXISTS:
    'An active goal already exists for this account',


  // 500 - Server errors

  INTERNAL_SERVER_ERROR:
    'Something went wrong. Please try again.'

};


/**
 * Converts a Savings Goal backend error code
 * into a message shown to the user.
 */
export function mapSavingsGoalErrorCode(
  errorCode: string
): string {


  return (
    SAVINGS_GOAL_ERROR_MESSAGES[errorCode]
    ??
    errorCode
  );

}


/**
 * Determines whether an error belongs
 * to the Savings Goal feature.
 *
 * This prevents accidentally replacing
 * messages for unrelated APIs.
 */
function isSavingsGoalError(
  code: string | undefined
): boolean {


  if (!code) {

    return false;

  }


  return (
    code in SAVINGS_GOAL_ERROR_MESSAGES
  );

}


/**
 * Enhanced Savings Goal error mapper.
 */
export function mapSavingsGoalError(
  error: HttpErrorResponse
): ApiError {

  const baseError =
    mapApiError(error);



  /*
   * If this is a known Savings Goal
   * error, replace only the message.
   */
  if (
    isSavingsGoalError(baseError.code)
  ) {

    return {

      ...baseError,

      message:
        mapSavingsGoalErrorCode(
          baseError.code
        )

    };

  }


  /*
   * Not a Savings Goal-specific error.
   *
   * Return the generic mapped error.
   */
  return baseError;

}