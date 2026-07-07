import { ApiError } from "@/lib/api";

type FieldError = {
  field: string;
  message: string;
};

type ErrorEnvelope = {
  message?: string;
  errors?: FieldError[];
};

export function getApiErrorMessage(error: unknown, fallback = "Đã có lỗi xảy ra"): string {
  if (error instanceof ApiError) {
    const payload = error.payload as ErrorEnvelope | null;
    if (payload?.errors?.length) {
      return payload.errors.map((item) => item.message).join(", ");
    }
    if (payload?.message) return payload.message;
  }
  if (error instanceof Error && error.message) return error.message;
  return fallback;
}
