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
        self.setFont("Helvetica", 9)
        self.setFillColor(colors.HexColor("#718096"))
        
        # Header (pages > 1)
        if self._pageNumber > 1:
            self.drawString(54, 750, "AI-ChainID: Integrated Platform Study — Asset Lifecycle & Zero-Trust Revocation")
            self.setStrokeColor(colors.HexColor("#CBD5E1"))
            self.setLineWidth(0.5)
            self.line(54, 742, 558, 742)
            
        # Footer
        footer_text = f"Page {self._pageNumber} of {page_count}"
        self.drawRightString(558, 35, footer_text)
        self.drawString(54, 35, "CONFIDENTIAL & PROPRIETARY — ZERO-TRUST COMPREHENSIVE STUDY")
        self.setStrokeColor(colors.HexColor("#CBD5E1"))
        self.setLineWidth(0.5)
        self.line(54, 48, 558, 48)
        self.restoreState()

def build_pdf():
    target_path = r"c:\Users\sonaw\Downloads\ai-chainid\AI-ChainID_Full_Platform_Study_Report.pdf"
    doc = SimpleDocTemplate(
        target_path,
        pagesize=letter,
        leftMargin=54,
        rightMargin=54,
        topMargin=54,
        bottomMargin=54
    )

    styles = getSampleStyleSheet()
    
    # Custom colors & typography
    primary_color = colors.HexColor("#0F172A") # Slate 900
    accent_color = colors.HexColor("#2563EB")  # Blue 600
    teal_color = colors.HexColor("#0D9488")    # Teal 600
    dark_text = colors.HexColor("#1E293B")
    
    title_style = ParagraphStyle(
        'DocTitle',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=20,
        leading=24,
        textColor=primary_color,
        spaceAfter=3
    )
    
    subtitle_style = ParagraphStyle(
        'DocSubTitle',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=10,
        leading=14,
        textColor=colors.HexColor("#475569"),
        spaceAfter=10
    )

    h1_style = ParagraphStyle(
        'Header1',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=12,
        leading=16,
        textColor=accent_color,
        spaceBefore=8,
        spaceAfter=4,
        keepWithNext=True
    )

    body_style = ParagraphStyle(
        'BodyDark',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=8,
        leading=11.5,
        textColor=dark_text,
        spaceAfter=4
    )

    bullet_style = ParagraphStyle(
        'BulletText',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=8,
        leading=11.5,
        textColor=dark_text,
        leftIndent=10,
        firstLineIndent=-10,
        spaceAfter=2
    )

    callout_style = ParagraphStyle(
        'CalloutText',
        parent=styles['Normal'],
        fontName='Helvetica-Oblique',
        fontSize=8,
        leading=11.5,
        textColor=colors.HexColor("#1E3A8A")
    )

    table_header_style = ParagraphStyle(
        'TH',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=7.5,
        leading=10,
        textColor=colors.white
    )

    table_body_style = ParagraphStyle(
        'TB',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=7,
        leading=9.5,
        textColor=dark_text
    )

    story = []

    # Title block
    story.append(Paragraph("AI-ChainID: Comprehensive Platform Study & Defense Guide", title_style))
    story.append(Paragraph("Zero-Trust Architecture | Hybrid Machine Learning | Block Chaining | Asset Custody | Instant Revocation", subtitle_style))
    story.append(HRFlowable(width="100%", thickness=1.5, color=accent_color, spaceAfter=8))

    # 1. Core Architecture
    story.append(Paragraph("1. Executive Summary & Zero-Trust Foundation", h1_style))
    story.append(Paragraph(
        "AI-ChainID solves the core vulnerabilities of traditional IAM (privilege creep, static credential theft, and unmonitored lateral movement). "
        "It couples <b>Dual AI Engines</b> (Least-Privilege Recommender & Continuous Anomaly Detector) with an active <b>Cryptographically Chained Ledger</b>. "
        "Access is evaluated dynamically based on context, and every lifecycle event is immutably anchored.",
        body_style
    ))

    # 2. Asset Management & Revoke Access Deep Dive
    story.append(Paragraph("2. Asset Management & Instant Revocation Architecture", h1_style))
    
    special_features_data = [
        [Paragraph("Feature Area", table_header_style), Paragraph("Technical Workflow & Implementation", table_header_style), Paragraph("Blockchain & Zero-Trust Impact", table_header_style)],
        [
            Paragraph("<b>Asset Lifecycle & Custody</b>", table_body_style),
            Paragraph("Manages physical & digital corporate hardware (YubiKeys, HSMs, Laptops) through finite state transitions: <code>AVAILABLE &rarr; ASSIGNED &rarr; RETURNED &rarr; RETIRED</code>. Double-assignment is barred at database level.", table_body_style),
            Paragraph("<b>Immutable Hardware Provenance:</b> Every assignment and return is SHA-256 hashed and mined onto the ledger. Guarantees non-repudiation—employees/admins cannot dispute physical custody records.", table_body_style)
        ],
        [
            Paragraph("<b>Instant Access Revocation</b>", table_body_style),
            Paragraph("Admins trigger <code>POST /api/access-requests/{id}/revoke</code> with audited justification. Instantly updates request status from <code>APPROVED &rarr; REVOKED</code> and appends a block.", table_body_style),
            Paragraph("<b>Cryptographic Edge Kill Switch:</b> The edge gate (<code>verifyEdgeAccess</code>) scans for <code>ACCESS_REVOKED</code> blocks. Access is terminated at the perimeter in microseconds, neutralizing replayed tokens.", table_body_style)
        ],
        [
            Paragraph("<b>Break-Glass Protocol</b>", table_body_style),
            Paragraph("Emergency access overrides normal approvals. Requires explicit reason and records administrator ID in both SQL and an emergency block.", table_body_style),
            Paragraph("<b>Cryptographic Signature:</b> Generates a dedicated block with policy signature <code>EMERGENCY_OVERRIDE_ADMIN_id_BLOCK_n</code> for forensic non-repudiation.", table_body_style)
        ]
    ]

    t_spec = Table(special_features_data, colWidths=[90, 205, 209])
    t_spec.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), primary_color),
        ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
        ('VALIGN', (0, 0), (-1, -1), 'TOP'),
        ('TEXTCOLOR', (0, 0), (-1, 0), colors.white),
        ('ROWBACKGROUNDS', (0, 1), (-1, -1), [colors.HexColor("#F8FAFC"), colors.white]),
        ('GRID', (0, 0), (-1, -1), 0.5, colors.HexColor("#E2E8F0")),
        ('TOPPADDING', (0, 0), (-1, -1), 3),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 3),
    ]))
    story.append(t_spec)
    story.append(Spacer(1, 6))

    # 3. Blockchain Upgrade (Old vs New)
    story.append(Paragraph("3. Blockchain Engine Upgrade: Old vs. New Ledger", h1_style))
    bc_comparison = [
        [Paragraph("Dimension", table_header_style), Paragraph("Old Deprecated Feature", table_header_style), Paragraph("New Upgraded Blockchain Engine", table_header_style)],
        [
            Paragraph("<b>Data Structure</b>", table_body_style),
            Paragraph("Flat mock hashes saved in isolation with zero mathematical linkage between records.", table_body_style),
            Paragraph("<b>Sequential Block Chaining:</b> Deterministic block linking: <code>txHash = SHA256(previousHash : recordHash : blockNumber)</code> starting from Genesis <code>0x000...000</code>.", table_body_style)
        ],
        [
            Paragraph("<b>Tamper Audit</b>", table_body_style),
            Paragraph("None. Direct SQL updates by rogue admins went completely undetected.", table_body_style),
            Paragraph("<b>Active Chain Auditor (<code>/verify-ledger</code>):</b> Traverses all blocks; mathematically flags any modified byte as a cryptographic ledger breach.", table_body_style)
        ],
        [
            Paragraph("<b>Edge Gate</b>", table_body_style),
            Paragraph("Blind trust in central SQL status: <code>status == 'APPROVED'</code>.", table_body_style),
            Paragraph("<b>Zero-Trust Edge Gate (<code>/verify-edge</code>):</b> Rejects requests if SQL says APPROVED but no matching block exists (stops direct SQL injection attacks).", table_body_style)
        ]
    ]

    t_bc = Table(bc_comparison, colWidths=[80, 160, 264])
    t_bc.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), accent_color),
        ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
        ('VALIGN', (0, 0), (-1, -1), 'TOP'),
        ('TEXTCOLOR', (0, 0), (-1, 0), colors.white),
        ('ROWBACKGROUNDS', (0, 1), (-1, -1), [colors.HexColor("#F8FAFC"), colors.white]),
        ('GRID', (0, 0), (-1, -1), 0.5, colors.HexColor("#E2E8F0")),
        ('TOPPADDING', (0, 0), (-1, -1), 3),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 3),
    ]))
    story.append(t_bc)

    story.append(PageBreak())

    # 4. Decentralized Identity & Microservices
    story.append(Paragraph("4. Decentralized Identity (DID) & Microservice Architecture", h1_style))
    story.append(Paragraph(
        "<b>W3C DID Implementation:</b> Identifiers follow <code>did:chainid:&lt;uuid&gt;</code> paired with 2048-bit RSA/ECC public key infrastructure.<br/>"
        "<b>Why DID Matters:</b> Replaces phishable passwords with asymmetric key signing. Edge gateways authenticate user requests directly against the public key without pinging the central user DB.",
        body_style
    ))

    arch_data = [
        [Paragraph("Tier", table_header_style), Paragraph("Technology", table_header_style), Paragraph("Core Responsibilities", table_header_style)],
        [
            Paragraph("<b>Gateway Core</b>", table_body_style),
            Paragraph("Java 17 / Spring Boot (:8080)", table_body_style),
            Paragraph("RBAC, JWT Security, Asset Management, Access Revocations, Audit Services, and Blockchain Ledger.", table_body_style)
        ],
        [
            Paragraph("<b>Permission AI</b>", table_body_style),
            Paragraph("FastAPI / Scikit-Learn (:8000)", table_body_style),
            Paragraph("RandomForestClassifier recommending ALLOW / REVIEW / DENY with confidence and explainability.", table_body_style)
        ],
        [
            Paragraph("<b>Risk & Anomaly AI</b>", table_body_style),
            Paragraph("RF Regressor + IsolationForest", table_body_style),
            Paragraph("Continuous risk scorer (0-100) + unsupervised outlier detection for novel zero-day insider threats.", table_body_style)
        ]
    ]
    t_arch = Table(arch_data, colWidths=[80, 150, 274])
    t_arch.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), teal_color),
        ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
        ('VALIGN', (0, 0), (-1, -1), 'TOP'),
        ('TEXTCOLOR', (0, 0), (-1, 0), colors.white),
        ('ROWBACKGROUNDS', (0, 1), (-1, -1), [colors.HexColor("#F8FAFC"), colors.white]),
        ('GRID', (0, 0), (-1, -1), 0.5, colors.HexColor("#E2E8F0")),
        ('TOPPADDING', (0, 0), (-1, -1), 3),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 3),
    ]))
    story.append(t_arch)
    story.append(Spacer(1, 6))

    # 5. Judge Defense Playbook
    story.append(Paragraph("5. Hackathon Gap Defense Playbook (How to Counter Judges)", h1_style))
    gaps_table_data = [
        [Paragraph("Perceived Gap", table_header_style), Paragraph("Judge Question", table_header_style), Paragraph("Winning Counter-Defense", table_header_style)],
        [
            Paragraph("<b>Private Ledger</b>", table_body_style),
            Paragraph("<i>'Why not public Ethereum?'</i>", table_body_style),
            Paragraph("<b>GDPR/HIPAA Compliance:</b> Public chains leak employee metadata and cannot honor 'Right to be Forgotten'. We use a <b>Private Consortium Ledger</b> with Merkle root anchoring.", table_body_style)
        ],
        [
            Paragraph("<b>Synthetic ML Logs</b>", table_body_style),
            Paragraph("<i>'Model trained on synthetic data.'</i>", table_body_style),
            Paragraph("<b>Cold-Start Solution:</b> Solves the Day-0 enterprise deployment problem. Our <b>Isolation Forest</b> is unsupervised and detects real-world novel anomalies from live telemetry.", table_body_style)
        ],
        [
            Paragraph("<b>Passive Remediation</b>", table_body_style),
            Paragraph("<i>'Why not auto-kill high-risk tokens?'</i>", table_body_style),
            Paragraph("<b>Preventing Operational Self-DoS:</b> Hard auto-bans on 1-2% false positives disrupt critical operations. We enforce <b>NIST SP 800-207</b>: Step-Up MFA and Break-Glass workflows.", table_body_style)
        ]
    ]
    t_gaps = Table(gaps_table_data, colWidths=[90, 120, 294])
    t_gaps.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), primary_color),
        ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
        ('VALIGN', (0, 0), (-1, -1), 'TOP'),
        ('TEXTCOLOR', (0, 0), (-1, 0), colors.white),
        ('ROWBACKGROUNDS', (0, 1), (-1, -1), [colors.HexColor("#F8FAFC"), colors.white]),
        ('GRID', (0, 0), (-1, -1), 0.5, colors.HexColor("#E2E8F0")),
        ('TOPPADDING', (0, 0), (-1, -1), 3),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 3),
    ]))
    story.append(t_gaps)
    story.append(Spacer(1, 8))

    # Callout
    box_data = [[
        Paragraph(
            "<b>The Winning 30-Second Elevator Pitch:</b><br/>"
            "<i>'AI and Blockchain are opposing yet complementary forces in Zero-Trust security. AI provides the dynamic, contextual intelligence to detect suspicious context in real time. The Blockchain provides deterministic mathematical immutability, ensuring that even if a database administrator or root attacker modifies access records, the edge verification gate catches the cryptographic mismatch and halts unauthorized access.'</i>",
            callout_style
        )
    ]]
    t_box = Table(box_data, colWidths=[504])
    t_box.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, -1), colors.HexColor("#EFF6FF")),
        ('BOX', (0, 0), (-1, -1), 1, colors.HexColor("#3B82F6")),
        ('TOPPADDING', (0, 0), (-1, -1), 6),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 6),
        ('LEFTPADDING', (0, 0), (-1, -1), 10),
        ('RIGHTPADDING', (0, 0), (-1, -1), 10),
    ]))
    story.append(t_box)

    doc.build(story, canvasmaker=NumberedCanvas)
    print("SUCCESS: Full Platform PDF generated at", target_path)

if __name__ == "__main__":
    build_pdf()
