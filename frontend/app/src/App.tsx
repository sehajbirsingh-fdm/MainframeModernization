import { Component, type ErrorInfo, type ReactNode } from 'react'
import { Link, NavLink, Navigate, Outlet, Route, Routes } from 'react-router-dom'
import { CustomerInquiryPage } from './features/customerInquiry/CustomerInquiryPage'
import { AccountInquiryPage } from './features/accountInquiry/AccountInquiryPage'
import { CustomerCreatePage } from './features/customerCreate/CustomerCreatePage'
import { CustomerAccountInquiryPage } from './features/customerAccountInquiry/CustomerAccountInquiryPage'
import { TransactionInquiryPage } from './features/transactionInquiry/TransactionInquiryPage'
import { StatementInquiryPage } from './features/statementInquiry/StatementInquiryPage'
import { TransactionDetailPage } from './features/transactionInquiry/TransactionDetailPage'
import { CustomerUpdatePage } from './features/customerUpdate/CustomerUpdatePage'

interface SiteLink {
  to: string
  label: string
  code?: string
}

interface RouteErrorBoundaryState {
  hasError: boolean
}

class RouteErrorBoundary extends Component<{ children: ReactNode }, RouteErrorBoundaryState> {
  constructor(props: { children: ReactNode }) {
    super(props)
    this.state = { hasError: false }
  }

  static getDerivedStateFromError(): RouteErrorBoundaryState {
    return { hasError: true }
  }

  componentDidCatch(error: unknown, errorInfo: ErrorInfo): void {
    console.error('Route render failure', error, errorInfo)
  }

  render() {
    if (this.state.hasError) {
      return (
        <section className="page" aria-labelledby="ui-error-heading">
          <header className="page-header">
            <p className="pill">SYSTEM</p>
            <h2 id="ui-error-heading">Something Went Wrong</h2>
            <p>The page hit an unexpected error. Please return to Customer Inquiry and retry.</p>
          </header>
          <section className="card">
            <div className="actions">
              <Link to="/customers" className="button-link">
                Go To Customer Inquiry
              </Link>
            </div>
          </section>
        </section>
      )
    }

    return this.props.children
  }
}

const featureLinks: SiteLink[] = [
  { to: '/customers', label: 'Customer Inquiry', code: 'INQCUST' },
  { to: '/customers/create', label: 'Create Customer', code: 'CRECUST' },
  { to: '/accounts', label: 'Account Inquiry', code: 'INQACC' },
  { to: '/customer-accounts', label: 'Customer Accounts', code: 'INQACCCU' },
  { to: '/transactions', label: 'Transaction Inquiry', code: 'INQTRAN' },
  { to: '/statements', label: 'Statement Inquiry', code: 'INQSTMT' },
]

const infoLinks: SiteLink[] = [
  { to: '/about', label: 'About Us' },
  { to: '/license', label: 'License' },
]

function renderNavLink(item: SiteLink) {
  return (
    <NavLink key={item.to} to={item.to} end className={({ isActive }) => `site-nav-link${isActive ? ' active' : ''}`}>
      {item.code ? <span className="site-nav-code">{item.code}</span> : null}
      <span>{item.label}</span>
    </NavLink>
  )
}

function SiteLayout() {
  const year = new Date().getFullYear()

  return (
    <div className="site-shell">
      <header className="site-header" aria-label="Top header">
        <div>
          <p className="site-kicker">Mainframe Modernization</p>
          <h1>Bank of Z Operations Console</h1>
          <p className="site-subtitle">Unified workflows for customer and account servicing teams.</p>
        </div>
        <div className="user-chip" aria-label="Signed in user">
          <span className="user-avatar" aria-hidden="true">
            BO
          </span>
          <div>
            <p className="user-name">Bank Ops User</p>
            <p className="user-role">Service Specialist</p>
          </div>
        </div>
      </header>

      <div className="site-body">
        <aside className="site-sidebar" aria-label="Primary navigation">
          <p className="site-sidebar-title">Core Functions</p>
          <nav className="site-nav">{featureLinks.map(renderNavLink)}</nav>
          <p className="site-sidebar-title">Information</p>
          <nav className="site-nav">{infoLinks.map(renderNavLink)}</nav>
        </aside>

        <main className="site-content">
          <RouteErrorBoundary>
            <Outlet />
          </RouteErrorBoundary>
        </main>
      </div>

      <footer className="site-footer">
        <p>Bank of Z Modernization Program</p>
        <p>© {year} Bank of Z. Internal Demonstration Build.</p>
      </footer>
    </div>
  )
}

function LandingPage() {
  return (
    <section className="page home-page" aria-labelledby="landing-heading">
      <header className="page-header">
        <p className="pill">WELCOME</p>
        <h2 id="landing-heading">A Better Front Door For Modernized Banking Flows</h2>
        <p>Pick a workflow from the navigation to begin customer or account operations.</p>
      </header>

      <section className="card" aria-labelledby="quick-start-heading">
        <h3 id="quick-start-heading">Quick Start</h3>
        <div className="home-grid">
          <article className="home-tile">
            <h4>INQCUST</h4>
            <p>Search a customer by sort code and customer number, including RANDOM and LATEST lookup values.</p>
            <NavLink to="/customers" className="text-link">
              Open Customer Inquiry
            </NavLink>
          </article>

          <article className="home-tile">
            <h4>CRECUST</h4>
            <p>Create new customer records with legacy-compatible validations and return mapping.</p>
            <NavLink to="/customers/create" className="text-link">
              Open Customer Create
            </NavLink>
          </article>

          <article className="home-tile">
            <h4>INQACC / INQACCCU</h4>
            <p>Review account details and customer-account relationships for service and support journeys.</p>
            <div className="home-inline-links">
              <NavLink to="/accounts" className="text-link">
                Account Inquiry
              </NavLink>
              <NavLink to="/customer-accounts" className="text-link">
                Customer Accounts
              </NavLink>
            </div>
          </article>

          <article className="home-tile">
            <h4>INQTRAN</h4>
            <p>Retrieve read-only transaction history with optional date boundaries and pagination controls.</p>
            <NavLink to="/transactions" className="text-link">
              Open Transaction Inquiry
            </NavLink>
          </article>

          <article className="home-tile">
            <h4>INQSTMT</h4>
            <p>Retrieve monthly account statements with summary totals and period transaction entries.</p>
            <NavLink to="/statements" className="text-link">
              Open Statement Inquiry
            </NavLink>
          </article>
        </div>
      </section>
    </section>
  )
}

function AboutPage() {
  return (
    <section className="page" aria-labelledby="about-heading">
      <header className="page-header">
        <p className="pill">ABOUT</p>
        <h2 id="about-heading">About This Program</h2>
        <p>
          This portal demonstrates modernization of legacy mainframe workflows into a Spring Boot and React architecture
          while preserving legacy behavior and status semantics.
        </p>
      </header>

      <section className="card" aria-labelledby="about-details-heading">
        <h3 id="about-details-heading">Program Goals</h3>
        <ul>
          <li>Preserve core legacy contracts while improving maintainability and developer speed.</li>
          <li>Provide clear, consistent user experience across all implemented banking features.</li>
          <li>Keep testing and traceability aligned with specification artifacts for repeatable delivery.</li>
        </ul>
      </section>
    </section>
  )
}

function LicensePage() {
  return (
    <section className="page" aria-labelledby="license-heading">
      <header className="page-header">
        <p className="pill">LICENSE</p>
        <h2 id="license-heading">License And Usage</h2>
        <p>This project follows the repository LICENSE terms and is intended for demonstration and internal learning.</p>
      </header>

      <section className="card" aria-labelledby="license-summary-heading">
        <h3 id="license-summary-heading">Summary</h3>
        <p>
          Source code and associated materials are distributed under the repository license. Review the root LICENSE file
          for complete legal terms, permissions, and limitations.
        </p>
      </section>
    </section>
  )
}

function App() {
  return (
    <Routes>
      <Route path="/" element={<SiteLayout />}>
        <Route index element={<LandingPage />} />
        <Route path="customers" element={<CustomerInquiryPage />} />
        <Route path="customers/:sortCode/:customerNumber/edit" element={<CustomerUpdatePage />} />
        <Route path="customers/create" element={<CustomerCreatePage />} />
        <Route path="accounts" element={<AccountInquiryPage />} />
        <Route path="customer-accounts" element={<CustomerAccountInquiryPage />} />
        <Route path="transactions" element={<TransactionInquiryPage />} />
        <Route path="statements" element={<StatementInquiryPage />} />
        <Route path="transactions/:sortCode/:accountNumber/:date/:time/:reference" element={<TransactionDetailPage />} />
        <Route path="about" element={<AboutPage />} />
        <Route path="license" element={<LicensePage />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  )
}

export default App