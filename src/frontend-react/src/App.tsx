import { Link, Navigate, Route, Routes } from 'react-router-dom'
import { CustomerInquiryPage } from './features/customerInquiry/CustomerInquiryPage'
import { AccountInquiryPage } from './features/accountInquiry/AccountInquiryPage'
import { CustomerAccountInquiryPage } from './features/customerAccountInquiry/CustomerAccountInquiryPage'

function App() {
  return (
    <>
      <nav className="app-nav" aria-label="Main navigation">
        <Link to="/customers">INQCUST</Link>
        <Link to="/accounts">INQACC</Link>
        <Link to="/customer-accounts">INQACCCU</Link>
      </nav>
      <Routes>
        <Route path="/" element={<Navigate to="/customers" replace />} />
        <Route path="/customers" element={<CustomerInquiryPage />} />
        <Route path="/accounts" element={<AccountInquiryPage />} />
        <Route path="/customer-accounts" element={<CustomerAccountInquiryPage />} />
        <Route path="*" element={<Navigate to="/customers" replace />} />
      </Routes>
    </>
  )
}

export default App
