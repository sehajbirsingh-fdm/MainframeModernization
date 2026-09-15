export interface CodeAssistantHistoryItem {
  role: 'user' | 'assistant'
  content: string
}

export interface CodeAssistantSource {
  file_id: string
  start_line: number
  end_line: number
  cosine_similarity: number
}

export interface CodeAssistantResponse {
  answer: string
  answer_model: string
  sources: CodeAssistantSource[]
}

const ragApiUrl: string =
  import.meta.env.VITE_RAG_API_URL ?? 'http://localhost:8000/rag-api/query'

interface CodeAssistantStreamEvent {
  type: 'status' | 'result' | 'error'
  message?: string
  data?: CodeAssistantResponse
}

export async function askCodeAssistant(
  question: string,
  history: CodeAssistantHistoryItem[],
  onStatus?: (message: string) => void,
  signal?: AbortSignal,
): Promise<CodeAssistantResponse> {
  const response = await fetch(`${ragApiUrl.replace(/\/$/, '')}/stream`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ question, history }),
    signal,
  })

  if (!response.ok) {
    let message = `The code assistant returned HTTP ${response.status}.`
    try {
      const payload = (await response.json()) as { detail?: string }
      if (payload.detail) {
        message = payload.detail
      }
    } catch {
      // Keep the status-based message when the response is not JSON.
    }
    throw new Error(message)
  }

  if (!response.body) {
    throw new Error('The code assistant returned an empty response stream.')
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''
  let result: CodeAssistantResponse | undefined

  function handleLine(line: string): void {
    if (!line.trim()) {
      return
    }
    const event = JSON.parse(line) as CodeAssistantStreamEvent
    if (event.type === 'status' && event.message) {
      onStatus?.(event.message)
    } else if (event.type === 'result' && event.data) {
      result = event.data
    } else if (event.type === 'error') {
      throw new Error(event.message ?? 'The code assistant could not complete the request.')
    }
  }

  while (true) {
    const { value, done } = await reader.read()
    buffer += decoder.decode(value, { stream: !done })
    const lines = buffer.split('\n')
    buffer = lines.pop() ?? ''
    lines.forEach(handleLine)
    if (done) {
      break
    }
  }
  handleLine(buffer)

  if (!result) {
    throw new Error('The code assistant completed without returning an answer.')
  }
  return result
}
