/**
 * Generates an idempotency key for POST money movement requests.
 *
 * Backend expects:
 *
 * Idempotency-Key: <uuid>
 */
export function generateIdempotencyKey(): string {

  if (typeof crypto !== 'undefined' && crypto.randomUUID) {
    return crypto.randomUUID();
  }

  // Fallback for older browsers
  return (
    Date.now().toString(36) +
    Math.random().toString(36).substring(2)
  );

}