export const apiBaseUrl: string = import.meta.env.VITE_API_BASE_URL ?? ''

export const requestTimeoutMs: number = Number(import.meta.env.VITE_API_TIMEOUT_MS ?? 8000)
