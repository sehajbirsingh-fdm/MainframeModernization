import { Navigate, Route, Routes } from 'react-router-dom'
import { CustomerInquiryPage } from './features/customerInquiry/CustomerInquiryPage'

function App() {
  return (
    <Routes>
      <Route path="/" element={<CustomerInquiryPage />} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

export default App
