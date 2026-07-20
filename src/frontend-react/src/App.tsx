import { Navigate, Route, Routes } from 'react-router-dom'
import { CustomerInquiryPage } from './features/customerInquiry/CustomerInquiryPage'
import { AccountInquiryPage } from './features/accountInquiry/AccountInquiryPage'

function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/customers" replace />} />
      <Route path="/customers" element={<CustomerInquiryPage />} />
      <Route path="/accounts" element={<AccountInquiryPage />} />
      <Route path="*" element={<Navigate to="/customers" replace />} />
    </Routes>
  )
}

export default App
