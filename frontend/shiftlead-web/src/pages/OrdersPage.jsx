import Layout from '../components/Layout';

export default function OrdersPage() {
  return (
    <Layout>
      <div className="sl-page">
        <div className="sl-page-header">
          <h1>Aufträge</h1>
          <p>Zugewiesene Aufträge des Schichtleiters · US-SL-007</p>
        </div>

        <div className="panel">
          <div className="empty-state">
            <p style={{ margin: '0 0 8px', fontWeight: 600 }}>Order Service noch nicht verbunden</p>
            <p style={{ margin: 0, fontSize: 14 }}>
              Sobald der Order Service implementiert ist, werden hier die zugewiesenen Aufträge
              mit Status, Zeitraum und Mitarbeiterzuordnung angezeigt.
            </p>
            <p style={{ margin: '12px 0 0', fontSize: 13, color: '#607080' }}>
              Eine optionale Auftrag-ID kann bereits jetzt beim Hinzufügen einer Schicht in der
              Arbeitsplanung hinterlegt werden.
            </p>
          </div>
        </div>
      </div>
    </Layout>
  );
}
