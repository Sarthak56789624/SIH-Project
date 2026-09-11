import os
from reportlab.lib.pagesizes import letter
from reportlab.lib import colors
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak, KeepTogether, HRFlowable
)
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.pdfgen import canvas

class NumberedCanvas(canvas.Canvas):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self._saved_page_states = []

    def showPage(self):
        self._saved_page_states.append(dict(self.__dict__))
        self._startPage()

    def save(self):
        num_pages = len(self._saved_page_states)
        for state in self._saved_page_states:
            self.__dict__.update(state)
            self.draw_page_decorations(num_pages)
            super().showPage()
        super().save()

    def draw_page_decorations(self, page_count):
        self.saveState()
        self.setFont("Helvetica", 8)
        self.setFillColor(colors.HexColor("#64748B"))
        
        if self._pageNumber > 1:
            self.drawString(54, 752, "AI-ChainID: Blockchain Architecture & Cryptographic Zero-Trust Defense")
            self.drawRightString(558, 752, "Enterprise Summary Report")
            self.setStrokeColor(colors.HexColor("#CBD5E1"))
            self.setLineWidth(0.5)
            self.line(54, 744, 558, 744)
            
        footer_text = f"Page {self._pageNumber} of {page_count}"
        self.drawRightString(558, 32, footer_text)
        self.drawString(54, 32, "CONFIDENTIAL & TECHNICAL ARCHITECTURE STUDY — AI-CHAINID PLATFORM")
        self.setStrokeColor(colors.HexColor("#CBD5E1"))
        self.setLineWidth(0.5)
        self.line(54, 44, 558, 44)
        self.restoreState()

def build_pdf():
    target_path = r"c:\Users\sonaw\Desktop\Testing\ai-chainid\AI-ChainID_Blockchain_Architecture_Summary_Report.pdf"
    doc = SimpleDocTemplate(
        target_path,
        pagesize=letter,
        leftMargin=54,
        rightMargin=54,
        topMargin=54,
        bottomMargin=54
    )

    styles = getSampleStyleSheet()
    
    c_primary = colors.HexColor("#0B0F19")
    c_indigo = colors.HexColor("#4F46E5")
    c_teal = colors.HexColor("#0D9488")
    c_dark_text = colors.HexColor("#1E293B")
    c_muted_text = colors.HexColor("#475569")
    c_light_bg = colors.HexColor("#F8FAFC")
    c_card_border = colors.HexColor("#E2E8F0")

    title_style = ParagraphStyle(
        'DocTitle', parent=styles['Normal'],
        fontName='Helvetica-Bold', fontSize=17, leading=21, textColor=c_primary, spaceAfter=2
    )
    
    subtitle_style = ParagraphStyle(
        'DocSubTitle', parent=styles['Normal'],
        fontName='Helvetica', fontSize=9, leading=13, textColor=c_muted_text, spaceAfter=6
    )

    sec_header_style = ParagraphStyle(
        'SecHeader', parent=styles['Normal'],
        fontName='Helvetica-Bold', fontSize=10.5, leading=14, textColor=c_indigo, spaceBefore=6, spaceAfter=2, keepWithNext=True
    )

    body_style = ParagraphStyle(
        'BodyMain', parent=styles['Normal'],
        fontName='Helvetica', fontSize=7.6, leading=10.8, textColor=c_dark_text, spaceAfter=3
    )

    callout_style = ParagraphStyle(
        'CalloutText', parent=styles['Normal'],
        fontName='Helvetica-Oblique', fontSize=7.5, leading=10.8, textColor=colors.HexColor("#1E3A8A")
    )

    th_style = ParagraphStyle(
        'TableHeader', parent=styles['Normal'],
        fontName='Helvetica-Bold', fontSize=7.2, leading=9.2, textColor=colors.white
    )

    tb_style = ParagraphStyle(
        'TableBody', parent=styles['Normal'],
        fontName='Helvetica', fontSize=6.8, leading=8.8, textColor=c_dark_text
    )

    story = []

    # PAGE 1
    story.append(Paragraph("AI-ChainID: Blockchain Architecture & Cryptographic Defense", title_style))
    story.append(Paragraph("Full Technical Summary of Ledger Implementation, 3-Key Quorum, Edge Gate Simulator & GDPR Off-Chain Vault", subtitle_style))
    story.append(HRFlowable(width="100%", thickness=1.5, color=c_indigo, spaceAfter=5))

    story.append(Paragraph("1. Executive Philosophy & The Problem Solved", sec_header_style))
    story.append(Paragraph(
        "<b>The Problem with Traditional IAM:</b> In Active Directory, Okta, or conventional cloud IAM, centralized databases (e.g. MySQL) represent a single point of failure. "
        "A rogue DBA or root attacker can execute <code>UPDATE access_requests SET status = 'APPROVED' WHERE user_id = 99;</code>, silently granting permissions without trace. "
        "Additionally, post-authentication trust allows compromised sessions to persist indefinitely, and hardware asset custody cannot be provably verified.",
        body_style
    ))
    story.append(Paragraph(
        "<b>The AI-ChainID Philosophy:</b> <i>'A valid identity never implies unconditional access'</i> and <i>'SQL is merely an operational cache — the Blockchain Ledger is the single source of cryptographic truth.'</i> "
        "AI provides real-time behavioral intelligence, while the Blockchain guarantees deterministic mathematical immutability.",
        body_style
    ))

    story.append(Paragraph("2. Technical Blockchain Implementation & Hash Chaining", sec_header_style))
    story.append(Paragraph(
        "AI-ChainID employs an enterprise consortium ledger using deterministic SHA-256 block linking: "
        "<code>TxHash = SHA256(prevHash : entityType : entityId : blockNumber : timestamp)</code>. "
        "Block #1 (Genesis) starts at <code>0x000...000</code>. Each subsequent block seals the previous block's hash. Tampering with any byte invalidates all downstream blocks.",
        body_style
    ))

    t_blocks_data = [
        [Paragraph("Lifecycle Event Type", th_style), Paragraph("Trigger & Cryptographic Payload", th_style), Paragraph("Zero-Trust Security Guarantee", th_style)],
        [
            Paragraph("<b>DID_KEYPAIR_ANCHORED</b>", tb_style),
            Paragraph("Mints W3C Decentralized ID (<code>did:org:...</code>) with Ed25519 public key.", tb_style),
            Paragraph("Replaces phishable passwords with asymmetric cryptographic identity.", tb_style)
        ],
        [
            Paragraph("<b>PII_VAULT_COMMIT</b>", tb_style),
            Paragraph("SHA-256 commitment of off-chain AES-256-GCM encrypted personal data.", tb_style),
            Paragraph("Guarantees GDPR compliance; sensitive PII is never exposed on the ledger.", tb_style)
        ],
        [
            Paragraph("<b>ACCESS_APPROVED</b>", tb_style),
            Paragraph("Minting of approved access permissions and verifiable credentials.", tb_style),
            Paragraph("Provides non-repudiable audit proof that manager sign-off occurred.", tb_style)
        ],
        [
            Paragraph("<b>ASSET_ASSIGNED</b>", tb_style),
            Paragraph("Maps physical hardware tag (ThinkPad, YubiKey) to employee DID.", tb_style),
            Paragraph("Hardware custody provenance; employees cannot dispute device possession.", tb_style)
        ],
        [
            Paragraph("<b>BREAK_GLASS_TRIGGERED</b>", tb_style),
            Paragraph("Emergency AI bypass signed with <code>EMERGENCY_OVERRIDE_ADMIN_1</code>.", tb_style),
            Paragraph("Prevents operational self-DoS during outages while creating indelible audit logs.", tb_style)
        ],
        [
            Paragraph("<b>ACCESS_REVOKED</b>", tb_style),
            Paragraph("Immediate status cutoff block appended to ledger.", tb_style),
            Paragraph("Edge gates terminate replayed tokens across the network in milliseconds.", tb_style)
        ]
    ]
    t_blocks = Table(t_blocks_data, colWidths=[110, 195, 199])
    t_blocks.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), c_primary),
        ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
        ('VALIGN', (0, 0), (-1, -1), 'TOP'),
        ('ROWBACKGROUNDS', (0, 1), (-1, -1), [c_light_bg, colors.white]),
        ('GRID', (0, 0), (-1, -1), 0.5, c_card_border),
        ('TOPPADDING', (0, 0), (-1, -1), 2),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 2),
    ]))
    story.append(t_blocks)
    story.append(Spacer(1, 4))

    story.append(Paragraph("3. The 3-Key Defense Quorum (Preventing Stolen Admin Hacks)", sec_header_style))
    story.append(Paragraph(
        "If an attacker steals the Super Admin credentials, they cannot silently grant access. AI-ChainID enforces a <b>3-Key Consensus Quorum</b>: "
        "<b>(1) Key 1: AI Risk Engine:</b> Evaluates 15 contextual telemetry features (3:00 AM? Foreign IP? Unknown MAC?). If Risk > 70, AI triggers an autonomous policy block. "
        "<b>(2) Key 2: Central Admin Key:</b> Governance sign-off; overriding AI forces Break-Glass justification and public on-chain signature. "
        "<b>(3) Key 3: Sub-Admin Key:</b> Department leads (Engineering, Biomedical, Infrastructure) control isolated scopes, preventing unchecked cross-department lateral elevation.",
        body_style
    ))

    story.append(PageBreak())

    # PAGE 2
    story.append(Paragraph("4. Zero-Trust Edge Resource Gate & Active Chain Auditor", sec_header_style))
    story.append(Paragraph(
        "<b>Perimeter Edge Resource Gate (<code>/api/blockchain/verify-edge-access</code>):</b> "
        "High-security endpoints (GPU servers, bastions, NFC door locks) do not trust MySQL. Before unlocking resources, the edge gate queries the blockchain directly: "
        "if MySQL claims <code>APPROVED</code> but no matching on-chain block exists, it logs <code>ZERO-TRUST VIOLATION: Database tampering detected!</code> and denies access immediately.",
        body_style
    ))
    story.append(Paragraph(
        "<b>Active Chain Auditor (<code>/api/blockchain/verify-ledger</code>):</b> "
        "Traverses all blocks from Genesis to Block N, re-computing SHA-256 Merkle hashes. If any database byte was manually modified, the hash link breaks and alerts administrators.",
        body_style
    ))

    story.append(Paragraph("5. Threat Modeling: Attack Scenarios vs. AI-ChainID Defenses", sec_header_style))
    t_threats_data = [
        [Paragraph("Attack Vector", th_style), Paragraph("Traditional IAM Vulnerability", th_style), Paragraph("AI-ChainID Cryptographic Defense", th_style)],
        [
            Paragraph("<b>Direct SQL Update</b><br/>(<code>status='APPROVED'</code>)", tb_style),
            Paragraph("Access granted silently without audit trail.", tb_style),
            Paragraph("<b>BLOCKED:</b> Edge Gate checks blockchain consensus; missing block triggers <code>TAMPER_DETECTED</code> and halts access.", tb_style)
        ],
        [
            Paragraph("<b>Stolen Admin Password</b>", tb_style),
            Paragraph("Attacker gains absolute, unrestricted control.", tb_style),
            Paragraph("<b>BLOCKED:</b> AI behavioral regressor detects contextual anomaly; Break-Glass leaves an indelible on-chain signature.", tb_style)
        ],
        [
            Paragraph("<b>Audit Log Deletion</b>", tb_style),
            Paragraph("Attacker clears log tables to conceal intrusion.", tb_style),
            Paragraph("<b>BLOCKED:</b> Chain Auditor re-computes parent hashes from Genesis; missing records break the sequential hash chain.", tb_style)
        ],
        [
            Paragraph("<b>Privilege Accumulation</b><br/>(Department Transfer)", tb_style),
            Paragraph("Legacy permissions accumulate indefinitely.", tb_style),
            Paragraph("<b>BLOCKED:</b> Dynamic Scope Re-Analysis calculates the delta and mints an <code>Ed25519-ScopeReMint</code> block, cutting old scopes.", tb_style)
        ]
    ]
    t_threats = Table(t_threats_data, colWidths=[105, 155, 244])
    t_threats.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), c_indigo),
        ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
        ('VALIGN', (0, 0), (-1, -1), 'TOP'),
        ('ROWBACKGROUNDS', (0, 1), (-1, -1), [c_light_bg, colors.white]),
        ('GRID', (0, 0), (-1, -1), 0.5, c_card_border),
        ('TOPPADDING', (0, 0), (-1, -1), 2),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 2),
    ]))
    story.append(t_threats)
    story.append(Spacer(1, 4))

    story.append(Paragraph("6. Regulatory Privacy (GDPR & HIPAA) Dual-Storage Architecture", sec_header_style))
    story.append(Paragraph(
        "Public blockchains violate GDPR's 'Right to be Forgotten' because data cannot be removed. AI-ChainID solves this through <b>Dual-Storage Separation</b>: "
        "Sensitive PII (names, emails, addresses) is encrypted with <b>AES-256-GCM</b> and stored strictly off-chain. Only pseudonymous DIDs (<code>did:org:...</code>) and cryptographic commitment hashes are anchored on-chain. "
        "Upon offboarding, the off-chain decryption key and record are destroyed, achieving 100% compliance while preserving ledger audit history.",
        body_style
    ))

    # Elevator pitch box
    box_data = [[
        Paragraph(
            "<b>The Winning 30-Second Elevator Pitch:</b><br/>"
            "<i>'AI and Blockchain are opposing yet complementary forces in Zero-Trust security. AI provides the dynamic, contextual intelligence to evaluate behavioral anomalies and least-privilege scoping in real time. The Blockchain provides deterministic mathematical immutability, ensuring that even if a database administrator or root attacker manipulates access tables, the perimeter verification gate catches the cryptographic hash mismatch and stops unauthorized intrusion.'</i>",
            callout_style
        )
    ]]
    t_box = Table(box_data, colWidths=[504])
    t_box.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, -1), colors.HexColor("#EFF6FF")),
        ('BOX', (0, 0), (-1, -1), 1, c_indigo),
        ('TOPPADDING', (0, 0), (-1, -1), 4),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 4),
        ('LEFTPADDING', (0, 0), (-1, -1), 8),
        ('RIGHTPADDING', (0, 0), (-1, -1), 8),
    ]))
    story.append(t_box)

    doc.build(story, canvasmaker=NumberedCanvas)
    print(f"SUCCESS: Generated PDF at {target_path}")

if __name__ == "__main__":
    build_pdf()
