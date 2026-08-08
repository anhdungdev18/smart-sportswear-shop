// Tiny module-level toast bus. Any client component can call `toast.success(...)`
// and the <Toaster /> mounted in the root layout renders it — no context wiring
// through the server-component tree required.

export type ToastKind = "success" | "error" | "info";
export type ToastItem = { id: number; kind: ToastKind; message: string };

type Listener = (item: ToastItem) => void;

let listeners: Listener[] = [];
let counter = 0;

export function subscribeToast(fn: Listener): () => void {
  listeners.push(fn);
  return () => {
    listeners = listeners.filter((l) => l !== fn);
  };
}

function emit(kind: ToastKind, message: string): void {
  const item: ToastItem = { id: ++counter, kind, message };
  for (const l of listeners) l(item);
}

export const toast = {
  success: (message: string) => emit("success", message),
  error: (message: string) => emit("error", message),
  info: (message: string) => emit("info", message),
};
