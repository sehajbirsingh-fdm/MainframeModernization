import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { askCodeAssistant } from '../../api/codeAssistantClient'
import { CodeAssistantOverlay } from './CodeAssistantOverlay'

vi.mock('../../api/codeAssistantClient', () => ({
  askCodeAssistant: vi.fn(),
}))

const mockedAskCodeAssistant = vi.mocked(askCodeAssistant)

describe('CodeAssistantOverlay', () => {
  beforeEach(() => {
    mockedAskCodeAssistant.mockReset()
  })

  it('opens without changing the surrounding application layout', async () => {
    const user = userEvent.setup()
    render(<CodeAssistantOverlay />)

    await user.click(screen.getByRole('button', { name: 'Open code assistant' }))

    expect(screen.getByRole('complementary', { name: 'Code assistant' })).toHaveClass(
      'rag-assistant-panel-open',
    )
    expect(screen.getByRole('heading', { name: 'How can I help you?' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Annotation mode is not available yet' })).toBeDisabled()
  })

  it('renders the retrieved answer as Markdown', async () => {
    mockedAskCodeAssistant.mockResolvedValue({
      answer: '## Customer lookup\n\nUse `0000000000` for a random customer.',
      answer_model: 'openai/gpt-oss-120b',
      sources: [
        {
          file_id: 'frontend/app/src/features/customerInquiry/CustomerInquiryPage.tsx',
          start_line: 150,
          end_line: 180,
          cosine_similarity: 0.71,
        },
      ],
    })

    const user = userEvent.setup()
    render(<CodeAssistantOverlay />)

    await user.click(screen.getByRole('button', { name: 'Open code assistant' }))
    await user.type(screen.getByRole('textbox', { name: 'Codebase question' }), 'How do I find a random customer?')
    await user.click(screen.getByRole('button', { name: 'Send question' }))

    expect(await screen.findByRole('heading', { name: 'Customer lookup' })).toBeInTheDocument()
    expect(screen.getByText('0000000000')).toBeInTheDocument()
    expect(screen.getByText('1 retrieved chunks')).toBeInTheDocument()
  })

  it('shows the validation phase before displaying the approved answer', async () => {
    let finishRequest: ((response: Awaited<ReturnType<typeof askCodeAssistant>>) => void) | undefined
    mockedAskCodeAssistant.mockImplementation((_question, _history, onStatus) => {
      onStatus?.('Validating the response...')
      return new Promise((resolve) => {
        finishRequest = resolve
      })
    })

    const user = userEvent.setup()
    render(<CodeAssistantOverlay />)

    await user.click(screen.getByRole('button', { name: 'Open code assistant' }))
    await user.type(screen.getByRole('textbox', { name: 'Codebase question' }), 'How does inquiry work?')
    await user.click(screen.getByRole('button', { name: 'Send question' }))

    expect(screen.getByRole('status')).toHaveTextContent('Validating the response...')

    finishRequest?.({
      answer: 'The validated answer.',
      answer_model: 'openai/gpt-oss-120b',
      sources: [],
    })
    expect(await screen.findByText('The validated answer.')).toBeInTheDocument()
  })
})
