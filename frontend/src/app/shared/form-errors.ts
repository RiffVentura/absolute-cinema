import { HttpErrorResponse } from '@angular/common/http';
import {
  FieldTree,
  isFieldTree,
  ValidationError as FormValidationError,
  WithOptionalFieldTree,
} from '@angular/forms/signals';

export interface ValidationError {
  field: string | null;
  message: string;
}

export interface ValidationErrors {
  type?: string;
  title: string;
  status: number;
  detail: string;
  instance?: string;
  errors?: ValidationError[];
}

type SubmissionError = WithOptionalFieldTree<FormValidationError>;

export function toSubmissionErrors<T>(error: unknown, form: FieldTree<T>): SubmissionError[] {
  const badRequest = error instanceof HttpErrorResponse && error.status === 400;
  const errors = badRequest ? (error.error as ValidationErrors | null)?.errors : undefined;
  if (!errors?.length) {
    const message = badRequest
      ? 'The server rejected this, check the fields'
      : 'Could not save, try again';
    return [{ kind: 'server', message }];
  }
  return errors.map(({ field, message }) => {
    const target: unknown =
      field === null ? undefined : (form as unknown as Record<string, unknown>)[field];
    return isFieldTree(target)
      ? { kind: 'server', message, fieldTree: target }
      : { kind: 'server', message };
  });
}
